package com.atguigu.gmall.index.service;

import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;

public interface IndexService {
    List<CategoryEntity> queryLv1Categories(Long pid);

    List<CategoryEntity> getCategoriesByLv1Id(Long pid);

}
