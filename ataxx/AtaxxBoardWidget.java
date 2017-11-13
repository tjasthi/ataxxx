package ataxx;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

import ucb.gui2.Pad;

/** Widget for displaying an Ataxx board.
 *  @author tapan.jasthi
 */
class AtaxxBoardWidget extends Pad implements Observer {

    /** Length of side of one square, in pixels. */
    static final int SQDIM = 50;
    /** Number of squares on a side. */
    static final int SIDE = Board.SIDE;
    /** Radius of circle representing a piece. */
    static final int PIECE_RADIUS = 15;

    /** Color of red pieces. */
    private static final Color RED_COLOR = Color.RED;
    /** Color of blue pieces. */
    private static final Color BLUE_COLOR = Color.BLUE;
    /** Color of blank squares. */
    private static final Color BLANK_COLOR = Color.WHITE;
    /** Stroke for lines. */
    private static final BasicStroke LINE_STROKE = new BasicStroke(1.0f);

    /** Dimension of current drawing surface in pixels. */
    private int _dim;

    /** Model being displayed. */
    private static Board _model;

    /** A new widget displaying MODEL. */
    AtaxxBoardWidget(Board model) {
        _model = model;
        setMouseHandler("click", this::readMove);
        _model.addObserver(this);
        _dim = SQDIM * SIDE;
        setPreferredSize(_dim, _dim + SQDIM);
    }

    @Override
    /** Whenever there are GUI changes, the paintComponent
     * is automatically called and it always fetches the latest
     * board (game) (model).
     */
    public synchronized void paintComponent(Graphics2D g) {
        g.setColor(BLANK_COLOR);
        g.fillRect(0, 0, _dim, _dim);

        for (int k = 0; k <= Board.SIDE; k += 1) {
            g.setColor(Color.RED);
            g.setStroke(LINE_STROKE);
            g.drawLine(0, k * SQDIM , _dim, k * SQDIM);
            g.drawLine(k * SQDIM, 0, k * SQDIM, _dim);
        }

        char col = 'a', row = '1';
        for (int square = 1; square <= SIDE * SIDE;  square += 1) {
            drawTheSquare(g, col, row);
            if (square % 7 != 0) {
                col += 1;
            } else {
                col = 'a';
                row += 1;
            }
        }
    }

    /** Method for drawing the square. Given a column and row,
     * returns the center of the column in pixels in the graphics component.
     *  @param col is the column of the pixel placement.
     *  @param row is the row of the pixel placement.
     *  @param g defines the graphic of the painting.
     *  */
    void drawTheSquare(Graphics2D g, char col, char row) {
        int cp = getColumnPixel(col, row);
        int rp = getRowPixel(col, row);

        switch (_model.get(col, row)) {
        case EMPTY: break;
        case BLOCKED: this.drawBlock(g, cp, rp);
            break;
        case RED: this.drawPiece(g, cp, rp, RED_COLOR);
            break;
        case BLUE: this.drawPiece(g, cp, rp, BLUE_COLOR);
            break;
        default: break;
        }
    }

    /** Given a column and row, return the center of the column in pixels
     *  in the graphics component.
     *  @param col is the column of the pixel placement.
     *  @param row is the row of the pixel placement.
     */
    int getColumnPixel(char col, char row) {
        int y = 0;
        int offset = SQDIM / 2;

        switch (col) {
        case 'a': y = (1 * SQDIM) - offset;
            break;
        case 'b': y = (2 * SQDIM) - offset;
            break;
        case 'c': y = (3 * SQDIM) - offset;
            break;
        case 'd': y = (4 * SQDIM) - offset;
            break;
        case 'e': y = (5 * SQDIM) - offset;
            break;
        case 'f': y = (6 * SQDIM) - offset;
            break;
        case 'g': y = (7 * SQDIM) - offset;
            break;
        default: y = 0;
            break;
        }
        return y;
    }

    /** Given a column and row, return the center of the row in pixels
     *  in the graphics component.
     *  @param col is the column of the pixel placement.
     *  @param row is the row of the pixel placement.
     */
    int getRowPixel(char col, char row) {
        int x = 0;
        int offset = SQDIM / 2;

        switch (row) {
        case '7': x = (1 * SQDIM) - offset;
            break;
        case '6': x = (2 * SQDIM) - offset;
            break;
        case '5': x = (3 * SQDIM) - offset;
            break;
        case '4': x = (4 * SQDIM) - offset;
            break;
        case '3': x = (5 * SQDIM) - offset;
            break;
        case '2': x = (6 * SQDIM) - offset;
            break;
        case '1': x = (7 * SQDIM) - offset;
            break;
        default: x = 0;
            break;
        }
        return x;
    }

    /** Draw a circle to represent centered at (CX, CY) on G.
     * @param color is the color of the piece being displayed.
     * @param cx is the column center of the pixel placement.
     * @param cy is the row center of the pixel placement.
     * @param g defines the graphic of the painting.
     */
    void drawPiece(Graphics2D g, int cx, int cy, Color color) {
        g.setColor(color);
        g.fillOval(cx, cy, PIECE_RADIUS, PIECE_RADIUS);
    }


    /** Draw a block centered at (CX, CY) on G. */
    void drawBlock(Graphics2D g, int cx, int cy) {
        g.setColor(Color.BLACK);
        g.fillRect(cx, cy, 10, 10);
    }

    /** Notify observers of mouse's current position from click event WHERE. */
    private void readMove(String unused, MouseEvent where) {
        int x = where.getX(), y = where.getY();
        char mouseCol, mouseRow;
        if (where.getButton() == MouseEvent.BUTTON1) {
            mouseCol = (char) (x / SQDIM + 'a');
            mouseRow = (char) ((SQDIM * SIDE - y) / SQDIM + '1');
            if (mouseCol >= 'a' && mouseCol <= 'g'
                    && mouseRow >= '1' && mouseRow <= '7') {
                setChanged();
                notifyObservers("" + mouseCol + mouseRow);
            }
        }
    }

    @Override
    /** Whenever the model changes, it fires the update method
     * in this Observer and the GUI gets updated.
     */
    public synchronized void update(Observable model, Object arg) {
        repaint();
    }
}
