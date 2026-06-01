package com.geolog.server.model.session;

import com.geolog.server.model.clientprofile.ClientProfile;
import com.geolog.server.model.clientprofile.ClientProfileRepository;
import com.geolog.server.model.device.Device;
import com.geolog.server.model.device.DeviceRepository;
import com.geolog.server.model.userworksite.UserWorksiteService;
import com.geolog.server.model.worksite.Worksite;
import jmaster.core.model.filter.DefaultFilter;
import jmaster.core.service.AbstractService;
import jmaster.core.service.EntityIO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientSessionService extends AbstractService implements EntityIO<ClientSession, Long, UserDetails> {
    private final ClientSessionRepository repository;
    private final DeviceRepository deviceRepository;
    private final UserWorksiteService userWorksiteService;
    private final ClientProfileRepository clientProfileRepository;

    @Transactional(readOnly = true)
    public Optional<ClientSession> findActive(Device device) {
        return repository.findFirstByDeviceAndStopRecordedAtIsNull(device);
    }

    @Transactional(readOnly = true)
    public List<ClientSession> listRecentByUserId(Long userId, int count) {
        return repository.findRecentByUserId(userId, PageRequest.of(0, Math.max(count, 1)));
    }

    @Transactional
    public ClientSession start(Device device, ClientSessionRequest request) {
        lockDevice(device);
        if (repository.findFirstByDeviceAndStopRecordedAtIsNull(device).isPresent()) {
            throw new IllegalStateException("Client session is already started");
        }

        LocationData start = LocationData.from(request);
        Worksite worksite = nearestWorksiteInZone(device, start)
                .orElseThrow(() -> new IllegalStateException("Start is allowed only inside an assigned worksite zone"));

        ClientSession session = new ClientSession();
        session.setDevice(device);
        session.setWorksite(worksite);
        session.setStart(start);

        return repository.save(session);
    }

    @Transactional
    public ClientSession stop(Device device, ClientSessionRequest request) {
        lockDevice(device);
        ClientSession session = repository.findFirstByDeviceAndStopRecordedAtIsNull(device)
                .orElseThrow(() -> new IllegalStateException("Client session is not started"));

        LocationData stop = LocationData.from(request);
        if (!canStopAtSessionWorksite(session, stop)) {
            throw new IllegalStateException("Stop is allowed only inside the worksite where the session was started");
        }

        session.setStop(stop);
        session.setStopReason(StopReason.userAction);

        return repository.save(session);
    }

    private boolean canStopAtSessionWorksite(ClientSession session, LocationData stop) {
        Worksite worksite = session.getWorksite();
        return worksite != null && isInsideZone(worksite, stop);
    }

    @Transactional
    public int closeExpired(Integer defaultExpirationMinutes) {
        List<ClientSession> sessions = repository.findActiveWithDeviceUser();
        Map<Long, ClientProfile> profiles = loadProfiles(sessions);
        Instant now = Instant.now();
        int closed = 0;

        for (ClientSession session : sessions) {
            Instant startedAt = session.getStart().getRecordedAt();
            Integer expirationMinutes = resolveExpirationMinutes(session, profiles, defaultExpirationMinutes);
            if (startedAt == null || expirationMinutes == null || expirationMinutes <= 0) {
                continue;
            }

            Instant expiredAt = startedAt.plusSeconds(expirationMinutes * 60L);
            if (expiredAt.isAfter(now)) {
                continue;
            }

            session.setStop(new LocationData(expiredAt, now, null, null, null));
            session.setStopReason(StopReason.timeout);
            closed++;
        }

        return closed;
    }

    @Override
    @Transactional(readOnly = true)
    public ClientSession get(UserDetails userDetails, Long id) {
        return require(repository.findById(id));
    }

    @Override
    @Transactional
    public ClientSession save(UserDetails userDetails, ClientSession entity) {
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(UserDetails userDetails, Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientSession> list(DefaultFilter<ClientSession> filter) {
        return page(filter, repository);
    }

    private void lockDevice(Device device) {
        deviceRepository.findByIdForUpdate(device.getId())
                .orElseThrow(() -> new IllegalStateException("Device not found"));
    }

    private Map<Long, ClientProfile> loadProfiles(List<ClientSession> sessions) {
        Set<Long> userIds = sessions.stream()
                .map(session -> session.getDevice() == null || session.getDevice().getUser() == null
                        ? null
                        : session.getDevice().getUser().getId())
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Map.of();
        }

        return clientProfileRepository.findAllByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), Function.identity()));
    }

    private Integer resolveExpirationMinutes(
            ClientSession session,
            Map<Long, ClientProfile> profiles,
            Integer defaultExpirationMinutes
    ) {
        Long userId = session.getDevice() == null || session.getDevice().getUser() == null
                ? null
                : session.getDevice().getUser().getId();
        ClientProfile profile = userId == null ? null : profiles.get(userId);
        return profile != null && profile.getSessionExpirationMinutes() != null
                ? profile.getSessionExpirationMinutes()
                : defaultExpirationMinutes;
    }

    private Optional<Worksite> nearestWorksiteInZone(Device device, LocationData location) {
        Long userId = device.getUser() == null ? null : device.getUser().getId();
        if (userId == null) {
            return Optional.empty();
        }

        return userWorksiteService.listAssignedWorksites(userId).stream()
                .filter(worksite -> isInsideZone(worksite, location))
                .min(Comparator.comparingDouble(worksite -> distanceToCenterMeters(location, worksite)));
    }

    private boolean isInsideZone(Worksite worksite, LocationData location) {
        return zoneDistanceMeters(location, worksite) == 0;
    }

    private double zoneDistanceMeters(LocationData location, Worksite worksite) {
        if (location.getLatitude() == null
                || location.getLongitude() == null
                || worksite.getLatitude() == null
                || worksite.getLongitude() == null
                || worksite.getRadiusMeters() == null) {
            return Double.POSITIVE_INFINITY;
        }

        return Math.max(distanceToCenterMeters(location, worksite) - worksite.getRadiusMeters(), 0);
    }

    private double distanceToCenterMeters(LocationData location, Worksite worksite) {
        double earthRadiusMeters = 6_371_000;
        double deltaLatitude = Math.toRadians(worksite.getLatitude() - location.getLatitude());
        double deltaLongitude = Math.toRadians(worksite.getLongitude() - location.getLongitude());
        double fromLatitude = Math.toRadians(location.getLatitude());
        double toLatitude = Math.toRadians(worksite.getLatitude());
        double haversine = Math.pow(Math.sin(deltaLatitude / 2), 2)
                + Math.cos(fromLatitude) * Math.cos(toLatitude) * Math.pow(Math.sin(deltaLongitude / 2), 2);

        return earthRadiusMeters * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }
}
