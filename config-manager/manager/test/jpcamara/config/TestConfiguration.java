package jpcamara.config;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestConfiguration extends TestSetup {
    private Configuration config;

    @Before
    public void setUpAdditional() {
        config = ConfigurationManager.instance().getConfigurationAs("config-manager", Configuration.class);
    }
    
    @Test
    public void projectAndStructure() throws Exception {
        assertNotNull(config.getProject());
        assertNotNull(config.getProject().getStructure());
        assertEquals("propertyprovider", config.getProject().getName());
    }

    @Test
    public void types() throws Exception {
        assertEquals("properties", config.getProject().getType().get(0).getExtension());
        assertEquals(Configuration.Project.Type.FileType.PROPERTIES, config.getProject().getType().get(0).getType());
        assertEquals("xml,mdf", config.getProject().getType().get(1).getExtension());
        assertEquals(Configuration.Project.Type.FileType.DOM, config.getProject().getType().get(1).getType());
        assertEquals("json", config.getProject().getType().get(2).getExtension());
        assertEquals(Configuration.Project.Type.FileType.JSON, config.getProject().getType().get(2).getType());
    }
}
