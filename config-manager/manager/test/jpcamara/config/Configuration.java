package jpcamara.config;

import javax.activation.FileTypeMap;
import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "ConfigManager")
public class Configuration {
    @XmlElement(name = "Project")
    private Project project;

    public Project getProject() {
        return project;
    }

    public static class Project {
        @XmlAttribute
        private String name;
        @XmlElement(name = "Structure")
        private Structure structure;
        @XmlElementWrapper(name = "ResourceTypes")
        @XmlElement(name = "Type")
        private List<Type> type;
        
        public String getName() {
            return name;
        }

        public Structure getStructure() {
            return structure;
        }
        
        public List<Type> getType() {
            return type;
        }

        public static class Structure {
            @XmlElement(name = "Path")
            private List<String> path;

            public List<String> getPath() {
                return path;
            }
        }

        public static class Type {
            @XmlAttribute
            private String extension;
            @XmlAttribute
            private FileType type;
            
            public String getExtension() {
                return extension;
            }

            public FileType getType() {
                return type;
            }
            
            public static enum FileType {
                @XmlEnumValue("Properties")
                PROPERTIES,
                @XmlEnumValue("DOM")
                DOM,
                @XmlEnumValue("JSON")
                JSON;
            }
        }
    }
}
