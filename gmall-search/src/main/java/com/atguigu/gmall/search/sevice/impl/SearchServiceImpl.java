package com.atguigu.gmall.search.sevice.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.search.sevice.SearchService;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResponseVo search(SearchParamVo searchParamVo) {
        SearchResponseVo responseVo = new SearchResponseVo();

        try {
            SearchRequest request = new SearchRequest(new String[]{"goods"}, buildDsl(searchParamVo));
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

            responseVo = parseResult(response);

            //为返回值设置页码和每页最大值
            if (searchParamVo.getPageNum() <= 0) {
                responseVo.setPageNum(1);
            } else {
              responseVo.setPageNum(searchParamVo.getPageNum());
            }

            responseVo.setPageSize(searchParamVo.getPageSize());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseVo;
    }

    /**
     * 解析返回结果集
     *
     * @param response
     * @return
     */
    private SearchResponseVo parseResult(SearchResponse response) {
        SearchResponseVo responseVo = new SearchResponseVo();

        SearchHits hits = response.getHits();
        //1.设置总查询结果数
        responseVo.setTotal(hits.getTotalHits());

        //2.解析查询结果集，设置查询到的商品集合
        SearchHit[] hitsHits = hits.getHits();

        if (hitsHits.length != 0 && hitsHits != null) {
            List<Goods> goodsList = Arrays.stream(hitsHits).map(hitsHit -> {
                //将Json 格式的查询结果_source 转化为商品对象
                String goodJson = hitsHit.getSourceAsString();
                Goods goods = JSON.parseObject(goodJson, Goods.class);

                //用高亮title 替代普通title
                Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
                goods.setTitle(highlightFields.get("title").getFragments()[0].toString());

                return goods;
            }).collect(Collectors.toList());
            //将获取到的商品集合设置给返回的Vo 对象
            responseVo.setGoodsList(goodsList);
        }

        Aggregations aggregations = response.getAggregations();
        Map<String, Aggregation> aggregationsAsMap = aggregations.getAsMap();

        //3.解析聚合结果集，设置品牌信息
        ParsedLongTerms brandIdAggs = (ParsedLongTerms) aggregationsAsMap.get("brandIdAggs");
        List<? extends Terms.Bucket> brandIdAggsBuckets = brandIdAggs.getBuckets();

        if (!CollectionUtils.isEmpty(brandIdAggsBuckets)) {
            List<BrandEntity> brands = brandIdAggsBuckets.stream().map(bucket -> {
                BrandEntity brandEntity = new BrandEntity();
                //设置品牌id
                brandEntity.setId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());

                Map<String, Aggregation> brandAggregationMap = ((Terms.Bucket) bucket).getAggregations().getAsMap();

                //设置品牌名
                ParsedStringTerms brandNameAggs = (ParsedStringTerms) brandAggregationMap.get("brandNameAggs");
                String brandName = (String) brandNameAggs.getBuckets().get(0).getKey();
                brandEntity.setName(brandName);

                //设置品牌Logo
                ParsedStringTerms brandLogoAggs = (ParsedStringTerms) brandAggregationMap.get("brandLogoAggs");
                String brandLogo = (String) brandLogoAggs.getBuckets().get(0).getKey();
                brandEntity.setLogo(brandLogo);

                return brandEntity;
            }).collect(Collectors.toList());
            responseVo.setBrands(brands);
        }

        //4.解析聚合结果集，设置分类信息
        ParsedLongTerms categoryIdAggs = (ParsedLongTerms) aggregationsAsMap.get("categoryIdAggs");
        List<? extends Terms.Bucket> categoryIdAggsBuckets = categoryIdAggs.getBuckets();

        if (!CollectionUtils.isEmpty(categoryIdAggsBuckets)) {
            List<CategoryEntity> categoryEntities = categoryIdAggsBuckets.stream().map(bucket -> {
                CategoryEntity categoryEntity = new CategoryEntity();
                //设置分类id
                categoryEntity.setId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());

                Map<String, Aggregation> aggregationMap = ((Terms.Bucket) bucket).getAggregations().getAsMap();

                //设置分类名
                ParsedStringTerms categoryNameAggs = (ParsedStringTerms) aggregationMap.get("categoryNameAggs");
                String categoryName = (String) categoryNameAggs.getBuckets().get(0).getKey();
                categoryEntity.setName(categoryName);

                return categoryEntity;
            }).collect(Collectors.toList());

            responseVo.setCategories(categoryEntities);
        }

        //5.解析聚合结果集，设置搜索参数信息
        ParsedNested searchAttrsAggs = (ParsedNested) aggregationsAsMap.get("searchAttrsAggs");
        ParsedLongTerms searchAttrsIdAggs = searchAttrsAggs.getAggregations().get("searchAttrsIdAggs");
        List<? extends Terms.Bucket> searchAttrsIdAggsBuckets = searchAttrsIdAggs.getBuckets();

        if (!CollectionUtils.isEmpty(searchAttrsIdAggsBuckets)) {
            List<SearchResponseAttrVo> filters = searchAttrsIdAggsBuckets.stream().map(bucket -> {
                SearchResponseAttrVo responseAttrVo = new SearchResponseAttrVo();
                //设置搜索参数id
                responseAttrVo.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());

                Map<String, Aggregation> aggregationMap = ((Terms.Bucket) bucket).getAggregations().getAsMap();

                //设置搜索参数值的集合
                ParsedStringTerms searchAttrValueAggs = (ParsedStringTerms)aggregationMap.get("searchAttrValueAggs");
                List<? extends Terms.Bucket> valueAggsBuckets = searchAttrValueAggs.getBuckets();

                if (!CollectionUtils.isEmpty(valueAggsBuckets)) {
                    List<String> attrValues = valueAggsBuckets.stream().map(valueBucket -> (String)((Terms.Bucket) valueBucket).getKey()).collect(Collectors.toList());
                    responseAttrVo.setAttrValues(attrValues);
                }

                //设置搜索参数名
                ParsedStringTerms searchAttrNameAggs = (ParsedStringTerms)aggregationMap.get("searchAttrNameAggs");
                responseAttrVo.setAttrName((String)searchAttrNameAggs.getBuckets().get(0).getKey());

                return responseAttrVo;
            }).collect(Collectors.toList());

            responseVo.setFilters(filters);
        }

        return responseVo;
    }

    /**
     * 构建搜索dsl 语句
     *
     * @param paramVo
     * @return
     */
    private SearchSourceBuilder buildDsl(SearchParamVo paramVo) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        String keyword = paramVo.getKeyword();
        if (StringUtils.isBlank(keyword)) {
            //可打广告
            return null;
        }

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1.布尔查询并过滤
        //1.1 按照 keyword 查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));

        //1.2 按照品牌 id 匹配过滤
        List<Long> brandIds = paramVo.getBrandId();

        if (!CollectionUtils.isEmpty(brandIds)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandIds));
        }

        //1.2 按照分类 id 匹配过滤
        Long cid = paramVo.getCid();

        if (cid != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryId", cid));
        }

        //1.3 按照是否有库存过滤
        Boolean store = paramVo.getStore();
        boolQueryBuilder.filter(QueryBuilders.termsQuery("store", store));

        //1.4 按照价格区间过滤
        Double priceFrom = paramVo.getPriceFrom();
        Double priceTo = paramVo.getPriceTo();

        if (priceFrom != null || priceTo != null) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");

            if (priceFrom != null) {
                rangeQueryBuilder.gte(priceFrom);
            }
            if (priceTo != null) {
                rangeQueryBuilder.lte(priceTo);
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        //1.5 按照规格参数 id 及值过滤 props=5:高通-麒麟&props=6:骁龙865-硅谷1000
        List<String> props = paramVo.getProps();
        if (!CollectionUtils.isEmpty(props)) {
            props.forEach(prop -> {
                String[] propSplit = StringUtils.split(prop, ":");
                if (propSplit.length == 2) {
                    String[] attrValueSplit = StringUtils.split(propSplit[1], "-");
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId", propSplit[0]));
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue", attrValueSplit));

                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs", boolQuery, ScoreMode.None));
                }
            });
        }

        //2.分页
        Integer pageNum = paramVo.getPageNum();
        Integer pageSize = paramVo.getPageSize();

        if (pageNum > 0) {
            sourceBuilder.from((pageNum - 1) * pageSize);
            sourceBuilder.size(pageSize);
        } else {
            sourceBuilder.from(1);
            sourceBuilder.size(pageSize);
        }


        //3.排序 0-默认，得分降序；1-按价格升序；2-按价格降序；3-按创建时间降序；4-按销量降序
        switch (paramVo.getSort()) {
            case 1:
                sourceBuilder.sort("price", SortOrder.ASC);
                break;
            case 2:
                sourceBuilder.sort("price", SortOrder.DESC);
                break;
            case 3:
                sourceBuilder.sort("createTime", SortOrder.DESC);
                break;
            case 4:
                sourceBuilder.sort("sales", SortOrder.DESC);
                break;
            default:
                break;
        }

        //4.高亮
        sourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<b style='color:red'>").postTags("</b>"));

        //5.聚合
        //5.1 品牌id 聚合
        TermsAggregationBuilder brandIdAggs = AggregationBuilders.terms("brandIdAggs").field("brandId");
        //5.1.1 品牌名聚合
        TermsAggregationBuilder brandNameAgg = AggregationBuilders.terms("brandNameAggs").field("brandName");
        //5.1.2 品牌logo 聚合
        TermsAggregationBuilder brandLogoAgg = AggregationBuilders.terms("brandLogoAggs").field("logo");

        brandIdAggs.subAggregation(brandNameAgg);
        brandIdAggs.subAggregation(brandLogoAgg);

        sourceBuilder.aggregation(brandIdAggs);

        //5.2 分类id 聚合/分类名聚合
        sourceBuilder.aggregation(
                AggregationBuilders.terms("categoryIdAggs").field("categoryId")
                        .subAggregation(AggregationBuilders.terms("categoryNameAggs").field("categoryName"))
        );

        //5.3 搜索参数聚合：id,name,value
        sourceBuilder.aggregation(
                AggregationBuilders.nested("searchAttrsAggs", "searchAttrs")
                        .subAggregation(AggregationBuilders.terms("searchAttrsIdAggs").field("searchAttrs.attrId")
                                .subAggregation(AggregationBuilders.terms("searchAttrNameAggs").field("searchAttrs.attrName"))
                                .subAggregation(AggregationBuilders.terms("searchAttrValueAggs").field("searchAttrs.attrValue"))
                        )
        );

        sourceBuilder.query(boolQueryBuilder).fetchSource(new String[]{"skuId", "title", "defaultImage", "subTitle", "price"}, null);

//        System.out.println(sourceBuilder);
        return sourceBuilder;
    }

    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private GoodsRepository goodsRepository;

    @Override
    public void createIndex(Long spuId) {
        SpuEntity spuEntity = gmallPmsClient.querySpuById(spuId).getData();
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
    }

}
