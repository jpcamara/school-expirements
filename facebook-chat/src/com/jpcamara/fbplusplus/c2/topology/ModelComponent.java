package com.jpcamara.fbplusplus.c2.topology;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jpcamara.fbplusplus.c2.contract.Component;
import com.jpcamara.fbplusplus.c2.contract.Connector;
import com.jpcamara.fbplusplus.c2.contract.Domain;
import com.jpcamara.fbplusplus.c2.contract.Notification;
import com.jpcamara.fbplusplus.c2.contract.Request;
import com.jpcamara.fbplusplus.c2.model.ChatRoom;
import com.jpcamara.fbplusplus.c2.model.Command;
import com.jpcamara.fbplusplus.c2.model.Data;
import com.jpcamara.fbplusplus.c2.model.User;
import com.jpcamara.fbplusplus.c2.util.LoggerUtil;

/**
 * The model components deals with things that facebook doesn't handle. such as
 * the creation of chatrooms
 * 
 * @author johnpcamara
 * 
 */
public class ModelComponent implements Component {

	private Domain<Request, Notification> topDomain;
	private Domain<Notification, Request> bottomDomain;
	private Connector topConnector;
	private Connector bottomConnector;
	private final Queue<Request> requests = new ConcurrentLinkedQueue<Request>();
	private final Queue<Notification> notifications = new ConcurrentLinkedQueue<Notification>();
	private final LoggerUtil aLogger = new LoggerUtil(this);
	private Map<String, ChatRoom> chatRooms = Collections
			.synchronizedMap(new HashMap<String, ChatRoom>());

	public ModelComponent() {
		topDomain = new TopDomain(this);
		bottomDomain = new BottomDomain(this);
	}

	@Override
	public void receive(Notification aNotification) {
		aLogger.info("ModelComponent: receive notification: " + aNotification);
		notifications.add(aNotification);
	}

	@Override
	public void receive(Request aRequest) {
		aLogger.info("ModelComponent: receive request: " + aRequest);
		requests.add(aRequest);
	}

	@Override
	public void send(Notification aNotification) {
		aLogger.info("ModelComponent: send notification: " + aNotification);
		bottomConnector.topDomain().receive(aNotification);
	}

	@Override
	public void send(Request aRequest) {
		aLogger.info("ModelComponent: receive request: " + aRequest);
		processCommand(aRequest);
		topConnector.bottomDomain().receive(aRequest);
	}

	@SuppressWarnings("unchecked")
	private void processCommand(Request aRequest) {
		if (aRequest.getCommand() == Command.CREATE_CHATROOM) {
			aLogger.info("ModelComponent: create a chatroom");
			User u = (User) aRequest.getData().get(Data.USER);

			ChatRoom chatRoom = null;
			synchronized (chatRooms) {
				chatRoom = chatRooms.get(u.getUserName());
				if (chatRoom == null) {
					chatRoom = new ChatRoom();
					chatRoom.setChatroomId(u.getUserName());
					chatRoom.setChatroomName(u.getUserName());
					chatRoom.setHost(u);
					chatRoom.getMembers().add(u);
					u.setChatRoom(chatRoom);
					chatRooms.put(u.getUserName(), chatRoom);
				}
			}

			// put the members into an array
			JSONArray dataArr = createMembersList(u);

			Notification notify = new MessageImpl(Command.CREATE_CHATROOM);
			notify.setId(u.getUniqueId());
			notify.getData().put(Data.USER, u);

			notify.getData().put(Data.MEMBERS, dataArr);
			notify.setSuccess(true);
			notify.setMessage("Chatroom created");
			notifications.add(notify);

		} else if (aRequest.getCommand() == Command.JOIN_CHAT) {
			aLogger.info("ModelComponent: join the chat");
			User u = (User) aRequest.getData().get(Data.USER);
			Map<String, Object> requestData = (Map<String, Object>) aRequest
					.getData().get(Data.REQUEST_DATA);
			String chatRoomId = (String) requestData.get("chatRoomId");
			ChatRoom chatRoom = chatRooms.get(chatRoomId);
			
			if (u.getChatRoom() != null) {
				leaveChatRoom(u);
			}
			
			// if chatRoom is null, there's a problem!
			chatRoom.getMembers().add(u);
			u.setChatRoom(chatRoom);

			Notification notify = new MessageImpl(Command.JOIN_CHAT);
			notify.setId(u.getUniqueId());
			notify.getData().put(Data.USER, u);

			// put the members into an array
			JSONArray dataArr = createMembersList(chatRoom.getMembers()
					.toArray(new User[] {}));

			notify.getData().put(Data.MEMBERS, dataArr);
			notify.setSuccess(true);
			notify.setMessage("Joined chat.");
			notifications.add(notify);

		} else if (aRequest.getCommand() == Command.LOGOUT) {
			aLogger.info("ModelComponent: leave the chat");
			User u = (User) aRequest.getData().get(Data.USER);
			leaveChatRoom(u);

			Notification notify = new MessageImpl(Command.LOGOUT);
			notify.setId(u.getUniqueId());
			notify.getData().put(Data.USER, u);

//			// put the members into an array
//			JSONArray dataArr = createMembersList(currentChatRoom.getMembers()
//					.toArray(new User[] {}));

//			notify.getData().put(Data.MEMBERS, dataArr);
			notify.setSuccess(true);
			notify.setMessage("logout");
			notifications.add(notify);
			
		} else if (aRequest.getCommand() == Command.LEAVE_CHAT) {
			aLogger.info("ModelComponent: leave the chat");
			User u = (User) aRequest.getData().get(Data.USER);
			ChatRoom currentChatRoom = u.getChatRoom();
			currentChatRoom.getMembers().remove(u);

			Notification notify = new MessageImpl(Command.LEAVE_CHAT);
			notify.setId(u.getUniqueId());
			notify.getData().put(Data.USER, u);

			// put the members into an array
			JSONArray dataArr = createMembersList(currentChatRoom.getMembers()
					.toArray(new User[] {}));

			notify.getData().put(Data.MEMBERS, dataArr);
			notify.getData().put(Data.CHATROOM, currentChatRoom);
			notify.setSuccess(true);
			notify.setMessage("leave chat");
			notifications.add(notify);
			
		} else if (aRequest.getCommand() == Command.RETRIEVE_CHATROOMS) {
			aLogger.info("ModelComponent: retrieve chatrooms");
			User u = (User) aRequest.getData().get(Data.USER);
			
			JSONArray chatroomContainer = new JSONArray();

			for (Entry<String, ChatRoom> entry : chatRooms.entrySet()) {
				ChatRoom room = entry.getValue();
				JSONObject objRoom = new JSONObject();
				try {
					objRoom.put("FBUserKey", room.getHost().getUserName());
		            objRoom.put("name", room.getHost().getUserName());
				} catch (Exception e) {
					aLogger.severe("Json setup failed", e);
				}
	            chatroomContainer.put(objRoom);
			}
			Notification notify = new MessageImpl(Command.RETRIEVE_CHATROOMS);
			notify.setId(u.getUniqueId());
			notify.getData().put(Data.USER, u);

			notify.getData().put(Data.CHATROOMS, chatroomContainer);
			notify.setSuccess(true);
			notify.setMessage("Retrieved chatrooms");
			notifications.add(notify);
		}
	}
	
	private void leaveChatRoom(User u) {
		aLogger.info("ModelComponent: leave the chat");
		ChatRoom currentChatRoom = u.getChatRoom();
		currentChatRoom.getMembers().remove(u);

		Notification notify = new MessageImpl(Command.LEAVE_CHAT);
		notify.setId(u.getUniqueId());
		notify.getData().put(Data.USER, u);

		// put the members into an array
		JSONArray dataArr = createMembersList(currentChatRoom.getMembers()
				.toArray(new User[] {}));

		notify.getData().put(Data.MEMBERS, dataArr);
		notify.getData().put(Data.CHATROOM, currentChatRoom);
		notify.setSuccess(true);
		notify.setMessage("leave chat");
		notifications.add(notify);
	}

	/**
	 * Goes through a vararg list of users and puts it into a JSONArray
	 * 
	 * @param users
	 * @return
	 */
	private JSONArray createMembersList(User... users) {
		// put the members into an array
		JSONArray dataArr = new JSONArray();
		for (User u : users) {
			JSONObject dataObj = new JSONObject();
			try {
				// if there are members in the notification, send them along in
				// their
				// owner array
				dataObj.put("FBUserKey", u.getUserName());
				dataObj.put("name", u.getUserName());
				dataArr.put(dataObj);
			} catch (Exception e) {
				aLogger.severe("Failed to load json data", e);
			}
		}
		return dataArr;
	}

	@Override
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

	@Override
	public void bindConnector(Connector top, Connector bottom) {
		topConnector = top;
		bottomConnector = bottom;
	}

	@Override
	public Domain<Notification, Request> bottomDomain() {
		return bottomDomain;
	}

	@Override
	public Domain<Request, Notification> topDomain() {
		return topDomain;
	}
}
