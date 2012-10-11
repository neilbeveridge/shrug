/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neilbeveridge.redis.throttle;

/**
 *
 * @author neilbeveridge
 */
public interface Throttle {

    long fetchRequestsPerSecond (String discriminator);

}
