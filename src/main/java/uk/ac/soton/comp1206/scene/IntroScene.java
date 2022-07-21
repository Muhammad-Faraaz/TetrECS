package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class IntroScene extends BaseScene{
    private ImageView image;
    SequentialTransition sequence;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public IntroScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    @Override
    public void initialise() {
        scene.setOnKeyPressed(e -> {gameWindow.startMenu();
            sequence.stop();});
    }

    /**
     * Takes the image and applies a rotation animation
     */
    @Override
    public void build() {
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("intro");
        root.getChildren().add(menuPane);

        image = new ImageView(new Image(this.getClass().getResource("/images/ECSGames.png").toExternalForm()));
        image.setPreserveRatio(true);
        image.setFitWidth(400);
        menuPane.getChildren().add(image);

        RotateTransition rt = new RotateTransition();
        rt.setDuration(Duration.seconds(2));
        rt.setNode(image);
        rt.setFromAngle(6.0);
        rt.setToAngle(-6.0);

        sequence = new SequentialTransition(new Animation[] { (Animation)rt });
        sequence.play();
        sequence.setOnFinished(e -> {
            this.gameWindow.startMenu();
        });

    }


}
