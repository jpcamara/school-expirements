package jpcamara.config;

import jpcamara.config.service.ConfigurationProvider;
import jpcamara.config.service.impl.DefaultProvider;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.ServiceLoader;

import static org.junit.Assert.assertEquals;

public class TestJsonProvider {
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
                    private JsonProvider provider;
                    @Override public boolean hasNext() {
                        return provider == null;
                    }

                    @Override public ConfigurationProvider next() {
                        provider = new JsonProvider();
                        return provider;
                    }

                    @Override public void remove() {
                        throw new UnsupportedOperationException();
                    }
                });
            }
        };
    }

    @Test
    public void parseJson() throws Exception {
        SimpleJson json = ConfigurationManager.instance().getConfigurationAs("sample", SimpleJson.class);
        assertEquals("value", json.getKey());
    }
}
