/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.contract;

/**
 * The Topology specifies how Component and Connector objects can interract.
 * 
 * <p>C2 constrains Connectors and Components to allow communication only through
 * a top and bottom Domain. On the top Domain, notifications can be received and
 * requests can be sent. On the bottom Domain, requests can be received and notifications
 * can be sent.</p>
 * <p>To constrain the topology of components and connectors by this definition,
 * this interface specifies that the top Domain object can send Request objects
 * and receive Notification objects.
 * It also specifies that the bottom Domain object can send Notification objects
 * and receive Request objects.</p>
 * @author johnpcamara
 */
public interface Topology extends Runnable {
    /**
     * The top Domain of an object in this Topology can make requests using
     * Request objects, and receive notifications using Notification objects
     * @return
     */
    Domain<Request, Notification> topDomain();
    /**
     * The bottom Domain of an object in this Topology can send notifications
     * using Notification objects, and receive requests using Request objects
     * @return
     */
    Domain<Notification, Request> bottomDomain();

    void send(Notification aNotification);
    void send(Request aRequest);
    void receive(Notification aNotification);
    void receive(Request aRequest);
}
