/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.contract;

/**
 * A Component is an independent, potentially concurrent message generator
 * and/or consumer 
 * Component has certain restrictions on communication. Implements Topology, which
 * gives it a top and bottom domain. Those Domain objects can request and notify
 * with Message objects.
 * <br/>They'll be able to make requests and receive notifications
 * on their top Domain.
 * <br/>They'll be able to receive requests and make notifications on their bottom
 * Domain.
 * @author johnpcamara
 */
public interface Component extends Topology {
    void bindConnector(Connector top, Connector bottom);
}
