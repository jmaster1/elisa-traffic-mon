package jmaster.etm.server.model.snapshot;

import jmaster.core.repo.JpaSpecRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ConsumptionSnapshotRepository extends JpaSpecRepository<ConsumptionSnapshot, Long>
{
    @Query(value = """
        SELECT
            x.id,
            x.created,
            x.phone_nr,
            x.timestamp,
            x.used_gb
        FROM (
            SELECT
                cs.*,
                ROW_NUMBER() OVER (
                    PARTITION BY cs.phone_nr
                    ORDER BY cs.timestamp DESC, cs.id DESC
                ) AS rn
            FROM consumption_snapshot cs
            WHERE cs.timestamp >= :from
              AND cs.timestamp < :to
        ) x
        WHERE x.rn = 1
        ORDER BY x.phone_nr
        """, nativeQuery = true)
    List<ConsumptionSnapshot> findLatestByPhoneBetween(
            @Param("from") Instant from,
            @Param("to") Instant to);
}
