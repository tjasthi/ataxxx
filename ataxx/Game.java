package ataxx;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;
import static ataxx.Game.State.*;
import static ataxx.Command.Type.*;

/** Controls the play of the game.
 *  @author tapan.jasthi
 */
class Game {

    /** States of play. */
    enum State {
        SETUP, PLAYING, FINISHED
    }

    /** A new Game, using BOARD to play on, reading initially from
     *  BASESOURCE and using REPORTER for error and informational messages. */
    Game(Board board, CommandSource baseSource, Reporter reporter) {
        _inputs.addSource(baseSource);
        _board = board;
        _reporter = reporter;
        _state = SETUP;
    }

    /** Run a session of Ataxx gaming.  Use an AtaxxGUI iff USEGUI. */
    void process(boolean useGUI) {

        GameLoop:
        while (true) {
            doClear(null);

            SetupLoop:
            while (_state.equals(SETUP)) {
                doCommand();
            }
            PlayingLoop:
            while (!_state.equals(SETUP)) {
                if (_state.equals(PLAYING)) {
                    Move move = currentPlayer.myMove();
                    if ((_state == SETUP)) {
                        continue;
                    }
                    if ((move == null)) {
                        _reporter.errMsg("illegal move, out of range");
                        continue;
                    }
                    if (move == Move.PASS) {
                        doPass(new String[] {"-"});
                    } else {
                        String col0 = move.col0() + "";
                        String row0 = move.row0() + "";
                        String col1 = move.col1() + "";
                        String row1 = move.row1() + "";
                        String[] tempOperands =
                                new String[]{col0, row0, col1, row1};
                        doMove(tempOperands);
                    }
                }
                if ((_board.gameOver()) && (_state != FINISHED)) {
                    reportWinner();
                    _state = FINISHED;
                }
                while (_state.equals(FINISHED)) {
                    doCommand();
                }
            }
        }
    }

    /** Returns the player depending on the turn. */
    void switchPlayer() {
        if (currentPlayer == redPlayer) {
            currentPlayer =  bluePlayer;
        } else {
            currentPlayer =  redPlayer;
        }
    }

    /** Return a view of my game board that should not be modified by
     *  the caller. */
    Board board() {
        return _board;
    }

    /** Perform the next command from our input source. */
    void doCommand() {
        try {
            Command cmnd =
                    Command.parseCommand(_inputs.getLine("ataxx: "));
            _commands.get(cmnd.commandType()).accept(cmnd.operands());
        } catch (GameException excp) {
            _reporter.errMsg(excp.getMessage());
        }
    }

    /** Read and execute commands until encountering a move or until
     *  the game leaves playing state due to one of the commands. Return
     *  the terminating move command, or null if the game first drops out
     *  of playing mode. If appropriate to the current input source, use
     *  PROMPT to prompt for input. */
    Command getMoveCmnd(String prompt) {
        while (_state.equals(PLAYING)) {
            try {
                Command cmnd = Command.parseCommand(_inputs.getLine(prompt));
                if (cmnd.commandType().equals(PIECEMOVE)
                        || cmnd.commandType().equals(PASS)) {
                    return cmnd;
                } else {
                    Consumer func = _commands.get(cmnd.commandType());
                    func.accept(cmnd.operands());
                }
            } catch (GameException excp) {
                _reporter.errMsg(excp.getMessage());
            }
        }
        return null;
    }

    /** Return random integer between 0 (inclusive) and MAX>0 (exclusive). */
    int nextRandom(int max) {
        return _randoms.nextInt(max);
    }

    /** Report a move, using a message formed from FORMAT and ARGS as
     *  for String.format. */
    void reportMove(String format, Object... args) {
        _reporter.moveMsg(format, args);
    }

    /** Report an error, using a message formed from FORMAT and ARGS as
     *  for String.format. */
    void reportError(String format, Object... args) {
        _reporter.errMsg(format, args);
    }

    /* Command Processors */
    /** Perform the command 'auto OPERANDS[0]'. */
    void doAuto(String[] operands) {
        checkState("auto", SETUP);
        if ("red".equals(operands[0])) {
            if (currentPlayer.equals(redPlayer)) {
                redPlayer = new AI(this, PieceColor.RED);
                currentPlayer = redPlayer;
            } else {
                redPlayer = new AI(this, PieceColor.RED);
            }
        } else {
            if (currentPlayer.equals(bluePlayer)) {
                bluePlayer = new AI(this, PieceColor.BLUE);
                currentPlayer = bluePlayer;
            } else {
                bluePlayer = new AI(this, PieceColor.BLUE);
            }
        }
    }

    /** Perform a 'help' command. */
    void doHelp(String[] unused) {
        InputStream helpIn =
            Game.class.getClassLoader().getResourceAsStream("ataxx/help.txt");
        if (helpIn == null) {
            _reporter.errMsg("No help available.");
        } else {
            try {
                BufferedReader r
                    = new BufferedReader(new InputStreamReader(helpIn));
                while (true) {
                    String line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    _reporter.outcomeMsg(line);
                }
                r.close();
            } catch (IOException e) {
                return;
            }
        }
    }

    /** Perform the command 'load OPERANDS[0]'. */
    void doLoad(String[] operands) {
        try {
            FileReader read = new FileReader(operands[0]);
            ReaderSource r = new ReaderSource(read, false);
            _inputs.addSource(r);
        } catch (IOException e) {
            _reporter.errMsg("Cannot open file %s", operands[0]);
        }
    }

    /** Perform the command 'manual OPERANDS[0]'. */
    void doManual(String[] operands) {
        checkState("manual", SETUP);
        if ("blue".equals(operands[0])) {
            if (currentPlayer.equals(bluePlayer)) {
                bluePlayer = new Manual(this, PieceColor.BLUE);
                currentPlayer = bluePlayer;
            } else {
                bluePlayer = new Manual(this, PieceColor.BLUE);
            }
        } else {
            if (currentPlayer.equals(redPlayer)) {
                redPlayer = new Manual(this, PieceColor.RED);
                currentPlayer = redPlayer;
            } else {
                redPlayer = new Manual(this, PieceColor.RED);
            }
        }
    }

    /** Exit the program. */
    void doQuit(String[] unused) {
        System.exit(0);
    }

    /** Perform the command 'start'. */
    void doStart(String[] unused) {
        checkState("start", SETUP);
        _state = PLAYING;
        if (board().gameOver()) {
            reportWinner();
            _state = FINISHED;
        }
        canBlock = true;
    }

    /** Perform the move OPERANDS[0]. */
    void doMove(String[] operands) {
        checkState("move", PLAYING, SETUP);

        Move m = Move.move(operands[0].charAt(0),
                operands[1].charAt(0),
                operands[2].charAt(0),
                operands[3].charAt(0));
        if (m == null) {
            _reporter.errMsg("illegal piece movement, out of range");
            return;
        }
        if (board().legalMove(m)) {
            this.board().makeMove(m);
        } else {
            _reporter.errMsg(board().getInfoMessage());
            return;
        }
        if ((currentPlayer instanceof AI) && (_state == PLAYING)) {
            _reporter.moveMsg(currentPlayer + " moves " + m + ".");
        }
        switchPlayer();
        canBlock = false;
    }

    /** Cause current player to pass. */
    void doPass(String[] unused) {
        checkState("pass", PLAYING, SETUP);
        if (!board().canMove(board().whoseMove())) {
            _board.pass();
            if (currentPlayer instanceof AI) {
                _reporter.moveMsg(currentPlayer + " passes.");
            }
            switchPlayer();
        } else {
            _reporter.errMsg("cannot pass at this time.");
        }
    }

    /** Perform the command 'clear'. */
    void doClear(String[] unused) {
        redPlayer = new Manual(this, PieceColor.RED);
        bluePlayer = new AI(this, PieceColor.BLUE);
        currentPlayer = redPlayer;
        _board.clear();
        _state = SETUP;
        canBlock = true;
    }

    /** Perform the command 'dump'. */
    void doDump(String[] unused) {
        _reporter.outcomeMsg(_board.toString(false));
    }

    /** Execute 'seed OPERANDS[0]' command, where the operand is a string
     *  of decimal digits. Silently substitutes another value if
     *  too large. */
    void doSeed(String[] operands) {
        checkState("seed", SETUP);
        long seed = Long.parseLong(operands[0]);
        _randoms = new Random(seed);
    }

    /** Execute the command 'block OPERANDS[0]'. */
    void doBlock(String[] operands) {
        checkState("block", SETUP);
        if (canBlock) {
            if (!(_board.setBlock(operands[0]))) {
                _reporter.errMsg("block placement is not allowed.");
            }
        } else {
            _reporter.errMsg("cannot block at this time.");
        }
    }

    /** Execute the artificial 'error' command. */
    void doError(String[] unused) {
        _reporter.errMsg("command not understood.");
    }

    /** Report the outcome of the current game. */
    void reportWinner() {
        String msg;
        if (board().bluePieces() < board().redPieces()) {
            msg = "Red wins.";
        } else if (board().redPieces() < board().bluePieces()) {
            msg = "Blue wins.";
        } else {
            msg = "Draw.";
        }
        _reporter.outcomeMsg(msg);
    }

    /** Check that game is currently in one of the states STATES, assuming
     *  CMND is the command to be executed. */
    private void checkState(Command cmnd, State... states) {
        for (State s : states) {
            if (s.equals(_state)) {
                return;
            }
        }
        _reporter.errMsg("'%s' command is not allowed now.",
                cmnd.commandType());
    }

    /** Check that game is currently in one of the states STATES, using
     *  CMND in error messages as the name of the command to be executed. */
    private void checkState(String cmnd, State... states) {
        for (State s : states) {
            if (s.equals(_state)) {
                return;
            }
        }
        _reporter.errMsg("'%s' command is not allowed now.", cmnd);
    }

    /** Mapping of command types to methods that process them. */
    private final HashMap<Command.Type, Consumer<String[]>> _commands =
        new HashMap<>();
    {
        _commands.put(AUTO, this::doAuto);
        _commands.put(BLOCK, this::doBlock);
        _commands.put(CLEAR, this::doClear);
        _commands.put(DUMP, this::doDump);
        _commands.put(HELP, this::doHelp);
        _commands.put(MANUAL, this::doManual);
        _commands.put(PASS, this::doPass);
        _commands.put(PIECEMOVE, this::doMove);
        _commands.put(SEED, this::doSeed);
        _commands.put(START, this::doStart);
        _commands.put(LOAD, this::doLoad);
        _commands.put(QUIT, this::doQuit);
        _commands.put(ERROR, this::doError);
        _commands.put(EOF, this::doQuit);
    }

    /** Input source. */
    private final CommandSources _inputs = new CommandSources();

    /** My board. */
    private Board _board;
    /** Current game state. */
    private State _state;
    /** Used to send messages to the user. */
    private Reporter _reporter;
    /** Source of pseudo-random numbers (used by AIs). */
    private Random _randoms = new Random();

    /** Boolean telling is we can set a block. */
    private boolean canBlock = true;

    /** Player object representing the red pieces. */
    private Player redPlayer;

    /** Player object representing the blue pieces. */
    private Player bluePlayer;

    /** Boolean telling user which player's turn. */
    private Player currentPlayer = redPlayer;
}
