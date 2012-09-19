/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.contract;

import java.util.Map;

import com.jpcamara.fbplusplus.c2.model.Command;
import com.jpcamara.fbplusplus.c2.model.Data;

/**
 * A Message is the data sent as first-class entities over connectors
 * It is the abstraction that allows for a simpler Domain interface for components
 * and connectors. It specifies the interface for the information a Message
 * should contain.
 * @author johnpcamara
 */
public interface Message {
    String getId();
    boolean success();
    void setSuccess(boolean successful);
    Command getCommand();
    String getMessage();
    void setMessage(String message);
    <T> Map<Data, T> getData();

    void setId(String id);
}
