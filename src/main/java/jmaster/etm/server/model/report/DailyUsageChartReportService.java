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
import java.util.Comparator;
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
        List<DailyUsageData> dailyUsageData = toDailyUsageData(filter, dates, zoneId);

        BarData data = new BarData();
        BarChart chart = new BarChart(data);

        if (dates.isEmpty()) {
            return chart;
        }

        dates.forEach(date -> data.addLabel(LABEL_FORMATTER.format(date)));

        for (DailyUsageData phoneData : dailyUsageData) {
            String color = ConsumptionReportColors.forPhone(phoneData.phoneNr);
            BarDataset dataset = new BarDataset();
            dataset.setLabel(phoneData.label);
            dataset.setBackgroundColor(color);
            dataset.setBorderColor(color);
            dates.forEach(date -> dataset.addData(roundGb(phoneData.dateToUsageGb.getOrDefault(date, 0f))));
            data.addDataset(dataset);
        }

        return chart;
    }

    private List<DailyUsageData> toDailyUsageData(ConsumptionReportFilter filter,
                                                  List<LocalDate> dates,
                                                  ZoneId zoneId) {
        Map<Long, Map<LocalDate, Float>> phoneToDateUsageGb = getDailyUsage(filter, dates, zoneId);
        List<Long> phoneNrs = createPhoneNrList(filter, phoneToDateUsageGb);
        List<DailyUsageData> result = new ArrayList<>(phoneNrs.size());
        for (Long phoneNr : phoneNrs) {
            DailyUsageData dailyUsageData = new DailyUsageData(phoneNr, PhoneOwner.getPhoneLabel(phoneNr));
            dailyUsageData.dateToUsageGb.putAll(phoneToDateUsageGb.getOrDefault(phoneNr, Map.of()));
            result.add(dailyUsageData);
        }
        return sortByPhoneOwner(result);
    }

    private List<Long> createPhoneNrList(ConsumptionReportFilter filter,
                                         Map<Long, Map<LocalDate, Float>> phoneToDateUsageGb) {
        List<Long> phoneNrs = new ArrayList<>();
        if (filter.getPhoneOwner() != null) {
            phoneNrs.add(filter.getPhoneOwner().phoneNr);
        } else {
            phoneNrs.addAll(Arrays.stream(PhoneOwner.values()).map(owner -> owner.phoneNr).toList());
        }
        phoneToDateUsageGb.keySet().forEach(phoneNr -> {
            if (!phoneNrs.contains(phoneNr)) {
                phoneNrs.add(phoneNr);
            }
        });
        return phoneNrs;
    }

    private List<DailyUsageData> sortByPhoneOwner(List<DailyUsageData> data) {
        return data.stream()
                .sorted(Comparator.comparingInt(item -> {
                    PhoneOwner owner = PhoneOwner.fromPhone(item.phoneNr);
                    return owner == null ? Integer.MAX_VALUE : owner.ordinal();
                }))
                .toList();
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
    private Map<Long, Map<LocalDate, Float>> getDailyUsage(ConsumptionReportFilter filter,
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
                            ORDER BY cs.timestamp ASC, cs.id ASC
                        ) AS first_rn,
                        ROW_NUMBER() OVER (
                            PARTITION BY day_value, phone_nr
                            ORDER BY cs.timestamp DESC, cs.id DESC
                        ) AS last_rn
                    FROM snapshots cs
                ),
                daily AS (
                    SELECT
                        day_value,
                        phone_nr,
                        MAX(CASE WHEN first_rn = 1 THEN used_gb END) AS first_used_gb,
                        MAX(CASE WHEN last_rn = 1 THEN used_gb END) AS last_used_gb
                    FROM ranked
                    GROUP BY day_value, phone_nr
                )
                SELECT
                    cur.phone_nr,
                    cur.day_value,
                    GREATEST(
                        cur.last_used_gb - COALESCE(prev.last_used_gb, cur.first_used_gb),
                        0
                    ) AS usage_gb
                FROM daily cur
                LEFT JOIN daily prev
                    ON prev.phone_nr = cur.phone_nr
                   AND prev.day_value = DATE_SUB(cur.day_value, INTERVAL 1 DAY)
                WHERE cur.day_value >= :reportFrom
                  AND cur.day_value <= :reportTo
                ORDER BY cur.phone_nr, cur.day_value
                """.formatted(createPhoneWhereSql(filter));

        var query = entityManager.createNativeQuery(sql);
        if (filter.getPhoneOwner() != null) {
            query.setParameter("phoneNr", filter.getPhoneOwner().phoneNr);
        }
        query.setParameter("offsetSeconds", offsetSeconds);
        query.setParameter("queryFrom", toSqlDateTime(queryFrom));
        query.setParameter("queryTo", toSqlDateTime(queryTo));
        query.setParameter("reportFrom", LABEL_FORMATTER.format(reportDates.getFirst()));
        query.setParameter("reportTo", LABEL_FORMATTER.format(reportDates.getLast()));

        Map<Long, Map<LocalDate, Float>> result = new LinkedHashMap<>();
        for (Object[] row : (List<Object[]>)query.getResultList()) {
            Long rowPhoneNr = ((Number)row[0]).longValue();
            LocalDate date = toLocalDate(row[1]);
            Float usageGb = ((Number)row[2]).floatValue();
            result.computeIfAbsent(rowPhoneNr, k -> new LinkedHashMap<>()).put(date, usageGb);
        }
        return result;
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

    private record DailyUsageData(Long phoneNr, String label, Map<LocalDate, Float> dateToUsageGb) {
        private DailyUsageData(Long phoneNr, String label) {
            this(phoneNr, label, new LinkedHashMap<>());
        }
    }
}
