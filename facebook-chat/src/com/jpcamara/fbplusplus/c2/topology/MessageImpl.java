/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.topology;

import java.util.EnumMap;
import java.util.Map;

import com.jpcamara.fbplusplus.c2.contract.Notification;
import com.jpcamara.fbplusplus.c2.contract.Request;
import com.jpcamara.fbplusplus.c2.model.Command;
import com.jpcamara.fbplusplus.c2.model.Data;

/**
 *
 * @author johnpcamara
 */
public class MessageImpl implements Request, Notification {
    private String id;
    private String message;
    private Command command;
    private Map<Data, Object> data;
    private boolean success;

    public MessageImpl(Command command) {
        data = new EnumMap<Data, Object>(Data.class);
        this.command = command;
        success = false;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Command getCommand() {
        return command;
    }

    public Map<Data, Object> getData() {
        return data;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public void setMessage(String message) {
    	this.message = message;
    }
    
    public boolean success() {
    	return success;
    }
    
    public void setSuccess(boolean success) {
    	this.success = success;
    }
    
    public String toString() {
    	return String.format("Message info: %ndata: %s || id: %s || command: %s || success: %b", data.toString(), id, command.toString(), success);
    }

}
