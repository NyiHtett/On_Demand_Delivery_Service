package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.example.demo.dao.OrderDao;
import com.example.demo.dto.OrderResponse;
import com.example.demo.dto.UpdateOrderStatusRequest;
import com.example.demo.exception.ApiException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;

public class OrderServiceTest{
    OrderDao orderDao = mock(OrderDao.class);
    DeliveryQueueService deliveryQueue = mock(DeliveryQueueService.class);
    StripePaymentService stripe = mock(StripePaymentService.class);
    OrderService orderService = new OrderService(orderDao, deliveryQueue, stripe);

    // order 5 belongs to customer 1
    private OrderResponse order(String status) {
        return new OrderResponse(5L, 1L, 3L, status, "777 The Alameda", new BigDecimal("2.00"),
            BigDecimal.ZERO, new BigDecimal("0.800"), List.of(), null, null, null);
    }

    @Test
    public void updateSavesValidChange() {
        when(orderDao.getOrderStatus(1L)).thenReturn("Pending");

        orderService.updateOrderStatus(1L, new UpdateOrderStatusRequest("Ordered"));

        verify(orderDao).updateOrderStatus(1L, "Ordered");
    }

    @Test
    public void updateRejectsShippingStatus() {
        when(orderDao.getOrderStatus(1L)).thenReturn("Shipping");

        assertThrows(ApiException.class, () -> orderService.updateOrderStatus(1L, new UpdateOrderStatusRequest("Cancelled")));

        //since it hits an error the orderDao shouldn't be able to call the last function to update
        verify(orderDao, never()).updateOrderStatus(anyLong(), anyString());
    }

    @Test
    public void updateRejectsCompleteStatus() {
        when(orderDao.getOrderStatus(1L)).thenReturn("Completed");

        assertThrows(ApiException.class, () -> orderService.updateOrderStatus(1L, new UpdateOrderStatusRequest("Cancelled")));

        verify(orderDao, never()).updateOrderStatus(anyLong(), anyString());
    }

    @Test 
    public void updateRejectsNullOrder() {
        when(orderDao.getOrderStatus(90L)).thenReturn(null);

        assertThrows(ApiException.class, () -> orderService.updateOrderStatus(90L, new UpdateOrderStatusRequest("Cancelled")));

        verify(orderDao, never()).updateOrderStatus(anyLong(), anyString());
    }

    @Test
    public void updateRejectsInvalidStatus() {
        assertThrows(ApiException.class, () -> orderService.updateOrderStatus(1L, new UpdateOrderStatusRequest("testing")));

        verify(orderDao, never()).updateOrderStatus(anyLong(), anyString());
    }

    @Test public void updateRejectsBlankStatus() {
        assertThrows(ApiException.class, () -> orderService.updateOrderStatus(1L, new UpdateOrderStatusRequest("  ")));

        verify(orderDao, never()).updateOrderStatus(anyLong(), anyString());
    }

    @Test
    public void customerCantSeeSomeoneElsesOrder() {
        when(orderDao.getOrder(5L)).thenReturn(Optional.of(order("Ordered")));

        ApiException error = assertThrows(ApiException.class, () -> orderService.getOrderForCustomer(2L, 5L));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    public void customerCantCancelShippingOrder() {
        when(orderDao.getOrder(5L)).thenReturn(Optional.of(order("Shipping")));

        assertThrows(ApiException.class, () -> orderService.cancelForCustomer(1L, 5L));

        verify(orderDao, never()).updateOrderStatus(anyLong(), anyString());
    }

    @Test
    public void customerCancelRemovesFromRobotRestocksAndRefunds() throws Exception {
        when(orderDao.getOrder(5L)).thenReturn(Optional.of(order("Ordered")));
        when(orderDao.getPaymentIntentId(5L)).thenReturn("pi_123");
        when(stripe.isEnabled()).thenReturn(true);

        orderService.cancelForCustomer(1L, 5L);

        verify(deliveryQueue).removeFromQueue(5L);
        verify(orderDao).updateOrderStatus(5L, "Cancelled");
        verify(orderDao).restock(5L);
        verify(stripe).refund("pi_123");
    }
}
