package util;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

/**
 * This class is used to interact with the configuration file
 * @version 03 March 2022
 */
public class PropertyHandler {
    private String fileName = "app.config";
    private Properties properties;

    /**
     * Constructor
     */
    public PropertyHandler() {
        try {
            FileReader fr = new FileReader(this.fileName);
            this.properties = new Properties();
            this.properties.load(fr);
            fr.close();
        } catch (Exception e) {
            System.err.println(this.fileName + " not found");
        }
    }

    /**
     * Constructor with custom file name
     * @param fileName Configuration file name
     */
    public PropertyHandler(String fileName) {
        this.fileName = fileName;
        try {
            FileReader fr = new FileReader(this.fileName);
            this.properties = new Properties();
            this.properties.load(fr);
            fr.close();
        } catch (Exception e) {
            System.err.println(this.fileName + "not found");
        }
    }

    /**
     * Gets a property in the configuration file
     * @param property Property key
     * @return Property value
     */
    public String getProperty(String property) {
        if(this.properties != null) {
            return this.properties.getProperty(property);
        }
        return null;
    }

    /**
     * Getter for properties
     * @return Properties object
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets a property in the configuration file
     * @param key Property key
     * @param value Property value
     */
    public void setProperty(String key, String value) {
        try {
            FileReader fileReader = new FileReader(this.fileName);
            this.properties.load(fileReader);
            this.properties.setProperty(key, value);
            this.properties.store(new FileWriter(this.fileName), null);
            System.out.println("Added settings to " + this.fileName);
            MessageBuilder.successMessage("Set new " + key);
        } catch (Exception e) {
            MessageBuilder.errorMessage("Failed to set new " + key);
        }
    }
}
