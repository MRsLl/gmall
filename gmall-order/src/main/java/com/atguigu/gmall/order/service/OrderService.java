package com.atguigu.gmall.order.service;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderConfirmVo;
import com.atguigu.gmall.oms.vo.OrderSumbitVo;

public interface OrderService {
    OrderConfirmVo confirm();

    OrderEntity submitOrder(OrderSumbitVo orderSumbitVo);
}
