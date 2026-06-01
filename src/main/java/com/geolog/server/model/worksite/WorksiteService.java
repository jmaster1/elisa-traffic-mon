package com.geolog.server.model.worksite;

import jmaster.core.model.filter.DefaultFilter;
import jmaster.core.service.AbstractService;
import jmaster.core.service.EntityIO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorksiteService extends AbstractService implements EntityIO<Worksite, Long, UserDetails> {

    private final WorksiteRepository repository;

    @Override
    public Worksite get(UserDetails userDetails, Long id) {
        return require(repository.findById(id));
    }

    @Transactional
    @Override
    public Worksite save(UserDetails userDetails, Worksite entity) {
        return repository.save(entity);
    }

    @Transactional
    @Override
    public void delete(UserDetails userDetails, Long id) {
        repository.deleteById(id);
    }

    @Override
    public Page<Worksite> list(DefaultFilter<Worksite> filter) {
        return page(filter, repository);
    }
}
