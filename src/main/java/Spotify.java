import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private ArrayList<String> trackIds = new ArrayList<>();
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";


    public Spotify() {
        ReadProperties rp = new ReadProperties();
        this.clientId = rp.getProperty("ClientId");
        this.clientSecret = rp.getProperty("ClientSecret");
        this.playlistId = rp.getProperty("PlaylistId");
        this.playlistIdWeekly = rp.getProperty("PlaylistIdWeekly");
        this.refreshToken = rp.getProperty("RefreshToken");

        this.getAuthToken();
        this.getSongs();
        this.addSongs();
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

            if(response.statusCode() >= 200 && response.statusCode() < 300) {
                JSONObject jsonResponse = new JSONObject(response.body());
                this.accessToken = jsonResponse.getString("access_token");

                System.out.println("Get Auth Token successfully " + ANSI_GREEN + "\u2713" + ANSI_RESET);
            } else {
                System.out.println("Failed to get Auth Token " + ANSI_RED + "\u2717" + ANSI_RESET);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void getSongs() {
        String header = "Bearer " + this.accessToken;
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(new URI(getPlaylistApiUrl(this.playlistIdWeekly)))
                    .header("Authorization", header)
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() >= 200 && response.statusCode() < 300) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONObject tracks = jsonResponse.getJSONObject("tracks");
                JSONArray items = tracks.getJSONArray("items");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    JSONObject track = item.getJSONObject("track");
                    String trackId = track.getString("id");
                    this.trackIds.add(trackId);
                }

                System.out.println("Track IDs loaded successfully " + ANSI_GREEN + "\u2713" + ANSI_RESET);
            } else {
                System.out.println("Failed to get songs from weekly playlist " + ANSI_RED + "\u2717" + ANSI_RESET);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addSongs() {
        String baseUrl = this.apiUrl + "playlists/" + this.playlistId + "/tracks?uris=";
        String prefix = "spotify:track:";
        AtomicBoolean first = new AtomicBoolean(true);
        StringBuilder url = new StringBuilder();

        url.append(baseUrl);

        this.trackIds.forEach((String trackId) -> {
            if (!first.get()) {
                url.append(",");
            }
            first.set(false);
            url.append(prefix);
            url.append(trackId);
        });

        try {
            HttpRequest hr = HttpRequest.newBuilder()
                    .uri(new URI(url.toString()))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + this.accessToken)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(hr, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Tracks added successfully " + ANSI_GREEN + "\u2713" + ANSI_RESET);
            } else {
                System.out.println("Failed to add songs " + ANSI_RED + "\u2717" + ANSI_RESET);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private String getPlaylistApiUrl(String playlistId) {
        return this.apiUrl + "playlists/" + playlistId + "/?market=" + this.market;
    }
}
