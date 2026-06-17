package jmaster.etm.server.model.snapshot;

import jmaster.core.service.AbstractService;
import jmaster.core.service.EntityIO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsumptionSnapshotService extends AbstractService {

    private final ConsumptionSnapshotRepository repository;

    public EntityIO<ConsumptionSnapshot, Long> getEntityIO() {
        return createEntityIO(repository);
    }
}
