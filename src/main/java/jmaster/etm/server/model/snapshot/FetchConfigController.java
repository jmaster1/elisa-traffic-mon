package jmaster.etm.server.model.snapshot;

import jmaster.core.controller.AbstractController;
import jmaster.system.prefs.PrefsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
		return redirect("/consumption/fetchConfig");
	}
}
