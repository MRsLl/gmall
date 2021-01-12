package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "cart-service")
public interface CartFeignClient extends GmallCartApi {
}
