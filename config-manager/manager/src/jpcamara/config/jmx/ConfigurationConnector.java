package jpcamara.config.jmx;

import jpcamara.config.ConfigurationManager;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.List;

public class ConfigurationConnector implements ConfigurationConnectorMBean {
    @Override
    public String[] listFiles() {
        ConfigurationManager manager = ConfigurationManager.instance();
        List<String> names = manager.getFileNames();
        return names.toArray(new String[0]);
    }

    @Override
    public String[] listProviders() {
        ConfigurationManager manager = ConfigurationManager.instance();
        List<String> names = manager.getProviderNames();
        return names.toArray(new String[0]);
    }

    @Override
    public void reload() {
        ConfigurationManager.instance().reload();
    }
}
