package com.atguigu.gmall.pms.controller;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 商品三级分类
 *
 * @author ll
 * @email ll@atguigu.com
 * @date 2020-12-14 19:34:31
 */
@Api(tags = "商品三级分类 管理")
@RestController
@RequestMapping("pms/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("all/{cid}")
    @ApiOperation("根据三级分类id 查询三级二级一级分类对象")
    public ResponseVo<List<CategoryEntity>> getCategoriesByLv3Id(@PathVariable(value = "cid") Long cid){
        List<CategoryEntity> categoryEntities = categoryService.getCategoriesByLv3Id(cid);
        return ResponseVo.ok(categoryEntities);
    }

    @GetMapping("subs/{pid}")
    @ApiOperation("根据一级分类id 查询所有二级三级分类")
    public ResponseVo<List<CategoryEntity>> getCategoriesByLv1Id(@PathVariable(value = "pid") Long parentId){
        List<CategoryEntity> categoryEntities = categoryService.getCategoriesByLv1Id(parentId);
        return ResponseVo.ok(categoryEntities);
    }

    @GetMapping("parent/{parentId}")
    @ApiOperation("根据父分类id 查询子分类")
    public ResponseVo<List<CategoryEntity>> queryCategoryByParentId(@PathVariable(value = "parentId") Long parentId) {
        List<CategoryEntity>  categoryEntities = categoryService.queryCategoryByParentId(parentId);
        return ResponseVo.ok(categoryEntities);
    }
    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryCategoryByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = categoryService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id){
		CategoryEntity category = categoryService.getById(id);

        return ResponseVo.ok(category);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody CategoryEntity category){
		categoryService.updateById(category);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		categoryService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
