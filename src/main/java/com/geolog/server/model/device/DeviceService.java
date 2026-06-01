package com.geolog.server.model.device;

import jmaster.core.model.filter.DefaultFilter;
import jmaster.core.service.AbstractService;
import jmaster.core.service.EntityIO;
import jmaster.system.user.User;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeviceService extends AbstractService implements EntityIO<Device, Long, UserDetails> {
    private static final int VERIFICATION_CODE_BOUND = 1_000_000;

    private final DeviceRepository repository;
    private final SecureRandom random = new SecureRandom();

    public DeviceService(DeviceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Device create(String name, User user) {
        Device device = new Device();
        device.setUuid(UUID.randomUUID().toString());
        device.setName(name);
        device.setVerificationCode(generateVerificationCode());
        device.setUser(user);
        return repository.save(device);
    }

    @Transactional
    public Device signup(String verificationCode) {
        Device device = repository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new NoSuchElementException("Invalid verification code"));
        device.setVerificationCode(null);
        device.setVerifiedAt(Instant.now());
        return repository.save(device);
    }

    @Transactional
    public Device generateVerificationCode(Long id) {
        Device device = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Device not found"));
        if (!StringUtils.hasText(device.getVerificationCode())) {
            device.setVerificationCode(generateVerificationCode());
            device = repository.save(device);
        }
        return device;
    }

    @Transactional(readOnly = true)
    public Device requireByUuid(String uuid) {
        return repository.findByUuid(uuid)
                .orElseThrow(() -> new NoSuchElementException("Device not found"));
    }

    @Transactional(readOnly = true)
    public Optional<Device> findByUuid(String uuid) {
        return repository.findByUuid(uuid);
    }

    private String generateVerificationCode() {
        String verificationCode;
        do {
            verificationCode = "%06d".formatted(random.nextInt(VERIFICATION_CODE_BOUND));
        } while (repository.existsByVerificationCode(verificationCode));
        return verificationCode;
    }

    public List<Device> listUserDevices(Long userId) {
        return repository.findAllByUserIdOrderByCreatedDesc(userId);
    }

    @Override
    public Device get(UserDetails userDetails, Long id) {
        return require(repository.findById(id));
    }

    @Transactional
    @Override
    public Device save(UserDetails userDetails, Device entity) {
        return repository.save(entity);
    }

    @Transactional
    @Override
    public void delete(UserDetails userDetails, Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Device> list(DefaultFilter<Device> filter) {
        filter.fetch(Device.Fields.user);
        return page(filter, repository);
    }
}
