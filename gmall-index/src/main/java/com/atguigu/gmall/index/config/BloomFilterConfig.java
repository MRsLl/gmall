package com.atguigu.gmall.index.config;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.PmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.sun.xml.internal.fastinfoset.Encoder;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class BloomFilterConfig {

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private PmsClient pmsClient;

    @Bean
    public RBloomFilter rBloomFilter() {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter("bloomFilter");
        bloomFilter.tryInit(50l,0.03);


        List<CategoryEntity> categoryEntities = pmsClient.queryCategoryByParentId(0L).getData();

        if (!CollectionUtils.isEmpty(categoryEntities)) {
            categoryEntities.forEach(categoryEntity -> {
                bloomFilter.add(categoryEntity.getId().toString());
            });
        }

        return bloomFilter;
    }
}
