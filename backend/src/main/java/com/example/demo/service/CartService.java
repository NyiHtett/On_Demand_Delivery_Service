package com.example.demo.service;

import com.example.demo.dao.CartDao;
import com.example.demo.dto.CartResponse;

import java.util.Optional;

// returns Cart, pulling from CartDao
public class CartService {
    private final CartDao cartDao;

    public CartService(CartDao cartDao) {
        this.cartDao = cartDao;
    }

    public CartResponse getCurrentCart(Long id) {
        // id -> cart id
        // cart id -> retrieve all entries containing cartID
        // build entries into cartResponse

        Optional<Long> cartId = cartDao.getUserCart(id);


        return null;
    }
}
