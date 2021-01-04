package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class GmallCacheAspect {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RBloomFilter bloomFilter;

    @Around("@annotation(com.atguigu.gmall.index.aspect.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        //先获取方法中的参数，若其在布隆过滤器中不存在，则直接返回null
        Object[] args = joinPoint.getArgs();
        String pid = Arrays.asList(args).get(0).toString();

        if (!bloomFilter.contains(pid)) {
            return null;
        }

        //获取切点方法的签名
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        //获取方法对象
        Method method = signature.getMethod();
        //获取方法上指定注解的类的对象
        GmallCache annotation = method.getAnnotation(GmallCache.class);
        //获取注解中的前缀
        String prefix = annotation.prefix();
        //获取方法的参数
        String param = Arrays.asList(args).toString();
        //获取方法的返回值类型
        Class<?> returnType = method.getReturnType();
        //拦截前代码块，判断缓存中有没有数据
        String json = redisTemplate.opsForValue().get(prefix + param);

        if (StringUtils.isNotBlank(json)) {
            return JSON.parseObject(json,returnType);
        }

        //若缓存中没有数据则加分布式锁
        String lock = annotation.lock();
        RLock rLock = redissonClient.getLock(lock + param);
        rLock.lock();

        //再判断缓存中是否有数据，因为在加锁时可能已有线程向缓存中存入数据
        String json2 = redisTemplate.opsForValue().get(prefix + param);

        if (StringUtils.isNotBlank(json2)) {
            rLock.unlock();
            return JSON.parseObject(json2,returnType);
        }

        //执行目标方法
        Object result = joinPoint.proceed(args);

        //拦截后代码块，放入缓存，释放分布式锁
        int random = annotation.random();
        int timeout = annotation.timeout();
        redisTemplate.opsForValue().set(prefix + param,JSON.toJSONString(result),timeout + new Random().nextInt(random), TimeUnit.MINUTES);

        rLock.unlock();
        return result;
    }
}
