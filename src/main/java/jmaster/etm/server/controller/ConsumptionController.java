package jmaster.etm.server.controller;

import com.turkraft.springfilter.converter.FilterSpecification;
import jmaster.core.controller.AbstractController;
import jmaster.etm.server.model.ConsumptionDataset;
import jmaster.etm.server.model.ConsumptionSnapshot;
import jmaster.etm.server.service.ConsumptionRegisterService;
import jmaster.etm.server.service.ConsumptionReportService;
import jmaster.etm.server.service.FetchConfig;
import jmaster.etm.server.service.LastError;
import jmaster.system.prefs.PrefsService;
import org.hibernate.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

@Controller
public class ConsumptionController extends AbstractController {
	
	@Autowired
    ConsumptionReportService consumptionReportService;
	
	@Autowired
    ConsumptionRegisterService consumptionRegisterService;

	@Autowired
	PrefsService prefsService;
	
	/**
	 * https://github.com/turkraft/springfilter
	 * https://github.com/sisimomo/Spring-Filter-Query-Builder
	 * @param filter
	 * @return
	 */
//	@GetMapping("/consumption/data")
//	Collection<ConsumptionDataset> find(@Parameter(hidden = true)
//	FilterSpecification<ConsumptionSnapshot> filter) {
//		return consumptionReportService.getConsumptionDatasets(filter);
//	}
	
	@GetMapping("/consumption/fetchConfig")
	String fetchConfig(Model model) {
		FetchConfig fetchConfig = prefsService.getPrefs(FetchConfig.class);
		model.addAttribute("fetchConfig", toJson(fetchConfig));
		return "consumption/fetchConfig";
	}

	@PostMapping("/consumption/fetchConfig")
	String parseFetch(@RequestParam("data") String data) {
		FetchConfig fetchConfig = consumptionRegisterService.parseFetch(data);
		consumptionRegisterService.saveFetchConfig(fetchConfig);
		return redirect("/consumption/fetchConfig");
	}

	@PutMapping("/consumption/fetchConfig")
	FetchConfig saveFetchConfig(@RequestBody FetchConfig fetchConfig) {
		return consumptionRegisterService.saveFetchConfig(fetchConfig);
	}
	
	@GetMapping("/consumption/lastError")
    LastError getLastError() {
		return consumptionRegisterService.getLastError();
	}
	
	@DeleteMapping("/consumption/lastError")
	void clearLastError() {
		consumptionRegisterService.clearLastError();
	}
	
	@PostMapping("/consumption/fetch")
	void queryConsumptionSnapshots() {
		consumptionRegisterService.queryConsumptionSnapshots();
	}
}
