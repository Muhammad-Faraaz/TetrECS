package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class InstructionsScene extends BaseScene{
    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    @Override
    public void initialise() {

    }

    @Override
    public void build() {
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var pane = new BorderPane();
        pane.setMaxWidth(gameWindow.getWidth());
        pane.setMaxHeight(gameWindow.getHeight());
        pane.getStyleClass().add("menu");

        var vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        pane.setTop(vBox);

        var gameInstructions = new Text("How To Play:");
        gameInstructions.getStyleClass().add("heading");
        vBox.getChildren().add(gameInstructions);

        var instructionPage = new Image(InstructionsScene.class.getResource("/images/Instructions.png").toExternalForm());
        ImageView image = new ImageView(instructionPage);
        image.setFitHeight(300);
        image.setFitWidth(500);
        vBox.getChildren().add(image);


        root.getChildren().add(pane);

        var piecesAvailable = new Text("Pieces available:");

        piecesAvailable.getStyleClass().add("heading");
        vBox.getChildren().add(piecesAvailable);

        var smallGrid = new GridPane();
        smallGrid.setAlignment(Pos.CENTER);
        smallGrid.setVgap(5);
        smallGrid.setHgap(5);
        int counter = 0;
        smallGrid.setAlignment(Pos.BOTTOM_CENTER);
        for(int y = 0; y < 3; y++){
            for(int x = 0; x < 5; x++){
                PieceBoard board = new PieceBoard(3,3,45,45);
                board.displayPiece(GamePiece.createPiece(counter));
                counter++;
                smallGrid.add(board, x, y);
                }
            }
        vBox.getChildren().add(smallGrid);
        }
    }

