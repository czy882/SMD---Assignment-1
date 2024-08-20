import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {
    public static final String DEFAULT_PROPERTIES_PATH = "properties/test.properties";

    public static Properties loadPropertiesFile(String propertiesFile) {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        System.out.println("Properties file = " + propertiesFile);
        try (FileInputStream input = new FileInputStream(propertiesFile)) {
            Properties prop = new Properties();
            // load a properties file
            prop.load(input);
            return prop;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String propertiesPath = (args.length > 0) ? args[0] : DEFAULT_PROPERTIES_PATH;
        final Properties properties = loadPropertiesFile(propertiesPath);
        new Simulation(properties).run();
    }
}