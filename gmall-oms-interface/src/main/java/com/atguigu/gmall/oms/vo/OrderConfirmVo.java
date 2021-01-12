package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderConfirmVo {

    //用户收货地址集合
    private List<UserAddressEntity> addresses;

    //送货清单，订单中展示的商品信息集合
    private List<OrderItemVo> items;

    //用户的购物积分
    private Integer bounds;

    //防止重复下单，保证下单的幂等性
    private String orderToken;

}
