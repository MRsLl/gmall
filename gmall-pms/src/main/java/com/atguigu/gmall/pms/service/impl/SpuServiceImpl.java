package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.entity.vo.SkuVo;
import com.atguigu.gmall.pms.entity.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.entity.vo.SpuVo;
import com.atguigu.gmall.pms.feign.SmsFeignClient;
import com.atguigu.gmall.pms.mapper.*;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.service.SpuDescService;
import com.atguigu.gmall.sms.entity.dto.SkuSaleDto;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.service.SpuService;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Resource
    private SpuDescMapper spuDescMapper;
    @Resource
    private SpuAttrValueService spuAttrValueService;
    @Resource
    private SkuMapper skuMapper;
    @Resource
    private SkuImagesMapper skuImagesMapper;
    @Resource
    private SkuAttrValueService skuAttrValueService;
    @Autowired
    private SmsFeignClient smsFeignClient;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuByPageAndCategoryId(Long categoryId,PageParamVo pageParamVo) {
        QueryWrapper<SpuEntity> queryWrapper = new QueryWrapper<>();

        if (categoryId != 0) {
            queryWrapper.eq("category_id",categoryId);
        }

        String key = pageParamVo.getKey();
        if (!StringUtils.isBlank(key)) {
            queryWrapper.and(t -> t.like("name", key).or().like("id", key));
//            queryWrapper.like("id", key).or().like("name", key);
        }

        IPage<SpuEntity> page = this.page(pageParamVo.getPage(), queryWrapper);

        return new PageResultVo(page);
    }

    @Override
    public void saveAllInformation(SpuVo spuVo) {
        //1.新增spu 信息 pms_spu
        SpuEntity spuEntity = new SpuEntity();
        BeanUtils.copyProperties(spuVo,spuEntity);
        spuEntity.setCreateTime(new Date());
        spuEntity.setUpdateTime(spuEntity.getCreateTime());

        baseMapper.insert(spuEntity);
        Long spuId = spuEntity.getId();

        //2.添加spu 图片描述 pms_spu_desc
        SpuDescEntity spuDescEntity = new SpuDescEntity();
        spuDescEntity.setSpuId(spuId);
        spuDescEntity.setDecript(StringUtils.join(spuVo.getSpuImages(),","));

        spuDescMapper.insert(spuDescEntity);

        //3.添加spu 基本规格参数 pms_spu_attr_value

//        List<SpuAttrValueEntity> baseAttrs = spuVo.getBaseAttrs();
//        baseAttrs.forEach(spuAttrValueEntity -> {
//            spuAttrValueEntity.setSpuId(spuId);
//        });
//
//        spuAttrValueService.saveBatch(baseAttrs);
        List<SpuAttrValueVo> baseAttrs = spuVo.getBaseAttrs();

        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrs.stream().map(spuAttrValueVo -> {
                SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity);
                spuAttrValueEntity.setSpuId(spuId);
                spuAttrValueEntity.setSort(0);

                return spuAttrValueEntity;
            }).collect(Collectors.toList());

            spuAttrValueService.saveBatch(spuAttrValueEntities);
        }

        List<SkuVo> skuVos = spuVo.getSkus();

        if (CollectionUtils.isEmpty(skuVos)) {
            return;
        }
        skuVos.forEach(skuVo -> {
            //4.新增sku 信息 pms_sku
            SkuEntity skuEntity = new SkuEntity();
            BeanUtils.copyProperties(skuVo,skuEntity);
            skuEntity.setSpuId(spuId);
            skuEntity.setBrandId(spuVo.getBrandId());
            skuEntity.setCatagoryId(spuVo.getCategoryId());

            List<String> images = skuVo.getImages();
            if (skuEntity.getDefaultImage() == null && !CollectionUtils.isEmpty(images)) {
                skuEntity.setDefaultImage(images.get(0));
            }

            skuMapper.insert(skuEntity);
            Long skuId = skuEntity.getId();

            //5.添加sku 图片 pms_sku_images
            if (!CollectionUtils.isEmpty(images)) {
                images.forEach(imageUrl -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setSort(0);

                    if (imageUrl.equals(skuEntity.getDefaultImage())){
                        skuImagesEntity.setDefaultStatus(1);
                    } else {
                        skuImagesEntity.setDefaultStatus(0);
                    }
                    skuImagesEntity.setUrl(imageUrl);

                    skuImagesMapper.insert(skuImagesEntity);
                });
            }

            //6.添加sku 销售参数 pms_sku_attr_value
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();

            saleAttrs.forEach(skuAttrValueEntity -> {
                skuAttrValueEntity.setSort(0);
                skuAttrValueEntity.setSkuId(skuId);
            });

            skuAttrValueService.saveBatch(saleAttrs);

            //7.远程调用sms 服务，保存sku 优惠信息
            SkuSaleDto skuSaleDto = new SkuSaleDto();
            BeanUtils.copyProperties(skuVo,skuSaleDto);
            skuSaleDto.setSkuId(skuId);

            smsFeignClient.saveSkuSaleInfo(skuSaleDto);
        });
    }

}