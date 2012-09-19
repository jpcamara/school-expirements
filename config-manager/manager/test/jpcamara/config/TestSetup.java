package jpcamara.config;

import jpcamara.config.service.ConfigurationProvider;
import jpcamara.config.service.impl.DefaultProvider;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Before;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Use JMockit to setup the ServiceLoader interaction
 */
public class TestSetup {
    @Mocked
    private ServiceLoader<ConfigurationProvider> loader;

    @Before
    public void setUp() throws Exception {
        new NonStrictExpectations() {
            {
                ServiceLoader.load(ConfigurationProvider.class);
                returns(loader);
                loader.iterator();
                returns(new Iterator<ConfigurationProvider>() {
                    private DefaultProvider provider;
                    @Override public boolean hasNext() {
                        return provider == null;
                    }

                    @Override public ConfigurationProvider next() {
                        provider = new DefaultProvider();
                        return provider;
                    }

                    @Override public void remove() {
                        throw new UnsupportedOperationException();
                    }
                });
            }
        };
    }
}
