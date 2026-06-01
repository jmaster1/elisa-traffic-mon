package jmaster.etm.server.repository;

import jmaster.etm.server.model.ConsumptionSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ConsumptionSnapshotRepository extends JpaRepository<ConsumptionSnapshot, Long>,
		JpaSpecificationExecutor<ConsumptionSnapshot>
{
}
