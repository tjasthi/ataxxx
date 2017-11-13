package ataxx;
import java.util.ArrayList;

/** A Player that computes its own moves.
 *  @author tapan.jasthi
 */
class AI extends Player {

    /**
     * Maximum minimax search depth before going to static evaluation.
     */
    private static final int MAX_DEPTH = 4;

    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new AI for GAME that will play MYCOLOR.
     */
    AI(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove() {
        Move move;
        if (!board().canMove(myColor())) {
            return Move.pass();
        } else {
            move = findMove();
        }
        return move;
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(board());
        if (myColor() == board().whoseMove()) {
            findMove(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            findMove(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /**
     * Used to communicate best moves found by findMove, when asked for.
     */
    private Move _lastFoundMove;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _lastFoundMove iff SAVEMOVE. The move
     * should have maximal value or have value >= BETA if SENSE==1,
     * and minimal value or value <= ALPHA if SENSE==-1. Searches up to
     * DEPTH levels before using a static estimate.
     */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        if (depth == 0 || board.gameOver()) {
            return staticScore(board);
        } else if (sense == 1) {
            ArrayList<Move> possibleMoves =
                    Board.getMoveArray(board, myColor());
            for (Move x : possibleMoves) {
                if (!board.legalMove(x)) {
                    continue;
                }
                board.makeMove(x);
                int max = findMove(board, depth - 1, false, sense, alpha, beta);
                if (alpha <= max) {
                    if (saveMove) {
                        _lastFoundMove = x;
                    }
                    alpha = max;
                }
                board.undo();
                if (beta <= alpha) {
                    break;
                }
            }
            return alpha;
        } else {
            ArrayList<Move> possibleMoves =
                    Board.getMoveArray(board, myColor());
            for (Move x : possibleMoves) {
                if (!board.legalMove(x)) {
                    continue;
                }
                board.makeMove(x);
                int min = findMove(board, depth - 1, false, sense, alpha, beta);
                if (beta > min) {
                    if (saveMove) {
                        _lastFoundMove = x;
                    }
                    beta = min;
                }
                board.undo();
                if (beta <= alpha) {
                    break;
                }
            }
            return beta;
        }
    }

    /** Returns a heuristic value for BOARD.
     */
    private int staticScore(Board board) {
        int piecesPlayer = board.numPieces(board.whoseMove());
        int piecesOpponent = board.numPieces(board.whoseMove().opposite());
        if (board.gameOver() && (piecesPlayer > piecesOpponent)) {
            return INFTY;
        } else if (board.gameOver() && (piecesPlayer < piecesOpponent)) {
            return -INFTY;
        } else if (board.gameOver() && (piecesPlayer == piecesOpponent)) {
            return 0;
        } else if (!board.canMove(myColor())) {
            return (piecesPlayer - piecesOpponent) / 2;
        } else {
            return piecesPlayer - piecesOpponent;
        }
    }
}
