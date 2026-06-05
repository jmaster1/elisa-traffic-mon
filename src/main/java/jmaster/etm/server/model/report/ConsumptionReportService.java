package jmaster.etm.server.model.report;

import jmaster.etm.server.model.snapshot.ConsumptionSnapshot;
import jmaster.etm.server.model.PhoneOwner;
import jmaster.etm.server.model.snapshot.ConsumptionSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConsumptionReportService {

    private final ConsumptionSnapshotRepository repository;

    public Collection<ConsumptionDataset> getConsumptionDatasets(ConsumptionReportFilter filter) {
        Map<Long, ConsumptionDataset> phoneToDataset = new LinkedHashMap<>();
        List<ConsumptionSnapshot> snapshots = filter.list(repository);
        for (ConsumptionSnapshot snapshot : snapshots) {
            Long phoneNr = snapshot.getPhoneNr();
            ConsumptionDataset dataset = phoneToDataset.get(phoneNr);
            if (dataset == null) {
                phoneToDataset.put(phoneNr, dataset = new ConsumptionDataset());
                dataset.phoneNr = phoneNr;
                dataset.label = PhoneOwner.getPhoneLabel(phoneNr);
            }
            Point lastAddedPoint = dataset.lastAddedPoint;
            if (lastAddedPoint == null || lastAddedPoint.y != snapshot.getUsedGb()) {
                dataset.data.add(dataset.lastAddedPoint = new Point(snapshot));
                dataset.lastSkippedSnapshot = null;
            } else {
                dataset.lastSkippedSnapshot = snapshot;
            }
        }
        for (ConsumptionDataset dataset : phoneToDataset.values()) {
            if (dataset.lastSkippedSnapshot != null) {
                dataset.data.add(dataset.lastAddedPoint = new Point(dataset.lastSkippedSnapshot));
            }
        }
        return phoneToDataset.values();
    }

    public List<MonthlyConsumptionProgress> getCurrentMonthProgress(int monthlyQuotaGb, ZoneId reportZoneId) {
        LocalDate monthStart = LocalDate.now(reportZoneId).withDayOfMonth(1);
        Date from = Date.from(monthStart.atStartOfDay(reportZoneId).toInstant());
        Date to = Date.from(monthStart.plusMonths(1).atStartOfDay(reportZoneId).toInstant().minusNanos(1));

        List<MonthlyConsumptionProgress> progressList = new ArrayList<>();
        Map<Long, ConsumptionSnapshot> phoneToSnapshot = new LinkedHashMap<>();
        for (ConsumptionSnapshot snapshot : repository.findLatestByPhoneBetween(from, to)) {
            phoneToSnapshot.put(snapshot.getPhoneNr(), snapshot);
        }

        for (PhoneOwner owner : PhoneOwner.values()) {
            ConsumptionSnapshot snapshot = phoneToSnapshot.remove(owner.phoneNr);
            progressList.add(createMonthlyProgress(owner.phoneNr, owner.name(), snapshot, monthlyQuotaGb));
        }
        for (ConsumptionSnapshot snapshot : phoneToSnapshot.values()) {
            Long phoneNr = snapshot.getPhoneNr();
            if (PhoneOwner.isKnownPhone(phoneNr)) {
                continue;
            }
            progressList.add(createMonthlyProgress(phoneNr, PhoneOwner.getPhoneLabel(phoneNr), snapshot, monthlyQuotaGb));
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
        progress.cappedPercent = Math.max(0, Math.min(progress.percent, 100));
        return progress;
    }

}
