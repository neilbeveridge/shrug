/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neilbeveridge.redis.throttle.redis;

import org.apache.commons.codec.digest.DigestUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisDataException;

import com.neilbeveridge.redis.throttle.CoolOff;

/**
 *
 * @author neilbeveridge
 */
public class RedisCoolOff implements CoolOff {
    //private static final Logger LOG = LoggerFactory.getLogger(RedisThrottleLua.class);

    private static final String LUA_TIME = "local time = redis.call(\"time\"); return tonumber(time[1]);";
    private static final String LUA_TIME_SHA1 = DigestUtils.sha1Hex(LUA_TIME);

    private final JedisPool pool;

    public RedisCoolOff() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxActive(50);

        this.pool = new JedisPool(config, "localhost");
    }

    @Override
    public int coolOffRemaining(String discriminator) {
        String key = String.format("cooloff:%s", discriminator);

        int cooloff = 0;

        Jedis jedis = pool.getResource();
        try {
            String sFinishTime = jedis.get(key);
            if (sFinishTime != null) {
                long finishTime = Long.parseLong(sFinishTime);
                cooloff = (int) (finishTime - redisEpoch());
            }
        } finally {
            pool.returnResource(jedis);
        }

        return cooloff;
    }

    @Override
    public void addCooloff(String discriminator, int coolOff) {
        String key = String.format("cooloff:%s", discriminator);

        Jedis jedis = pool.getResource();
        try {
            long redisTime = redisEpoch();
            long finishTime = redisTime + coolOff;

            Transaction t = jedis.multi();
            t.set(key, finishTime + "");
            t.expire(key, coolOff);
            t.exec();
        } finally {
            pool.returnResource(jedis);
        }
    }

    private long redisEpoch() {
        long epochTime = 0;

        Jedis jedis = pool.getResource();
        try {
            try {
                epochTime = (Long) jedis.evalsha(LUA_TIME_SHA1, 0, "1");
            } catch (JedisDataException e) { //the script was not cached
                epochTime = (Long) jedis.eval(LUA_TIME, 0, "1");
            }
        } finally {
            pool.returnResource(jedis);
        }

        return epochTime;
    }

}
