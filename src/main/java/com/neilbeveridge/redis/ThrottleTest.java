/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neilbeveridge.redis;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.neilbeveridge.redis.throttle.CoolOff;
import com.neilbeveridge.redis.throttle.Throttle;
import com.neilbeveridge.redis.throttle.redis.RedisCoolOff;
import com.neilbeveridge.redis.throttle.redis.RedisThrottleLua;

/**
 *
 * @author neilbeveridge
 */
@Controller
public class ThrottleTest {
    private static Throttle throttle = new RedisThrottleLua();
    private static CoolOff coolOff = new RedisCoolOff();

    private long requestMax = 0;

    @RequestMapping("/throttle/{discriminator}/{limit}/{cooloff}")
    @ResponseBody
    public String throttle(@PathVariable String discriminator, @PathVariable int limit, @PathVariable int cooloff, HttpServletResponse httpResponse)
        throws IOException {

        boolean errord = false;

        long requestsPerSecond = throttle.fetchRequestsPerSecond(discriminator);
        int coolOffRemaining = coolOff.coolOffRemaining(discriminator);

        if (coolOffRemaining > 0) {
            errord = true;
        }

        if (requestsPerSecond > limit) {
            coolOffRemaining = cooloff;
            coolOff.addCooloff(discriminator, cooloff);
            errord = true;
        }

        if (errord) {
            httpResponse.addIntHeader("Retry-After", coolOffRemaining);
            httpResponse.sendError(429);
        }

        if (requestsPerSecond > requestMax) {
            requestMax = requestsPerSecond;
        }

        return String.format("Requests per second: %s, max: %s", requestsPerSecond, requestMax);

    }

}
