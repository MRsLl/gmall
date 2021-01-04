package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "pms-service")
public interface PmsFeignClient extends GmallPmsApi {
}
