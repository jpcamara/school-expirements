/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.contract;

/**
 * A Request Message requests the performance of an action.
 * A Request object may only be sent upwards in a C2 architecture.
 * It is the way that lower layers communicate with higher layers.
 * <p>Connectors and Components may either send a Request object from their
 * top Domain, or receive a Request object from their bottom Domain.</p>
 * @author johnpcamara
 */
public interface Request extends Message {

}
