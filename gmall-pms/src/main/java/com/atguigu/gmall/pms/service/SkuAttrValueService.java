package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author ll
 * @email ll@atguigu.com
 * @date 2020-12-14 19:34:30
 */
public interface SkuAttrValueService extends IService<SkuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SkuAttrValueEntity> querySearchAttrValueBySkuId(Long skuId);

    List<SaleAttrValueVo> querySkuAttrValuesBySpuId(Long spuId);

    List<SkuAttrValueEntity> querySkuAttrValueEntityBySkuId(Long skuId);

    String querySkusJson(Long spuId);
}

