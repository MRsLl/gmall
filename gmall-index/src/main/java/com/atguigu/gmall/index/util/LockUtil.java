package com.atguigu.gmall.index.util;


import io.swagger.models.auth.In;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.management.timer.Timer;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class LockUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 加锁
     *
     * @param lockName 锁的键
     * @param uuid     锁的hash属性名
     * @param expire   过期时间
     * @return
     */
    private Boolean tryLock(String lockName, String uuid, Long expire) {
        String script = "if (redis.call('exists',KEYS[1])==0 or redis.call('hexists',KEYS[1],ARGV[1]) == 1) " +
                "then " +
                "redis.call('hincrby',KEYS[1],ARGV[1],1) " +
                "redis.call('expire',KEYS[1],ARGV[2]) " +
                "return 1 " +
                "else " +
                "return 0 " +
                "end";

        if (!redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, expire.toString())) {
            try {
                Thread.sleep(100);
                tryLock(lockName, uuid, expire);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.renewTime(lockName, expire);
        return true;
    }

    /**
     * 尝试解锁
     *
     * @param lockName
     * @param uuid
     */
    private void unLock(String lockName, String uuid) {
        String script = "if (redis.call('hexists',KEYS[1],ARGV[1]) == 0) " +
                "then  " +
                "return nil " +
                "elseif (redis.call('hincrby',KEYS[1],ARGV[1],-1)==0) " +
                "then " +
                "redis.call('del',KEYS[1]) " +
                "return 1 " +
                "else  " +
                "return 0 " +
                "end";

        Long execute = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(lockName), uuid);

        if (execute == null) {
            throw new IllegalMonitorStateException("attempt to unlock lock, not locked by lockName: "
                    + lockName + " with request: " + uuid);
        }

    }

    private void renewTime(String lockName, Long expire) {
        String script = "if ( redis.call('exists',KEYS[1]) == 1) " +
                "then " +
                "redis.call('expire',KEYS[1],ARGV[1]) " +
                "return 1 " +
                "else " +
                "return 0 " +
                "end";
        new Thread(() -> {
            while (redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), expire.toString())) {
                try {
                    Thread.sleep(expire * 1000 * 2 / 3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Autowired
    private RedissonClient redissonClient;

    public void testLock1() {
        RLock lock = redissonClient.getLock("lock");
        boolean b = false;
        try {
            b = lock.tryLock(10, TimeUnit.SECONDS);

            if (b) {
                String numStr = redisTemplate.opsForValue().get("num");

                if (StringUtils.isBlank(numStr)) {
                    return;
                }

                int num = Integer.parseInt(numStr);
                redisTemplate.opsForValue().set("num", String.valueOf(++num));

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

//    public void testLock() {
//        String uuid = UUID.randomUUID().toString();
//        Boolean lock = this.tryLock("lock", uuid, 30l);
//
//        if (lock) {
//            String numStr = redisTemplate.opsForValue().get("num");
//
//            if (StringUtils.isBlank(numStr)) {
//                return;
//            }
//
//            int num = Integer.parseInt(numStr);
//            redisTemplate.opsForValue().set("num", String.valueOf(++num));
//
//            try {
//                TimeUnit.SECONDS.sleep(60);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
////            this.testSubLock(uuid);
//            this.unLock("lock", uuid);
//        }
//    }

    private void testSubLock(String uuid) {
        Boolean lock = this.tryLock("lock", uuid, 30l);
        if (lock) {
            System.out.println("测试分布式可重入锁");
            this.unLock("lock", uuid);
        }
    }
}
