package com.geolog.server.controller.client;

import com.geolog.server.model.location.LocationReportRequest;
import com.geolog.server.model.location.LocationReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@RestController
public class LocationReportController {

    private final LocationReportService locationReportService;

    public LocationReportController(LocationReportService locationReportService) {
        this.locationReportService = locationReportService;
    }

    @PostMapping("/api/locations/report")
    public ResponseEntity<Void> report(@Valid @RequestBody LocationReportRequest request) {
        locationReportService.report(request);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<String> handleNotFound(NoSuchElementException error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
    }
}
