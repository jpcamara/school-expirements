package com.jpcamara.fbplusplus.c2.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.jpcamara.fbplusplus.c2.model.User;

/**
 * Due to some throttling concerns, added this class to allow for testing of the facebook chat connections
 * @author johnpcamara
 *
 */
public class ChannelRegistry {
	private static final Map<String, String> channels = Collections.synchronizedMap(new HashMap<String, String>());
	private static final ChannelRegistry INSTANCE = new ChannelRegistry();
	static {
		channels.put("dbgotd@gmail.com", "31");
		channels.put("johnpcamara@gmail.com", "42");
		channels.put("jp@schwadesign.com", "58");
//		channels.put("", "");
//		channels.put("", "");
	}
	
	public String getChannel(User u) {
		return channels.get(u.getUserName());
	}
	
	public static ChannelRegistry instance() {
		return INSTANCE;
	}
}
