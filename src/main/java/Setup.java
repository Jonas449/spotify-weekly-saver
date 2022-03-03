import util.MessageBuilder;

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
            MessageBuilder.successMessage("Config found");
        } else {
            System.out.println("Starting setup...");
            this.startSetup();
        }
        new Spotify();
    }

    private void startSetup() {
        //Create new config file
        File config = new File(defaultConfigName);

        try {
            if (config.createNewFile()) {
                MessageBuilder.successMessage("Created new config file");
            }
        } catch (IOException e) {
            MessageBuilder.errorMessage("Failed to create config file");
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
        System.out.println("Refresh Token:");
        String refreshToken = scanner.next();
        System.out.println("Remove duplicates? (y/n)");
        String duplicates  = scanner.next();
        Boolean removeDuplicates = false;

        if (duplicates.equals("y")) {
            removeDuplicates = true;
        }

        this.properties = new Properties();

        // Write settings to config file
        try {
            FileReader fileReader = new FileReader(this.defaultConfigName);
            this.properties.load(fileReader);
            this.properties.setProperty("ClientId", clientId);
            this.properties.setProperty("ClientSecret", clientSecret);
            this.properties.setProperty("PlaylistId", playlistId);
            this.properties.setProperty("PlaylistIdWeekly", playlistIdWeekly);
            this.properties.setProperty("RefreshToken", refreshToken);
            this.properties.setProperty("RemoveDuplications", removeDuplicates.toString());
            this.properties.store(new FileWriter(this.defaultConfigName), null);
            fileReader.close();
            MessageBuilder.successMessage("Added settings to " + this.defaultConfigName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
