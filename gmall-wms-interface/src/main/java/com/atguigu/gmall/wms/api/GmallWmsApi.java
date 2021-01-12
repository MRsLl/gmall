package com.atguigu.gmall.wms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallWmsApi {

    /**
     * 根据 skuId 查询sku 库存
     * @param skuId
     * @return
     */
    @GetMapping("wms/waresku/sku/{skuId}")
    ResponseVo<List<WareSkuEntity>> queryWareBySkuId(@PathVariable("skuId") Long skuId);

    /**
     * 根据订单中锁定的商品信息检验和锁定数据库中的商品
     * @param skuLockVos
     * @return
     */
    @PostMapping("wms/waresku/check/lock")
    ResponseVo<List<SkuLockVo>> checkAndLockWare(@RequestBody List<SkuLockVo> skuLockVos);
}
