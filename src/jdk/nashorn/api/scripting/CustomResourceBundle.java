package jdk.nashorn.api.scripting;

import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Collections;

public class CustomResourceBundle {
    private Properties properties;

    public CustomResourceBundle(String baseName) {
        properties = new Properties();
        loadProperties(baseName);
    }

    private void loadProperties(String baseName) {
        String resPath = baseName.replace(".", "/");
        System.out.println("resPath:"+resPath);
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resPath + ".properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new IOException("Resource not found: " + resPath + ".properties");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getString(String key) {
        return properties.getProperty(key);
    }
    public final Object getObject(String key) {
        return (Object)properties.getProperty(key);
    }

    public Enumeration<String> getKeys() {
        return Collections.enumeration(properties.stringPropertyNames());
    }

    public void listKeys() {
        for (String key : properties.stringPropertyNames()) {
            System.out.println(key + ": " + properties.getProperty(key));
        }
    }
}
