package jmaster.etm.server.service;

import jmaster.etm.server.model.ConsumptionDataset;
import jmaster.etm.server.model.ConsumptionReportFilter;
import jmaster.etm.server.model.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.xdev.chartjs.model.charts.ScatterChart;
import software.xdev.chartjs.model.data.ScatterData;
import software.xdev.chartjs.model.datapoint.ScatterDataPoint;
import software.xdev.chartjs.model.dataset.ScatterDataset;

import java.util.Collection;
import java.util.List;

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

    public ScatterChart buildChart(ConsumptionReportFilter filter) {
        Collection<ConsumptionDataset> consumptionDatasets = consumptionReportService.getConsumptionDatasets(filter);

        ScatterData data = new ScatterData();
        ScatterChart chart = new ScatterChart(data);

        int index = 0;
        for (ConsumptionDataset consumptionDataset : consumptionDatasets) {
            String color = COLORS.get(index % COLORS.size());
            ScatterDataset dataset = new ScatterDataset()
                    .setType("line")
                    .setLabel(consumptionDataset.label)
                    .setData(toChartPoints(consumptionDataset.data))
                    .setBorderColor(color)
                    .setBackgroundColor(color)
                    .addPointRadius(2)
                    .addPointHoverRadius(5)
                    .setShowLine(true)
                    .setTension(0.2);
            data.addDataset(dataset);
            index++;
        }

        return chart;
    }

    private List<ScatterDataPoint> toChartPoints(List<Point> points) {
        return points.stream()
                .map(point -> new ScatterDataPoint(point.x.getTime(), roundGb(point.y)))
                .toList();
    }

    private float roundGb(float value) {
        return Math.round(value * 100) / 100f;
    }
}
