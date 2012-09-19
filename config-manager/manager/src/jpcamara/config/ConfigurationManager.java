package jpcamara.config;

import jpcamara.config.jmx.ConfigurationConnector;
import jpcamara.config.service.ConfigurationProvider;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point to dealing with configurations. Provides the interface the delegates to service providers which
 * understand how to parse property files according to their needs.
 */
public class ConfigurationManager {
    /* Singleton instance */
    private static final ConfigurationManager MANAGER = new ConfigurationManager();

    /* resources retrieved from the classpath */
    private volatile Map<String, Resource> resources = new HashMap<String, Resource>();
    /* providers on the spi path */
    private volatile Set<ConfigurationProvider> providers = new HashSet<ConfigurationProvider>();
    /* cache for improved performance on subsequent retrievals of resources */
    private Map<String, Object> resourceCache = new Hashtable<String, Object>();
    /* logger for messages in this class */
    private Logger logger = Logger.getLogger(getClass().getName());
    /* default provider handles the base case when no obvious provider is available to handle a request */
    private ConfigurationProvider defaultProvider;

    /**
     * Exposes JMX,
     * initializes all the config providers,
     * does initial file discovery.
     */
    private ConfigurationManager() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("jpcamara.config.jmx:type=ConfigurationConnector");
            ConfigurationConnector mbean = new ConfigurationConnector();
            mbs.registerMBean(mbean, name);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }

        ServiceLoader<ConfigurationProvider> configs = ServiceLoader.load(ConfigurationProvider.class);
        for (ConfigurationProvider provider : configs) {
            providers.add(provider);
            if (provider.handlesExtension("*")) {
                if (defaultProvider != null) {
                    throw new ConfigurationException("Multiple providers found that handle any extension --> '*'");
                }
                defaultProvider = provider;
            }
        }
        discoverFiles();
    }

    public static ConfigurationManager instance() {
        return MANAGER;
    }

    private synchronized void discoverFiles() {
        try {
            discoverFiles(new File(URLDecoder.decode(getClass().getResource("/").getFile(), "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 exists. This is strange.");
        }
    }

    /**
     * Iterate over project and grab each file.
     * @param file file currently being analyzed
     */
    private synchronized void discoverFiles(File file) {
        if (file.isDirectory()) {
            for (File childFile : file.listFiles(new JavaFilter())) {
                discoverFiles(childFile);
            }
            return;
        }
        String fileName = file.getName();
        int lastIndex = fileName.lastIndexOf(".");
        String extension = fileName.substring(lastIndex + 1);
        fileName = fileName.substring(0, lastIndex);
        if (resources.containsKey(fileName)) {
            throw new IllegalArgumentException(fileName + " already exists");
        }
        Resource resource = new Resource();
        resource.setConfiguration(file);
        resource.setExtension(extension);
        resource.setName(fileName);
        resources.put(fileName, resource);
    }

    /**
     * Find the provider that corresponds to a particular class
     * @param type the class that the provider will ultimately instantiate into
     * @return config provider that fits the need
     */
    private ConfigurationProvider getProvider(Class<?> type, Resource resource) {
        for (ConfigurationProvider provider : providers) {
            if (provider.handlesExtension(resource.getExtension()) &&
                    (provider.handlesType(type.getName()) || provider.handlesType("*"))) {
                return provider;
            }
        }
        return defaultProvider;
    }

    public Properties getConfigurationProperties(String name) {
        Object cache = getFromCache(name, Properties.class);
        if (cache != null) {
            return (Properties)cache;
        }
        Resource resource = getResource(name);
        ConfigurationProvider provider = getProvider(Properties.class, resource);
        return putToCache(name, provider.getConfigurationAs(resource.getConfiguration(), Properties.class));
    }

    public Document getConfigurationDocument(String name) {
        Object cache = getFromCache(name, Document.class);
        if (cache != null) {
            return (Document)cache;
        }
        Resource resource = getResource(name);
        ConfigurationProvider provider = getProvider(Document.class, resource);
        return putToCache(name, provider.getConfigurationAs(resource.getConfiguration(), Document.class));
    }

    /**
     * Attempts to take the configuration information and apply it to a particular class. There are some
     * internal framework defaults, and additional functionality can be applied using the Service DefaultProvider Interface.
     * @param name name of the configuration
     * @param type Class that the configuration will be injected into
     * @param <T> the type of Object
     * @return an object of type T, containing the configuration information
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfigurationAs(String name, Class<T> type) {
        Object cache = getFromCache(name, type);
        if (cache != null) {
            return (T)cache;
        }
        Resource resource = getResource(name);
        ConfigurationProvider provider = getProvider(type, resource);
        if (provider == null) {
            provider = defaultProvider;
        }
        return putToCache(name, provider.getConfigurationAs(resource.getConfiguration(), type));
    }

    private synchronized Object getFromCache(String name, Class<?> type) {
        Object retrieved = resourceCache.get(name);
        if (retrieved == null) {
            return null;
        }
        if (type.isAssignableFrom(retrieved.getClass())) {
            return retrieved;
        }
        return null;
    }

    private synchronized <T> T putToCache(String name, T result) {
        logger.info("Caching resource with key [" + name + "]");
        resourceCache.put(name, result);
        return result;
    }

    public synchronized void reload() {
        for (String key : resourceCache.keySet()) {
            logger.info("Clearing cache for key [" + key + "]");
        }
        resourceCache.clear();
        resources.clear();
        discoverFiles();
    }

    private synchronized Resource getResource(String name) {
        Resource file = resources.get(name);
        if (file == null) {
            throw new ConfigurationException("No configuration file was found by the name [" + name + "]");
        }
        return file;
    }

    public synchronized List<String> getFileNames() {
        List<String> fileNames = new ArrayList<String>();
        for (Resource resource : resources.values()) {
            fileNames.add(resource.getConfiguration().getAbsolutePath());
        }
        return fileNames;
    }

    public List<String> getProviderNames() {
        List<String> fileNames = new ArrayList<String>();
        for (ConfigurationProvider provider : providers) {
            for (String extension : provider.handledTypes().keySet()) {
                StringBuilder providerInfo = new StringBuilder("Provider name: ").append(provider.getName());
                providerInfo.append(" | Extension [").append(extension).append("] with type [")
                            .append(provider.handledTypes().get(extension)).append("]");
                fileNames.add(providerInfo.toString());
            }
        }
        return fileNames;
    }

    private static class JavaFilter implements FilenameFilter {
        @Override public boolean accept(File file, String name) {
            return !name.endsWith(".java") && !name.endsWith(".class");
        }
    }
}
