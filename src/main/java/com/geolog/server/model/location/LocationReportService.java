package com.geolog.server.model.location;

import com.geolog.server.model.device.Device;
import com.geolog.server.model.device.DeviceRepository;
import com.geolog.server.model.session.ClientSessionRequest;
import com.geolog.server.model.session.LocationData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class LocationReportService {

    private final DeviceRepository deviceRepository;

    private final LocationEventRepository locationEventRepository;

    public LocationReportService(DeviceRepository deviceRepository, LocationEventRepository locationEventRepository) {
        this.deviceRepository = deviceRepository;
        this.locationEventRepository = locationEventRepository;
    }

    @Transactional
    public LocationEvent report(LocationReportRequest request) {
        Device device = deviceRepository.findByUuid(request.deviceUuid().trim())
                .orElseThrow(() -> new NoSuchElementException("Device not found"));

        LocationEvent event = new LocationEvent();
        event.setDevice(device);
        event.setLocation(LocationData.from(new ClientSessionRequest(
                request.latitude(),
                request.longitude(),
                request.accuracy(),
                request.recordedAt()
        )));

        return locationEventRepository.save(event);
    }
}
