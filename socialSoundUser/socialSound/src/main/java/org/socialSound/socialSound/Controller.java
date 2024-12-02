
package org.socialSound.socialSound;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class Controller implements Initializable {

    @FXML
    private Stage mainWindow;

    @FXML
    private Label songLabel;

    @FXML
    private Button playPause, prevButton, nextButton, searchButton;

    @FXML
    private Slider volumeSlider;

    @FXML
    private ProgressBar songProgressBar;

    @FXML
    private TextField searchBar;
    
    @FXML
    private ListView<String> playListView;
   
    private List<Song> songs;
    
    private boolean isInPlaylistView = true; // Tracks whether we're in the playlist view
    
    private PlaylistService playlistService;

    private Timer timer;
    private TimerTask task;
    private boolean running;
    private int songNumber = 0;
    private MediaPlayer mediaPlayer;
    private Media media;
    
    String[] playList = {"Liked Songs", "Rap", "Pop", "Rock", "R&B", "Jazz"};

    public void setMainWindow(Stage stage) {
        this.mainWindow = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        MongoDatabase db = App.getDatabase(); // Ensure database is initialized correctly
        if (db == null) {
            System.out.println("Error: Database connection is null.");
            return; // Exit early if connection is not established
        }

        MongoCollection<Document> songCollection = db.getCollection("songs");
        playlistService = new PlaylistService(songCollection);
        songs = new ArrayList<>(); // Start with an empty song list
        songLabel.setText("Search or select a playlist to view songs."); // Update label for clarity
        songProgressBar.setStyle("-fx-accent: #e0b0ff; -fx-control-inner-background: #000000; -fx-border-color: #000000");
        enableScrubbing();
        volumeSlider.setValue(50); // Default volume

        // Initialize playlist options in playListView
        playListView.getItems().clear();
        playListView.getItems().addAll(playList);

        // Handle clicks on the playListView
        playListView.setOnMouseClicked(event -> handleListViewClick());

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue() / 100.0);
            }
        });
    }

    @FXML
    public void create(ActionEvent event) {
        // Implementation of the create action
        System.out.println("Create button clicked!");
    }

    // Search for songs in MongoDB by title or artist
    @FXML
    private void searchSongs() {
        String query = searchBar.getText().trim().toLowerCase();

        songs = searchSongsInDatabase(query);

        if (songs.isEmpty()) {
        	songLabel.setText("No songs found.");
        } else {
            songLabel.setText(songs.get(0).getTitle());
            songNumber = 0;
            playSong(songNumber);
        }
    }
    
    @FXML
    private void goBack() {
        if (!isInPlaylistView) {
            playListView.getItems().clear();
            playListView.getItems().addAll(playList); // Restore playlist options
            songLabel.setText("Search or select a playlist to view songs."); // Reset label
            isInPlaylistView = true; // Switch back to playlist view
        }
    }
    
    @FXML
    private void goForward() {
        if (!isInPlaylistView && songs != null && !songs.isEmpty()) {
            if (songNumber < songs.size() - 1) {
                songNumber++;
            } else {
                songNumber = 0; // Loop back to the first song
            }
            playSong(songNumber);
        }
    }


    private List<Song> searchSongsInDatabase(String query) {
        List<Song> searchResults = new ArrayList<>();
        try {
            MongoCollection<Document> collection = App.getDatabase().getCollection("songs");
            var filter = new Document("$or", List.of(
                new Document("title", new Document("$regex", query).append("$options", "i")),
                new Document("artist", new Document("$regex", query).append("$options", "i"))
            ));

            try (MongoCursor<Document> cursor = collection.find(filter).iterator()) {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    String title = doc.getString("title");
                    String artist = doc.getString("artist");
                    String googleDriveLink = doc.getString("googleDriveLink");
                    List<String> tags = (List<String>) doc.get("tags");

                    searchResults.add(new Song(title, artist, googleDriveLink, tags));
                }
            }
        } catch (Exception e) {
            System.out.println("Error searching songs in database: " + e.getMessage());
        }
        return searchResults;
    }

    private void playSong(int songIndex) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            cancelTimer();
        }

        Song song = songs.get(songIndex);
        media = new Media(song.getGoogleDriveLink());

        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);

        Platform.runLater(() -> songLabel.setText(song.getTitle() + " - " + song.getArtist()));

        mediaPlayer.setOnError(() -> {
            System.out.println("MediaPlayer error: " + mediaPlayer.getError().getMessage());
        });

        mediaPlayer.setOnReady(() -> {
            playPause.setText("⏸");
            mediaPlayer.play();
            beginTimer();
        });

        mediaPlayer.setOnEndOfMedia(this::nextMedia);
    }

    public void playPauseMedia() {
        if (mediaPlayer == null) {
            System.out.println("MediaPlayer is not initialized.");
            return;
        }

        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playPause.setText("▶");
        } else {
            mediaPlayer.play();
            if (timer == null || !running) {
                beginTimer();
            }
            playPause.setText("⏸");
        }
    }

    public void prevMedia() {
        
        if (mediaPlayer.getCurrentTime().toSeconds() > 2) {
            mediaPlayer.seek(javafx.util.Duration.ZERO);
        } else {
            if (songNumber > 0) {
                songNumber--;
            } else {
                songNumber = songs.size() - 1;
            }
            playSong(songNumber);
        }
    }

    public void nextMedia() {
        
        if (songNumber < songs.size() - 1) {
            songNumber++;
        } else {
            songNumber = 0;
        }
        playSong(songNumber);
    }

    public void beginTimer() {
        timer = new Timer();

        task = new TimerTask() {
            public void run() {
                running = true;
                double current = mediaPlayer.getCurrentTime().toSeconds();
                double end = media.getDuration().toSeconds();

                songProgressBar.setProgress(current / end);

                if (current / end == 1) {
                    cancelTimer();
                }
            }
        };

        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    public void cancelTimer() {
        if (timer != null) {
            running = false;
            timer.cancel();
            timer = null;
        }
    }

    private void enableScrubbing() {
        songProgressBar.setOnMousePressed(event -> {
            if (mediaPlayer != null) {
                double mouseX = event.getX();
                double progressBarWidth = songProgressBar.getWidth();
                double progress = mouseX / progressBarWidth;

                mediaPlayer.seek(javafx.util.Duration.seconds(progress * mediaPlayer.getTotalDuration().toSeconds()));
            }
        });

        songProgressBar.setOnMouseDragged(event -> {
            if (mediaPlayer != null) {
                double mouseX = event.getX();
                double progressBarWidth = songProgressBar.getWidth();
                double progress = mouseX / progressBarWidth;

                mediaPlayer.seek(javafx.util.Duration.seconds(progress * mediaPlayer.getTotalDuration().toSeconds()));
            }
        });
    }
    
    public void addHoverEffect(MouseEvent event) {
        Button button = (Button) event.getSource();    
        button.setStyle("-fx-text-fill: #9574ab; -fx-background-color: #000000;");
    }
    public void removeHoverEffect(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #000000;");
    }
    
    private void createPlaylistByTag(String tag) {
        List<Song> songsByTag = playlistService.getSongsByTag(tag);

        Platform.runLater(() -> {
            playListView.getItems().clear();
            if (songsByTag.isEmpty()) {
                playListView.getItems().add("No songs found for tag: " + tag);
            } else {
                for (Song song : songsByTag) {
                    playListView.getItems().add(song.getTitle() + " - " + song.getArtist());
                }
                this.songs = songsByTag;
                songLabel.setText("Songs in " + tag + " playlist"); // Update label
            }
            isInPlaylistView = false; // Switch to song view
        });
    }

    
    private void handleListViewClick() {
        String selectedItem = playListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        if (isInPlaylistView) {
            // User clicked on a playlist
            createPlaylistByTag(selectedItem);
        } else {
            // User clicked on a song
            for (int i = 0; i < songs.size(); i++) {
                Song song = songs.get(i);
                if ((song.getTitle() + " - " + song.getArtist()).equals(selectedItem)) {
                    songNumber = i;
                    playSong(songNumber);
                    break;
                }
            }
        }
    }
}