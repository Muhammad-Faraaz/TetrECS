package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.LeaderBoard;
import uk.ac.soton.comp1206.ui.ScoreList;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class MultiplayerScene extends ChallengeScene{
    Logger logger = LogManager.getLogger(MultiplayerScene.class);
    private Communicator communicator;
    protected LeaderBoard leaderBoard;
    protected TextField chat;
    protected Text msgAndTime;
    protected VBox vBox;
    protected TextFlow instruction;
    protected Text openChat;
    protected boolean sendingMsg = false;
    private StringProperty name = new SimpleStringProperty("");
    protected ArrayList<Pair<String, Integer>> remotePlayerScores = new ArrayList<>();
    protected ObservableList<Pair<String, Integer>> remotePlayerScoresList = FXCollections.observableArrayList(remotePlayerScores);
    DateTimeFormatter date = DateTimeFormatter.ofPattern("HH:mm:ss");


    /**
     * Create a new Multiplayer Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow) {
        super(gameWindow);
        this.communicator = gameWindow.getCommunicator();
    }

    @Override
    public void build(){
        super.build(); // do this instead of repeating everything

        vBox = new VBox();
        vBox.setMaxHeight(80);
        mainPane.setBottom(vBox);

        rightBox.getChildren().remove(highScoreTitle);
        rightBox.getChildren().remove(highScore);
        rightBox.getChildren().remove(levelTitle);
        rightBox.getChildren().remove(level);

        remotePlayerScoresList = FXCollections.observableArrayList(remotePlayerScores);
        SimpleListProperty<Pair<String, Integer>> deadPlayerScoresListWrapper = new SimpleListProperty<>(remotePlayerScoresList);
        leaderBoard = new LeaderBoard();
        Text leaderTitle = new Text("LeaderBoard");
        leaderTitle.getStyleClass().add("title");
        leaderBoard.getStyleClass().add("title");
        leaderBoard.getScoreProperty().bind(deadPlayerScoresListWrapper);
        leaderBoard.getName().bind(name);
        leaderBoard.userAmount(3);
        rightBox.getChildren().add(0, leaderTitle);
        rightBox.getChildren().add(1, leaderBoard);

        instruction = new TextFlow();
        instruction.setTextAlignment(TextAlignment.CENTER);
        instruction.setMaxWidth(500);
        vBox.getChildren().add(instruction);

        openChat = new Text("Press the key T to start chat");
        openChat.getStyleClass().add("chat");
        instruction.getChildren().add(openChat);

        chat = new TextField();
        chat.setMaxWidth(200);
        chat.setVisible(false);
        vBox.getChildren().add(chat);

        timeBar = new Rectangle(0,10, gameWindow.getWidth(), 20);
        timeBar.fillProperty().set(Color.RED);
        VBox lowerBox = new VBox();
        lowerBox.getChildren().add(timeBar);


        mainPane.setBottom(lowerBox);
        // design

//        chat.setOnKeyPressed(keyEvent -> {
//            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
//                this.communicator.send("MSG  " + chat.getText());
//            }
//        });
    }

    @Override
    public void initialise(){
        super.initialise();
        communicator.addListener(this::receiveMsg);
        communicator.send("SCORES");
        communicator.send("NICK");
    }

    /**
     * handles when a message starts with a particular message
     * @param message
     */
    private void handleMessage(String message) {
        String[] msg = message.split(" ", 2);
        if (message.startsWith("SCORES")) {
            logger.info("handling scores 2");
             handleScores(msg[1]);
        } else if (message.startsWith("MSG")) {
             handleMsg(msg[1]);
            } else if (message.startsWith("NICK")) {
                handleNick(msg[1]);
            }
        }

    /**
     * method to receive message and implement handleMessage method.
     * @param message
     */
    private void receiveMsg(String message) {
        Platform.runLater(() -> handleMessage(message));
    }

    public void setupGame(){
        this.game = new MultiplayerGame(5 , 5, communicator);
    }

    protected void openChat(){
        sendingMsg = true;
        Platform.runLater(() ->{
            chat.setVisible(true);
            sendingMsg = true;
            chat.setEditable(true);
            chat.requestFocus();
        });
    }

    /**
     * handles message time
     * @param message
     */
    public void handleMsg(String message){
        var current = date.format(LocalDateTime.now());
        msgAndTime = new Text("[" + current + "] " + message.replace("MSG ", "") + "\n");
        msgAndTime.getStyleClass().add("myname");
        chat.clear();
        chat.setVisible(false);
        sendingMsg = false;
    }

    /**
     * method to set user name
     * @param name
     */
    private void setName(String name) {
        this.name.set(name);
    }

    /**
     * method to handle score, and add or remove player from leaderBoard.
     * @param message
     */
    public void handleScores(String message){
        logger.info(message + " message");
        remotePlayerScores.clear();
        String[] parts = message.split("\n");
            for(String part: parts) {
                String[] component = part.split(":");
                String name = component[0];
                logger.info(name + " name");
                int score = Integer.parseInt(component[1]);
                String lives = component[2];
                if(lives.equals("DEAD")){
                    leaderBoard.removePlayer(name);
                    }
                remotePlayerScores.add(new Pair(name, score));
                logger.info("adding player" + name + score);
                }
            remotePlayerScores.sort((s1, s2) -> {return s2.getValue().compareTo(s1.getValue());});
            logger.info(remotePlayerScores + "dead player scores");
            remotePlayerScoresList.clear();
            remotePlayerScoresList.addAll(remotePlayerScores);
            logger.info(remotePlayerScoresList + " list ");
    }


    /**
     * method to handle new nickname
     * @param message
     */
    public void handleNick(String message) {
        setName(message.replace("NICK ",""));
    }

    /**
     * handles keyboard events
     * @param event
     */
    @Override
    public void keyBoard(KeyEvent event) {
        super.keyBoard(event);
        switch (event.getCode()){
            case T:
                sendingMsg = true;
                chat.setVisible(true);
                break;
            case ESCAPE:
                communicator.send("DIE");
                gameWindow.startMenu();
                break;
        }
        if(event.getCode() == KeyCode.ENTER && sendingMsg == true){
            communicator.send("MSG " + chat.getText());
        }
    }
}

