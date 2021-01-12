package com.atguigu.gmall.oms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderConfirmVo;
import com.atguigu.gmall.oms.vo.OrderSumbitVo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface GmallOmsApi {

    /**
     * 保存订单和订单详情
     * @param orderSumbitVo
     * @param userId
     * @return
     */
    @PostMapping("oms/order/{userId}")
    ResponseVo<OrderEntity> saveOrder(@RequestBody OrderSumbitVo orderSumbitVo, @PathVariable(value = "userId") Long userId);
}
