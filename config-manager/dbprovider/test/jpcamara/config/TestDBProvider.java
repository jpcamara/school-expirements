package jpcamara.config;

import jpcamara.config.service.ConfigurationProvider;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.*;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;

import static org.junit.Assert.assertEquals;

public class TestDBProvider {
    @Mocked
    private ServiceLoader<ConfigurationProvider> loader;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        final String sql =
                "CREATE TABLE sample (id INT not null primary key GENERATED ALWAYS AS IDENTITY, myKey varchar(30), value varchar(30))";
        final String connURL = "jdbc:derby:memory:testdb;create=true;user=Sample;password=Password";
        Connection conn = DriverManager.getConnection(connURL, new Properties());

        try {
            conn.createStatement().execute(sql);
            conn.createStatement().execute("INSERT INTO sample (myKey, value) VALUES ('ok0', 'whatever0')");
            conn.createStatement().execute("INSERT INTO sample (myKey, value) VALUES ('ok1', 'whatever1')");
            conn.createStatement().execute("INSERT INTO sample (myKey, value) VALUES ('ok2', 'whatever2')");
            conn.createStatement().execute("INSERT INTO sample (myKey, value) VALUES ('ok3', 'whatever3')");
        } finally {
            conn.close();
        }
    }

    @Before
    public void setUp() throws Exception {
        new NonStrictExpectations() {
            {
                ServiceLoader.load(ConfigurationProvider.class);
                returns(loader);
                loader.iterator();
                returns(new Iterator<ConfigurationProvider>() {
                    private DBProvider provider;
                    @Override public boolean hasNext() {
                        return provider == null;
                    }

                    @Override public ConfigurationProvider next() {
                        provider = new DBProvider();
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
    public void treatDbAsProperties() throws Exception {
        Properties dbProps = ConfigurationManager.instance().getConfigurationAs("dbprops", Properties.class);
        int i = 0;
        for (Object key : dbProps.keySet()) {
            assertEquals("whatever" + i, dbProps.get("ok" + i));
            i++;
        }
    }
}
