import javafx.application.Application;
import javafx.application.HostServices;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.Button;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

public class website extends Application {

    private static final String CLIENT_ID = "d8738858dcb440c1862f5a00da61a945";
    private static final String CLIENT_SECRET = "014b8129663840d2a74254865dd328f5";
    private static final String REDIRECT_URI = "http://localhost:8080";
    private static final String AUTH_URL = "https://accounts.spotify.com/authorize" +
            "?client_id=" + CLIENT_ID +
            "&response_type=code" +
            "&redirect_uri=" + REDIRECT_URI +
            "&scope=user-top-read";
    private static final String TOKEN_EXCHANGE_URL = "https://accounts.spotify.com/api/token";

    // Add these variables to store the authorization code and access token
    private static String authorizationCode;
    private static String accessToken;
    private static final String SPOTIFY_API_SEARCH_URL = "https://api.spotify.com/v1/search";
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Spotify Authentication");
        System.out.println(AUTH_URL);
        
        startHttpServer();
        
        Button openBrowserButton = new Button("Open Authentication URL");
        openBrowserButton.setOnAction(e -> openBrowserTab(AUTH_URL));
        TextField searchField = new TextField();
        Button searchButton = new Button("Search");
        TextArea resultsArea = new TextArea();

        // Event handler for the search button
        searchButton.setOnAction(e -> {
            String query = searchField.getText();
            String searchResults = searchSpotify(query);
            resultsArea.setText(searchResults);
        });

        // Layout
        VBox layout = new VBox(10);
        layout.getChildren().addAll(searchField, searchButton, resultsArea,openBrowserButton);

        // Scene
        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private static void exchangeCodeForToken() {
        try {
            // Construct the token exchange request body
            String requestBody = "grant_type=authorization_code" +
                    "&code=" + URLEncoder.encode(authorizationCode, "UTF-8") +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8") +
                    "&client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8") +
                    "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8");
    
            // Make a POST request to the token exchange URL
            URL tokenUrl = new URL(TOKEN_EXCHANGE_URL);
            HttpURLConnection tokenConnection = (HttpURLConnection) tokenUrl.openConnection();
            tokenConnection.setRequestMethod("POST");
            tokenConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            tokenConnection.setDoOutput(true);
    
            // Write the request body to the connection
            try (OutputStream os = tokenConnection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
    
            int responseCode = tokenConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Parse the response to extract the access token
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(tokenConnection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
    
                    // Extract the access token from the JSON response
                    // Note: In a real application, you would use a JSON parsing library like Gson
                    accessToken = response.toString().split("\"access_token\":\"")[1].split("\"")[0];
                    System.out.println("Access Token: " + accessToken);
                }
            } else {
                System.out.println("Token Exchange Error: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Token Exchange Exception: " + e.getMessage());
        }
    }
    
    private String searchSpotify(String query) {
        try {
            // URL encode the search query
            String encodedQuery = URLEncoder.encode(query, "UTF-8");

            // Build the search API URL
            String searchUrl = SPOTIFY_API_SEARCH_URL + "?q=" + encodedQuery + "&type=track"; // You can change type to 'album' or 'artist' based on your needs

            // Make a request to the Spotify API search endpoint using the access token
            URL apiUrl = new URL(searchUrl);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                    return response.toString();
                }
            } else {
                System.out.println("Error performing Spotify search: " + responseCode);
                return "Error: " + responseCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private void startHttpServer() {
        int port = 8080; // Choose a port for your server

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new AuthHandler());
            server.setExecutor(null); // creates a default executor
            server.start();

            System.out.println("Server is running on port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            URI requestUri = t.getRequestURI();
            String query = requestUri.getQuery();
            String response = "Authentication code not found.";
    
            System.out.println("Received query: " + query);  // Debug print
    
            if (query != null && query.contains("code=")) {
                authorizationCode = query.split("code=")[1];
                System.out.println("Authorization code received: " + authorizationCode);  // Debug print
                response = "Authentication code received successfully. You can close this window.";
                exchangeCodeForToken();
            }
    
            t.sendResponseHeaders(200, response.length());
            try (OutputStream os = t.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    
    private void openBrowserTab(String url) {
        HostServices hostServices = getHostServices();
        hostServices.showDocument(url);
    }
}
