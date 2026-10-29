package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dao.DeliveryDao;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class DeliveryQueueServiceTest {
    DeliveryDao deliveryDao = mock(DeliveryDao.class);
    // 60-second queue unit, 10 orders, 200 lb, like application.properties
    DeliveryQueueService queue = new DeliveryQueueService(deliveryDao, 60, 10, new BigDecimal("200"), "OFS store");

    private DeliveryDao.Batch openBatch(int orders, String weight) {
        return new DeliveryDao.Batch(7L, orders, new BigDecimal(weight), 0L, 0L);
    }

    @Test
    public void timerShrinksOneMinutePerOrderDownToOne() {
        assertEquals(300, DeliveryQueueService.dispatchDelaySeconds(1, 60));
        assertEquals(240, DeliveryQueueService.dispatchDelaySeconds(2, 60));
        assertEquals(180, DeliveryQueueService.dispatchDelaySeconds(3, 60));
        assertEquals(120, DeliveryQueueService.dispatchDelaySeconds(4, 60));
        assertEquals(60, DeliveryQueueService.dispatchDelaySeconds(5, 60));
        assertEquals(60, DeliveryQueueService.dispatchDelaySeconds(9, 60));
    }

    @Test
    public void firstOrderOpensBatchWithFiveMinuteTimer() {
        when(deliveryDao.lockOpenBatch()).thenReturn(Optional.empty());
        when(deliveryDao.createBatch("OFS store")).thenReturn(1L);

        queue.enqueue(100L, new BigDecimal("5"));

        verify(deliveryDao).addOrderToBatch(1L, 100L, new BigDecimal("5"));
        verify(deliveryDao).setDispatchAfterOpen(1L, 300);
        verify(deliveryDao, never()).dispatchNow(anyLong());
    }

    @Test
    public void secondOrderJoinsBatchAndShortensTimerToFourMinutes() {
        when(deliveryDao.lockOpenBatch()).thenReturn(Optional.of(openBatch(1, "5")));

        queue.enqueue(101L, new BigDecimal("3"));

        verify(deliveryDao, never()).createBatch(anyString());
        verify(deliveryDao).addOrderToBatch(7L, 101L, new BigDecimal("3"));
        verify(deliveryDao).setDispatchAfterOpen(7L, 240);
    }

    @Test
    public void tenthOrderSendsRobotImmediately() {
        when(deliveryDao.lockOpenBatch()).thenReturn(Optional.of(openBatch(9, "40")));

        queue.enqueue(110L, new BigDecimal("2"));

        verify(deliveryDao).addOrderToBatch(7L, 110L, new BigDecimal("2"));
        verify(deliveryDao).dispatchNow(7L);
    }

    @Test
    public void reachingExactly200PoundsSendsRobotImmediately() {
        when(deliveryDao.lockOpenBatch()).thenReturn(Optional.of(openBatch(2, "150")));

        queue.enqueue(120L, new BigDecimal("50"));

        verify(deliveryDao).addOrderToBatch(7L, 120L, new BigDecimal("50"));
        verify(deliveryDao).dispatchNow(7L);
    }

    @Test
    public void orderThatWouldOverload200PoundsGoesToNewBatch() {
        when(deliveryDao.lockOpenBatch()).thenReturn(Optional.of(openBatch(3, "180")));
        when(deliveryDao.createBatch("OFS store")).thenReturn(8L);

        queue.enqueue(130L, new BigDecimal("30"));

        // full batch leaves now, the heavy order starts a fresh 5-minute batch
        verify(deliveryDao).dispatchNow(7L);
        verify(deliveryDao).addOrderToBatch(8L, 130L, new BigDecimal("30"));
        verify(deliveryDao).setDispatchAfterOpen(8L, 300);
    }

    @Test
    public void cancellingWaitingOrderResetsTimerForRemainingOrders() {
        when(deliveryDao.getWaitingBatchId(130L)).thenReturn(7L);
        when(deliveryDao.removeFromWaitingBatch(130L)).thenReturn(2);

        assertTrue(queue.removeFromQueue(130L));

        verify(deliveryDao).setDispatchAfterOpen(7L, 240);
    }

    @Test
    public void cancellingOrderNotInQueueDoesNothing() {
        when(deliveryDao.removeFromWaitingBatch(140L)).thenReturn(null);

        assertFalse(queue.removeFromQueue(140L));

        verify(deliveryDao, never()).setDispatchAfterOpen(anyLong(), anyLong());
    }
}
