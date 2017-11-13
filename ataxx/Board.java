package ataxx;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;
import java.util.Formatter;
import java.util.Observable;
import static ataxx.PieceColor.*;

/** An Ataxx board.   The squares are labeled by column (a char value between
 *  'a' - 2 and 'g' + 2) and row (a char value between '1' - 2 and '7'
 *  + 2) or by linearized index, an integer described below.  Values of
 *  the column outside 'a' and 'g' and of the row outside '1' to '7' denote
 *  two layers of border squares, which are always blocked.
 *  This artificial border (which is never actually printed) is a common
 *  trick that allows one to avoid testing for edge conditions.
 *  For example, to look at all the possible moves from a square, sq,
 *  on the normal board (i.e., not in the border region), one can simply
 *  look at all squares within two rows and columns of sq without worrying
 *  about going off the board. Since squares in the border region are
 *  blocked, the normal logic that prevents moving to a blocked square
 *  will apply.
 *
 *  For some purposes, it is useful to refer to squares using a single
 *  integer, which we call its "linearized index".  This is simply the
 *  number of the square in row-major order (counting from 0).
 *
 *  Moves on this board are denoted by Moves.
 *  @author tapan.jasthi
 */
class Board extends Observable {

    /** Number of squares on a side of the board. */
    static final int SIDE = 7;
    /** Length of a side + an artificial 2-deep border region. */
    static final int EXTENDED_SIDE = SIDE + 4;

    /** Number of non-extending moves before game ends. */
    static final int JUMP_LIMIT = 25;

    /** A new, cleared board at the start of the game. */
    Board() {
        _board = new PieceColor[EXTENDED_SIDE * EXTENDED_SIDE];
        Arrays.fill(_board, PieceColor.BLOCKED);
        clear();
    }

    /** A copy of B. */
    Board(Board b) {
        this._board = b._board.clone();
        this._whoseMove = b.whoseMove();
        this.numMoves = b.numMoves();
        this.numJumps = b.numJumps();
        this.moveStack = (Stack) b.moveStack.clone();
        this.moveMap = (HashMap<Move, ArrayList<String>>) b.moveMap.clone();
    }

    /** Return the linearized index of square COL ROW. */
    static int index(char col, char row) {
        return (row - '1' + 2) * EXTENDED_SIDE + (col - 'a' + 2);
    }

    /** Return the column from a linearized int index.
     * @param index is the index of the square. */
    static char colInd(int index) {
        int ret = (index % EXTENDED_SIDE) - 2 + 'a';
        return (char) ret;
    }

    /** Return the row from a linearized int index.
     * @param index is the index of the square. */
    static char rowInd(int index) {
        int ret = (index / EXTENDED_SIDE) - 2 + '1';
        return (char) ret;
    }

    /** Return the linearized index of the square that is DC columns and DR
     *  rows away from the square with index SQ. */
    static int neighbor(int sq, int dc, int dr) {
        return sq + dc + dr * EXTENDED_SIDE;
    }

    /** Clear me to my starting state, with pieces in their initial
     *  positions and no blocks. */
    void clear() {
        char col = 'a', row = '1';
        for (int square = 1; square <= SIDE * SIDE; square++) {
            set(col, row, PieceColor.EMPTY);
            if (square % 7 != 0) {
                col += 1;
            } else {
                col = 'a';
                row += 1;
            }
        }
        _whoseMove = RED;
        set('a', '7', PieceColor.RED);
        set('g', '1', PieceColor.RED);
        set('a', '1', PieceColor.BLUE);
        set('g', '7', PieceColor.BLUE);

        numMoves = 0;
        numJumps = 0;
        moveStack = new Stack<>();
        moveMap = new HashMap<>();
        setChanged();
        notifyObservers();
    }

    /** Return true iff the game is over: i.e., if neither side has
     *  any moves, if one side has no pieces, or if there have been
     *  MAX_JUMPS consecutive jumps without intervening extends. */
    boolean gameOver() {
        if (numJumps == JUMP_LIMIT) {
            return true;
        } else if (numPieces(RED) == 0 || numPieces(BLUE) == 0) {
            return true;
        } else {
            return (!canMove(RED) && (!canMove(BLUE)));
        }
    }

    /** Return number of red pieces on the board. */
    int redPieces() {
        return numPieces(RED);
    }

    /** Return number of blue pieces on the board. */
    int bluePieces() {
        return numPieces(BLUE);
    }

    /** Return number of COLOR pieces on the board. */
    int numPieces(PieceColor color) {
        int count = 0;
        for (PieceColor x: _board) {
            if (x == color) {
                count += 1;
            }
        }
        return count;
    }

    /** The current contents of square CR, where 'a'-2 <= C <= 'g'+2, and
     *  '1'-2 <= R <= '7'+2.  Squares outside the range a1-g7 are all
     *  BLOCKED.  Returns the same value as get(index(C, R)). */
    PieceColor get(char c, char r) {
        return _board[index(c, r)];
    }

    /** Return the current contents of square with linearized index SQ. */
    PieceColor get(int sq) {
        return _board[sq];
    }

    /** Set get(C, R) to V, where 'a' <= C <= 'g', and
     *  '1' <= R <= '7'. */
    private void set(char c, char r, PieceColor v) {
        set(index(c, r), v);
    }

    /** Set square with linearized index SQ to V.  This operation is
     *  undoable. */
    private void set(int sq, PieceColor v) {
        _board[sq] = v;
    }

    /** Set square at C R to V (not undoable). */
    private void unrecordedSet(char c, char r, PieceColor v) {
        _board[index(c, r)] = v;
    }

    /** Set square at linearized index SQ to V (not undoable). */
    private void unrecordedSet(int sq, PieceColor v) {
        _board[sq] = v;
    }

    /** Return true iff MOVE is legal on the current board.
     * The end square should be empty. */
    boolean legalMove(Move move) {
        if (get(move.col0(), move.row0()) != _whoseMove) {
            infoMessage = "illegal piece movement, not a "
                    + _whoseMove + " piece";
            return false;
        } else if (move.col1() < 'a' || move.row1() < '1'
                || move.col1() > 'g' || move.row1() > '7') {
            infoMessage = "illegal piece movement, square out of board";
            return false;
        } else if (move.col0() < 'a' || move.row0() < '1'
                || move.col0() > 'g' || move.row0() > '7') {
            infoMessage = "illegal piece movement, square out of board";
            return false;
        } else if (move == null) {
            infoMessage = "move is null";
            return false;
        } else if (get(move.col1(), move.row1()) == EMPTY) {
            return true;
        } else {
            infoMessage = "illegal piece movement, end square not empty";
            return false;
        }
    }


    /** Return true iff player WHO can move, ignoring whether it is
     *  that player's move and whether the game is over. */
    boolean canMove(PieceColor who) {
        for (int i = 0; i < _board.length; i += 1) {
            if (get(i) == who) {
                if (emptySurr(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns true if there is an empty square surrounding
     *  piece at index int i.
     *  @param i is the index of the square. */
    boolean emptySurr(int i) {
        char r = rowInd(i);
        char c = colInd(i);
        for (int x = -2; x <= 2; x += 1) {
            for (int y = -2; y <= 2; y += 1) {
                int rMove = r + x;
                int cMove = c + y;
                if (get((char) cMove, (char) rMove) == EMPTY) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Return the color of the player who has the next move.  The
     *  value is arbitrary if gameOver(). */
    PieceColor whoseMove() {
        return _whoseMove;
    }

    /** Return total number of moves and passes since the last
     *  clear or the creation of the board. */
    int numMoves() {
        return numMoves;
    }

    /** Return number of non-pass moves made in the current game since the
     *  last extend move added a piece to the board (or since the
     *  start of the game). Used to detect end-of-game. */
    int numJumps() {
        return numJumps;
    }

    /** Perform the move C0R0-C1R1, or pass if C0 is '-'.  For moves
     *  other than pass, assumes that legalMove(C0, R0, C1, R1). */
    void makeMove(char c0, char r0, char c1, char r1) {
        if (c0 == '-') {
            makeMove(Move.pass());
        } else {
            makeMove(Move.move(c0, r0, c1, r1));
        }
    }

    /** Make the MOVE on this Board, assuming it is legal. */
    void makeMove(Move move) {
        if (move.isPass()) {
            pass();
            return;
        }
        if (move.isJump()) {
            _board[index(move.col0(), move.row0())] = EMPTY;
            _board[index(move.col1(), move.row1())] = _whoseMove;
            numJumps += 1;
        } else {
            _board[index(move.col1(), move.row1())] = _whoseMove;
            numMoves += 1;
        }
        ArrayList<String> changedSquared = changeColor(move);
        moveStack.push(move);
        moveMap.put(move, changedSquared);
        _whoseMove = _whoseMove.opposite();
        setChanged();
        notifyObservers();
    }

    /** Change color of surrounding piece after doing Move m.
     * Returns an array of changed colors.
     * @param m is the move that is being made. */
    ArrayList<String> changeColor(Move m) {
        ArrayList<String> changedSquares = new ArrayList<>();
        for (int x = -1; x <= 1; x += 1) {
            for (int y = -1; y <= 1; y += 1) {
                int rMove = m.row1() + x;
                int cMove = m.col1() + y;
                int spot = index((char) cMove, (char) rMove);
                if ((get(spot) != _whoseMove) && (get(spot).isPiece())
                        && (y != 0 || x != 0)) {
                    set(spot, whoseMove());
                    changedSquares.add((char) cMove + "" + (char) rMove);
                }
            }
        }
        return changedSquares;
    }

    /** Causes the current player can pass.
     * The only effect is to change whoseMove(). */
    void pass() {
        if (!canMove(_whoseMove)) {
            _whoseMove = _whoseMove.opposite();
            setChanged();
            notifyObservers();
        }
    }

    /** Undo the last move. */
    void undo() {
        Move lastMove = moveStack.pop();

        if (lastMove.isJump()) {
            numJumps -= 1;
            set(lastMove.col0(), lastMove.row0(), _whoseMove.opposite());
            set(lastMove.col1(), lastMove.row1(), EMPTY);
        } else {
            numMoves -= 1;
            set(lastMove.col1(), lastMove.row1(), EMPTY);
        }
        ArrayList<String> changedColors = moveMap.get(lastMove);
        for (String x : changedColors) {
            set(x.charAt(0), x.charAt(1), _whoseMove);
        }

        _whoseMove = _whoseMove.opposite();
        moveMap.remove(lastMove);
        setChanged();
        notifyObservers();
    }

    /** Return true iff it is legal to place a block at C R. */
    boolean legalBlock(char c, char r) {
        return (get(c, r).equals(EMPTY));
    }

    /** Return true iff it is legal to place a block at CR. */
    boolean legalBlock(String cr) {
        return legalBlock(cr.charAt(0), cr.charAt(1));
    }

    /** Returns true if it is legal to set a block on a given square.
     *  Set a block on the square C R and its reflections across the middle
     *  row and/or column, if that square is unoccupied and not
     *  in one of the corners. Has no effect if any of the squares is
     *  already occupied by a block.  It is an error to place a block on a
     *  piece. */
    boolean setBlock(char c, char r) {
        int cIntReflect = 'a' + ('g' - c);
        int rIntReflect = '1' + ('7' - r);
        char cCharReflect = (char) cIntReflect;
        char rCharReflect = (char) rIntReflect;

        if ((!legalBlock(c, r)) || (!legalBlock(cCharReflect, r))
                || (!legalBlock(c, rCharReflect))
                || (!legalBlock(cCharReflect, rCharReflect))) {
            return false;
        }
        set(c, r, BLOCKED);
        set(cCharReflect, r, BLOCKED);
        set(c, rCharReflect, BLOCKED);
        set(cCharReflect, rCharReflect, BLOCKED);

        setChanged();
        notifyObservers();
        return true;
    }

    /** Returns true if legal to set a block at CR. */
    boolean setBlock(String cr) {
        return setBlock(cr.charAt(0), cr.charAt(1));
    }

    /** Returns an error string when called. */
    public String getInfoMessage() {
        return infoMessage;
    }

    /** Return a list of a potential moves of a certain color
     * depending given a board b.
     * @param b is the board that is being examined.
     * @param color is the color of the piece that is being examined. */
    public static ArrayList<Move> getMoveArray(Board b, PieceColor color) {
        ArrayList<Move> out = new ArrayList();
        char row = '1';
        char col = 'a';
        for (int i = 0; i < 7; i += 1) {
            for (int j = 0; j < 7; j += 1) {
                int colInt = i + col;
                int rowInt = j + row;
                if (color == b.get((char) colInt, (char) rowInt)) {
                    for (int x = -2; x <= 2; x += 1) {
                        for (int y = -2; y <= 2; y += 1) {
                            int rMove = rowInt + x;
                            int cMove = colInt + y;
                            if ((b.get((char) cMove,
                                    (char) rMove).equals(EMPTY))
                                    && !((x == 0) && (y == 0))) {
                                out.add(Move.move((char) colInt, (char) rowInt,
                                        (char) cMove, (char) rMove));
                            }
                        }
                    }
                }
            }
        }
        return out;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    @Override
    public boolean equals(Object obj) {
        Board other = (Board) obj;
        if (other._whoseMove.equals(this._whoseMove)) {
            if ((other.numMoves == this.numMoves)
                    && (other.numJumps == this.numJumps)) {
                if ((other.moveMap.equals(this.moveMap))
                        && (other.moveStack.equals(this.moveStack))) {
                    return Arrays.equals(this._board, other._board);
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(_board);
    }

    /** Return a text depiction of the board (not a dump).  If LEGEND,
     *  supply row and column numbers around the edges. */
    String toString(boolean legend) {
        Formatter out = new Formatter();
        out.format("===");
        char col = 'a', row = '7';
        for (int square = 0; square < SIDE * SIDE; square += 1) {
            String spot;
            if (get(col, row) == EMPTY) {
                spot = "-";
            } else if (get(col, row) == BLUE) {
                spot = "b";
            } else if (get(col, row) == RED) {
                spot = "r";
            } else {
                spot = "X";
            }

            if (square % 7 == 0) {
                if (legend) {
                    out.format("\n  " + row + " ");
                } else {
                    out.format("\n  ");
                }
                out.format(spot + " ");
                col += 1;
            } else if (square % 7 == 6) {
                out.format(spot);
                row -= 1;
                col = 'a';
            } else {
                out.format(spot + " ");
                col += 1;
            }
        }
        if (legend) {
            out.format("\n    a b c d e f g");
        }
        out.format("\n===");
        return out.toString();
    }

    /** Returns moveStack of the board. */
    public Stack<Move> getMoveStack() {
        return moveStack;
    }

    /** Returns moveMap of the board. */
    public HashMap<Move, ArrayList<String>> getMoveMap() {
        return moveMap;
    }

    /** For reasons of efficiency in copying the board,
     *  we use a 1D array to represent it, using the usual access
     *  algorithm: row r, column c => index(r, c).
     *
     *  Next, instead of using a 7x7 board, we use an 11x11 board in
     *  which the outer two rows and columns are blocks, and
     *  row 2, column 2 actually represents row 0, column 0
     *  of the real board.  As a result of this trick, there is no
     *  need to special-case being near the edge: we don't move
     *  off the edge because it looks blocked.
     *
     *  Using characters as indices, it follows that if 'a' <= c <= 'g'
     *  and '1' <= r <= '7', then row c, column r of the board corresponds
     *  to board[(c -'a' + 2) + 11 (r - '1' + 2) ], or by a little
     *  re-grouping of terms, board[c + 11 * r + SQUARE_CORRECTION]. */
    private final PieceColor[] _board;

    /** Player that is on move. */
    private PieceColor _whoseMove;

    /** Returns the number of extensions made. */
    private int numMoves;

    /** Returns the number of jumps made. */
    private int numJumps;

    /** Hashmap object containing moves and changed colors. */
    private HashMap<Move, ArrayList<String>> moveMap;

    /** Store moves made in game to support Undo(). */
    private Stack<Move> moveStack;

    /** String object to store error messages. */
    private String infoMessage;
}
