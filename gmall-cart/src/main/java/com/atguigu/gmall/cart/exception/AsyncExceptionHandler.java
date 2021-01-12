package com.atguigu.gmall.cart.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 自定义异步方法异常处理类
 */
@Component
@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String KEY = "cart:async:exception";

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
      log.error("异步调用方法发生异常，方法为{}，参数为{}，异常信息为{}。", method.getName(),objects,throwable.getMessage());
      String userId = (String)objects[0];
      redisTemplate.opsForSet().add(KEY,userId);
    }
}
