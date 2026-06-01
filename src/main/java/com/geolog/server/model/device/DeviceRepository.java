package com.geolog.server.model.device;

import jmaster.core.repo.AbstractEntityRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends AbstractEntityRepository<Device> {
    @Query("select device from Device device left join fetch device.user where device.uuid = :uuid")
    Optional<Device> findByUuid(String uuid);

    @Query("select device from Device device left join fetch device.user where device.verificationCode = :verificationCode")
    Optional<Device> findByVerificationCode(String verificationCode);

    boolean existsByVerificationCode(String verificationCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select device from Device device where device.id = :id")
    Optional<Device> findByIdForUpdate(@Param("id") Long id);

    @Query("select device from Device device left join fetch device.user order by device.created desc")
    List<Device> findAllWithUserOrderByCreatedDesc();

    @Query("select device from Device device where device.user.id = :userId order by device.created desc")
    List<Device> findAllByUserIdOrderByCreatedDesc(@Param("userId") Long userId);

    long countByVerifiedAtIsNotNull();
}
