package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.entity.Cart;

public interface CartAsyncService {
    void insertCart(String userid,Cart cart);

    void updateByUserIdAndSkuId(String userId,Cart cart);

    void delete(String userKey);

    void deleteCartByUserIdAndSkuId(String userId, Long skuId);
}
