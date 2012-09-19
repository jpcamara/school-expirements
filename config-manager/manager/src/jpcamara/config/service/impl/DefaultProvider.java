package jpcamara.config.service.impl;

import jpcamara.config.ConfigurationException;
import jpcamara.config.service.ConfigurationProvider;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class DefaultProvider extends AbstractProvider {
    private static final String DOCUMENT_TYPE = Document.class.getName();
    private static final String PROPERTIES_TYPE = Properties.class.getName();

    public DefaultProvider() {
        Map<String, String> handled = handledTypes();
        handled.put("xml", Document.class.getName());
        handled.put("properties", Properties.class.getName());
        handled.put("*", "*");
    }

    @Override
    public String getName() {
        return "Default";
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getConfigurationAs(File configuration, Class<T> type) {
        String typeInfo = type.getName();
        if (DOCUMENT_TYPE.equals(typeInfo)) {
            return (T)createDocument(configuration);
        } else if (PROPERTIES_TYPE.equals(typeInfo)) {
            return (T)createProperties(configuration);
        } else { //catch-all
            return (T)createObject(configuration, type);
        }
    }

    private <T> Object createObject(File configuration, Class<T> type) {
        try {
            JAXBContext context = JAXBContext.newInstance(type);
            if (type.getAnnotation(XmlRootElement.class) != null) {
                return context.createUnmarshaller().unmarshal(configuration);
            }
            try {
                JAXBElement<T> element =
                        context.createUnmarshaller().unmarshal(new StreamSource(new FileInputStream(configuration)), type);
                return element.getValue();
            } catch (FileNotFoundException e) {
                throw new ConfigurationException(e);
            }
        } catch (JAXBException e) {
            throw new ConfigurationException(e);
        }
    }

    private Properties createProperties(File configuration) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(configuration));
            return properties;
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }

    private Document createDocument(File configuration) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                new FileInputStream(configuration)
            );
        } catch (SAXException e) {
            throw new ConfigurationException(e);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        } catch (ParserConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }
}
