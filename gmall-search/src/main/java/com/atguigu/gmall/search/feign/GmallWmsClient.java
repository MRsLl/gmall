package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.pms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
