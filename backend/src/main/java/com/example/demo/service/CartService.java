package com.example.demo.service;

import com.example.demo.dao.CartDao;
import com.example.demo.dao.ProductDao;
import com.example.demo.dto.CartItemResponse;
import com.example.demo.dto.CartResponse;
import com.example.demo.dto.ProductResponse;
import com.example.demo.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

// returns Cart, pulling from CartDao
@Service
public class CartService {
    // Requirements 3b/3c: free delivery under 20 lb, $10 at 20 lb or more.
    public static final BigDecimal FREE_DELIVERY_LIMIT_LBS = BigDecimal.valueOf(20);
    public static final BigDecimal DELIVERY_FEE = BigDecimal.valueOf(10);

    private final CartDao cartDao;
    private final ProductDao productDao;

    public CartService(CartDao cartDao, ProductDao productDao) {
        this.cartDao = cartDao;
        this.productDao = productDao;
    }

    public static BigDecimal deliveryFeeFor(BigDecimal totalWeight) {
        return totalWeight.compareTo(FREE_DELIVERY_LIMIT_LBS) >= 0 ? DELIVERY_FEE : BigDecimal.ZERO;
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
        // deliveryFee, if 20 lbs or more, add fee (+10)
        // Total, Subtotal+deliveryFee

        BigDecimal subTotal = new BigDecimal(0);
        BigDecimal unitWeight = new BigDecimal(0);

        for (CartItemResponse item : items) {
            subTotal = subTotal.add(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
            unitWeight = unitWeight.add(item.unitWeight().multiply(BigDecimal.valueOf(item.quantity())));
        }

        BigDecimal deliveryFee = deliveryFeeFor(unitWeight);
        BigDecimal totalPrice = subTotal.add(deliveryFee);

        return new CartResponse(
                cartId,
                List.copyOf(items),
                totalPrice.setScale(2, RoundingMode.HALF_UP),
                subTotal.setScale(2, RoundingMode.HALF_UP),
                deliveryFee.setScale(2, RoundingMode.HALF_UP),
                unitWeight.setScale(3, RoundingMode.HALF_UP)
        );
    }

    /** Adds quantity of a product on top of what is already in the cart. */
    @Transactional
    public CartResponse addItem(long userId, Long productId, Integer quantity) {
        if (productId == null || quantity == null || quantity <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "productId and a quantity of at least 1 are required.");
        }
        long cartId = activeCartId(userId);
        int newQuantity = cartDao.getItemQuantity(cartId, productId) + quantity;
        checkStock(productId, newQuantity);
        cartDao.upsertItem(cartId, productId, newQuantity);
        return getCurrentCart(userId);
    }

    /** Sets the quantity of a product in the cart; 0 removes it. */
    @Transactional
    public CartResponse updateItem(long userId, long productId, Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "quantity must be 0 or more.");
        }
        if (quantity == 0) {
            return removeItem(userId, productId);
        }
        long cartId = activeCartId(userId);
        if (cartDao.getItemQuantity(cartId, productId) == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "That product is not in your cart.");
        }
        checkStock(productId, quantity);
        cartDao.upsertItem(cartId, productId, quantity);
        return getCurrentCart(userId);
    }

    @Transactional
    public CartResponse removeItem(long userId, long productId) {
        if (cartDao.containsActiveCart(userId)) {
            cartDao.deleteItem(cartDao.getUserCartID(userId), productId);
        }
        return getCurrentCart(userId);
    }

    private long activeCartId(long userId) {
        return cartDao.containsActiveCart(userId) ? cartDao.getUserCartID(userId) : cartDao.createCart(userId);
    }

    // Requirement 2d: customers can't put more in the cart than is in stock.
    private void checkStock(long productId, int wanted) {
        ProductResponse product = productDao.getProduct(productId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found."));
        if (wanted > product.quantity()) {
            throw new ApiException(HttpStatus.CONFLICT,
                "Only " + product.quantity() + " " + product.name() + " in stock.");
        }
    }
}
