package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.vo.SaleAttrValueVo;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Resource
    private AttrMapper attrMapper;
    @Resource
    private SkuMapper skuMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySearchAttrValueBySkuId(Long skuId) {
        QueryWrapper<SkuAttrValueEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id",skuId);

        List<SkuAttrValueEntity> skuAttrValueEntities = baseMapper.selectList(queryWrapper);

        ArrayList<SkuAttrValueEntity> searchAttrValueEntityArrayList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
            skuAttrValueEntities.forEach(skuAttrValueEntity -> {
                AttrEntity attrEntity = attrMapper.selectById(skuAttrValueEntity.getAttrId());

                if (attrEntity.getSearchType() == 1) {
                    searchAttrValueEntityArrayList.add(skuAttrValueEntity);
                }
            });
        }

        return searchAttrValueEntityArrayList;
    }

    @Override
    public List<SaleAttrValueVo> querySkuAttrValuesBySpuId(Long spuId) {
        //1.根据spuId 查询所有sku 对象
        QueryWrapper<SkuEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id",spuId);

        List<SkuEntity> skuEntities = skuMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(skuEntities)) {
            return null;
        }
        //将skuEntity 集合转化为skuId 集合
        List<Long> skuIds = skuEntities.stream().map(skuEntity ->
                skuEntity.getId()
        ).collect(Collectors.toList());

        //2.根据skuId 集合查询所有对应的skuAttrValueEntity
        QueryWrapper<SkuAttrValueEntity> wrapper = new QueryWrapper<>();
        wrapper.in("sku_id",skuIds);

        List<SkuAttrValueEntity> skuAttrValueEntities = baseMapper.selectList(wrapper);

        //3.将sku 规格参数集合转化为 SaleAttrValueVo 集合
        Map<Long, List<SkuAttrValueEntity>> map = skuAttrValueEntities.stream().collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));

        List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();
        map.forEach((attrId,skuAttrValueEntityList) -> {
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();

            saleAttrValueVo.setAttrId(attrId);
            saleAttrValueVo.setAttrName(skuAttrValueEntities.get(0).getAttrName());
            Set<String> attrValues = skuAttrValueEntityList.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet());
            saleAttrValueVo.setAttrValues(attrValues);

            saleAttrValueVos.add(saleAttrValueVo);
        });

        return saleAttrValueVos;
    }

    @Override
    public List<SkuAttrValueEntity> querySkuAttrValueEntityBySkuId(Long skuId) {
        QueryWrapper<SkuAttrValueEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id",skuId);

        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public String querySkusJson(Long spuId) {
        List<Map<String,Object>> skus = baseMapper.querySkusJsonBySpuId(spuId);

        Map<String, Long> map = skus.stream().collect(Collectors.toMap(sku -> sku.get("attr_values").toString(), sku -> (Long)sku.get("sku_id")));

        return JSON.toJSONString(map);
    }

}