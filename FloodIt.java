import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//------------------------------------UTILS------------------------------------------------------

// Utility interface to contain constants used throughout FloodIt
interface Utils {
  // width of each cell
  int CELL_WIDTH = 30;

  // colors
  Color PURPLE = Color.magenta;
  Color BLUE = Color.blue;
  Color GREEN = Color.green;
  Color YELLOW = Color.yellow;
  Color PINK = Color.pink;
  Color CYAN = Color.CYAN;

  // array list of colors
  ArrayList<Color> colors = new ArrayList<Color>(
      Arrays.asList(PURPLE, BLUE, GREEN, YELLOW, PINK, CYAN));
}

// ----------------------------------------------CELL-------------------------------------------

// Represents a single square of the game board
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  // Main Constructor
  Cell(int x, int y, Color color) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = false;
  }

  // Draws a single cell
  WorldImage drawCell() {
    return new RectangleImage(Utils.CELL_WIDTH, Utils.CELL_WIDTH, OutlineMode.SOLID, this.color);
  }

  // mutates the color of this cell
  // Effect: Changes the color value of this cell to inputted color
  void changeColor(Color color) {
    this.color = color;
  }

  // Updates this cell's neighbors to be flooded if they share the same color as
  // this cell
  // Effect: Replaces the flooded value of non-null, non-flooded cells with
  // true if the neighboring cell shares the same color as this cell.
  void update(Color color) {
    if (this.left != null && !this.left.flooded && this.left.color.equals(color)) {
      this.left.flooded = true;
      this.left.update(color);
    }
    if (this.top != null && !this.top.flooded && this.top.color.equals(color)) {
      this.top.flooded = true;
      this.top.update(color);

    }
    if (this.right != null && !this.right.flooded && this.right.color.equals(color)) {
      this.right.flooded = true;
      this.right.update(color);
    }
    if (this.bottom != null && !this.bottom.flooded && this.bottom.color.equals(color)) {
      this.bottom.flooded = true;
      this.bottom.update(color);
    }
  }

}

//------------------------------------------FLOOD IT WORLD--------------------------------------

// Represents world state for user interaction
class FloodItWorld extends World {

  // -----------------------------FIELDS------------------------------------
  // All the cells of the game
  ArrayList<ArrayList<Cell>> gamebackground;
  // Enhancements:
  int clicks; // number of clicks a user has inputted
  int clicksAllowed; // clicks allowed based on size of game board
  int seconds; // seconds elapsed since start of game

  // -----------------------------CONSTRUCTORS------------------------------
  // Zero Argument Constructor used for games
  FloodItWorld() {
    this(createBoard(boardSize, new Random()), boardSize);
  }

  // -----------------------Constructors used for testing-------------------

  // Two argument constructor which takes in an ArrayList<ArrayList<Cell>> and
  // integer
  // representing the size of a square game board.
  FloodItWorld(ArrayList<ArrayList<Cell>> gamebackground, int boardSize) {
    this.gamebackground = gamebackground;
    this.assignAdjacent(boardSize);
    this.clicks = 0;
    this.clicksAllowed = boardSize + 10;
    this.seconds = 0;
  }

  // One Argument Constructor that takes in Size of Board
  FloodItWorld(int boardSize) {
    this(createBoard(boardSize, new Random()), boardSize);
  }

  // One Argument Constructor that takes in Size of Board and a Random Seed
  FloodItWorld(int boardSize, Random rand) {
    this(createBoard(boardSize, rand), boardSize);
  }

  // -----------------------------CONSTANTS---------------------------------

  // Constants
  static int boardSize = 15; // width and height of game board in cells
  int boardHeight = boardSize * Utils.CELL_WIDTH; // height of board in pixels

  // -----------------------------METHODS-----------------------------------

  // Creates board based on inputted boardSize and a random seed
  static ArrayList<ArrayList<Cell>> createBoard(int boardSize, Random rand) {

    int posnx = Utils.CELL_WIDTH / 2;
    int posny = Utils.CELL_WIDTH / 2;

    ArrayList<ArrayList<Cell>> grid = new ArrayList<>();
    // Rows
    for (int r = 0; r <= boardSize - 1; r++) {
      ArrayList<Cell> rowOfCells = new ArrayList<>();
      for (int c = 0; c <= boardSize - 1; c++) {
        // Cells
        Cell cell = new Cell(posnx, posny, Utils.colors.get(rand.nextInt(Utils.colors.size())));
        rowOfCells.add(cell);
        posnx = posnx + Utils.CELL_WIDTH;
      }

      grid.add(rowOfCells);

      posnx = Utils.CELL_WIDTH / 2;
      posny = posny + Utils.CELL_WIDTH;
    }
    return grid;
  }

  // Assigns adjacent cells for each cell in board
  void assignAdjacent(int boardSize) {
    for (int r = 0; r <= boardSize - 1; r++) {

      ArrayList<Cell> row = this.gamebackground.get(r);

      for (int c = 0; c <= boardSize - 1; c++) {

        Cell cel = row.get(c);

        cel.right = getRight(r, c);
        cel.left = getLeft(r, c);
        cel.top = getTop(r, c);
        cel.bottom = getBottom(r, c);
      }
    }
  }

  // get the right of the current cell given its row and cell number
  Cell getRight(int r, int c) {
    if (c >= gamebackground.size() - 1) {
      return null;
    }
    else {
      return gamebackground.get(r).get(c + 1);
    }

  }

  // get the left cell of the current cell given its row and cell number
  Cell getLeft(int r, int c) {
    if (c <= 0) {
      return null;
    }
    else {
      return gamebackground.get(r).get(c - 1);
    }

  }

  // get the top cell of the current cell given its row and cell number
  Cell getTop(int r, int c) {
    if (r <= 0) {
      return null;
    }
    else {
      return gamebackground.get(r - 1).get(c);
    }
  }

  // get the bottom cell of the current cell given its row and cell number
  Cell getBottom(int r, int c) {
    if (r >= gamebackground.size() - 1) {
      return null;
    }
    else {
      return gamebackground.get(r + 1).get(c);
    }
  }

  // Draws the game board as an image
  public WorldImage drawBoard() {
    WorldImage board = new EmptyImage();
    WorldImage rowImage = new EmptyImage();
    for (int r = 0; r < boardSize; r = r + 1) {
      ArrayList<Cell> row = this.gamebackground.get(r);
      for (int c = 0; c < boardSize; c = c + 1) {
        WorldImage cell = row.get(c).drawCell();
        rowImage = new BesideImage(rowImage, cell);
      }
      board = new AboveImage(board, rowImage);
      rowImage = new EmptyImage();
    }
    return board;
  }

  // Creates a world scene based on the image of the board
  public WorldScene makeScene() {
    WorldScene finalScene = new WorldScene(Utils.CELL_WIDTH * boardSize + 200,
        Utils.CELL_WIDTH * boardSize);
    finalScene.placeImageXY(
        new TextImage("Time: " + this.seconds / 10 + "s", 30, FontStyle.BOLD, Color.BLACK),
        Utils.CELL_WIDTH * boardSize + 100, Utils.CELL_WIDTH * boardSize / 2 - 30);
    finalScene.placeImageXY(this.drawBoard(), Utils.CELL_WIDTH * boardSize / 2,
        Utils.CELL_WIDTH * boardSize / 2);
    finalScene.placeImageXY(
        new TextImage(Integer.toString(this.clicks) + "/" + (Integer.toString(this.clicksAllowed)),
            30, FontStyle.BOLD, Color.BLACK),
        Utils.CELL_WIDTH * boardSize + 100, Utils.CELL_WIDTH * boardSize / 2);
    if (clicks <= clicksAllowed && allFlooded()) {
      finalScene.placeImageXY(new TextImage("YOU WON!", 30, FontStyle.BOLD, Color.GREEN),
          Utils.CELL_WIDTH * boardSize + 100, Utils.CELL_WIDTH * boardSize / 2 - 80);
    }
    else if (clicks >= clicksAllowed && (!allFlooded())) {
      finalScene.placeImageXY(new TextImage("YOU LOST!", 30, FontStyle.BOLD, Color.MAGENTA),
          Utils.CELL_WIDTH * boardSize + 100, Utils.CELL_WIDTH * boardSize / 2 - 80);
    }
    return finalScene;
  }

  // -----------------------------MOUSE-CLICK-----------------------------------------

  // Determines which cell in the board has been clicked
  public Cell clickedCell(Posn pos) {
    Cell clicked = null;
    for (ArrayList<Cell> r : gamebackground) {
      for (Cell c : r) {
        if (c.x - Utils.CELL_WIDTH / 2 <= pos.x && c.x + Utils.CELL_WIDTH / 2 >= pos.x
            && c.y - Utils.CELL_WIDTH / 2 <= pos.y && c.y + Utils.CELL_WIDTH / 2 >= pos.y) {
          clicked = c;
        }
      }
    }
    return clicked;
  }

  // Updates the first cell in the boards color
  // Effect: Changes the first cell to have the color that has been clicked on.
  public void updateOnClick(Cell cell) {
    if (cell != null) {
      Cell c = gamebackground.get(0).get(0);
      c.color = cell.color;
      gamebackground.get(0).set(0, c);
    }
  }

  // To handle mouse clicking
  // Effect: Changes the world state according to which cell was clicked.
  public void onMouseClicked(Posn pos) {
    Cell clickedCell = this.clickedCell(pos);
    Cell firstCell = gamebackground.get(0).get(0);
    firstCell.flooded = true;
    firstCell.update(firstCell.color);

    if (!clickedCell.flooded) {
      clicks++;
    }

    if (clickedCell.equals(this.gamebackground.get(0).get(0))) {
      return;
    }
    // If first cell is clicked, do nothing
    else {
      this.updateOnClick(this.clickedCell(pos));
      updateWorld();
    }
  }

  // ------------------------ WORLD SCENE ----------------------------------------

  // Updates cells in the world to be the color of the clicked cell
  // Effect: mutates all of the cells in the gameboard to have the color
  // of the clicked cell
  public void updateWorld() {
    Cell firstCell = this.gamebackground.get(0).get(0);
    Color colorToFlood = firstCell.color;
    for (int r = 0; r < gamebackground.size(); r++) {
      for (int c = 0; c < gamebackground.size(); c++) {
        Cell cell = gamebackground.get(r).get(c);
        if (cell.flooded) {
          cell.changeColor(colorToFlood);
          cell.update(colorToFlood);
        }
      }
    }
    makeScene();
  }

  // returns whether all the cells in the board have been flooded
  boolean allFlooded() {
    boolean result = true;
    for (ArrayList<Cell> r : gamebackground) {
      for (Cell c : r) {
        result = result && c.flooded;
      }
    }
    return result;
  }

  // --------------------------ON TICK-----------------------------------
  // Updates the world state with each tick
  // Effect: modifies the colors and flooded values for cells
  public void onTick() {
    seconds++;
  }

  // -------------------------ON KEY -----------------------------------
  // Starts a new FloodIt game when the r key is pressed
  // Effect: initializes a new game of FloodIt
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.gamebackground = createBoard(boardSize, new Random());
      this.assignAdjacent(boardSize);
      this.clicks = 0;
      this.clicksAllowed = boardSize + 10;
      this.seconds = 0;
    }
  }

  // --------------------------START GAME ------------------------------
  // Starts a new game of FloodIt
  // Effect: Starts a new bigBang program in which a FloodIt game will run
  public void startGame() {
    FloodItWorld w = new FloodItWorld(boardSize);
    w.bigBang(1200, 800, 0.1);
  }
}

//-----------------------------EXAMPLES---------------------------------

//To represent examples and test of Flood It game
class ExamplesFloodIt {

  Cell c1;
  Cell c2;
  Cell c3;
  Cell c4;
  Cell c5;
  Cell c6;
  Cell c7;
  Cell c8;
  Cell c9;
  Cell c10;

  ArrayList<Cell> row1;
  ArrayList<Cell> row2;
  ArrayList<Cell> row3;
  ArrayList<Cell> row4;

  ArrayList<ArrayList<Cell>> board1;
  ArrayList<ArrayList<Cell>> board2;

  FloodItWorld w1;
  FloodItWorld w2;

  Random randomSeed1;
  Random randomSeed2;
  Random randomSeed3;

  FloodItWorld rsgame1;
  FloodItWorld rsgame2;
  FloodItWorld rsgame3;

  // Examples:

  // Constants
  static int boardSize = 3;
  int BOARD_HEIGHT = boardSize * Utils.CELL_WIDTH;

  FloodItWorld game1 = new FloodItWorld(3);

  void initData() {
    // Cells
    c1 = new Cell(this.BOARD_HEIGHT, Utils.CELL_WIDTH, Utils.BLUE);
    c2 = new Cell(this.BOARD_HEIGHT, Utils.CELL_WIDTH, Utils.PURPLE);
    c3 = new Cell(this.BOARD_HEIGHT, Utils.CELL_WIDTH, Utils.GREEN);

    c4 = new Cell(this.BOARD_HEIGHT, Utils.CELL_WIDTH, Utils.BLUE);
    c5 = new Cell(this.BOARD_HEIGHT, Utils.CELL_WIDTH, Utils.PINK);
    c6 = new Cell(this.BOARD_HEIGHT, Utils.CELL_WIDTH, Utils.CYAN);

    c7 = new Cell(this.BOARD_HEIGHT, Utils.CELL_WIDTH, Utils.BLUE);
    c8 = new Cell(this.BOARD_HEIGHT, Utils.CELL_WIDTH, Utils.PURPLE);
    c9 = new Cell(this.BOARD_HEIGHT, Utils.CELL_WIDTH, Utils.GREEN);
    c10 = new Cell(this.BOARD_HEIGHT, Utils.CELL_WIDTH, Utils.BLUE);

    row1 = new ArrayList<Cell>(Arrays.asList(this.c1, this.c2, this.c3));
    row2 = new ArrayList<Cell>(Arrays.asList(this.c4, this.c5, this.c6));
    row3 = new ArrayList<Cell>(Arrays.asList(this.c7, this.c8, this.c9));
    row4 = new ArrayList<Cell>(Arrays.asList(this.c10));

    board1 = new ArrayList<ArrayList<Cell>>(Arrays.asList(this.row1, this.row2, this.row3));
    board2 = new ArrayList<ArrayList<Cell>>(Arrays.asList(this.row4));

    w1 = new FloodItWorld(this.board1, 3);
    w2 = new FloodItWorld(this.board2, 1);

    randomSeed1 = new Random(1);
    randomSeed2 = new Random(2);
    randomSeed3 = new Random(3);

    rsgame1 = new FloodItWorld(2, this.randomSeed1);
    rsgame2 = new FloodItWorld(3, this.randomSeed2);
    rsgame3 = new FloodItWorld(3, this.randomSeed3);

  }

  // Image Examples

  EmptyImage mt = new EmptyImage();

  RectangleImage pur = new RectangleImage(30, 30, OutlineMode.SOLID, Color.magenta);
  RectangleImage br = new RectangleImage(30, 30, OutlineMode.SOLID, Color.blue);
  RectangleImage gr = new RectangleImage(30, 30, OutlineMode.SOLID, Color.green);
  RectangleImage yr = new RectangleImage(30, 30, OutlineMode.SOLID, Color.yellow);
  RectangleImage pir = new RectangleImage(30, 30, OutlineMode.SOLID, Color.pink);
  RectangleImage cr = new RectangleImage(30, 30, OutlineMode.SOLID, Color.cyan);

  WorldImage rsgame2image = new AboveImage(
      new AboveImage(
          new AboveImage(this.mt,
              new BesideImage(new BesideImage(new BesideImage(this.mt, this.pir), this.pur),
                  this.gr)),
          new BesideImage(new BesideImage(new BesideImage(this.mt, this.br), this.yr), this.pur)),
      new BesideImage(new BesideImage(new BesideImage(this.mt, this.pur), this.yr), this.br));

  WorldImage rsgame3image = new AboveImage(
      new AboveImage(
          new AboveImage(this.mt,
              new BesideImage(new BesideImage(new BesideImage(this.mt, this.gr), this.gr),
                  this.pur)),
          new BesideImage(new BesideImage(new BesideImage(this.mt, this.br), this.pur), this.pur)),
      new BesideImage(new BesideImage(new BesideImage(this.mt, this.yr), this.pir), this.br));

  Posn p1 = new Posn(15, 15);
  Posn p2 = new Posn(40, 50);
  Posn p3 = new Posn(72, 88);
  Posn p4 = new Posn(1000, 1000);

  //-----------------------------------------------------------------------------------------------

  // Tests:

  // Cell Class Tests:
  void testChangeColor(Tester t) {
    this.initData();
    this.c1.changeColor(Color.YELLOW);
    this.c2.changeColor(Color.PINK);
    this.c3.changeColor(Color.CYAN);
    t.checkExpect(this.c1.color, Color.YELLOW);
    t.checkExpect(this.c2.color, Color.PINK);
    t.checkExpect(this.c3.color, Color.CYAN);
  }

  // tests the big bang rendering of game 1
  void testGame(Tester t) {
    game1.startGame();
  }

  // tests the draw board method
  void testDrawBoard(Tester t) {
    initData();
    t.checkExpect(this.rsgame2.drawBoard(), this.rsgame2image);

    t.checkExpect(this.rsgame3.drawBoard(), this.rsgame3image);
  }

  // test the method make scene in flood it class

  void testMakeScene(Tester t) {
    this.initData();
    WorldScene finalScene = new WorldScene((Utils.CELL_WIDTH * boardSize + 200),
        Utils.CELL_WIDTH * boardSize);
    WorldScene finalScene2 = new WorldScene((Utils.CELL_WIDTH * boardSize + 200),
        Utils.CELL_WIDTH * boardSize);

    finalScene.placeImageXY(
        new TextImage("Time: " + this.rsgame2.seconds / 10 + "s", 30, FontStyle.BOLD, Color.BLACK),
        Utils.CELL_WIDTH * boardSize + 100, Utils.CELL_WIDTH * boardSize / 2 - 30);

    finalScene2.placeImageXY(
        new TextImage("Time: " + this.rsgame3.seconds / 10 + "s", 30, FontStyle.BOLD, Color.BLACK),
        Utils.CELL_WIDTH * boardSize + 100, Utils.CELL_WIDTH * boardSize / 2 - 30);

    finalScene.placeImageXY(this.rsgame2image, Utils.CELL_WIDTH * boardSize / 2,
        Utils.CELL_WIDTH * boardSize / 2);

    finalScene2.placeImageXY(this.rsgame3image, Utils.CELL_WIDTH * boardSize / 2,
        Utils.CELL_WIDTH * boardSize / 2);

    finalScene.placeImageXY(
        new TextImage(Integer.toString(this.rsgame2.clicks) + "/"
            + (Integer.toString(this.rsgame2.clicksAllowed)), 30, FontStyle.BOLD, Color.BLACK),
        Utils.CELL_WIDTH * boardSize + 100, Utils.CELL_WIDTH * boardSize / 2);

    finalScene2.placeImageXY(
        new TextImage(Integer.toString(this.rsgame3.clicks) + "/"
            + (Integer.toString(this.rsgame3.clicksAllowed)), 30, FontStyle.BOLD, Color.BLACK),
        Utils.CELL_WIDTH * boardSize + 100, Utils.CELL_WIDTH * boardSize / 2);

    t.checkExpect(rsgame2.makeScene(), finalScene);
    t.checkExpect(rsgame3.makeScene(), finalScene2);

  }

  void testClickedCell(Tester t) {
    initData();
    t.checkExpect(this.game1.clickedCell(this.p1), this.game1.gamebackground.get(0).get(0));
    t.checkExpect(this.game1.clickedCell(this.p2), this.game1.gamebackground.get(1).get(1));
    t.checkExpect(this.game1.clickedCell(this.p3), this.game1.gamebackground.get(2).get(2));
    t.checkExpect(this.game1.clickedCell(this.p4), null);
  }

  void testOnTick(Tester t) {
    initData();
    t.checkExpect(w1.seconds, 0);
    w1.onTick();
    t.checkExpect(w1.seconds, 1);
    w1.onTick();
    w1.onTick();
    t.checkExpect(w1.seconds, 3);
  }

  void testOnKeyEvent(Tester t) {
    initData();
    this.w1.onTick();
    this.w1.onTick();
    this.w1.clickedCell(p3);
    this.w1.onMouseClicked(p3);

    t.checkExpect(this.w1.clicks, 1);
    t.checkExpect(this.w1.seconds, 2);
    t.checkExpect(this.w1.gamebackground, this.board1);

    this.w1.onKeyEvent("a");
    t.checkExpect(this.w1.clicks, 1);
    t.checkExpect(this.w1.seconds, 2);
    t.checkExpect(this.w1.gamebackground.equals(board1), true);

    this.w1.onKeyEvent("r");
    t.checkExpect(this.w1.seconds, 0);
    t.checkExpect(this.w1.clicks, 0);
    t.checkExpect(this.w1.gamebackground.equals(board1), false);
  }

  // test get right
  boolean testGetRight(Tester t) {
    return t.checkExpect(this.game1.getRight(0, 0), this.game1.gamebackground.get(0).get(1))
        && t.checkExpect(this.game1.getRight(1, 0), this.game1.gamebackground.get(1).get(1))
        && t.checkExpect(this.game1.getRight(0, 2), null)
        && t.checkExpect(this.game1.getRight(1, 2), null);

  }

  // test the method get left
  boolean testGetLeft(Tester t) {
    return t.checkExpect(this.game1.getLeft(0, 0), null)
        && t.checkExpect(this.game1.getLeft(1, 0), null)
        && t.checkExpect(this.game1.getLeft(0, 2), this.game1.gamebackground.get(0).get(1))
        && t.checkExpect(this.game1.getLeft(1, 2), this.game1.gamebackground.get(1).get(1));
  }

  // test the method get top
  boolean testGetTop(Tester t) {
    return t.checkExpect(this.game1.getTop(0, 0), null)
        && t.checkExpect(this.game1.getTop(1, 0), this.game1.gamebackground.get(0).get(0))
        && t.checkExpect(this.game1.getTop(0, 2), null)
        && t.checkExpect(this.game1.getTop(1, 2), this.game1.gamebackground.get(0).get(2));
  }

  // test the method get bottom
  boolean testGetBottom(Tester t) {
    return t.checkExpect(this.game1.getBottom(0, 0), this.game1.gamebackground.get(1).get(0))
        && t.checkExpect(this.game1.getBottom(2, 0), null)
        && t.checkExpect(this.game1.getBottom(0, 2), this.game1.gamebackground.get(1).get(2))
        && t.checkExpect(this.game1.getBottom(2, 2), null);
  }

  void testAssignAdjacent(Tester t) {
    // assignAdjacent is called in a line of flooditworld constructor
    this.initData();
    t.checkExpect(this.w1.gamebackground.get(0).get(0).left, null);
    t.checkExpect(this.w1.gamebackground.get(0).get(0).top, null);
    t.checkExpect(this.w1.gamebackground.get(0).get(0).bottom, this.c4); //
    t.checkExpect(this.w1.gamebackground.get(2).get(2).right, null);
    t.checkExpect(this.w1.gamebackground.get(2).get(2).left, this.c8); //
    t.checkExpect(this.w1.gamebackground.get(2).get(2).top, this.c6); //
    t.checkExpect(this.w1.gamebackground.get(2).get(2).bottom, null);
  }

  void testDrawCell(Tester t) {
    this.initData();
    t.checkExpect(this.c1.drawCell(),
        new RectangleImage(Utils.CELL_WIDTH, Utils.CELL_WIDTH, OutlineMode.SOLID, this.c1.color));
    t.checkExpect(this.c2.drawCell(),
        new RectangleImage(Utils.CELL_WIDTH, Utils.CELL_WIDTH, OutlineMode.SOLID, this.c2.color));
    t.checkExpect(this.c3.drawCell(),
        new RectangleImage(Utils.CELL_WIDTH, Utils.CELL_WIDTH, OutlineMode.SOLID, this.c3.color));
  }

  void testUpdate(Tester t) {
    this.initData();
    t.checkExpect(this.c4.flooded, false);
    t.checkExpect(this.c2.flooded, false);
    this.c1.update(c1.color);
    t.checkExpect(this.c4.flooded, true);
    t.checkExpect(this.c2.flooded, false);

  }

  void testUpdateOnClick(Tester t) {
    this.initData();
    t.checkExpect(this.c4.color, Utils.BLUE);
    w1.updateOnClick(this.c4);
    t.checkExpect(this.c1.color, c4.color);

  }

  void testAllFlooded(Tester t) {
    this.initData();
    t.checkExpect(this.w2.allFlooded(), false);
    this.c10.flooded = true;
    t.checkExpect(this.w2.allFlooded(), true);
    t.checkExpect(this.w1.allFlooded(), false);
    this.c1.flooded = true;
    this.c2.flooded = true;
    this.c3.flooded = true;
    this.c4.flooded = true;
    this.c5.flooded = true;
    this.c6.flooded = true;
    this.c7.flooded = true;
    this.c8.flooded = true;
    this.c9.flooded = true;
    t.checkExpect(this.w1.allFlooded(), true);
  }

  //IMPORTANT:
  // IN ORDER FOR THIS TEST TO PASS, THE boardSize CONSTANTS MUST BE 3
  void testUpdateWorld(Tester t) {
    initData();
    t.checkExpect(this.w1.gamebackground.get(0).get(0).color, Utils.BLUE);
    t.checkExpect(this.w1.gamebackground.get(0).get(1).flooded, false);
    t.checkExpect(this.w1.gamebackground.get(0).get(1).color, Utils.PURPLE);
    this.c2.flooded = true;
    this.w1.updateWorld();
    t.checkExpect(this.c1.color, this.c2.color);
  }

  // Tests for onMouseClicked method
  void testOnMouseClicked(Tester t) {
    initData();
    this.w1.makeScene();
    ArrayList<ArrayList<Cell>> copy = this.w1.gamebackground;
    t.checkExpect(copy.get(0).get(0).color, Utils.BLUE);
    t.checkExpect(this.w1.clicks, 0);
    this.w1.onMouseClicked(new Posn(0, 0));
    t.checkExpect(copy.get(0).get(0), this.c1);
    t.checkExpect(this.w1.clicks, 0);
    this.w1.onMouseClicked(new Posn(40, 10));
    t.checkExpect(copy.get(0).get(0).color, this.c2.color);
  }

}