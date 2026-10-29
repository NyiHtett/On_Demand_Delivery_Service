package com.example.demo.dto;

import java.util.List;

// serverTime lets the dashboard line its countdowns up with the server clock.
public record TrackingResponse(
    long serverTime,
    String pickupAddress,
    QueueBatchResponse queue,
    List<RobotRunResponse> robots,
    String routeError,
    // True when GOOGLE_MAPS_API_KEY is set on the server, so the page doesn't need to ask for a key.
    boolean serverHasMapsKey
) {}
