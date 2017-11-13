package ataxx;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Observable;
import java.util.Observer;

import ucb.gui2.LayoutSpec;
import ucb.gui2.TopLevel;

/** The GUI for the Ataxx game.
 *  @author tapan.jasthi
 */
class AtaxxGUI extends TopLevel implements Observer, Reporter {

    /** Minimum size of board in pixels. */
    private static final int MIN_SIZE = 300;

    /** Contains the drawing logic for the Ataxx model. */
    private AtaxxBoardWidget _widget;
    /** The model of the game. */
    private Board _model;
    /** Output sink for sending commands to a game. */
    private PrintWriter _out;

    /** String representing the index of the first click
     *  (initial position of a move). */
    private String sq1 = "";

    /** String representing the index of the second click
     *  (final position of a move). */
    private String sq2 = "";

    /** A new display observing MODEL, with TITLE as its window title.
     *  It uses OUTCOMMANDS to send commands to a game instance, using the
     *  same commands as the text format for Ataxx. */
    AtaxxGUI(String title, Board model, Writer outCommands) {
        super(title, true);

        addMenuButton("Game->Clear", this::clear);
        addMenuButton("Game->Dump", this::dump);
        addMenuButton("Game->Help", this::help);
        addMenuButton("Game->Load...", this::load);
        addMenuButton("Game->Pass", this::pass);
        addMenuButton("Game->Start", this::start);
        addMenuButton("Game->Quit", this::quit);

        addMenuButton("Options->auto...", this::auto);
        addMenuButton("Options->manual...", this::manual);
        addMenuButton("Options->Seed...", this::setSeed);
        addMenuButton("Options->block...", this::block);

        _model = model;
        _widget = new AtaxxBoardWidget(model);
        _out = new PrintWriter(outCommands, true);

        add(_widget,
                new LayoutSpec("height", "1", "width",
                        "REMAINDER", "ileft", 5, "itop",
                        5, "iright", 5, "ibottom", 5));
        setMinimumSize(MIN_SIZE, MIN_SIZE);

        _widget.addObserver(this);
        _model.addObserver(this);
    }

    /** Execute the "auto" button function. */
    private synchronized void auto(String unused) {
        String resp =
                getTextInput("Which piece would you like to set to auto?",
                        "auto", "question", "");
        if (resp == null) {
            return;
        }
        _out.printf("auto %s%n", resp);
    }

    /** Execute the "block" button function. */
    private synchronized void block(String unused) {
        String resp =
                getTextInput("Enter the square cr to be blocked?",
                        "block", "question", "");
        if (resp == null) {
            return;
        }
        System.out.println("I am in block");
        System.out.println(resp + " is the square to be blocked");

        _out.printf("block %s%n", resp);
    }

    /** Execute the "load" button function. */
    private synchronized void load(String unused) {
        String resp =
                getTextInput("Enter the file name: ", "load", "question", "");
        if (resp == null) {
            return;
        }
        _out.printf("load %s%n", resp);
    }

    /** Execute the "manual" button function. */
    private synchronized void manual(String unused) {
        String resp =
                getTextInput("Which piece would you like to set to manual?",
                        "manual", "question", "");
        if (resp == null) {
            return;
        }
        _out.printf("manual %s%n", resp);
    }

    /** Execute the "Dump" button function. */
    private synchronized void dump(String unused) {
        _out.printf("dump%n");
    }

    /** Execute the "Clear" button function. */
    private synchronized void clear(String unused) {
        _out.printf("clear%n");
    }

    /** Execute the "Quit" button function. */
    private synchronized void quit(String unused) {
        _out.printf("quit%n");
    }

    /** Execute the "start" button function. */
    private synchronized void start(String unused) {
        _out.printf("start%n");
    }

    /** Execute the "pass" button function. */
    private synchronized void pass(String unused) {
        _out.printf("pass%n");
    }

    /** Execute the "help" button function. */
    private synchronized void help(String unused) {
        _out.printf("help%n");
    }

    /** Execute Seed... command. */
    private synchronized void setSeed(String unused) {
        String resp =
                getTextInput("Random Seed", "Get Seed", "question", "");
        if (resp == null) {
            return;
        }
        try {
            long s = Long.parseLong(resp);
            _out.printf("seed %d%n", s);
        } catch (NumberFormatException excp) {
            return;
        }
    }

    /** Display a message intended to show the title of the game.
     * @param format is the structure of the string.
     * @param args is the operands of the output. */
    public void titleMsg(String format, Object... args) {
        String s = String.format(format, args);
        this.showMessage(s,  "Error", null);
    }

    @Override
    public void errMsg(String format, Object... args) {
        String s = String.format(format, args);
        this.showMessage(s,  "Error", null);
    }

    @Override
    public void outcomeMsg(String format, Object... args) {
        String s = String.format(format, args);
        this.showMessage(s,  "Outcome", null);
    }

    @Override
    public void moveMsg(String format, Object... args) {
        String s = String.format(format, args);
        this.showMessage(s,  "Move", null);
    }

    @Override
    public void update(Observable obs, Object arg) {
        if (obs == _model) {
            _model.notifyObservers();
        } else if (obs == _widget) {
            if (sq1.equals("")) {
                sq1 = (String) arg;
            } else {
                sq2 = (String) arg;
            }
            System.out.println("clicked square" + arg);
            if (!(sq1.equals("")) && !(sq2.equals(""))) {
                _out.printf(sq1 + "-" + sq2 + "%n");
                sq1 = sq2 = "";
            }
        }
    }

    /** Run Ataxx game.  Use display if ARGS[k] is '--display'. */
    public static void main(String[] args) {
        Game game = null;
        Board board = new Board();
        try {
            PipedWriter writer = new PipedWriter();
            AtaxxGUI display = new AtaxxGUI("Ataxx", board, writer);
            game = new Game(board,
                    new ReaderSource(new PipedReader(writer,
                            BUFFER_LEN),
                            false),
                    display);
            display.display(true);
        } catch (IOException excp) {
            System.err.printf("Could not connect to display.%n");
            System.exit(1);
        }
        game.process(false);
    }

    /** Size of the buffer for reading commands from a GUI (bytes). */
    private static final int BUFFER_LEN = 128;
}
