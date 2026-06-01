package jmaster.etm.server.controller;

import com.turkraft.springfilter.converter.FilterSpecification;
import jmaster.core.controller.AbstractController;
import jmaster.etm.server.model.ConsumptionSnapshot;
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
    String chart(
            FilterSpecification<ConsumptionSnapshot> filter,
            Model model) {
        model.addAttribute("chartJson", toJson(consumptionChartReportService.buildChart(filter)));
        return "consumption/report";
    }

    @GetMapping("json")
    ResponseEntity<?> json(FilterSpecification<ConsumptionSnapshot> filter) {
        return doWithJsonResponse(() -> toJson(consumptionChartReportService.buildChart(filter)));
    }
}
