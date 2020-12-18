package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.sms.feign.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "sms-service")
public interface SmsFeignClient extends GmallSmsApi {
}
