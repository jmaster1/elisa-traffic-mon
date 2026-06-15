package jmaster.etm.server.model.report;

import jmaster.core.model.LocalDateTimeRange;
import jmaster.core.model.filter.DefaultFilter;
import jmaster.core.ui.annot.Ui;
import jmaster.core.util.jpa.SpecBuilder;
import jmaster.etm.server.model.PhoneOwner;
import jmaster.etm.server.model.snapshot.ConsumptionSnapshot;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Accessors(chain = true)
public class ConsumptionReportFilter extends DefaultFilter<ConsumptionSnapshot> {

    @Ui(label = "Phone")
    private PhoneOwner phoneOwner;

    @Ui(label = "Period")
    private final LocalDateTimeRange timestampRange = new LocalDateTimeRange(ConsumptionSnapshot.Fields.timestamp);

    @Override
    protected void apply(SpecBuilder<ConsumptionSnapshot> spec) {
        spec.eq(phoneOwner != null ? phoneOwner.phoneNr : null, ConsumptionSnapshot.Fields.phoneNr);
        timestampRange.apply(spec);
    }

    public void normalizeDatesToUtc(ZoneId zoneId) {
        timestampRange.setFrom(atUtc(timestampRange.getFrom(), zoneId));
        timestampRange.setTo(atUtc(timestampRange.getTo(), zoneId));
    }

    private LocalDateTime atUtc(LocalDateTime value, ZoneId zoneId) {
        return value == null ? null : LocalDateTime.ofInstant(value.atZone(zoneId).toInstant(), ZoneId.of("UTC"));
    }
}
