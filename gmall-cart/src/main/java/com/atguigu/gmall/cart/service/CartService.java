package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.entity.Cart;

import java.util.List;

public interface CartService {
    void addToCart(Cart cart) ;

    Cart toCart(Long skuId);

    List<Cart> queryCartByUserId();

    void updateCartBySkuId(Cart cart);

    void deleteCartBySkuId(Long skuId);

    List<Cart> queryCheckedCartsByUserId(Long userId);
}
