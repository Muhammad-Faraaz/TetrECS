package uk.ac.soton.comp1206.game;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;


public class MultiMedia {
    public static Logger logger = LogManager.getLogger(MultiMedia.class);
    public static MediaPlayer audioPlayer;
    public static MediaPlayer musicPlayer;
    public static boolean start = false;


    /**
     * Plays the audio files.
     * @param audio
     */
    public static void playAudio(String audio){
        //Media takes the source of the file to be played
        //to External form converts URL link to String
        Media audioPath = new Media(MultiMedia.class.getResource("/sounds/" + audio).toExternalForm());
        //Media player contains the controls to play the media
        audioPlayer = new MediaPlayer(audioPath);
        start = true;
        audioPlayer.setVolume(0.15);
        audioPlayer.play();
        logger.info("Playing audio" + audio);
    }

    /**
     * plays the music files.
     * Initially the music is set to false unless it is able to find a music path.
     * @param music
     */
    public static void playMusic(String music){
        if(start) musicPlayer.stop();
        try{
        Media musicPath = new Media(MultiMedia.class.getResource("/music/" + music).toExternalForm());
        musicPlayer = new MediaPlayer(musicPath);
        start = true;
        musicPlayer.setVolume(0.2);
        musicPlayer.play();
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        logger.info("Playing music" + music);
        } catch (Exception E) {
            logger.info("Error");
            start = false;
        }
    }
}
