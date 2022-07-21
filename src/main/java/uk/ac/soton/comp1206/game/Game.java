package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Pair;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.*;

import java.util.*;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to
 * manipulate the game state and to handle actions made by the player should take place inside this
 * class.
 */
public class Game {

  private static final Logger logger = LogManager.getLogger(Game.class);

  /** Number of rows */
  protected final int rows;

  /** Number of columns */
  protected final int cols;

  /** The grid model linked to the game */
  protected final Grid grid;

  public IntegerProperty totalScore = new SimpleIntegerProperty(0);
  public SimpleIntegerProperty level = new SimpleIntegerProperty(0);
  public SimpleIntegerProperty lives = new SimpleIntegerProperty(3);
  public IntegerProperty multiplier = new SimpleIntegerProperty(1);
  public NextPieceListener nextPieceListener;
  public LineClearedListener lineClearedListener;
  public LivesListener livesListener;
  public NewScoreListener newScoreListener;
  protected GamePiece currentPiece;
  protected GamePiece followingPiece;
  public Timer timer;
  public GameLoopListener gameLoopListener;
  public ArrayList<Pair<String, Integer>> scoreList = new ArrayList<>();
  protected final ScheduledExecutorService executor;

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Game(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    // Create a new grid model to represent the game state
    this.grid = new Grid(cols, rows);
    this.executor = Executors.newSingleThreadScheduledExecutor();
  }

  /** Start the game */
  public void start() {
    logger.info("Starting game");
    initialiseGame();
    createTimer();
  }

  /** Initialise a new game and set up anything that needs to be done at the start */
  public void initialiseGame() {
    logger.info("Initialising game");
    currentPiece = spawnPiece();
    followingPiece = spawnPiece();
    nextPiece();
  }

  /**
   * Handle what should happen when a particular block is clicked
   *
   * @param gameBlock the block that was clicked
   */
  public void blockClicked(GameBlock gameBlock) {
    // Get the position of this block
    int x = gameBlock.getX();
    int y = gameBlock.getY();
    // if the piece can be played, play it, restart the timer, check whether the lines are cleared and get the next pieces
    if (grid.canPlayPiece(currentPiece, x, y)) {
      grid.playPiece(currentPiece, x, y);
      restartTimer();
      afterPiece();
      nextPiece();
    } else {
      MultiMedia.playAudio("fail.wav");
    }
  }

  /**
   * Get the grid model inside this game representing the game state of the board
   *
   * @return game grid model
   */
  public Grid getGrid() {
    return grid;
  }

  /**
   * Get the number of columns in this game
   *
   * @return number of columns
   */
  public int getCols() {
    return cols;
  }

  /**
   * Get the number of rows in this game
   *
   * @return number of rows
   */
  public int getRows() {
    return rows;
  }

  /**
   * Generate a random number
   * @return the corresponding piece number
   */
  public GamePiece spawnPiece() {
    logger.info("Generating Random Piece");
    Random rand = new Random();
    return GamePiece.createPiece(rand.nextInt(15));
    //return GamePiece.createPiece(3);
  }

  /**
   * returns the following piece by generating a random piece and providing it to the listener
   */
  public void nextPiece() {
    logger.info("Spawning next Piece");
    currentPiece = followingPiece;
    followingPiece = spawnPiece();
    nextPieceListener();
  }

  /**
   * The method checks if any line needs to be cleared
   * If a row and/or column are full the corresponding grid values will be set to zero
   * Once a line is cleared it adds it to the number of lines cleard which is used to calculate the new score.
   */
  public void afterPiece() {
    ArrayList<Integer> full_Row = new ArrayList<>(); // HashSet can't be used because it removes duplicates
    ArrayList<Integer> full_Column = new ArrayList<>();
    int linesCleared;
    int blocksCleared = 0;
    var lines = new HashSet<GameBlockCoordinate>();

    // for vertical lines
    for (var i = 0; i < cols; i++) {
      int counter = 0;
      for (var j = 0; j < rows; j++) {
        if (grid.get(i, j) == 0) {
          logger.info("Column is not full");
          break;
        }
        counter++;
        if (counter == rows) { // when all the rows in the column are occupied
          logger.info("Column was full");
          full_Column.add(i);
        }
      }
    }
    // for horizontal lines
    for (var i = 0; i < rows; i++) {
      int counterTwo = 0;
      for (var j = 0; j < cols; j++) {
        if (grid.get(j, i) == 0) {
          logger.info("Row is not Full");
          break;
        }
        counterTwo++;
        if (counterTwo == cols) { // when all the columns in the rows are occupied
          logger.info("Row was full");
          full_Row.add(i);
        }
      }
    }

    for (int r : full_Row) {
      for (int i = 0; i < cols; i++) {
        blocksCleared++;
        grid.set(i, r, 0);
        MultiMedia.playAudio("clear.wav");
        lines.add(new GameBlockCoordinate(i, r));
      }
    }

    for (int c : full_Column) {
      for (int j = 0; j < rows; j++) {
        blocksCleared++;
        grid.set(c, j, 0);
        MultiMedia.playAudio("clear.wav");
        lines.add(new GameBlockCoordinate(c, j));
      }
    }
    linesCleared = full_Row.size() + full_Column.size();
    score(linesCleared, blocksCleared);
    level.set(totalScore.get() / 1000);
    lineCleared(lines);
  }

  /**
   * getter method which gives the score property
   * @return total score of user.
   */
  public IntegerProperty getScoreProperty() {
    return totalScore;
  }
  /**
   * getter method which gives the level property
   * @return level of user.
   */
  public SimpleIntegerProperty getLevelProperty() {
    return level;
  }

  /**
   * getter method which gives the lives property
   * @return lives of user.
   */
  public SimpleIntegerProperty getLivesProperty() {
    return lives;
  }

  /**
   * getter method which gives the multiplier property
   * @return multiplier.
   */
  public IntegerProperty getMultiplierProperty() {return multiplier;}

  /**
   * This score method is used to calculate the users score
   * It takes the lines and blocks cleared as parameters which is used in the formula as provided in the coursework description
   * Furthermore it also calculates the multiplier amount.
   */
  public void score(int lines, int blocks) {
    int addScore = lines * blocks * 10 * getMultiplier();
    int score = getScore() + addScore;
    setScore(score);

    if (addScore > 0) {
      setMultiplier(getMultiplier() + 1);
    } else {
      setMultiplier(1);
    }
  }

  /**
   * A general comment, in the getter methods, the .get() method was used since it returns the
   *    primitive int type
   *    getValue() could not be used as it returns a wrapped value which does not work for methods such
   *    as the score method.
   */
  public int getLevel() {
    return getLevelProperty().get();
  }
  public void setLevel(int level) {
    this.level.set(level);
  }

  public int getScore() {
    return getScoreProperty().get();
  }
  public void setScore(int score) {
    totalScore.set(score);
    newScoreListener();
  }

  public int getLives() {
    return getLivesProperty().get();
  }
  public void setLives(int lives) {this.lives.set(lives);}

  public int getMultiplier() {
    return getMultiplierProperty().get();
  }
  public void setMultiplier(int multiplier) {
    this.multiplier.set(multiplier);
  }

  /**
   * setting listener for next piece
   * @param listener
   */

  public void setNextPieceListener(NextPieceListener listener) {
    nextPieceListener = listener;
  }

  /**
   * listener sends next piece
   */
  public void nextPieceListener() {
    if (nextPieceListener != null) {
      nextPieceListener.nextPiece(currentPiece, followingPiece);
    }
  }

  /**
   * setting listener for line cleared
   * @param listener
   */
  public void setLineClearedListener(LineClearedListener listener) {
    lineClearedListener = listener;
  }

  /**
   * listener which sends the coordinates of the lines cleared
   * @param coordinates
   */
  public void lineCleared(HashSet<GameBlockCoordinate> coordinates) {
    if (lineClearedListener != null) {
      lineClearedListener.clearLine(coordinates);
    }
  }

  /**
   * setting listener for lives
   * @param listener
   */

  public void setLivesListener(LivesListener listener){livesListener = listener;}
  public void livesListener(){
    if(livesListener != null){
      Platform.runLater(() -> livesListener.checkLives());
    }
  }

  /**
   * setting listener for new score
   * @param listener
   */

  public void setNewScoreListener(NewScoreListener listener){newScoreListener = listener;}
  public void newScoreListener(){
    if(newScoreListener != null){
      newScoreListener.newScore();
    }
  }

  /**
   * getter method for current piece
   * @return current piece
   */
  public GamePiece getCurrentPiece() {
    return currentPiece;
  }

  /**
   * getter method for following piece
   * @return
   */
  public GamePiece getFollowingPiece() {
    return followingPiece;
  }

  /**
   * method to rotate piece an "x" amount of times
   * @param x
   */
  public void rotatePiece(int x) {
    MultiMedia.playAudio("rotate.wav");
    currentPiece.rotate(x);
  }

  /**
   * method to swap the current game piece which will be played with the following piece
   */
  public void swapCurrentPiece() {
    logger.info("Swapping pieces");
    GamePiece piece = currentPiece;
    currentPiece = followingPiece;
    followingPiece = piece;
  }

  /**
   * getter method for timer
   * @return timer
   */
  public Timer getTimer(){
    return timer;
  }

  /**
   * This method creates a timer and runs the game loop
   */
  public void createTimer(){
    logger.info("creating timer" + getTimerDelay());
    timer = new Timer();
    //The timer-task executes game loop once the getTimerDelay returns a specific time.
    TimerTask timerTask = new TimerTask() {
      @Override
      public void run() {
        gameLoop();}
    };
    timer.schedule(timerTask, getTimerDelay());
    loopListener(getTimerDelay());
  }

  /**
   * setting game loop listener
   * @param listener
   */
  public void setOnGameLoop(GameLoopListener listener){
    this.gameLoopListener = listener;
  }

  /**
   * setting the timer delay which returns the bigger number between the two provided.
   * @return delay
   */
  public int getTimerDelay(){
    return Math.max(2500, (12000-500*getLevel()));
  }

  /**
   * game loop method checks if the user has any lives
   * If the user has lives, each time the loop runs it subtracts alife, provides the next piece, sets the multiplier to 1, and creates a new timer.
   * However if there are no more lives, once the loop finishes the timer is cancelled.
   */
  public void gameLoop(){
    if(getLives() > 0){
      lives.set(lives.get()-1);
      nextPiece();
      multiplier.set(1);
      createTimer();
      MultiMedia.playAudio("lifelose.wav");
    } else if(getLives() == 0){
      timer.cancel();
      livesListener();
    }
  }

  /**
   * restarts the timer by cancelling it and creating a new one
   */

  public void restartTimer(){
    timer.cancel();
    timer.purge();
    createTimer();
  }

  /**
   * listener which will be used to link the listener to the timer in the game which also restarts the loop
   */
  public void loopListener(int loops){
    if(gameLoopListener != null){
      gameLoopListener.gameLoop(loops);
    }
  }

  /**
   * method to stop all the processes withing the game
   */
  public void stopGame(){
    logger.info("shutting down game");
    executor.shutdownNow();
  }

//  public ArrayList<Pair<String, Integer>> getScoreList(){
//    return scoreList;
//  }
}
