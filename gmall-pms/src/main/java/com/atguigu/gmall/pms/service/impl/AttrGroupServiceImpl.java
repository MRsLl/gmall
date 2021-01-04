package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.entity.vo.AttrValueVo;
import com.atguigu.gmall.pms.entity.vo.GroupVo;
import com.atguigu.gmall.pms.entity.vo.ItemGroupVo;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private AttrMapper attrMapper;
    @Resource
    private SpuAttrValueMapper spuAttrValueMapper;
    @Resource
    private SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public  List<GroupVo> queryAttrsByCategoryId(Long categoryId) {
        //先根据分类id 查询规格参数分组集合
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id",categoryId);
        List<AttrGroupEntity> attrGroupEntities = baseMapper.selectList(queryWrapper);

        //根据规格参数分组集合查询所有规格参数，并封装为groupVo，最终返回一个groupVo 集合
        List<GroupVo> groupVos = attrGroupEntities.stream().map(attrGroupEntity -> {
            //根据分组id 查询每个分组下的规格参数（不需要销售参数）
            QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("group_id", attrGroupEntity.getId());
            wrapper.eq("type", 1);
            List<AttrEntity> attrEntities = attrMapper.selectList(wrapper);

            GroupVo groupVo = new GroupVo();
            BeanUtils.copyProperties(attrGroupEntity, groupVo);
            groupVo.setAttrEntities(attrEntities);

            return groupVo;
        }).collect(Collectors.toList());

        return groupVos;

    }

    @Override
    public List<ItemGroupVo> queryItemGroupVosByCategoryIdAndSpuIdAndSkuId(Long cid, Long spuId, Long skuId) {
        ArrayList<ItemGroupVo> itemGroupVos = new ArrayList<>();

        //1.根据cid 查询所有规格参数分组对象
        QueryWrapper<AttrGroupEntity> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("category_id",cid);

        List<AttrGroupEntity> attrGroupEntities = baseMapper.selectList(queryWrapper1);

        if (CollectionUtils.isEmpty(attrGroupEntities)) {
            return null;
        }

        //2.根据分组id 查询所有规格参数对象
        attrGroupEntities.forEach(attrGroupEntity -> {
            ItemGroupVo itemGroupVo = new ItemGroupVo();
            itemGroupVo.setGroupName(attrGroupEntity.getName());
            ArrayList<AttrValueVo> attrValueVos = new ArrayList<>();

            QueryWrapper<AttrEntity> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.eq("group_id",attrGroupEntity.getId());

            List<AttrEntity> attrEntities = attrMapper.selectList(queryWrapper2);

            if (!CollectionUtils.isEmpty(attrEntities)) {
                List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

                //3.根据规格参数id 和spuId 查询spu 基本参数对象
                QueryWrapper<SpuAttrValueEntity> queryWrapper3 = new QueryWrapper<>();
                queryWrapper3.eq("spu_id",spuId);
                queryWrapper3.in("attr_id",attrIds);

                List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueMapper.selectList(queryWrapper3);
                //把spu 基本参数对象集合转化为attrValue 集合
                if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                    List<AttrValueVo> spuAttrValueVos = spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                        AttrValueVo attrValueVo = new AttrValueVo();
                        BeanUtils.copyProperties(spuAttrValueEntity, attrValueVo);

                        return attrValueVo;
                    }).collect(Collectors.toList());

                    attrValueVos.addAll(spuAttrValueVos);
                }

                //4.根据规格参数id 和skuId 查询sku 销售参数对象
                QueryWrapper<SkuAttrValueEntity> queryWrapper4 = new QueryWrapper<>();
                queryWrapper4.eq("sku_id",skuId);
                queryWrapper4.in("attr_id",attrIds);

                List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueMapper.selectList(queryWrapper4);

                //把sku 基本参数对象集合转化为attrValue 集合
                if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                    List<AttrValueVo> skuAttrValueVos = skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                        AttrValueVo attrValueVo = new AttrValueVo();
                        BeanUtils.copyProperties(skuAttrValueEntity, attrValueVo);

                        return attrValueVo;
                    }).collect(Collectors.toList());

                    attrValueVos.addAll(skuAttrValueVos);
                }
            }

            itemGroupVo.setAttrValues(attrValueVos);
            itemGroupVos.add(itemGroupVo);
        });

        return itemGroupVos;
    }

}