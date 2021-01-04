package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author ll
 * @email ll@atguigu.com
 * @date 2020-12-14 19:34:31
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<CategoryEntity> queryCategoryByParentId(Long parentId);

    List<CategoryEntity> getCategoriesByLv1Id(Long parentId);

    List<CategoryEntity> getCategoriesByLv3Id(Long cid);
}

