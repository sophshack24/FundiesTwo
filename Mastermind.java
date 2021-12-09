import tester.Tester;

import javalib.worldimages.*; // images, like RectangleImage or OverlayImages
import javalib.funworld.*; // the abstract World class and the big-bang library
import javalib.worldcanvas.WorldCanvas;
import java.awt.Color;
import java.util.Random;

//--------------------------TO DO LIST--------------------------------------------------------------------------------------------------------------
/* Data Design: Check Expects
 * Random Sequence Generator with and without Duplicates
 * InExact and Exact Match Checker
 * public WorldScene lastScene(String msg);
 * Abstractions if Needed
 * Current Score
 * Past Guesses
 * Duplicate boolean
 */
//-----------------------------GAME-----------------------------------------------------------------------------------------------------------------

// To represent the game of Mastermind 
class Mastermind extends World {
  static Utils u = new Utils();
  int DIAMETER = 60;
  int SPACING = 10;

  boolean duplicate; // are duplicate allowed in the correct sequence?
  int length; // length of correct sequence
  int guesses; // number of guesses
  ILoMyColor colors; // list of possible colors
  Random rand;
  ILoMyColor currentguesses;
  int previousguesses;
  int guessesleft;
  ILoMyColor correctsequence;
  ILoMyColor correctsequenceaccum;
  ILoGuess guesseslist;
  ILoScore scoreslist;

  // Constructor for Use in Testing, with a Specified Random Object
  Mastermind(boolean duplicate, int length, int guesses, ILoMyColor colors, Random rand) {
    this.duplicate = u.duplicateException(duplicate, length, colors.listLength(),
        "Invalid Duplicate");
    this.length = u.checkRange(length, 0, "Invalid Length: " + Integer.toString(length));
    this.guesses = u.checkRange(guesses, 0, "Invalid # of Guesses: " + Integer.toString(guesses));
    this.colors = u.checkLength(colors, 0, "Invalid List of Colors");
    this.rand = rand; // with seed
    this.currentguesses = new MtLoMyColor();
    this.previousguesses = 0;
    this.guessesleft = this.guesses;
    this.correctsequence = this.colors.randColorList(length, rand);
    this.correctsequenceaccum = this.correctsequence;
    this.guesseslist = new MtLoGuess();
    this.scoreslist = new MtLoScore();
  }

  // Constructor for Use in "Real" Games
  Mastermind(boolean duplicate, int length, int guesses, ILoMyColor colors) {
    this(duplicate, length, guesses, colors, new Random(), new MtLoMyColor(), 0, guesses,
        new MtLoMyColor(), new MtLoGuess(), new MtLoScore());
  }

  // Constructor for Use in Big Bang Methods
  Mastermind(boolean duplicate, int length, int guesses, ILoMyColor colors, Random rand,
      ILoMyColor currentguesses, int previousguesses, int guessesleft,
      ILoMyColor correctsequenceaccum, ILoGuess guesseslist, ILoScore scoreslist) {
    this.duplicate = u.duplicateException(duplicate, length, colors.listLength(),
        "Invalid Duplicate");
    this.length = u.checkRange(length, 0, "Invalid Length: " + Integer.toString(length));
    this.guesses = u.checkRange(guesses, 0, "Invalid # of Guesses: " + Integer.toString(guesses));
    this.colors = u.checkLength(colors, 0, "Invalid List of Colors");
    this.rand = rand; // with seed
    this.currentguesses = currentguesses;
    this.previousguesses = previousguesses;
    this.guessesleft = guessesleft;
    this.correctsequence = this.colors.randColorList(length, rand);
    this.correctsequenceaccum = correctsequenceaccum;
    this.guesseslist = guesseslist;
    this.scoreslist = scoreslist;
  }

  // Creates a list of Circles based upon the length of the sequence to be guessed
  public ILoCircle lengthToList() {
    return this.lengthToListHelper(this.length, new MtLoCircle());
  }

  // Helps create a list of Circles by using an accumulator for the number of
  // circles
  // need to be created and an accumulator for the list so far
  public ILoCircle lengthToListHelper(int length, ILoCircle listSoFar) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: length ... int listSoFar ... ILoCircle
     * 
     * Methods on Parameters: this.listSoFar.listLength() ... int
     * this.listSoFar.drawColorCircles() ... WorldImage
     * this.listSoFar.drawEmptyCircles() ... WorldImage
     * this.listSoFar.drawEmptyGameBoard(int guesses) ... WorldImage
     * this.listSoFar.drawEmptyGameBoardHelper(WorldImage imageSoFar, int guesses)
     * ... WorldImage
     */
    if (length == 0) {
      return listSoFar;
    }
    else {
      return new ConsLoCircle(new Circle(Color.black),
          this.lengthToListHelper(length - 1, listSoFar));
    }
  }

  // Draws an empty gameboard based upon the number of guesses a player is allowed
  public WorldImage drawEmptyGuessesBoard() {
    return this.lengthToList().drawEmptyGameBoard(this.guesses);
  }

  // Draws the randomized correct sequence (generated through the inputed list of
  // colors)
  public WorldImage drawCorrectSequence() {
    return this.correctsequence.colorsToCircles().drawColorCircles();
  }

  // Draws the current guess at the appropriate position on this Mastermind
  // gameboard
  public WorldImage drawCurGuessOffset() {
    return new OverlayOffsetImage(this.currentguesses.drawCurGuess(),
        (((this.length - this.currentguesses.listLength()) * (this.DIAMETER + this.SPACING)) / 2)
            + (this.DIAMETER + this.SPACING),
        this.previousguesses * (this.DIAMETER + this.SPACING), this.drawGameBoard());
  }

  // Draws the exact and inexact match score on this Mastermind gameboard
  public WorldImage drawFindMatches() {
    return new OverlayOffsetImage(this.correctsequence.findMatches(this.currentguesses).drawPosn(),
        (Math.max(this.length, this.colors.listLength()) * (this.DIAMETER + this.SPACING)) / 2,
        this.previousguesses * (this.DIAMETER + this.SPACING), this.drawGameBoard());
  }

  // Creates the World scene of the BigBang (the entire Mastermind gameboard with
  // all of its elements)
  public WorldScene makeScene() {
    return new WorldScene( // Dimensions:
        Math.max((this.length + 2) * (DIAMETER + SPACING),
            (this.colors.listLength()) * (DIAMETER + SPACING)),
        (3 + this.guesses) * (DIAMETER + SPACING))
            .placeImageXY(this.drawGameBoard(), ((this.length + 2) * (DIAMETER + SPACING)) / 2, // GameBoard
                ((this.guesses + 3) * (DIAMETER + SPACING)) / 2)
            .placeImageXY(this.currentguesses.drawCurGuess(), // Current Guesses
                (this.currentguesses.listLength() * (this.DIAMETER + this.SPACING) / 2),
                (1 + this.guessesleft) * (this.DIAMETER + this.SPACING)
                    + (this.DIAMETER + this.SPACING) / 2)
            .placeImageXY(this.guesseslist.drawGuessList(),
                (this.length * (this.DIAMETER + this.SPACING) / 2),
                ((this.guesses + 1) * (this.DIAMETER + this.SPACING)
                    + (this.DIAMETER + this.SPACING) / 2) + (this.DIAMETER + this.SPACING) / 2
                    - (this.guesseslist.listLength() * (this.DIAMETER + this.SPACING)) / 2)
            .placeImageXY(this.scoreslist.drawScoreList(), // Score
                (this.length + 1) * (this.DIAMETER + this.SPACING),
                ((this.guesses + 1) * (this.DIAMETER + this.SPACING)
                    + (this.DIAMETER + this.SPACING) / 2) + (this.DIAMETER + this.SPACING) / 2
                    - (this.scoreslist.listLength() * (this.DIAMETER + this.SPACING)) / 2);
  }

  public WorldScene lastScene() {
    return new WorldScene( // Dimensions:
        Math.max((this.length + 2) * (DIAMETER + SPACING),
            (this.colors.listLength()) * (DIAMETER + SPACING)),
        (3 + this.guesses) * (DIAMETER + SPACING))
            .placeImageXY(this.drawGameBoard(), ((this.length + 2) * (DIAMETER + SPACING)) / 2, // GameBoard
                ((this.guesses + 3) * (DIAMETER + SPACING)) / 2)
            .placeImageXY(this.currentguesses.drawCurGuess(), // Current Guesses
                (this.currentguesses.listLength() * (this.DIAMETER + this.SPACING) / 2),
                (1 + this.guessesleft) * (this.DIAMETER + this.SPACING)
                    + (this.DIAMETER + this.SPACING) / 2)
            .placeImageXY(this.guesseslist.drawGuessList(),
                (this.length * (this.DIAMETER + this.SPACING) / 2),
                ((this.guesses + 1) * (this.DIAMETER + this.SPACING)
                    + (this.DIAMETER + this.SPACING) / 2) + (this.DIAMETER + this.SPACING) / 2
                    - (this.guesseslist.listLength() * (this.DIAMETER + this.SPACING)) / 2)
            .placeImageXY(this.scoreslist.drawScoreList(), // Score
                (this.length + 1) * (this.DIAMETER + this.SPACING),
                ((this.guesses + 1) * (this.DIAMETER + this.SPACING)
                    + (this.DIAMETER + this.SPACING) / 2) + (this.DIAMETER + this.SPACING) / 2
                    - (this.scoreslist.listLength() * (this.DIAMETER + this.SPACING)) / 2)
            .placeImageXY(this.drawLose(), ((this.length + 2) * (this.DIAMETER + this.SPACING))
                - (this.DIAMETER + this.SPACING), (this.DIAMETER + this.SPACING) / 2);
  }

  // Draws the message output by the game when the user wins
  public WorldImage drawWin() {
    return new OverlayOffsetImage(
        new OverlayImage(new TextImage("You Win!", 70, Color.black),
            new RectangleImage((this.DIAMETER + this.SPACING) * 2, (this.DIAMETER + this.SPACING),
                OutlineMode.SOLID, Color.gray)),
        ((this.DIAMETER + this.SPACING) * (this.length + 2)) / 2,
        (this.guesses * (this.DIAMETER + this.SPACING)) / 2, drawGameBoard());
  }

  // Draws the message output by the game when the user loses
  public WorldImage drawLose() {
    return new OverlayImage(new TextImage("You Lose", 35, Color.black),
        new RectangleImage((this.DIAMETER + this.SPACING) * 2, (this.DIAMETER + this.SPACING),
            OutlineMode.SOLID, Color.gray));
  }

  // Draws the elements of the Mastermind gameboard that are not manipulated
  public WorldImage drawGameBoard() {
    return new AboveImage(
        new BesideImage(new AboveImage(this.drawCoverCorrect(), (this.drawEmptyGuessesBoard())),
            this.drawGuessOutcomeBox()),
        this.colors.drawAvailableColors());
  }

  // Draws the black rectangle that resides at top of the Mastermind gameboard
  // where the correct sequence
  // should be
  public WorldImage drawCoverCorrect() {
    return new RectangleImage((this.DIAMETER + this.SPACING) * this.length,
        this.DIAMETER + this.SPACING, OutlineMode.SOLID, Color.BLACK);
  }

  // Draws the space on the Mastermind gameboard allocated to the exact and
  // inexact match score
  public WorldImage drawGuessOutcomeBox() {
    return new RectangleImage((this.DIAMETER + this.SPACING) * 2,
        (this.DIAMETER + this.SPACING) * (this.guesses + 2), OutlineMode.SOLID, Color.GRAY);
  }

  // ------------------------------------BIGBANG-------------------------------------------------------------------

  // BigBang Key handler for backspace, enter, and numbers 1-9
  public World onKeyEvent(String key) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: key ... String
     * 
     * Methods on Parameters: this.key.equals(String key) ... boolean
     * this.key.contains(String range) ... boolean
     */
    if (key.equals("backspace")) {
      return this.backspaceKey();
    }
    if (key.equals("enter")) {
      return this.enterKey();
    }
    if ("123456789".contains(key)) {
      return this.numberKeys(key);
    }
    else {
      return new Mastermind(this.duplicate, this.length, this.guesses, this.colors, this.rand,
          this.currentguesses, this.previousguesses, this.guessesleft, this.correctsequence,
          this.guesseslist, this.scoreslist);
    }
  }

  // Creates a World based upon the user's number key input with an updated
  // current guess
  World numberKeys(String key) {/*
                                 * METHOD TEMPLATE: refer to class template
                                 * 
                                 * Parameters: key ... String
                                 * 
                                 * Methods on Parameters: Integer.parseInt(this.key) ... int
                                 * 
                                 */
    if (Integer.parseInt(key) <= this.colors.listLength()
        && !(this.currentguesses.listLength() > this.length - 1)) {
      return new Mastermind(this.duplicate, this.length, this.guesses, this.colors, this.rand,
          this.currentguesses
              .addToList(this.colors.assignValues().findMatch((Integer.parseInt(key)) - 1)),
          this.previousguesses, this.guessesleft, this.correctsequenceaccum, this.guesseslist,
          this.scoreslist);
    }
    else {
      return new Mastermind(this.duplicate, this.length, this.guesses, this.colors, this.rand,
          this.currentguesses, this.previousguesses, this.guessesleft, this.correctsequence,
          this.guesseslist, this.scoreslist);
    }
  }

  // Creates a World based upon the user's enter key input that either ends the
  // game or updates guesses
  World enterKey() {
    if (this.currentguesses.sameLoColor(this.correctsequence)) {
      return this.endOfWorld("You win!");
    }
    if (this.guessesleft == 0) {
      return this.endOfWorld("You lose.");
    }
    if (this.currentguesses.listLength() < this.length) {
      return new Mastermind(this.duplicate, this.length, this.guesses, this.colors, this.rand,
          this.currentguesses, this.previousguesses, this.guessesleft, this.correctsequence,
          this.guesseslist, this.scoreslist);
    }
    else {
      return new Mastermind(this.duplicate, this.length, this.guesses, this.colors, this.rand,
          new MtLoMyColor(), // .completedRow(),
          this.previousguesses + 1, this.guessesleft - 1, this.correctsequence,
          this.guesseslist.addToGuessList(this.currentguesses),
          this.scoreslist.addToScores(this.correctsequence.findMatches(this.currentguesses)));
    }
  }

  // Creates a World based upon the user's backspace key input with an updated
  // current guess
  World backspaceKey() {
    if (currentguesses.listLength() == 0) {
      return new Mastermind(this.duplicate, this.length, this.guesses, this.colors, this.rand,
          this.currentguesses, this.previousguesses, this.guessesleft, this.correctsequence,
          this.guesseslist, this.scoreslist);
    }
    else {
      return new Mastermind(this.duplicate, this.length, this.guesses, this.colors, this.rand,
          this.currentguesses.backspaceKey(), this.previousguesses, this.guessesleft,
          this.correctsequence, this.guesseslist, this.scoreslist);
    }

  }
}

//----------------------------UTILS------------------------------------------------------------------------------------------------------------------

//Acts as a container for functions implemented in constructors 
class Utils {

// Determines if the given value is in a range of numbers and therefore, valid
  int checkRange(int val, int max, String msg) {
    if (val >= max) {
      return val;
    }
    else {
      throw new IllegalArgumentException(msg);
    }
  }

  // Determines if certain Mastermind inputs are in an invalid configuration
  boolean duplicateException(boolean duplicate, int length, int length2, String msg) {
    if (duplicate == false && length > length2) {
      throw new IllegalArgumentException(msg);
    }
    else {
      return duplicate;
    }
  }

  // Determines if the given list is greater than length and therefore, valid
  ILoMyColor checkLength(ILoMyColor list, int length, String msg) {
    if (list.listLength() > length) {
      return list;
    }
    else {
      throw new IllegalArgumentException(msg);
    }
  }
}

//------------------------------LIST OF GUESSES-----------------------------------------------------------------------------------------------------------

//To represent a list of guesses(ILoMyColor)
interface ILoGuess {

  ConsLoGuess addToGuessList(ILoMyColor currentguess);

  WorldImage drawGuessList();

  int listLength();

}

class MtLoGuess implements ILoGuess {
  MtLoGuess() {
  }

  public ConsLoGuess addToGuessList(ILoMyColor currentguess) {
    return new ConsLoGuess(currentguess, new MtLoGuess());
  }

  public WorldImage drawGuessList() {
    return new EmptyImage();
  }

  public int listLength() {
    return 0;
  }
}

class ConsLoGuess implements ILoGuess {
  ILoMyColor first;
  ILoGuess rest;

  ConsLoGuess(ILoMyColor first, ILoGuess rest) {
    this.first = first;
    this.rest = rest;
  }

  public ConsLoGuess addToGuessList(ILoMyColor currentguess) {
    return new ConsLoGuess(currentguess, this);
  }

  public WorldImage drawGuessList() {
    return new AboveImage(this.first.drawCurGuess(), this.rest.drawGuessList());
  }

  public int listLength() {
    return 1 + this.rest.listLength();
  }
}

//------------------------------LIST OF SCORES --------------------------------------------------------------------------------------------------------

//To represent a list of guesses(ILoMyColor)
interface ILoScore {

  ILoScore addToScores(MyPosn score);

  WorldImage drawScoreList();

  int listLength();

}

class MtLoScore implements ILoScore {
  MtLoScore() {
  }

  public ConsLoScore addToScores(MyPosn score) {
    return new ConsLoScore(score, new MtLoScore());
  }

  public WorldImage drawScoreList() {
    return new EmptyImage();
  }

  public int listLength() {
    return 0;
  }

}

class ConsLoScore implements ILoScore {
  MyPosn first;
  ILoScore rest;

  ConsLoScore(MyPosn first, ILoScore rest) {
    this.first = first;
    this.rest = rest;
  }

  public ConsLoScore addToScores(MyPosn score) {
    return new ConsLoScore(score, new MtLoScore());
  }

  public WorldImage drawScoreList() {
    return new AboveImage(this.first.drawPosn(), this.rest.drawScoreList());
  }

  public int listLength() {
    return 1 + this.rest.listLength();
  }

}

// -----------------------------CIRCLES-----------------------------------------------------------------------------------------------------------------

// To represent a list of Circles
interface ILoCircle {

  // Computes how many MyColors are in this list
  int listLength();

  // Draws this list of Circles as filled circles beside one another
  WorldImage drawColorCircles();

  // Draws this list of Circles as outlined circles beside one another
  WorldImage drawEmptyCircles();

  // Draws the empty guesses of the Mastermind gameboard
  WorldImage drawEmptyGameBoard(int guesses);

  // Helps draw the Mastermind gameboard using an accumulator that builds the
  // empty guesses based upon the number of guesses allowed
  WorldImage drawEmptyGameBoardHelper(WorldImage imageSoFar, int guesses);

}

// To represent an empty list of Circles
class MtLoCircle implements ILoCircle {
  MtLoCircle() {
  }

  /*
   * CLASS TEMPLATE: Fields:
   * 
   * Methods: this.listLength() ... int this.drawColorCircles() ... WorldImage
   * this.drawEmptyCircles() ... WorldImage this.drawEmptyGameBoard(int guesses)
   * ... WorldImage this.drawEmptyGameBoardHelper(WorldImage imageSoFar, int
   * guesses) ... WorldImage
   * 
   * Methods on Fields:
   * 
   */

  public int listLength() {
    return 0;
  }

  public WorldImage drawColorCircles() {
    return new EmptyImage();
  }

  public WorldImage drawEmptyCircles() {
    return new EmptyImage();
  }

  public WorldImage drawEmptyGameBoard(int guesses) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: guesses ... int
     * 
     * Methods on Parameters:
     */
    return new EmptyImage();
  }

  public WorldImage drawEmptyGameBoardHelper(WorldImage imageSoFar, int guesses) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: guesses ... int imageSoFar ... WorldImage
     * 
     * Methods on Parameters:
     */
    return new EmptyImage();
  }

}

// To represent a non-empty list of Circles
class ConsLoCircle implements ILoCircle {
  Circle first;
  ILoCircle rest;

  // Main Constructor
  ConsLoCircle(Circle first, ILoCircle rest) {
    this.first = first;
    this.rest = rest;
  }

  /*
   * CLASS TEMPLATE: Fields: this.first ... Circle this.rest ... ILoCircle
   * 
   * Methods: this.listLength() ... int this.drawColorCircles() ... WorldImage
   * this.drawEmptyCircles() ... WorldImage this.drawEmptyGameBoard(int guesses)
   * ... WorldImage this.drawEmptyGameBoardHelper(WorldImage imageSoFar, int
   * guesses) ... WorldImage
   * 
   * Methods on Fields: this.first.drawCircleOutline() ... WorldImage
   * this.first.drawCircleFill() ... WorldImage this.first.offsetCircle(int
   * listPosition) Circle
   * 
   * this.rest.listLength() ... int this.rest.drawColorCircles() ... WorldImage
   * this.rest.drawEmptyCircles() ... WorldImage this.rest.drawEmptyGameBoard(int
   * guesses) ... WorldImage this.rest.drawEmptyGameBoardHelper(WorldImage
   * imageSoFar, int guesses) ... WorldImage
   */

  // Creates a new position based upon the length of the list of Circles // DO WE
  // STILL NEED THIS??????????
  public MyPosn imagePosn() {
    return new MyPosn(this.listLength() * 70, 35);
  }

  public int listLength() {
    return 1 + this.rest.listLength();
  }

  public WorldImage drawColorCircles() {
    return new BesideImage(this.first.drawCircleFill(), this.rest.drawColorCircles());
  }

  public WorldImage drawEmptyCircles() {
    return new BesideImage(this.first.drawCircleOutline(), this.rest.drawEmptyCircles());
  }

  public WorldImage drawEmptyGameBoard(int guesses) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: guesses ... int
     * 
     * Methods on Parameters:
     */
    final WorldImage ROWOFCIRCLES = this.drawEmptyCircles();
    return this.drawEmptyGameBoardHelper(ROWOFCIRCLES, guesses); // call number of guesses times
  }

  public WorldImage drawEmptyGameBoardHelper(WorldImage imageSoFar, int guesses) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: guesses ... int imageSoFar ... WorldImage
     * 
     * Methods on Parameters:
     */
    if (guesses == 0) {
      return imageSoFar;
    }
    else {
      return drawEmptyGameBoardHelper(new AboveImage(this.drawEmptyCircles(), imageSoFar),
          guesses - 1);
    }
  }
}

// To represent a customizable circle
class Circle {
  static MyPosn POSN1 = new MyPosn(40, 40);

  int radius;
  MyPosn position;
  Color color;

  // Main Constructor
  Circle(int radius, MyPosn position, Color color) {
    this.radius = radius;
    this.position = position;
    this.color = color;
  }

  // 1 parameter Convenience Constructor
  Circle(Color color) {
    this(30, POSN1, color);
  }

  // 2 parameter Convenience Constructor
  Circle(MyPosn position, Color color) {
    this(30, position, color);
  }

  /*
   * CLASS TEMPLATE: Fields: this.radius ... int this.position ... MyPosn
   * this.color ... Color
   * 
   * Methods: this.drawCircleOutline() ... WorldImage this.drawCircleFill() ...
   * WorldImage this.offsetCircle(int listPosition) Circle
   * 
   * Methods on Fields: this.position.offsetPosn(MyPosn p) ... MyPosn
   * this.position.placeImageOnScene(WorldScene scene, WorldImage image) ...
   * WorldImage this.position.addPosn(MyPosn p) ... MyPosn
   * this.position.drawPosn() ... WorldImage
   * 
   * this.color.colorToCircle() ... Circle this.color.changeNum(int value) ...
   * MyColor this.color.compare(int val) ... boolean
   */

  // Draws an outline of this circle on a gray background
  WorldImage drawCircleOutline() {
    return new OverlayImage(new CircleImage(this.radius, OutlineMode.OUTLINE, Color.black),
        (new RectangleImage((this.radius * 2) + 10, (this.radius * 2) + 10, OutlineMode.SOLID,
            Color.gray)));
  }

  // Draws this filled circle on a gray background
  WorldImage drawCircleFill() {
    return new OverlayImage(new CircleImage(this.radius, OutlineMode.SOLID, this.color),
        (new RectangleImage((this.radius * 2) + 10, (this.radius * 2) + 10, OutlineMode.SOLID,
            Color.gray)));
  }

  // Offsets the position of this circle based upon the given list position
  Circle offsetCircle(int listPosition) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: listPosition ... int
     * 
     * Methods on Parameters:
     */
    return new Circle(this.position.offsetPosn(listPosition), this.color);
  }

}

//-----------------------------COLORS-----------------------------------------------------------------------------------------------------------------

// To represent a list of MyColors
interface ILoMyColor {

  // finds if a guess is an exact match
  public int exact(ILoMyColor guess);

  // helps determine if a guess is an exact match
  public int exactHelp(MyColor firstofseq, ILoMyColor restofseq);

  // Computes how many MyColors are in this list
  int listLength();

  // Converts this list of MyColors into a list of Circles
  ILoCircle colorsToCircles();

  // Assigns identifiable numbers to each MyColor in this list
  ILoMyColor assignValues();

  // Uses an accumulating identifier that assigns helps assignValues
  // assign an increasing integer to each MyColor in the list
  ILoMyColor assignValuesHelper(int acc);

  // Finds a MyColor in this list whose identifier corresponds with the given
  // integer
  MyColor findMatch(int randomNum);

  // Draws the given list of MyColors at the appropriate place on the game board
  WorldImage drawAvailableColors();

  // Adds the given MyColor to this list of MyColors
  ILoMyColor addToList(MyColor colortoadd);

  // Removes a MyColor from this list of MyColors
  ILoMyColor removefromList();

  // Finds exact and inexact matches in this list from the given guesses
  MyPosn findMatches(ILoMyColor guess);

  // Helps find matches by using an accumulating MyPosn that keeps track of
  // exact and inexact matches
  MyPosn findMatchHelper(ILoMyColor guess, MyPosn posn);

  // Determines if this first MyColor is equivalent to the given MyColor
  boolean isExact(MyColor first);

  // Helps determine if the rest of the list has exact matches
  ILoMyColor isExactHelper(MyColor color);

  // Determines if this list contains an inexact match with the given guess
  boolean inExact(MyColor first);

  // Determines if two list of MyColors contain the same elements in the same
  // order
  boolean sameLoColor(ILoMyColor that);

  // Determines if this list is equivalent to that empty list
  boolean sameMtLoColor(MtLoMyColor that);

  // Determines if this list is equivalent to that non-empty list
  boolean sameConsLoColor(ConsLoMyColor that);

  // Draws the Mastermind current guess on the gameboard
  WorldImage drawCurGuess();

  // Called when the backspace key is hit to remove a current guess
  ILoMyColor backspaceKey();

  // Generates a random list of MyColors of a given length
  ILoMyColor randColorList(int length, Random rand);
}

// To represent an empty list of MyColors
class MtLoMyColor implements ILoMyColor {
  MtLoMyColor() {
  }

  /*
   * CLASS TEMPLATE: Fields:
   * 
   * Methods: this.listLength() ... int this.colorsToCircles() ... ILoCircle
   * this.assignValues() ... ILoMyColor this.assignValuesHelper(int acc) ...
   * ILoMyColor this.findMatch(int randomNum) ... MyColor
   * this.drawAvailableColors() ... WorldImage this.addToList(MyColor colortoadd)
   * ... ILoMyColor this.removefromList() ... ILoMyColor
   * this.findMatches(ILoMyColor guess) ... MyPosn this.findMatchHelper(ILoMyColor
   * guess, MyPosn posn) ... MyPosn this.isExact(MyColor first) ... boolean
   * this.inExact(MyColor first) ... boolean this.isExactHelper(MyColor color) ...
   * ILoMyColor this.sameLoColor(ILoMyColor that) ... boolean
   * this.sameMtLoColor(MtLoMyColor that) ... boolean
   * this.sameConsLoColor(MtLoMyColor that) ... boolean this.drawCurGuess() ...
   * WorldImage this.backspaceKey() ... ILoMyColor this.randColorList(int length,
   * Random rand) ... ILoMyColor
   * 
   * Methods on Fields:
   */

  public int listLength() {
    return 0;
  }

  public ILoCircle colorsToCircles() {
    return new MtLoCircle();
  }

  public ILoMyColor assignValues() {
    return new MtLoMyColor();
  }

  public ILoMyColor assignValuesHelper(int acc) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: acc ... int
     * 
     * Methods on Parameters:
     */
    return new MtLoMyColor();
  }

  public MyColor findMatch(int randomNum) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: randomNum ... int
     * 
     * Methods on Parameters:
     */
    return null;
  }

  public WorldImage drawAvailableColors() {
    return new EmptyImage();
  }

  public ILoMyColor addToList(MyColor colortoadd) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: colortoadd ... MyColor
     * 
     * Methods on Parameters: this.colortoadd.colorToCircle() ... Circle
     * this.colortoadd.changeNum(int value) ... MyColor this.colortoadd.compare(int
     * val) ... boolean
     */
    return new ConsLoMyColor(colortoadd, new MtLoMyColor());
  }

  public ILoMyColor removefromList() {
    return this;
  }

  public MyPosn findMatchHelper(ILoMyColor guess, MyPosn posn) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: guess ... ILoMyColor posn ... MyPosn
     * 
     * Methods on Parameters: this.guess.listLength() ... int
     * this.guess.colorsToCircles() ... ILoCircle this.guess.assignValues() ...
     * ILoMyColor this.guess.assignValuesHelper(int acc) ... ILoMyColor
     * this.guess.findMatch(int randomNum) ... MyColor
     * this.guess.drawAvailableColors() ... WorldImage this.guess.addToList(MyColor
     * colortoadd) ... ILoMyColor this.guess.removefromList() ... ILoMyColor
     * this.guess.findMatches(ILoMyColor guess) ... MyPosn
     * this.guess.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
     * this.guess.isExact(MyColor first) ... boolean this.guess.inExact(MyColor
     * first) ... boolean this.guess.isExactHelper(MyColor color) ... ILoMyColor
     * this.guess.sameLoColor(ILoMyColor that) ... boolean
     * this.guess.sameMtLoColor(MtLoMyColor that) ... boolean
     * this.guess.sameConsLoColor(MtLoMyColor that) ... boolean
     * this.guess.drawCurGuess() ... WorldImage this.guess.backspaceKey() ...
     * ILoMyColor this.guess.randColorList(int length, Random rand) ... ILoMyColor
     * 
     * this.posn.offsetPosn(MyPosn p) ... MyPosn
     * this.posn.placeImageOnScene(WorldScene scene, WorldImage image) ...
     * WorldImage this.posn.addPosn(MyPosn p) ... MyPosn this.posn.drawPosn() ...
     * WorldImage
     */
    return new MyPosn(0, 0);
  }

  public boolean isExact(MyColor first) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: first ... MyColor
     * 
     * Methods on Parameters: this.first.colorToCircle() ... Circle
     * this.first.changeNum(int value) ... MyColor this.first.compare(int val) ...
     * boolean
     */
    return false;
  }

  public boolean inExact(MyColor first) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: first ... MyColor
     * 
     * Methods on Parameters: this.first.colorToCircle() ... Circle
     * this.first.changeNum(int value) ... MyColor this.first.compare(int val) ...
     * boolean
     */
    return false;
  }

  public ILoMyColor isExactHelper(MyColor color) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: color ... MyColor
     * 
     * Methods on Parameters: this.color.colorToCircle() ... Circle
     * this.color.changeNum(int value) ... MyColor this.color.compare(int val) ...
     * boolean
     */
    return new MtLoMyColor();
  }

  public boolean sameLoColor(ILoMyColor that) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: that ... ILoMyColor
     * 
     * Methods on Parameters: this.that.listLength() ... int
     * this.that.colorsToCircles() ... ILoCircle this.that.assignValues() ...
     * ILoMyColor this.that.assignValuesHelper(int acc) ... ILoMyColor
     * this.that.findMatch(int randomNum) ... MyColor
     * this.that.drawAvailableColors() ... WorldImage this.that.addToList(MyColor
     * colortoadd) ... ILoMyColor this.that.removefromList() ... ILoMyColor
     * this.that.findMatches(ILoMyColor guess) ... MyPosn
     * this.that.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
     * this.that.isExact(MyColor first) ... boolean this.that.inExact(MyColor first)
     * ... boolean this.that.isExactHelper(MyColor color) ... ILoMyColor
     * this.that.sameLoColor(ILoMyColor that) ... boolean
     * this.that.sameMtLoColor(MtLoMyColor that) ... boolean
     * this.that.sameConsLoColor(MtLoMyColor that) ... boolean
     * this.that.drawCurGuess() ... WorldImage this.that.backspaceKey() ...
     * ILoMyColor this.that.randColorList(int length, Random rand) ... ILoMyColor
     */
    return that.sameMtLoColor(this);
  }

  public boolean sameMtLoColor(MtLoMyColor that) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: that ... MtLoMyColor
     * 
     * Methods on Parameters: this.that.listLength() ... int
     * this.that.colorsToCircles() ... ILoCircle this.that.assignValues() ...
     * ILoMyColor this.that.assignValuesHelper(int acc) ... ILoMyColor
     * this.that.findMatch(int randomNum) ... MyColor
     * this.that.drawAvailableColors() ... WorldImage this.that.addToList(MyColor
     * colortoadd) ... ILoMyColor this.that.removefromList() ... ILoMyColor
     * this.that.findMatches(ILoMyColor guess) ... MyPosn
     * this.that.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
     * this.that.isExact(MyColor first) ... boolean this.that.inExact(MyColor first)
     * ... boolean this.that.isExactHelper(MyColor color) ... ILoMyColor
     * this.that.sameLoColor(ILoMyColor that) ... boolean
     * this.that.sameMtLoColor(MtLoMyColor that) ... boolean
     * this.that.sameConsLoColor(MtLoMyColor that) ... boolean
     * this.that.drawCurGuess() ... WorldImage this.that.backspaceKey() ...
     * ILoMyColor this.that.randColorList(int length, Random rand) ... ILoMyColor
     */
    return true;
  }

  public boolean sameConsLoColor(ConsLoMyColor that) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: that ... ConsLoMyColor
     * 
     * Methods on Parameters: this.that.listLength() ... int
     * this.that.colorsToCircles() ... ILoCircle this.that.assignValues() ...
     * ILoMyColor this.that.assignValuesHelper(int acc) ... ILoMyColor
     * this.that.findMatch(int randomNum) ... MyColor
     * this.that.drawAvailableColors() ... WorldImage this.that.addToList(MyColor
     * colortoadd) ... ILoMyColor this.that.removefromList() ... ILoMyColor
     * this.that.findMatches(ILoMyColor guess) ... MyPosn
     * this.that.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
     * this.that.isExact(MyColor first) ... boolean this.that.inExact(MyColor first)
     * ... boolean this.that.isExactHelper(MyColor color) ... ILoMyColor
     * this.that.sameLoColor(ILoMyColor that) ... boolean
     * this.that.sameMtLoColor(MtLoMyColor that) ... boolean
     * this.that.sameConsLoColor(MtLoMyColor that) ... boolean
     * this.that.drawCurGuess() ... WorldImage this.that.backspaceKey() ...
     * ILoMyColor this.that.randColorList(int length, Random rand) ... ILoMyColor
     */
    return false;
  }

  public WorldImage drawCurGuess() {
    return new EmptyImage();
  }

  public ILoMyColor backspaceKey() {
    return this.removefromList();
  }

  public ILoMyColor randColorList(int length, Random rand) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: length ... int rand ... Random
     * 
     * Methods on Parameters: this.rand.nextInt(int value) ... int
     */
    return this;
  }

  public ILoMyColor randColorListHelper(int length, ILoMyColor acc, int originallength,
      Random rand) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: length ... int acc ... ILoMyColor originallength ... int
     * 
     * Methods on Parameters: this.acc.listLength() ... int
     * this.acc.colorsToCircles() ... ILoCircle this.acc.assignValues() ...
     * ILoMyColor this.acc.assignValuesHelper(int acc) ... ILoMyColor
     * this.acc.findMatch(int randomNum) ... MyColor this.acc.drawAvailableColors()
     * ... WorldImage this.acc.addToList(MyColor colortoadd) ... ILoMyColor
     * this.acc.removefromList() ... ILoMyColor this.acc.findMatches(ILoMyColor
     * guess) ... MyPosn this.acc.findMatchHelper(ILoMyColor guess, MyPosn posn) ...
     * MyPosn this.acc.isExact(MyColor first) ... boolean this.acc.inExact(MyColor
     * first) ... boolean this.acc.isExactHelper(MyColor color) ... ILoMyColor
     * this.acc.sameLoColor(ILoMyColor that) ... boolean
     * this.acc.sameMtLoColor(MtLoMyColor that) ... boolean
     * this.acc.sameConsLoColor(MtLoMyColor that) ... boolean
     * this.acc.drawCurGuess() ... WorldImage this.acc.backspaceKey() ... ILoMyColor
     * this.acc.randColorList(int length, Random rand) ... ILoMyColor
     */
    return acc;
  }

  public int exact(ILoMyColor guess) {
    return 0;
  }

  public int exactHelp(MyColor firstofseq, ILoMyColor restofseq) {
    return 0;
  }

}

// To represent a non-empty list of MyColors
class ConsLoMyColor implements ILoMyColor {
  MyColor first;
  ILoMyColor rest;

  // Main Constructor
  ConsLoMyColor(MyColor first, ILoMyColor rest) {
    this.first = first;
    this.rest = rest;
  }

  // Color to MyColor Constructor
  ConsLoMyColor(Color first, ILoMyColor rest) {
    this(new MyColor(first, 0), rest);
  }

  /*
   * CLASS TEMPLATE: Fields: this.first ... MyColor this.rest ... ILoMyColor
   * 
   * Methods: this.listLength() ... int this.colorsToCircles() ... ILoCircle
   * this.assignValues() ... ILoMyColor this.assignValuesHelper(int acc) ...
   * ILoMyColor this.findMatch(int randomNum) ... MyColor
   * this.drawAvailableColors() ... WorldImage this.addToList(MyColor colortoadd)
   * ... ILoMyColor this.removefromList() ... ILoMyColor
   * this.findMatches(ILoMyColor guess) ... MyPosn this.findMatchHelper(ILoMyColor
   * guess, MyPosn posn) ... MyPosn this.isExact(MyColor first) ... boolean
   * this.inExact(MyColor first) ... boolean this.isExactHelper(MyColor color) ...
   * ILoMyColor this.sameLoColor(ILoMyColor that) ... boolean
   * this.sameMtLoColor(MtLoMyColor that) ... boolean
   * this.sameConsLoColor(MtLoMyColor that) ... boolean this.drawCurGuess() ...
   * WorldImage this.backspaceKey() ... ILoMyColor this.randColorList(int length,
   * Random rand) ... ILoMyColor
   * 
   * Methods on Fields: this.first.colorToCircle() ... Circle
   * this.first.changeNum(int value) ... MyColor this.first.compare(int val) ...
   * boolean
   * 
   * this.rest.listLength() ... int this.rest.colorsToCircles() ... ILoCircle
   * this.rest.assignValues() ... ILoMyColor this.rest.assignValuesHelper(int acc)
   * ... ILoMyColor this.rest.findMatch(int randomNum) ... MyColor
   * this.rest.drawAvailableColors() ... WorldImage this.rest.addToList(MyColor
   * colortoadd) ... ILoMyColor this.rest.removefromList() ... ILoMyColor
   * this.rest.findMatches(ILoMyColor guess) ... MyPosn
   * this.rest.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
   * this.rest.isExact(MyColor first) ... boolean this.rest.inExact(MyColor first)
   * ... boolean this.rest.isExactHelper(MyColor color) ... ILoMyColor
   * this.rest.sameLoColor(ILoMyColor that) ... boolean
   * this.rest.sameMtLoColor(MtLoMyColor that) ... boolean
   * this.rest.sameConsLoColor(MtLoMyColor that) ... boolean
   * this.rest.drawCurGuess() ... WorldImage this.rest.backspaceKey() ...
   * ILoMyColor this.rest.randColorList(int length, Random rand) ... ILoMyColor
   */

  public int listLength() {
    return 1 + this.rest.listLength();
  }

  public ILoCircle colorsToCircles() {
    return new ConsLoCircle(this.first.colorToCircle(), this.rest.colorsToCircles());
  }

  public ILoMyColor assignValues() {
    return this.assignValuesHelper(0);
  }

  public MyPosn findMatches(ILoMyColor guess) {
    return new MyPosn(this.exact(guess), 0);
  }

  public int exact(ILoMyColor guess) {
    return guess.exactHelp(this.first, this.rest);
  }

  public int exactHelp(MyColor firstofseq, ILoMyColor restofseq) {
    if (firstofseq.compareColor(this.first)) { // guess
      return 1 + restofseq.exact(this.rest);
    }
    else {
      return 0 + restofseq.exact(this.rest);
    }
  }

  public ILoMyColor assignValuesHelper(int acc) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: acc ... int
     * 
     * Methods on Parameters:
     */
    return new ConsLoMyColor(this.first.changeNum(acc), this.rest.assignValuesHelper(acc + 1));
  }

  public MyColor findMatch(int randomNum) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: randomNum ... int
     * 
     * Methods on Parameters:
     */
    if (this.first.compare(randomNum)) {
      return this.first;
    }
    else {
      return this.rest.findMatch(randomNum);
    }
  }

  public WorldImage drawAvailableColors() {
    return this.colorsToCircles().drawColorCircles();
  }

  public ILoMyColor addToList(MyColor colortoadd) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: colortoadd ... MyColor
     * 
     * Methods on Parameters: this.colortoadd.colorToCircle() ... Circle
     * this.colortoadd.changeNum(int value) ... MyColor this.colortoadd.compare(int
     * val) ... boolean
     */
    return new ConsLoMyColor(colortoadd, this);
  }

  public ILoMyColor removefromList() {
    return this.rest;
  }

  public boolean isExact(MyColor color) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: color ... MyColor
     * 
     * Methods on Parameters: this.color.colorToCircle() ... Circle
     * this.color.changeNum(int value) ... MyColor this.color.compare(int val) ...
     * boolean
     */
    return (this.first == color);
  }

  public ILoMyColor isExactHelper(MyColor color) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: color ... MyColor
     * 
     * Methods on Parameters: this.color.colorToCircle() ... Circle
     * this.color.changeNum(int value) ... MyColor this.color.compare(int val) ...
     * boolean
     */
    if (this.isExact(color)) {
      return this.rest;
    }
    else {
      return this;
    }
  }

  public boolean inExact(MyColor color) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: color ... MyColor
     * 
     * Methods on Parameters: this.color.colorToCircle() ... Circle
     * this.color.changeNum(int value) ... MyColor this.color.compare(int val) ...
     * boolean
     */
    return this.rest.isExact(color);
  }

  public MyPosn findMatchHelper(ILoMyColor guess, MyPosn posn) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: guess ... ILoMyColor posn ... MyPosn
     * 
     * Methods on Parameters: this.guess.listLength() ... int
     * this.guess.colorsToCircles() ... ILoCircle this.guess.assignValues() ...
     * ILoMyColor this.guess.assignValuesHelper(int acc) ... ILoMyColor
     * this.guess.findMatch(int randomNum) ... MyColor
     * this.guess.drawAvailableColors() ... WorldImage this.guess.addToList(MyColor
     * colortoadd) ... ILoMyColor this.guess.removefromList() ... ILoMyColor
     * this.guess.findMatches(ILoMyColor guess) ... MyPosn
     * this.guess.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
     * this.guess.isExact(MyColor first) ... boolean this.guess.inExact(MyColor
     * first) ... boolean this.guess.isExactHelper(MyColor color) ... ILoMyColor
     * this.guess.sameLoColor(ILoMyColor that) ... boolean
     * this.guess.sameMtLoColor(MtLoMyColor that) ... boolean
     * this.guess.sameConsLoColor(MtLoMyColor that) ... boolean
     * this.guess.drawCurGuess() ... WorldImage this.guess.backspaceKey() ...
     * ILoMyColor this.guess.randColorList(int length, Random rand) ... ILoMyColor
     * 
     * this.posn.offsetPosn(MyPosn p) ... MyPosn
     * this.posn.placeImageOnScene(WorldScene scene, WorldImage image) ...
     * WorldImage this.posn.addPosn(MyPosn p) ... MyPosn this.posn.drawPosn() ...
     * WorldImage
     */
    if (guess.isExact(this.first)) {
      return new MyPosn(1, 0)
          .addPosn(this.rest.findMatchHelper(guess.isExactHelper(this.first), posn));
    }
    else {
      if (guess.inExact(this.first)) {
        return posn.addPosn(new MyPosn(0, 1));
      }
      else {
        return posn;
      }
    }
  }

  public boolean sameLoColor(ILoMyColor that) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: that ... ILoMyColor
     * 
     * Methods on Parameters: this.that.listLength() ... int
     * this.that.colorsToCircles() ... ILoCircle this.that.assignValues() ...
     * ILoMyColor this.that.assignValuesHelper(int acc) ... ILoMyColor
     * this.that.findMatch(int randomNum) ... MyColor
     * this.that.drawAvailableColors() ... WorldImage this.that.addToList(MyColor
     * colortoadd) ... ILoMyColor this.that.removefromList() ... ILoMyColor
     * this.that.findMatches(ILoMyColor guess) ... MyPosn
     * this.that.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
     * this.that.isExact(MyColor first) ... boolean this.that.inExact(MyColor first)
     * ... boolean this.that.isExactHelper(MyColor color) ... ILoMyColor
     * this.that.sameLoColor(ILoMyColor that) ... boolean
     * this.that.sameMtLoColor(MtLoMyColor that) ... boolean
     * this.that.sameConsLoColor(MtLoMyColor that) ... boolean
     * this.that.drawCurGuess() ... WorldImage this.that.backspaceKey() ...
     * ILoMyColor this.that.randColorList(int length, Random rand) ... ILoMyColor
     */
    return that.sameConsLoColor(this);
  }

  public boolean sameMtLoColor(MtLoMyColor that) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: that ... MtLoMyColor
     * 
     * Methods on Parameters: this.that.listLength() ... int
     * this.that.colorsToCircles() ... ILoCircle this.that.assignValues() ...
     * ILoMyColor this.that.assignValuesHelper(int acc) ... ILoMyColor
     * this.that.findMatch(int randomNum) ... MyColor
     * this.that.drawAvailableColors() ... WorldImage this.that.addToList(MyColor
     * colortoadd) ... ILoMyColor this.that.removefromList() ... ILoMyColor
     * this.that.findMatches(ILoMyColor guess) ... MyPosn
     * this.that.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
     * this.that.isExact(MyColor first) ... boolean this.that.inExact(MyColor first)
     * ... boolean this.that.isExactHelper(MyColor color) ... ILoMyColor
     * this.that.sameLoColor(ILoMyColor that) ... boolean
     * this.that.sameMtLoColor(MtLoMyColor that) ... boolean
     * this.that.sameConsLoColor(MtLoMyColor that) ... boolean
     * this.that.drawCurGuess() ... WorldImage this.that.backspaceKey() ...
     * ILoMyColor this.that.randColorList(int length, Random rand) ... ILoMyColor
     */
    return false;
  }

  public boolean sameConsLoColor(ConsLoMyColor that) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: that ... ConsLoMyColor
     * 
     * Methods on Parameters: this.that.listLength() ... int
     * this.that.colorsToCircles() ... ILoCircle this.that.assignValues() ...
     * ILoMyColor this.that.assignValuesHelper(int acc) ... ILoMyColor
     * this.that.findMatch(int randomNum) ... MyColor
     * this.that.drawAvailableColors() ... WorldImage this.that.addToList(MyColor
     * colortoadd) ... ILoMyColor this.that.removefromList() ... ILoMyColor
     * this.that.findMatches(ILoMyColor guess) ... MyPosn
     * this.that.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
     * this.that.isExact(MyColor first) ... boolean this.that.inExact(MyColor first)
     * ... boolean this.that.isExactHelper(MyColor color) ... ILoMyColor
     * this.that.sameLoColor(ILoMyColor that) ... boolean
     * this.that.sameMtLoColor(MtLoMyColor that) ... boolean
     * this.that.sameConsLoColor(MtLoMyColor that) ... boolean
     * this.that.drawCurGuess() ... WorldImage this.that.backspaceKey() ...
     * ILoMyColor this.that.randColorList(int length, Random rand) ... ILoMyColor
     */
    return this.first == that.first && this.rest.sameLoColor(that.rest);
  }

  public WorldImage drawCurGuess() {
    return this.colorsToCircles().drawColorCircles();
  }

  public WorldImage drawFinishedGuess() {
    return this.colorsToCircles().drawColorCircles();
  }

  public ILoMyColor backspaceKey() {
    return this.removefromList();
  }

  public ILoMyColor randColorList(int length, Random rand) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: length ... int rand ... Random
     * 
     * Methods on Parameters: this.rand.nextInt(int value) ... int
     */
    return this.randColorListHelper(length, new MtLoMyColor(), length, rand);
  }

  public ILoMyColor randColorListHelper(int length, ILoMyColor acc, int originallength,
      Random rand) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: length ... int acc ... ILoMyColor originallength ... int
     * 
     * Methods on Parameters: this.acc.listLength() ... int
     * this.acc.colorsToCircles() ... ILoCircle this.acc.assignValues() ...
     * ILoMyColor this.acc.assignValuesHelper(int acc) ... ILoMyColor
     * this.acc.findMatch(int randomNum) ... MyColor this.acc.drawAvailableColors()
     * ... WorldImage this.acc.addToList(MyColor colortoadd) ... ILoMyColor
     * this.acc.removefromList() ... ILoMyColor this.acc.findMatches(ILoMyColor
     * guess) ... MyPosn this.acc.findMatchHelper(ILoMyColor guess, MyPosn posn) ...
     * MyPosn this.acc.isExact(MyColor first) ... boolean this.acc.inExact(MyColor
     * first) ... boolean this.acc.isExactHelper(MyColor color) ... ILoMyColor
     * this.acc.sameLoColor(ILoMyColor that) ... boolean
     * this.acc.sameMtLoColor(MtLoMyColor that) ... boolean
     * this.acc.sameConsLoColor(MtLoMyColor that) ... boolean
     * this.acc.drawCurGuess() ... WorldImage this.acc.backspaceKey() ... ILoMyColor
     * this.acc.randColorList(int length, Random rand) ... ILoMyColor
     */
    if (length == 0) {
      return acc;
    }
    else {
      return new ConsLoMyColor(this.assignValues().findMatch(rand.nextInt(originallength)),
          this.randColorListHelper(length - 1, acc, originallength, rand));
    }
  }
}

// To represent an identifiable color
class MyColor {
  Color color;
  int number; // the identifier

  // Main Constructor
  MyColor(Color color, int number) {
    this.color = color;
    this.number = number;
  }

  // Convenience Constructor
  MyColor(Color color) {
    this(color, 0);
  }

  /*
   * CLASS TEMPLATE: Fields: this.color ... Color this.number ... int
   * 
   * Methods: this.colorToCircle() ... Circle this.changeNum(int value) ...
   * MyColor this.compare(int val) ... boolean
   * 
   * Methods on Fields:
   */

  // Creates a Circle with this color in its field
  Circle colorToCircle() {
    return new Circle(this.color);
  }

  // Creates a MyColor with altered identifier (number field)
  MyColor changeNum(int value) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: value ... int
     * 
     * Methods on Parameters:
     */
    return new MyColor(this.color, value);
  }

  // Compare two MyColors to see if they are equivalent
  boolean compareColor(MyColor color) {
    return this.color.equals(color.color);
  }

  boolean compare(int val) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: value ... int
     * 
     * Methods on Parameters:
     */
    return this.number == val;
  }
}

//-----------------------------NUMBERS--------------------------------------------------------------------------------------------------------------

// To represent a list of numbers (used for testing)
interface ILoNumber {
}

// To represent an empty list of numbers
class MtLoNumber implements ILoNumber {
  MtLoNumber() {
  }
}

// To represent a non-empty list of numbers
class ConsLoNumber implements ILoNumber {
  int first;
  ILoNumber rest;

  // Main Constructor
  ConsLoNumber(int first, ILoNumber rest) {
    this.first = first;
    this.rest = rest;
  }
}
//-----------------------------POSN-----------------------------------------------------------------------------------------------------------------

// To represent positions that can be manipulated
class MyPosn extends Posn {

  // Standard constructor
  MyPosn(int x, int y) {
    super(x, y);
  }

  // Constructor to convert from a Posn to a MyPosn
  MyPosn(Posn p) {
    this(p.x, p.y);
  }

  /*
   * CLASS TEMPLATE: Fields: this.x ... int this.y ... int
   * 
   * Methods: this.offsetPosn(MyPosn p) ... MyPosn
   * this.placeImageOnScene(WorldScene scene, WorldImage image) ... WorldImage
   * this.addPosn(MyPosn p) ... MyPosn this.drawPosn() ... WorldImage
   * 
   * Methods on Fields:
   */

  // Offsets the x of this MyPosn based upon its position in the list
  MyPosn offsetPosn(int listPosition) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: listPosition ... int
     * 
     * Methods on Parameters:
     */
    return new MyPosn(this.x * listPosition, this.y);
  }

  // Adds the x and y values of two MyPosns together
  MyPosn addPosn(MyPosn p) {
    /*
     * METHOD TEMPLATE: refer to class template
     * 
     * Parameters: p ... MyPosn
     * 
     * Methods on Parameters: this.p.offsetPosn(MyPosn p) ... MyPosn
     * this.p.placeImageOnScene(WorldScene scene, WorldImage image) ... WorldImage
     * this.p.addPosn(MyPosn p) ... MyPosn this.p.drawPosn() ... WorldImage
     */
    return new MyPosn(this.x + p.x, this.y + p.y);
  }

  // Creates an image of this MyPosn in text on a background
  WorldImage drawPosn() {
    return new OverlayImage(new TextImage(this.x + "  " + this.y, 60, Color.BLACK),
        new RectangleImage(70 * 2, 70, OutlineMode.SOLID, Color.GRAY));
  }
}

//----------------------------EXAMPLES------------------------------------------------------------------------------------------------------------------

// To represent the examples and tests of Mastermind
class ExamplesCircles {

  // EXAMPLES:

  Random randomSeed = new Random(10);

  // Number
  ILoNumber emptylon = new MtLoNumber();
  ILoNumber randomLoNumber = new ConsLoNumber(this.randomSeed.nextInt(5),
      new ConsLoNumber(this.randomSeed.nextInt(5),
          new ConsLoNumber(this.randomSeed.nextInt(5), new ConsLoNumber(this.randomSeed.nextInt(5),
              new ConsLoNumber(this.randomSeed.nextInt(5), new MtLoNumber())))));

  // MyPosn
  MyPosn pos1 = new MyPosn(2, 2);
  MyPosn posn0 = new MyPosn(0, 0);
  MyPosn posnneg = new MyPosn(-5, -4);
  MyPosn posnpos = new MyPosn(3, 2);

  // MyColor
  MyColor MyColor1 = new MyColor(Color.RED);
  MyColor MyColor2 = new MyColor(Color.BLUE);
  MyColor MyColor3 = new MyColor(Color.GREEN);
  MyColor mc5 = new MyColor(Color.RED, 1);
  MyColor mc6 = new MyColor(Color.BLUE, -2);
  MyColor mc7 = new MyColor(Color.GREEN, 0);

  ILoMyColor emptylomc = new MtLoMyColor();
  ILoMyColor funcolors = new ConsLoMyColor(new MyColor(Color.BLUE, 2),
      new ConsLoMyColor(new MyColor(Color.GREEN, 3), new ConsLoMyColor(new MyColor(Color.PINK, 4),
          new ConsLoMyColor(new MyColor(Color.CYAN, 5), this.emptylomc))));
  ILoMyColor emptyColorList = new MtLoMyColor();
  ILoMyColor colorlist1 = new ConsLoMyColor(new MyColor(Color.RED, 1),
      new ConsLoMyColor(new MyColor(Color.BLUE, 2),
          new ConsLoMyColor(new MyColor(Color.GREEN, 3),
              new ConsLoMyColor(new MyColor(Color.WHITE, 4),
                  new ConsLoMyColor(new MyColor(Color.BLACK, 5), this.emptyColorList)))));
  ILoMyColor lomc1 = new ConsLoMyColor(this.MyColor1,
      new ConsLoMyColor(this.MyColor2, this.emptylomc));
  ILoMyColor colorlist2 = new ConsLoMyColor(new MyColor(Color.RED, 1),
      new ConsLoMyColor(new MyColor(Color.BLUE, 5),
          new ConsLoMyColor(new MyColor(Color.GREEN, 3), this.emptyColorList)));
  ILoMyColor correctlist = new ConsLoMyColor(this.MyColor1,
      new ConsLoMyColor(this.MyColor2, new ConsLoMyColor(this.MyColor3, new MtLoMyColor())));
  ILoMyColor guesseslist = new ConsLoMyColor(this.MyColor1,
      new ConsLoMyColor(this.MyColor2, new ConsLoMyColor(this.MyColor3, new MtLoMyColor())));
  ILoMyColor lomc2 = new ConsLoMyColor(Color.RED, new ConsLoMyColor(Color.BLUE, this.emptylomc));

  // Circle
  Circle circle1 = new Circle(Color.black);
  Circle circle2 = new Circle(Color.blue);
  Circle circle3 = new Circle(Color.green);
  Circle circle4 = new Circle(Color.red);
  Circle circle5 = new Circle(50, this.posnpos, Color.red);
  Circle circle6 = new Circle(60, this.posnpos, Color.blue);

  ILoCircle emptyloc = new MtLoCircle();
  ILoCircle loc1 = new ConsLoCircle(circle1, new ConsLoCircle(circle2,
      new ConsLoCircle(circle3, new ConsLoCircle(circle4, this.emptyloc))));

  // Mastermind
  Mastermind game2 = new Mastermind(true, 5, 9, this.colorlist1);
  Mastermind game3 = new Mastermind(true, 5, 5, this.colorlist1, this.randomSeed);
  Mastermind game4 = new Mastermind(true, 4, 6, this.colorlist1, this.randomSeed, this.funcolors, 3,
      1, this.colorlist1, new MtLoGuess(), new MtLoScore());

  /*
   * 
   * Mastermind game5 = new Mastermind(true, 4, 6, this.colorlist1,
   * this.randomSeed, this.emptylomc, 3, 3); Mastermind game7 = new
   * Mastermind(true, 3, 3, this.colorlist1, this.randomSeed, this.emptylomc, 3,
   * 3);
   * 
   * Mastermind game6 = new Mastermind(true, 4, 6, this.colorlist1,
   * this.randomSeed, this.emptylomc, 3, 3);
   * 
   */

  // TESTS:

  boolean testGuessesToList(Tester t) {
    return t.checkExpect(this.game2.lengthToList(),
        new ConsLoCircle(new Circle(Color.black),
            new ConsLoCircle(new Circle(Color.black),
                new ConsLoCircle(new Circle(Color.black), new ConsLoCircle(new Circle(Color.black),
                    new ConsLoCircle(new Circle(Color.black), this.emptyloc))))));
  }
  /*
   * boolean testColorConstructor(Tester t) { return t.checkExpect(this.lomc1 ==
   * this.lomc2, true); }
   */

  boolean testGame(Tester t) {
    return this.game2.bigBang(1000, 1000);
  }

  boolean testAssignValues(Tester t) {
    return t.checkExpect(this.lomc1.assignValues(), new ConsLoMyColor(new MyColor(Color.RED, 0),
        new ConsLoMyColor(new MyColor(Color.BLUE, 1), new MtLoMyColor())));
  }

  /*
   * boolean testRandNumList(Tester t) { return
   * t.checkExpect(this.game3.randNumList(), this.randomLoNumber); }
   */

  boolean testFindMatches(Tester t) {
    return t.checkExpect(this.correctlist.findMatches(this.guesseslist), new MyPosn(2, 0));
  }

  boolean testdraw(Tester t) {
    WorldCanvas c = new WorldCanvas(900, 900);
    WorldScene s = new WorldScene(900, 900);
    return c.drawScene(s.placeImageXY(this.game4.drawCurGuessOffset(), 450, 450)) && c.show()
        && c.show();
  }

  boolean testparse(Tester t) {
    return t.checkExpect(7 == Integer.parseInt("7"), true);
  }

  boolean testFindMatch(Tester t) {
    return t.checkExpect(this.colorlist1.findMatch(3), new MyColor(Color.GREEN, 3));
  }

  /*
   * boolean testNumberKey(Tester t) { return
   * t.checkExpect(this.game6.numberKeys("1"), new Mastermind(true, 4, 6,
   * this.colorlist1, this.randomSeed, new ConsLoMyColor(new MyColor(Color.RED,
   * 1), this.emptylomc), 3, 3)); }
   */

  /*
   * drawEmptyGuessesBoard drawCorrectSequence drawCurGuessOffset drawFindMatches
   * makeScene drawWin drawLose drawGameBoard drawCoverCorrect drawGuessOutcomeBox
   * onKeyEvent numberKeys enterKey backspaceKey
   * 
   * colorsToCircles assignValues assignValuesHelper findMatch drawAvailableColors
   * addToList removefromList findMatches findMatchHelper isExact isExactHelper
   * inExact drawCurGuess backspaceKey randColorList
   */

  // Mastermind
  boolean testlengthToList(Tester t) {
    return t.checkExpect(this.game7.lengthToList(),
        new ConsLoCircle(new Circle(Color.black), new ConsLoCircle(new Circle(Color.black),
            new ConsLoCircle(new Circle(Color.black), this.emptyloc))));
  }

  // List of Circles
  boolean testlistlength(Tester t) {
    return t.checkExpect(this.emptylomc.listLength(), 0)
        && t.checkExpect(this.lomc1.listLength(), 2);
  }

  boolean testsameLoColor(Tester t) {
    return t.checkExpect(this.emptylomc.sameLoColor(this.emptylomc), true)
        && t.checkExpect(this.lomc2.sameLoColor(this.lomc2), true)
        && t.checkExpect(this.colorlist2.sameLoColor(this.emptylomc), false)
        && t.checkExpect(this.colorlist2.sameLoColor(this.lomc1), false);
  }

  // Circle
  ILoCircle loc2 = new ConsLoCircle(this.circle1, new ConsLoCircle(this.circle5, this.emptyloc));

  WorldImage c1 = new OverlayImage(new CircleImage(30, OutlineMode.SOLID, Color.BLACK),
      (new RectangleImage((30 * 2) + 10, (30 * 2) + 10, OutlineMode.SOLID, Color.gray)));
  WorldImage c5 = new OverlayImage(new CircleImage(50, OutlineMode.SOLID, Color.RED),
      (new RectangleImage((50 * 2) + 10, (50 * 2) + 10, OutlineMode.SOLID, Color.gray)));
  WorldImage c1mt = new OverlayImage(new CircleImage(30, OutlineMode.OUTLINE, Color.black),
      (new RectangleImage((30 * 2) + 10, (30 * 2) + 10, OutlineMode.SOLID, Color.gray)));
  WorldImage c5mt = new OverlayImage(new CircleImage(50, OutlineMode.SOLID, Color.RED),
      (new RectangleImage((50 * 2) + 10, (50 * 2) + 10, OutlineMode.SOLID, Color.gray)));
  WorldImage row = new BesideImage(this.c1, this.c5);
  WorldImage mtrow = new BesideImage(this.c1mt, this.c5mt);

  boolean testlistLength(Tester t) {
    return t.checkExpect(this.emptyloc.listLength(), 0) && t.checkExpect(this.loc1.listLength(), 4)
        && t.checkExpect(this.loc2.listLength(), 2);
  }

  boolean testdrawColorCircles(Tester t) {
    return t.checkExpect(this.emptyloc.drawColorCircles(), new EmptyImage())
        && t.checkExpect(this.loc2.drawColorCircles(), new BesideImage(c1, c5));
  }

  boolean testdrawEmptyCircles(Tester t) {
    return t.checkExpect(this.emptyloc.drawEmptyCircles(), new EmptyImage())
        && t.checkExpect(this.loc2.drawEmptyCircles(), new BesideImage(this.c1mt, this.c5mt));
  }

  boolean testdrawEmptyGameBoard(Tester t) {
    return t.checkExpect(this.emptyloc.drawEmptyGameBoard(5), new EmptyImage())
        && t.checkExpect(this.loc2.drawEmptyGameBoard(2), new AboveImage(this.mtrow, this.mtrow));
  }

  // Circle
  boolean testdrawCircleOutline(Tester t) {
    return t.checkExpect(this.circle1.drawCircleOutline(),
        new OverlayImage(new CircleImage(30, OutlineMode.OUTLINE, Color.black),
            (new RectangleImage((30 * 2) + 10, (30 * 2) + 10, OutlineMode.SOLID, Color.gray))))
        && t.checkExpect(this.circle5.drawCircleOutline(),
            new OverlayImage(new CircleImage(50, OutlineMode.OUTLINE, Color.black),
                (new RectangleImage((50 * 2) + 10, (50 * 2) + 10, OutlineMode.SOLID, Color.gray))))
        && t.checkExpect(this.circle6.drawCircleOutline(),
            new OverlayImage(new CircleImage(60, OutlineMode.OUTLINE, Color.black),
                (new RectangleImage((60 * 2) + 10, (60 * 2) + 10, OutlineMode.SOLID, Color.gray))));
  }

  boolean testdrawCircleFill(Tester t) {
    return t.checkExpect(this.circle1.drawCircleFill(),
        new OverlayImage(new CircleImage(30, OutlineMode.SOLID, Color.BLACK),
            (new RectangleImage((30 * 2) + 10, (30 * 2) + 10, OutlineMode.SOLID, Color.gray))))
        && t.checkExpect(this.circle5.drawCircleFill(),
            new OverlayImage(new CircleImage(50, OutlineMode.SOLID, Color.RED),
                (new RectangleImage((50 * 2) + 10, (50 * 2) + 10, OutlineMode.SOLID, Color.gray))))
        && t.checkExpect(this.circle6.drawCircleFill(),
            new OverlayImage(new CircleImage(60, OutlineMode.SOLID, Color.BLUE),
                (new RectangleImage((60 * 2) + 10, (60 * 2) + 10, OutlineMode.SOLID, Color.gray))));
  }

  boolean testoffsetCircle(Tester t) {
    return t.checkExpect(this.circle1.offsetCircle(4), new Circle(new MyPosn(160, 40), Color.BLACK))
        && t.checkExpect(this.circle5.offsetCircle(2), new Circle(new MyPosn(6, 2), Color.RED))
        && t.checkExpect(this.circle6.offsetCircle(3), new Circle(new MyPosn(9, 2), Color.BLUE));
  }

  // Utils
  boolean testCheckRange(Tester t) {
    Utils u = new Utils();
    return t.checkExpect(u.checkRange(20, 10, "blerp"), 20)
        && t.checkException(new IllegalArgumentException("boop"), u, "checkRange", -5, 0, "boop")
        && t.checkException(new IllegalArgumentException("bop"), u, "checkRange", 10, 100, "bop");
  }

  boolean testduplicateException(Tester t) {
    Utils u = new Utils();
    return t.checkExpect(u.duplicateException(true, 5, 4, "blerp"), true)
        && t.checkException(new IllegalArgumentException("boop"), u, "duplicateException", false, 5,
            3, "boop")
        && t.checkExpect(u.duplicateException(false, 4, 5, "blerp"), false);
  }

  boolean testcheckLength(Tester t) {
    Utils u = new Utils();
    return t.checkExpect(u.checkLength(this.colorlist1, 0, "blerp"), this.colorlist1)
        && t.checkException(new IllegalArgumentException("boop"), u, "checkLength", this.colorlist1,
            1000, "boop");
  }

  // Circle
  boolean testcolorToCircle(Tester t) {
    return t.checkExpect(this.mc5.colorToCircle(), new Circle(Color.RED))
        && t.checkExpect(this.mc6.colorToCircle(), new Circle(Color.BLUE))
        && t.checkExpect(this.mc7.colorToCircle(), new Circle(Color.GREEN));
  }

  boolean testchangeNum(Tester t) {
    return t.checkExpect(this.mc5.changeNum(1), new MyColor(Color.RED, 2))
        && t.checkExpect(this.mc6.changeNum(-2), new MyColor(Color.BLUE, -4))
        && t.checkExpect(this.mc7.changeNum(-2), new MyColor(Color.GREEN, -2));
  }

  boolean testcompare(Tester t) {
    return t.checkExpect(this.mc5.compare(1), true) && t.checkExpect(this.mc6.compare(2), false)
        && t.checkExpect(this.MyColor3.compare(0), true);
  }

  // MyPosn
  boolean testoffsetPosn(Tester t) {
    return t.checkExpect(this.posn0.offsetPosn(5), this.posn0)
        && t.checkExpect(this.posnneg.offsetPosn(0), new MyPosn(0, -4))
        && t.checkExpect(this.posnpos.offsetPosn(-2), new MyPosn(-6, 2));
  }

  boolean testaddPosn(Tester t) {
    return t.checkExpect(this.posn0.addPosn(this.posnneg), this.posnneg)
        && t.checkExpect(this.posnneg.addPosn(this.posnpos), new MyPosn(-2, -2))
        && t.checkExpect(this.posnpos.addPosn(this.posn0), this.posnpos);
  }

  boolean testdrawPosn(Tester t) {
    return t.checkExpect(this.posn0.drawPosn(),
        new OverlayImage(new TextImage(0 + "  " + 0, 60, Color.BLACK),
            new RectangleImage(70 * 2, 70, OutlineMode.SOLID, Color.GRAY)))
        && t.checkExpect(this.posnneg.drawPosn(),
            new OverlayImage(new TextImage(-5 + "  " + -4, 60, Color.BLACK),
                new RectangleImage(70 * 2, 70, OutlineMode.SOLID, Color.GRAY)))
        && t.checkExpect(this.posnpos.drawPosn(),
            new OverlayImage(new TextImage(3 + "  " + 2, 60, Color.BLACK),
                new RectangleImage(70 * 2, 70, OutlineMode.SOLID, Color.GRAY)));
  }

}
/*
 * boolean testonKeyEvent(Tester t) { return t.checkExpect( } }
 * 
 * public World onKeyEvent(String key) { if (key.equals("backspace")) { return
 * this.backspaceKey(); } if (key.equals("enter")) { return this.enterKey(); }
 * if (key.contains("123456789")) { return this.numberKeys(key); } else { return
 * new Mastermind(this.duplicate, this.length, this.guesses, this.colors,
 * this.rand, this.currentguesses, this.previousguesses, this.guessesleft); } }
 */
//import tester.Tester;
//
//import javalib.worldimages.*; // images, like RectangleImage or OverlayImages
//import javalib.funworld.*; // the abstract World class and the big-bang library
//import javalib.worldcanvas.WorldCanvas;
//import java.awt.Color;
//import java.util.Random;
//
////--------------------------TO DO LIST---------------------------------------------
///* Data Design: Check Expects
// * Random Sequence Generator with and without Duplicates
// * InExact and Exact Match Checker
// * public WorldScene lastScene(String msg);
// * Abstractions if Needed
// * Current Score
// * Past Guesses
// * Duplicate boolean
// */
////-----------------------------GAME-----------------------------------------------
//
//// To represent the game of Mastermind 
//class Mastermind extends World {
//  static Utils u = new Utils();
//
//  boolean duplicate; // are duplicate allowed in the correct sequence?
//  int length; // length of correct sequence
//  int guesses; // number of guesses
//  ILoMyColor colors; // list of possible colors
//  Random rand;
//  ILoMyColor currentguesses;
//  int previousguesses;
//  int guessesleft;
//  ILoMyColor correctsequence;
//  ILoMyColor correctsequenceaccum;
//  ILoGuess guesseslist;
//  ILoScore scoreslist;
//
//  // Constructor for Use in Testing, with a Specified Random Object
//  Mastermind(boolean duplicate, int length, int guesses, ILoMyColor colors, Random rand) {
//    this.duplicate = u.duplicateException(duplicate, length, colors.listLength(),
//        "Invalid Duplicate");
//    this.length = u.checkRange(length, 0, "Invalid Length: " + Integer.toString(length));
//    this.guesses = u.checkRange(guesses, 0, "Invalid # of Guesses: " + Integer.toString(guesses));
//    this.colors = u.checkLength(colors, 0, "Invalid List of Colors");
//    this.rand = rand; // with seed
//    this.currentguesses = new MtLoMyColor();
//    this.previousguesses = 0;
//    this.guessesleft = this.guesses;
//    this.correctsequence = this.colors.randColorList(length, rand);
//    this.correctsequenceaccum = this.correctsequence;
//    this.guesseslist = new MtLoGuess();
//    this.scoreslist = new MtLoScore();
//  }
//
//  // Constructor for Use in "Real" Games
//  Mastermind(boolean duplicate, int length, int guesses, ILoMyColor colors) {
//    this(duplicate, length, guesses, colors, new Random(), new MtLoMyColor(), 0, guesses,
//        new MtLoMyColor(), new MtLoGuess(), new MtLoScore());
//  }
//
//  // Constructor for Use in Big Bang Methods
//  Mastermind(boolean duplicate, int length, int guesses, ILoMyColor colors, Random rand,
//      ILoMyColor currentguesses, int previousguesses, int guessesleft,
//      ILoMyColor correctsequenceaccum, ILoGuess guesseslist, ILoScore scoreslist) {
//    this.duplicate = u.duplicateException(duplicate, length, colors.listLength(),
//        "Invalid Duplicate");
//    this.length = u.checkRange(length, 0, "Invalid Length: " + Integer.toString(length));
//    this.guesses = u.checkRange(guesses, 0, "Invalid # of Guesses: " + Integer.toString(guesses));
//    this.colors = u.checkLength(colors, 0, "Invalid List of Colors");
//    this.rand = rand; // with seed
//    this.currentguesses = currentguesses;
//    this.previousguesses = previousguesses;
//    this.guessesleft = guessesleft;
//    this.correctsequence = this.colors.randColorList(length, rand);
//    this.correctsequenceaccum = correctsequenceaccum;
//    this.guesseslist = guesseslist;
//    this.scoreslist = scoreslist;
//  }
//  
//  // Constants
//  final int DIAMETER = 60;
//  final int SPACING = 10;
//
//  // Creates a list of Circles based upon the length of the sequence to be guessed
//  public ILoCircle lengthToList() {
//    return this.lengthToListHelper(this.length, new MtLoCircle());
//  }
//
//  // Helps create a list of Circles by using an accumulator for the number of
//  // circles
//  // need to be created and an accumulator for the list so far
//  public ILoCircle lengthToListHelper(int length, ILoCircle listSoFar) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: length ... int listSoFar ... ILoCircle
//     * 
//     * Methods on Parameters: 
//     * this.listSoFar.listLength() ... int
//     * this.listSoFar.drawColorCircles() ... WorldImage
//     * this.listSoFar.drawEmptyCircles() ... WorldImage
//     * this.listSoFar.drawEmptyGameBoard(int guesses) ... WorldImage
//     * this.listSoFar.drawEmptyGameBoardHelper(WorldImage imageSoFar, int guesses)
//     * ... WorldImage
//     */
//    if (length == 0) {
//      return listSoFar;
//    }
//    else {
//      return new ConsLoCircle(new Circle(Color.black),
//          this.lengthToListHelper(length - 1, listSoFar));
//    }
//  }
//
//  // Draws an empty gameboard based upon the number of guesses a player is allowed
//  public WorldImage drawEmptyGuessesBoard() {
//    return this.lengthToList().drawEmptyGameBoard(this.guesses);
//  }
//
//  // Draws the randomized correct sequence (generated through the inputed list of
//  // colors)
//  public WorldImage drawCorrectSequence() {
//    return this.correctsequence.colorsToCircles().drawColorCircles();
//  }
//
//  // Draws the current guess at the appropriate position on this Mastermind
//  // gameboard
//  public WorldImage drawCurGuessOffset() {
//    return new OverlayOffsetImage(this.currentguesses.drawCurGuess(),
//        (((this.length - this.currentguesses.listLength()) * (this.DIAMETER + this.SPACING)) / 2)
//            + (this.DIAMETER + this.SPACING),
//        this.previousguesses * (this.DIAMETER + this.SPACING), this.drawGameBoard());
//  }
//
//  // Draws the exact and inexact match score on this Mastermind gameboard
//  public WorldImage drawFindMatches() {
//    return new OverlayOffsetImage(this.correctsequence.findMatches(this.currentguesses).drawPosn(),
//        (Math.max(this.length, this.colors.listLength()) * (this.DIAMETER + this.SPACING)) / 2,
//        this.previousguesses * (this.DIAMETER + this.SPACING), this.drawGameBoard());
//  }
//
//  // Creates the World scene of the BigBang (the entire Mastermind gameboard with
//  // all of its elements)
//  public WorldScene makeScene() {
//    return new WorldScene( // Dimensions:
//        Math.max((this.length + 2) * (DIAMETER + SPACING),
//            (this.colors.listLength()) * (DIAMETER + SPACING)),
//        (3 + this.guesses) * (DIAMETER + SPACING))
//            .placeImageXY(this.drawGameBoard(), ((this.length + 2) * (DIAMETER + SPACING)) / 2,
//                ((this.guesses + 3) * (DIAMETER + SPACING)) / 2)
//            .placeImageXY(this.currentguesses.drawCurGuess(), // Current Guesses
//                (this.currentguesses.listLength() * (this.DIAMETER + this.SPACING) / 2),
//                (1 + this.guessesleft) * (this.DIAMETER + this.SPACING)
//                    + (this.DIAMETER + this.SPACING) / 2)
//            .placeImageXY(this.guesseslist.drawGuessList(),
//                (this.length * (this.DIAMETER + this.SPACING) / 2),
//                ((this.guesses + 1) * (this.DIAMETER + this.SPACING)
//                    + (this.DIAMETER + this.SPACING) / 2) + (this.DIAMETER + this.SPACING) / 2
//                    - (this.guesseslist.listLength() * (this.DIAMETER + this.SPACING)) / 2)
//            .placeImageXY(this.scoreslist.drawScoreList(), // Score
//                (this.length + 1) * (this.DIAMETER + this.SPACING),
//                ((this.guesses + 1) * (this.DIAMETER + this.SPACING)
//                    + (this.DIAMETER + this.SPACING) / 2) + (this.DIAMETER + this.SPACING) / 2
//                    - (this.scoreslist.listLength() * (this.DIAMETER + this.SPACING)) / 2);
//  }
//
//  public WorldScene lastScene() {
//    return new WorldScene( // Dimensions:
//        Math.max((this.length + 2) * (DIAMETER + SPACING),
//            (this.colors.listLength()) * (DIAMETER + SPACING)),
//        (3 + this.guesses) * (DIAMETER + SPACING))
//            .placeImageXY(this.drawGameBoard(), ((this.length + 2) * (DIAMETER + SPACING)) / 2, 
//                ((this.guesses + 3) * (DIAMETER + SPACING)) / 2)
//            .placeImageXY(this.currentguesses.drawCurGuess(), // Current Guesses
//                (this.currentguesses.listLength() * (this.DIAMETER + this.SPACING) / 2),
//                (1 + this.guessesleft) * (this.DIAMETER + this.SPACING)
//                    + (this.DIAMETER + this.SPACING) / 2)
//            .placeImageXY(this.guesseslist.drawGuessList(),
//                (this.length * (this.DIAMETER + this.SPACING) / 2),
//                ((this.guesses + 1) * (this.DIAMETER + this.SPACING)
//                    + (this.DIAMETER + this.SPACING) / 2) + (this.DIAMETER + this.SPACING) / 2
//                    - (this.guesseslist.listLength() * (this.DIAMETER + this.SPACING)) / 2)
//            .placeImageXY(this.scoreslist.drawScoreList(), // Score
//                (this.length + 1) * (this.DIAMETER + this.SPACING),
//                ((this.guesses + 1) * (this.DIAMETER + this.SPACING)
//                    + (this.DIAMETER + this.SPACING) / 2) + (this.DIAMETER + this.SPACING) / 2
//                    - (this.scoreslist.listLength() * (this.DIAMETER + this.SPACING)) / 2)
//            .placeImageXY(this.drawLose(), ((this.length + 2) * (this.DIAMETER + this.SPACING))
//                - (this.DIAMETER + this.SPACING), (this.DIAMETER + this.SPACING) / 2);
//  }
//
//  // Draws the message output by the game when the user wins
//  public WorldImage drawWin() {
//    return new OverlayOffsetImage(
//        new OverlayImage(new TextImage("You Win!", 70, Color.black),
//            new RectangleImage((this.DIAMETER + this.SPACING) * 2, (this.DIAMETER + this.SPACING),
//                OutlineMode.SOLID, Color.gray)),
//        ((this.DIAMETER + this.SPACING) * (this.length + 2)) / 2,
//        (this.guesses * (this.DIAMETER + this.SPACING)) / 2, drawGameBoard());
//  }
//
//  // Draws the message output by the game when the user loses
//  public WorldImage drawLose() {
//    return new OverlayImage(new TextImage("You Lose", 35, Color.black),
//        new RectangleImage((this.DIAMETER + this.SPACING) * 2, (this.DIAMETER + this.SPACING),
//            OutlineMode.SOLID, Color.gray));
//  }
//
//  // Draws the elements of the Mastermind gameboard that are not manipulated
//  public WorldImage drawGameBoard() {
//    return new AboveImage(
//        new BesideImage(new AboveImage(this.drawCoverCorrect(), (this.drawEmptyGuessesBoard())),
//            this.drawGuessOutcomeBox()),
//        this.colors.drawAvailableColors());
//  }
//
//  // Draws the black rectangle that resides at top of the Mastermind gameboard
//  // where the correct sequence
//  // should be
//  public WorldImage drawCoverCorrect() {
//    return new RectangleImage((this.DIAMETER + this.SPACING) * this.length,
//        this.DIAMETER + this.SPACING, OutlineMode.SOLID, Color.BLACK);
//  }
//
//  // Draws the space on the Mastermind gameboard allocated to the exact and
//  // inexact match score
//  public WorldImage drawGuessOutcomeBox() {
//    return new RectangleImage((this.DIAMETER + this.SPACING) * 2,
//        (this.DIAMETER + this.SPACING) * (this.guesses + 2), OutlineMode.SOLID, Color.GRAY);
//  }
//
//  // ------------------------------------BIGBANG---------------------------------------------
//  
//  // BigBang Key handler for backspace, enter, and numbers 1-9
//  public World onKeyEvent(String key) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: key ... String
//     * 
//     * Methods on Parameters: this.key.equals(String key) ... boolean
//     * this.key.contains(String range) ... boolean
//     */
//    if (key.equals("backspace")) {
//      return this.backspaceKey();
//    }
//    if (key.equals("enter")) {
//      return this.enterKey();
//    }
//    if ("123456789".contains(key)) {
//      return this.numberKeys(key);
//    }
//    else {
//      return new Mastermind(this.duplicate, this.length, this.guesses, this.colors, this.rand,
//          this.currentguesses, this.previousguesses, this.guessesleft, this.correctsequence,
//          this.guesseslist, this.scoreslist);
//    }
//  }
//
//  // Creates a World based upon the user's number key input with an updated
//  // current guess
//  World numberKeys(String key) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: 
//     * key ... String
//     * 
//     * Methods on Parameters: 
//     * Integer.parseInt(this.key) ... int
//     * 
//     */
//    if (Integer.parseInt(key) <= this.colors.listLength()
//        && (this.currentguesses.listLength() < this.length)) {
//      return new Mastermind(this.duplicate, this.length, this.guesses, this.colors, this.rand,
//          this.currentguesses
//              .addToList(this.colors.assignValues().findMatch((Integer.parseInt(key)) - 1)),
//          this.previousguesses, this.guessesleft, this.correctsequenceaccum, this.guesseslist,
//          this.scoreslist);
//    }
//    else {
//      return new Mastermind(this.duplicate, this.length, this.guesses, this.colors, this.rand,
//          this.currentguesses, this.previousguesses, this.guessesleft, this.correctsequence,
//          this.guesseslist, this.scoreslist);
//    }
//  }
//
//  // Creates a World based upon the user's enter key input that either ends the
//  // game or updates guesses
//  World enterKey() {
//    if (this.currentguesses.sameLoColor(this.correctsequence)) {
//      return this.endOfWorld("You win!");
//    }
//    if (this.guessesleft == 0) {
//      return this.endOfWorld("You lose.");
//    }
//    if (this.currentguesses.listLength() < this.length) {
//      return new Mastermind(this.duplicate, this.length, this.guesses, this.colors, this.rand,
//          this.currentguesses, this.previousguesses, this.guessesleft, this.correctsequence,
//          this.guesseslist, this.scoreslist);
//    }
//    else {
//      return new Mastermind(this.duplicate, this.length, this.guesses, this.colors, this.rand,
//          new MtLoMyColor(), // .completedRow(),
//          this.previousguesses + 1, this.guessesleft - 1, this.correctsequence,
//          this.guesseslist.addToGuessList(this.currentguesses),
//          this.scoreslist.addToScores(this.correctsequence.findMatches(this.currentguesses)));
//    }
//  }
//
//  // Creates a World based upon the user's backspace key input with an updated
//  // current guess
//  World backspaceKey() {
//    if (currentguesses.listLength() == 0) {
//      return new Mastermind(this.duplicate, this.length, this.guesses, this.colors, this.rand,
//          this.currentguesses, this.previousguesses, this.guessesleft, this.correctsequence,
//          this.guesseslist, this.scoreslist);
//    }
//    else {
//      return new Mastermind(this.duplicate, this.length, this.guesses, this.colors, this.rand,
//          this.currentguesses.backspaceKey(), this.previousguesses, this.guessesleft,
//          this.correctsequence, this.guesseslist, this.scoreslist);
//    }
//
//  }
//}
//
////----------------------------UTILS----------------------------------------------------
//
////Acts as a container for functions implemented in constructors 
//class Utils {
//
//  // Determines if the given value is in a range of numbers and therefore, valid
//  int checkRange(int val, int max, String msg) {
//    if (val >= max) {
//      return val;
//    }
//    else {
//      throw new IllegalArgumentException(msg);
//    }
//  }
//
//  // Determines if certain Mastermind inputs are in an invalid configuration
//  boolean duplicateException(boolean duplicate, int length, int length2, String msg) {
//    if (!duplicate && length > length2) {
//      throw new IllegalArgumentException(msg);
//    }
//    else {
//      return duplicate;
//    }
//  }
//
//  // Determines if the given list is greater than length and therefore, valid
//  ILoMyColor checkLength(ILoMyColor list, int length, String msg) {
//    if (list.listLength() > length) {
//      return list;
//    }
//    else {
//      throw new IllegalArgumentException(msg);
//    }
//  }
//}
//
////------------------------------LIST OF GUESSES-----------------------------------------
//
////To represent a list of of lists of guesses(ILoMyColor)
//interface ILoGuess {
//
//  // Adds list of guesses to this list of list of guesses
//  ConsLoGuess addToGuessList(ILoMyColor currentguess);
//
//  // Draws the multiple lists of guesses
//  WorldImage drawGuessList();
//
//  // Calculates the number of lists of guesses in this list
//  int listLength();
//
//}
//
//// To represent an empty list of lists of guesses
//class MtLoGuess implements ILoGuess {
//  MtLoGuess() {
//  }
//
//  public ConsLoGuess addToGuessList(ILoMyColor currentguess) {
//    /* METHOD TEMPLATE:
//     * refer to class template
//     * 
//     * Parameters:
//     * currentguess ... ILoMyColor
//     * 
//     * Methods on Parameters:
//     * this.guess.listLength() ... int
//     * this.guess.colorsToCircles() ... ILoCircle this.guess.assignValues() ...
//     * ILoMyColor this.guess.assignValuesHelper(int acc) ... ILoMyColor
//     * this.guess.findMatch(int randomNum) ... MyColor
//     * this.guess.drawAvailableColors() ... WorldImage this.guess.addToList(MyColor
//     * colortoadd) ... ILoMyColor this.guess.removefromList() ... ILoMyColor
//     * this.guess.findMatches(ILoMyColor guess) ... MyPosn
//     * this.guess.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
//     * this.guess.isExact(MyColor first) ... boolean this.guess.inExact(MyColor
//     * first) ... boolean this.guess.isExactHelper(MyColor color) ... ILoMyColor
//     * this.guess.sameLoColor(ILoMyColor that) ... boolean
//     * this.guess.sameMtLoColor(MtLoMyColor that) ... boolean
//     * this.guess.sameConsLoColor(MtLoMyColor that) ... boolean
//     * this.guess.drawCurGuess() ... WorldImage this.guess.backspaceKey() ...
//     * ILoMyColor this.guess.randColorList(int length, Random rand) ... ILoMyColor
//     */
//    return new ConsLoGuess(currentguess, new MtLoGuess());
//  }
//
//  public WorldImage drawGuessList() {
//    return new EmptyImage();
//  }
//
//  public int listLength() {
//    return 0;
//  }
//}
//
////To represent a non-empty list of lists of guesses
//class ConsLoGuess implements ILoGuess {
//  ILoMyColor first;
//  ILoGuess rest;
//
//  ConsLoGuess(ILoMyColor first, ILoGuess rest) {
//    this.first = first;
//    this.rest = rest;
//  }
//
//  public ConsLoGuess addToGuessList(ILoMyColor currentguess) {
//    /* METHOD TEMPLATE:
//     * refer to class template
//     * 
//     * Parameters:
//     * currentguess ... ILoMyColor
//     * 
//     * Methods on Parameters:
//     * this.guess.listLength() ... int
//     * this.guess.colorsToCircles() ... ILoCircle this.guess.assignValues() ...
//     * ILoMyColor this.guess.assignValuesHelper(int acc) ... ILoMyColor
//     * this.guess.findMatch(int randomNum) ... MyColor
//     * this.guess.drawAvailableColors() ... WorldImage this.guess.addToList(MyColor
//     * colortoadd) ... ILoMyColor this.guess.removefromList() ... ILoMyColor
//     * this.guess.findMatches(ILoMyColor guess) ... MyPosn
//     * this.guess.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
//     * this.guess.isExact(MyColor first) ... boolean this.guess.inExact(MyColor
//     * first) ... boolean this.guess.isExactHelper(MyColor color) ... ILoMyColor
//     * this.guess.sameLoColor(ILoMyColor that) ... boolean
//     * this.guess.sameMtLoColor(MtLoMyColor that) ... boolean
//     * this.guess.sameConsLoColor(MtLoMyColor that) ... boolean
//     * this.guess.drawCurGuess() ... WorldImage this.guess.backspaceKey() ...
//     * ILoMyColor this.guess.randColorList(int length, Random rand) ... ILoMyColor
//     */
//    return new ConsLoGuess(currentguess, this);
//  }
//
//  public WorldImage drawGuessList() {
//    return new AboveImage(this.first.drawCurGuess(), this.rest.drawGuessList());
//  }
//
//  public int listLength() {
//    return 1 + this.rest.listLength();
//  }
//}
//
////------------------------------LIST OF SCORES -------------------------------------------
//
////To represent a list of scores
//interface ILoScore {
//
//  // Adds a score to this list of scores
//  ILoScore addToScores(MyPosn score);
//
//  // Draws the list of scores on the game board
//  WorldImage drawScoreList();
//
//  // Computes the number of scores in this list 
//  int listLength();
//
//}
//
//// To represent an empty list of scores
//class MtLoScore implements ILoScore {
//  MtLoScore() {
//  }
//
//  public ConsLoScore addToScores(MyPosn score) {
//    /* METHOD TEMPLATE:
//     * refer to class template
//     * 
//     * Parameters:
//     * score ... MyPosn
//     * 
//     * Methods on Parameters:
//     * this.offsetPosn(MyPosn p) ... MyPosn
//     * this.placeImageOnScene(WorldScene scene, WorldImage image) ... WorldImage
//     * this.addPosn(MyPosn p) ... MyPosn this.drawPosn() ... WorldImage
//     */
//    return new ConsLoScore(score, new MtLoScore());
//  }
//
//  public WorldImage drawScoreList() {
//    return new EmptyImage();
//  }
//
//  public int listLength() {
//    return 0;
//  }
//
//}
//
////To represent a non-empty list of scores
//class ConsLoScore implements ILoScore {
//  MyPosn first;
//  ILoScore rest;
//
//  ConsLoScore(MyPosn first, ILoScore rest) {
//    this.first = first;
//    this.rest = rest;
//  }
//
//  public ConsLoScore addToScores(MyPosn score) {
//    /* METHOD TEMPLATE:
//     * refer to class template
//     * 
//     * Parameters:
//     * score ... MyPosn
//     * 
//     * Methods on Parameters:
//     * this.offsetPosn(MyPosn p) ... MyPosn
//     * this.placeImageOnScene(WorldScene scene, WorldImage image) ... WorldImage
//     * this.addPosn(MyPosn p) ... MyPosn this.drawPosn() ... WorldImage
//     */
//    return new ConsLoScore(score, new MtLoScore());
//  }
//
//  public WorldImage drawScoreList() {
//    return new AboveImage(this.first.drawPosn(), this.rest.drawScoreList());
//  }
//
//  public int listLength() {
//    return 1 + this.rest.listLength();
//  }
//
//}
//
//// -----------------------------CIRCLES------------------------------------------------------
//
//// To represent a list of Circles
//interface ILoCircle {
//
//  // Computes how many MyColors are in this list
//  int listLength();
//
//  // Draws this list of Circles as filled circles beside one another
//  WorldImage drawColorCircles();
//
//  // Draws this list of Circles as outlined circles beside one another
//  WorldImage drawEmptyCircles();
//
//  // Draws the empty guesses of the Mastermind gameboard
//  WorldImage drawEmptyGameBoard(int guesses);
//
//  // Helps draw the Mastermind gameboard using an accumulator that builds the
//  // empty guesses based upon the number of guesses allowed
//  WorldImage drawEmptyGameBoardHelper(WorldImage imageSoFar, int guesses);
//
//}
//
//// To represent an empty list of Circles
//class MtLoCircle implements ILoCircle {
//  MtLoCircle() {
//  }
//
//  /*
//   * CLASS TEMPLATE: Fields:
//   * 
//   * Methods: this.listLength() ... int 
//   * this.drawColorCircles() ... WorldImage
//   * this.drawEmptyCircles() ... WorldImage 
//   * this.drawEmptyGameBoard(int guesses)... WorldImage 
//   * this.drawEmptyGameBoardHelper(WorldImage imageSoFar, int guesses) ... WorldImage
//   * 
//   * Methods on Fields:
//   * 
//   */
//
//  public int listLength() {
//    return 0;
//  }
//
//  public WorldImage drawColorCircles() {
//    return new EmptyImage();
//  }
//
//  public WorldImage drawEmptyCircles() {
//    return new EmptyImage();
//  }
//
//  public WorldImage drawEmptyGameBoard(int guesses) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: 
//     * guesses ... int
//     * 
//     * Methods on Parameters:
//     */
//    return new EmptyImage();
//  }
//
//  public WorldImage drawEmptyGameBoardHelper(WorldImage imageSoFar, int guesses) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: 
//     * guesses ... int 
//     * imageSoFar ... WorldImage
//     * 
//     * Methods on Parameters:
//     */
//    return new EmptyImage();
//  }
//
//}
//
//// To represent a non-empty list of Circles
//class ConsLoCircle implements ILoCircle {
//  Circle first;
//  ILoCircle rest;
//
//  // Main Constructor
//  ConsLoCircle(Circle first, ILoCircle rest) {
//    this.first = first;
//    this.rest = rest;
//  }
//
//  /*
//   * CLASS TEMPLATE: Fields: this.first ... Circle this.rest ... ILoCircle
//   * 
//   * Methods: this.listLength() ... int this.drawColorCircles() ... WorldImage
//   * this.drawEmptyCircles() ... WorldImage this.drawEmptyGameBoard(int guesses)
//   * ... WorldImage this.drawEmptyGameBoardHelper(WorldImage imageSoFar, int
//   * guesses) ... WorldImage
//   * 
//   * Methods on Fields: this.first.drawCircleOutline() ... WorldImage
//   * this.first.drawCircleFill() ... WorldImage this.first.offsetCircle(int
//   * listPosition) Circle
//   * 
//   * this.rest.listLength() ... int this.rest.drawColorCircles() ... WorldImage
//   * this.rest.drawEmptyCircles() ... WorldImage this.rest.drawEmptyGameBoard(int
//   * guesses) ... WorldImage this.rest.drawEmptyGameBoardHelper(WorldImage
//   * imageSoFar, int guesses) ... WorldImage
//   */
//
//  // Creates a new position based upon the length of the list of Circles // DO WE
//  // STILL NEED THIS??????????
//  public MyPosn imagePosn() {
//    return new MyPosn(this.listLength() * 70, 35);
//  }
//
//  public int listLength() {
//    return 1 + this.rest.listLength();
//  }
//
//  public WorldImage drawColorCircles() {
//    return new BesideImage(this.first.drawCircleFill(), this.rest.drawColorCircles());
//  }
//
//  public WorldImage drawEmptyCircles() {
//    return new BesideImage(this.first.drawCircleOutline(), this.rest.drawEmptyCircles());
//  }
//
//  public WorldImage drawEmptyGameBoard(int guesses) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: guesses ... int
//     * 
//     * Methods on Parameters:
//     */
//    final WorldImage rowOfCircles = this.drawEmptyCircles();
//    return this.drawEmptyGameBoardHelper(rowOfCircles, guesses); // call number of guesses times
//  }
//
//  public WorldImage drawEmptyGameBoardHelper(WorldImage imageSoFar, int guesses) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: guesses ... int imageSoFar ... WorldImage
//     * 
//     * Methods on Parameters:
//     */
//    if (guesses == 0) {
//      return imageSoFar;
//    }
//    else {
//      return drawEmptyGameBoardHelper(new AboveImage(this.drawEmptyCircles(), imageSoFar),
//          guesses - 1);
//    }
//  }
//}
//
//// To represent a customizable circle
//class Circle {
//  static MyPosn POSN1 = new MyPosn(40, 40);
//
//  int radius;
//  MyPosn position;
//  Color color;
//
//  // Main Constructor
//  Circle(int radius, MyPosn position, Color color) {
//    this.radius = radius;
//    this.position = position;
//    this.color = color;
//  }
//
//  // 1 parameter Convenience Constructor
//  Circle(Color color) {
//    this(30, POSN1, color);
//  }
//
//  // 2 parameter Convenience Constructor
//  Circle(MyPosn position, Color color) {
//    this(30, position, color);
//  }
//
//  /*
//   * CLASS TEMPLATE: Fields: this.radius ... int this.position ... MyPosn
//   * this.color ... Color
//   * 
//   * Methods: this.drawCircleOutline() ... WorldImage this.drawCircleFill() ...
//   * WorldImage this.offsetCircle(int listPosition) Circle
//   * 
//   * Methods on Fields: this.position.offsetPosn(MyPosn p) ... MyPosn
//   * this.position.placeImageOnScene(WorldScene scene, WorldImage image) ...
//   * WorldImage this.position.addPosn(MyPosn p) ... MyPosn
//   * this.position.drawPosn() ... WorldImage
//   * 
//   * this.color.colorToCircle() ... Circle this.color.changeNum(int value) ...
//   * MyColor this.color.compare(int val) ... boolean
//   */
//
//  // Draws an outline of this circle on a gray background
//  WorldImage drawCircleOutline() {
//    return new OverlayImage(new CircleImage(this.radius, OutlineMode.OUTLINE, Color.black),
//        (new RectangleImage((this.radius * 2) + 10, (this.radius * 2) + 10, OutlineMode.SOLID,
//            Color.gray)));
//  }
//
//  // Draws this filled circle on a gray background
//  WorldImage drawCircleFill() {
//    return new OverlayImage(new CircleImage(this.radius, OutlineMode.SOLID, this.color),
//        (new RectangleImage((this.radius * 2) + 10, (this.radius * 2) + 10, OutlineMode.SOLID,
//            Color.gray)));
//  }
//
//  // Offsets the position of this circle based upon the given list position
//  Circle offsetCircle(int listPosition) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: listPosition ... int
//     * 
//     * Methods on Parameters:
//     */
//    return new Circle(this.position.offsetPosn(listPosition), this.color);
//  }
//
//}
//
////-----------------------------COLORS--------------------------------------------------------
//
//// To represent a list of MyColors
//interface ILoMyColor {
//
//  // finds if a guess is an exact match
//  public int exact(ILoMyColor guess);
//
//  // helps determine if a guess is an exact match
//  public int exactHelp(MyColor firstofseq, ILoMyColor restofseq);
//
//  // Computes how many MyColors are in this list
//  int listLength();
//
//  // Converts this list of MyColors into a list of Circles
//  ILoCircle colorsToCircles();
//
//  // Assigns identifiable numbers to each MyColor in this list
//  ILoMyColor assignValues();
//
//  // Uses an accumulating identifier that assigns helps assignValues
//  // assign an increasing integer to each MyColor in the list
//  ILoMyColor assignValuesHelper(int acc);
//
//  // Finds a MyColor in this list whose identifier corresponds with the given
//  // integer
//  MyColor findMatch(int randomNum);
//
//  // Draws the given list of MyColors at the appropriate place on the game board
//  WorldImage drawAvailableColors();
//
//  // Adds the given MyColor to this list of MyColors
//  ILoMyColor addToList(MyColor colortoadd);
//
//  // Removes a MyColor from this list of MyColors
//  ILoMyColor removefromList();
//
//  // Determines if two list of MyColors contain the same elements in the same
//  // order
//  boolean sameLoColor(ILoMyColor that);
//  
//  // Finds number of inexact and exact matches
//  MyPosn findMatches(ILoMyColor guess);
//
//  // Determines if this list is equivalent to that empty list
//  boolean sameMtLoColor(MtLoMyColor that);
//
//  // Determines if this list is equivalent to that non-empty list
//  boolean sameConsLoColor(ConsLoMyColor that);
//
//  // Draws the Mastermind current guess on the gameboard
//  WorldImage drawCurGuess();
//
//  // Called when the backspace key is hit to remove a current guess
//  ILoMyColor backspaceKey();
//
//  // Generates a random list of MyColors of a given length
//  ILoMyColor randColorList(int length, Random rand);
//}
//
//// To represent an empty list of MyColors
//class MtLoMyColor implements ILoMyColor {
//  MtLoMyColor() {
//  }
//
//  /*
//   * CLASS TEMPLATE: Fields:
//   * 
//   * Methods: this.listLength() ... int this.colorsToCircles() ... ILoCircle
//   * this.assignValues() ... ILoMyColor this.assignValuesHelper(int acc) ...
//   * ILoMyColor this.findMatch(int randomNum) ... MyColor
//   * this.drawAvailableColors() ... WorldImage this.addToList(MyColor colortoadd)
//   * ... ILoMyColor this.removefromList() ... ILoMyColor
//   * this.findMatches(ILoMyColor guess) ... MyPosn this.findMatchHelper(ILoMyColor
//   * guess, MyPosn posn) ... MyPosn this.isExact(MyColor first) ... boolean
//   * this.inExact(MyColor first) ... boolean this.isExactHelper(MyColor color) ...
//   * ILoMyColor this.sameLoColor(ILoMyColor that) ... boolean
//   * this.sameMtLoColor(MtLoMyColor that) ... boolean
//   * this.sameConsLoColor(MtLoMyColor that) ... boolean this.drawCurGuess() ...
//   * WorldImage this.backspaceKey() ... ILoMyColor this.randColorList(int length,
//   * Random rand) ... ILoMyColor
//   * 
//   * Methods on Fields:
//   */
//
//  public int listLength() {
//    return 0;
//  }
//
//  public ILoCircle colorsToCircles() {
//    return new MtLoCircle();
//  }
//
//  public ILoMyColor assignValues() {
//    return new MtLoMyColor();
//  }
//
//  public ILoMyColor assignValuesHelper(int acc) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: acc ... int
//     * 
//     * Methods on Parameters:
//     */
//    return new MtLoMyColor();
//  }
//
//  public MyColor findMatch(int randomNum) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: randomNum ... int
//     * 
//     * Methods on Parameters:
//     */
//    return null;
//  }
//
//  public WorldImage drawAvailableColors() {
//    return new EmptyImage();
//  }
//
//  public ILoMyColor addToList(MyColor colortoadd) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: colortoadd ... MyColor
//     * 
//     * Methods on Parameters: this.colortoadd.colorToCircle() ... Circle
//     * this.colortoadd.changeNum(int value) ... MyColor this.colortoadd.compare(int
//     * val) ... boolean
//     */
//    return new ConsLoMyColor(colortoadd, new MtLoMyColor());
//  }
//
//  public ILoMyColor removefromList() {
//    return this;
//  }
//
//  public boolean isExact(MyColor first) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: first ... MyColor
//     * 
//     * Methods on Parameters: this.first.colorToCircle() ... Circle
//     * this.first.changeNum(int value) ... MyColor this.first.compare(int val) ...
//     * boolean
//     */
//    return false;
//  }
//
//
//  public boolean sameLoColor(ILoMyColor that) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: that ... ILoMyColor
//     * 
//     * Methods on Parameters: this.that.listLength() ... int
//     * this.that.colorsToCircles() ... ILoCircle this.that.assignValues() ...
//     * ILoMyColor this.that.assignValuesHelper(int acc) ... ILoMyColor
//     * this.that.findMatch(int randomNum) ... MyColor
//     * this.that.drawAvailableColors() ... WorldImage this.that.addToList(MyColor
//     * colortoadd) ... ILoMyColor this.that.removefromList() ... ILoMyColor
//     * this.that.findMatches(ILoMyColor guess) ... MyPosn
//     * this.that.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
//     * this.that.isExact(MyColor first) ... boolean this.that.inExact(MyColor first)
//     * ... boolean this.that.isExactHelper(MyColor color) ... ILoMyColor
//     * this.that.sameLoColor(ILoMyColor that) ... boolean
//     * this.that.sameMtLoColor(MtLoMyColor that) ... boolean
//     * this.that.sameConsLoColor(MtLoMyColor that) ... boolean
//     * this.that.drawCurGuess() ... WorldImage this.that.backspaceKey() ...
//     * ILoMyColor this.that.randColorList(int length, Random rand) ... ILoMyColor
//     */
//    return that.sameMtLoColor(this);
//  }
//
//  public boolean sameMtLoColor(MtLoMyColor that) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: that ... MtLoMyColor
//     * 
//     * Methods on Parameters: this.that.listLength() ... int
//     * this.that.colorsToCircles() ... ILoCircle this.that.assignValues() ...
//     * ILoMyColor this.that.assignValuesHelper(int acc) ... ILoMyColor
//     * this.that.findMatch(int randomNum) ... MyColor
//     * this.that.drawAvailableColors() ... WorldImage this.that.addToList(MyColor
//     * colortoadd) ... ILoMyColor this.that.removefromList() ... ILoMyColor
//     * this.that.findMatches(ILoMyColor guess) ... MyPosn
//     * this.that.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
//     * this.that.isExact(MyColor first) ... boolean this.that.inExact(MyColor first)
//     * ... boolean this.that.isExactHelper(MyColor color) ... ILoMyColor
//     * this.that.sameLoColor(ILoMyColor that) ... boolean
//     * this.that.sameMtLoColor(MtLoMyColor that) ... boolean
//     * this.that.sameConsLoColor(MtLoMyColor that) ... boolean
//     * this.that.drawCurGuess() ... WorldImage this.that.backspaceKey() ...
//     * ILoMyColor this.that.randColorList(int length, Random rand) ... ILoMyColor
//     */
//    return true;
//  }
//
//  public boolean sameConsLoColor(ConsLoMyColor that) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: that ... ConsLoMyColor
//     * 
//     * Methods on Parameters: this.that.listLength() ... int
//     * this.that.colorsToCircles() ... ILoCircle this.that.assignValues() ...
//     * ILoMyColor this.that.assignValuesHelper(int acc) ... ILoMyColor
//     * this.that.findMatch(int randomNum) ... MyColor
//     * this.that.drawAvailableColors() ... WorldImage this.that.addToList(MyColor
//     * colortoadd) ... ILoMyColor this.that.removefromList() ... ILoMyColor
//     * this.that.findMatches(ILoMyColor guess) ... MyPosn
//     * this.that.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
//     * this.that.isExact(MyColor first) ... boolean this.that.inExact(MyColor first)
//     * ... boolean this.that.isExactHelper(MyColor color) ... ILoMyColor
//     * this.that.sameLoColor(ILoMyColor that) ... boolean
//     * this.that.sameMtLoColor(MtLoMyColor that) ... boolean
//     * this.that.sameConsLoColor(MtLoMyColor that) ... boolean
//     * this.that.drawCurGuess() ... WorldImage this.that.backspaceKey() ...
//     * ILoMyColor this.that.randColorList(int length, Random rand) ... ILoMyColor
//     */
//    return false;
//  }
//
//  public WorldImage drawCurGuess() {
//    return new EmptyImage();
//  }
//
//  public ILoMyColor backspaceKey() {
//    return this.removefromList();
//  }
//
//  public ILoMyColor randColorList(int length, Random rand) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: length ... int rand ... Random
//     * 
//     * Methods on Parameters: this.rand.nextInt(int value) ... int
//     */
//    return this;
//  }
//
//  public ILoMyColor randColorListHelper(int length, ILoMyColor acc, int originallength,
//      Random rand) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: length ... int acc ... ILoMyColor originallength ... int
//     * 
//     * Methods on Parameters: this.acc.listLength() ... int
//     * this.acc.colorsToCircles() ... ILoCircle this.acc.assignValues() ...
//     * ILoMyColor this.acc.assignValuesHelper(int acc) ... ILoMyColor
//     * this.acc.findMatch(int randomNum) ... MyColor this.acc.drawAvailableColors()
//     * ... WorldImage this.acc.addToList(MyColor colortoadd) ... ILoMyColor
//     * this.acc.removefromList() ... ILoMyColor this.acc.findMatches(ILoMyColor
//     * guess) ... MyPosn this.acc.findMatchHelper(ILoMyColor guess, MyPosn posn) ...
//     * MyPosn this.acc.isExact(MyColor first) ... boolean this.acc.inExact(MyColor
//     * first) ... boolean this.acc.isExactHelper(MyColor color) ... ILoMyColor
//     * this.acc.sameLoColor(ILoMyColor that) ... boolean
//     * this.acc.sameMtLoColor(MtLoMyColor that) ... boolean
//     * this.acc.sameConsLoColor(MtLoMyColor that) ... boolean
//     * this.acc.drawCurGuess() ... WorldImage this.acc.backspaceKey() ... ILoMyColor
//     * this.acc.randColorList(int length, Random rand) ... ILoMyColor
//     */
//    return acc;
//  }
//
//  public int exact(ILoMyColor guess) {
//    return 0;
//  }
//
//  public int exactHelp(MyColor firstofseq, ILoMyColor restofseq) {
//    return 0;
//  }
//  
//  public MyPosn findMatches(ILoMyColor guess) {
//    return new MyPosn(0, 0);
//  }
//
//
//}
//
//// To represent a non-empty list of MyColors
//class ConsLoMyColor implements ILoMyColor {
//  MyColor first;
//  ILoMyColor rest;
//
//  // Main Constructor
//  ConsLoMyColor(MyColor first, ILoMyColor rest) {
//    this.first = first;
//    this.rest = rest;
//  }
//
//  // Color to MyColor Constructor
//  ConsLoMyColor(Color first, ILoMyColor rest) {
//    this(new MyColor(first, 0), rest);
//  }
//
//  /*
//   * CLASS TEMPLATE: Fields: this.first ... MyColor this.rest ... ILoMyColor
//   * 
//   * Methods: this.listLength() ... int this.colorsToCircles() ... ILoCircle
//   * this.assignValues() ... ILoMyColor this.assignValuesHelper(int acc) ...
//   * ILoMyColor this.findMatch(int randomNum) ... MyColor
//   * this.drawAvailableColors() ... WorldImage this.addToList(MyColor colortoadd)
//   * ... ILoMyColor this.removefromList() ... ILoMyColor
//   * this.findMatches(ILoMyColor guess) ... MyPosn this.findMatchHelper(ILoMyColor
//   * guess, MyPosn posn) ... MyPosn this.isExact(MyColor first) ... boolean
//   * this.inExact(MyColor first) ... boolean this.isExactHelper(MyColor color) ...
//   * ILoMyColor this.sameLoColor(ILoMyColor that) ... boolean
//   * this.sameMtLoColor(MtLoMyColor that) ... boolean
//   * this.sameConsLoColor(MtLoMyColor that) ... boolean this.drawCurGuess() ...
//   * WorldImage this.backspaceKey() ... ILoMyColor this.randColorList(int length,
//   * Random rand) ... ILoMyColor
//   * 
//   * Methods on Fields: this.first.colorToCircle() ... Circle
//   * this.first.changeNum(int value) ... MyColor this.first.compare(int val) ...
//   * boolean
//   * 
//   * this.rest.listLength() ... int this.rest.colorsToCircles() ... ILoCircle
//   * this.rest.assignValues() ... ILoMyColor this.rest.assignValuesHelper(int acc)
//   * ... ILoMyColor this.rest.findMatch(int randomNum) ... MyColor
//   * this.rest.drawAvailableColors() ... WorldImage this.rest.addToList(MyColor
//   * colortoadd) ... ILoMyColor this.rest.removefromList() ... ILoMyColor
//   * this.rest.findMatches(ILoMyColor guess) ... MyPosn
//   * this.rest.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
//   * this.rest.isExact(MyColor first) ... boolean this.rest.inExact(MyColor first)
//   * ... boolean this.rest.isExactHelper(MyColor color) ... ILoMyColor
//   * this.rest.sameLoColor(ILoMyColor that) ... boolean
//   * this.rest.sameMtLoColor(MtLoMyColor that) ... boolean
//   * this.rest.sameConsLoColor(MtLoMyColor that) ... boolean
//   * this.rest.drawCurGuess() ... WorldImage this.rest.backspaceKey() ...
//   * ILoMyColor this.rest.randColorList(int length, Random rand) ... ILoMyColor
//   */
//
//  public int listLength() {
//    return 1 + this.rest.listLength();
//  }
//
//  public ILoCircle colorsToCircles() {
//    return new ConsLoCircle(this.first.colorToCircle(), this.rest.colorsToCircles());
//  }
//
//  public ILoMyColor assignValues() {
//    return this.assignValuesHelper(0);
//  }
//
//  public MyPosn findMatches(ILoMyColor guess) {
//    return new MyPosn(this.exact(guess), 0);
//  }
//
//  public int exact(ILoMyColor guess) {
//    return guess.exactHelp(this.first, this.rest);
//  }
//
//  public int exactHelp(MyColor firstofseq, ILoMyColor restofseq) {
//    if (firstofseq.compareColor(this.first)) { // guess
//      return 1 + restofseq.exact(this.rest);
//    }
//    else {
//      return 0 + restofseq.exact(this.rest);
//    }
//  }
//
//  public ILoMyColor assignValuesHelper(int acc) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: acc ... int
//     * 
//     * Methods on Parameters:
//     */
//    return new ConsLoMyColor(this.first.changeNum(acc), this.rest.assignValuesHelper(acc + 1));
//  }
//
//  public MyColor findMatch(int randomNum) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: randomNum ... int
//     * 
//     * Methods on Parameters:
//     */
//    if (this.first.compare(randomNum)) {
//      return this.first;
//    }
//    else {
//      return this.rest.findMatch(randomNum);
//    }
//  }
//
//  public WorldImage drawAvailableColors() {
//    return this.colorsToCircles().drawColorCircles();
//  }
//
//  public ILoMyColor addToList(MyColor colortoadd) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: colortoadd ... MyColor
//     * 
//     * Methods on Parameters: this.colortoadd.colorToCircle() ... Circle
//     * this.colortoadd.changeNum(int value) ... MyColor this.colortoadd.compare(int
//     * val) ... boolean
//     */
//    return new ConsLoMyColor(colortoadd, this);
//  }
//
//  public ILoMyColor removefromList() {
//    return this.rest;
//  }
//
//  public boolean sameLoColor(ILoMyColor that) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: that ... ILoMyColor
//     * 
//     * Methods on Parameters: this.that.listLength() ... int
//     * this.that.colorsToCircles() ... ILoCircle this.that.assignValues() ...
//     * ILoMyColor this.that.assignValuesHelper(int acc) ... ILoMyColor
//     * this.that.findMatch(int randomNum) ... MyColor
//     * this.that.drawAvailableColors() ... WorldImage this.that.addToList(MyColor
//     * colortoadd) ... ILoMyColor this.that.removefromList() ... ILoMyColor
//     * this.that.findMatches(ILoMyColor guess) ... MyPosn
//     * this.that.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
//     * this.that.isExact(MyColor first) ... boolean this.that.inExact(MyColor first)
//     * ... boolean this.that.isExactHelper(MyColor color) ... ILoMyColor
//     * this.that.sameLoColor(ILoMyColor that) ... boolean
//     * this.that.sameMtLoColor(MtLoMyColor that) ... boolean
//     * this.that.sameConsLoColor(MtLoMyColor that) ... boolean
//     * this.that.drawCurGuess() ... WorldImage this.that.backspaceKey() ...
//     * ILoMyColor this.that.randColorList(int length, Random rand) ... ILoMyColor
//     */
//    return that.sameConsLoColor(this);
//  }
//
//  public boolean sameMtLoColor(MtLoMyColor that) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: that ... MtLoMyColor
//     * 
//     * Methods on Parameters: this.that.listLength() ... int
//     * this.that.colorsToCircles() ... ILoCircle this.that.assignValues() ...
//     * ILoMyColor this.that.assignValuesHelper(int acc) ... ILoMyColor
//     * this.that.findMatch(int randomNum) ... MyColor
//     * this.that.drawAvailableColors() ... WorldImage this.that.addToList(MyColor
//     * colortoadd) ... ILoMyColor this.that.removefromList() ... ILoMyColor
//     * this.that.findMatches(ILoMyColor guess) ... MyPosn
//     * this.that.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
//     * this.that.isExact(MyColor first) ... boolean this.that.inExact(MyColor first)
//     * ... boolean this.that.isExactHelper(MyColor color) ... ILoMyColor
//     * this.that.sameLoColor(ILoMyColor that) ... boolean
//     * this.that.sameMtLoColor(MtLoMyColor that) ... boolean
//     * this.that.sameConsLoColor(MtLoMyColor that) ... boolean
//     * this.that.drawCurGuess() ... WorldImage this.that.backspaceKey() ...
//     * ILoMyColor this.that.randColorList(int length, Random rand) ... ILoMyColor
//     */
//    return false;
//  }
//
//  public boolean sameConsLoColor(ConsLoMyColor that) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: that ... ConsLoMyColor
//     * 
//     * Methods on Parameters: this.that.listLength() ... int
//     * this.that.colorsToCircles() ... ILoCircle this.that.assignValues() ...
//     * ILoMyColor this.that.assignValuesHelper(int acc) ... ILoMyColor
//     * this.that.findMatch(int randomNum) ... MyColor
//     * this.that.drawAvailableColors() ... WorldImage this.that.addToList(MyColor
//     * colortoadd) ... ILoMyColor this.that.removefromList() ... ILoMyColor
//     * this.that.findMatches(ILoMyColor guess) ... MyPosn
//     * this.that.findMatchHelper(ILoMyColor guess, MyPosn posn) ... MyPosn
//     * this.that.isExact(MyColor first) ... boolean this.that.inExact(MyColor first)
//     * ... boolean this.that.isExactHelper(MyColor color) ... ILoMyColor
//     * this.that.sameLoColor(ILoMyColor that) ... boolean
//     * this.that.sameMtLoColor(MtLoMyColor that) ... boolean
//     * this.that.sameConsLoColor(MtLoMyColor that) ... boolean
//     * this.that.drawCurGuess() ... WorldImage this.that.backspaceKey() ...
//     * ILoMyColor this.that.randColorList(int length, Random rand) ... ILoMyColor
//     */
//    return this.first == that.first && this.rest.sameLoColor(that.rest);
//  }
//
//  public WorldImage drawCurGuess() {
//    return this.colorsToCircles().drawColorCircles();
//  }
//
//  public WorldImage drawFinishedGuess() {
//    return this.colorsToCircles().drawColorCircles();
//  }
//
//  public ILoMyColor backspaceKey() {
//    return this.removefromList();
//  }
//
//  public ILoMyColor randColorList(int length, Random rand) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: length ... int rand ... Random
//     * 
//     * Methods on Parameters: this.rand.nextInt(int value) ... int
//     */
//    return this.randColorListHelper(length, new MtLoMyColor(), length, rand);
//  }
//
//  public ILoMyColor randColorListHelper(int length, ILoMyColor acc, int originallength,
//      Random rand) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: length ... int acc ... ILoMyColor originallength ... int
//     * 
//     * Methods on Parameters: this.acc.listLength() ... int
//     * this.acc.colorsToCircles() ... ILoCircle this.acc.assignValues() ...
//     * ILoMyColor this.acc.assignValuesHelper(int acc) ... ILoMyColor
//     * this.acc.findMatch(int randomNum) ... MyColor this.acc.drawAvailableColors()
//     * ... WorldImage this.acc.addToList(MyColor colortoadd) ... ILoMyColor
//     * this.acc.removefromList() ... ILoMyColor this.acc.findMatches(ILoMyColor
//     * guess) ... MyPosn this.acc.findMatchHelper(ILoMyColor guess, MyPosn posn) ...
//     * MyPosn this.acc.isExact(MyColor first) ... boolean this.acc.inExact(MyColor
//     * first) ... boolean this.acc.isExactHelper(MyColor color) ... ILoMyColor
//     * this.acc.sameLoColor(ILoMyColor that) ... boolean
//     * this.acc.sameMtLoColor(MtLoMyColor that) ... boolean
//     * this.acc.sameConsLoColor(MtLoMyColor that) ... boolean
//     * this.acc.drawCurGuess() ... WorldImage this.acc.backspaceKey() ... ILoMyColor
//     * this.acc.randColorList(int length, Random rand) ... ILoMyColor
//     */
//    if (length == 0) {
//      return acc;
//    }
//    else {
//      return new ConsLoMyColor(this.assignValues().findMatch(rand.nextInt(originallength)),
//          this.randColorListHelper(length - 1, acc, originallength, rand));
//    }
//  }
//}
//
//// To represent an identifiable color
//class MyColor {
//  Color color;
//  int number; // the identifier
//
//  // Main Constructor
//  MyColor(Color color, int number) {
//    this.color = color;
//    this.number = number;
//  }
//
//  // Convenience Constructor
//  MyColor(Color color) {
//    this(color, 0);
//  }
//
//  /*
//   * CLASS TEMPLATE: Fields: this.color ... Color this.number ... int
//   * 
//   * Methods: this.colorToCircle() ... Circle this.changeNum(int value) ...
//   * MyColor this.compare(int val) ... boolean
//   * 
//   * Methods on Fields:
//   */
//
//  // Creates a Circle with this color in its field
//  Circle colorToCircle() {
//    return new Circle(this.color);
//  }
//
//  // Creates a MyColor with altered identifier (number field)
//  MyColor changeNum(int value) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: value ... int
//     * 
//     * Methods on Parameters:
//     */
//    return new MyColor(this.color, value);
//  }
//
//  // Compare two MyColors to see if they are equivalent
//  boolean compareColor(MyColor color) {
//    return this.color.equals(color.color);
//  }
//
//  boolean compare(int val) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: value ... int
//     * 
//     * Methods on Parameters:
//     */
//    return this.number == val;
//  }
//}
//
////-----------------------------NUMBERS------------------------------------------------------
//
//// To represent a list of numbers (used for testing)
//interface ILoNumber {
//}
//
//// To represent an empty list of numbers
//class MtLoNumber implements ILoNumber {
//  MtLoNumber() {
//  }
//}
//
//// To represent a non-empty list of numbers
//class ConsLoNumber implements ILoNumber {
//  int first;
//  ILoNumber rest;
//
//  // Main Constructor
//  ConsLoNumber(int first, ILoNumber rest) {
//    this.first = first;
//    this.rest = rest;
//  }
//}
////-----------------------------POSN---------------------------------------------------------
//
//// To represent positions that can be manipulated
//class MyPosn extends Posn {
//
//  // Standard constructor
//  MyPosn(int x, int y) {
//    super(x, y);
//  }
//
//  // Constructor to convert from a Posn to a MyPosn
//  MyPosn(Posn p) {
//    this(p.x, p.y);
//  }
//
//  /*
//   * CLASS TEMPLATE: Fields: this.x ... int this.y ... int
//   * 
//   * Methods: this.offsetPosn(MyPosn p) ... MyPosn
//   * this.placeImageOnScene(WorldScene scene, WorldImage image) ... WorldImage
//   * this.addPosn(MyPosn p) ... MyPosn this.drawPosn() ... WorldImage
//   * 
//   * Methods on Fields:
//   */
//
//  // Offsets the x of this MyPosn based upon its position in the list
//  MyPosn offsetPosn(int listPosition) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: listPosition ... int
//     * 
//     * Methods on Parameters:
//     */
//    return new MyPosn(this.x * listPosition, this.y);
//  }
//
//  // Adds the x and y values of two MyPosns together
//  MyPosn addPosn(MyPosn p) {
//    /*
//     * METHOD TEMPLATE: refer to class template
//     * 
//     * Parameters: p ... MyPosn
//     * 
//     * Methods on Parameters: this.p.offsetPosn(MyPosn p) ... MyPosn
//     * this.p.placeImageOnScene(WorldScene scene, WorldImage image) ... WorldImage
//     * this.p.addPosn(MyPosn p) ... MyPosn this.p.drawPosn() ... WorldImage
//     */
//    return new MyPosn(this.x + p.x, this.y + p.y);
//  }
//
//  // Creates an image of this MyPosn in text on a background
//  WorldImage drawPosn() {
//    return new OverlayImage(new TextImage(this.x + "  " + this.y, 60, Color.BLACK),
//        new RectangleImage(70 * 2, 70, OutlineMode.SOLID, Color.GRAY));
//  }
//}
//
////----------------------------EXAMPLES--------------------------------------------------------
//
//// To represent the examples and tests of Mastermind
//class ExamplesCircles {
//
//  // EXAMPLES:
//
//  Random randomSeed = new Random(10);
//
//  // Number
//  ILoNumber emptylon = new MtLoNumber();
//  ILoNumber randomLoNumber = new ConsLoNumber(this.randomSeed.nextInt(5),
//      new ConsLoNumber(this.randomSeed.nextInt(5),
//          new ConsLoNumber(this.randomSeed.nextInt(5), new ConsLoNumber(this.randomSeed.nextInt(5),
//              new ConsLoNumber(this.randomSeed.nextInt(5), new MtLoNumber())))));
//
//  // MyPosn
//  MyPosn pos1 = new MyPosn(2, 2);
//  MyPosn posn0 = new MyPosn(0, 0);
//  MyPosn posnneg = new MyPosn(-5, -4);
//  MyPosn posnpos = new MyPosn(3, 2);
//
//  // MyColor
//  MyColor MyColor1 = new MyColor(Color.RED);
//  MyColor MyColor2 = new MyColor(Color.BLUE);
//  MyColor MyColor3 = new MyColor(Color.GREEN);
//  MyColor mc5 = new MyColor(Color.RED, 1);
//  MyColor mc6 = new MyColor(Color.BLUE, -2);
//  MyColor mc7 = new MyColor(Color.GREEN, 0);
//
//  ILoMyColor emptylomc = new MtLoMyColor();
//  ILoMyColor funcolors = new ConsLoMyColor(new MyColor(Color.BLUE, 2),
//      new ConsLoMyColor(new MyColor(Color.GREEN, 3), new ConsLoMyColor(new MyColor(Color.PINK, 4),
//          new ConsLoMyColor(new MyColor(Color.CYAN, 5), this.emptylomc))));
//  ILoMyColor emptyColorList = new MtLoMyColor();
//  ILoMyColor colorlist1 = new ConsLoMyColor(new MyColor(Color.RED, 1),
//      new ConsLoMyColor(new MyColor(Color.BLUE, 2),
//          new ConsLoMyColor(new MyColor(Color.GREEN, 3),
//              new ConsLoMyColor(new MyColor(Color.WHITE, 4),
//                  new ConsLoMyColor(new MyColor(Color.BLACK, 5), this.emptyColorList)))));
//  ILoMyColor lomc1 = new ConsLoMyColor(this.MyColor1,
//      new ConsLoMyColor(this.MyColor2, this.emptylomc));
//  ILoMyColor colorlist2 = new ConsLoMyColor(new MyColor(Color.RED, 1),
//      new ConsLoMyColor(new MyColor(Color.BLUE, 5),
//          new ConsLoMyColor(new MyColor(Color.GREEN, 3), this.emptyColorList)));
//  ILoMyColor correctlist = new ConsLoMyColor(this.MyColor1,
//      new ConsLoMyColor(this.MyColor2, new ConsLoMyColor(this.MyColor3, new MtLoMyColor())));
//  ILoMyColor guesseslist = new ConsLoMyColor(this.MyColor1,
//      new ConsLoMyColor(this.MyColor2, new ConsLoMyColor(this.MyColor3, new MtLoMyColor())));
//  ILoMyColor lomc2 = new ConsLoMyColor(Color.RED, new ConsLoMyColor(Color.BLUE, this.emptylomc));
//
//  // Circle
//  Circle circle1 = new Circle(Color.black);
//  Circle circle2 = new Circle(Color.blue);
//  Circle circle3 = new Circle(Color.green);
//  Circle circle4 = new Circle(Color.red);
//  Circle circle5 = new Circle(50, this.posnpos, Color.red);
//  Circle circle6 = new Circle(60, this.posnpos, Color.blue);
//
//  ILoCircle emptyloc = new MtLoCircle();
//  ILoCircle loc1 = new ConsLoCircle(circle1, new ConsLoCircle(circle2,
//      new ConsLoCircle(circle3, new ConsLoCircle(circle4, this.emptyloc))));
//
//  // Mastermind
//  Mastermind game2 = new Mastermind(true, 5, 9, this.colorlist1);
//  Mastermind game3 = new Mastermind(true, 5, 5, this.colorlist1, this.randomSeed);
//  Mastermind game4 = new Mastermind(true, 4, 6, this.colorlist1, this.randomSeed, this.funcolors, 3,
//      1, this.colorlist1, new MtLoGuess(), new MtLoScore());
//
// 
//  // TESTS:
//
//  boolean testGuessesToList(Tester t) {
//    return t.checkExpect(this.game2.lengthToList(),
//        new ConsLoCircle(new Circle(Color.black),
//            new ConsLoCircle(new Circle(Color.black),
//                new ConsLoCircle(new Circle(Color.black), new ConsLoCircle(new Circle(Color.black),
//                    new ConsLoCircle(new Circle(Color.black), this.emptyloc))))));
//  }
//  
//  boolean testGame(Tester t) {
//    return this.game2.bigBang(1000, 1000);
//  }
//
//  boolean testAssignValues(Tester t) {
//    return t.checkExpect(this.lomc1.assignValues(), new ConsLoMyColor(new MyColor(Color.RED, 0),
//        new ConsLoMyColor(new MyColor(Color.BLUE, 1), new MtLoMyColor())));
//  }
//
//  boolean testFindMatches(Tester t) {
//    return t.checkExpect(this.correctlist.findMatches(this.guesseslist), new MyPosn(2, 0));
//  }
//
//  boolean testdraw(Tester t) {
//    WorldCanvas c = new WorldCanvas(900, 900);
//    WorldScene s = new WorldScene(900, 900);
//    return c.drawScene(s.placeImageXY(this.game4.drawCurGuessOffset(), 450, 450)) && c.show()
//        && c.show();
//  }
//
//  boolean testparse(Tester t) {
//    return t.checkExpect(7 == Integer.parseInt("7"), true);
//  }
//
//  boolean testFindMatch(Tester t) {
//    return t.checkExpect(this.colorlist1.findMatch(3), new MyColor(Color.GREEN, 3));
//  }
//
//  // Mastermind
//  boolean testlengthToList(Tester t) {
//    return t.checkExpect(this.game4.lengthToList(),
//        new ConsLoCircle(new Circle(Color.black), new ConsLoCircle(new Circle(Color.black),
//            new ConsLoCircle(new Circle(Color.black), new ConsLoCircle(new Circle(Color.black), 
//                new ConsLoCircle(new Circle(Color.black), this.emptyloc))))));
//  }
//
//  // List of Circles
//  boolean testlistlength(Tester t) {
//    return t.checkExpect(this.emptylomc.listLength(), 0)
//        && t.checkExpect(this.lomc1.listLength(), 2);
//  }
//
//  boolean testsameLoColor(Tester t) {
//    return t.checkExpect(this.emptylomc.sameLoColor(this.emptylomc), true)
//        && t.checkExpect(this.lomc2.sameLoColor(this.lomc2), true)
//        && t.checkExpect(this.colorlist2.sameLoColor(this.emptylomc), false)
//        && t.checkExpect(this.colorlist2.sameLoColor(this.lomc1), false);
//  }
//
//  // Circle
//  ILoCircle loc2 = new ConsLoCircle(this.circle1, new ConsLoCircle(this.circle5, this.emptyloc));
//
//  WorldImage c1 = new OverlayImage(new CircleImage(30, OutlineMode.SOLID, Color.BLACK),
//      (new RectangleImage((30 * 2) + 10, (30 * 2) + 10, OutlineMode.SOLID, Color.gray)));
//  WorldImage c5 = new OverlayImage(new CircleImage(50, OutlineMode.SOLID, Color.RED),
//      (new RectangleImage((50 * 2) + 10, (50 * 2) + 10, OutlineMode.SOLID, Color.gray)));
//  WorldImage c1mt = new OverlayImage(new CircleImage(30, OutlineMode.OUTLINE, Color.black),
//      (new RectangleImage((30 * 2) + 10, (30 * 2) + 10, OutlineMode.SOLID, Color.gray)));
//  WorldImage c5mt = new OverlayImage(new CircleImage(50, OutlineMode.SOLID, Color.RED),
//      (new RectangleImage((50 * 2) + 10, (50 * 2) + 10, OutlineMode.SOLID, Color.gray)));
//  WorldImage row = new BesideImage(this.c1, this.c5);
//  WorldImage mtrow = new BesideImage(this.c1mt, this.c5mt);
//
//  boolean testlistLength(Tester t) {
//    return t.checkExpect(this.emptyloc.listLength(), 0) && t.checkExpect(this.loc1.listLength(), 4)
//        && t.checkExpect(this.loc2.listLength(), 2);
//  }
//
//  boolean testdrawColorCircles(Tester t) {
//    return t.checkExpect(this.emptyloc.drawColorCircles(), new EmptyImage())
//        && t.checkExpect(this.loc2.drawColorCircles(), new BesideImage(c1, c5));
//  }
//
//  boolean testdrawEmptyCircles(Tester t) {
//    return t.checkExpect(this.emptyloc.drawEmptyCircles(), new EmptyImage())
//        && t.checkExpect(this.loc2.drawEmptyCircles(), new BesideImage(this.c1mt, this.c5mt));
//  }
//
//  boolean testdrawEmptyGameBoard(Tester t) {
//    return t.checkExpect(this.emptyloc.drawEmptyGameBoard(5), new EmptyImage())
//        && t.checkExpect(this.loc2.drawEmptyGameBoard(2), new AboveImage(this.mtrow, this.mtrow));
//  }
//
//  // Circle
//  boolean testdrawCircleOutline(Tester t) {
//    return t.checkExpect(this.circle1.drawCircleOutline(),
//        new OverlayImage(new CircleImage(30, OutlineMode.OUTLINE, Color.black),
//            (new RectangleImage((30 * 2) + 10, (30 * 2) + 10, OutlineMode.SOLID, Color.gray))))
//        && t.checkExpect(this.circle5.drawCircleOutline(),
//            new OverlayImage(new CircleImage(50, OutlineMode.OUTLINE, Color.black),
//                (new RectangleImage((50 * 2) + 10, (50 * 2) + 10, OutlineMode.SOLID, Color.gray))))
//        && t.checkExpect(this.circle6.drawCircleOutline(),
//            new OverlayImage(new CircleImage(60, OutlineMode.OUTLINE, Color.black),
//                (new RectangleImage((60 * 2) + 10, (60 * 2) + 10, OutlineMode.SOLID, Color.gray))));
//  }
//
//  boolean testdrawCircleFill(Tester t) {
//    return t.checkExpect(this.circle1.drawCircleFill(),
//        new OverlayImage(new CircleImage(30, OutlineMode.SOLID, Color.BLACK),
//            (new RectangleImage((30 * 2) + 10, (30 * 2) + 10, OutlineMode.SOLID, Color.gray))))
//        && t.checkExpect(this.circle5.drawCircleFill(),
//            new OverlayImage(new CircleImage(50, OutlineMode.SOLID, Color.RED),
//                (new RectangleImage((50 * 2) + 10, (50 * 2) + 10, OutlineMode.SOLID, Color.gray))))
//        && t.checkExpect(this.circle6.drawCircleFill(),
//            new OverlayImage(new CircleImage(60, OutlineMode.SOLID, Color.BLUE),
//                (new RectangleImage((60 * 2) + 10, (60 * 2) + 10, OutlineMode.SOLID, Color.gray))));
//  }
//
//  boolean testoffsetCircle(Tester t) {
//    return t.checkExpect(this.circle1.offsetCircle(4), new Circle(new MyPosn(160, 40), Color.BLACK))
//        && t.checkExpect(this.circle5.offsetCircle(2), new Circle(new MyPosn(6, 2), Color.RED))
//        && t.checkExpect(this.circle6.offsetCircle(3), new Circle(new MyPosn(9, 2), Color.BLUE));
//  }
//
//  // Utils
//  boolean testCheckRange(Tester t) {
//    Utils u = new Utils();
//    return t.checkExpect(u.checkRange(20, 10, "blerp"), 20)
//        && t.checkException(new IllegalArgumentException("boop"), u, "checkRange", -5, 0, "boop")
//        && t.checkException(new IllegalArgumentException("bop"), u, "checkRange", 10, 100, "bop");
//  }
//
//  boolean testduplicateException(Tester t) {
//    Utils u = new Utils();
//    return t.checkExpect(u.duplicateException(true, 5, 4, "blerp"), true)
//        && t.checkException(new IllegalArgumentException("boop"), u, "duplicateException", false, 5,
//            3, "boop")
//        && t.checkExpect(u.duplicateException(false, 4, 5, "blerp"), false);
//  }
//
//  boolean testcheckLength(Tester t) {
//    Utils u = new Utils();
//    return t.checkExpect(u.checkLength(this.colorlist1, 0, "blerp"), this.colorlist1)
//        && t.checkException(new IllegalArgumentException("boop"), u, "checkLength", this.colorlist1,
//            1000, "boop");
//  }
//
//  // Circle
//  boolean testcolorToCircle(Tester t) {
//    return t.checkExpect(this.mc5.colorToCircle(), new Circle(Color.RED))
//        && t.checkExpect(this.mc6.colorToCircle(), new Circle(Color.BLUE))
//        && t.checkExpect(this.mc7.colorToCircle(), new Circle(Color.GREEN));
//  }
//
//  boolean testchangeNum(Tester t) {
//    return t.checkExpect(this.mc5.changeNum(1), new MyColor(Color.RED, 2))
//        && t.checkExpect(this.mc6.changeNum(-2), new MyColor(Color.BLUE, -4))
//        && t.checkExpect(this.mc7.changeNum(-2), new MyColor(Color.GREEN, -2));
//  }
//
//  boolean testcompare(Tester t) {
//    return t.checkExpect(this.mc5.compare(1), true) && t.checkExpect(this.mc6.compare(2), false)
//        && t.checkExpect(this.MyColor3.compare(0), true);
//  }
//
//  // MyPosn
//  boolean testoffsetPosn(Tester t) {
//    return t.checkExpect(this.posn0.offsetPosn(5), this.posn0)
//        && t.checkExpect(this.posnneg.offsetPosn(0), new MyPosn(0, -4))
//        && t.checkExpect(this.posnpos.offsetPosn(-2), new MyPosn(-6, 2));
//  }
//
//  boolean testaddPosn(Tester t) {
//    return t.checkExpect(this.posn0.addPosn(this.posnneg), this.posnneg)
//        && t.checkExpect(this.posnneg.addPosn(this.posnpos), new MyPosn(-2, -2))
//        && t.checkExpect(this.posnpos.addPosn(this.posn0), this.posnpos);
//  }
//
//  boolean testdrawPosn(Tester t) {
//    return t.checkExpect(this.posn0.drawPosn(),
//        new OverlayImage(new TextImage(0 + "  " + 0, 60, Color.BLACK),
//            new RectangleImage(70 * 2, 70, OutlineMode.SOLID, Color.GRAY)))
//        && t.checkExpect(this.posnneg.drawPosn(),
//            new OverlayImage(new TextImage(-5 + "  " + -4, 60, Color.BLACK),
//                new RectangleImage(70 * 2, 70, OutlineMode.SOLID, Color.GRAY)))
//        && t.checkExpect(this.posnpos.drawPosn(),
//            new OverlayImage(new TextImage(3 + "  " + 2, 60, Color.BLACK),
//                new RectangleImage(70 * 2, 70, OutlineMode.SOLID, Color.GRAY)));
//  }
//
//}