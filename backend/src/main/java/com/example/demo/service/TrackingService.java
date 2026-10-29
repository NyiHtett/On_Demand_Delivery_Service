package com.example.demo.service;

import com.example.demo.dao.DeliveryDao;
import com.example.demo.dto.QueueBatchResponse;
import com.example.demo.dto.RobotRunResponse;
import com.example.demo.dto.TrackingResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackingService {
    private final DeliveryDao deliveryDao;
    private final DeliveryQueueService deliveryQueue;
    private final GoogleRoutesService googleRoutesService;

    public TrackingService(DeliveryDao deliveryDao, DeliveryQueueService deliveryQueue, GoogleRoutesService googleRoutesService) {
        this.deliveryDao = deliveryDao;
        this.deliveryQueue = deliveryQueue;
        this.googleRoutesService = googleRoutesService;
    }

    public TrackingResponse getTracking(String mapApiKey) {
        // Routes are computed when a robot leaves, so remember the employee's key for the dispatcher.
        googleRoutesService.rememberApiKey(mapApiKey);

        QueueBatchResponse queue = deliveryDao.getOpenBatch()
            .map(batch -> new QueueBatchResponse(
                batch.taskId(),
                batch.orderCount(),
                batch.totalWeight(),
                deliveryQueue.getMaxOrders(),
                deliveryQueue.getMaxWeight(),
                batch.openedAt(),
                batch.dispatchAt() == null ? batch.openedAt() : batch.dispatchAt(),
                deliveryDao.getTaskOrders(batch.taskId())))
            .orElse(null);

        List<RobotRunResponse> robots = deliveryDao.getRobotsOnTheRoad().stream()
            .map(run -> new RobotRunResponse(
                run.taskId(),
                run.status(),
                run.startTime(),
                run.durationSeconds(),
                run.distanceMeters(),
                run.encodedPolyline(),
                deliveryDao.getTaskOrders(run.taskId())))
            .toList();

        String routeError;
        if (!googleRoutesService.hasApiKey()) {
            routeError = "No Google Maps API key yet, so robots use estimated stop times and no map is drawn. Enter a Routes API key (or set GOOGLE_MAPS_API_KEY) and the next robot to leave will get a real route.";
        } else if (googleRoutesService.getLastError() != null) {
            routeError = "Google Maps couldn't plan the last robot's route, so it used estimated stop times. " + googleRoutesService.getLastError();
        } else {
            routeError = null;
        }

        return new TrackingResponse(System.currentTimeMillis(), deliveryQueue.getPickupAddress(), queue, robots, routeError,
            googleRoutesService.hasServerApiKey());
    }
}
