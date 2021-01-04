package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.index.util.LockUtil;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;
    @Autowired
    private LockUtil lockUtil;

    @GetMapping
    public String toIndex(Model model) {
        List<CategoryEntity> categoryEntities = indexService.queryLv1Categories(0L);
        model.addAttribute("categories",categoryEntities);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/cates/{pid}")
    public ResponseVo<List<CategoryEntity>> getCategoriesByLv1Id(@PathVariable("pid") Long pid) {
        List<CategoryEntity> categoryEntities = indexService.getCategoriesByLv1Id(pid);
        return ResponseVo.ok(categoryEntities);
    }

    @ResponseBody
    @GetMapping("/index/testLock")
    public ResponseVo testLock() {
        lockUtil.testLock1();
        return ResponseVo.ok();
    }
}
