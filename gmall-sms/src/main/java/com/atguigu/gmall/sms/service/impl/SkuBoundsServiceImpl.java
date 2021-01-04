package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.entity.ItemSaleVo;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.entity.dto.SkuSaleDto;
import com.atguigu.gmall.sms.mapper.SkuFullReductionMapper;
import com.atguigu.gmall.sms.mapper.SkuLadderMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {

    @Resource
    private SkuFullReductionMapper skuFullReductionMapper;
    @Resource
    private SkuLadderMapper skuLadderMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    @Transactional
    public void saveSkuSaleInfo(SkuSaleDto skuSaleDto) {
        //7.添加sku 积分信息 sms_sku_bounds
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        skuBoundsEntity.setSkuId(skuSaleDto.getSkuId());
        skuBoundsEntity.setBuyBounds(skuSaleDto.getBuyBounds());
        skuBoundsEntity.setGrowBounds(skuSaleDto.getGrowBounds());
        List<Integer> work = skuSaleDto.getWork();
        skuBoundsEntity.setWork(work.get(3) + work.get(2)*2 + work.get(1)*4 + work.get(0)*8);

        baseMapper.insert(skuBoundsEntity);

        //8.添加sku 满减信息 sms_sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSaleDto,skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuSaleDto.getFullAddOther());

        skuFullReductionMapper.insert(skuFullReductionEntity);

        //9.添加sku 满几件打几折信息 sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSaleDto,skuLadderEntity);
        skuLadderEntity.setAddOther(skuSaleDto.getLadderAddOther());

        skuLadderMapper.insert(skuLadderEntity);
    }

    @Override
    public List<ItemSaleVo> querySalesBySkuId(Long skuId) {
        List<ItemSaleVo> itemSaleVos = new ArrayList<>();

        //查询积分信息
        SkuBoundsEntity skuBoundsEntity = baseMapper.selectOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        ItemSaleVo bounds = new ItemSaleVo();
        if (skuBoundsEntity != null) {
            bounds.setType("积分");
            bounds.setDesc("成长积分为" + skuBoundsEntity.getGrowBounds() + "，购物积分为" + skuBoundsEntity.getBuyBounds());
            itemSaleVos.add(bounds);
        }

        //查询满减信息
        SkuFullReductionEntity skuFullReductionEntity = skuFullReductionMapper.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        ItemSaleVo fullReduction = new ItemSaleVo();
        if (skuFullReductionEntity != null) {
            fullReduction.setType("满减");
            fullReduction.setDesc("满" + skuFullReductionEntity.getFullPrice() + "减" + skuFullReductionEntity.getReducePrice());
            itemSaleVos.add(fullReduction);
        }

        //查询打折信息
        SkuLadderEntity skuLadderEntity = skuLadderMapper.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        ItemSaleVo ladder = new ItemSaleVo();
        if (skuLadderEntity != null) {
            ladder.setType("打折");
            ladder.setDesc("满" + skuLadderEntity.getFullCount() + "件" + "打" + skuLadderEntity.getDiscount() + "折");
            itemSaleVos.add(ladder);
        }
        return itemSaleVos;
    }

}