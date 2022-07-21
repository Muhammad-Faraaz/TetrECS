package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer
 * values arranged in a 2D arrow, with rows and columns.
 *
 * <p>Each value inside the Grid is an IntegerProperty can be bound to enable modification and
 * display of the contents of the grid.
 *
 * <p>The Grid contains functions related to modifying the model, for example, placing a piece
 * inside the grid.
 *
 * <p>The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

  /** The number of columns in this grid */
  private final int cols;
  /** The number of rows in this grid */
  private final int rows;
  /** The grid is a 2D arrow with rows and columns of SimpleIntegerProperties. */
  private final SimpleIntegerProperty[][] grid;

  Logger logger = LogManager.getLogger(Grid.class);

  /**
   * Create a new Grid with the specified number of columns and rows and initialise them
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Grid(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    // Create the grid itself
    grid = new SimpleIntegerProperty[cols][rows];

    // Add a SimpleIntegerProperty to every block in the grid
    for (var y = 0; y < rows; y++) {
      for (var x = 0; x < cols; x++) {
        grid[x][y] = new SimpleIntegerProperty(0);
      }
    }
  }

  /**
   * Get the Integer property contained inside the grid at a given row and column index. Can be used
   * for binding.
   *
   * @param x column
   * @param y row
   * @return the IntegerProperty at the given x and y in this grid
   */
  public IntegerProperty getGridProperty(int x, int y) {
    return grid[x][y];
  }

  /**
   * Update the value at the given x and y index within the grid
   *
   * @param x column
   * @param y row
   * @param value the new value
   */
  public void set(int x, int y, int value) {
    grid[x][y].set(value);
  }

  /**
   * Get the value represented at the given x and y index within the grid
   *
   * @param x column
   * @param y row
   * @return the value
   */
  public int get(int x, int y) {
    try {
      // Get the value held in the property at the x and y index provided
      return grid[x][y].get();
    } catch (ArrayIndexOutOfBoundsException e) {
      // No such index
      return -1;
    }
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

  public SimpleIntegerProperty[][] getGrid() {
    return grid;
  }

  /**
   * This method checks whether a piece can be played or not
   * It iterates through each row and the corresponding column to see which grid is empty
   * IF a grid value is 1 or -1 the method returns false else true.
   * @param gamePiece
   * @param blockX
   * @param blockY
   * @return
   */
  public boolean canPlayPiece(GamePiece gamePiece, int blockX, int blockY) {
    blockX = blockX - 1;
    blockY = blockY - 1;
    logger.info("Checking Can Play Piece Method " + gamePiece.toString());
    int[][] pieces = gamePiece.getBlocks(); // Array of Shape (3x3)
    // first for loop responsible to loop through each row
    for (int x = 0; x < pieces.length; x++) {
      // The second for loop is for each individual element in each individual array and runs until
      // it is less than the length
      // of the current row (blockX)
      for (int y = 0; y < pieces[x].length; y++) {
        int value = pieces[x][y];
        if (value == 0) continue;
        int gridValue = get(x + blockX, y + blockY);
        if (gridValue > 0 || gridValue == -1) {
          logger.info("There is something there");
          return false;
        }
      }
    }
    return true;
  }

  /**
   * the play piece method first calls the can play piece method.
   * If it can't play a piece it won't execute anything
   * Otherwise it requests the game piece block values and sets them into the grid.
   * @param gamePiece
   * @param x grid value
   * @param y grid value
   */
  public void playPiece(GamePiece gamePiece, int x, int y) {
    if (!canPlayPiece(gamePiece, x, y)) return;
    x = x - 1;
    y = y - 1;
    logger.info("Checking play piece method " + gamePiece);
    int[][] pieces = gamePiece.getBlocks();
    for (int i = 0; i < pieces.length; i++) {
      for (int j = 0; j < pieces[i].length; j++) {
        int value = pieces[i][j];
        if (value == 0) continue;
        set(i + x, j + y, value);
      }
    }
  }
}


