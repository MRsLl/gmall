package com.atguigu.gmall.item.service;

import com.atguigu.gmall.item.vo.ItemVo;

public interface ItemService {
    ItemVo getItemBySkuId(Long skuId);
}
