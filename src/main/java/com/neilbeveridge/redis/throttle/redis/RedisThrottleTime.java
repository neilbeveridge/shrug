/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neilbeveridge.redis.throttle.redis;

import java.util.Date;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import com.neilbeveridge.redis.throttle.Throttle;

/**
 *
 * @author neilbeveridge
 */
public class RedisThrottleTime implements Throttle {

    private final JedisPool pool;
    
    public RedisThrottleTime () {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxActive(50);
        
        this.pool = new JedisPool(config, "localhost");
    }
    
    @Override
    public long fetchRequestsPerSecond(String discriminator) {
        String key = String.format("throttle:%s:%tS", discriminator, new Date());

        Jedis jedis = pool.getResource();
        
        long counter = 0;
        try {
            Transaction t = jedis.multi();
            Response<Long> postIncrement = t.incr(key);
            t.expire(key, 2);
            t.exec();
            counter = postIncrement.get();
        } finally {
            pool.returnResource(jedis);
        }
        
        return counter;
    }
    
}
