package com.geolog.server.model.userworksite;

import com.geolog.server.model.worksite.Worksite;
import com.geolog.server.model.worksite.WorksiteRepository;
import jmaster.core.model.filter.DefaultFilter;
import jmaster.core.service.AbstractService;
import jmaster.core.service.EntityIO;
import jmaster.system.user.User;
import jmaster.system.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserWorksiteService extends AbstractService implements EntityIO<UserWorksite, Long, UserDetails> {

    private final UserRepository userRepository;

    private final WorksiteRepository worksiteRepository;

    private final UserWorksiteRepository userWorksiteRepository;

    public List<User> listMatrixUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, User.Fields.name));
    }

    public List<Worksite> listMatrixWorksites() {
        return worksiteRepository.findAllByOrderByNameAsc();
    }

    public Map<String, Boolean> listMatrixAssignedKeys() {
        return userWorksiteRepository.findAll().stream()
                .map(this::toMatrixKey)
                .collect(Collectors.toMap(Function.identity(), key -> true));
    }

    public List<UserWorksiteOption> listOptions(Long userId) {
        Set<Long> assignedWorksiteIds = new HashSet<>(listAssignedWorksiteIds(userId));
        return worksiteRepository.findAllByOrderByNameAsc().stream()
                .map(worksite -> new UserWorksiteOption(worksite, assignedWorksiteIds.contains(worksite.getId())))
                .toList();
    }

    public List<Long> listAssignedWorksiteIds(Long userId) {
        return userWorksiteRepository.findAllByUserId(userId).stream()
                .map(userWorksite -> userWorksite.getWorksite().getId())
                .toList();
    }

    public List<Worksite> listAssignedWorksites(Long userId) {
        return userWorksiteRepository.findAllByUserId(userId).stream()
                .map(userWorksite -> userWorksite.getWorksite())
                .toList();
    }

    @Transactional
    public void saveUserWorksites(Long userId, Collection<Long> worksiteIds) {
        User user = require(userRepository.findById(userId));
        Set<Long> selectedIds = worksiteIds == null ? new HashSet<>() : new HashSet<>(worksiteIds);

        for (UserWorksite existing : userWorksiteRepository.findAllByUserId(userId)) {
            Long worksiteId = existing.getWorksite().getId();
            if (!selectedIds.remove(worksiteId)) {
                userWorksiteRepository.delete(existing);
            }
        }

        for (Long worksiteId : selectedIds) {
            Worksite worksite = require(worksiteRepository.findById(worksiteId));
            UserWorksite userWorksite = new UserWorksite();
            userWorksite.setUser(user);
            userWorksite.setWorksite(worksite);
            userWorksiteRepository.save(userWorksite);
        }
    }

    @Transactional
    public void saveMatrix(Collection<String> selectedKeys) {
        Set<String> selected = selectedKeys == null ? new HashSet<>() : new HashSet<>(selectedKeys);
        List<UserWorksite> existing = userWorksiteRepository.findAll();
        Set<String> existingKeys = existing.stream()
                .map(this::toMatrixKey)
                .collect(Collectors.toSet());

        for (UserWorksite userWorksite : existing) {
            if (!selected.contains(toMatrixKey(userWorksite))) {
                userWorksiteRepository.delete(userWorksite);
            }
        }

        List<UserWorksite> additions = new ArrayList<>();
        for (String key : selected) {
            if (!existingKeys.contains(key)) {
                additions.add(createUserWorksite(key));
            }
        }
        userWorksiteRepository.saveAll(additions);
    }

    private UserWorksite createUserWorksite(String key) {
        String[] parts = key.split(":", 2);
        Long userId = Long.valueOf(parts[0]);
        Long worksiteId = Long.valueOf(parts[1]);

        UserWorksite userWorksite = new UserWorksite();
        userWorksite.setUser(require(userRepository.findById(userId)));
        userWorksite.setWorksite(require(worksiteRepository.findById(worksiteId)));
        return userWorksite;
    }

    private String toMatrixKey(UserWorksite userWorksite) {
        return toMatrixKey(userWorksite.getUser().getId(), userWorksite.getWorksite().getId());
    }

    private String toMatrixKey(Long userId, Long worksiteId) {
        return userId + ":" + worksiteId;
    }

    @Override
    public UserWorksite get(UserDetails userDetails, Long id) {
        return require(userWorksiteRepository.findById(id));
    }

    @Transactional
    @Override
    public UserWorksite save(UserDetails userDetails, UserWorksite entity) {
        return userWorksiteRepository.save(entity);
    }

    @Transactional
    @Override
    public void delete(UserDetails userDetails, Long id) {
        userWorksiteRepository.deleteById(id);
    }

    @Override
    public Page<UserWorksite> list(DefaultFilter<UserWorksite> filter) {
        return page(filter, userWorksiteRepository);
    }
}
