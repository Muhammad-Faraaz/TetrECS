package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class MultiplayerGame extends Game{
    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
    private final Communicator communicator;
    private Queue<GamePiece> pieceList = new LinkedList<>();
    /**
     * Create a new multiplayer game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(int cols, int rows, Communicator communicator) {
        super(cols, rows);
        this.communicator = communicator;
        communicator.addListener(this::handleMsg);
        for (int i = 0; i<10; i++) {
            this.communicator.send("PIECE");
        }
    }
    @Override
    public void start() {
        Platform.runLater(this::initialiseGame);
        createTimer();
    }

    @Override
    public void initialiseGame() {
        Platform.runLater(() -> {
            currentPiece = spawnPiece();
            followingPiece = spawnPiece();
            nextPieceListener();
        });
    }

    @Override
    public void nextPiece() {
        logger.info("Spawning next Piece");
        currentPiece = followingPiece;
        followingPiece = spawnPiece();
        nextPieceListener();
        communicator.send("SCORES");
    }

    /**
     * handles if a message received starts with the word "PIECE"
     * @param message
     */
    public void handleMsg(String message) {
        if (message.startsWith("PIECE")) {
             newPiece(message);
        }
    }


    /**
     * The new piece method takes the piece message provided and adds the commanded piece into the list.
     * @param piece
     */
    private void newPiece(String piece) {
        piece = piece.replace("PIECE ","");
        pieceList.add(GamePiece.createPiece(Integer.parseInt(piece)));
    }

    /**
     * Method sends piece and returns/spawns the first piece in the list
     * @return
     */
    @Override
    public GamePiece spawnPiece() {
        communicator.send("PIECE");
        return pieceList.poll();
    }

    @Override
    public void blockClicked(GameBlock gameBlock) {
        super.blockClicked(gameBlock);
        StringBuilder board = new StringBuilder();
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                board.append(grid.get(x,y) + " ");
            }
        }
        communicator.send("BOARD " + board);
        communicator.send(board.toString());
    }

    @Override
    public void score(int lines, int blocks) {
        super.score(lines,blocks);
        communicator.send("SCORE " + totalScore.getValue());
    }
}
