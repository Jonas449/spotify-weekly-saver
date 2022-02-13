import java.io.FileReader;
import java.util.Properties;

public class ReadProperties {
    private String fileName = "app.config";
    private Properties properties;

    public ReadProperties() {
        try {
            FileReader fr = new FileReader(this.fileName);
            this.properties = new Properties();
            this.properties.load(fr);
            fr.close();
        } catch (Exception e) {
            System.out.println(this.fileName + " not found");
        }
    }

    public ReadProperties(String fileName) {
        this.fileName = fileName;
        try {
            FileReader fr = new FileReader(this.fileName);
            this.properties = new Properties();
            this.properties.load(fr);
            fr.close();
        } catch (Exception e) {
            System.out.println(this.fileName + "not found");
        }
    }

    public String getProperty(String property) {
        if(this.properties != null) {
            return this.properties.getProperty(property);
        }
        return null;
    }
}
