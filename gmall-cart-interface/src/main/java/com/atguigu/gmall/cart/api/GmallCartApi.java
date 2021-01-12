package com.atguigu.gmall.cart.api;

import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

public interface GmallCartApi {

    /**
     * 根据userId 查询用户选中的订单
     * @param userId
     * @return
     */
    @ResponseBody
    @GetMapping("check/{userId}")
    ResponseVo<List<Cart>> queryCheckedCartsByUserId(@PathVariable(value = "userId")Long userId);
}
