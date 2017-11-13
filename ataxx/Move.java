package ataxx;

/** Represents an Ataxx move. There is one Move object created for
 *  each distinct Move.
 *  @author tapan.jasthi
 */
class Move {

    /** The move COL0 ROW0 - COL1 ROW1.  This must be a legal move. */
    private Move(int col0, int row0, int col1, int row1) {
        _col0 = (char) (col0 + 'a' - 2);
        _row0 = (char) (row0 + '1' - 2);
        _col1 = (char) (col1 + 'a' - 2);
        _row1 = (char) (row1 + '1' - 2);
        _fromIndex = row0 * EXTENDED_SIDE + col0;
        _toIndex = row1 * EXTENDED_SIDE + col1;
    }

    /** A pass. */
    private Move() {
        _col0 = _col1 = _row0 = _row1 = 0;
        _fromIndex = _toIndex = -1;
    }

    /** A factory method that returns a Move from COL0 ROW0 to COL1 ROW1,
     *  assuming the column and row designations are valid. Returns null
     *  if no such move is ever possible because it is more than 2 squares
     *  in some direction.  The moves are on the extended board (i.e., they
     *  may go into the border layers). */
    static Move move(char col0, char row0, char col1, char row1) {
        return
            ALL_MOVES[col0 - 'a' + 2][row0 - '1' + 2]
            [col1 - 'a' + 2][row1 - '1' + 2];
    }

    /** Returns a pass. */
    static Move pass() {
        return PASS;
    }

    /** Return true iff I am a pass. */
    boolean isPass() {
        return this == PASS;
    }

    /** Return true if this is an extension (move to adjacent square). */
    boolean isExtend() {
        int colDif = Math.abs(this.col0() - this.col1());
        int rowDif = Math.abs(this.row0() - this.row1());
        if ((colDif > 1) || (rowDif > 1)) {
            return false;
        }
        return !((colDif == 0) && (rowDif == 0));
    }

    /** Return true if this is a jump (move to nonadjacent square). */
    boolean isJump() {
        int colDif = Math.abs(this.col0() - this.col1());
        int rowDif = Math.abs(this.row0() - this.row1());
        return ((colDif == 2) || (rowDif == 2));
    }

    /** Returns from column.  Undefined if a pass. */
    char col0() {
        return _col0;
    }

    /** Returns from row.  Undefined if a pass. */
    char row0() {
        return _row0;
    }

    /** Returns to column.  Undefined if a pass. */
    char col1() {
        return _col1;
    }

    /** Returns to row.  Undefined if a pass. */
    char row1() {
        return _row1;
    }

    /** Return the linearized index of my 'from' square,
     *  or -1 if I am a pass. */
    int fromIndex() {
        return _fromIndex;
    }

    /** Return The linearized index of my 'to' square,
     *  or -1 if I am a pass. */
    int toIndex() {
        return _toIndex;
    }

    @Override
    public String toString() {
        if (this.isPass()) {
            return "-";
        } else {
            return this.col0() + "" + this.row0()
                    + "-" + this.col1() + "" + this.row1();
        }
    }

    /** Size of a side of the board. */
    static final int SIDE = 7;

    /** Size of side of a board plus 2-deep boundary. */
    static final int EXTENDED_SIDE = SIDE + 4;

    /** The pass. */
    static final Move PASS = new Move();

    /** Linearized indices. */
    private final int _fromIndex, _toIndex;

    /** From and two squares, or 0s if a pass. */
    private char _col0, _row0, _col1, _row1;

    /** The set of all Moves other than pass, indexed by from and to row and
     *  column positions. */
    private static final Move[][][][] ALL_MOVES =
        new Move[EXTENDED_SIDE][EXTENDED_SIDE][EXTENDED_SIDE][EXTENDED_SIDE];

    /** A "static initializer".  These code sections are run when the class
     * is initialized and are intended to initialize static variables. */
    static {
        for (int c = 2; c < SIDE + 2; c += 1) {
            for (int r = 2; r < SIDE + 2; r += 1) {
                for (int dc = -2; dc <= 2; dc += 1) {
                    for (int dr = -2; dr <= 2; dr += 1) {
                        if (dc != 0 || dr != 0) {
                            ALL_MOVES[c][r][c + dc][r + dr] =
                                new Move(c, r, c + dc, r + dr);
                        }
                    }
                }
            }
        }
    }
}
