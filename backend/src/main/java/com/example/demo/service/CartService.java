package com.example.demo.service;

import com.example.demo.dao.CartDao;
import com.example.demo.dto.CartItemResponse;
import com.example.demo.dto.CartResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

// returns Cart, pulling from CartDao
@Service
public class CartService {
    private final CartDao cartDao;

    public CartService(CartDao cartDao) {
        this.cartDao = cartDao;
    }

    public CartResponse getCurrentCart(Long id) {
        // id -> cart id
        // cart id -> retrieve all entries containing cartID
        // build entries into cartResponse

        if (!cartDao.containsActiveCart(id)) {
            return new CartResponse(
                    -1L,
                    List.copyOf(List.of()),
                    new BigDecimal(0),
                    new BigDecimal(0),
                    new BigDecimal(0),
                    new BigDecimal(0)
            );
        }

        Long cartId = cartDao.getUserCartID(id);
        List<CartItemResponse> items = cartDao.getCartItems(cartId);

        // Subtotal, sum of cart item prices
        // totalWeight, sum of cart item weight
        // deliveryFee, if greater than 20 lbs, add fee (+10)
        // Total, Subtotal+deliveryFee

        BigDecimal subTotal = new BigDecimal(0);
        BigDecimal unitWeight = new BigDecimal(0);
        BigDecimal DeliveryFee = new BigDecimal(0);
        BigDecimal totalPrice;


        // Put the cart items into a CartResponse.

        for (CartItemResponse item : items) {
            subTotal = subTotal.add(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
            unitWeight = unitWeight.add(item.unitWeight().multiply(BigDecimal.valueOf(item.quantity())));
        }

        totalPrice = subTotal;

        if (unitWeight.compareTo(BigDecimal.valueOf(20)) >= 0) { // hardcoded 20 lbs for delivery fee
            totalPrice = totalPrice.add(BigDecimal.valueOf(10)); // Hardcoded $10 delivery fee
            DeliveryFee = DeliveryFee.add(BigDecimal.valueOf(10));
        }

        subTotal = subTotal.setScale(2, RoundingMode.HALF_UP);
        unitWeight = unitWeight.setScale(3, RoundingMode.HALF_UP);
        DeliveryFee = DeliveryFee.setScale(2, RoundingMode.HALF_UP);
        totalPrice = totalPrice.setScale(2, RoundingMode.HALF_UP);

        return new CartResponse(
                cartId,
                List.copyOf(items),
                totalPrice,
                subTotal,
                DeliveryFee,
                unitWeight
        );
    }
}
