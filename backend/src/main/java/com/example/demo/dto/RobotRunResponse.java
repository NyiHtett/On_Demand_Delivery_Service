package com.example.demo.dto;

import java.util.List;

// A robot that has left the store. Its position is (now - startTime) / durationSeconds along the polyline.
public record RobotRunResponse(
    long taskId,
    String status,
    long startTime,
    Integer durationSeconds,
    Integer distanceMeters,
    String encodedPolyline,
    List<TrackingOrderResponse> stops
) {}
