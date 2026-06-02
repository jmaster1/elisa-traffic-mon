package jmaster.etm.server.service;

import jmaster.etm.server.model.ConsumptionDataset;
import jmaster.etm.server.model.ConsumptionReportFilter;
import jmaster.etm.server.model.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConsumptionChartReportService {

    private static final List<String> COLORS = List.of(
            "#2563eb",
            "#dc2626",
            "#16a34a",
            "#9333ea",
            "#ea580c",
            "#0891b2",
            "#be123c",
            "#4f46e5"
    );

    private final ConsumptionReportService consumptionReportService;

    public Map<String, Object> buildChart(ConsumptionReportFilter filter) {
        Collection<ConsumptionDataset> consumptionDatasets = consumptionReportService.getConsumptionDatasets(filter);

        List<Map<String, Object>> datasets = new ArrayList<>();
        int index = 0;
        for (ConsumptionDataset consumptionDataset : consumptionDatasets) {
            String color = COLORS.get(index % COLORS.size());
            Map<String, Object> dataset = new LinkedHashMap<>();
            dataset.put("label", consumptionDataset.label);
            dataset.put("data", toChartPoints(consumptionDataset.data));
            dataset.put("borderColor", color);
            dataset.put("backgroundColor", color);
            dataset.put("pointRadius", 2);
            dataset.put("pointHoverRadius", 5);
            dataset.put("tension", 0.2);
            datasets.add(dataset);
            index++;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("datasets", datasets);

        Map<String, Object> chart = new LinkedHashMap<>();
        chart.put("type", "line");
        chart.put("data", data);
        return chart;
    }

    private List<Map<String, Object>> toChartPoints(List<Point> points) {
        List<Map<String, Object>> chartPoints = new ArrayList<>();
        for (Point point : points) {
            Map<String, Object> chartPoint = new LinkedHashMap<>();
            chartPoint.put("x", point.x.getTime());
            chartPoint.put("y", roundGb(point.y));
            chartPoints.add(chartPoint);
        }
        return chartPoints;
    }

    private float roundGb(float value) {
        return Math.round(value * 100) / 100f;
    }
}
