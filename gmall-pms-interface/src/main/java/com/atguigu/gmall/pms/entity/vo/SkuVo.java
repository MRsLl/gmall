package com.atguigu.gmall.pms.entity.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import io.swagger.models.auth.In;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuVo extends SkuEntity {

    /**
     *sku 销售属性
     */
    private List<SkuAttrValueEntity> saleAttrs;

    /**
     * sku 图片信息
     */
    private List<String> images;
    /**
     * 成长积分
     */
    private BigDecimal growBounds;
    /**
     * 购物积分
     */
    private BigDecimal buyBounds;

    /**
     * 优惠生效情况
     */
    private List<Integer> work;

    /**
     * 满多少
     */
    private BigDecimal fullPrice;
    /**
     * 减多少
     */
    private BigDecimal reducePrice;

    /**
     * 满减是否叠加其他优惠[0-不可叠加，1-可叠加]
     */
    private Integer fullAddOther;


    /**
     * 满几件
     */
    private Integer fullCount;
    /**
     * 打几折
     */
    private BigDecimal discount;

    /**
     * 打折是否叠加其他优惠[0-不可叠加，1-可叠加]
     */
    private Integer ladderAddOther;

}
