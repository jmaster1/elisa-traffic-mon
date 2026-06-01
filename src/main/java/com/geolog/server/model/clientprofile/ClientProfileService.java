package com.geolog.server.model.clientprofile;

import jmaster.core.service.AbstractService;
import jmaster.system.user.User;
import jmaster.system.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientProfileService extends AbstractService {

    private final ClientProfileRepository repository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Optional<ClientProfile> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Transactional
    public ClientProfile findOrCreate(User user) {
        return repository.findById(user.getId())
                .orElseGet(() -> create(user));
    }

    @Transactional
    public ClientProfile findOrCreate(Long userId) {
        User user = userService.getUserById(userId);
        return findOrCreate(user);
    }

    @Transactional
    public ClientProfile save(ClientProfile profile) {
        return repository.save(profile);
    }

    private ClientProfile create(User user) {
        ClientProfile profile = new ClientProfile();
        profile.setUser(user);
        return repository.save(profile);
    }
}
