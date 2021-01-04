package com.atguigu.gmall.search.entity;

import lombok.Data;

import java.util.List;

/**
 * 接受页面传递过来的检索参数
 * search?keyword=小米&brandId=1,3&cid=225&props=5:高通-麒麟&props=6:骁龙865-硅谷1000&sort=1&priceFrom=1000&priceTo=6000&pageNum=1&store=true
 */
@Data
public class SearchParamVo {
    //搜索框查询条件
    private String keyword;
    //品牌id 集合
    private List<Long> brandId;
    //需要过滤的检索规格参数
    private List<String> props;
    //分类id
    private Long cid;
    //排序字段：0-默认，得分降序；1-按价格升序；2-按价格降序；3-按创建时间降序；4-按销量降序
    private Integer sort = 0;
    //价格区间起始值
    private Double priceFrom;
    //价格区间终止值
    private Double priceTo;
    //页码
    private Integer pageNum = 1;
    //每页最大商品条数
    private final Integer pageSize = 20;
    //库存
    private Boolean store = false;
}
