package com.example.demo.controller;

import com.example.demo.dto.TrackingResponse;
import com.example.demo.service.TrackingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/manager")
public class TrackingController {
    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @GetMapping("/tracking")
    public TrackingResponse getTracking() {
        return trackingService.getTracking();
    }
}
