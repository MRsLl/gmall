package com.atguigu.gmall.wms.mapper;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author ll
 * @email ll@atguigu.com
 * @date 2020-12-14 21:12:01
 */
@Mapper
public interface WareSkuMapper extends BaseMapper<WareSkuEntity> {

    List<WareSkuEntity> checkSkuStock(@Param(value = "skuId") Long skuId,@Param(value = "count") Integer count);

    Integer tryLock(@Param(value = "wareSkuId") Long wareSkuId, @Param(value = "count") Integer count);

    Integer tryUnLock(@Param(value = "wareSkuId") Long wareSkuId, @Param(value = "count") Integer count);
}
