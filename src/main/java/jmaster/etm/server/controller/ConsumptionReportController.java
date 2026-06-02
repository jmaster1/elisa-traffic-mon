package jmaster.etm.server.controller;

import jmaster.core.controller.AbstractController;
import jmaster.etm.server.model.ConsumptionReportFilter;
import jmaster.etm.server.service.ConsumptionChartReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import software.xdev.chartjs.model.charts.AbstractChart;

@Controller
@RequestMapping("/consumption/report")
@RequiredArgsConstructor
public class ConsumptionReportController extends AbstractController {

    private final ConsumptionChartReportService consumptionChartReportService;

    @GetMapping({"", "/"})
    String chart(ConsumptionReportFilter filter, Model model) {
        AbstractChart chart = consumptionChartReportService.buildChart(filter);
        model.addAttribute("chartJson", chart.toJson());
        createFilterFormState(filter, model, "reportFilter").setMethod("get");
        return "consumption/report";
    }
}
