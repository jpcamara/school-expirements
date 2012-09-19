package jpcamara.config;

import jpcamara.config.service.impl.AbstractProvider;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class DBProvider extends AbstractProvider {
    public DBProvider() {
        handledTypes().put("dbprops", Properties.class.getName());
    }

    @Override
    public <T> T getConfigurationAs(File configuration, Class<T> type) {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(configuration));
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
        String connectionUrl = (String)properties.get("connectionUrl");
        String driver = (String)properties.get("driver");
        String user = (String)properties.get("user");
        String pass = (String)properties.get("pass");
        String sql = (String)properties.get("sql");
        String key = (String)properties.get("key");
        String value = (String)properties.get("value");

        if (connectionUrl == null || driver == null || user == null || pass == null ||
                sql == null || key == null || value == null) {
            throw new ConfigurationException("Missing required field. Required fields are: " +
                    "[connectionUrl], [driver], [user], [pass], [sql], [key], [value]");
        }

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(e);
        }


        try {
            Properties dbProperties = new Properties();
            Connection con = DriverManager.getConnection(
                    connectionUrl,
                    user,
                    pass);

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                dbProperties.put(rs.getString(key), rs.getString(value));
            }
            return (T)dbProperties;
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public String getName() {
        return "DBProps";
    }
}
