package jmaster.etm.server.model.report;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.xdev.chartjs.model.charts.ScatterChart;
import software.xdev.chartjs.model.data.ScatterData;
import software.xdev.chartjs.model.datapoint.ScatterDataPoint;
import software.xdev.chartjs.model.dataset.ScatterDataset;

import java.time.ZoneId;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsumptionChartReportService {

    private final ConsumptionReportService consumptionReportService;

    public ScatterChart buildChart(ConsumptionReportFilter filter, ZoneId zoneId) {
        Collection<ConsumptionDataset> consumptionDatasets = consumptionReportService.getConsumptionDatasets(filter);

        ScatterData data = new ScatterData();
        ScatterChart chart = new ScatterChart(data);

        for (ConsumptionDataset consumptionDataset : consumptionDatasets) {
            String color = ConsumptionReportColors.forPhone(consumptionDataset.phoneNr);
            ScatterDataset dataset = new ScatterDataset()
                    .setType("line")
                    .setLabel(consumptionDataset.label)
                    .setData(toChartPoints(consumptionDataset.data, zoneId))
                    .setBorderColor(color)
                    .setBackgroundColor(color)
                    .addPointRadius(2)
                    .addPointHoverRadius(5)
                    .setShowLine(true)
                    .setTension(0.2);
            data.addDataset(dataset);
        }

        return chart;
    }

    private List<ScatterDataPoint> toChartPoints(List<Point> points, ZoneId zoneId) {
        return points.stream()
                .map(point -> new ScatterDataPoint(toEpochMillis(point, zoneId), roundGb(point.y)))
                .toList();
    }

    private long toEpochMillis(Point point, ZoneId zoneId) {
        return point.x.toInstant().atZone(zoneId).toInstant().toEpochMilli();
    }

    private float roundGb(float value) {
        return Math.round(value * 100) / 100f;
    }
}
