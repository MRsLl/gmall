package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;


@Service("spuAttrValueService")
public class SpuAttrValueServiceImpl extends ServiceImpl<SpuAttrValueMapper, SpuAttrValueEntity> implements SpuAttrValueService {

    @Resource
    private AttrMapper attrMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SpuAttrValueEntity> querySearchAttrValueBySpuId(Long spuId) {
        //1.根据spuId 查询spu的规格参数值集合
        QueryWrapper<SpuAttrValueEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id",spuId);

        List<SpuAttrValueEntity> spuAttrValueEntities = baseMapper.selectList(queryWrapper);

        ArrayList<SpuAttrValueEntity> searchAttrValueEntityArrayList = new ArrayList<>();
        //2.根据spu 规格参数值的 attrId 查询其参数类型，若为1 则是搜索参数，放入返回值集合
        if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
            spuAttrValueEntities.forEach(spuAttrValueEntity -> {
                AttrEntity attrEntity = attrMapper.selectById(spuAttrValueEntity.getAttrId());

                if (attrEntity.getSearchType() == 1) {
                    searchAttrValueEntityArrayList.add(spuAttrValueEntity);
                }
            });
        }

        return searchAttrValueEntityArrayList;
    }

}