package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.example.demo.dao.OrderDao;
import com.example.demo.dto.UpdateOrderStatusRequest;
import com.example.demo.exception.ApiException;

public class OrderServiceTest{
    OrderDao orderDao = mock(OrderDao.class);
    OrderService orderService = new OrderService(orderDao);

    @Test
    public void updateSavesValidChange() {
        when(orderDao.getOrderStatus(1L)).thenReturn("Ordered");

        orderService.updateOrderStatus(1L, new UpdateOrderStatusRequest("Shipping"));

        verify(orderDao).updateOrderStatus(1L, "Shipping");
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
}