/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.topology;

import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jpcamara.fbplusplus.c2.contract.Component;
import com.jpcamara.fbplusplus.c2.contract.Connector;
import com.jpcamara.fbplusplus.c2.contract.Domain;
import com.jpcamara.fbplusplus.c2.contract.Notification;
import com.jpcamara.fbplusplus.c2.contract.Request;
import com.jpcamara.fbplusplus.c2.model.Command;
import com.jpcamara.fbplusplus.c2.model.Data;
import com.jpcamara.fbplusplus.c2.model.User;
import com.jpcamara.fbplusplus.c2.util.LoggerUtil;

/**
 * 
 * @author johnpcamara
 */
public class LogicComponent implements Component {

	private Domain<Request, Notification> topDomain;
	private Domain<Notification, Request> bottomDomain;
	private Connector topConnector;
	private Connector bottomConnector;
	private final Queue<Request> requests = new ConcurrentLinkedQueue<Request>();
	private final Queue<Notification> notifications = new ConcurrentLinkedQueue<Notification>();
	private final LoggerUtil aLogger = new LoggerUtil(this);

	public LogicComponent() {
		topDomain = new TopDomain(this);
		bottomDomain = new BottomDomain(this);
	}

	public void send(Notification aNotification) {
		aLogger.info("LogicComponent: send(Notification) " + aNotification);
		bottomConnector.topDomain().receive(aNotification);
	}

	public void send(Request aRequest) {
		aLogger.info("send the message to the topconnectors bottom domain");
		processCommand(aRequest.getCommand(), aRequest);
		topConnector.bottomDomain().receive(aRequest);
	}

	public void receive(Notification aNotification) {
		notifications.add(aNotification);
	}

	public void receive(Request aRequest) {
		aLogger.info("LogicComponent receive(request): " + aRequest);
		requests.add(aRequest);
	}

	private void processCommand(Command command, Request aRequest) {
		switch (command) {
			case LOGIN:
				setLoginCredentials(aRequest);
				break;
			case SEND_CHAT:
				sendChat(aRequest);
				break;
		}
	}
	
	public void createChatroom(Request aRequest) {
		
	}

	@SuppressWarnings("unchecked")
	public void setLoginCredentials(Request aRequest) {
		aLogger.info("Logic component: " + aRequest.toString());
		Map<String, Object> requestData = (Map<String, Object>) aRequest
				.getData().get(Data.REQUEST_DATA);
		User u = (User) aRequest.getData().get(Data.USER);
		aLogger.info("data: " + requestData);
		aLogger.info("command in request: " + aRequest.getCommand() + " command in data: " + requestData.get("command"));
		aLogger.info("userName : " + requestData.get("userName"));
		aLogger.info("password : " + requestData.get("password"));
		u.setUserName((String)requestData.get("userName"));
		u.setPassword((String)requestData.get("password"));
	}

	public void sendChat(Request aRequest) {

	}

	public void run() {
		while (true) {
			synchronized (requests) {
				if (requests.isEmpty() == false) {
					send(requests.poll());
				}
			}
			synchronized (notifications) {
				if (notifications.isEmpty() == false) {
					send(notifications.poll());
				}
			}
		}
	}

	public void bindConnector(Connector top, Connector bottom) {
		topConnector = top;
		bottomConnector = bottom;
	}

	public Domain<Request, Notification> topDomain() {
		return topDomain;
	}

	public Domain<Notification, Request> bottomDomain() {
		return bottomDomain;
	}

}
