package com.atguigu.gmall.search.sevice;

import com.atguigu.gmall.search.entity.SearchParamVo;
import com.atguigu.gmall.search.entity.SearchResponseVo;

public interface SearchService {
    SearchResponseVo search(SearchParamVo searchParamVo);
}
