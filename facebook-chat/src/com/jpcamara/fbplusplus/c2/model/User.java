/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.model;


/**
 * Represents a user of the Facebook++ application
 * @author johnpcamara
 */
public class User {
    private static volatile int uid = 0;

//    private static final Logger aLogger = Logger.getLogger(User.class.getName());

    private String uniqueId;
    private String userName;
    private String password;
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((facebookId == null) ? 0 : facebookId.hashCode());
		result = prime * result
				+ ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result
				+ ((uniqueId == null) ? 0 : uniqueId.hashCode());
		result = prime * result
				+ ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (facebookId == null) {
			if (other.facebookId != null)
				return false;
		} else if (!facebookId.equals(other.facebookId))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (uniqueId == null) {
			if (other.uniqueId != null)
				return false;
		} else if (!uniqueId.equals(other.uniqueId))
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}

//	private List<User> friends;
    private boolean loggedIn = false;
    private String facebookId;
    private String firstName;
    private String name;
    private ChatRoom currentChatRoom;

    public User(String userName, String password) {
        this();
        this.userName = userName;
        this.password = password;
    }

    public User() {
        uniqueId = incrementAndGetId();
//        friends = Collections.emptyList();
    }
    
    public void setChatRoom(ChatRoom chatRoom) {
    	currentChatRoom = chatRoom;
    }
    
    public ChatRoom getChatRoom() {
    	return currentChatRoom;
    }

    private synchronized String incrementAndGetId() {
        String id = "" + (++uid);
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public synchronized boolean isLoggedIn() {
        return loggedIn;
    }

    public synchronized void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

//    public synchronized List<User> getFriends() {
//        return friends;
//    }

//    public synchronized void loadFriends(JSONObject buddyList) {
//        // parsing logic and then set the friends object with a reference
//        // to an unmodifiable
//        List<User> buddies = new ArrayList<User>();
//        try {
//            JSONObject friendsList = buddyList.getJSONObject("userInfos");
//            Iterator<String> userKeys = friendsList.keys();
//            while (userKeys.hasNext()) {
//                String key = userKeys.next();
//                User user = new User();
//                user.setFacebookId(key);
//                user.setFirstName(friendsList.getJSONObject(key).getString("firstName"));
//                user.setName(friendsList.getJSONObject(key).getString("name"));
//                buddies.add(user);
//            }
//        } catch (Exception e) {
//            aLogger.log(Level.INFO, "No valid friend info", e);
//        }
//
//        friends = Collections.unmodifiableList(buddies);
//    }
//
//    public static void main(String... args) throws Exception {
//        String json = "{\"userInfos\":" +
//    "{\"14312466\":{\"name\":\"Ankawha Blain\",\"firstName\":\"Ankawha\",\"thumbSrc\":\"http://profile.ak.fbcdn.net/v22939/340/35/q14312466_6283.jpg\"}," +
//"\"603612880\":{\"name\":\"Kathleen Mackie\",\"firstName\":\"kathleen\",\"thumbSrc\":\"http://profile.ak.fbcdn.net/hprofile-ak-sf2p/hs629.snc3/27477_603612880_2455_q.jpg\"}}}";
//        JSONObject buddies = new JSONObject(json);
//        User u = new User("username", "password");
//        u.loadFriends(buddies);
//        System.out.println(u.getFriends());
//    }

	public void setFacebookId(String facebookId) {
		this.facebookId = facebookId;
	}

	public String getFacebookId() {
		return facebookId;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
