package jpcamara.config;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: johnpcamara
 * Date: 4/24/12
 * Time: 11:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class Resource {
    private String name;
    private String extension;
    private File configuration;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public File getConfiguration() {
        return configuration;
    }

    public void setConfiguration(File configuration) {
        this.configuration = configuration;
    }
}
