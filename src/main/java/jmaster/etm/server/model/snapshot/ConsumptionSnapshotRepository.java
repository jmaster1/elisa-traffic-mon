package jmaster.etm.server.model.snapshot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface ConsumptionSnapshotRepository extends JpaRepository<ConsumptionSnapshot, Long>,
		JpaSpecificationExecutor<ConsumptionSnapshot>
{
    @Query(value = """
            SELECT snapshot.*
            FROM consumption_snapshot snapshot
            WHERE snapshot.timestamp BETWEEN :from AND :to
              AND NOT EXISTS (
                  SELECT 1
                  FROM consumption_snapshot newer
                  WHERE newer.phone_nr <=> snapshot.phone_nr
                    AND newer.timestamp BETWEEN :from AND :to
                    AND (
                        newer.timestamp > snapshot.timestamp
                        OR (newer.timestamp = snapshot.timestamp AND newer.id > snapshot.id)
                    )
              )
            ORDER BY snapshot.phone_nr
            """, nativeQuery = true)
    List<ConsumptionSnapshot> findLatestByPhoneBetween(
            @Param("from") Date from,
            @Param("to") Date to);
}
