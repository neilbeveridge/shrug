/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neilbeveridge.redis.throttle.redis;

import org.apache.commons.codec.digest.DigestUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisDataException;

import com.neilbeveridge.redis.throttle.Throttle;

/**
 *
 * @author neilbeveridge
 */
public class RedisThrottleLua implements Throttle {
    //private static final Logger LOG = LoggerFactory.getLogger(RedisThrottleLua.class);

    private static final String LUA_RATE = "local inc = redis.call(\"incr\",KEYS[1]); if inc == 1 then redis.call(\"expire\",KEYS[1],1) end; return inc;";
    private static final String LUA_RATE_SHA1 = DigestUtils.sha1Hex(LUA_RATE);
    
    private final JedisPool pool;
    
    public RedisThrottleLua () {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxActive(50);
        
        this.pool = new JedisPool(config, "localhost");
    }

    @Override
    public long fetchRequestsPerSecond(String discriminator) {
        String key = String.format("throttle:%s", discriminator);

        long counter = 0;
        
        Jedis jedis = pool.getResource();
        try {
            try {
                counter = (Long) jedis.evalsha(LUA_RATE_SHA1, 1, key);
            } catch (JedisDataException e) { //the script was not cached
                counter = (Long) jedis.eval(LUA_RATE, 1, key);
            }
        } finally {
            pool.returnResource(jedis);
        }

        return counter;
    }

}
