package jmaster.etm.server.model.report;

import jmaster.etm.server.model.snapshot.ConsumptionSnapshot;
import jmaster.etm.server.model.PhoneOwner;
import jmaster.etm.server.model.snapshot.ConsumptionSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConsumptionReportService {

    private static final Integer MAX_DATA_SIZE = 50000;

    private final ConsumptionSnapshotRepository repository;

    public List<ConsumptionSnapshot> getConsumptionSnapshots(ConsumptionReportFilter filter, ZoneId reportZoneId) {
        filter.setSize(MAX_DATA_SIZE);
        return filter.list(repository, reportZoneId).stream()
                .filter(snapshot -> snapshot.getTimestamp() != null)
                .sorted(Comparator.comparing(ConsumptionSnapshot::getTimestamp))
                .toList();
    }

    public Collection<ConsumptionDataset> getConsumptionDatasets(ConsumptionReportFilter filter, ZoneId reportZoneId) {
        Map<Long, ConsumptionDataset> phoneToDataset = new LinkedHashMap<>();
        Map<Long, Point> phoneToLastAddedPoint = new LinkedHashMap<>();
        Map<Long, ConsumptionSnapshot> phoneToLastSkippedSnapshot = new LinkedHashMap<>();
        List<ConsumptionSnapshot> snapshots = getConsumptionSnapshots(filter, reportZoneId);
        for (ConsumptionSnapshot snapshot : snapshots) {
            Long phoneNr = snapshot.getPhoneNr();
            ConsumptionDataset dataset = phoneToDataset.get(phoneNr);
            if (dataset == null) {
                phoneToDataset.put(phoneNr, dataset = new ConsumptionDataset());
                dataset.phoneNr = phoneNr;
                dataset.label = PhoneOwner.getPhoneLabel(phoneNr);
            }
            Point lastAddedPoint = phoneToLastAddedPoint.get(phoneNr);
            if (lastAddedPoint == null || lastAddedPoint.y != snapshot.getUsedGb()) {
                Point point = new Point(snapshot);
                dataset.data.add(point);
                phoneToLastAddedPoint.put(phoneNr, point);
                phoneToLastSkippedSnapshot.remove(phoneNr);
            } else {
                phoneToLastSkippedSnapshot.put(phoneNr, snapshot);
            }
        }
        for (ConsumptionDataset dataset : phoneToDataset.values()) {
            ConsumptionSnapshot lastSkippedSnapshot = phoneToLastSkippedSnapshot.get(dataset.phoneNr);
            if (lastSkippedSnapshot != null) {
                Point point = new Point(lastSkippedSnapshot);
                dataset.data.add(point);
                phoneToLastAddedPoint.put(dataset.phoneNr, point);
            }
        }
        return phoneToDataset.values();
    }

    public List<MonthlyConsumptionProgress> getCurrentMonthProgress(int monthlyQuotaGb, ZoneId reportZoneId) {
        LocalDate monthStart = LocalDate.now(reportZoneId).withDayOfMonth(1);
        Instant from = monthStart.atStartOfDay(reportZoneId).toInstant();
        Instant to = monthStart.plusMonths(1).atStartOfDay(reportZoneId).toInstant().minusNanos(1);

        List<ConsumptionSnapshot> snapshots = repository.findLatestByPhoneBetween(from, to);
        List<MonthlyConsumptionProgress> progressList = new ArrayList<>(PhoneOwner.values().length);
        for (PhoneOwner owner : PhoneOwner.values()) {
            ConsumptionSnapshot snapshot = snapshots.stream().filter(s -> s.getPhoneNr() == owner.phoneNr)
                    .findFirst().orElse(null);
            progressList.add(createMonthlyProgress(owner.phoneNr, owner.name(), snapshot, monthlyQuotaGb));
        }
        return progressList;
    }

    private MonthlyConsumptionProgress createMonthlyProgress(Long phoneNr, String label, ConsumptionSnapshot snapshot, int monthlyQuotaGb) {
        float usedGb = snapshot == null || snapshot.getUsedGb() == null ? 0f : snapshot.getUsedGb();

        MonthlyConsumptionProgress progress = new MonthlyConsumptionProgress();
        progress.phoneNr = phoneNr;
        progress.label = label;
        progress.color = ConsumptionReportColors.forPhone(phoneNr);
        progress.usedGbText = String.format(Locale.US, "%.2f", usedGb);
        progress.quotaGb = monthlyQuotaGb;
        progress.percent = monthlyQuotaGb <= 0 ? 0 : Math.round(usedGb * 100 / monthlyQuotaGb);
        progress.cappedPercent = Math.clamp(progress.percent, 0, 100);
        return progress;
    }

}
