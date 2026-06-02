package jmaster.etm.server.model.snapshot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ConsumptionSnapshotRepository extends JpaRepository<ConsumptionSnapshot, Long>,
		JpaSpecificationExecutor<ConsumptionSnapshot>
{
}
