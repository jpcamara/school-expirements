package jpcamara.config;

import com.google.gson.Gson;
import jpcamara.config.service.impl.AbstractProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created with IntelliJ IDEA.
 * User: johnpcamara
 * Date: 4/24/12
 * Time: 11:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class JsonProvider extends AbstractProvider {
    public JsonProvider() {
        handledTypes().put("json", "*");
    }

    @Override
    public <T> T getConfigurationAs(File configuration, Class<T> type) {
        try {
            return (T)new Gson().fromJson(new FileReader(configuration), type);
        } catch (FileNotFoundException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public String getName() {
        return "Json";
    }
}
