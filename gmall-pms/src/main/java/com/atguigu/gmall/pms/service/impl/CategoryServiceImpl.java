package com.atguigu.gmall.pms.service.impl;

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

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {


    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategoryByParentId(Long parentId) {
        QueryWrapper<CategoryEntity> queryWrapper = new QueryWrapper<>();

        if (parentId != -1) {
            queryWrapper.eq("parent_id",parentId);
        }

        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<CategoryEntity> getCategoriesByLv1Id(Long parentId) {

        return baseMapper.getCategoriesByLv1Id(parentId);

    }

    @Override
    public List<CategoryEntity> getCategoriesByLv3Id(Long cid) {
        ArrayList<CategoryEntity> categoryEntities = new ArrayList<>();

        CategoryEntity lv3CategoryEntity = baseMapper.selectById(cid);
        categoryEntities.add(lv3CategoryEntity);

        CategoryEntity lv2CategoryEntity = baseMapper.selectById(lv3CategoryEntity.getParentId());
        categoryEntities.add(lv2CategoryEntity);

        CategoryEntity lv1CategoryEntity = baseMapper.selectById(lv2CategoryEntity.getParentId());
        categoryEntities.add(lv1CategoryEntity);

        return categoryEntities;
    }

}