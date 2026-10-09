package com.example.demo.dto;

import java.util.List;

public record TrackingResponse(
    String routeStatus,
    String pickupAddress,
    List<TrackingOrderResponse> orders,
    Long routeDistanceMeters,
    String routeDuration,
    String encodedPolyline,
    String routeError,
    List<RoutePoint> stopLocations
) {}
