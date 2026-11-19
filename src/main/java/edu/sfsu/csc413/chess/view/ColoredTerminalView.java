package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.Position;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * A terminal view with ANSI-colored squares and Unicode chess symbols.
 *
 * <p>The board is always shown from the side-to-move's perspective. This view
 * only reads the model and turns input into {@link PlayerAction}s; the
 * controller remains responsible for applying moves and announcing results.
 */
public class ColoredTerminalView implements BoardView {

    private static final String RESET = "\u001B[0m";
    // M7: My 'hand' edit - changing colors to be more visible
    private static final String LIGHT_SQUARE = "\u001B[48;5;223m";
    private static final String DARK_SQUARE = "\u001B[48;5;94m";
    private static final String WHITE_PIECE = "\u001B[38;5;231m";
    private static final String BLACK_PIECE = "\u001B[38;5;16m";
    private static final String EMPTY_SQUARE = "  ";

    private final PrintStream out;
    private final Scanner in;

    /** A colored terminal view reading from standard input. */
    public ColoredTerminalView() {
        this(System.out, new Scanner(System.in));
    }

    /** Injectable streams, so terminal input and output can be tested. */
    public ColoredTerminalView(PrintStream out, Scanner in) {
        this.out = out;
        this.in = in;
    }

    @Override
    public void render(Game game) {
        Color perspective = game.sideToMove();
        boolean flipped = perspective == Color.BLACK;

        out.println();
        for (int displayRow = 0; displayRow < Position.BOARD_SIZE; displayRow++) {
            int rank = flipped
                    ? displayRow
                    : Position.BOARD_SIZE - 1 - displayRow;
            out.printf("%d ", rank + 1);

            for (int displayColumn = 0;
                 displayColumn < Position.BOARD_SIZE;
                 displayColumn++) {
                int file = flipped
                        ? Position.BOARD_SIZE - 1 - displayColumn
                        : displayColumn;
                Piece piece = game.board().pieceAt(new Position(file, rank));
                String background = (file + rank) % 2 == 0
                        ? LIGHT_SQUARE
                        : DARK_SQUARE;
                String foreground = piece == null
                        ? ""
                        : piece.color() == Color.WHITE ? WHITE_PIECE : BLACK_PIECE;
                String glyph = piece == null
                        ? EMPTY_SQUARE
                        // M7: by hand edit - fixing purple black pawns bug
                        : " " + PieceGlyphs.FIGURES.glyphFor(piece) + "\uFE0E";
                out.print(background + foreground + glyph + " " + RESET);
            }
            out.println();
        }

        out.print("  ");
        for (int displayColumn = 0;
             displayColumn < Position.BOARD_SIZE;
             displayColumn++) {
            int file = flipped
                    ? Position.BOARD_SIZE - 1 - displayColumn
                    : displayColumn;
            out.printf(" %c ", (char) ('a' + file));
        }
        out.println();
        out.println(perspective + " to move  |  " + game.status());
    }

    @Override
    public void showMessage(String message) {
        out.println(message);
    }

    @Override
    public void start(GameController controller) {
        controller.runLoop();
    }

    @Override
    public PlayerAction nextAction(Game game) {
        out.print("Your move (e.g. e2e4, 'undo', 'moves', 'quit'): ");
        out.flush();
        return PlayerAction.parse(in.hasNextLine() ? in.nextLine() : "quit");
    }
}
