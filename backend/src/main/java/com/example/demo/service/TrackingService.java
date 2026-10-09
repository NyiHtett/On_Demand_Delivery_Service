package com.example.demo.service;

import com.example.demo.dao.TrackingDao;
import com.example.demo.dto.TrackingResponse;
import com.example.demo.dto.GoogleRouteResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import com.example.demo.dto.RoutePoint;

@Service
public class TrackingService {
    private final TrackingDao trackingDao;
    private final GoogleRoutesService googleRoutesService;

    public TrackingService(TrackingDao trackingDao, GoogleRoutesService googleRoutesService) {
        this.trackingDao = trackingDao;
        this.googleRoutesService = googleRoutesService;
    }

    public TrackingResponse getTracking() {
        String pickupAddress = trackingDao.getPickupAddress();
        List<com.example.demo.dto.TrackingOrderResponse> orders = trackingDao.getActiveOrders();
        Long distanceMeters = null;
        String duration = null;
        String encodedPolyline = null;
        String routeError = null;
        List<RoutePoint> stopLocations = List.of();

        if (pickupAddress != null && !orders.isEmpty()) {
            try {
                GoogleRouteResponse route = googleRoutesService.calculateRoute(pickupAddress, orders);
                if (route != null) {
                    distanceMeters = route.distanceMeters();
                    duration = route.duration();
                    encodedPolyline = route.encodedPolyline();
                    stopLocations = route.stopLocations();
                    orders = reorderOrders(orders, route.optimizedIntermediateWaypointIndex());
                }
            } catch (IllegalStateException exception) {
                routeError = exception.getMessage();
            }
        }

        return new TrackingResponse(trackingDao.getRouteStatus(), pickupAddress, orders,
            distanceMeters, duration, encodedPolyline, routeError, stopLocations);
    }

    private List<com.example.demo.dto.TrackingOrderResponse> reorderOrders(
        List<com.example.demo.dto.TrackingOrderResponse> orders,
        List<Integer> indexes
    ) {
        if (indexes == null || indexes.size() != orders.size() - 1) return orders;
        List<com.example.demo.dto.TrackingOrderResponse> reordered = new ArrayList<>();
        for (Integer index : indexes) {
            if (index == null || index < 0 || index >= orders.size()) return orders;
            reordered.add(orders.get(index));
        }
        // The final destination is intentionally fixed as the last stop so the
        // route does not return to the pickup location.
        reordered.add(orders.get(orders.size() - 1));
        return reordered;
    }
}
