/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.topology;

import static com.jpcamara.fbplusplus.c2.model.Command.GET_BUDDY_LIST;
import static com.jpcamara.fbplusplus.c2.model.Command.INVITE_USER;
import static com.jpcamara.fbplusplus.c2.model.Command.LOGIN;
import static com.jpcamara.fbplusplus.c2.model.Command.LOGOUT;
import static com.jpcamara.fbplusplus.c2.model.Command.SEND_CHAT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jpcamara.fbplusplus.c2.contract.Connector;
import com.jpcamara.fbplusplus.c2.contract.Domain;
import com.jpcamara.fbplusplus.c2.contract.Notification;
import com.jpcamara.fbplusplus.c2.contract.Request;
import com.jpcamara.fbplusplus.c2.contract.Topology;
import com.jpcamara.fbplusplus.c2.model.ChatRoom;
import com.jpcamara.fbplusplus.c2.model.Command;
import com.jpcamara.fbplusplus.c2.model.Data;
import com.jpcamara.fbplusplus.c2.model.User;
import com.jpcamara.fbplusplus.c2.util.LoggerUtil;

/**
 * Connector for communicating with the Facebook API
 * 
 * @author johnpcamara
 */
public class ChatApiConnector implements Connector {

	private Domain<Request, Notification> top;
	private Domain<Notification, Request> bottom;
	private List<Topology> topComponents;
	private List<Topology> bottomComponents;
	private final Queue<Request> requests = new ConcurrentLinkedQueue<Request>();
	private final Queue<Notification> notifications = new ConcurrentLinkedQueue<Notification>();
	private Map<String, FacebookChatApi> facebookApi;
	private Command[] commandWhitelist = new Command[] { LOGIN, SEND_CHAT,
			INVITE_USER, GET_BUDDY_LIST, LOGOUT };
	private static final ExecutorService exec = Executors
			.newFixedThreadPool(10);
	private final LoggerUtil aLogger = new LoggerUtil(this);

	public ChatApiConnector() {
		top = new TopDomain(this);
		bottom = new BottomDomain(this);
		facebookApi = Collections
				.synchronizedMap(new HashMap<String, FacebookChatApi>());
		topComponents = Collections
				.synchronizedList(new ArrayList<Topology>());
		bottomComponents = Collections
				.synchronizedList(new ArrayList<Topology>());
	}

	public void send(Notification aNotification) {
		// send notifications to lower layer components/connectors
		for (Topology c : bottomComponents) {
			c.topDomain().receive(aNotification);
		}
	}

	public void send(Request aRequest) {
		// check if the command received is one we care about
		// we care about stuff that interacts with the facebook api
		if (aRequest.getCommand().in(commandWhitelist)) {
			aLogger.info("ChatAPI rejected command: " + aRequest.getCommand());
			processCommand(aRequest);
		}
		// the rest is movin' on up
		for (Topology c : topComponents) {
			c.bottomDomain().receive(aRequest);
		}
	}

	public void receive(Notification aNotification) {
		notifications.add(aNotification);
	}

	public void receive(Request aRequest) {
		aLogger.info("ChatApi received request : " + aRequest.getCommand());
		requests.add(aRequest);
	}

	private void processCommand(Request aRequest) {
		switch (aRequest.getCommand()) {
			case LOGIN:
				processLogin(aRequest);
				break;
			case SEND_CHAT:
				processSendChat(aRequest);
				break;
			case GET_BUDDY_LIST:
				processGetBuddies(aRequest);
				break;
			case INVITE_USER:
				processInviteUser(aRequest);
				break;
			case LOGOUT:
				processLogout(aRequest);
				break;
			default:
				break;
		}
	}

	/**
	 * 
	 * @param aRequest
	 */
	private void processLogout(Request aRequest) {
		final User u = (User) aRequest.getData().get(Data.USER);
		final FacebookChatApi api = facebookApi.get(u.getUniqueId());
		aLogger.info("ChatApi: disconnect");

		if (api.isConnected()) {
			api.disconnect();
		}
		Notification notify = new MessageImpl(Command.LOGOUT);
		notify.setId(u.getUniqueId());
		notify.getData().put(Data.USER, u);
		notify.setSuccess(true);
		notify.setMessage("Logout successful");
		// bottomDomain().send(notify);
		aLogger.info("Chat api notification: " + notify);
		notifications.add(notify);
	}

	/**
	 * Process the Login and notify appropriately
	 * 
	 * @param aRequest
	 */
	private void processLogin(Request aRequest) {
		final User u = (User) aRequest.getData().get(Data.USER);
		final FacebookChatApi api;
		aLogger.info("ChatApi: add or retrieve facebook api object");
		synchronized (facebookApi) {
			FacebookChatApi get = facebookApi.get(u.getUniqueId());
			if (get == null) {
				get = new FacebookChatApi(u);
				facebookApi.put(u.getUniqueId(), get);
				api = get;
			} else {
				api = get;
			}
		}
		exec.execute(new Runnable() {
			public void run() {
				// connect to the facebook api, and then respond
				aLogger.info("ChatApi: Connect to facebook");
				api.connect();
				Notification notify = new MessageImpl(LOGIN);
				notify.setId(u.getUniqueId());
				notify.getData().put(Data.USER, u);
				if (api.isConnected()) {
					notify.setSuccess(true);
					notify.setMessage("Login successful");
					pollIncomingMessages(api);
				} else {
					notify.setSuccess(false);
					notify.setMessage("Login failed");
				}
				// bottomDomain().send(notify);
				aLogger.info("Chat api notification: " + notify);
				notifications.add(notify);
			}
		});
	}

	/**
	 * Called after logging in successfully to facebook.
	 * 
	 * @param api
	 */
	private void pollIncomingMessages(final FacebookChatApi api) {
		exec.execute(new Runnable() {
			public void run() {
				// this works, but keep it closed for the moment to avoid
				// getting throttled
				// by facebook and being unable to test
				aLogger.info("ChatApi: poll facebook for messages");
				while (api.isConnected()) {
					JSONObject msg = api.pollMessages();
					User u = api.getUser();
					// if this user is the host of the room, 
					try {
						// if the message is from the same user, ignore it
						// facebook sends messages to the "to", as well as the "from"
						aLogger.info("Is host: " + (u == u.getChatRoom().getHost()));
						aLogger.info("Facebook id: " + u.getFacebookId() + " : msg from id " + msg.getString("fromId"));
						if (u.getFacebookId().equals(msg.getString("fromId"))) {
							aLogger.info("\n\n\ncontinued on from id\n\n\n");
							continue;
						}
					} catch (JSONException e) {
						aLogger.severe("failed to get fromId", e);
					}
					Notification notify = new MessageImpl(Command.RECEIVE_CHAT);
					notify.setId(u.getUniqueId());
					notify.getData().put(Data.USER, u);
					if (api.isConnected()) {
						notify.setSuccess(true);
						notify.setMessage("Msg returned!");
						notify.getData().put(Data.RECEIVED_CHAT, msg);
					} else {
						notify.setSuccess(false);
						notify.setMessage("Msg returned and ran amuck!");
					}
					aLogger.info("Chat api notification: " + notify);
					notifications.add(notify);
				}

			}
		});
	}

	@SuppressWarnings("unchecked")
	private synchronized void processSendChat(final Request aRequest) {
		final User u = (User) aRequest.getData().get(Data.USER);
		final ChatRoom chatRoom = u.getChatRoom();
		// get the host, since the host needs to send out all of the messages to the people
		// in the chatroom
		final User host = chatRoom.getHost();
		final FacebookChatApi api = facebookApi.get(host.getUniqueId());
		
		aLogger.info("ChatApi: add or retrieve facebook api object");

		exec.execute(new Runnable() {
			public void run() {
				// connect to the facebook api, and then respond
				aLogger.info("ChatApi: send chat to users");
				Map<String, Object> reqData = (Map<String, Object>) aRequest
						.getData().get(Data.REQUEST_DATA);
				String chatContent = (String) reqData.get("chatContent");
				chatContent = u.getUserName() + "::: " + chatContent;
//				List<String> userIds = (List<String>) reqData.get("userIds");
				for (User member : chatRoom.getMembers()) {
					if (member == chatRoom.getHost()) {
						continue;
					}
//					if ((member == chatRoom.getHost()) == false) {
					api.sendMessage(member.getFacebookId(), chatContent);
//					}
				}
				// if the user sending the message isn't the host, we need to send a message to the
				// host
				if (u != host) {
					FacebookChatApi userApi = facebookApi.get(u.getUniqueId());
					userApi.sendMessage(host.getFacebookId(), chatContent);
				}

				Notification notify = new MessageImpl(Command.SEND_CHAT);
				notify.setId(u.getUniqueId());
				notify.getData().put(Data.USER, u);

				notify.setSuccess(true);
				notify.setMessage("Chat sent successfully");

				aLogger.info("Chat api notification: " + notify);
				notifications.add(notify);
			}
		});
	}

	private synchronized void processGetBuddies(Request aRequest) {
		final User u = (User) aRequest.getData().get(Data.USER);
		final FacebookChatApi api = facebookApi.get(u.getUniqueId());
		aLogger.info("ChatApi: add or retrieve facebook api object");
		
		exec.execute(new Runnable() {
			public void run() {
				// connect to the facebook api, and then respond
				aLogger.info("ChatApi: get buddies");
				// api.connect();
				Notification notify = new MessageImpl(Command.GET_BUDDY_LIST);
				notify.setId(u.getUniqueId());
				notify.getData().put(Data.USER, u);

				try {
					String jsonBuddyList = api.getBuddyList();
					JSONObject buddyListReply = new JSONObject(jsonBuddyList);
					JSONObject payLoad = buddyListReply
							.getJSONObject("payload");
					JSONObject buddyList = payLoad.getJSONObject("buddy_list");
					JSONObject nowAvailableList = buddyList
							.getJSONObject("nowAvailableList");
					JSONObject userInfos = buddyList.getJSONObject("userInfos");

					JSONObject buddiesContainer = new JSONObject();
					JSONArray buddies = new JSONArray();

					Iterator<String> it = nowAvailableList.keys();
					while (it.hasNext()) {
						String userId = it.next();
						aLogger.info("DEBUG> " + userId);
						JSONObject obj = userInfos.getJSONObject(userId);
						String name = obj.getString("name");
						aLogger.info("DEBUG> " + name);
						JSONObject buddy = new JSONObject();
						buddy.put("buddy", name);
						buddies.put(buddy);
					}

					buddiesContainer.put("buddies", buddies);

					// JSONObject obj = new JSONObject();
					// obj.put("buddylist", buddiesContainer);

					aLogger.info(buddiesContainer.toString());
					aLogger.info("DEBUG> end FacebookRetrieveBuddylist()");

					notify.setSuccess(true);
					notify.setMessage("Retrieved buddy list");
					notify.getData().put(Data.BUDDY_LIST, buddiesContainer);
				} catch (Exception e) {
					aLogger.severe("Doh! no buddy list for you", e);
					notify.setSuccess(false);
					notify.setMessage("Retrieved buddy list failed.");
				}

				// bottomDomain().send(notify);
				aLogger.info("Chat api notification: " + notify);
				notifications.add(notify);
			}
		});
	}

	private synchronized void processInviteUser(Request aRequest) {

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

	public void bindTopTopology(Topology c) {
		topComponents.add(c);
	}

	public void bindBottomTopology(Topology c) {
		bottomComponents.add(c);
	}

	public Domain<Request, Notification> topDomain() {
		return top;
	}

	public Domain<Notification, Request> bottomDomain() {
		return bottom;
	}
}
