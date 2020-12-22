package com.atguigu.gmall.search.entity;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

@Data
public class SearchResponseVo {

    //品牌集合
    private List<BrandEntity> brands;
    //分类集合
    private List<CategoryEntity> categories;
    //搜索参数集合
    private List<SearchResponseAttrVo> filters;

    //分页参数
    private Integer pageNum;
    private Integer pageSize;
    private Long total;

    //页面展示商品集合
    private List<Goods> goodsList;
}
