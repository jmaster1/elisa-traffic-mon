package jmaster.etm.server.model;

import jmaster.core.model.LocalDateTimeRange;
import jmaster.core.model.filter.DefaultFilter;
import jmaster.core.ui.InputAttr;
import jmaster.core.ui.InputType;
import jmaster.core.ui.annot.Ui;
import jmaster.core.ui.annot.UiInputAttr;
import jmaster.core.util.LangHelper;
import jmaster.core.util.jpa.SpecBuilder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZoneId;
import java.util.Date;

@Data
@Accessors(chain = true)
public class ConsumptionReportFilter extends DefaultFilter<ConsumptionSnapshot> {

    private static final ZoneId REPORT_ZONE_ID = ZoneId.of("Europe/Tallinn");

    private PhoneOwner phoneOwner;

    @Ui(label = "Period")
    private final LocalDateTimeRange timestampRange = new LocalDateTimeRange(ConsumptionSnapshot.Fields.timestamp);

    @Ui(label = "Max points", inputType = InputType.number, inputAttrs = {
            @UiInputAttr(attr = InputAttr.MIN, value = "1"),
            @UiInputAttr(attr = InputAttr.MAX, value = "10000")
    })
    private Integer maxPoints = 1000;

    @Override
    protected void apply(SpecBuilder<ConsumptionSnapshot> spec) {
        if (phoneOwner != null) {
            spec.eq(phoneOwner.phoneNr, ConsumptionSnapshot.Fields.phoneNr);
        }
        spec.between(
                timestampRange.getFrom() == null ? null : Date.from(timestampRange.getFrom().atZone(REPORT_ZONE_ID).toInstant()),
                timestampRange.getTo() == null ? null : Date.from(timestampRange.getTo().atZone(REPORT_ZONE_ID).toInstant()),
                Date.class,
                ConsumptionSnapshot.Fields.timestamp);
    }

    public int resolveMaxPoints() {
        return Math.max(1, Math.min(LangHelper.nvl(maxPoints, 1000), 10000));
    }
}
