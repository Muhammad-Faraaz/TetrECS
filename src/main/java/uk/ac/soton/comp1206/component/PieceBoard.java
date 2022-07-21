package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

public class PieceBoard extends GameBoard{
    GameBlock[][] blocks;
    public PieceBoard(int cols, int rows, double width, double height) {
        super(cols, rows, width, height);
    }

    /**
     * method to display piece on the piece boards.
     * @param gamePiece
     */
    public void displayPiece(GamePiece gamePiece){
        int[][] piece = gamePiece.getBlocks();
        for(int i = 0; i < piece.length; i++){
            for(int j = 0; j < piece[i].length; j++){
                int value = piece[i][j];
                grid.set(i,j,value);
            }
        }
    }

    /**
     * method to play a piece in the specific position
     * method is used by the rotate method in Challenge Scene
     * @param piece
     */
    public void playPiece(GamePiece piece){
        grid.playPiece(piece, 0,0);
    }
}
