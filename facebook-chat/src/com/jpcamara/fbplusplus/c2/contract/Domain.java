/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.contract;

/**
 * <p>A Domain object is defined as <code>Domain&lt;S extends Message, R extends
 * Message&gt;</code>, where <code>S</code> and <code>R</code> define what
 * can be <em>S</em>ent and <em>R</em>eceived by an object.</p>
 * @author johnpcamara
 */
public interface Domain<S extends Message, R extends Message> {
    /**
     * Implementing class can send messages of type S, which must minimally be
     * Message objects
     * @param aMessage
     */
    void send(S aMessage);
    /**
     * Implementing class can receive messages of type R, which must minimally
     * be Message objects
     * @param aMessage
     */
    void receive(R aMessage);

    Topology getRelatedTopology();
}
