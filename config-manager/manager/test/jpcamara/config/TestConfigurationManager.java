package jpcamara.config;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class TestConfigurationManager extends TestSetup {
    @Test
    public void getDocument() {
        Document doc = ConfigurationManager.instance().getConfigurationDocument("doc");
        assertEquals("content", ((Element)doc.getElementsByTagName("root").item(0)).getTextContent());
    }

    @Test
    public void getProperties() {
        Properties prop = ConfigurationManager.instance().getConfigurationProperties("someProperties");
        assertEquals("certainly", prop.get("ok"));
    }

    @Test
    public void getXmlRootElementBean() {
        Configuration config = ConfigurationManager.instance().getConfigurationAs("config-manager", Configuration.class);
        assertNotNull(config);
        assertEquals("propertyprovider", config.getProject().getName());
    }

    @Test
    public void getSimpleBeanFromXml() {
        SimpleBean bean = ConfigurationManager.instance().getConfigurationAs("simple-bean", SimpleBean.class);
        assertEquals("simple", bean.getName());
        assertEquals(123, bean.getValue());
    }
}
