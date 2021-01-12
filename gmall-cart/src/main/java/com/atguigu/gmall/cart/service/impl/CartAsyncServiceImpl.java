package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.service.CartAsyncService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CartAsyncServiceImpl implements CartAsyncService {
    @Resource
    private CartMapper cartMapper;

    @Async
    @Override
    public void insertCart(String userid,Cart cart) {
        int i = 1/0;
        cartMapper.insert(cart);
    }

    @Async
    @Override
    public void updateByUserIdAndSkuId(String userId,Cart cart) {
        cartMapper.update(cart,new UpdateWrapper<Cart>().eq("user_id",cart.getUserId()).eq("sku_id",cart.getSkuId()));
    }

    @Override
    public void delete(String userKey) {
        cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id",userKey));
    }

    @Override
    public void deleteCartByUserIdAndSkuId(String userId, Long skuId) {
        cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id",userId).eq("sku_id",skuId));
    }
}
