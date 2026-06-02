package jmaster.etm.server.controller;

import jmaster.core.controller.AbstractController;
import jmaster.etm.server.model.ConsumptionReportFilter;
import jmaster.etm.server.service.ConsumptionChartReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/consumption/report")
@RequiredArgsConstructor
public class ConsumptionReportController extends AbstractController {

    private final ConsumptionChartReportService consumptionChartReportService;

    @GetMapping({"", "/"})
    String chart(ConsumptionReportFilter filter, Model model) {
        model.addAttribute("chartJson", toJson(consumptionChartReportService.buildChart(filter)));
        createFilterFormState(filter, model, "reportFilter").setMethod("get");
        return "consumption/report";
    }

    @GetMapping("json")
    ResponseEntity<?> json(ConsumptionReportFilter filter) {
        return doWithJsonResponse(() -> toJson(consumptionChartReportService.buildChart(filter)));
    }
}
