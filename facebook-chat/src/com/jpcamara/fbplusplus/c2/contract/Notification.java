/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.contract;

/**
 * A Notification Message announces changes of state.
 * A Notification object may only be send downwards in a C2 architecture.
 * It is the way that higher layers communicate with lower layers
 * <p>Connectors and Components may either send a Notification object from
 * their bottom Domain, or receive a Notification object from their top Domain</p>
 * @author johnpcamara
 */
public interface Notification extends Message {
    
}
