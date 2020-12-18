package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.vo.GroupVo;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

import javax.annotation.Resource;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private AttrMapper attrMapper;

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

}