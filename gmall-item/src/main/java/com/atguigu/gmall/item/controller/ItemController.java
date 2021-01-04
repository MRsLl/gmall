package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ItemController {

    @Autowired
    private ItemService itemService;

    @RequestMapping(value = "{skuId}.html")
    @ApiOperation("根据skuId 查询商品详情页信息")
    public String getItemBySkuId(@PathVariable(value = "skuId") Long skuId, Model model) {

        ItemVo itemVo = itemService.getItemBySkuId(skuId);
        model.addAttribute("itemVo",itemVo);

        return "item";
    }
}
