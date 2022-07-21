package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.ScoreList;

import java.io.*;
import java.util.*;

public class ScoresScene extends BaseScene {
  private static final Logger logger = LogManager.getLogger(ScoresScene.class);
  protected Game game;
  private static Communicator communicator;
  protected SimpleListProperty<Pair<String, Integer>> localScores; // wrapper
  protected ObservableList<Pair<String, Integer>> localScoresList = FXCollections.observableArrayList(); // holds the scores
  protected SimpleListProperty<Pair<String,Integer>> remoteScore;
  protected ObservableList<Pair<String, Integer>> remoteScoresList;
  protected ArrayList<Pair<String, Integer>> onlineScores = new ArrayList<>();
  private final SimpleStringProperty userName = new SimpleStringProperty("Name");
  boolean insertScore = false;
  boolean insertOnlineScore = false;
  protected VBox scoreScreen;
  public ScoreList scoreList;
  public ScoreList onlineScoreList;
  public boolean checkScore = true;
  public Timer timer;


  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public ScoresScene(GameWindow gameWindow, Game game) {
    super(gameWindow);
    this.game = game;
    communicator = gameWindow.getCommunicator();
  }

  @Override
  public void initialise() {
    //adding a listener to filter a score once a message is received
    communicator.addListener(message ->
            Platform.runLater(() -> {filterScore(message);}));
    loadOnlineScores();
  }

  @Override
  public void build() {
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var scoreScenePane = new StackPane();
    scoreScenePane.setMaxHeight(gameWindow.getHeight());
    scoreScenePane.setMaxWidth(gameWindow.getWidth());
    scoreScenePane.getStyleClass().add("menu-background");
    root.getChildren().add(scoreScenePane);

//    var mainPane = new BorderPane();
//    scoreScenePane.getChildren().add(mainPane)

    scoreScreen = new VBox();
    scoreScreen.setAlignment(Pos.TOP_CENTER);
    scoreScenePane.getChildren().add(scoreScreen);

    Text allScores = new Text("High Scores");
    allScores.getStyleClass().add("bigtitle");
    scoreScreen.getChildren().add(allScores);

    var scoreTitles = new HBox();
    scoreTitles.setAlignment(Pos.CENTER);
    scoreTitles.setSpacing(30);
    scoreScreen.getChildren().add(scoreTitles);

    var scoreBoxes = new HBox();
    scoreBoxes.setAlignment(Pos.BOTTOM_CENTER);
    scoreBoxes.setSpacing(30);
    scoreScreen.getChildren().add(scoreBoxes);

//    var gridPane = new GridPane();
//    gridPane.setAlignment(Pos.CENTER_LEFT);
//    gridPane.setHgap(30);
//    scoreScreen.getChildren().add(gridPane);

    Text localScoresText = new Text("Local Scores");
    localScoresText.getStyleClass().add("title");
    scoreTitles.getChildren().add(localScoresText);

    Text onlineScoresText = new Text("Online Scores");
    onlineScoresText.getStyleClass().add("title");
    scoreTitles.getChildren().add(onlineScoresText);

    scoreList = new ScoreList();
    scoreBoxes.getChildren().add(scoreList);

    localScoresList = FXCollections.observableArrayList(loadScores());
    logger.info(localScoresList + " local scores list");
    localScores = new SimpleListProperty(localScoresList);
    scoreList.getScoreProperty().bind(localScores);
    scoreList.getName().bind(userName);

    onlineScoreList = new ScoreList();
    scoreBoxes.getChildren().add(onlineScoreList);

    remoteScoresList = FXCollections.observableArrayList(onlineScores);
    remoteScore = new SimpleListProperty(remoteScoresList);
    onlineScoreList.getScoreProperty().bind(remoteScore);
    onlineScoreList.getName().bind(userName);


//    onlineScoreList = new ScoreList();
//    remoteScore = new SimpleListProperty<>(remoteScoresList);
//    onlineScoreList.getScoreProperty().bind(remoteScore);

  }

  /**
   * method to split the message received into parts and handle when the message read "HISCORES"
   * The scores are then further filtered and put into a remote score list
   * @param message
   */
  public void filterScore(String message){
    logger.info("Filter Score");
    onlineScores.clear();
    String[] parts = message.split(" ", 2);
    String receivedScore = "null";
    if(parts[0].equals("HISCORES")){
      if(parts.length > 1){
        receivedScore = parts[1];
      }
    }

    String[] lines = receivedScore.split("\n");
    for(String line : lines){
      String[] component = line.split(":", 2);
      onlineScores.add(new Pair(component[0], Integer.parseInt(component[1])));
    }
    onlineScores.sort((s1,s2) -> (s2.getValue().compareTo(s1.getValue())));
    logger.info(remoteScoresList + " remote scores list");
    remoteScoresList.clear();
    remoteScoresList.addAll(onlineScores);
    if (checkScore) {
      checkScore = false;
      scoreScene();
    }
    scoreList.reveal();
    onlineScoreList.reveal();
  }


  /**
   * method to check whether a score obtained by the user is a new local or online high score.
   * If so the user is prompted to enter his or her name
   * and the list is iterated through and compared to the score which is then put into the right position in the list.
   */
  public void scoreScene(){
    int newScore = game.getScore();
//    int lowestScore = localScoresList.get(localScoresList.size()-1).getValue();
//    int lowestOnlineScore = remoteScoresList.get(remoteScoresList.size()-1).getValue();

    int counter = 0;
    int onlineCounter = 0;

      if(localScoresList.size() < 8){
        insertScore = true;
      }

      // loop through the scores and see where the new score ranks
        for(Pair<String, Integer> score : localScoresList){
          if(score.getValue() < newScore){
            logger.info("local score");
            insertScore = true;
            break;
          }
          counter++;
        }

      for(Pair<String, Integer> score : remoteScoresList){
        if(score.getValue() < newScore){
          insertOnlineScore = true;
          break;
        }
        onlineCounter++;
      }

      if(insertOnlineScore || insertScore){
        logger.info("Checking local score");

        var enter = new Button("Enter");
        enter.getStyleClass().add("menuItem");

        var highScoreText = new Text();
        highScoreText.setText("New High Score");
        highScoreText.getStyleClass().add("hiscore");

        var nameField = new TextField();
        nameField.setPromptText("Enter your name");
        nameField.requestFocus();
        nameField.setMaxWidth(300);

        scoreScreen.getChildren().add(highScoreText);
        scoreScreen.getChildren().add(nameField);
        scoreScreen.getChildren().add(enter);

        int onlineIndex = onlineCounter;
        int index = counter;

        enter.setOnAction((event) -> {
          String name = nameField.getText();
          userName.set(name);
          scoreScreen.getChildren().remove(highScoreText);
          scoreScreen.getChildren().remove(nameField);
          scoreScreen.getChildren().remove(enter);

          if(insertScore){
            localScoresList.add(index, new Pair<String, Integer>(name, newScore));
          }
          if(insertOnlineScore){
            remoteScoresList.add(onlineIndex, new Pair<String, Integer>(name, newScore));
            communicator.send("HISCORE " + name + ":" + newScore);
          }

          writeLocalScore(localScoresList);
          writeOnlineScores(name, newScore);
          loadOnlineScores();
          insertOnlineScore = false;
        });
      } else {
        scoreList.reveal();
        onlineScoreList.reveal();
        endGame();
      }
  }

  /**
   * method to load the new high scores within a list
   * if a file does not exist, the program runs the write scores method to put in default scores.
   * @return
   */
  public static ArrayList<Pair<String, Integer>> loadScores() {

    File f = new File("File.txt");
    ArrayList<Pair<String, Integer>> scores = new ArrayList<>();
    BufferedReader br = null;
    String line;
    if(!f.exists()){writeScores(scores);}

    try {
      br = new BufferedReader(new FileReader(f));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    try {
      while ((line = br.readLine()) != null) {
        String[] newLine = line.split(":");
        scores.add(new Pair<>(newLine[0], Integer.parseInt(newLine[1])));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    scores.sort((s1,s2) -> (s2.getValue().compareTo(s1.getValue())));
    logger.info(scores + " local scores list");
    return scores;
  }

  /**
   * method to put in default preloaded player scores.
   * @param scores
   */
  public static void writeScores(List<Pair<String, Integer>> scores) {
    scores.sort((s1, s2) -> (s2.getValue().compareTo(s1.getValue())));
    try {
      File f = new File("File.txt");
      ArrayList<Pair<String, Integer>> defaultScores = new ArrayList<>();
      int defaultScore = 100;
      if (!f.exists()){
      for (int i = 0; i < 10; i++) {
        defaultScores.add(new Pair("Player" + i, defaultScore));
        defaultScore = defaultScore + 100;
      }
      writeLocalScore(defaultScores);
     }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * method to write scores withing a list by obtaining the user name and user score
   * @param scores
   */
  public static void writeLocalScore(List<Pair<String, Integer>> scores) {
    scores.sort((s1, s2) -> (s2.getValue().compareTo(s1.getValue())));
    try{
    File f = new File("File.txt");
    FileWriter fw = new FileWriter(f);
    BufferedWriter bw = new BufferedWriter(fw);
    int count = 0;
      for (Pair<String, Integer> score : scores) {
        bw.write(score.getKey() + ":" + score.getValue() + "\n");
        count++;
        if(count == 10) break;
        }
        bw.close();
      } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * loading online high scores scores
   */

  public static void loadOnlineScores(){
    communicator.send("HISCORES");
  }

  /**
   * writing online high scores
   * @param name
   * @param score
   */
  public static void writeOnlineScores(String name, Integer score){
    communicator.send("HISCORE" + name + " :" + score);
  }

  /**
   * timer to move to menu once the user finishes seeing score scene
   * @param delay
   */
  public void menuTimer(long delay) {
    if (timer != null) {
      timer.cancel();
    }
    TimerTask task = new TimerTask() {
      public void run() {
        Platform.runLater(ScoresScene.this::startMenu);
      }
    };
    this.timer = new Timer();
    this.timer.schedule(task, delay);

  }

  /**
   * method to start menu
   */
  public void startMenu() {
    if (!insertScore && timer != null) {
      timer.cancel();
      gameWindow.startMenu();
    }
  }

  /**
   * method to end game after timer completed 10 seconds.
   */
  public void endGame(){
    menuTimer(10000);
  }
}
