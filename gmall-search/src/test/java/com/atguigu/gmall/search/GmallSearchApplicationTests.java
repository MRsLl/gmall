package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.entity.Goods;
import com.atguigu.gmall.search.entity.SearchAttrValue;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchRestTemplate restTemplate;
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;

    @Test
    void contextLoads() {
    }

    @Test
    void uploadGoods() {
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);

        Integer pageNum = 1;
        Integer pageSize = 100;

        do {
            PageParamVo pageParamVo = new PageParamVo();
            pageParamVo.setPageNum(pageNum);
            pageParamVo.setPageSize(pageSize);
            //1.分页查询spu 信息
            List<SpuEntity> spus = gmallPmsClient.querySpusByPage(pageParamVo).getData();

            //2.遍历spu 集合，根据spuId 查询sku 集合
            spus.forEach(spuEntity -> {
                Long spuId = spuEntity.getId();
                List<SkuEntity> skus = gmallPmsClient.querySkuBySpuId(spuId).getData();

                if (!CollectionUtils.isEmpty(skus)) {
                    //3.遍历sku 集合，将sku 集合转为 goods 集合
                    List<Goods> goodsList = skus.stream().map(skuEntity -> {
                        Long skuId = skuEntity.getId();
                        Goods goods = new Goods();

                        //为goods 设置sku 相关信息
                        goods.setSkuId(skuId);
                        goods.setPrice(skuEntity.getPrice().doubleValue());
                        goods.setCreateTime(spuEntity.getCreateTime());
                        goods.setTitle(skuEntity.getTitle());
                        goods.setSubTitle(skuEntity.getSubtitle());
                        goods.setDefaultImage(skuEntity.getDefaultImage());

                        //为goods 设置品牌相关信息
                        BrandEntity brandEntity = gmallPmsClient.queryBrandById(skuEntity.getBrandId()).getData();
                        goods.setBrandId(brandEntity.getId());
                        goods.setBrandName(brandEntity.getName());
                        goods.setLogo(brandEntity.getLogo());
                        goods.setSales(0l);

                        //为goods 设置分类相关信息
                        CategoryEntity categoryEntity = gmallPmsClient.queryCategoryById(skuEntity.getCatagoryId()).getData();
                        goods.setCategoryId(categoryEntity.getId());
                        goods.setCategoryName(categoryEntity.getName());

                        //为goods 设置库存信息
                        List<WareSkuEntity> wareSkuEntities = gmallWmsClient.queryWareBySkuId(skuId).getData();
                        if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                            boolean flag = wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0);

                            goods.setStore(flag);
                        }

                        //为goods 设置spu 搜索参数信息
                        ArrayList<SearchAttrValue> searchAttrValues = new ArrayList<>();
                        List<SpuAttrValueEntity> spuAttrValueEntities = gmallPmsClient.querySearchAttrValueBySpuId(spuId).getData();

                        if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                            searchAttrValues = (ArrayList<SearchAttrValue>) spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                SearchAttrValue searchAttrValue = new SearchAttrValue();
                                BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValue);

                                return searchAttrValue;
                            }).collect(Collectors.toList());
                        }

                        //为goods 设置sku 搜索参数信息
                        List<SkuAttrValueEntity> skuAttrValueEntities = gmallPmsClient.querySearchAttrValueBySkuId(skuId).getData();

                        if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {

                            List<SearchAttrValue> searchSkuAttrValues = skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                SearchAttrValue searchAttrValue = new SearchAttrValue();
                                BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValue);

                                return searchAttrValue;
                            }).collect(Collectors.toList());

                            searchAttrValues.addAll(searchSkuAttrValues);
                        }

                        goods.setSearchAttrs(searchAttrValues);

                        return goods;
                    }).collect(Collectors.toList());

                    goodsRepository.saveAll(goodsList);
                }
            });

            pageSize = spus.size();
            pageNum++;

        } while (pageSize == 100);
    }
    
    @Test
    public void test() {
        List<SkuAttrValueEntity> skuAttrValueEntities = gmallPmsClient.querySearchAttrValueBySkuId(1l).getData();

        if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {

            List<SearchAttrValue> searchSkuAttrValues = skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                SearchAttrValue searchAttrValue = new SearchAttrValue();
                BeanUtils.copyProperties(searchAttrValue, skuAttrValueEntity);

                return searchAttrValue;
            }).collect(Collectors.toList());

            System.out.println("searchSkuAttrValues = " + searchSkuAttrValues);
        }
    }

}
