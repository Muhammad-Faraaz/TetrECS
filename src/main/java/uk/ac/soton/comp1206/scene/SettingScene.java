package uk.ac.soton.comp1206.scene;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.game.MultiMedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class SettingScene extends BaseScene {
    public Slider slider;

    /**
     * Create a new Setting scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public SettingScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    @Override
    public void initialise() {
        slider.setValue(MultiMedia.musicPlayer.getVolume() * 100);
        slider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                MultiMedia.musicPlayer.setVolume(slider.getValue() / 100);
            }
        });
    }

    /**
     * build method which installs a volume slider to adjust music volume (this is done for the extension)
     */
    @Override
    public void build() {
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var settingPane = new BorderPane();
        settingPane.setMaxHeight(gameWindow.getHeight());
        settingPane.setMaxWidth(gameWindow.getWidth());
        settingPane.getStyleClass().add("menu-background");
        root.getChildren().add(settingPane);

        var vBox = new VBox();
        vBox.setSpacing(30);
        vBox.setAlignment(Pos.CENTER);
        settingPane.setCenter(vBox);

        //Title for settings
        var settingsTitle = new Label("Settings");
        settingsTitle.getStyleClass().add("title");
        vBox.getChildren().add(settingsTitle);

        var soundBox = new VBox();
        soundBox.setAlignment(Pos.CENTER);
        soundBox.setSpacing(10);
        vBox.getChildren().add(soundBox);


        var volumeTitle = new Text("Sound Volume:");
        volumeTitle.getStyleClass().add("menuItem");
        soundBox.getChildren().add(volumeTitle);

        slider = new Slider();
        slider.setMaxWidth(gameWindow.getWidth()/3);
        soundBox.getChildren().add(slider);

    }
}
