package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.Position;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

/** A small event-driven Swing view for the chess game. */
public class SwingView implements BoardView {

    private static final java.awt.Color LIGHT_SQUARE = new java.awt.Color(240, 217, 181);
    private static final java.awt.Color DARK_SQUARE = new java.awt.Color(181, 136, 99);
    private static final java.awt.Color SELECTED_SQUARE = new java.awt.Color(246, 246, 105);
    private static final java.awt.Color LEGAL_SQUARE = new java.awt.Color(170, 210, 120);

    private final JButton[][] squares = new JButton[8][8];
    private final JLabel statusLabel = new JLabel();
    private final JTextArea messages = new JTextArea(4, 40);
    private final StringBuilder messageHistory = new StringBuilder();

    private JFrame frame;
    private Game game;
    private GameController controller;
    private Position selected;

    @Override
    public void render(Game game) {
        this.game = game;
        if (frame == null) {
            return;
        }
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            refreshBoard();
        } else {
            javax.swing.SwingUtilities.invokeLater(this::refreshBoard);
        }
    }

    @Override
    public void showMessage(String message) {
        messageHistory.append(message).append('\n');
        if (messages != null) {
            Runnable update = () -> {
                messages.setText(messageHistory.toString());
                messages.setCaretPosition(messages.getDocument().getLength());
            };
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                update.run();
            } else {
                javax.swing.SwingUtilities.invokeLater(update);
            }
        }
    }

    /** Creates the window and returns immediately; Swing handles later events. */
    @Override
    public void start(GameController controller) {
        this.controller = controller;
        Runnable create = () -> {
            frame = new JFrame("CSC 413 Chess");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel boardPanel = new JPanel(new GridLayout(8, 8));
            for (int screenRow = 0; screenRow < 8; screenRow++) {
                for (int file = 0; file < 8; file++) {
                    int rank = 7 - screenRow;
                    int squareFile = file;
                    int squareRank = rank;
                    JButton square = new JButton();
                    square.setFocusable(false);
                    square.setFont(new Font("Serif", Font.PLAIN, 40));
                    square.setOpaque(true);
                    square.setBorder(BorderFactory.createLineBorder(java.awt.Color.DARK_GRAY));
                    square.addActionListener(event -> squareClicked(
                            new Position(squareFile, squareRank)));
                    squares[file][rank] = square;
                    boardPanel.add(square);
                }
            }

            JButton undo = new JButton("Undo");
            undo.addActionListener(event -> controller.handle(new PlayerAction.Undo()));

            statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            messages.setEditable(false);
            messages.setLineWrap(true);
            messages.setWrapStyleWord(true);
            messages.setText(messageHistory.toString());

            JPanel bottom = new JPanel(new BorderLayout());
            bottom.add(statusLabel, BorderLayout.CENTER);
            bottom.add(undo, BorderLayout.EAST);

            frame.add(boardPanel, BorderLayout.CENTER);
            frame.add(bottom, BorderLayout.NORTH);
            frame.add(new JScrollPane(messages), BorderLayout.SOUTH);
            frame.setMinimumSize(new Dimension(560, 650));
            frame.pack();
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
            refreshBoard();
        };

        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            create.run();
        } else {
            javax.swing.SwingUtilities.invokeLater(create);
        }
    }

    @Override
    public void close() {
        if (frame != null) {
            javax.swing.SwingUtilities.invokeLater(() -> frame.dispose());
        }
    }

    private void squareClicked(Position position) {
        if (game == null || game.isGameOver()) {
            return;
        }

        if (selected == null) {
            Piece piece = game.board().pieceAt(position);
            if (piece == null || piece.color() != game.sideToMove()) {
                showMessage("Select a piece belonging to " + game.sideToMove() + ".");
                return;
            }
            List<Move> legalMoves = game.legalMoves();
            if (legalMoves.stream().noneMatch(move -> move.from().equals(position))) {
                showMessage("That piece has no legal moves.");
                return;
            }
            selected = position;
            refreshBoard();
            return;
        }

        if (selected.equals(position)) {
            selected = null;
            refreshBoard();
            return;
        }

        controller.handle(new PlayerAction.Move(selected.toString() + position));
        selected = null;
    }

    private void refreshBoard() {
        if (game == null || frame == null) {
            return;
        }

        List<Move> legalMoves = selected == null ? List.of() : game.legalMoves();
        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                Position position = new Position(file, rank);
                JButton square = squares[file][rank];
                Piece piece = game.board().pieceAt(position);
                square.setText(piece == null ? "" : PieceGlyphs.FIGURES.glyphFor(piece));
                java.awt.Color background = ((file + rank) % 2 == 0)
                        ? LIGHT_SQUARE : DARK_SQUARE;
                if (position.equals(selected)) {
                    background = SELECTED_SQUARE;
                } else if (legalMoves.stream().anyMatch(move ->
                        move.from().equals(selected) && move.to().equals(position))) {
                    background = LEGAL_SQUARE;
                }
                square.setBackground(background);
                square.setForeground(piece != null && piece.color()
                        == edu.sfsu.csc413.chess.model.Color.BLACK
                        ? java.awt.Color.BLACK : java.awt.Color.WHITE);
            }
        }
        statusLabel.setText(game.sideToMove() + " to move  |  " + game.status());
    }

}
