/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.topology;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.jpcamara.fbplusplus.c2.contract.Topology;

/**
 *
 * @author johnpcamara
 */
public class TopologyRegistry {
    private static final TopologyRegistry INSTANCE = new TopologyRegistry();
    private final Map<String, Topology> topologies;

    private TopologyRegistry() {
        topologies = Collections.synchronizedMap(new HashMap<String, Topology>());        
    }

    public static TopologyRegistry instance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
	public <T extends Topology> T get(String name) {
        return (T)topologies.get(name);
    }

    public void register(String name, Topology t) {
        topologies.put(name, t);
    }
}
