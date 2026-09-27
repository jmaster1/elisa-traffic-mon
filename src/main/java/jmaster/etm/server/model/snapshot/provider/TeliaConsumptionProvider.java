package jmaster.etm.server.model.snapshot.provider;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jmaster.etm.server.model.PhoneOwner;
import jmaster.etm.server.model.snapshot.FetchConfig;
import jmaster.etm.server.model.snapshot.http.HttpRequestData;
import jmaster.etm.server.model.snapshot.http.HttpResponseData;
import jmaster.etm.server.model.snapshot.http.LocalHttpExecutor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TeliaConsumptionProvider implements ConsumptionProvider {

    private static final String PROVIDER_HOST = "telia.ee";

    private static final Pattern PAGE_PARAMETER = Pattern.compile("([?&]page=)\\d+");

    @Override
    public String getName() {
        return "Telia";
    }

    @Override
    public boolean supports(String source) {
        return source != null && source.contains(PROVIDER_HOST);
    }

    @Override
    public FetchConfig parseFetchConfig(String fetch, FetchConfig currentConfig) {
        FetchConfig fetchConfig = currentConfig == null ? new FetchConfig() : currentConfig;
        try {
            fetchConfig.uri = StringUtils.substringBetween(fetch, "fetch(\"", "\",");
            Map<String, String> headers = fetchConfig.headers = new HashMap<>();
            int jsonBegin = fetch.indexOf('{');
            int jsonEnd = fetch.lastIndexOf('}');
            String json = fetch.substring(jsonBegin - 1, jsonEnd + 1);
            JsonObject obj = new Gson().fromJson(json, JsonObject.class);
            JsonObject headersObj = obj.get("headers").getAsJsonObject();
            for (String key : headersObj.keySet()) {
                headers.put(key, headersObj.get(key).getAsString());
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Bad Telia fetch data", ex);
        }
        return fetchConfig;
    }

    @Override
    public Map<PhoneOwner, BigDecimal> queryUsedGbByOwner(FetchConfig fetchConfig) {
        Map<PhoneOwner, String> productIdsByOwner = getProductIdsByOwner(fetchConfig);
        Map<PhoneOwner, BigDecimal> result = new EnumMap<>(PhoneOwner.class);
        for (Map.Entry<PhoneOwner, String> entry : productIdsByOwner.entrySet()) {
            JsonObject response = queryJson(createProductRequest(fetchConfig, entry.getValue()));
            result.put(entry.getKey(), parseUsedGb(response));
        }
        return result;
    }

    private Map<PhoneOwner, String> getProductIdsByOwner(FetchConfig fetchConfig) {
        Map<PhoneOwner, String> result = new EnumMap<>(PhoneOwner.class);
        int page = 1;
        boolean hasNextPage;
        do {
            JsonObject response = queryJson(createListRequest(fetchConfig, page));
            ensureAccessAllowed(response);

            JsonObject data = response.getAsJsonObject("data");
            for (JsonElement element : data.getAsJsonArray("items")) {
                JsonObject productData = element.getAsJsonObject().getAsJsonObject("productData");
                PhoneOwner phoneOwner = getPhoneOwner(productData);
                if (phoneOwner != null) {
                    result.put(phoneOwner, productData.get("productId").getAsString()
                            + "_" + productData.get("origin").getAsString());
                }
            }
            hasNextPage = data.get("hasNextPage").getAsBoolean();
            page++;
        } while (hasNextPage);

        if (result.isEmpty()) {
            throw new IllegalArgumentException("Telia response contains no known mobile numbers");
        }
        return result;
    }

    private HttpRequestData createListRequest(FetchConfig fetchConfig, int page) {
        HttpRequestData request = new HttpRequestData();
        request.url = replacePage(fetchConfig.uri, page);
        copyHeaders(fetchConfig, request);
        return request;
    }

    private HttpRequestData createProductRequest(FetchConfig fetchConfig, String productId) {
        HttpRequestData request = new HttpRequestData();
        request.url = "https://iseteenindus.telia.ee/myse-frontend/api/v1/products/"
                + productId
                + "/product-details/product_details_mobile_data?pageId="
                + UUID.randomUUID()
                + "&name=mobile_data_usage";
        copyHeaders(fetchConfig, request);
        request.headers.put("Referer", "https://iseteenindus.telia.ee/ru/teenused/"
                + productId + "?from=/teenused/mobiil");
        return request;
    }

    private void copyHeaders(FetchConfig fetchConfig, HttpRequestData request) {
        if (fetchConfig.headers != null) {
            request.headers.putAll(fetchConfig.headers);
        }
        request.method = HttpMethod.GET.name();
    }

    private JsonObject queryJson(HttpRequestData request) {
        Function<HttpRequestData, HttpResponseData> httpQueryExecutor = new LocalHttpExecutor();
        HttpResponseData response = httpQueryExecutor.apply(request);
        response.ensureStatusOk();
        return response.getContentAsJsonObject();
    }

    private BigDecimal parseUsedGb(JsonObject response) {
        ensureAccessAllowed(response);

        JsonObject domesticUsage = response
                .getAsJsonObject("data")
                .getAsJsonObject("domesticUsage");
        String unit = domesticUsage.get("unit").getAsString();
        return toGb(domesticUsage.get("usage").getAsBigDecimal(), unit);
    }

    private void ensureAccessAllowed(JsonObject response) {
        JsonElement errors = response.get("errors");
        if (errors != null
                && !errors.isJsonNull()
                && (!errors.isJsonArray() || errors.getAsJsonArray().size() > 0)) {
            throw new IllegalArgumentException("Telia response contains errors");
        }

        JsonObject accessData = response.getAsJsonObject("accessData");
        String state = accessData == null || accessData.get("state") == null
                ? null
                : accessData.get("state").getAsString();
        if (!"ALLOWED".equals(state)) {
            throw new IllegalArgumentException("Telia access is not allowed: " + state);
        }
    }

    private PhoneOwner getPhoneOwner(JsonObject productData) {
        for (JsonElement number : productData.getAsJsonArray("communicationNumbers")) {
            String value = number.getAsString();
            if (value.startsWith("372")) {
                value = value.substring(3);
            }
            PhoneOwner phoneOwner = PhoneOwner.fromPhone(Long.valueOf(value));
            if (phoneOwner != null) {
                return phoneOwner;
            }
        }
        return null;
    }

    private String replacePage(String uri, int page) {
        Matcher matcher = PAGE_PARAMETER.matcher(uri);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Telia list URL must contain the page parameter");
        }
        return matcher.replaceFirst("$1" + page);
    }

    private BigDecimal toGb(BigDecimal used, String unit) {
        if ("KB".equals(unit)) {
            return used.divide(BigDecimal.valueOf(1024 * 1024));
        }
        if ("MB".equals(unit)) {
            return used.divide(BigDecimal.valueOf(1024));
        }
        if ("GB".equals(unit)) {
            return used;
        }
        throw new IllegalArgumentException("Unexpected Telia used unit: " + unit);
    }
}
