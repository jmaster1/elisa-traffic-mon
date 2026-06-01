package jmaster.etm.server.controller;

import jmaster.core.controller.AbstractController;
import jmaster.etm.server.service.ConsumptionRegisterService;
import jmaster.etm.server.service.ConsumptionReportService;
import jmaster.etm.server.service.FetchConfig;
import jmaster.etm.server.service.LastError;
import jmaster.system.prefs.PrefsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ConsumptionConfigController extends AbstractController {
	
	@Autowired
    ConsumptionReportService consumptionReportService;
	
	@Autowired
    ConsumptionRegisterService consumptionRegisterService;

	@Autowired
	PrefsService prefsService;
	
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
		FetchConfig fetchConfig = consumptionRegisterService.parseFetch(data);
		consumptionRegisterService.saveFetchConfig(fetchConfig);
		return redirect("/consumption/fetchConfig");
	}
}
