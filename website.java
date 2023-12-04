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
import javafx.animation.FadeTransition;
import javafx.scene.text.*;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.json.JSONObject;
import org.json.JSONArray;
import javafx.scene.image.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;

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

import javazoom.*;
import javazoom.jl.decoder.JavaLayerErrors;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.InputStream;
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

    private AdvancedPlayer player;

    private static String authorizationCode;
    private static String accessToken;
    private static TokenExchangeTask tokenExchangeTask;

    private static final String SPOTIFY_API_SEARCH_URL = "https://api.spotify.com/v1/search";
    
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

    public static void main(String[] args) {
        launch(args);
    }
    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    int width = gd.getDisplayMode().getWidth();
    int height = gd.getDisplayMode().getHeight();
    @Override
    public void start(Stage primaryStage) {
        tokenExchangeTask = new TokenExchangeTask();
        Screen primaryScreen = Screen.getPrimary();

        Button playButton = new Button("Play");
        playButton.setOnAction(e -> {openSpotifyTrack();});
        Button stopButton = new Button("Stop");
        webEngine = webView.getEngine();

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

        new Thread(() -> startHttpServer()).start();

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
                    tokenExchangeTask.isDone();
                }
            } else {
                System.out.println("Token Exchange Error: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Token Exchange Exception: " + e.getMessage());
        }
    }
    
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
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
    
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
            exchangeCodeForToken();
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
        exchangeCodeForToken();
        return null;
    }
    }
    
    private void openSpotifyTrack() {
        try{
        webEngine = webView.getEngine(); // Initialize the WebEngine


        StringBuilder scriptBuilder = new StringBuilder();

scriptBuilder.append("window.onSpotifyWebPlaybackSDKReady = () => {");
scriptBuilder.append("console.log('Spotify SDK is ready.');");
scriptBuilder.append("const player = new Spotify.Player({");
scriptBuilder.append("name: 'Your App Name',");
scriptBuilder.append("getOAuthToken: cb => { cb('"+ accessToken +"'); },"); // Use the injected token
scriptBuilder.append("});");
scriptBuilder.append("player.addListener('ready', ({ device_id }) => {");
scriptBuilder.append("console.log('Spotify player is ready.');");
scriptBuilder.append("console.log('Device ID', device_id);");
// Add the code to play a specific song
scriptBuilder.append("player.play({ uris: ['https://open.spotify.com/track/3GCL1PydwsLodcpv0Ll1ch?si=b6e47c84a75547cd&nd=1&dlsi=ead3cd89d5c44742'] });"); // Replace TRACK_URI with the actual URI of the song
scriptBuilder.append("});");
scriptBuilder.append("player.connect();");
scriptBuilder.append("window.spotifyPlayer = player;");
scriptBuilder.append("};");

String script = scriptBuilder.toString();

        // Load HTML content into the WebView
        String htmlContent = "<html><head>" +
        "<script src='https://sdk.scdn.co/spotify-player.js'></script>" +
        "</head><body></body></html>";

        webEngine.loadContent(htmlContent);

        System.out.println(accessToken);
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                // WebView is fully loaded, now inject your JavaScript code
                webEngine.executeScript(script);
            }
        });
        topVBox.getChildren().add(webView);
        
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading content into WebView: " + e.getMessage());
        }
    }

}

