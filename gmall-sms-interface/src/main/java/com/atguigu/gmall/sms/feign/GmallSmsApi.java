package com.atguigu.gmall.sms.feign;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.entity.dto.SkuSaleDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface GmallSmsApi {

    @PostMapping("/sms/skubounds/skusale/save")
    ResponseVo saveSkuSaleInfo(@RequestBody SkuSaleDto skuSaleDto);
}
