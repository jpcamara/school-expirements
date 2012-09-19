package com.jpcamara.fbplusplus.c2;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.jpcamara.fbplusplus.c2.model.Data;

public class AppContext {
	private static final AppContext INSTANCE = new AppContext();
	private static final Map<Data, Object> context = Collections.synchronizedMap(new EnumMap<Data, Object>(Data.class));
	
	private AppContext() {
	}
	
	public static AppContext instance() {
		return INSTANCE;
	}
	
	public void setAttribute(Data name, Object value) {
		context.put(name, value);
	}
	
	public Object getAttribute(Data name) {
		return context.get(name);
	}
}
