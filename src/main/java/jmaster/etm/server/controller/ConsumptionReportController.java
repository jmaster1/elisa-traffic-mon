package jmaster.etm.server.controller;

import jmaster.core.controller.AbstractController;
import jmaster.etm.server.model.report.ConsumptionChartReportService;
import jmaster.etm.server.model.report.ConsumptionReportFilter;
import jmaster.etm.server.model.report.ConsumptionReportService;
import jmaster.etm.server.model.snapshot.FetchConfig;
import jmaster.system.prefs.PrefsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import software.xdev.chartjs.model.charts.AbstractChart;

import java.time.ZoneId;

@Controller
@RequestMapping("/consumption/report")
@RequiredArgsConstructor
public class ConsumptionReportController extends AbstractController {

    private final ConsumptionChartReportService consumptionChartReportService;

    private final ConsumptionReportService consumptionReportService;

    private final PrefsService prefsService;

    @GetMapping({"", "/"})
    String chart(ConsumptionReportFilter filter, Model model) {
        ZoneId zoneId = resolveZoneId();
        AbstractChart chart = consumptionChartReportService.buildChart(filter);
        FetchConfig fetchConfig = prefsService.getPrefs(FetchConfig.class);
        int monthlyQuotaGb = fetchConfig == null ? 0 : fetchConfig.monthlyQuotaGb;
        model.addAttribute("chartJson", chart.toJson());
        model.addAttribute("reportZoneId", zoneId.getId());
        model.addAttribute("monthlyConsumptionProgress",
                consumptionReportService.getCurrentMonthProgress(monthlyQuotaGb, zoneId));
        createFilterFormState(filter, model, "reportFilter").setMethod("get");
        return "consumption/report";
    }
}
