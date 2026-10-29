package com.example.demo.service;

import com.example.demo.dao.DeliveryDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Groups paid orders into robot batches.
 *
 * Rule: the first order opens a batch and starts a 5-minute timer (measured from when the batch opened).
 * Each later order shortens it by a minute: 2 orders -> 4 min, 3 -> 3 min, down to 1 min.
 * If the shortened time has already passed, the batch leaves right away.
 * A batch also leaves immediately when it reaches 10 orders or 200 lb, and an order that
 * would push it over either limit goes into a new batch instead.
 */
@Service
public class DeliveryQueueService {
    static final int FIRST_ORDER_UNITS = 5;
    static final int MIN_UNITS = 1;

    private final DeliveryDao deliveryDao;
    private final int queueUnitSeconds;
    private final int maxOrders;
    private final BigDecimal maxWeight;
    private final String pickupAddress;

    public DeliveryQueueService(
        DeliveryDao deliveryDao,
        @Value("${robot.queue-unit-seconds:60}") int queueUnitSeconds,
        @Value("${robot.max-orders:10}") int maxOrders,
        @Value("${robot.max-weight-lbs:200}") BigDecimal maxWeight,
        @Value("${robot.pickup-address}") String pickupAddress
    ) {
        this.deliveryDao = deliveryDao;
        this.queueUnitSeconds = queueUnitSeconds;
        this.maxOrders = maxOrders;
        this.maxWeight = maxWeight;
        this.pickupAddress = pickupAddress;
    }

    /** Seconds after the batch opened that a batch holding orderCount orders should leave. */
    public static long dispatchDelaySeconds(int orderCount, int unitSeconds) {
        int units = Math.max(MIN_UNITS, FIRST_ORDER_UNITS - (orderCount - 1));
        return (long) units * unitSeconds;
    }

    public int getMaxOrders() {
        return maxOrders;
    }

    public BigDecimal getMaxWeight() {
        return maxWeight;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    @Transactional
    public synchronized void enqueue(long orderId, BigDecimal orderWeight) {
        DeliveryDao.Batch open = deliveryDao.lockOpenBatch().orElse(null);

        if (open != null && (open.orderCount() + 1 > maxOrders || open.totalWeight().add(orderWeight).compareTo(maxWeight) > 0)) {
            // This order doesn't fit: send the full batch now and start a new one.
            deliveryDao.dispatchNow(open.taskId());
            open = null;
        }

        long taskId = open == null ? deliveryDao.createBatch(pickupAddress) : open.taskId();
        int orderCount = (open == null ? 0 : open.orderCount()) + 1;
        BigDecimal totalWeight = (open == null ? BigDecimal.ZERO : open.totalWeight()).add(orderWeight);
        deliveryDao.addOrderToBatch(taskId, orderId, orderWeight);

        if (orderCount >= maxOrders || totalWeight.compareTo(maxWeight) >= 0) {
            deliveryDao.dispatchNow(taskId);
        } else {
            deliveryDao.setDispatchAfterOpen(taskId, dispatchDelaySeconds(orderCount, queueUnitSeconds));
        }
    }

    /** Returns true if the order was waiting at the store and has been taken off its batch. */
    @Transactional
    public synchronized boolean removeFromQueue(long orderId) {
        Long taskId = deliveryDao.getWaitingBatchId(orderId);
        Integer remaining = deliveryDao.removeFromWaitingBatch(orderId);
        if (remaining == null) return false;
        // Fewer orders means a longer wait again, counted from when the batch opened.
        if (remaining > 0) deliveryDao.setDispatchAfterOpen(taskId, dispatchDelaySeconds(remaining, queueUnitSeconds));
        return true;
    }
}
