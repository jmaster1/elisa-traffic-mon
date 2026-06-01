package com.geolog.server.model.userworksite;

import jmaster.core.repo.AbstractEntityRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserWorksiteRepository extends AbstractEntityRepository<UserWorksite> {

    @Query("select userWorksite from UserWorksite userWorksite left join fetch userWorksite.worksite where userWorksite.user.id = :userId order by userWorksite.worksite.name")
    List<UserWorksite> findAllByUserId(@Param("userId") Long userId);

    @Query("select userWorksite from UserWorksite userWorksite left join fetch userWorksite.user where userWorksite.worksite.id = :worksiteId order by userWorksite.user.name")
    List<UserWorksite> findAllByWorksiteId(@Param("worksiteId") Long worksiteId);
}
