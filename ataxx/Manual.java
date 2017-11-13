package ataxx;

/** A Player that receives its moves from its Game's getMoveCmnd method.
 *  @author tapan.jasthi
 */
class Manual extends Player {

    /** A Player that will play MYCOLOR on GAME, taking its moves from
     *  GAME. */
    Manual(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove() {
        Command cmnd = game().getMoveCmnd(myColor() + ": ");
        if (cmnd != null) {
            if (cmnd.commandType() == Command.Type.PIECEMOVE) {
                char c0 = cmnd.operands()[0].charAt(0);
                char r0 = cmnd.operands()[1].charAt(0);
                char c1 = cmnd.operands()[2].charAt(0);
                char r1 = cmnd.operands()[3].charAt(0);
                Move move = Move.move(c0, r0, c1, r1);
                return move;
            } else if (cmnd.commandType() == Command.Type.PASS) {
                Move move = Move.pass();
                return move;
            }
        }
        return null;
    }
}

