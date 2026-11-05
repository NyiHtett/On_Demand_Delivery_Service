package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dao.CartDao;
import com.example.demo.dao.OrderDao;
import com.example.demo.dao.ProductDao;
import com.example.demo.dao.UserDao;
import com.example.demo.dto.ConfirmCheckoutRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.exception.ApiException;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;

public class CheckoutServiceTest {
    CartDao cartDao = mock(CartDao.class);
    OrderDao orderDao = mock(OrderDao.class);
    StripePaymentService stripe = mock(StripePaymentService.class);
    DeliveryQueueService deliveryQueue = mock(DeliveryQueueService.class);
    CheckoutService checkoutService = new CheckoutService(cartDao, mock(ProductDao.class), orderDao, mock(UserDao.class),
        stripe, deliveryQueue, mock(PlatformTransactionManager.class), "http://localhost:5173");

    BigDecimal weight = new BigDecimal("0.800");

    // order 7 in cart 3, owned by the given customer
    private OrderResponse order(long customerId, String status) {
        return new OrderResponse(7L, customerId, 3L, status, "777 The Alameda", new BigDecimal("2.00"),
            BigDecimal.ZERO, weight, List.of(), null, null, null);
    }

    // stripe says the session is paid or not
    private void stripeSession(String paymentStatus) throws Exception {
        Session session = mock(Session.class);
        when(session.getPaymentStatus()).thenReturn(paymentStatus);
        when(session.getPaymentIntent()).thenReturn("pi_123");
        when(stripe.isEnabled()).thenReturn(true);
        when(stripe.retrieveSession("cs_1")).thenReturn(session);
    }

    @Test
    public void confirmSomeoneElsesOrderNotFound() {
        when(orderDao.getOrder(7L)).thenReturn(Optional.of(order(99L, "Ordered")));

        ApiException error = assertThrows(ApiException.class,
            () -> checkoutService.confirm(1L, new ConfirmCheckoutRequest(null, 7L)));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    public void confirmUnpaidSessionDoesNotMarkPaid() throws Exception {
        when(orderDao.getOrderByStripeSession("cs_1")).thenReturn(Optional.of(order(1L, "Pending")));
        stripeSession("unpaid");

        ApiException error = assertThrows(ApiException.class,
            () -> checkoutService.confirm(1L, new ConfirmCheckoutRequest("cs_1", null)));

        assertEquals(HttpStatus.PAYMENT_REQUIRED, error.getStatus());
        verify(orderDao, never()).markPaid(anyLong(), any());
    }

    @Test
    public void confirmPaidSessionQueuesOrderForRobot() throws Exception {
        when(orderDao.getOrderByStripeSession("cs_1")).thenReturn(Optional.of(order(1L, "Pending")));
        when(orderDao.getOrder(7L)).thenReturn(Optional.of(order(1L, "Ordered")));
        when(orderDao.markPaid(7L, "pi_123")).thenReturn(1);
        stripeSession("paid");

        checkoutService.confirm(1L, new ConfirmCheckoutRequest("cs_1", null));

        verify(cartDao).setCartStatus(3L, "Ordered");
        verify(deliveryQueue).enqueue(7L, weight);
    }

    @Test
    public void cancelReleasesOwnPendingOrder() {
        when(orderDao.getOrderByStripeSession("cs_1")).thenReturn(Optional.of(order(1L, "Pending")));
        when(orderDao.getOrderStatus(7L)).thenReturn("Pending");

        checkoutService.cancel(1L, "cs_1");

        verify(orderDao).restock(7L);
        verify(orderDao).deleteOrder(7L);
    }

    @Test
    public void cancelIgnoresSomeoneElsesOrder() {
        when(orderDao.getOrderByStripeSession("cs_1")).thenReturn(Optional.of(order(99L, "Pending")));

        checkoutService.cancel(1L, "cs_1");

        verify(orderDao, never()).restock(anyLong());
        verify(orderDao, never()).deleteOrder(anyLong());
    }
}
