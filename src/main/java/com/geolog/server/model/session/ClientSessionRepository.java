package com.geolog.server.model.session;

import com.geolog.server.model.device.Device;
import jmaster.core.repo.AbstractEntityRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ClientSessionRepository extends AbstractEntityRepository<ClientSession> {
    @Query("""
            select session
            from ClientSession session
            left join fetch session.worksite
            where session.device = :device
              and session.stop.recordedAt is null
            """)
    Optional<ClientSession> findFirstByDeviceAndStopRecordedAtIsNull(@Param("device") Device device);

    @Query("select count(session) from ClientSession session where session.stop.recordedAt is null")
    long countActive();

    @Query("""
            select session
            from ClientSession session
            where session.stop.recordedAt is null
              and session.start.recordedAt <= :startedBefore
            """)
    List<ClientSession> findActiveStartedBefore(@Param("startedBefore") Instant startedBefore);

    @Query("""
            select session
            from ClientSession session
            left join fetch session.device device
            left join fetch device.user
            where session.stop.recordedAt is null
            """)
    List<ClientSession> findActiveWithDeviceUser();

    @Query("""
            select session
            from ClientSession session
            left join fetch session.device device
            left join fetch session.worksite
            where device.user.id = :userId
            order by session.created desc
            """)
    List<ClientSession> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);
}
