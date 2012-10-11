/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neilbeveridge.redis.throttle;

/**
 *
 * @author neilbeveridge
 */
public interface CoolOff {

    int coolOffRemaining (String discriminator);
    void addCooloff (String discriminator, int coolOffSeconds);

}
