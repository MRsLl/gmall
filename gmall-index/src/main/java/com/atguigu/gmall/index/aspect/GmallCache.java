package com.atguigu.gmall.index.aspect;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {

    /**
     * 缓存前缀
     * @return
     */
    String prefix() default "";

    /**
     * 缓存的有效时间
     * 单位：分钟
     * @return
     */
    int timeout() default 5;

    /**
     * 为防止雪崩设置的随机缓存时间范围
     * 单位：分钟
     * @return
     */
    int random() default 5;

    /**
     * 防击穿，分布式锁的key
     * @return
     */
    String lock() default "lock";
}
