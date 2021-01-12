package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "wms-service")
public interface WmsFeignClient extends GmallWmsApi {
}
