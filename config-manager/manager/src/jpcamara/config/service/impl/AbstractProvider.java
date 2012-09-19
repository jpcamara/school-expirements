package jpcamara.config.service.impl;

import jpcamara.config.service.ConfigurationProvider;
import org.w3c.dom.Document;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractProvider implements ConfigurationProvider {
    private final Map<String, String> handledTypes = new ConcurrentHashMap<String, String>();

    @Override
    public Map<String, String> handledTypes() {
        return handledTypes;
    }

    @Override
    public boolean handlesExtension(String key) {
        return handledTypes.containsKey(key);
    }

    @Override
    public boolean handlesType(String type) {
        return handledTypes.containsValue(type);
    }
}
