package com.atguigu.gmall.item.service.impl;


import com.atguigu.gmall.item.feign.PmsFeignClient;
import com.atguigu.gmall.item.feign.SmsFeignClient;
import com.atguigu.gmall.item.feign.WmsFeignClient;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.entity.vo.ItemGroupVo;
import com.atguigu.gmall.pms.entity.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.entity.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private PmsFeignClient pmsFeignClient;
    @Autowired
    private WmsFeignClient wmsFeignClient;
    @Autowired
    private SmsFeignClient smsFeignClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;


    @Override
    public ItemVo getItemBySkuId(Long skuId) {
        ItemVo itemVo = new ItemVo();

//        1. 根据skuId查询sku（已有）
        CompletableFuture<SkuEntity> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuEntity skuEntity = pmsFeignClient.querySkuById(skuId).getData();
            if (skuEntity == null) {
                return null;
            }
            itemVo.setSkuId(skuId);
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubTitle(skuEntity.getSubtitle());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setWeight(skuEntity.getWeight());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());

            return skuEntity;
        }, threadPoolExecutor);

//        2. 根据sku中的三级分类id查询一二三级分类
        CompletableFuture<Void> catesCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {

            List<CategoryEntity> categoryEntities = pmsFeignClient.getCategoriesByLv3Id(skuEntity.getCatagoryId()).getData();
            if (!CollectionUtils.isEmpty(categoryEntities)) {
                itemVo.setCategoryEntities(categoryEntities);
            }
        },threadPoolExecutor);

//        3. 根据sku中的品牌id查询品牌（已有）
        CompletableFuture<Void> brandCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            BrandEntity brandEntity = pmsFeignClient.queryBrandById(skuEntity.getBrandId()).getData();
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        },threadPoolExecutor);

//        4. 根据sku中的spuId查询spu信息（已有）
        CompletableFuture<Void> spuCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            SpuEntity spuEntity = pmsFeignClient.querySpuById(skuEntity.getSpuId()).getData();
            Long spuId = null;
            if (spuEntity != null) {
                spuId = spuEntity.getId();
                itemVo.setSpuId(spuId);
                itemVo.setSpuName(spuEntity.getName());
            }
        },threadPoolExecutor);

//        5. 根据skuId查询sku所有图片
        CompletableFuture<Void> skuImagesCompletableFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> skuImagesEntities = pmsFeignClient.querySkuImagesBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(skuImagesEntities)) {
                itemVo.setImages(skuImagesEntities);
            }
        },threadPoolExecutor);

//        6. 根据skuId查询sku的所有营销信息
        CompletableFuture<Void> salesCompletableFuture = CompletableFuture.runAsync(() -> {
            List<ItemSaleVo> itemSaleVos = smsFeignClient.querySalesBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(itemSaleVos)) {
                itemVo.setSales(itemSaleVos);
            }
        },threadPoolExecutor);

//        7. 根据skuId查询sku的库存信息（已有）
        CompletableFuture<Void> storeCompletableFuture = CompletableFuture.runAsync(() -> {
            List<WareSkuEntity> wareSkuEntities = wmsFeignClient.queryWareBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                boolean anyMatch = wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0);
                itemVo.setStore(anyMatch);
            }
        },threadPoolExecutor);

//        8. 根据sku中的spuId查询spu下的所有销售属性
        CompletableFuture<Void> saleAttrsCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            List<SaleAttrValueVo> saleAttrValueVos = pmsFeignClient.querySkuAttrValuesBySpuId(skuEntity.getSpuId()).getData();
            if (!CollectionUtils.isEmpty(saleAttrValueVos)) {
                itemVo.setSaleAttrs(saleAttrValueVos);
            }
        },threadPoolExecutor);

//        9. 根据skuId查询当前sku的销售属性
        CompletableFuture<Void> saleAttrCompletableFuture = CompletableFuture.runAsync(() -> {
            List<SkuAttrValueEntity> skuAttrValues = pmsFeignClient.querySkuAttrValueEntityBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(skuAttrValues)) {
                Map<Long, String> skuAttrValueMap = skuAttrValues.stream().collect(Collectors.toMap(skuAttrValueEntity -> skuAttrValueEntity.getAttrId(), skuAttrValueEntity -> skuAttrValueEntity.getAttrValue()));
                itemVo.setSaleAttr(skuAttrValueMap);
            }
        },threadPoolExecutor);

//        10. 根据sku中的spuId查询spu下所有sku：销售属性组合与skuId映射关系
        CompletableFuture<Void> skusJsonCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            String skusJson = pmsFeignClient.querySkusJson(skuEntity.getSpuId()).getData();
            if (!StringUtils.isEmpty(skusJson)) {
                itemVo.setSkusJson(skusJson);
            }
        },threadPoolExecutor);

//        11. 根据sku中spuId查询spu的描述信息（已有）
        CompletableFuture<Void> spuDescCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            SpuDescEntity spuDescEntity = pmsFeignClient.querySpuDescById(skuEntity.getSpuId()).getData();
            if (spuDescEntity != null) {
                String[] spuImages = spuDescEntity.getDecript().split(",");
                if (spuImages.length > 0) {
                    itemVo.setSpuImages(Arrays.asList(spuImages));
                }
            }
        },threadPoolExecutor);

//        12. 根据分类id、spuId及skuId查询分组及组下的规格参数值
        CompletableFuture<Void> groupsCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            List<ItemGroupVo> itemGroupVos = pmsFeignClient.queryItemGroupVosByCategoryIdAndSpuIdAndSkuId(skuEntity.getCatagoryId(), skuEntity.getSpuId(), skuId).getData();
            if (!CollectionUtils.isEmpty(itemGroupVos)) {
                itemVo.setGroups(itemGroupVos);
            }
        },threadPoolExecutor);

        //等待所有任务完成
        CompletableFuture.allOf(skuCompletableFuture,catesCompletableFuture,brandCompletableFuture,spuCompletableFuture,
                skuImagesCompletableFuture,salesCompletableFuture,storeCompletableFuture,saleAttrsCompletableFuture,saleAttrCompletableFuture,
                skusJsonCompletableFuture,spuDescCompletableFuture,groupsCompletableFuture).join();
        
        return itemVo;
    }
}
