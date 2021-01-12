package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.sms.entity.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {

    private Long skuId;

    private String defaultImage;
    private String title;
    private BigDecimal weight;
    private BigDecimal price; // 价格

    private List<SkuAttrValueEntity> saleAttrs; // 销售属性
    private List<ItemSaleVo> sales; // 营销信息

    private BigDecimal count;

    private Boolean store = false; // 是否有货

}