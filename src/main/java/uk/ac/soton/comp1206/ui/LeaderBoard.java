package uk.ac.soton.comp1206.ui;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class LeaderBoard extends ScoreList {
  private static final Logger logger = LogManager.getLogger(LeaderBoard.class);
  ArrayList<String> eliminatedPlayers = new ArrayList();

  public LeaderBoard() {
    setVisible(true);
  }

  /**
   * method to remove player and add to eliminated players list.
   * @param name
   */
  public void removePlayer(String name){
    eliminatedPlayers.add(name);
  }

  /**
   * method to update leaderboard.
   */
  public void updateScore() {
    logger.info("Making a leader board");
      scoreBoxList.clear();
      getChildren().clear();
      int numberOfScores = 0;
      for (Pair<String, Integer> score : scoreList) {
        if (numberOfScores == 5) break;

        Text userName = new Text(score.getKey() + ": ");
        userName.getStyleClass().add("title");
        if (score.getKey().equals(name.get())) {
            userName.getStyleClass().add("title");
        }
        if(eliminatedPlayers.contains(score.getKey())){
          userName.getStyleClass().add("deadscore");
        }
        userName.setTextAlignment(TextAlignment.CENTER);

        Text userScore = new Text(score.getValue().toString());
        userScore.getStyleClass().add("title");
        userScore.setTextAlignment(TextAlignment.CENTER);

        Text userDetails = new Text(score.getKey() + " " + score.getValue());
        logger.info(userDetails + " userdetails");
        HBox scoreBox = new HBox();
        scoreBox.setAlignment(Pos.CENTER);
        scoreBox.getChildren().addAll(userName, userScore);
        scoreBox.setSpacing(10);
        getChildren().add(userDetails);
        scoreBoxList.add(scoreBox);

        reveal();
    }
  }
}

