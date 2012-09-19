/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.topology;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.jpcamara.fbplusplus.c2.AppContext;
import com.jpcamara.fbplusplus.c2.contract.Notification;
import com.jpcamara.fbplusplus.c2.model.ChatRoom;
import com.jpcamara.fbplusplus.c2.model.Data;
import com.jpcamara.fbplusplus.c2.model.User;
import com.jpcamara.fbplusplus.c2.util.LoggerUtil;

/**
 * Web application lifecycle listener.
 * 
 * @author johnpcamara
 */
@WebListener()
public class ApplicationInitializer implements ServletContextListener {

	private LoggerUtil aLogger = new LoggerUtil(this);
	private static final ExecutorService exec = Executors
			.newFixedThreadPool(15);
	private static final Queue<AsyncContext> jobQueue = new ConcurrentLinkedQueue<AsyncContext>();
	private static final BlockingQueue<Notification> messageQueue = new LinkedBlockingQueue<Notification>();

	public void contextInitialized(ServletContextEvent sce) {
		aLogger.info("Starting up Facebook++ application");
		setupAndStartTopology();
		setupAndStartCommunication();
	}

	/**
	 * Sets up the component/connector relationships and starts each Topology
	 * object
	 */
	private void setupAndStartTopology() {
		// create components/connectors, register them, and then start them
		HttpConnector httpConnector = new HttpConnector();
		ChatApiConnector chatConnector = new ChatApiConnector();
		DefaultConnector defaultConnector = new DefaultConnector();
		
		LogicComponent logicComponent = new LogicComponent();
		ModelComponent modelComponent = new ModelComponent();
		
		
		// httpConnector communicates with the logic component
		httpConnector.bindTopTopology(logicComponent);
		// which communicates with both httpConnector and chatConnector
		logicComponent.bindConnector(chatConnector, httpConnector);
		// which communicates back to the logic component or with the model component
		chatConnector.bindBottomTopology(logicComponent);
		chatConnector.bindTopTopology(modelComponent);
		// which communicates with no one (default) or the chat connector
		modelComponent.bindConnector(defaultConnector, chatConnector);
		

		TopologyRegistry.instance().register("HttpConnector", httpConnector);
		TopologyRegistry.instance().register("ChatConnector", chatConnector);
		TopologyRegistry.instance().register("DefaultConnector", defaultConnector);
		TopologyRegistry.instance().register("ModelConnector", modelComponent);
		TopologyRegistry.instance().register("LogicComponent", logicComponent);
		
		exec.execute(httpConnector);
		exec.execute(chatConnector);
		exec.execute(defaultConnector);
		exec.execute(modelComponent);
		exec.execute(logicComponent);
	}

	/**
	 * Sets up the servlet context. starts the main running comet-enabling task
	 * @param context
	 */
	private void setupAndStartCommunication() {
		AppContext.instance().setAttribute(Data.NOTIFICATIONS, messageQueue);
		AppContext.instance().setAttribute(Data.JOBS, jobQueue);

		// setup the pool to start executing when the servlet starts up
		exec.execute(new Runnable() {
			public void run() {
				while (true) {
					try {
						Notification n = messageQueue.take();
						for (AsyncContext async : jobQueue) {
							HttpServletRequest request = (HttpServletRequest) async
									.getRequest();
							// check if the requestId of the user and the cookie
							// match
							// if so, send some info back!
							aLogger.info("AppInitializer: Notification: " + n);
							
							if (isApplicableNotification(n, request)) {
								ServletResponse response = async.getResponse();
								try {
									response.setContentType("text/html");
									// create a JSON object from the notification
									JSONObject message = new JSONObject();
									try {
										message.put("success", n.success() ? "1" : "0");
										message.put("message", n.getMessage());
										message.put("requestId", n.getId());
										message.put("command", n.getCommand().getCommandName());
										// if there are members in the notification, send them along in their
										// owner array
										if (n.getData().get(Data.MEMBERS) != null) {
											//JSONArray members = (JSONArray)n.getData().get(Data.MEMBERS);
											message.put("members", n.getData().get(Data.MEMBERS));
										}
										// if buddylist is available, put it in the response notification
										if (n.getData().get(Data.BUDDY_LIST) != null) {
											message.put("buddylist", n.getData().get(Data.BUDDY_LIST));
										}
										if (n.getData().get(Data.CHATROOMS) != null) {
											message.put("chatrooms", n.getData().get(Data.CHATROOMS));											
										}
										if (n.getData().get(Data.RECEIVED_CHAT) != null) {
											message.put("receivedChat", n.getData().get(Data.RECEIVED_CHAT));
										}
									} catch (Exception e) {
										aLogger.warning("Failure to generate JSON Object", e);
									}
									
									response.getWriter().print(
											message.toString());
									response.getWriter().flush();
									aLogger.info("Response payload: " + message.toString());
								} catch (Exception e) {
									aLogger.warning("IOException", e);
								}
								async.complete();
							}
						}
					} catch (InterruptedException e) {
						aLogger.warning("Interrupted the user queue take()", e);
					}
				}
			}
		});
	}
	
//	private static class Strategy {
//		public boolean 
//	}
	/**
	 * Checks if the notification is applicable to a particular group or to only one user
	 */
	private boolean isApplicableNotification(Notification n, HttpServletRequest request) {
		switch (n.getCommand()) {
			case CREATE_CHATROOM:
				return isUsersRequest(n, request);
			case GET_BUDDY_LIST:
				return isUsersRequest(n, request);
//			case INVITE_USER:
//				
//				break;
			case RETRIEVE_CHATROOMS:
				return isUsersRequest(n, request);
			case JOIN_CHAT:
				return isUserValid(n, request);
			case LEAVE_CHAT:
				return leaveChat(n, request);
			case LOGIN:
				aLogger.info("LOGIN! user : " + ((User)n.getData().get(Data.USER)).getUserName() + " is users request" + isUsersRequest(n, request));
				return isUsersRequest(n, request);
			case LOGOUT:
				if (isUsersRequest(n, request)) {
					Map<String, User> users = (Map<String,User>)AppContext.instance().getAttribute(Data.USERS);
					User u = (User)n.getData().get(Data.USER);
					users.remove(u.getUniqueId());
				}
				return isUsersRequest(n, request);
//			case OPEN_COMM:
//				
//				break;
			case RECEIVE_CHAT:
				return isUsersRequest(n, request);
			case SEND_CHAT:
				
				break;
		}
		return false;
	}
	
	private boolean leaveChat(Notification n, HttpServletRequest request) {
		User u = (User) n.getData().get(Data.USER);
		ChatRoom chatRoom = (ChatRoom)n.getData().get(Data.CHATROOM);
		String requestId = retrieveRequestId(request);
		for (User user : chatRoom.getMembers()) {
			if (requestId.equals(user.getUniqueId())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * check if the request has the same id as the user
	 * @param n
	 * @param request
	 * @return
	 */
	private boolean isUsersRequest(Notification n, HttpServletRequest request) {
		User u = (User) n.getData().get(Data.USER);
		return u.getUniqueId().equals(retrieveRequestId(request));
	}
	
	private boolean isUserValid(Notification n, HttpServletRequest request) {
		User u = (User) n.getData().get(Data.USER);
		ChatRoom chatRoom = u.getChatRoom();
		String requestId = retrieveRequestId(request);
		for (User user : chatRoom.getMembers()) {
			if (requestId.equals(user.getUniqueId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieve the requestId from the cookie
	 * @param request
	 * @return
	 */
	private String retrieveRequestId(HttpServletRequest request) {
		String requestId = null;
		for (Cookie c : request.getCookies()) {
			if (c.getName().equals("requestId")) {
				requestId = c.getValue();
			}
		}
		return requestId;
	}

	public void contextDestroyed(ServletContextEvent sce) {
		aLogger.info("Shutting down Facebook++ application");
		exec.shutdown();
	}
}
