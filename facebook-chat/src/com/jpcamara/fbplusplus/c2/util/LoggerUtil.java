/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author johnpcamara
 */
public class LoggerUtil {
    private Logger aLogger;

    public LoggerUtil(Object o) {
        aLogger = Logger.getLogger(o.getClass().getName());
    }

    public void info(String message) {
        aLogger.log(Level.INFO, message);
    }

    public void info(String message, Throwable e) {
        aLogger.log(Level.INFO, message, e);
    }

    public void warning(String message) {
        aLogger.log(Level.WARNING, message);
    }

    public void warning(String message, Throwable e) {
        aLogger.log(Level.WARNING, message, e);
    }

    public void severe(String message, Throwable e) {
        aLogger.log(Level.SEVERE, message, e);
    }
}
