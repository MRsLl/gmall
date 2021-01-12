package com.atguigu.gmall.scheduled.jobhandler;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.scheduled.entity.Cart;
import com.atguigu.gmall.scheduled.mapper.CartMapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Component
public class CartJobHandler {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private CartMapper cartMapper;
    private static final String KEY = "cart:async:exception";
    private static final String KEY_PREFIX = "cart:info:";


    @XxlJob(value = "cartJobHandler")
    public ReturnT<String> executor(String param) {
        BoundSetOperations<String, String> setOps = redisTemplate.boundSetOps(KEY);
        if (setOps.size() == 0) {
            return ReturnT.SUCCESS;
        }

        String userId = setOps.pop();
        while (StringUtils.isNotBlank(userId)) {
            //1.先删除用户在mysql 中的购物车记录
            cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id",userId));

            //2.获取用户在redis 中的购物车记录
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
            List<Object> cartJsons = hashOps.values();

            //若购物车记录为空，直接进入下次循环
            if (CollectionUtils.isEmpty(cartJsons)) {
                continue;
            }

            //3.若购物车记录不为空，将用户的json 形式的购物车记录转化为cart 对象，并存入mysql 数据库
            cartJsons.forEach(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                cartMapper.insert(cart);
            });

            userId = setOps.pop();
        }

        return ReturnT.SUCCESS;
    }
}
