package jpcamara.config.service;

import java.io.File;
import java.util.Map;
import java.util.Set;

public interface ConfigurationProvider {
    <T> T getConfigurationAs(File configuration, Class<T> type);
    Map<String, String> handledTypes();
    boolean handlesExtension(String extension);
    boolean handlesType(String type);
    String getName();
}
