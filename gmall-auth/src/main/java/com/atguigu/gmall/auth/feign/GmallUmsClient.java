package com.atguigu.gmall.auth.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}
