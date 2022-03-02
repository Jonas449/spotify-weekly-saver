import org.json.JSONArray;
import org.json.JSONObject;
import util.PropertyHandler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class Spotify {
    private String accessToken;
    private final String clientId;
    private final String clientSecret;
    private final String playlistId;
    private final String playlistIdWeekly;
    private final String refreshToken;
    private final String authUrl = "https://accounts.spotify.com/api/token";
    private final String apiUrl = "https://api.spotify.com/v1/";
    private final String market = "DE";
    private PropertyHandler rp;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";


    public Spotify() {
        this.rp = new PropertyHandler();
        this.clientId = rp.getProperty("ClientId");
        this.clientSecret = rp.getProperty("ClientSecret");
        this.playlistId = rp.getProperty("PlaylistId");
        this.playlistIdWeekly = rp.getProperty("PlaylistIdWeekly");
        this.refreshToken = rp.getProperty("RefreshToken");

        this.getAuthToken();
        this.addSongs(this.getSongs(this.playlistIdWeekly));

        System.out.println("Checking for duplicates...");
        ArrayList<String> duplicates = this.checkDuplications(this.getSongs(playlistId));

        if (Boolean.parseBoolean(rp.getProperty("RemoveDuplications"))) {
            System.out.println("Removing duplicates...");
            this.removeTrack(duplicates);
            this.addSongs(duplicates);
        }
    }

    private void getAuthToken() {
        String header = clientId + ":" + clientSecret;
        String encodedHeader = "Basic " + Base64.getEncoder().encodeToString(header.getBytes());

        try {
            HttpRequest hr = HttpRequest.newBuilder()
                    .uri(new URI(authUrl))
                    .header("Authorization", encodedHeader)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("grant_type=refresh_token&refresh_token=" + this.refreshToken))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(hr, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JSONObject jsonResponse = new JSONObject(response.body());
                this.accessToken = jsonResponse.getString("access_token");
                if (jsonResponse.has("refresh_token")) {
                    rp.setProperty("RefreshToken", jsonResponse.getString("refresh_token"));
                    System.out.println("Saved new refresh token " + ANSI_GREEN + "\u2713" + ANSI_RESET);
                }

                System.out.println("Get Auth Token successfully " + ANSI_GREEN + "\u2713" + ANSI_RESET);
            } else {
                System.err.println("Failed to get Auth Token " + ANSI_RED + "\u2717" + ANSI_RESET);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> getSongs(String playlistId) {
        ArrayList<String> trackIds = new ArrayList<>();
        String header = "Bearer " + this.accessToken;
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(new URI(getPlaylistApiUrl(playlistId)))
                    .header("Authorization", header)
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONObject tracks = jsonResponse.getJSONObject("tracks");
                JSONArray items = tracks.getJSONArray("items");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    JSONObject track = item.getJSONObject("track");
                    String trackId = track.getString("uri");
                    trackIds.add(trackId);
                }
                System.out.printf("Track IDs from %s loaded successfully " + ANSI_GREEN + "\u2713" + ANSI_RESET + "\n",
                        jsonResponse.getString("name"));
            } else {
                System.err.println("Failed to get songs from playlist " + ANSI_RED + "\u2717" + ANSI_RESET);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return trackIds;
    }

    private void addSongs(ArrayList<String> trackIds) {
        if (trackIds.size() == 0) {
            System.out.println("No new Tracks to add " + ANSI_GREEN + "\u2713" + ANSI_RESET);
            return;
        }

        String url = this.apiUrl + "playlists/" + this.playlistId + "/tracks";

        JSONArray jsonArray = new JSONArray();

        for (String trackId: trackIds) {
            jsonArray.put(trackId);
        }
        JSONObject requestBody = new JSONObject();
        requestBody.put("uris", jsonArray);

        try {
            HttpRequest hr = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + this.accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(hr, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Tracks added successfully " + ANSI_GREEN + "\u2713" + ANSI_RESET);
            } else {
                System.err.println("Failed to add songs " + ANSI_RED + "\u2717" + ANSI_RESET);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> checkDuplications(ArrayList<String> trackIds) {
        Set<String> duplicates = new HashSet<>();
        Set<String> tracks = new HashSet<>();
        int counter = 0;

        for (String trackId : trackIds) {
            if (!tracks.add(trackId)) {
                duplicates.add(trackId);
                counter++;
            }
        }
        
        if (tracks.size() < trackIds.size()) {
            System.out.printf("%d duplicates found \n", counter);
        }

        return new ArrayList<>(duplicates);
    }

    private void removeTrack(ArrayList<String> trackIds) {
        JSONArray jsonArray = new JSONArray();

        for (String trackId: trackIds) {
            JSONObject track = new JSONObject();
            track.put("uri", trackId);
            jsonArray.put(track);
        }
        JSONObject requestBody = new JSONObject();
        requestBody.put("tracks", jsonArray);

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(new URI(this.apiUrl + "playlists/" + this.playlistId + "/tracks"))
                    .header("Authorization", "Bearer " + this.accessToken)
                    .header("Content-Type", "application/json")
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.printf("%d track ids removed " + ANSI_GREEN + "\u2713" + ANSI_RESET + "\n", trackIds.size());
            } else {
                System.err.println("Failed to remove tracks " + ANSI_RED + "\u2717" + ANSI_RESET);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPlaylistApiUrl(String playlistId) {
        return this.apiUrl + "playlists/" + playlistId + "/?market=" + this.market;
    }
}
