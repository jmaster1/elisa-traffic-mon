package jmaster.etm.server.controller;

import com.turkraft.springfilter.converter.FilterSpecification;
import jmaster.etm.server.model.ConsumptionDataset;
import jmaster.etm.server.model.ConsumptionSnapshot;
import jmaster.etm.server.service.ConsumptionRegisterService;
import jmaster.etm.server.service.ConsumptionReportService;
import jmaster.etm.server.service.FetchConfig;
import jmaster.etm.server.service.LastError;
import org.hibernate.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Controller
public class ConsumptionController {
	
	@Autowired
    ConsumptionReportService consumptionReportService;
	
	@Autowired
    ConsumptionRegisterService consumptionRegisterService;
	
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
	
	@PostMapping("/consumption/parseFetch")
	FetchConfig parseFetch(@RequestBody String data) {
		return consumptionRegisterService.parseFetch(data);
	}

	@GetMapping("/consumption/fetchConfig")
	FetchConfig getFetchData() {
		return consumptionRegisterService.getFetchConfig();
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
