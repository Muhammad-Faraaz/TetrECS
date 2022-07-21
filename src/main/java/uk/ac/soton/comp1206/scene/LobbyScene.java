package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class LobbyScene extends BaseScene{
    private static Communicator communicator;
    private Timer timer;
    protected TextFlow channelNames;
    private VBox rightBox;
    private VBox leftBox;
    private HBox bottomBox;
    private ScrollPane scroller;
    private boolean channelCreated = false;
    private boolean host = false;
    DateTimeFormatter date = DateTimeFormatter.ofPattern("HH:mm:ss");
    public TextFlow chatBox = new TextFlow();
    protected TextFlow userList;
    public Label error;
    private Button startButton;
    private Button leaveButton;
    private Timer buttonTimer;
    private Timer inChannel;
    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        communicator = gameWindow.getCommunicator();
    }

    /**
     * method ot handle messages with different commands.
     * @param message
     */
    public void handleTasks(String message) {
        String[] parts = message.split(" ", 2);
        String part = parts[0];
        switch(part){
            case "CHANNELS":
                this.handleChannel(message);
                break;
            case "ERROR":
                //this.handleError();
                break;
            case "MSG":
                this.handleMsg(message);
                break;
            case "PARTED":
                //createButton();
                break;
            case "USERS":
                handleUserList(message);
                break;
            case "START":
                startGame();
                break;
            case "HOST":
                host = true;
                break;
        }
    }

    @Override
    public void initialise() {
        communicator.addListener(message ->
                Platform.runLater(() -> {
                    handleTasks(message);
                }));
        // if the user presses the escape button the communicator sends part
        // Next all the timers are cancelled and the user is taken to the menu scene
    this.scene.setOnKeyPressed(
        (e) -> {
          if (e.getCode().equals(KeyCode.ESCAPE)) {
            if (channelCreated) {
              communicator.send("PART");
              timer.cancel();
              buttonTimer.cancel();
              this.gameWindow.startMenu();
            }
          }
        });
         lobbyTimer();
    }

    @Override
    public void build() {
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var lobbyPane = new BorderPane();
        lobbyPane.setMaxWidth(gameWindow.getWidth());
        lobbyPane.setMaxHeight(gameWindow.getHeight());
        lobbyPane.getStyleClass().add("menu-background");
        root.getChildren().add(lobbyPane);

        var lobbyStack = new StackPane();
        lobbyStack.setAlignment(Pos.CENTER);
        lobbyPane.setCenter(lobbyStack);

        var titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER);
        lobbyPane.setTop(titleBox);

        var multiplayerTitle = new Text("Multiplayer Lobby");
        multiplayerTitle.getStyleClass().add("title");
        titleBox.getChildren().add(multiplayerTitle);


        var hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setSpacing(50);
        lobbyStack.getChildren().add(hBox);

        // will hold the channels
        rightBox = new VBox();
        rightBox.setMaxHeight(600);
        rightBox.setMaxWidth(200);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        leftBox = new VBox();
        leftBox.setMaxWidth(150);
        leftBox.setPrefHeight(250);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getChildren().add(leftBox);
        hBox.getChildren().add(rightBox);

        var channelTitle = new Text("Active Channels");
        channelTitle.getStyleClass().add("title");
        rightBox.getChildren().add(channelTitle);

        // holds the channels made
        channelNames = new TextFlow();
        channelNames.getStyleClass().add("textFlow");
        channelNames.setMaxWidth(200);
        channelNames.setPrefHeight(200);
        channelNames.setTextAlignment(TextAlignment.LEFT);
        leftBox.getChildren().add(channelNames);

        userList = new TextFlow();
        userList.getStyleClass().add("textFlow");

        //game is created once the user enters a name for the channel and the user presses clicks.
        var createButton = new Button("Create Game");
        createButton.getStyleClass().add("menuItem");
        rightBox.getChildren().add(createButton);
        createButton.setOnMouseClicked(
                mouseEvent -> {
                    if (!channelCreated) {
                        createChannelChat();
                        communicator.send("CREATE");
                    }
                });


        ScrollPane scroller = new ScrollPane();
        scroller.getStyleClass().add("scroll-pane");
        scroller.setMinWidth(300);
        scroller.setFitToWidth(true);
        scroller.setPrefHeight(gameWindow.getHeight()/2);
        scroller.setContent(channelNames);
        rightBox.getChildren().add(scroller);

    }

    /**
     * Timer for the game to update the list of users.
     */
    public void lobbyTimer(){
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                communicator.send("LIST");
            }
        };
        timer.schedule(timerTask, 100, 1000);
    }

    /**
     * The button timer is used in order for the game to set the start button visible once the user is within the channel.
     */
    public void buttonTimer(){
        buttonTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                startButton.setVisible(host);
            }
        };
        timer.schedule(timerTask, 100, 1000);
    }

    /**
     * This method handles when the message reads "CHANNELS"
     * It receives the channel names and allsows the user to jon the channel
     * @param message
     */
    public void handleChannel(String message){
    // The word CHANNELS is replaced by an empty space and the channel names are received from the received message
        String[] channels = message.replace("CHANNELS", "").split("/n");
        channelNames.getChildren().clear();
        for(String channel: channels){
            var channelName = new Text(channel);
            channelName.getStyleClass().add("menu-item");
            channelName.setOnMouseClicked((e) -> {
                    if(!channelCreated){
                    joinChannel(channel);
                    channelChat(channel);
                    }
        });
            channelNames.getChildren().add(channelName);
        }
    }

    /**
     * Creates a box to allow the user to enter a channel name once the user presses the create channel button.
     */
    private void createChannelChat() {
        if (!channelCreated) {
            channelCreated = true;

            var newChannel = new TextField();
            newChannel.setMaxWidth(300);
            rightBox.getChildren().add(newChannel);
            newChannel.setPromptText("Enter Channel Name (Press Enter)");

            newChannel.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ENTER && !newChannel.getText().isEmpty()) {
                        String channelName = newChannel.getText();
                        createChannel(channelName);
                        channelChat(channelName);
                        host = true;
                }
            });
            leftBox.getChildren().add(newChannel);
        }
    }

    /**
     * creates a Chat UI which allows users to send messages, change nicknames, start a game, and leave the game as well.
     * @param name
     */
    public void channelChat(String name){
        channelCreated = true;

        var channelName = new Text(name);
        channelName.getStyleClass().add("heading");
        leftBox.getChildren().clear();
        leftBox.getChildren().add(channelName);

        chatBox = new TextFlow();
        chatBox.getStyleClass().add("TextField");
        chatBox.setMinWidth(300);
        chatBox.setPrefHeight(300);

        var userList = new TextFlow();
        userList.setMinWidth(200);
        chatBox.getChildren().add(userList);

        var msgBox = new TextField();
        msgBox.setPromptText("Send a message");
        msgBox.setPrefWidth(40);
        leftBox.getChildren().add(msgBox);

        scroller = new ScrollPane();
        scroller.getStyleClass().add("scroll-pane");
        scroller.setMinWidth(gameWindow.getWidth()/2);
        scroller.setPrefHeight(300);
        scroller.setFitToWidth(true);
        scroller.setContent(chatBox);
        leftBox.getChildren().add(scroller);

        Text instructions = new Text(
                "\n" + "Welcome to the lobby " + "\n"  + "Type /nick to change nickname \n" +
                "Type /part to quit the channel \n" +
                "Type /start to start the game" );

        instructions.getStyleClass().add("chat");
        chatBox.getChildren().add(instructions);

        msgBox.setOnKeyPressed(keyEvent -> {
            // the msg box not being empty is neccesary otherwise the game can command to play a piece or do some other command.
            if (keyEvent.getCode() == KeyCode.ENTER && !msgBox.getText().isEmpty()) {
                if(msgBox.getText().startsWith("/start") && host == true){
                    communicator.send("START");
                } else if(msgBox.getText().startsWith("/part")){
                    communicator.send("PART");
                } else if (msgBox.getText().startsWith("/nick")) {
                    String nick = msgBox.getText().replace("/nick ", "");
                    communicator.send("NICK " + nick);
                } else {
                    String msg = msgBox.getText();
                    communicator.send("MSG  " + msg);
                }
                msgBox.clear();
            }
        });
        bottomBox = new HBox();
        bottomBox.setSpacing(30);
        leftBox.getChildren().add(bottomBox);

        startButton = new Button("Start Game");
        startButton.getStyleClass().add("menuItem");
        bottomBox.getChildren().add(startButton);
        startButton.setOnMouseClicked(e -> communicator.send("START"));

        leaveButton = new Button("Leave");
        leaveButton.getStyleClass().add("menuItem");
        bottomBox.getChildren().add(leaveButton);
        leaveButton.setOnMouseClicked(
                e -> {
                    host = false;
                    channelCreated = false;
                    buttonTimer.cancel();
                    leftBox.getChildren().removeAll(leftBox.getChildren());
                    communicator.send("PART");
                });
        buttonTimer();
    }

    /**
     * This method allows the user to see the time of the messages sent by the user themselves or another user.
     * @param message
     */
    public void handleMsg(String message){
//        Text msg = new Text(message.replace("MSG ", ""));
        var current = date.format(LocalDateTime.now());
        Text msgTime = new Text("[" + current + "] " + (message.replace("MSG ", "")) + "\n");
        msgTime.getStyleClass().add("myname");
        chatBox.getChildren().add(msgTime);
    }

    /**
     * This method receives the username of the users.
     * @param message
     */
    public void handleUserList(String message){
        userList.getChildren().clear();
        String[] userNames = message.replace("USERS ", "").split("\n");
        for(String userName : userNames){
            Text name = new Text(userName + ", ");
            name.getStyleClass().add("myname");
            userList.getChildren().add(name);

            //if there is only one channel then the user is the host
            if(userNames.length == 1){
                host = true;
            }
        }
    }

    public void handleError(){
        error = new Label("Error");
        error.getStyleClass();
        leftBox.getChildren().add(error);
    }

    /**
     * method to command communicator to join a channel
     * @param message
     */
    public void joinChannel(String message){
        communicator.send("JOIN " + message);
    }

    /**
     * method to command communicator to create a channel
     * @param channelName
     */
    public void createChannel(String channelName){
        communicator.send("CREATE " + channelName);
    }

    /**
     * method to start the game
     */
    public void startGame(){
        timer.cancel();
        buttonTimer.cancel();
        gameWindow.loadMultiplayerGame();

    }

//    public void createButton(){
//        var createButton = new Button("Create Game");
//        createButton.getStyleClass().add("menuItem");
//        rightBox.getChildren().add(createButton);
//        createButton.setOnMouseClicked(
//                mouseEvent -> {
//                    if (!channelCreated) {
//                        createChannelChat();
//                        communicator.send("CREATE");
//                    }
//                });
//    }
}
