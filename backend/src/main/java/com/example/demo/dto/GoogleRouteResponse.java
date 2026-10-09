package com.example.demo.dto;

import java.util.List;

public record GoogleRouteResponse(
    long distanceMeters,
    String duration,
    String encodedPolyline,
    List<Integer> optimizedIntermediateWaypointIndex,
    List<RoutePoint> stopLocations
) {}
