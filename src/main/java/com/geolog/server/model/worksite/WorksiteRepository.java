package com.geolog.server.model.worksite;

import jmaster.core.repo.AbstractEntityRepository;

import java.util.List;

public interface WorksiteRepository extends AbstractEntityRepository<Worksite> {
    List<Worksite> findAllByOrderByNameAsc();
}
