import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class Controller implements Initializable {

    @FXML
    private Pane pane;

    @FXML
    private Label songLabel;

    @FXML
    private Button playButton;

    @FXML
    private ComboBox<String> speedBox;

    @FXML
    private Slider volumeSlider;

    @FXML
    private ProgressBar songProgressBar;

    private Media media;
    private MediaPlayer mediaPlayer;

    private File directory;
    private File[] files;

    private ArrayList<File> songs;

    private int songNumber;
    private int[] speeds = {25, 50, 75, 100, 125, 150, 175, 200};

    private Timer timer;
    private TimerTask task;
    private boolean running;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        //setup music directory
        songs = new ArrayList<File>();
        directory = new File("music");
        files = directory.listFiles(); //get the files from the directory

        if (files != null)
        {
            for(File file : files)
            {
                songs.add(file);
                //System.out.println(file); //prints out each music name in the folder 
            }
        }

        //create media and its player
        media = new Media(songs.get(songNumber).toURI().toString()); //song number will start as 0 (first song)
        mediaPlayer = new MediaPlayer(media);

        //set label to current song name
        songLabel.setText(songs.get(songNumber).getName());

        for (int i = 0; i < speeds.length; i++)
        {
            speedBox.getItems().add(Integer.toString(speeds[i]) + "%"); //add speeds to the combobox
        }

        //onAction here since it can't be added 
        speedBox.setOnAction(this::changeSpeed);
        
        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
                mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
            }
        });

        songProgressBar.setStyle("-fx-accent: yellow;"); //green progress bar
    }
    
    public void playMedia()
    {
        beginTimer();
        mediaPlayer.setVolume(volumeSlider.getValue() * 0.01); //change volume to whichever is set by the slider
        changeSpeed(null); //in case you change the speed before playing
        mediaPlayer.play();
    }

    public void pauseMedia()
    {
        cancelTimer();
        mediaPlayer.pause();
    }

    public void resetMedia()
    {
        songProgressBar.setProgress(0);
        mediaPlayer.seek(Duration.seconds(0));
    }

    //add playMedia() in the end to play the song auto
    public void previousMedia()
    {
        if (songNumber == 0)
        {
            songNumber = songs.size() - 1;

            mediaPlayer.stop();

            if (running)
            {
                cancelTimer();
            }

            media = new Media(songs.get(songNumber).toURI().toString()); //song number will start as 0 (first song)
            mediaPlayer = new MediaPlayer(media);

            songLabel.setText(songs.get(songNumber).getName());

        } else {
            songNumber--;

            mediaPlayer.stop();

            if (running)
            {
                cancelTimer();
            }

            media = new Media(songs.get(songNumber).toURI().toString()); //song number will start as 0 (first song)
            mediaPlayer = new MediaPlayer(media);

            songLabel.setText(songs.get(songNumber).getName());
        }
    }

    public void nextMedia()
    {
        if(songNumber < songs.size() - 1)
        {
            songNumber++;

            mediaPlayer.stop();

            media = new Media(songs.get(songNumber).toURI().toString()); //song number will start as 0 (first song)
            mediaPlayer = new MediaPlayer(media);

            songLabel.setText(songs.get(songNumber).getName());
        } else { //if not greater then it's the last song, so start over, hence songNumber = 0
            songNumber = 0;

            mediaPlayer.stop();

            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            songLabel.setText(songs.get(songNumber).getName());
        }
    }

    public void changeSpeed(ActionEvent event)
    {
        if (speedBox.getValue() == null) //because this value is called upon clicking "play" and the user might've not set anything prior to playing
        {
            mediaPlayer.setRate(1);
        } else
        {
            mediaPlayer.setRate(Integer.parseInt(speedBox.getValue().substring(0, speedBox.getValue().length() - 1)) * 0.01);
        }

    }

    public void beginTimer()
    {
        timer = new Timer();

        task = new TimerTask() {
            public void run()
            {
                running = true;
                double current = mediaPlayer.getCurrentTime().toSeconds();
                double end = media.getDuration().toSeconds();
                //System.out.println(current/end);
                songProgressBar.setProgress(current/end);

                if(current/end == 1)
                {
                    cancelTimer();
                }
            }
        };

        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    public void cancelTimer()
    {
        running = false;
        timer.cancel();
    }
}
