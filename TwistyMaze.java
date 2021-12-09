import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.LinkedList;

//------------------------------------------------------------------------------------------------
// GAME DOCUMENTATION:
// Toggle different maze sizes by entering them as parameters into the game being tested.
// It may be helpful to increase the Utils.CELL_SIZE value for smaller mazes, and vice versa.
// For example: TwistyMaze w = new TwistyMaze(50, 50); --> w.bigBang(1000, 1000, 0.00001);
// To reset the maze, hit "ENTER"

// To enable solving the maze manually, hit "M"
// Use the arrow keys to move throughout the maze (up, down, left, right).
// When you reach the end of the maze, the solution will be animated from finish to start.

// To display the breadth first solution, hit "B"
// To display the depth first solution, hit "D"
// The animation will display the search for the solution from start to finish,
// followed by the solution from finish to start.

// ------------------------------------------------------------------------------------------------
//Utility interface to contain constants used throughout TwistyMaze
interface Utils {
  int EDGE_WEIGHT = 1;
  int CELL_SIZE = 8;
  Color BASE_COLOR = new Color(240, 230, 116);
  Color START_COLOR = new Color(245, 160, 255);
  Color END_COLOR = new Color(165, 145, 255);
  Color VISIT_COLOR = new Color(170, 237, 112);
  Color SOLN_COLOR = new Color(134, 220, 244);
  Color CURRENT_COLOR = Color.WHITE;
}

//------------------------------------------------------------------------------------------------

// to represent a function that will maintain a given hashmap (union and find in hashmap)
class UnionFind {

  HashMap<Integer, Integer> representative;

  // Main constructor
  UnionFind(HashMap<Integer, Integer> representative) {
    this.representative = representative;
  }

  // Union the two values of the hashmap
  void union(int n1, int n2) {
    representative.put(this.find(n1), this.find(n2));
  }

  // return the representative of this nodePosn key
  int find(int given) {
    if (given == (representative.get(given))) {
      return given;
    }
    else {
      return find(representative.get(given));
    }
  }
}

//------------------------------------------------------------------------------------------------
// To represent the individual cells of the board
class Cell {
  Posn posn;
  int value;
  Edge left;
  Edge top;
  Edge right;
  Edge bottom;
  Cell leftCell;
  Cell rightCell;
  Cell topCell;
  Cell bottomCell;
  boolean alreadyVisited;
  boolean solution;
  Color color;

  // Main Constructor
  Cell(Posn posn, int value, Color color) {
    this.posn = posn;
    this.value = value;
    this.left = null;
    this.top = null;
    this.right = null;
    this.bottom = null;
    this.leftCell = null;
    this.rightCell = null;
    this.topCell = null;
    this.bottomCell = null;
    this.alreadyVisited = false;
    this.solution = false;
    this.color = color;
  }

  // Draws this cell on the board
  public WorldImage drawCell() {
    if (this.solution) {
      return new RectangleImage(Utils.CELL_SIZE, Utils.CELL_SIZE, OutlineMode.SOLID,
          Utils.SOLN_COLOR);
    }
    if (this.alreadyVisited) {
      return new RectangleImage(Utils.CELL_SIZE, Utils.CELL_SIZE, OutlineMode.SOLID,
          Utils.VISIT_COLOR);
    }
    else {
      return new RectangleImage(Utils.CELL_SIZE, Utils.CELL_SIZE, OutlineMode.SOLID, this.color);
    }
  }

  // Makes the first cell of the grid the color white
  public void startCell() {
    this.color = Utils.START_COLOR;
  }

  // Makes the last cell of the grid the color black
  public void endCell() {
    this.color = Utils.END_COLOR;
  }

  // Makes the current cell that the user has landed on as cyan
  public void currentCell() {
    this.color = Utils.CURRENT_COLOR;
  }

}

//------------------------------------------------------------------------------------------------
// To represent the individual edges of the board
class Edge {
  Posn posn;
  Cell start;
  Cell end;
  int weight;
  boolean isWall;
  boolean vertical;

  // Main Constructor
  Edge(Posn posn, Cell start, Cell end, int weight, boolean isWall, boolean vertical) {
    this.posn = posn;
    this.start = start;
    this.end = end;
    this.weight = weight;
    this.isWall = isWall;
    this.vertical = vertical;
  }

  // Unweighted Convenience Constructor
  Edge(Posn posn, boolean vertical) {
    this(posn, null, null, 1, true, vertical);
  }

  // Weighted Convenience Constructor
  Edge(Posn posn, int weight, boolean vertical) {
    this(posn, null, null, weight, true, vertical);
  }

  // Draws this edge depending on its orientation and existence
  WorldImage drawEdge() {
    if (isWall && vertical) {
      return new RectangleImage(2, Utils.CELL_SIZE, OutlineMode.SOLID, Color.BLACK);
    }
    if (isWall && !vertical) {
      return new RectangleImage(Utils.CELL_SIZE, 2, OutlineMode.SOLID, Color.BLACK);
    }
    else {
      return new EmptyImage();
    }
  }
}

//------------------------------------------------------------------------------------------------
// To represent the maze game and all of its interactive components
class TwistyMaze extends World {
  int width;
  int height;
  ArrayList<ArrayList<Cell>> board;
  ArrayList<ArrayList<Edge>> edgesH;
  ArrayList<ArrayList<Edge>> edgesV;
  UnionFind unionFind;
  Cell curr;
  ArrayList<Cell> solution;
  ArrayList<Cell> alreadySeen;
  boolean bfs;
  boolean dfs;
  boolean manual;

  // Main Constructor
  TwistyMaze(int width, int height) {
    this.width = width;
    this.height = height;
    this.board = createBoard(width, height);
    this.edgesH = createEdgesH(width, height);
    this.edgesV = createEdgesV(width, height);
    this.unionFind = new UnionFind(this.mapReps(this.board));
    this.assignValues();
    this.curr = this.board.get(0).get(0);
    this.solution = new ArrayList<Cell>();
    this.alreadySeen = new ArrayList<Cell>();
    this.bfs = false;
    this.dfs = false;
    this.manual = false;
  }

//Compares the weights of edges
  class WeightComparator implements Comparator<Edge> {
    // determines which edge comes first based on their weight
    public int compare(Edge e1, Edge e2) {
      return e1.weight - e2.weight;
    }
  }

  // Create the hash map of the game and map all nodes to themselves
  HashMap<Integer, Integer> mapReps(ArrayList<ArrayList<Cell>> cells) {
    HashMap<Integer, Integer> reps = new HashMap<Integer, Integer>();

    for (int index = 0; index < cells.size(); index++) {
      for (int index2 = 0; index2 < cells.get(index).size(); index2 += 1) {
        reps.put(cells.get(index).get(index2).value, cells.get(index).get(index2).value);
      }
    }
    return reps;
  }

  // Creates a board of cells
  static ArrayList<ArrayList<Cell>> createBoard(int width, int height) {
    int posnx = Utils.CELL_SIZE / 2;
    int posny = Utils.CELL_SIZE / 2;
    ArrayList<ArrayList<Cell>> grid = new ArrayList<>();
    int value = 0;
    // Rows
    for (int r = 0; r <= height - 1; r++) {
      ArrayList<Cell> rowOfCells = new ArrayList<>();
      for (int c = 0; c <= width - 1; c++) {
        // Cells
        Cell cell = new Cell(new Posn(posnx, posny), value, Utils.BASE_COLOR);
        rowOfCells.add(cell);
        posnx = posnx + Utils.CELL_SIZE;
        value++;
      }
      grid.add(rowOfCells);
      posnx = Utils.CELL_SIZE / 2;
      posny = posny + Utils.CELL_SIZE;
    }
    return grid;
  }

  // to create the horizontal edges of a maze
  static ArrayList<ArrayList<Edge>> createEdgesH(int width, int height) {
    // Starting X and Y Values
    int posnxH = Utils.CELL_SIZE / 2;
    int posnyH = Utils.CELL_SIZE;

    ArrayList<ArrayList<Edge>> edges = new ArrayList<>();

    // Rows
    for (int r = 0; r <= height - 2; r++) {
      ArrayList<Edge> rowOfEdges = new ArrayList<>();
      for (int e = 0; e <= width - 1; e++) {
        // Horizontal edges
        Edge edgeH = new Edge(new Posn(posnxH, posnyH), new Random().nextInt(1000), false);
        rowOfEdges.add(edgeH);
        posnxH = posnxH + Utils.CELL_SIZE;
      }
      edges.add(rowOfEdges);
      posnxH = Utils.CELL_SIZE / 2;
      posnyH = posnyH + Utils.CELL_SIZE;
    }
    return edges;
  }

  // to create vertical edges of a maze
  static ArrayList<ArrayList<Edge>> createEdgesV(int width, int height) {
    // Starting X and Y Values
    int posnxV = Utils.CELL_SIZE;
    int posnyV = Utils.CELL_SIZE / 2;

    ArrayList<ArrayList<Edge>> edges = new ArrayList<>();

    // Rows
    for (int r = 0; r <= height - 1; r++) {
      ArrayList<Edge> rowOfEdges = new ArrayList<>();
      for (int e = 0; e <= width - 2; e++) {
        // Vertical edges
        Edge edgeH = new Edge(new Posn(posnxV, posnyV), new Random().nextInt(1000), true);
        rowOfEdges.add(edgeH);
        posnxV = posnxV + Utils.CELL_SIZE;
      }
      edges.add(rowOfEdges);
      posnxV = Utils.CELL_SIZE;
      posnyV = posnyV + Utils.CELL_SIZE;
    }
    return edges;
  }

  // to assign the adjacent cells to this cell
  public void assignValues() {
    for (int r = 0; r <= this.height - 1; r++) {
      ArrayList<Cell> row = this.board.get(r);
      for (int c = 0; c <= this.width - 1; c++) {
        Cell cell = row.get(c);
        getLeft(r, c, cell);
        getTop(r, c, cell);
        getRight(r, c, cell);
        getBottom(r, c, cell);
      }
    }
  }

  // get the left Edge of the current cell given its row and cell number
  public void getLeft(int r, int c, Cell cell) {
    if (c <= 0) {
      return;
    }
    else {
      Edge edge = edgesV.get(r).get(c - 1);
      edge.end = this.board.get(r).get(c);
      cell.left = edge;
      cell.leftCell = this.board.get(r).get(c - 1);
    }
  }

  // get the right Edge of the current cell given its row and cell number
  public void getRight(int r, int c, Cell cell) {
    if (c >= this.width - 1) {
      return;
    }
    else {
      Edge edge = edgesV.get(r).get(c);
      edge.start = this.board.get(r).get(c);
      cell.right = edge;
      cell.rightCell = this.board.get(r).get(c + 1);
    }
  }

  // get the top Edge of the current cell given its row and cell number
  public void getTop(int r, int c, Cell cell) {
    if (r <= 0) {
      return;
    }
    else {
      Edge edge = edgesH.get(r - 1).get(c);
      edge.end = this.board.get(r).get(c);
      cell.top = edge;
      cell.topCell = this.board.get(r - 1).get(c);
    }
  }

  // get the bottom Edge of the current cell given its row and cell number
  public void getBottom(int r, int c, Cell cell) {
    if (r >= this.height - 1) {
      return;
    }
    else {
      Edge edge = edgesH.get(r).get(c);
      edge.start = this.board.get(r).get(c);
      cell.bottom = edge;
      cell.bottomCell = this.board.get(r + 1).get(c);
    }
  }

  public ArrayList<Edge> createWorklist() {
    ArrayList<Edge> worklist = new ArrayList<Edge>();
    // Add Horizontal Edges to Work List
    for (ArrayList<Edge> rowH : this.edgesH) {
      for (Edge edge : rowH) {
        worklist.add(edge);
      }
    }
    // Add Vertical Edges to Work List
    for (ArrayList<Edge> rowV : this.edgesV) {
      for (Edge edge : rowV) {
        worklist.add(edge);
      }
    }
    return worklist;
  }

  // Uses Kruskal's Algorithm to create a randomized maze
  public void createMaze() {
    // Work list on all Edges in the Game
    ArrayList<Edge> worklist = this.createWorklist();
    worklist.sort(new WeightComparator());

    while (worklist.size() > 0) {
      int randomNumber = new Random().nextInt(worklist.size());
      Edge edge = worklist.get(randomNumber);
      int key1 = this.unionFind.find(edge.start.value);
      int key2 = this.unionFind.find(edge.end.value);
      if (this.unionFind.find(key1) == (this.unionFind.find(key2))) {
        // the cells are connected -> discard this edge and the wall stays
        worklist.remove(randomNumber);
      }
      else {
        // the cells are not connected -> knock the wall down
        edge.isWall = false;
        this.unionFind.union(key1, key2);
        worklist.remove(randomNumber);
      }
    }
  }

//Determines if the user has reached the last cell in the game
  public boolean endReached() {
    return this.curr.equals(this.board.get(this.height - 1).get(this.width - 1));
  }

  void bfs(Cell from, Cell to) {
    searchHelp(from, to, new Queue<Cell>());
  }

  void dfs(Cell from, Cell to) {
    searchHelp(from, to, new Stack<Cell>());
  }

  void searchHelp(Cell from, Cell to, ICollection<Cell> worklist) {
    HashMap<Cell, Edge> cameFromEdge = new HashMap<Cell, Edge>();

//Initialize the work list with the from vertex
    worklist.add(from);
//As long as the work list isn't empty...
    while (!(worklist.isEmpty())) {
      Cell next = worklist.remove();
      if (alreadySeen.contains(next)) {
        // do nothing: we've already seen this one
      }
      else if (next.equals(to)) {
        worklist.clear();
        reconstruct(cameFromEdge, next); // Success, the end of the maze has been reached.
      }
      else {
        // add all the neighbors of next to the worklist for further processing
        Cell leftNeighbor = next.leftCell;
        Cell rightNeighbor = next.rightCell;
        Cell topNeighbor = next.topCell;
        Cell bottomNeighbor = next.bottomCell;
        if (!(leftNeighbor == null) && !(next.left.isWall)) {
          worklist.add(leftNeighbor);
          if (!(cameFromEdge.containsKey(leftNeighbor))) {
            cameFromEdge.put(leftNeighbor, leftNeighbor.right);
          }
        }
        if (!(rightNeighbor == null) && !(next.right.isWall)) {
          worklist.add(rightNeighbor);
          if (!(cameFromEdge.containsKey(rightNeighbor))) {
            cameFromEdge.put(rightNeighbor, rightNeighbor.left);
          }
        }
        if (!(topNeighbor == null) && !(next.top.isWall)) {
          worklist.add(topNeighbor);
          if (!(cameFromEdge.containsKey(topNeighbor))) {
            cameFromEdge.put(topNeighbor, topNeighbor.bottom);
          }
        }
        if (!(bottomNeighbor == null) && !(next.bottom.isWall)) {
          worklist.add(bottomNeighbor);
          if (!(cameFromEdge.containsKey(bottomNeighbor))) {
            cameFromEdge.put(bottomNeighbor, bottomNeighbor.top);
          }
        }
      }
      // add next to alreadySeen, since we're done with it
      alreadySeen.add(next);
    }
  }

  // creates the solution path by backtracking through the HashMap
  void reconstruct(HashMap<Cell, Edge> cameFromEdge, Cell next) {
    if (next.equals(this.board.get(0).get(0))) {
      this.solution.add(next);
    }
    else if ((cameFromEdge.get(next).start.equals(next.leftCell))
        || cameFromEdge.get(next).start.equals(next.topCell)) {
      this.solution.add(next);
      reconstruct(cameFromEdge, cameFromEdge.get(next).start);
    }
    else {
      this.solution.add(next);
      reconstruct(cameFromEdge, cameFromEdge.get(next).end);
    }
  }

//------------------------------------------------------------------------------------------------

  // Creates World Scene featuring the cells of the grid
  public WorldScene makeScene() {
    this.board.get(0).get(0).startCell();
    this.board.get(height - 1).get(width - 1).endCell();
    this.curr.currentCell();

    WorldScene finalScene = new WorldScene(Utils.CELL_SIZE * this.width,
        Utils.CELL_SIZE * this.height + 200);
    // Print Cells
    for (int r = 0; r < this.height; r = r + 1) {
      ArrayList<Cell> row = this.board.get(r);
      for (int c = 0; c < this.width; c = c + 1) {
        Cell cell = row.get(c);
        WorldImage cellImage = row.get(c).drawCell();
        finalScene.placeImageXY(cellImage, cell.posn.x, cell.posn.y);
      }
    }
    // Print Horizontal Edges
    for (int r = 0; r <= this.edgesH.size() - 1; r++) {
      ArrayList<Edge> row = this.edgesH.get(r);
      for (int c = 0; c <= row.size() - 1; c++) {
        Edge edgeH = row.get(c);
        WorldImage edgeHImage = row.get(c).drawEdge();
        finalScene.placeImageXY(edgeHImage, edgeH.posn.x, edgeH.posn.y);
      }
    }
    // Print Vertical Edges
    for (int a = 0; a <= this.edgesV.size() - 1; a++) {
      ArrayList<Edge> rowV = this.edgesV.get(a);
      for (int b = 0; b <= rowV.size() - 1; b++) {
        Edge edgeV = rowV.get(b);
        WorldImage edgeVImage = rowV.get(b).drawEdge();
        finalScene.placeImageXY(edgeVImage, edgeV.posn.x, edgeV.posn.y);
      }
    }
    // User Message
    if (!this.endReached() && !this.dfs && !this.bfs) {
      finalScene.placeImageXY(new TextImage("FIND YOUR WAY OUT!", 20, FontStyle.BOLD, Color.BLACK),
          Utils.CELL_SIZE * this.width / 2, Utils.CELL_SIZE * this.height + 100);
    }
    if (this.dfs || this.bfs) {
      finalScene.placeImageXY(
          new TextImage("HERE IS THE SOLUTION!", 20, FontStyle.BOLD, Utils.START_COLOR),
          Utils.CELL_SIZE * this.width / 2, Utils.CELL_SIZE * this.height + 100);
    }
    if (this.endReached()) {
      finalScene.placeImageXY(
          new TextImage("MAZE COMPLETE!", 20, FontStyle.BOLD, Utils.START_COLOR),
          Utils.CELL_SIZE * this.width / 2, Utils.CELL_SIZE * this.height + 100);
    }
    return finalScene;
  }

  // Starts a new FloodIt game when the r key is pressed
  // Effect: initializes a new game of FloodIt
  public void onKeyEvent(String key) {
    if (key.equals("enter")) {
      this.board = createBoard(width, height);
      this.edgesH = createEdgesH(width, height);
      this.edgesV = createEdgesV(width, height);
      this.unionFind = new UnionFind(this.mapReps(this.board));
      this.assignValues();
      this.curr = this.board.get(0).get(0);
      this.bfs = false;
      this.dfs = false;
      this.manual = false;
      this.solution = new ArrayList<Cell>();
      this.alreadySeen = new ArrayList<Cell>();
      this.createMaze();
    }
    if (key.equals("m")) {
      this.manual = true;
    }
    if (key.equals("left") && !(this.curr.left == null) && !(this.curr.left.isWall)
        && !(this.endReached()) && this.manual) {
      this.curr.color = Utils.VISIT_COLOR;
      Cell newCurr = this.curr.leftCell;
      if (!(newCurr == null))
        this.curr = newCurr;
    }
    if (key.equals("right") && !(this.curr.right == null) && !(this.curr.right.isWall)
        && !(this.endReached()) && this.manual) {
      this.curr.color = Utils.VISIT_COLOR;
      Cell newCurr = this.curr.rightCell;
      if (!(newCurr == null))
        this.curr = newCurr;
    }
    if (key.equals("up") && !(this.curr.top == null) && !(this.curr.top.isWall)
        && !(this.endReached()) && this.manual) {
      this.curr.color = Utils.VISIT_COLOR;
      Cell newCurr = this.curr.topCell;
      if (!(newCurr == null))
        this.curr = newCurr;
    }
    if (key.equals("down") && !(this.curr.bottom == null) && !(this.curr.bottom.isWall)
        && !(this.endReached()) && this.manual) {
      this.curr.color = Utils.VISIT_COLOR;
      Cell newCurr = this.curr.bottomCell;
      if (!(newCurr == null))
        this.curr = newCurr;
    }

    Cell first = this.board.get(0).get(0);
    Cell last = this.board.get(this.height - 1).get(this.width - 1);

    if (key.equals("d")) {
      this.dfs = true;
      this.dfs(first, last);
    }
    if (key.equals("b")) {
      this.bfs = true;
      this.bfs(first, last);
    }
  }

//updates the world state for every tick
  public void onTick() {
    if (!this.alreadySeen.isEmpty() && !this.manual) {
      Cell curr = this.alreadySeen.remove(0);
      curr.alreadyVisited = true;
    }
    else if (!this.solution.isEmpty() && !this.manual) {
      Cell curr = this.solution.remove(0);
      curr.solution = true;
    }
    else if (this.manual && this.endReached()) {
      this.bfs(this.board.get(0).get(0), this.board.get(this.height - 1).get(this.width - 1));
      if (!this.solution.isEmpty()) {
        Cell curr = this.solution.remove(0);
        curr.solution = true;
      }
    }
  }
}

//------------------------------------------------------------------------------------------------

//Represents a mutable collection of items
interface ICollection<T> {

//Is this collection empty?
  boolean isEmpty();

//EFFECT: adds the item to the collection with the given priority
  void add(T item);

//Returns the first item of the collection
//EFFECT: removes that first item
  T remove();

  void clear();
}

//------------------------------------------------------------------------------------------------

class Stack<T> implements ICollection<T> {
  LinkedList<T> contents;

  Stack() {
    this.contents = new LinkedList<T>();
  }

  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  public T remove() {
    return this.contents.removeFirst();
  }

  public void add(T item) {
    this.contents.addFirst(item);
  }

  public void clear() {
    this.contents.clear();
  }
}

//------------------------------------------------------------------------------------------------

class Queue<T> implements ICollection<T> {
  LinkedList<T> contents;

  Queue() {
    this.contents = new LinkedList<T>();
  }

  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  public T remove() {
    return this.contents.removeFirst();
  }

  public void add(T item) {
    this.contents.addLast(item);
  }

  public void clear() {
    this.contents.clear();
  }
}

//------------------------------------------------------------------------------------------------

// Examples
class ExamplesMaze {
  Cell c1;
  Cell c2;
  Cell c3;
  Edge e1;
  Edge e2;
  Edge e3;
  Edge e4;
  TwistyMaze game1;
  TwistyMaze game2;

  TwistyMaze game3;
  TwistyMaze game4;
  TwistyMaze game5;

  WorldImage wr = new RectangleImage(Utils.CELL_SIZE, Utils.CELL_SIZE, OutlineMode.SOLID,
      Color.white);
  WorldImage br = new RectangleImage(Utils.CELL_SIZE, Utils.CELL_SIZE, OutlineMode.SOLID,
      Color.black);
  WorldImage yr = new RectangleImage(Utils.CELL_SIZE, Utils.CELL_SIZE, OutlineMode.SOLID,
      Color.yellow);
  WorldImage hl = new RectangleImage(Utils.CELL_SIZE, 2, OutlineMode.SOLID, Color.BLACK);
  WorldImage vl = new RectangleImage(2, Utils.CELL_SIZE, OutlineMode.SOLID, Color.BLACK);
  WorldImage c = new CircleImage(2, OutlineMode.SOLID, Color.CYAN);

  // initialize data
  void initData() {
    game1 = new TwistyMaze(80, 80);
    game1.createMaze();
    game2 = new TwistyMaze(3, 3);
    game2.createMaze();
    game3 = new TwistyMaze(1, 4);
    game4 = new TwistyMaze(2, 3);
    game5 = new TwistyMaze(2, 2);
    c1 = new Cell(new Posn(0, 0), 1, Color.BLUE);
    c2 = new Cell(new Posn(50, 50), 1, Color.GREEN);
    c3 = new Cell(new Posn(100, 100), 1, Color.BLACK);
    e1 = new Edge(new Posn(10, 10), this.c1, this.c2, 1, true, true);
    e2 = new Edge(new Posn(10, 10), this.c1, this.c2, 1, true, false);
    e3 = new Edge(new Posn(10, 10), this.c1, this.c2, 1, false, true);
    e4 = new Edge(new Posn(10, 10), this.c1, this.c2, 1, false, false);
  }

  void testCreateMaze(Tester t) {
    initData();
    TwistyMaze w = this.game1;
    w.bigBang(1000, 1000, 0.00001);
  }

  
    void testDrawCell(Tester t) {
    initData();
    t.checkExpect(this.c1.drawCell(),
    new RectangleImage(Utils.CELL_SIZE, Utils.CELL_SIZE, OutlineMode.SOLID,
    Color.BLUE));
    t.checkExpect(this.c2.drawCell(),
    new RectangleImage(Utils.CELL_SIZE, Utils.CELL_SIZE, OutlineMode.SOLID,
    Color.GREEN));
    t.checkExpect(this.c3.drawCell(),
    new RectangleImage(Utils.CELL_SIZE, Utils.CELL_SIZE, OutlineMode.SOLID,
    Color.BLACK));
    }
    
    void testStartAndEndCell(Tester t) {
    initData();
    t.checkExpect(this.game1.board.get(0).get(0).color, Color.YELLOW);
    t.checkExpect(this.game1.board.get(99).get(99).color, Color.YELLOW);
    this.game1.board.get(0).get(0).startCell();
    this.game1.board.get(99).get(99).endCell();
    t.checkExpect(this.game1.board.get(0).get(0).color, Color.WHITE);
    t.checkExpect(this.game1.board.get(99).get(99).color, Color.BLACK);
   }
    
    void testDrawEdge(Tester t) {
    initData();
    t.checkExpect(this.e1.drawEdge(),
    new RectangleImage(2, Utils.CELL_SIZE, OutlineMode.SOLID, Color.BLACK));
    t.checkExpect(this.e2.drawEdge(),
    new RectangleImage(Utils.CELL_SIZE, 2, OutlineMode.SOLID, Color.BLACK));
    t.checkExpect(this.e3.drawEdge(), new EmptyImage());
    t.checkExpect(this.e4.drawEdge(), new EmptyImage());
    }
    
    void testMakeScene(Tester t) {
    this.game3.board.get(0).get(0).startCell();
    this.game4.board.get(0).get(0).startCell();
    this.game3.board.get(game3.height - 1).get(game3.width - 1).endCell();
    this.game4.board.get(game4.height - 1).get(game4.width - 1).endCell();
    
    WorldScene finalScene = new WorldScene(Utils.CELL_SIZE * game3.width,
   Utils.CELL_SIZE * game3.height);
    WorldScene finalScene2 = new WorldScene(Utils.CELL_SIZE * game4.width,
    Utils.CELL_SIZE * game4.height);
    finalScene.placeImageXY(br, 4, 16);
    finalScene.placeImageXY(wr, 4, 4);
    finalScene.placeImageXY(yr, 4, 12);
    finalScene.placeImageXY(yr, 4, 20);
    finalScene.placeImageXY(br, 4, 28);
    finalScene.placeImageXY(hl, 4, 8);
    finalScene.placeImageXY(hl, 4, 16);
    finalScene.placeImageXY(hl, 4, 24);
    
    finalScene2.placeImageXY(br, 8, 12);
    finalScene2.placeImageXY(wr, 4, 4);
    finalScene2.placeImageXY(yr, 12, 4);
    finalScene2.placeImageXY(yr, 4, 12);
    finalScene2.placeImageXY(yr, 12, 12);
    finalScene2.placeImageXY(yr, 4, 20);
    finalScene2.placeImageXY(br, 12, 20);
    finalScene2.placeImageXY(hl, 4, 8);
    finalScene2.placeImageXY(hl, 12, 8);
    finalScene2.placeImageXY(hl, 4, 16);
    finalScene2.placeImageXY(hl, 12, 16);
    finalScene2.placeImageXY(vl, 8, 4);
    finalScene2.placeImageXY(vl, 8, 12);
    finalScene2.placeImageXY(vl, 8, 20);
    
    t.checkExpect(game3.makeScene(), finalScene);
    t.checkExpect(game4.makeScene(), finalScene2);
    }
    
    void testAssignValues(Tester t) {
    // assignValues is called in a line of TwistyMaze constructor
    this.initData();
    t.checkExpect(game5.board.get(0).get(0).left, null);
    t.checkExpect(game5.board.get(0).get(0).top, null);
    
    t.checkExpect(game5.board.get(0).get(0).right, new Edge(new Posn(8, 4),
    true));
    t.checkExpect(game5.board.get(0).get(0).bottom, new Edge(new Posn(4, 8),
    false));
    
    t.checkExpect(game5.board.get(1).get(1).left, new Edge(new Posn(8, 12),
    true));
    t.checkExpect(game5.board.get(1).get(1).top, new Edge(new Posn(12, 8),
   false));
    
    t.checkExpect(game5.board.get(1).get(1).right, null);
    t.checkExpect(game5.board.get(1).get(1).bottom, null);
    }
    
    
    void testGetTop(Tester t) {
    this.initData();
    t.checkExpect(game5.getTop(0, 0), null);
    t.checkExpect(game5.getTop(-2, 0), null);
    t.checkExpect(game5.getTop(1, 0), new Edge(new Posn(4, 8), false));
    t.checkExpect(game5.getTop(1, 0).end, new Cell(new Posn(4, 12), 2,
    Color.YELLOW));
    }
    
    void testGetBottom(Tester t) {
    this.initData();
    t.checkExpect(game5.getBottom(4, 0), null);
    t.checkExpect(game5.getBottom(2, 0), null);
    t.checkExpect(game5.getBottom(0, 0), new Edge(new Posn(4, 8), false));
    t.checkExpect(game5.getBottom(0, 0).start, new Cell(new Posn(4, 4), 0,
    Color.YELLOW));
    }
    
    void testGetLeft(Tester t) {
    this.initData();
    t.checkExpect(game5.getLeft(1, 0), null);
    t.checkExpect(game5.getLeft(1, -2), null);
    t.checkExpect(game5.getLeft(0, 1), new Edge(new Posn(8, 4), true));
    t.checkExpect(game5.getLeft(0, 1).end, new Cell(new Posn(12, 12), 0,
    Color.YELLOW));
    }
    
    void testGetRight(Tester t) {
    this.initData();
    t.checkExpect(game5.getRight(0, 4), null);
    t.checkExpect(game5.getRight(0, 2), null);
    t.checkExpect(game5.getRight(0, 0), new Edge(new Posn(8, 4), true));
    t.checkExpect(game5.getRight(0, 0).start, new Cell(new Posn(4, 4), 0,
    Color.YELLOW));
    }
   }
