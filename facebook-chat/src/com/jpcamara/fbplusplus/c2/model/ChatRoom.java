/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author johnpcamara
 */
public class ChatRoom {
	private String chatroomId;
	private String chatroomName;
	private User host;
	private List<User> members;
	
	public ChatRoom() {
		members = Collections.synchronizedList(new ArrayList<User>());
	}
	
	public String getChatroomId() {
		return chatroomId;
	}
	public void setChatroomId(String chatroomId) {
		this.chatroomId = chatroomId;
	}
	public String getChatroomName() {
		return chatroomName;
	}
	public void setChatroomName(String chatroomName) {
		this.chatroomName = chatroomName;
	}
	public User getHost() {
		return host;
	}
	public void setHost(User host) {
		this.host = host;
	}
	public List<User> getMembers() {
		return members;
	}
}
