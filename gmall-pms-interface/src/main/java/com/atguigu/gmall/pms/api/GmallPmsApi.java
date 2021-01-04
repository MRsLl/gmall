package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.entity.vo.ItemGroupVo;
import com.atguigu.gmall.pms.entity.vo.SaleAttrValueVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {

    /**
     * 根据 id 查询spu
     *
     * @param id
     * @return
     */
    @GetMapping("pms/spu/{id}")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    /**
     * 分页查询spu
     *
     * @param paramVo
     * @return
     */
    @PostMapping("pms/spu/page")
    ResponseVo<List<SpuEntity>> querySpusByPage(@RequestBody PageParamVo paramVo);

    /**
     * 根据spuId 查询spu 描述信息
     *
     * @param spuId
     * @return
     */
    @GetMapping("pms/spudesc/{spuId}")
    ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);

    /**
     * 根据 spuId 查询sku
     *
     * @param spuId
     * @return
     */
    @GetMapping("pms/sku/spu/{spuId}")
    ResponseVo<List<SkuEntity>> querySkuBySpuId(@PathVariable("spuId") Long spuId);

    /**
     * 根据 skuId 查询sku
     *
     * @param id
     * @return
     */
    @GetMapping("pms/sku/{id}")
    ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    /**
     * 根据 分类id 查询商品分类
     *
     * @param id
     * @return
     */
    @GetMapping("pms/category/{id}")
    ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    /**
     * 根据父分类id 查询子分类
     *
     * @param parentId
     * @return
     */
    @GetMapping("pms/category/parent/{parentId}")
    ResponseVo<List<CategoryEntity>> queryCategoryByParentId(@PathVariable(value = "parentId") Long parentId);

    /**
     * 根据一级分类id 获取其所有二级，三级分类
     *
     * @param parentId
     * @return
     */
    @GetMapping("pms/category/subs/{pid}")
    ResponseVo<List<CategoryEntity>> getCategoriesByLv1Id(@PathVariable(value = "pid") Long parentId);

    /**
     * 根据三级分类id 获取一二三级分类对象
     *
     * @param cid
     * @return
     */
    @GetMapping("pms/category/all/{cid}")
    ResponseVo<List<CategoryEntity>> getCategoriesByLv3Id(@PathVariable(value = "cid") Long cid);

    /**
     * 根据品牌id 查询商品品牌
     *
     * @param id
     * @return
     */
    @GetMapping("pms/brand/{id}")
    ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    /**
     * "根据 spuId 查询'检索'规格参数及值"
     *
     * @param spuId
     * @return
     */
    @GetMapping("pms/spuattrvalue/spu/{spuId}")
    ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValueBySpuId(@PathVariable("spuId") Long spuId);

    /**
     * "根据 skuId 查询'检索'规格参数及值"
     *
     * @param skuId
     * @return
     */
    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValueBySkuId(@PathVariable("skuId") Long skuId);

    /**
     * 根据spuId 查询其所有sku的销售属性
     *
     * @param spuId
     * @return
     */
    @GetMapping("pms/skuattrvalue/spu/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySkuAttrValuesBySpuId(@PathVariable("spuId") Long spuId);

    /**
     * 根据skuId 查询其销售属性
     *
     * @param skuId
     * @return
     */
    @GetMapping("pms/skuattrvalue/sku/sale/{skuId}")
    ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueEntityBySkuId(@PathVariable(value = "skuId") Long skuId);

    /**
     * 根据spuId 查询其sku销售属性和skuId 的映射关系
     * @param spuId
     * @return
     */
    @GetMapping("pms/skuattrvalue/spu/sku/{spuId}")
    ResponseVo<String> querySkusJson(@PathVariable(value = "spuId") Long spuId);

    /**
     * 根据skuId 查询sku 图片集合
     *
     * @param skuId
     * @return
     */
    @GetMapping("pms/skuimages/sku/{skuId}")
    ResponseVo<List<SkuImagesEntity>> querySkuImagesBySkuId(@PathVariable(value = "skuId") Long skuId);

    /**
     * 根据分类id 查询规格参数分组及其组下所有规格参数(包含值)
     *
     * @param cid
     * @param spuId
     * @param skuId
     * @return
     */
    @GetMapping("pms/attrgroup/withattrs/category/{cid}")
    ResponseVo<List<ItemGroupVo>> queryItemGroupVosByCategoryIdAndSpuIdAndSkuId(
            @PathVariable(value = "cid") Long cid,
            @RequestParam Long spuId,
            @RequestParam Long skuId
    );
}
