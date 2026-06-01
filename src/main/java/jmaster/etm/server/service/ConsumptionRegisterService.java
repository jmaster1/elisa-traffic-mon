package jmaster.etm.server.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jmaster.etm.server.model.ConsumptionSnapshot;
import jmaster.etm.server.model.PhoneOwner;
import jmaster.etm.server.repository.ConsumptionSnapshotRepository;
import jmaster.etm.server.service.http.DelegateHttpExecutor;
import jmaster.etm.server.service.http.HttpRequestData;
import jmaster.etm.server.service.http.HttpResponseData;
import jmaster.etm.server.service.http.LocalHttpExecutor;
import jmaster.system.prefs.PrefsService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

/**
 * reads consumption snapshots from web and writes them into db
 */
@Service
public class ConsumptionRegisterService {

	Logger logger = LoggerFactory.getLogger(ConsumptionRegisterService.class);

	@Autowired
    ConsumptionSnapshotRepository repository;
	
	@Autowired
    PrefsService prefsService;

	private Exception lastError;
	
	private Date lastErrorDate;

	/**
	 * parse request text acquired by "copy as fetch Node.js" from chrome devtools
	 *
	 * fetch("https://www.elisa.ee/rest/customer/gsm/getMobileInternetUsageData/53065326", {
	 *   "headers": {
	 *     "accept": "application/json, text/plain, *",
		*"accept-language":"ru-RU,ru;q=0.9,en-GB;q=0.8,en-US;q=0.7,en;q=0.6",
		*"cache-control":"no-cache",
		*"pragma":"no-cache",
		*"sec-ch-ua":"\"Not A(Brand\";v=\"99\", \"Google Chrome\";v=\"121\", \"Chromium\";v=\"121\"",
		*"sec-ch-ua-mobile":"?0",
		*"sec-ch-ua-platform":"\"Windows\"",
		*"sec-fetch-dest":"empty",
		*"sec-fetch-mode":"cors",
		*"sec-fetch-site":"same-origin",
		*"x-if-id":"8073848",
		*"cookie":"ekak=false; cookie-categories=WyJmdW5jdGlvbmFsX2Nvb2tpZXMiLCJzdGF0aXN0aWNhbF9jb29raWVzIiwibWFya2V0aW5nX2Nvb2tpZXMiXQ==; _gcl_au=1.1.1935118975.1706108031; _eid=r6NaNKK12WRfRxOKvVZMrUAcj1Ba3CQ6JnSvz2gI; web_language=et; nps_30=Thu%20Feb%2001%202024%2015%3A28%3A01%20GMT%2B0200%20(Eastern%20European%20Standard%20Time); _gid=GA1.2.981981388.1707038718; _clck=1kjtcaz%7C2%7Cfiz%7C0%7C1484; _ga=GA1.2.2033097381.1706108032; MINUELISA_SSO=1cvvtncwf63xs1l66p0jd7yg92828296; elisa_smooch=eyJ0aW1lIjoxNzA3MDM4NzY1LCJ0b2tlbiI6ImV5SmhiR2NpT2lKSVV6STFOaUlzSW10cFpDSTZJbUZ3Y0Y4MVlXWTBPR1V4TURFeFltTXdNREF3TWpKaU5qVXdNRE1pTENKMGVYQWlPaUpLVjFRaWZRLmV5SnpZMjl3WlNJNkltRndjRlZ6WlhJaUxDSjFjMlZ5U1dRaU9pSmxaR05pTXpOaFpDMDNNVFZpTFRSaE5UTXRPREUwT1MxbU1HUmtPV1V3TkRsaVkyRWlmUS5iQThkMTR3UkxyTG9xS0tnR1lvSWpOcjZ3NkRjS2lBdC0yYWVza0MyaFdBIiwidWlkIjoiZWRjYjMzYWQtNzE1Yi00YTUzLTgxNDktZjBkZDllMDQ5YmNhIiwib3BlbmVkIjp0cnVlfQ==; _clsk=m9hgu3%7C1707038778141%7C5%7C1%7Cz.clarity.ms%2Fcollect; _ga_XYJYTP5DDP=GS1.1.1707038718.4.1.1707038784.0.0.0; _ga_37XJ02JCD9=GS1.1.1707038718.4.1.1707038784.0.0.0",
		*"Referer":"https://www.elisa.ee/itb/mobile-internet-usage/53065326",
		*"Referrer-Policy":"strict-origin-when-cross-origin"
		*},
		*"body":null,
		*"method":"GET"
		*});
	 */
	public FetchConfig parseFetch(String fetch) {
		FetchConfig fetchConfig = getFetchConfig();
		try
		{
			fetchConfig.uri = StringUtils.substringBetween(fetch, "fetch(\"", "\",")
					.replaceAll("\\d*$", "");
			Map<String, String> headers = fetchConfig.headers = new HashMap<>();
			int jsonBegin = fetch.indexOf('{');
			int jsonEnd = fetch.lastIndexOf('}');
			String json = fetch.substring(jsonBegin - 1, jsonEnd + 1);
			JsonObject obj = new Gson().fromJson(json, JsonObject.class);
			JsonObject headersObj = obj.get("headers").getAsJsonObject();
			for(Iterator<String> it = headersObj.keySet().iterator(); it.hasNext(); )
			{
				String key = it.next();
				String val = headersObj.get(key).getAsString();
				headers.put(key, val);
			}
		} catch(Exception ex) {
			throw new IllegalArgumentException("Bad fetch data");
		}
		prefsService.savePrefs(fetchConfig);
		return fetchConfig;
	}
	
	@Scheduled(initialDelay = 1000, fixedDelay = 600000)
	public void queryConsumptionSnapshots() {
		try {
			FetchConfig fetchData = prefsService.getPrefs(FetchConfig.class);
			if (fetchData != null && fetchData.uri != null && fetchData.enabled) {
				for (PhoneOwner phoneOwner : PhoneOwner.values()) {
					BigDecimal usedGb = queryConsumptionSnapshot(phoneOwner, fetchData);
					ConsumptionSnapshot snapshot = new ConsumptionSnapshot();
					snapshot.setTimestamp(new Date());
					snapshot.setPhoneNr(phoneOwner.phoneNr);
					snapshot.setUsedGb(usedGb.floatValue());
					repository.save(snapshot);
				}
			}
		} catch (Exception ex) {
			logger.error("queryConsumptionSnapshots() failed", ex);
			lastError = ex;
			lastErrorDate = new Date();
		}
	}

	/**
	 * fetch consumption for given user
	 * @param phoneOwner
	 * @param fetchData
	 * @return consumed amount (GB)
	 */
	private BigDecimal queryConsumptionSnapshot(PhoneOwner phoneOwner, FetchConfig fetchData)
	{
		Function<HttpRequestData, HttpResponseData> httpQueryExecutor =
				StringUtils.isEmpty(fetchData.delegateUri) ?
				new LocalHttpExecutor() :
				new DelegateHttpExecutor(fetchData.delegateUri,
						fetchData.delegateBasicAuthUsername, fetchData.delegateBasicAuthPassword);

		HttpRequestData request = new HttpRequestData();
		request.url = fetchData.uri.replaceAll(
				"(getMobileUsageData/)(\\d+)",
				"$1" + phoneOwner.phoneNr
		);
		if(fetchData.headers != null) {
			request.headers.putAll(fetchData.headers);
		}
		request.method = HttpMethod.GET.name();
		HttpResponseData response = httpQueryExecutor.apply(request);
		
		response.ensureStatusOk();
		JsonObject obj = response.getContentAsJsonObject();
		JsonObject graph = obj
				.getAsJsonArray("internetConsumptionGraphs")
				.get(0)
				.getAsJsonObject();

		BigDecimal used = graph.get("used").getAsBigDecimal();
		String usedUnit = graph.get("usedUnit").getAsString();

		if("KB".equals(usedUnit)) {
			used = used.divide(BigDecimal.valueOf(1024 * 1024));
		} else if("MB".equals(usedUnit)) {
			used = used.divide(BigDecimal.valueOf(1024));
		} else if(!"GB".equals(usedUnit)) {
			throw new RuntimeException("Unexpected used unit: " + usedUnit);
		}
		return used;
	}

	public FetchConfig getFetchConfig() {
		return prefsService.getPrefs(FetchConfig.class);
	}

	public FetchConfig saveFetchConfig(FetchConfig fetchConfig) {
		prefsService.savePrefs(fetchConfig);
		return fetchConfig;
	}
	
	public LastError getLastError() {
		if(lastError == null) {
			return null;
		}
		LastError ret = new LastError();
		ret.message = lastError.getMessage();
		ret.date = lastErrorDate;
		return ret;
	}
	
	public void clearLastError()
	{
		lastError = null;
		lastErrorDate = null;
	}
}
