import org.json.JSONArray;
import org.json.JSONObject;
import util.MessageBuilder;
import util.PropertyHandler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to interact with the Spotify Api
 * @version 03 March 2022
 */
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

    public Spotify() {
        this.rp = new PropertyHandler();
        this.clientId = rp.getProperty("ClientId");
        this.clientSecret = rp.getProperty("ClientSecret");
        this.playlistId = rp.getProperty("PlaylistId");
        this.playlistIdWeekly = rp.getProperty("PlaylistIdWeekly");
        this.refreshToken = rp.getProperty("RefreshToken");

        if (this.getAuthToken()) {
            this.addSongs(this.getSongs(this.playlistIdWeekly), this.playlistId);

            System.out.println("Checking for duplicates...");
            ArrayList<String> duplicates = this.checkDuplications(this.getSongs(playlistId));

            if (Boolean.parseBoolean(rp.getProperty("RemoveDuplications"))) {
                System.out.println("Removing duplicates...");
                this.removeTrack(duplicates, this.playlistId);
                // The Spotify api deletes all songs that have the same track id,
                // so after deleting the duplicates they will be added to the playlist again
                System.out.println("Adding tracks...");
                this.addSongs(duplicates, this.playlistId);
            }
        }
    }

    /**
     * Method to get the Auth Token by using the Refresh Token
     * @return True if successfull
     */
    private boolean getAuthToken() {
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
                    MessageBuilder.successMessage("Saved new refresh token");
                }

                MessageBuilder.successMessage("Get Auth Token successfully");
            } else {
                MessageBuilder.errorMessage("Failed to get Auth Token");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Method to get songs from a playlist
     * @param playlistId Playlist ID to get the songs from
     * @return ArrayList with Spotify Track Uris
     */
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
                MessageBuilder.successMessage(String.format("Track IDs from %s loaded successfully",
                        jsonResponse.getString("name")));
            } else {
                MessageBuilder.errorMessage("Failed to get songs from playlist");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return trackIds;
    }

    /**
     * Method to add songs to the desired playlist
     * @param trackIds Array list with track IDs
     * @param playlistId Playlist ID
     */
    private void addSongs(ArrayList<String> trackIds, String playlistId) {
        String url = this.apiUrl + "playlists/" + playlistId + "/tracks";

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
                MessageBuilder.successMessage("Tracks added successfully");
            } else {
                MessageBuilder.errorMessage("Failed to add songs");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks for duplicates in an array list
     * @param trackIds Array list with track IDs
     * @return Unique duplicates
     */
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
            System.out.printf("%d Duplicates found \n", counter);
        }

        return new ArrayList<>(duplicates);
    }

    /**
     * Method to remove tracks from a desired playlist
     * @param trackIds Array list with track IDs
     * @param playlistId Playlist ID
     */
    private void removeTrack(ArrayList<String> trackIds, String playlistId) {
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
                    .uri(new URI(this.apiUrl + "playlists/" + playlistId + "/tracks"))
                    .header("Authorization", "Bearer " + this.accessToken)
                    .header("Content-Type", "application/json")
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                MessageBuilder.successMessage(String.format("%d Track IDs removed", trackIds.size()));
            } else {
                MessageBuilder.errorMessage("Failed to remove tracks");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter for playlist api url
     * @param playlistId Playlist ID
     * @return Api url
     */
    private String getPlaylistApiUrl(String playlistId) {
        return this.apiUrl + "playlists/" + playlistId + "/?market=" + this.market;
    }
}
