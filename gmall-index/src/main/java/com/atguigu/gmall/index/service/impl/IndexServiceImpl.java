package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.aspect.GmallCache;
import com.atguigu.gmall.index.feign.PmsClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.index.util.LockUtil;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class IndexServiceImpl implements IndexService {
    @Resource
    private PmsClient pmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private LockUtil lockUtil;

    public static final String KEY_PREFIX = "index:category:";

    @Override
    public List<CategoryEntity> queryLv1Categories(Long pid) {
        List<CategoryEntity> categoryEntities = pmsClient.queryCategoryByParentId(pid).getData();
        return categoryEntities;
    }

    @Override
    @GmallCache(prefix = "index:cates:",timeout = 14400,random = 3600,lock = "lock")
    public List<CategoryEntity> getCategoriesByLv1Id(Long pid) {
        //1.先从缓存中查数据，若有则直接返回，若没有则远程调用pms 微服务从数据库中查询
/*        String cacheCategories = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (!StringUtils.isBlank(cacheCategories)) {
            List<CategoryEntity> cacheCateroryList = JSON.parseArray(cacheCategories, CategoryEntity.class);
            return cacheCateroryList;
        }*/

        List<CategoryEntity> categoryEntities = pmsClient.getCategoriesByLv1Id(pid).getData();

        //2.将从数据库中查询到的分类数据保存到缓存中
//        redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), 30, TimeUnit.DAYS);

        return categoryEntities;
    }
}
