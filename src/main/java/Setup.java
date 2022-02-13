import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

public class Setup {

    private final String defaultConfigName = "app.config";
    private Properties properties;

    /**
     * Checks if the config exists and then starts the saver. If not, the setup is started and a config is created.
     */
    public Setup() {
        File file = new File(defaultConfigName);
        if(file.exists()) {
            System.out.println("Config found");
        } else {
            System.out.println("Starting setup...");
            this.startSetup();
        }
        new Spotify();
    }

    private void startSetup() {
        //Create new config file
        File config = new File(defaultConfigName);
        boolean isCreated = false;

        try {
            isCreated = config.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!isCreated) {
            System.out.println("Failed to create config file");
            System.exit(0);
        }

        // Get needed settings per user input
        Scanner scanner = new Scanner(System.in);

        System.out.println("Spotify Client ID:");
        String clientId = scanner.next();
        System.out.println("Spotify Client Secret:");
        String clientSecret = scanner.next();
        System.out.println("Spotify Playlist ID:");
        String playlistId = scanner.next();
        System.out.println("Spotify Playlist Weekly ID:");
        String playlistIdWeekly = scanner.next();

        this.properties = new Properties();

        // Write settings to config file
        try {
            FileReader fileReader = new FileReader(defaultConfigName);
            this.properties.load(fileReader);
            this.properties.setProperty("ClientId", clientId);
            this.properties.setProperty("ClientSecret", clientSecret);
            this.properties.setProperty("PlaylistId", playlistId);
            this.properties.setProperty("PlaylistIdWeekly", playlistIdWeekly);
            this.properties.store(new FileWriter(defaultConfigName), null);
            System.out.println("Added settings to " + defaultConfigName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //TODO: Test if client id and secret works
    }
}
