/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.contract;

/**
 * Connector objects are routers that may filter, translate, and broadcast messages
 * of two kinds -- notifications and requests
 * @author johnpcamara
 */
public interface Connector extends Topology {
    void bindTopTopology(Topology t);
    void bindBottomTopology(Topology t);
}
