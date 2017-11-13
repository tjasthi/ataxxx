package ataxx;

import org.junit.Test;
import ucb.junit.textui;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/** The suite of all JUnit tests for the ataxx package.
 *  @author tapan.jasthi
 */
public class UnitTest {

    @Test
    public void colRowTest() {
        assertEquals(Board.colInd(90), 'a');
        assertEquals(Board.colInd(26), 'c');
        assertEquals(Board.rowInd(50), '3');
        assertEquals(Board.rowInd(36), '2');
    }

    @Test
    public void toStringTest() {
        Board b = new Board();
        assertEquals(b.toString(false),
                "===\n"
                        + "  r - - - - - b\n"
                        + "  - - - - - - -\n"
                        + "  - - - - - - -\n"
                        + "  - - - - - - -\n"
                        + "  - - - - - - -\n"
                        + "  - - - - - - -\n"
                        + "  b - - - - - r\n"
                        + "===");
        assertEquals(b.toString(true),
                "===\n"
                        + "  7 r - - - - - b\n"
                        + "  6 - - - - - - -\n"
                        + "  5 - - - - - - -\n"
                        + "  4 - - - - - - -\n"
                        + "  3 - - - - - - -\n"
                        + "  2 - - - - - - -\n"
                        + "  1 b - - - - - r\n"
                        + "    a b c d e f g\n"
                        + "===");
    }

    @Test
    public void blockTest() {
        Board b0 = new Board();
        b0.setBlock("b3");
        b0.setBlock("c3");

        assertEquals(b0.get('b', '3'), PieceColor.BLOCKED);
        assertEquals(b0.get('c', '3'), PieceColor.BLOCKED);
        assertEquals(b0.get('b', '5'), PieceColor.BLOCKED);
        assertEquals(b0.get('e', '3'), PieceColor.BLOCKED);
    }

    @Test
    public void extendTest() {
        Board b0 = new Board();
        Move m;

        m = Move.move('a', '7', 'b', '7');
        b0.makeMove(m);
        assertEquals(m.isJump(), false);


        m = Move.move('g', '7', 'f', '6');
        b0.makeMove(m);
        assertEquals(m.isJump(), false);

        assertEquals(b0.get('a', '7'), PieceColor.RED);
        assertEquals(b0.get('b', '7'), PieceColor.RED);
        assertEquals(b0.get('f', '6'), PieceColor.BLUE);
        assertEquals(b0.get('g', '7'), PieceColor.BLUE);
    }

    @Test
    public void jumpTest() {
        Board b0 = new Board();
        Move m;
        m = Move.move('a', '7', 'b', '5');
        b0.makeMove(m);
        assertEquals(b0.get('a', '7'), PieceColor.EMPTY);
        assertEquals(b0.get('b', '5'), PieceColor.RED);
        assertEquals(m.isJump(), true);

        m = Move.move('g', '7', 'e', '6');
        b0.makeMove(m);
        assertEquals(b0.get('g', '7'), PieceColor.EMPTY);
        assertEquals(b0.get('e', '6'), PieceColor.BLUE);
        assertEquals(m.isJump(), true);

        m = Move.move('g', '1', 'g', '3');
        b0.makeMove(m);
        assertEquals(b0.get('g', '1'), PieceColor.EMPTY);
        assertEquals(b0.get('g', '3'), PieceColor.RED);
        assertEquals(m.isJump(), true);

        m = Move.move('e', '6', 'e', '4');
        b0.makeMove(m);
        assertEquals(b0.get('e', '4'), PieceColor.BLUE);
        assertEquals(b0.get('e', '6'), PieceColor.EMPTY);
        assertEquals(m.isJump(), true);
    }

    @Test
    public void colorTest() {
        Board b0 = new Board();
        Move m;

        m = Move.move('a', '7', 'b', '7');
        b0.makeMove(m);

        m = Move.move('g', '7', 'e', '6');
        b0.makeMove(m);

        m = Move.move('a', '7', 'b', '6');
        b0.makeMove(m);

        m = Move.move('e', '6', 'e', '7');
        b0.makeMove(m);

        m = Move.move('b', '7', 'c', '7');
        b0.makeMove(m);

        m = Move.move('e', '6', 'd', '5');
        b0.makeMove(m);

        m = Move.move('c', '7', 'd', '6');
        b0.makeMove(m);

        assertEquals(b0.get('d', '5'), PieceColor.RED);
        assertEquals(b0.get('e', '6'), PieceColor.RED);
        assertEquals(b0.get('e', '7'), PieceColor.RED);
        assertEquals(b0.get('c', '7'), PieceColor.RED);
    }

    @Test
    public void clearTest() {
        Board b0 = new Board();
        Board b1 = new Board(b0);
        Move m;

        m = Move.move('a', '7', 'b', '7');
        b0.makeMove(m);

        m = Move.move('g', '7', 'e', '6');
        b0.makeMove(m);

        m = Move.move('a', '7', 'b', '6');
        b0.makeMove(m);

        m = Move.move('e', '6', 'e', '7');
        b0.makeMove(m);

        m = Move.move('b', '7', 'c', '7');
        b0.makeMove(m);

        m = Move.move('e', '6', 'd', '5');
        b0.makeMove(m);

        m = Move.move('c', '7', 'd', '6');
        b0.makeMove(m);
        b0.clear();

        assertEquals(b0, b1);
    }

    @Test
    public void undoTest() {
        Board b0 = new Board();
        Board b1 = new Board(b0);

        b0.makeMove('a', '7', 'a', '5');
        Board b2 = new Board(b0);

        b0.makeMove('a', '1', 'b', '1');

        Board b3 = new Board(b0);

        b0.makeMove('a', '5', 'a', '4');
        Board b4 = new Board(b0);

        b0.makeMove('a', '1', 'a', '2');
        Board b5 = new Board(b0);

        b0.makeMove('a', '4', 'b', '2');

        b0.undo();
        assertEquals(b0, b5);

        b0.undo();
        assertEquals(b0, b4);

        b0.undo();
        assertEquals(b0, b3);

        b0.undo();
        assertEquals(b0, b2);

        b0.undo();
        assertEquals(b0, b1);
    }

    @Test
    public void turnTest() {
        Board b0 = new Board();
        b0.setBlock("b3");
        b0.setBlock("c3");
        assertEquals(b0.whoseMove(), PieceColor.RED);
        b0.makeMove('a', '7', 'a', '5');
        assertEquals(b0.whoseMove(), PieceColor.BLUE);
        b0.makeMove('a', '1', 'b', '1');
        assertEquals(b0.whoseMove(), PieceColor.RED);
        b0.makeMove('a', '5', 'a', '4');
        assertEquals(b0.whoseMove(), PieceColor.BLUE);
        b0.makeMove('a', '1', 'a', '2');
        assertEquals(b0.whoseMove(), PieceColor.RED);
        b0.makeMove('a', '4', 'b', '2');
        assertEquals(b0.whoseMove(), PieceColor.BLUE);
    }

    @Test
    public void piecesMovesTest() {
        Board b2 = new Board();
        assertEquals(b2.numPieces(PieceColor.RED), 2);
        assertEquals(b2.numPieces(PieceColor.BLUE), 2);
        assertEquals(b2.numMoves(), 0);
        assertEquals(b2.numJumps(), 0);

        Move m = Move.move('a', '7', 'b', '7');
        b2.makeMove(m);
        assertEquals(b2.numPieces(PieceColor.RED), 3);
        assertEquals(b2.numPieces(PieceColor.BLUE), 2);

        m = Move.move('g', '7', 'e', '6');
        b2.makeMove(m);
        assertEquals(b2.numPieces(PieceColor.RED), 3);
        assertEquals(b2.numPieces(PieceColor.BLUE), 2);

        assertEquals(b2.whoseMove(), PieceColor.RED);
        assertEquals(b2.numMoves(), 1);
        assertEquals(b2.numJumps(), 1);
    }

    @Test
    public void gameoverTest() {
        Board b = new Board();
        Move m = Move.move('a', '7', 'a', '5');
        Move n = Move.move('a', '5', 'a', '7');
        Move x = Move.move('a', '1', 'a', '3');
        Move y = Move.move('a', '3', 'a', '1');

        for (int i = 0; i < 6; i += 1) {
            b.makeMove(m);
            b.makeMove(x);
            b.makeMove(n);
            b.makeMove(y);
        }

        assertEquals(b.gameOver(), false);
        b.makeMove(m);
        assertEquals(b.gameOver(), true);
    }

    @Test
    public void testGetListOfMoves() {
        Board b1 = new Board();
        b1.setBlock('c', '3');

        ArrayList<Move> listOfMoves = Board.getMoveArray(b1, PieceColor.RED);
        assertEquals(14, listOfMoves.size());


        b1.makeMove('a', '7', 'c', '6');
        listOfMoves = Board.getMoveArray(b1, PieceColor.RED);
        assertEquals(24, listOfMoves.size());

        b1.makeMove('g', '7', 'e', '6');
        listOfMoves = Board.getMoveArray(b1, PieceColor.BLUE);
        assertEquals(23, listOfMoves.size());
    }

    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(CommandTest.class, MoveTest.class,
                          BoardTest.class, UnitTest.class);
    }
}


