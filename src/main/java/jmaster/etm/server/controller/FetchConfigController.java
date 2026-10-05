package jmaster.etm.server.controller;

import jmaster.core.controller.AbstractController;
import jmaster.etm.server.model.snapshot.ConsumptionRegisterService;
import jmaster.etm.server.model.snapshot.FetchConfig;
import jmaster.etm.server.model.snapshot.LastError;
import jmaster.system.prefs.PrefsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class FetchConfigController extends AbstractController {

	private final ConsumptionRegisterService consumptionRegisterService;

	private final PrefsService prefsService;

	@GetMapping("/consumption/config")
	String fetchConfig(Model model) {
		FetchConfig fetchConfig = prefsService.getPrefs(FetchConfig.class);
		model.addAttribute("fetchConfig", toJson(maskSensitiveHeaders(fetchConfig)));
		LastError lastError = consumptionRegisterService.getLastError();
		model.addAttribute("lastError", lastError);
		return "consumption/fetchConfig";
	}

	@PostMapping("/consumption/config")
	String parseFetch(@RequestParam("data") String data, RedirectAttributes redirectAttributes) {
		try {
			FetchConfig fetchConfig = consumptionRegisterService.parseFetchConfig(data);
			consumptionRegisterService.saveFetchConfig(fetchConfig);
			redirectAttributes.addFlashAttribute("parsedProvider", consumptionRegisterService.getProviderName(fetchConfig));
			redirectAttributes.addFlashAttribute("parsedFetchUrl", fetchConfig.uri);
			redirectAttributes.addFlashAttribute("parsedHeaderCount",
					fetchConfig.headers == null ? 0 : fetchConfig.headers.size());
			redirectAttributes.addFlashAttribute(ATTR_INFO_MESSAGE, "Fetch configuration saved.");
		} catch (Exception ex) {
			var errorData = errorLogService.handleError(ex);
			redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, ex.getMessage());
			redirectAttributes.addFlashAttribute(ATTR_ERROR_DETAILS, formatErrorDetails(errorData, ex));
		}
		return redirect("/consumption/config");
	}

	private FetchConfig maskSensitiveHeaders(FetchConfig source) {
		if (source == null) {
			return null;
		}
		FetchConfig result = new FetchConfig();
		result.monthlyQuotaGb = source.monthlyQuotaGb;
		result.enabled = source.enabled;
		result.uri = source.uri;
		if (source.headers != null) {
			result.headers = new LinkedHashMap<>();
			for (Map.Entry<String, String> entry : source.headers.entrySet()) {
				String key = entry.getKey();
				boolean isSensitive = "cookie".equalsIgnoreCase(key)
						|| "authorization".equalsIgnoreCase(key);
				result.headers.put(key, isSensitive ? "***" : entry.getValue());
			}
		}
		return result;
	}

	@PostMapping("/consumption/config/test")
	String testFetch(RedirectAttributes redirectAttributes) {
		consumptionRegisterService.clearLastError();
		consumptionRegisterService.queryConsumptionSnapshots();
		LastError lastError = consumptionRegisterService.getLastError();
		if (lastError == null) {
			redirectAttributes.addFlashAttribute(ATTR_INFO_MESSAGE, "Consumption snapshot query completed.");
		} else {
			redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, lastError.message);
		}
		return redirect("/consumption/config");
	}

}
