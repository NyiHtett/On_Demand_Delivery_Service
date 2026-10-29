package com.example.demo.service;

import com.example.demo.dao.DeliveryDao;
import com.example.demo.dto.GoogleRouteResponse;
import com.example.demo.dto.RoutePoint;
import com.example.demo.dto.TrackingOrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulated robot fleet. Every couple of seconds it:
 *  1. sends out batches whose queue timer has run out (orders -> Shipping, route + ETAs saved),
 *  2. marks each order Completed once the robot reaches its stop,
 *  3. marks the batch Completed once the whole route is done.
 * Routes come from Google Routes when a key is available; otherwise each stop gets a fixed time.
 */
@Component
public class RobotDispatcher {
    private static final Logger log = LoggerFactory.getLogger(RobotDispatcher.class);
    private static final long MIN_LEG_SECONDS = 10;

    private final DeliveryDao deliveryDao;
    private final GoogleRoutesService googleRoutesService;
    private final TransactionTemplate transactionTemplate;
    private final double timeScale;
    private final int fallbackMinutesPerStop;

    public RobotDispatcher(
        DeliveryDao deliveryDao,
        GoogleRoutesService googleRoutesService,
        PlatformTransactionManager transactionManager,
        @Value("${robot.time-scale:1}") double timeScale,
        @Value("${robot.fallback-minutes-per-stop:5}") int fallbackMinutesPerStop
    ) {
        this.deliveryDao = deliveryDao;
        this.googleRoutesService = googleRoutesService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.timeScale = timeScale <= 0 ? 1 : timeScale;
        this.fallbackMinutesPerStop = fallbackMinutesPerStop;
    }

    @Scheduled(fixedDelay = 2000, initialDelay = 3000)
    public void tick() {
        try {
            for (long taskId : deliveryDao.getDueBatchIds()) {
                dispatch(taskId);
            }
            deliveryDao.completeArrivedOrders();
            deliveryDao.completeFinishedTasks();
        } catch (RuntimeException exception) {
            log.error("Robot dispatcher tick failed", exception);
        }
    }

    void dispatch(long taskId) {
        List<TrackingOrderResponse> orders = deliveryDao.getTaskOrders(taskId);
        if (orders.isEmpty()) return;

        // The Google call happens outside the transaction so no rows stay locked during the HTTP request.
        GoogleRouteResponse route = null;
        try {
            route = googleRoutesService.calculateRoute(null, deliveryDao.getPickupAddress(taskId), orders);
        } catch (IllegalStateException exception) {
            log.warn("Robot {}: route lookup failed, using estimated stop times. {}", taskId, exception.getMessage());
        }

        List<TrackingOrderResponse> stops = orders;
        List<RoutePoint> locations = List.of();
        List<Long> offsets = new ArrayList<>();
        Integer distanceMeters = null;
        String polyline = null;

        if (route != null && route.legDurationSeconds().size() == orders.size()) {
            stops = reorder(orders, route.optimizedIntermediateWaypointIndex());
            locations = route.stopLocations();
            distanceMeters = (int) route.distanceMeters();
            polyline = route.encodedPolyline();
            long elapsed = 0;
            for (long legSeconds : route.legDurationSeconds()) {
                elapsed += Math.max(MIN_LEG_SECONDS, Math.round(legSeconds / timeScale));
                offsets.add(elapsed);
            }
        } else {
            long perStop = Math.max(MIN_LEG_SECONDS, Math.round(fallbackMinutesPerStop * 60 / timeScale));
            for (int index = 1; index <= orders.size(); index++) {
                offsets.add(perStop * index);
            }
        }

        List<TrackingOrderResponse> finalStops = stops;
        List<RoutePoint> finalLocations = locations;
        Integer finalDistance = distanceMeters;
        String finalPolyline = polyline;
        transactionTemplate.executeWithoutResult(status -> {
            int duration = offsets.get(offsets.size() - 1).intValue();
            if (deliveryDao.startTask(taskId, duration, finalDistance, finalPolyline) == 0) return;
            for (int index = 0; index < finalStops.size(); index++) {
                RoutePoint point = index < finalLocations.size() ? finalLocations.get(index) : null;
                deliveryDao.setStop(taskId, finalStops.get(index).orderId(), index + 1,
                    point == null ? null : point.latitude(), point == null ? null : point.longitude(), offsets.get(index));
            }
            deliveryDao.markTaskOrdersShipping(taskId);
        });
        log.info("Robot {} dispatched with {} order(s), route {}", taskId, orders.size(), polyline == null ? "estimated" : "from Google");
    }

    // Google returns the visiting order of the intermediate stops; the last order stays the final destination.
    static List<TrackingOrderResponse> reorder(List<TrackingOrderResponse> orders, List<Integer> indexes) {
        if (indexes == null || indexes.size() != orders.size() - 1) return orders;
        List<TrackingOrderResponse> reordered = new ArrayList<>();
        for (Integer index : indexes) {
            if (index == null || index < 0 || index >= orders.size() - 1) return orders;
            reordered.add(orders.get(index));
        }
        reordered.add(orders.get(orders.size() - 1));
        return reordered;
    }
}
