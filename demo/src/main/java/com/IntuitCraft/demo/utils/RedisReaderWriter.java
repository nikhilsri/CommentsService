package com.IntuitCraft.demo.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.util.List;

@Component
@Scope("singleton")
public class RedisReaderWriter {

    private final JedisPool jedisPool;

    public RedisReaderWriter(@Autowired(required = false) JedisPool jedisPool) {
        this.jedisPool = jedisPool;

    }

    public void addKeyWithExpiry(String key, String value, int expiryTimeInSec) {

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, expiryTimeInSec, value);
        }
    }

    public String getKey(String key) {
        String value;

        try (Jedis jedis = jedisPool.getResource()) {
            value = jedis.get(key);
        }

        return value;
    }


    public void setKeyExpiry(String key, int expiryTimeInSec) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.expire(key, expiryTimeInSec);
        }
    }

    public void removeKey(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }
}
