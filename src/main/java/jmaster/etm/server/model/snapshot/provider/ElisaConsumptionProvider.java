package jmaster.etm.server.model.snapshot.provider;

import com.google.gson.Gson;
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
import java.util.function.Function;

@Service
public class ElisaConsumptionProvider implements ConsumptionProvider {

    private static final String PROVIDER_HOST = "elisa.ee";

    @Override
    public String getName() {
        return "Elisa";
    }

    @Override
    public boolean supports(String source) {
        return source != null && source.contains(PROVIDER_HOST);
    }

    @Override
    public FetchConfig parseFetchConfig(String fetch, FetchConfig currentConfig) {
        FetchConfig fetchConfig = currentConfig == null ? new FetchConfig() : currentConfig;
        try {
            fetchConfig.uri = StringUtils
                    .substringBetween(fetch, "fetch(\"", "\",")
                    .replaceAll("\\d*$", "");
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
            throw new IllegalArgumentException("Bad Elisa fetch data", ex);
        }
        return fetchConfig;
    }

    @Override
    public Map<PhoneOwner, BigDecimal> queryUsedGbByOwner(FetchConfig fetchConfig) {
        Map<PhoneOwner, BigDecimal> result = new EnumMap<>(PhoneOwner.class);
        for (PhoneOwner phoneOwner : PhoneOwner.values()) {
            result.put(phoneOwner, queryUsedGb(phoneOwner, fetchConfig));
        }
        return result;
    }

    private BigDecimal queryUsedGb(PhoneOwner phoneOwner, FetchConfig fetchConfig) {
        Function<HttpRequestData, HttpResponseData> httpQueryExecutor = new LocalHttpExecutor();

        HttpRequestData request = new HttpRequestData();
        request.url = buildRequestUrl(fetchConfig.uri, phoneOwner.phoneNr);
        if (fetchConfig.headers != null) {
            request.headers.putAll(fetchConfig.headers);
        }
        request.method = HttpMethod.GET.name();
        HttpResponseData response = httpQueryExecutor.apply(request);

        response.ensureStatusOk();
        JsonObject graph = response.getContentAsJsonObject()
                .getAsJsonArray("internetConsumptionGraphs")
                .get(0)
                .getAsJsonObject();

        BigDecimal used = graph.get("used").getAsBigDecimal();
        String usedUnit = graph.get("usedUnit").getAsString();

        if ("KB".equals(usedUnit)) {
            return used.divide(BigDecimal.valueOf(1024 * 1024));
        }
        if ("MB".equals(usedUnit)) {
            return used.divide(BigDecimal.valueOf(1024));
        }
        if ("GB".equals(usedUnit)) {
            return used;
        }
        throw new IllegalArgumentException("Unexpected Elisa used unit: " + usedUnit);
    }

    private String buildRequestUrl(String uri, long phoneNr) {
        if (uri.endsWith("/")) {
            return uri + phoneNr;
        }
        return uri.replaceAll(
                "(getMobile(?:Internet)?UsageData/)(\\d+)",
                "$1" + phoneNr
        );
    }
}
