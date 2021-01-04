package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.search.entity.SearchParamVo;
import com.atguigu.gmall.search.entity.SearchResponseVo;
import com.atguigu.gmall.search.sevice.SearchService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Controller
@RequestMapping("search")
public class SearchController {


    @Autowired
    private SearchService searchService;

    @GetMapping
    @ApiOperation("按条件查询索引库中数据")
    public String search(SearchParamVo searchParamVo, Model model) {
        SearchResponseVo responseVo = searchService.search(searchParamVo);

        model.addAttribute("searchParam",searchParamVo);
        model.addAttribute("response",responseVo);

        return "search";
    }
}
