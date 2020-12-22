package com.atguigu.gmall.pms.entity.vo;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

@Data
public class SpuVo extends SpuEntity {
    /**
     * spu 图片描述信息
     */
    private List<String> spuImages;

    /**
     * spu 基本规格参数
     */
    private List<SpuAttrValueVo> baseAttrs;

    /**
     * sku 信息及营销信息
     */
    private List<SkuVo> skus;
}
