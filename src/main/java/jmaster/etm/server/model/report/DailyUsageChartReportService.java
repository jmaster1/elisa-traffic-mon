package jmaster.etm.server.model.report;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jmaster.etm.server.model.PhoneOwner;
import org.springframework.stereotype.Service;
import software.xdev.chartjs.model.charts.BarChart;
import software.xdev.chartjs.model.data.BarData;
import software.xdev.chartjs.model.dataset.BarDataset;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DailyUsageChartReportService {

    private static final DateTimeFormatter LABEL_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final DateTimeFormatter SQL_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @PersistenceContext
    private EntityManager entityManager;

    public BarChart buildChart(ConsumptionReportFilter filter, ZoneId zoneId) {
        List<LocalDate> dates = createDateRange(filter);
        Map<Long, Map<LocalDate, Float>> phoneToDateLastUsedGb = getDailyLastUsedGb(filter, dates, zoneId);

        BarData data = new BarData();
        BarChart chart = new BarChart(data);

        if (dates.isEmpty()) {
            return chart;
        }

        dates.forEach(date -> data.addLabel(LABEL_FORMATTER.format(date)));

        for (Long phoneNr : createPhoneNrList(filter, phoneToDateLastUsedGb)) {
            String color = ConsumptionReportColors.forPhone(phoneNr);
            BarDataset dataset = new BarDataset();
            dataset.setLabel(PhoneOwner.getPhoneLabel(phoneNr));
            dataset.setBackgroundColor(color);
            dataset.setBorderColor(color);
            Map<LocalDate, Float> dateToLastUsedGb = phoneToDateLastUsedGb.getOrDefault(phoneNr, Map.of());
            dates.forEach(date -> dataset.addData(roundGb(getUsageGb(
                    dateToLastUsedGb.get(date),
                    dateToLastUsedGb.get(date.minusDays(1))))));
            data.addDataset(dataset);
        }

        return chart;
    }

    private List<Long> createPhoneNrList(ConsumptionReportFilter filter,
                                         Map<Long, Map<LocalDate, Float>> phoneToDateLastUsedGb) {
        List<Long> phoneNrs = new ArrayList<>();
        if (filter.getPhoneOwner() != null) {
            phoneNrs.add(filter.getPhoneOwner().phoneNr);
        } else {
            phoneNrs.addAll(Arrays.stream(PhoneOwner.values()).map(owner -> owner.phoneNr).toList());
        }
        phoneToDateLastUsedGb.keySet().forEach(phoneNr -> {
            if (!phoneNrs.contains(phoneNr)) {
                phoneNrs.add(phoneNr);
            }
        });
        return phoneNrs;
    }

    private List<LocalDate> createDateRange(ConsumptionReportFilter filter) {
        LocalDate from = filter.getTimestampRange().getFrom() == null ? null :
                filter.getTimestampRange().getFrom().toLocalDate();
        LocalDate to = filter.getTimestampRange().getTo() == null ? null :
                filter.getTimestampRange().getTo().toLocalDate();
        List<LocalDate> dates = new ArrayList<>();
        if (from != null && to != null) {
            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                dates.add(date);
            }
        }
        return dates;
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Map<LocalDate, Float>> getDailyLastUsedGb(ConsumptionReportFilter filter,
                                                                List<LocalDate> reportDates,
                                                                ZoneId zoneId) {
        if (reportDates.isEmpty()) {
            return Map.of();
        }

        LocalDate queryFromDate = reportDates.getFirst().minusDays(1);
        LocalDate queryToDate = reportDates.getLast().plusDays(1);
        Instant queryFrom = queryFromDate.atStartOfDay(zoneId).toInstant();
        Instant queryTo = queryToDate.atStartOfDay(zoneId).toInstant();
        int offsetSeconds = zoneId.getRules().getOffset(queryFrom).getTotalSeconds();

        String sql = """
                WITH snapshots AS (
                    SELECT
                        DATE(TIMESTAMPADD(SECOND, :offsetSeconds, cs.timestamp)) AS day_value,
                        cs.phone_nr,
                        cs.timestamp,
                        cs.id,
                        cs.used_gb
                    FROM consumption_snapshot cs
                    WHERE cs.timestamp >= :queryFrom
                      AND cs.timestamp < :queryTo
                    %s
                ),
                ranked AS (
                    SELECT
                        day_value,
                        phone_nr,
                        cs.used_gb,
                        ROW_NUMBER() OVER (
                            PARTITION BY day_value, phone_nr
                            ORDER BY cs.timestamp DESC, cs.id DESC
                        ) AS last_rn
                    FROM snapshots cs
                )
                SELECT
                    phone_nr,
                    day_value,
                    used_gb AS last_used_gb
                FROM ranked
                WHERE last_rn = 1
                ORDER BY phone_nr, day_value
                """.formatted(createPhoneWhereSql(filter));

        var query = entityManager.createNativeQuery(sql);
        if (filter.getPhoneOwner() != null) {
            query.setParameter("phoneNr", filter.getPhoneOwner().phoneNr);
        }
        query.setParameter("offsetSeconds", offsetSeconds);
        query.setParameter("queryFrom", toSqlDateTime(queryFrom));
        query.setParameter("queryTo", toSqlDateTime(queryTo));

        Map<Long, Map<LocalDate, Float>> result = new LinkedHashMap<>();
        for (Object[] row : (List<Object[]>)query.getResultList()) {
            Long rowPhoneNr = ((Number)row[0]).longValue();
            LocalDate date = toLocalDate(row[1]);
            Float lastUsedGb = ((Number)row[2]).floatValue();
            result.computeIfAbsent(rowPhoneNr, k -> new LinkedHashMap<>()).put(date, lastUsedGb);
        }
        return result;
    }

    private float getUsageGb(Float currentDayLastUsedGb, Float previousDayLastUsedGb) {
        if (currentDayLastUsedGb == null || previousDayLastUsedGb == null) {
            return 0f;
        }
        return Math.max(currentDayLastUsedGb - previousDayLastUsedGb, 0f);
    }

    private String createPhoneWhereSql(ConsumptionReportFilter filter) {
        return filter.getPhoneOwner() == null ? "" : "AND cs.phone_nr = :phoneNr";
    }

    private String toSqlDateTime(Instant instant) {
        return SQL_DATE_TIME_FORMATTER.format(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
    }

    private LocalDate toLocalDate(Object value) {
        return switch (value) {
            case LocalDate localDate -> localDate;
            case java.sql.Date date -> date.toLocalDate();
            default -> LocalDate.parse(String.valueOf(value));
        };
    }

    private float roundGb(float value) {
        return Math.round(value * 100) / 100f;
    }
}
