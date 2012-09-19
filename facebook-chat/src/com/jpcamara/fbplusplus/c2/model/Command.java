/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.model;

/**
 * 
 * @author johnpcamara
 */
public enum Command {
	GET_ID("getId"), LOGIN("login"), OPEN_COMM("openComm"), SEND_CHAT(
			"sendChat"), RECEIVE_CHAT("receiveChat"), INVITE_USER("inviteUser"), GET_BUDDY_LIST(
			"getBuddyList"), CREATE_CHATROOM("createChatroom"), RETRIEVE_CHATROOMS(
			"retrieveChatroomList"), LOGOUT("logout"), JOIN_CHAT("joinChat"), LEAVE_CHAT("leaveChat");

	private String command;

	private Command(String commandName) {
		command = commandName;
	}

	public static Command fromValue(String name) {
		for (Command c : values()) {
			if (c.command.equals(name)) {
				return c;
			}
		}
		return null;
	}

	public boolean in(Command... commands) {
		for (Command c : commands) {
			// == comparison is okay with enums, since there's only
			// one instance of each
			if (this == c) {
				return true;
			}
		}
		return false;
	}

	public String getCommandName() {
		return command;
	}
}
