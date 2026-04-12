package com.wuhan.seckill.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 写入缓存（带过期时间+随机值，防雪崩）
     */
    public boolean set(String key, Object value, long time, int random) {
        try {
            long expireTime = time + new java.util.Random().nextInt(random);
            if (expireTime > 0) {
                redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 写入缓存（永久有效，防热点key击穿）
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 读取缓存
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     */
    public boolean delete(String key) {
        try {
            redisTemplate.delete(key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 分布式锁（防缓存击穿）
     */
    public boolean tryLock(String key, long expireTime) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "lock", expireTime, TimeUnit.SECONDS));
    }

    /**
     * 释放分布式锁
     */
    public boolean releaseLock(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }
}