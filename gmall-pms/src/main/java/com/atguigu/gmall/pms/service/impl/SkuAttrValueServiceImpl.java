package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

}