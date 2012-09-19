package main;

import jpcamara.config.ConfigurationManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        ConfigurationManager manager = ConfigurationManager.instance();
        System.out.println("Waiting forever...");
        while (true) {
            Properties props = manager.getConfigurationProperties("props");
            Document doc = manager.getConfigurationDocument("dom");
            JsonBean bean = manager.getConfigurationAs("json", JsonBean.class);

            System.out.println("Props with key of [ok] and value of [" + props.get("ok") + "]");
            System.out.println("Document with root named [root] and value of [" +
                    ((Element)doc.getElementsByTagName("root").item(0)).getTextContent() + "]");
            System.out.println("Json with key of [something] and value of [" + bean.getSomething() + "]");
            System.out.println("===========================================");
            TimeUnit.SECONDS.sleep(4);
        }
    }
}
