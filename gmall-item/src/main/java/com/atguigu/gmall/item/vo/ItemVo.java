package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.vo.ItemGroupVo;
import com.atguigu.gmall.pms.entity.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.entity.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ItemVo {

    //三级分类
    private List<CategoryEntity> categoryEntities;

    //品牌
    private Long brandId;
    private String brandName;

    //spu
    private Long spuId;
    private String spuName;

    //sku 相关信息
    private Long skuId;
    private String title;
    private String subTitle;
    private BigDecimal price;
    private Integer weight;
    private String defaultImage;

    //sku 图片
    private List<SkuImagesEntity> images;

    //是否有库存
    private Boolean store;

    //sku 营销信息
    private List<ItemSaleVo> sales;

    // sku所属spu下的所有sku的销售属性
    // [{attrId: 3, attrName: '颜色', attrValues: '白色','黑色','粉色'},
    // {attrId: 8, attrName: '内存', attrValues: '6G','8G','12G'},
    // {attrId: 9, attrName: '存储', attrValues: '128G','256G','512G'}]
    private List<SaleAttrValueVo> saleAttrs;

    // 当前sku的销售属性：{3:'白色',8:'8G',9:'128G'}
    private Map<Long,String> saleAttr;

    // sku列表：{'白色,8G,128G': 4, '白色,8G,256G': 5, '白色,8G,512G': 6, '白色,12G,128G': 7}
    private String skusJson;

    // spu的海报信息
    private List<String> spuImages;

    // 规格参数组及组下的规格参数(带值)
    private List<ItemGroupVo> groups;

}
