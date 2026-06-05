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

@Controller
@RequiredArgsConstructor
public class FetchConfigController extends AbstractController {

	private final ConsumptionRegisterService consumptionRegisterService;

	private final PrefsService prefsService;
	
	@GetMapping("/consumption/config")
	String fetchConfig(Model model) {
		FetchConfig fetchConfig = prefsService.getPrefs(FetchConfig.class);
		model.addAttribute("fetchConfig", toJson(fetchConfig));
		LastError lastError = consumptionRegisterService.getLastError();
		model.addAttribute("lastError", lastError);
		return "consumption/fetchConfig";
	}

	@PostMapping("/consumption/config")
	String parseFetch(@RequestParam("data") String data) {
		FetchConfig fetchConfig = consumptionRegisterService.parseFetchConfig(data);
		consumptionRegisterService.saveFetchConfig(fetchConfig);
		return redirect("/consumption/config");
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
