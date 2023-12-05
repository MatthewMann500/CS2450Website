import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.text.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.json.JSONObject;
import org.json.JSONArray;
import javafx.scene.image.*;
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
import java.nio.charset.StandardCharsets;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;


import java.io.InputStream;
public class website extends Application {

    private static final String CLIENT_ID = "d8738858dcb440c1862f5a00da61a945";
    private static final String CLIENT_SECRET = "014b8129663840d2a74254865dd328f5";
    private static final String REDIRECT_URI = "http://localhost:8080";
    private static final String AUTH_URL = "https://accounts.spotify.com/authorize" +
            "?client_id=" + CLIENT_ID +
            "&response_type=code" +
            "&redirect_uri=" + REDIRECT_URI +
            "&scope=user-modify-playback-state user-read-playback-state user-top-read";
    private static final String TOKEN_EXCHANGE_URL = "https://accounts.spotify.com/api/token";

    private static String authorizationCode;
    private static String accessToken;
    private static TokenExchangeTask tokenExchangeTask;
    private static String playbackAccessToken;
    private static JSONArray devicesArray;


private static String activatedDeviceId;
    private static final String SPOTIFY_AVAILABLE_DEVICES_API_URL = "https://api.spotify.com/v1/me/player/devices";

    private static final String SPOTIFY_API_SEARCH_URL = "https://api.spotify.com/v1/search";
    private static final String SPOTIFY_PLAYBACK_SCOPE = "user-modify-playback-state";
    private static final String SPOTIFY_PLAYBACK_API_URL = "https://api.spotify.com/v1/me/player/play";
    
    private ImageView[] imageViews = new ImageView[18];
    private Image[] images = new Image[18];

    String trackUrl = "";
    private WebView webView = new WebView();
    private WebEngine webEngine;
    Label[] labels = new Label[18];

    Label topTrackLabel = new Label("Top Track");
    Label topArtistLabel = new Label("Top Artist");
    Label topAlbumLabel = new Label("Top Album");

    private HBox trackBox = new HBox(100);
    private HBox artistBox = new HBox(100);
    private HBox albumnBox = new HBox(100);
    private HBox topBox = new HBox(300);
    private HBox topLabelBox = new HBox(350, topTrackLabel,topArtistLabel,topAlbumLabel);

    private VBox topVBox = new VBox(10,topLabelBox,topBox);
    VBox layout = new VBox(10);

    public static void main(String[] args) {
        launch(args);
    }
    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    int width = gd.getDisplayMode().getWidth();
    int height = gd.getDisplayMode().getHeight();
    @Override
    public void start(Stage primaryStage) {
        webEngine = webView.getEngine();
        

        tokenExchangeTask = new TokenExchangeTask();
        Screen primaryScreen = Screen.getPrimary();
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36 Edge/17.17134";
        webEngine.setUserAgent(userAgent);
        Button playButton = new Button("Play");
        playButton.setOnAction(e -> {
            
        });
        Button stopButton = new Button("Stop");

        Rectangle2D bounds = primaryScreen.getBounds();

        double screenWidth = bounds.getWidth();
        double screenHeight = bounds.getHeight();
        primaryStage.setTitle("Music Website");
        System.out.println(AUTH_URL);
        
        topBox.setAlignment(Pos.CENTER);
        artistBox.setAlignment(Pos.CENTER);
        albumnBox.setAlignment(Pos.CENTER);
        trackBox.setAlignment(Pos.CENTER);
        topLabelBox.setAlignment(Pos.CENTER);

        new Thread(this::startHttpServer).start();

        Button openBrowserButton = new Button("Open Authentication URL"); // to be moved to the popup
        openBrowserButton.setOnAction(e -> openBrowserTab(AUTH_URL));

        TextField searchField = new TextField(); // search bar 
        Button searchButton = new Button("Search"); // needst o be intergrated into search bar
        searchField.setPromptText("Search...");
        searchField.getStyleClass().add("search-field");
        searchButton.getStyleClass().add("search-button");
        
        Menu homeMenu = new Menu("Home");
        Menu libraryMenu = new Menu("Library");
            MenuItem playlistItem = new MenuItem("Playlists");
            MenuItem albumnItem = new MenuItem("Albumns");
        libraryMenu.getItems().addAll(playlistItem, albumnItem);
        Menu accountMenu = new Menu("Account");
            MenuItem settingsItem = new MenuItem("Settings");
            MenuItem logoutItem = new MenuItem("Logout");
        accountMenu.getItems().addAll(settingsItem, logoutItem);
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(homeMenu, libraryMenu, accountMenu);

        ScrollPane s1 = new ScrollPane();
        BorderPane b1 = new BorderPane();
        Rectangle rect = new Rectangle(100,100);
        Text text = new Text("THis is a test");
        Rectangle background = new Rectangle(screenWidth, screenHeight, Color.rgb(0, 0, 0, 0.5));
        StackPane stack = new StackPane(rect,text);

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(5), rect);
        fadeTransition.setFromValue(0.0); 
        fadeTransition.setToValue(1.0);
        fadeTransition.play();

        
        StackPane root = new StackPane(b1,background,stack);
        s1.setFitToWidth(true);
        s1.setFitToHeight(true);
        b1.setCenter(s1);
        s1.setContent(b1);
        Label title = new Label("title of website");
        VBox layout = new VBox(10);
        HBox search = new HBox();
        search.setAlignment(Pos.CENTER);
        searchField.setPrefWidth(screenWidth/2);
        search.getChildren().addAll(searchField,searchButton);
        layout.getChildren().addAll(menuBar,title,search,playButton,stopButton);
        s1.setContent(layout);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
        stack.setOnMouseClicked(event -> removeTopTwoChildren(root));
        searchButton.setOnAction(e -> {
            String query = searchField.getText();
            String searchResults = searchSpotify(query,5);
            processSpotifySearchResults(searchResults,1);
            layout.getChildren().remove(title);
            layout.getChildren().addAll(topVBox,trackBox,albumnBox,artistBox);
        });
    }

    // private static void exchangeCodeForToken() {
    //     try {
    //         // Construct the token exchange request body
    //         String requestBody = "grant_type=authorization_code" +
    //                 "&code=" + URLEncoder.encode(authorizationCode, "UTF-8") +
    //                 "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8") +
    //                 "&client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8") +
    //                 "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8");
    
    //         // Make a POST request to the token exchange URL
    //         URL tokenUrl = new URL(TOKEN_EXCHANGE_URL);
    //         HttpURLConnection tokenConnection = (HttpURLConnection) tokenUrl.openConnection();
    //         tokenConnection.setRequestMethod("POST");
    //         tokenConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    //         tokenConnection.setDoOutput(true);
    
    //         // Write the request body to the connection
    //         try (OutputStream os = tokenConnection.getOutputStream()) {
    //             byte[] input = requestBody.getBytes("utf-8");
    //             os.write(input, 0, input.length);
    //         }
    
    //         int responseCode = tokenConnection.getResponseCode();
    //         if (responseCode == HttpURLConnection.HTTP_OK) {
    //             // Parse the response to extract the access token
    //             try (BufferedReader reader = new BufferedReader(new InputStreamReader(tokenConnection.getInputStream()))) {
    //                 StringBuilder response = new StringBuilder();
    //                 String line;
    //                 while ((line = reader.readLine()) != null) {
    //                     response.append(line);
    //                 }
    
    //                 // Extract the access token from the JSON response
    //                 // Note: In a real application, you would use a JSON parsing library like Gson
    //                 accessToken = response.toString().split("\"access_token\":\"")[1].split("\"")[0];
    //                 System.out.println("Access Token: " + accessToken);
    //                 tokenExchangeTask.isDone();
    //             }
    //         } else {
    //             System.out.println("Token Exchange Error: " + responseCode);
    //         }
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //         System.out.println("Token Exchange Exception: " + e.getMessage());
    //     }
    // }
    
    private String searchSpotify(String query, int limit) {
        try {
            // URL encode the search query
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
    
            // Build the search API URL with multiple parameters
            String searchUrl = SPOTIFY_API_SEARCH_URL + "?q=" + encodedQuery + "&type=track,artist,album&limit=" + limit;
    
            // Make a request to the Spotify API search endpoint using the access token
            URL apiUrl = new URL(searchUrl);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + playbackAccessToken);
    
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line).append("\n");
                    }
    
                    // Process the search results
                    processSpotifySearchResults(response.toString(), limit);
    
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
    
    private void processSpotifySearchResults(String json, int limit) {
        if(trackBox.getChildren().size() == 5)
        {
            trackBox.getChildren().clear();
            artistBox.getChildren().clear();
            albumnBox.getChildren().clear();
            topBox.getChildren().clear();
        }
        try {
            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(json);
    
            // Get the 'tracks' object from the response
            JSONObject tracksObject = jsonResponse.getJSONObject("tracks");
    
            // Get the 'items' array from the 'tracks' object
            JSONArray tracksArray = tracksObject.getJSONArray("items");
    
            // Process each track
            for (int i = 0; i < Math.min(limit, tracksArray.length()); i++) {
                JSONObject track = tracksArray.getJSONObject(i);
                String trackName = track.getString("name");
                
                JSONObject externalUrls = track.getJSONObject("external_urls");
                trackUrl = externalUrls.getString("spotify");
                // Get the track album object
                JSONObject albumObject = track.getJSONObject("album");
    
                // Get the album images array
                JSONArray albumImages = albumObject.getJSONArray("images");
    
                // Check if there are images and retrieve the URL of the first image (300x300)
                if (albumImages.length() > 0) {
                    String trackImageURL = albumImages.getJSONObject(0).getString("url");
                    images[i] = new Image(trackImageURL);
                    imageViews[i] = new ImageView(images[i]);
                    imageViews[i].setFitWidth(100);  // Adjust width as needed
                    imageViews[i].setFitHeight(100);
                    if(topBox.getChildren().size() == 0)
                    {
                        topBox.getChildren().add(imageViews[i]);
                    }
                    else
                        trackBox.getChildren().add(imageViews[i]);
                    System.out.println("Track: " + trackName);
                    System.out.println("Track Image URL: " + trackImageURL);
                    System.out.println("Track Image URL: " + trackUrl);
                }
            }
    
            // Get the 'artists' object from the response
            JSONObject artistsObject = jsonResponse.getJSONObject("artists");
    
            // Get the 'items' array from the 'artists' object
            JSONArray artistsArray = artistsObject.getJSONArray("items");
    
            // Process each artist
            for (int i = 0; i < Math.min(limit, artistsArray.length()); i++) {
                JSONObject artist = artistsArray.getJSONObject(i);
                String artistName = artist.getString("name");
                
                // Get the artist images array
                JSONArray artistImages = artist.getJSONArray("images");
                // Check if there are images and retrieve the URL of the first image (300x300)
                if (artistImages.length() > 0) {
                    String artistImageURL = artistImages.getJSONObject(0).getString("url");
                    images[i+5] = new Image(artistImageURL);
                    imageViews[i+5] = new ImageView(images[i+5]);
                    imageViews[i+5].setFitWidth(100);  
                    imageViews[i+5].setFitHeight(100);
                    if(topBox.getChildren().size() == 1)
                    {
                        topBox.getChildren().add(imageViews[i+5]);
                    }
                    else
                        artistBox.getChildren().add(imageViews[i+5]);
                    System.out.println("Artist: " + artistName);
                    System.out.println("Artist Image URL: " + artistImageURL);
                }
            }
    
            // Get the 'albums' object from the response
            JSONObject albumsObject = jsonResponse.getJSONObject("albums");
    
            // Get the 'items' array from the 'albums' object
            JSONArray albumsArray = albumsObject.getJSONArray("items");
    
            // Process each album
            for (int i = 0; i < Math.min(limit, albumsArray.length()); i++) {
                JSONObject album = albumsArray.getJSONObject(i);
                String albumName = album.getString("name");
    
                // Get the album images array
                JSONArray albumImages = album.getJSONArray("images");
    
                // Check if there are images and retrieve the URL of the first image (300x300)
                if (albumImages.length() > 0) {
                    String albumImageURL = albumImages.getJSONObject(0).getString("url");
                    images[i+10] = new Image(albumImageURL);
                    imageViews[i+10] = new ImageView(images[i+10]);
                    imageViews[i+10].setFitWidth(100);  
                    imageViews[i+10].setFitHeight(100);
                    if(topBox.getChildren().size() == 2)
                    {
                        topBox.getChildren().add(imageViews[i+10]);
                    }
                    else
                        albumnBox.getChildren().add(imageViews[i+10]);
                    System.out.println("Album: " + albumName);
                    System.out.println("Album Image URL: " + albumImageURL);
                }
            }
    
        } catch (Exception e) {
            e.printStackTrace();
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

    try {
        if (query != null && query.contains("code=")) {
            authorizationCode = query.split("code=")[1];
            System.out.println("Authorization code received: " + authorizationCode);  // Debug print
            new Thread(tokenExchangeTask).start();
            response = "Authentication code received successfully. You can close this window.";
        }
    } catch (Exception e) {
        e.printStackTrace();
        response = "Error processing authentication code.";
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

    private void removeTopTwoChildren(StackPane stackPane) {
        tokenExchangeTask.setOnSucceeded(event -> {
            stackPane.getChildren().remove(stackPane.getChildren().size() - 1);
            stackPane.getChildren().remove(stackPane.getChildren().size() - 1);
        });
        openBrowserTab(AUTH_URL);
        
    }
    
    public class TokenExchangeTask extends Task<Void> {
    @Override
    protected Void call() throws Exception {
        exchangeCodeForPlaybackToken();
        playSpotifyTrack("spotify:track:1BxfuPKGuaTgP7aM0Bbdwr");
        return null;
    }
    }
    
    private void openSpotifyTrack() {
        try{
        webEngine = webView.getEngine(); // Initialize the WebEngine
        webEngine.load(getClass().getResource("spotify.html").toExternalForm());
        topVBox.getChildren().add(webView);
        System.out.println(accessToken);
        
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading content into WebView: " + e.getMessage());
        }
    }

    private static void exchangeCodeForPlaybackToken() {
        try {
            // Construct the token exchange request body for playback access token
            String requestBody = "grant_type=authorization_code" +
                    "&code=" + URLEncoder.encode(authorizationCode, "UTF-8") +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8") +
                    "&client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8") +
                    "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8") +
                    "&scope=" + SPOTIFY_PLAYBACK_SCOPE;
    
            // Make a POST request to the token exchange URL for playback access token
            URL tokenUrl = new URL(TOKEN_EXCHANGE_URL);
            HttpURLConnection tokenConnection = (HttpURLConnection) tokenUrl.openConnection();
            tokenConnection.setRequestMethod("POST");
            tokenConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            tokenConnection.setDoOutput(true);
    
            // Write the request body to the connection
            try (OutputStream os = tokenConnection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
    
            int responseCode = tokenConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Parse the response to extract the playback access token
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(tokenConnection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
    
                    // Extract the playback access token from the JSON response
                    // Note: In a real application, you would use a JSON parsing library like Gson
                    playbackAccessToken = response.toString().split("\"access_token\":\"")[1].split("\"")[0];
                    System.out.println("Playback Access Token: " + playbackAccessToken);
                    tokenExchangeTask.isDone();
                }
            } else {
                System.out.println("Playback Token Exchange Error: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Playback Token Exchange Exception: " + e.getMessage());
        }
    }

    private  void playSpotifyTrack(String trackUri) {
        try {
            getAvailableDevices();
            String deviceId = getActivatedDeviceId();
            System.out.println("Playback Access Token: " + playbackAccessToken);
            String jsonInputString = "{\"uris\": [\"" + trackUri + "\"], \"device_id\": \"" + deviceId + "\"}";
            System.out.println("Request URL: " + "https://api.spotify.com/v1/me/player/play");
System.out.println("Request Headers: Authorization: Bearer " + playbackAccessToken);
System.out.println("Request Body: " + jsonInputString);
            // Create a connection to the Spotify playback API
            URL apiUrl = new URL("https://api.spotify.com/v1/me/player/play");
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Authorization", "Bearer " + playbackAccessToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
    
            // Write the JSON payload to the connection
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
    
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                System.out.println("Playback started successfully.");

        
            } else {
                System.out.println("Error starting playback: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Playback API Exception: " + e.getMessage());
        }
    }

    private static void getAvailableDevices() {
        try {
            URL apiUrl = new URL(SPOTIFY_AVAILABLE_DEVICES_API_URL);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + playbackAccessToken);
    
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
    
                    // Parse the JSON response to extract the list of devices
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    devicesArray = jsonResponse.getJSONArray("devices");
    
                    // Print the devices for debugging (remove in production)
                    System.out.println("Available Devices: " + devicesArray);
    
                    // Activate the desired device (e.g., the first device in the list)
                    if (devicesArray.length() > 0) {
                        String deviceIdToActivate = devicesArray.getJSONObject(0).getString("id");
                        activateDevice(deviceIdToActivate);
                    
                        // Set the activated device ID
                        activatedDeviceId = deviceIdToActivate;
                    }
                }
            } else {
                System.out.println("Error getting available devices: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Exception getting available devices: " + e.getMessage());
        }
    }
    
    // Method to retrieve the activated device ID
    private static String getActivatedDeviceId() {
        return activatedDeviceId;
    }
    private static void activateDevice(String deviceId) {
        try {
            // Build the URL for activating the device
            String activateDeviceUrl = "https://api.spotify.com/v1/me/player";
            URL apiUrl = new URL(activateDeviceUrl);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Authorization", "Bearer " + playbackAccessToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
    
            // Write the JSON payload to the connection
            try (OutputStream os = connection.getOutputStream()) {
                String jsonInputString = "{\"device_ids\": [\"" + deviceId + "\"], \"play\": true}";
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
    
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                System.out.println("Device activation successful.");
            } else {
                System.out.println("Error activating device: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Exception activating device: " + e.getMessage());
        }
    }
    
    private void initializeSpotifySDK() {

        String sdkScript = "https://sdk.scdn.co/spotify-player.js";
        String script = """
                var script = document.createElement('script');
                script.src = '%s';
                script.onload = function() {
                    console.log('Spotify SDK loaded');
                    initializeSpotify('%s'); // Pass playbackAccessToken to the initialization function
                };
                document.head.appendChild(script);
                
                function initializeSpotify(playbackAccessToken) {
                    // Your Spotify Web Playback SDK initialization code here
                    var player = new Spotify.Player({
                        name: 'Your Player Name',
                        getOAuthToken: function (callback) {
                            callback(playbackAccessToken);
                        }
                    });

                    // Add event listeners and perform other SDK setup

                    // Connect to the player
                    player.connect();
                }
                """.formatted(sdkScript, playbackAccessToken);

        // Load the HTML content containing the SDK initialization script
        String htmlContent = """
                <html>
                <head>
                    <title>Spotify SDK Initialization</title>
                </head>
                <body>
                    <script>
                        %s
                    </script>
                </body>
                </html>
                """.formatted(script);

        webEngine.loadContent(htmlContent);
    }
}

