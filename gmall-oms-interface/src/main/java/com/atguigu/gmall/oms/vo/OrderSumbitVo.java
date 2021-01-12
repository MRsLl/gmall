package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSumbitVo {

    private String orderToken;

    private UserAddressEntity address;

    private String deliveryCompany;

    //用户的购物积分
    private Integer bounds;

    //送货清单，订单中展示的商品信息集合
    private List<OrderItemVo> items;

    private Integer payType;

    private BigDecimal totalPrice;
}
