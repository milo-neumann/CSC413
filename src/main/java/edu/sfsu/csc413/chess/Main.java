package edu.sfsu.csc413.chess;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.view.BoardView;
import edu.sfsu.csc413.chess.view.ColoredTerminalView;
import edu.sfsu.csc413.chess.view.ConsoleView;
import edu.sfsu.csc413.chess.view.EmojiView;
import edu.sfsu.csc413.chess.view.GameController;
import edu.sfsu.csc413.chess.view.SwingView;

public final class Main {

    public static void main(String[] args) {

        Game game = new Game();

        BoardView view;

        if (args.length > 0 && args[0].equalsIgnoreCase("swing")) {
            view = new SwingView();
        } else if (args.length > 0 && args[0].equalsIgnoreCase("colored")) {
            view = new ColoredTerminalView();
        } else if (args.length > 0 && args[0].equalsIgnoreCase("emoji")) {
            view = new EmojiView();
        } else {
            view = new ConsoleView();
        }

        GameController controller = new GameController(game, view);
        controller.start();
    }

    private Main() {
        // hm?
    }
}
