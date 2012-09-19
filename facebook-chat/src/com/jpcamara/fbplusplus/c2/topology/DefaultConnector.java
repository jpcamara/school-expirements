package com.jpcamara.fbplusplus.c2.topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jpcamara.fbplusplus.c2.contract.Connector;
import com.jpcamara.fbplusplus.c2.contract.Domain;
import com.jpcamara.fbplusplus.c2.contract.Notification;
import com.jpcamara.fbplusplus.c2.contract.Request;
import com.jpcamara.fbplusplus.c2.contract.Topology;
import com.jpcamara.fbplusplus.c2.util.LoggerUtil;

/**
 * Used to bind to a component that doesn't need a connector
 * @author johnpcamara
 *
 */
public class DefaultConnector implements Connector {

	private final LoggerUtil aLogger = new LoggerUtil(this);
	private Domain<Request, Notification> top;
    private Domain<Notification, Request> bottom;
    private List<Topology> topComponents;
    private List<Topology> bottomComponents;
    
    public DefaultConnector() {
    	top = new TopDomain(this);
        bottom = new BottomDomain(this);
        topComponents = Collections.synchronizedList(new ArrayList<Topology>());
        bottomComponents = Collections.synchronizedList(new ArrayList<Topology>());
    }
	
	@Override
	public void bindBottomTopology(Topology c) {
		bottomComponents.add(c);
	}

	@Override
	public void bindTopTopology(Topology c) {
		topComponents.add(c);
	}

	@Override
	public Domain<Notification, Request> bottomDomain() {
		return bottom;
	}

	@Override
	public void receive(Notification aNotification) {
		aLogger.info("DefaultConnector: received notification");
		
	}

	@Override
	public void receive(Request aRequest) {
		aLogger.info("DefaultConnector: received request");
	}

	@Override
	public void send(Notification aNotification) {
		aLogger.info("DefaultConnector: sent notification");
		
	}

	@Override
	public void send(Request aRequest) {
		aLogger.info("DefaultConnector: send request");
	}

	@Override
	public Domain<Request, Notification> topDomain() {
		return top;
	}

	@Override
	public void run() {
		
	}
}
