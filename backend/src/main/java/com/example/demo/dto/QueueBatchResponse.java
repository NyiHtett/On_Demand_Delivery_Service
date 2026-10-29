package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

// The batch currently filling up at the store, waiting for its timer or capacity limit.
public record QueueBatchResponse(
    long taskId,
    int orderCount,
    BigDecimal totalWeight,
    int maxOrders,
    BigDecimal maxWeight,
    long openedAt,
    long dispatchAt,
    List<TrackingOrderResponse> orders
) {}
