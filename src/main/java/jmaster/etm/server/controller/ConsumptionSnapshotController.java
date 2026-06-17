package jmaster.etm.server.controller;

import jmaster.core.controller.AbstractEntityController;
import jmaster.core.model.AbstractEntity;
import jmaster.core.model.filter.DefaultFilter;
import jmaster.etm.server.model.PhoneOwner;
import jmaster.etm.server.model.report.ConsumptionReportFilter;
import jmaster.etm.server.model.snapshot.ConsumptionSnapshot;
import jmaster.etm.server.model.snapshot.ConsumptionSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/consumption")
@RequiredArgsConstructor
public class ConsumptionSnapshotController
        extends AbstractEntityController<Long, ConsumptionSnapshot, ConsumptionReportFilter> {

    private final ConsumptionSnapshotService service;

    @Override
    protected void configure() {
        config
                .setEntityIO(service.getEntityIO())
                .filterDefaultSort(ConsumptionSnapshot.Fields.timestamp, DefaultFilter.SORT_DESC)
                .pageModel(model -> {
                    model.useProperties(
                            AbstractEntity.Fields.id,
                            AbstractEntity.Fields.created,
                            ConsumptionSnapshot.Fields.phoneNr,
                            ConsumptionSnapshot.Fields.usedGb,
                            ConsumptionSnapshot.Fields.timestamp);
                    model.setTextValueSupplier(
                            ConsumptionSnapshot.Fields.phoneNr,
                            snapshot -> PhoneOwner.getPhoneLabel(snapshot.getPhoneNr()));
                });
    }
}
