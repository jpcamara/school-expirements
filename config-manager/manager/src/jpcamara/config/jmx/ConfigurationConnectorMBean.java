package jpcamara.config.jmx;

public interface ConfigurationConnectorMBean {
    String[] listFiles();
    String[] listProviders();
    void reload();
}
