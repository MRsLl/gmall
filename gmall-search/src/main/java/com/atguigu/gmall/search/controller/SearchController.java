package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.entity.SearchParamVo;
import com.atguigu.gmall.search.entity.SearchResponseVo;
import com.atguigu.gmall.search.sevice.SearchService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("search")
public class SearchController {


    @Autowired
    private SearchService searchService;

    @GetMapping
    @ApiOperation("按条件查询索引库中数据")
    public ResponseVo<Object> search(SearchParamVo searchParamVo) {
        SearchResponseVo responseVo = searchService.search(searchParamVo);

        return ResponseVo.ok(responseVo);
    }
}
