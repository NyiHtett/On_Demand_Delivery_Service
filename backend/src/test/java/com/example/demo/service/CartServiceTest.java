package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dao.CartDao;
import com.example.demo.dao.ProductDao;
import com.example.demo.dto.CartItemResponse;
import com.example.demo.dto.CartResponse;
import com.example.demo.dto.ProductResponse;
import com.example.demo.exception.ApiException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class CartServiceTest {
    CartDao cartDao = mock(CartDao.class);
    ProductDao productDao = mock(ProductDao.class);
    CartService cartService = new CartService(cartDao, productDao);

    private ProductResponse milk(int stock) {
        return new ProductResponse(7L, "Milk", "1 gallon", new BigDecimal("8.601"), new BigDecimal("6.13"), null, stock);
    }

    @Test
    public void deliveryIsFreeUnder20PoundsAnd10DollarsFrom20() {
        assertEquals(0, BigDecimal.ZERO.compareTo(CartService.deliveryFeeFor(new BigDecimal("19.999"))));
        assertEquals(0, BigDecimal.TEN.compareTo(CartService.deliveryFeeFor(new BigDecimal("20"))));
    }

    @Test
    public void cartTotalsIncludeDeliveryFeeAt20Pounds() {
        when(cartDao.containsActiveCart(1L)).thenReturn(true);
        when(cartDao.getUserCartID(1L)).thenReturn(5L);
        when(cartDao.getCartItems(5L)).thenReturn(List.of(
            new CartItemResponse(1L, 7L, "Milk", new BigDecimal("6.13"), new BigDecimal("8.601"), 3, null, 40)));

        CartResponse cart = cartService.getCurrentCart(1L);

        assertEquals(new BigDecimal("18.39"), cart.subtotal());
        assertEquals(new BigDecimal("25.803"), cart.totalWeight());
        assertEquals(new BigDecimal("10.00"), cart.deliveryFee());
        assertEquals(new BigDecimal("28.39"), cart.total());
    }

    @Test
    public void addItemCreatesCartAndAddsToExistingQuantity() {
        when(cartDao.containsActiveCart(1L)).thenReturn(false);
        when(cartDao.createCart(1L)).thenReturn(9L);
        when(cartDao.getItemQuantity(9L, 7L)).thenReturn(2);
        when(productDao.getProduct(7L)).thenReturn(Optional.of(milk(40)));

        cartService.addItem(1L, 7L, 3);

        verify(cartDao).upsertItem(9L, 7L, 5);
    }

    @Test
    public void addItemRejectsMoreThanInStock() {
        when(cartDao.containsActiveCart(1L)).thenReturn(true);
        when(cartDao.getUserCartID(1L)).thenReturn(5L);
        when(cartDao.getItemQuantity(5L, 7L)).thenReturn(38);
        when(productDao.getProduct(7L)).thenReturn(Optional.of(milk(40)));

        ApiException error = assertThrows(ApiException.class, () -> cartService.addItem(1L, 7L, 3));

        assertEquals("Only 40 Milk in stock.", error.getMessage());
        verify(cartDao, never()).upsertItem(anyLong(), anyLong(), anyInt());
    }

    @Test
    public void addItemRejectsZeroQuantity() {
        assertThrows(ApiException.class, () -> cartService.addItem(1L, 7L, 0));
    }

    @Test
    public void updateToZeroRemovesItem() {
        when(cartDao.containsActiveCart(1L)).thenReturn(true);
        when(cartDao.getUserCartID(1L)).thenReturn(5L);

        cartService.updateItem(1L, 7L, 0);

        verify(cartDao).deleteItem(5L, 7L);
    }
}
