package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.game.MultiMedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.game.MultiMedia;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);
        mainPane.setMaxHeight(gameWindow.getHeight());
        mainPane.setMaxWidth(gameWindow.getWidth());

        var btnBox = new VBox();
        btnBox.setAlignment(Pos.BOTTOM_CENTER);
        btnBox.setSpacing(5);
        mainPane.setBottom(btnBox);

        var title = new Text("TetrECS");
        title.getStyleClass().add("bigtitle");
        mainPane.setCenter(title);

        var play = new Button("Play");
        play.getStyleClass().add("menuItem");
        play.setPrefSize(150, 50);
        animation(1.20, play);
        //Bind the button action to the startGame method in the menu
        play.setOnAction(this::startGame);


        var instructions = new Button("Instructions");
        instructions.getStyleClass().add("menuItem");
        instructions.setPrefSize(150, 50);
        //mainPane.setCenter(instructions);
        animation(1.20, instructions);
        instructions.setOnAction(this::openInstructionPage);

        var exit = new Button("Exit");
        exit.getStyleClass().add("menuItem");
        exit.setPrefSize(150, 50);
        //mainPane.setCenter(exit);
        animation(1.20, exit);
        exit.setOnMouseClicked(event -> endGame());

        var settings = new Button("Settings");
        settings.getStyleClass().add("menuItem");
        settings.setPrefSize(150, 50);
        //mainPane.setCenter(settings);
        animation(1.20, settings);
        settings.setOnAction(this::startSettings);

        var multiplayer = new Button("Multiplayer");
        multiplayer.getStyleClass().add("menuItem");
        multiplayer.setPrefSize(150, 50);
        //mainPane.setCenter(multiplayer);
        animation(1.20, multiplayer);
        multiplayer.setOnAction(this::startLobby);

        btnBox.getChildren().add(play);
        btnBox.getChildren().add(multiplayer);
        btnBox.getChildren().add(settings);
        btnBox.getChildren().add(instructions);
        btnBox.getChildren().add(exit);
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise(){
        MultiMedia.playMusic("lofisound.mp3");
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
    }
    /**
     * Handle when the End Game button is pressed
     */
    public void endGame(){App.getInstance().shutdown();}
    /**
     * Handle when the Instruction button is pressed
     * @param event event
     */
    public void openInstructionPage(ActionEvent event){gameWindow.openInstruction();}

    /**
     * Handle when the Multiplayer button is pressed
     * @param event event
     */
    public void startLobby(ActionEvent event){gameWindow.loadLobbyScene();}

    /**
     * Handle when the settings button is pressed
     * @param event event
     */
    public void startSettings(ActionEvent event){gameWindow.loadSettingScene();}
}
