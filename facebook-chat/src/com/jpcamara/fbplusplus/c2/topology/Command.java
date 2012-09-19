/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.topology;

/**
 *
 * @author johnpcamara
 */
public enum Command {
    GET_ID("getId"),
    LOGIN("login"),
    LOGIN_SUCCESS(""),
    OPEN_COMM("openComm"),
    SEND_CHAT("sendChat"),
    RECEIVE_CHAT("receiveChat"),
    INVITE_USER("inviteUser");
    
    private String command;
    
    private Command(String commandName) {
    	command = commandName;
    }
    
    public static Command fromValue(String name) {
    	for (Command c : values()) {
    		
    		return c;
    	}
		return null;
    }
    
    public String getCommandName() {
    	return command;
    }
}
