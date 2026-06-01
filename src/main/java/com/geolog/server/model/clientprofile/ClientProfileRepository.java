package com.geolog.server.model.clientprofile;

import jmaster.core.repo.AbstractEntityRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ClientProfileRepository extends AbstractEntityRepository<ClientProfile> {

    @Query("select profile from ClientProfile profile left join fetch profile.user where profile.user.id = :userId")
    Optional<ClientProfile> findByUserId(@Param("userId") Long userId);

    @Query("select profile from ClientProfile profile where profile.user.id in :userIds")
    List<ClientProfile> findAllByUserIdIn(@Param("userIds") Collection<Long> userIds);
}
