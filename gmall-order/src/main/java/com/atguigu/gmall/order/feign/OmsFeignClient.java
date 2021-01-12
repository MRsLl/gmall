package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "oms-service")
public interface OmsFeignClient extends GmallOmsApi {
}
