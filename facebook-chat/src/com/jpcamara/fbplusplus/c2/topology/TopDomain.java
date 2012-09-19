/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpcamara.fbplusplus.c2.topology;

import com.jpcamara.fbplusplus.c2.contract.Domain;
import com.jpcamara.fbplusplus.c2.contract.Notification;
import com.jpcamara.fbplusplus.c2.contract.Request;
import com.jpcamara.fbplusplus.c2.contract.Topology;

/**
 *
 * @author johnpcamara
 */
public class TopDomain implements Domain<Request, Notification> {

    private Topology relatedTopology;

    public TopDomain(Topology relatedTopology) {
        this.relatedTopology = relatedTopology;
    }

    public void send(Request aNotification) {
        relatedTopology.send(aNotification);
    }

    public void receive(Notification aRequest) {
        relatedTopology.receive(aRequest);
    }

    public Topology getRelatedTopology() {
        return relatedTopology;
    }
}
