package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(MenuScene.class);
  protected Game game;
  protected PieceBoard currentPieceBoard;
  protected PieceBoard smallerNextPieceBoard;
  protected GameBoard board;
  public int y;
  public int x;
  protected Rectangle timeBar;
  protected Timeline timeLine;
  protected StackPane challengePane;
  protected BorderPane mainPane;
  protected boolean sendingMsg;
  protected VBox rightBox;
  protected Text highScoreTitle;
  protected Label highScore;
  protected Text levelTitle;
  protected Text level;


  /**
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public ChallengeScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Challenge Scene");
  }

  /** Build the Challenge window */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    setupGame();

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    challengePane = new StackPane();
    challengePane.setMaxWidth(gameWindow.getWidth());
    challengePane.setMaxHeight(gameWindow.getHeight());
    challengePane.getStyleClass().add("challenge-background");
    root.getChildren().add(challengePane);

    mainPane = new BorderPane();
    challengePane.getChildren().add(mainPane);

    board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
    mainPane.setCenter(board);

    currentPieceBoard = new PieceBoard(3, 3, 100, 100);
    currentPieceBoard.getBlock(1,1).setCentre();
    // if the piece board is clicked the piece rotates
    currentPieceBoard.setOnMouseClicked(event -> {
      if(event.getButton() == MouseButton.PRIMARY) {
        game.getCurrentPiece().rotate();
        currentPieceBoard.displayPiece(game.getCurrentPiece());}});
    smallerNextPieceBoard = new PieceBoard(3, 3, 70, 70);


    // Hbox where all the other UI elements will be stored
    HBox gamePane = new HBox();
    gamePane.setAlignment(Pos.CENTER);
    HBox.setHgrow(gamePane, Priority.ALWAYS);
    gamePane.setSpacing(50);

    VBox gamePaneTwo = new VBox();
    gamePaneTwo.setAlignment(Pos.CENTER_RIGHT);
    gamePaneTwo.setPadding(new Insets(10, 0, 0, 0)); // (top/right/bottom/left)

    // This code refers to the score board
    VBox scoreBox = new VBox();
    scoreBox.setAlignment(Pos.TOP_LEFT);
    scoreBox.setPadding(new Insets(10, 0, 0, 70));

    highScoreTitle = new Text("High Score");
    highScore = new Label(getHighScore());

    highScoreTitle.getStyleClass().add("title");
    highScore.getStyleClass().add("title");

    Text scoreTitle = new Text("Score");
    Text score = new Text();
    score.textProperty().bind(game.getScoreProperty().asString());

    scoreTitle.getStyleClass().add("title");
    score.getStyleClass().add("title");

    scoreBox.getChildren().add(scoreTitle);
    scoreBox.getChildren().add(score);
    gamePane.getChildren().add(scoreBox);

    // This code refers to the lives board
    VBox livesBox = new VBox();
    livesBox.setAlignment(Pos.TOP_RIGHT);
    livesBox.setPadding(new Insets(10, 10, 0, 0));

    Text livesTitle = new Text("Lives");
    Text lives = new Text();
    lives.textProperty().bind(game.getLivesProperty().asString());

    livesTitle.getStyleClass().add("title");
    lives.getStyleClass().add("title");

    livesBox.getChildren().add(livesTitle);
    livesBox.getChildren().add(lives);
    gamePane.getChildren().add(livesBox);

    mainPane.setTop(gamePane);

    // level Box
    rightBox = new VBox();
    rightBox.setSpacing(5);
    mainPane.setRight(rightBox);
    rightBox.setPadding(new Insets(0, 10, 0, 0));

    levelTitle = new Text("Level");
    level = new Text();
    level.textProperty().bind(game.getLevelProperty().asString());

    levelTitle.getStyleClass().add("title");
    level.getStyleClass().add("title");

    rightBox.getChildren().add(levelTitle);
    rightBox.getChildren().add(level);
    gamePaneTwo.getChildren().add(rightBox);

    rightBox.getChildren().add(highScoreTitle);
    rightBox.getChildren().add(highScore);

    Text multiplierTitle = new Text("Multiplier");
    Text multiplier = new Text();
    multiplier.textProperty().bind(game.getMultiplierProperty().asString());

    multiplierTitle.getStyleClass().add("title");
    multiplier.getStyleClass().add("title");

    rightBox.getChildren().add(multiplierTitle);
    rightBox.getChildren().add(multiplier);

    gamePaneTwo.getChildren().addAll(currentPieceBoard, smallerNextPieceBoard);

    mainPane.setRight(gamePaneTwo);

    // Handle block on gameboard grid being clicked
    board.setOnBlockClick(this::blockClicked);
    board.setonRightClick(this::rotateOnce);

    timeBar = new Rectangle(0,10, gameWindow.getWidth(), 20);
    timeBar.fillProperty().set(Color.RED);
    VBox lowerBox = new VBox();
    lowerBox.getChildren().add(timeBar);


    mainPane.setBottom(lowerBox);
  }

  /**
   * Handle when a block is clicked
   *
   * @param gameBlock the Game Block that was clocked
   */
  private void blockClicked(GameBlock gameBlock) {
    game.blockClicked(gameBlock);
  }

  private void rotate(int number) {
    game.rotatePiece(number);
    currentPieceBoard.playPiece(game.getCurrentPiece());
  }
  /** Setup the game object and model */
  public void setupGame() {
    logger.info("Starting a new challenge");
    // Start new game
    game = new Game(5, 5);
  }

  /** Initialise the scene and start the game */
  @Override
  public void initialise() {
    logger.info("Initialising Challenge");
    game.setLineClearedListener(this::lineCleared);
    game.setNextPieceListener(this::nextPiece);
    scene.setOnKeyPressed(this::keyBoard);
    game.setOnGameLoop(this::timerBar);
    game.setLivesListener(() -> {
      logger.info("lives listener");
      game.timer.purge();
      gameWindow.loadScoreScene(game);
    });
    game.setNewScoreListener(this::getHighScore);
    game.start();
  }

  private void nextPiece(GamePiece piece, GamePiece followingPiece) {
    logger.info("next Piece method");
    currentPieceBoard.displayPiece(game.getCurrentPiece());
    smallerNextPieceBoard.displayPiece(game.getFollowingPiece());
    logger.info("next Piece method two");
  }

  /**
   * if the line is cleared the coordinates are obtained and the fade animation is applied.
   * @param blockCoordinates
   */
  public void lineCleared(HashSet<GameBlockCoordinate> blockCoordinates) {
    for (GameBlockCoordinate blockCoordinate : blockCoordinates) {
      this.board.fadeOut(board.getBlock(blockCoordinate.getX(), blockCoordinate.getY()));
    }
  }

  /**
   * method to rotate the piece once
   */
  public void rotateOnce(){
    game.rotatePiece(1);
    currentPieceBoard.playPiece(game.getCurrentPiece());
  }

  /**
   * Method to create animation for the timer
   * Creating a timeline which turns from green to yellow to red as time progresses, to indicate urgency.
   * @param time
   */
  public void timerBar(int time){
    logger.info("timerbar");
    Timeline timeLine = new Timeline();
    // KeyValues
    Color color1 =  Color.LIGHTGREEN;
    Color color2 =  Color.GREENYELLOW;
    Color color3 =  Color.RED;

    timeLine.getKeyFrames().add(new KeyFrame(Duration.ZERO, new KeyValue(timeBar.widthProperty(), gameWindow.getWidth())));
    timeLine.getKeyFrames().add(new KeyFrame(Duration.millis(time/4), new KeyValue(timeBar.fillProperty(), color1)));
    timeLine.getKeyFrames().add(new KeyFrame(Duration.millis(time/3), new KeyValue(timeBar.fillProperty(), color2)));
    timeLine.getKeyFrames().add(new KeyFrame(Duration.millis(time/2), new KeyValue(timeBar.fillProperty(), color3)));
    timeLine.getKeyFrames().add(new KeyFrame(Duration.millis(time), new KeyValue(timeBar.widthProperty(), 0)));
    timeLine.play();
  }

  protected void openChat(){}


  /**
   * keyboard events to indicate what happens as each button is pressed.
   * @param event
   */
  public void keyBoard(KeyEvent event) {
    switch (event.getCode()) {
      case E:
      case C:
        rotateOnce(); // rotate right
        currentPieceBoard.displayPiece(game.getCurrentPiece());
        break;
      case Q:
      case Z:
        rotate(3); // rotate left
        currentPieceBoard.displayPiece(game.getCurrentPiece());
        break;
      case ESCAPE:
        gameWindow.startMenu();
        game.stopGame();
        game.getTimer().cancel();
        game.getTimer().purge();
        break;
      case R:
      case SPACE:
        game.swapCurrentPiece();
        currentPieceBoard.displayPiece(game.getCurrentPiece());
        smallerNextPieceBoard.displayPiece(game.getFollowingPiece());
        break;
      case T:
        sendingMsg = true;
        openChat();
        break;
      case ENTER:
      case X:
        blockClicked(board.getBlock(x, y));
        break;
    }
        if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) {
          if (y > 0) y--;
        } else if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) {
          if (x > 0) x--;
        } else if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) {
          if (y < game.getRows() - 1) {
            y++;
          }
        } else if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) {
          if (x < game.getCols() - 1) {
            x++;
          }
        } else if(event.getCode() == KeyCode.U){ // method is added if a user wants to go and end game early.
          game.timer.cancel();
          game.stopGame();
          Platform.runLater(() -> gameWindow.loadScoreScene(game));
        }
        board.hovering(board.getBlock(x,y)); //hovering with keys
    }

  /**
   * method returns the zero index from the list which is also the highest score
   * @return high score from list
   */
  public String getHighScore(){
      ArrayList<Pair<String, Integer>> newHighScore = ScoresScene.loadScores();
      return newHighScore.get(0).getValue().toString();
    }
  }
