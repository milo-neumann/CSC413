package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.PieceType;
import edu.sfsu.csc413.chess.model.Position;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/** A small, event-driven Swing board view. */
public class SwingView implements BoardView {

    private static final String[] GLYPHS = {
            "♙♟", "♘♞", "♗♝", "♖♜", "♕♛", "♔♚"
    };

    private final List<String> messages = new ArrayList<>();
    private JFrame frame;
    private BoardPanel boardPanel;
    private JLabel turnLabel;
    private JLabel statusLabel;
    private JTextArea messageArea;
    private GameController controller;
    private Game game;
    private Position selected;
    private GameStatus lastStatus;

    @Override
    public void render(Game game) {
        this.game = game;
        runOnEdt(() -> {
            updateLabels();
            if (boardPanel != null) {
                boardPanel.repaint();
            }
            announceCheckIfNeeded();
        });
    }

    @Override
    public void showMessage(String message) {
        messages.add(message);
        runOnEdt(this::refreshMessages);
    }

    /** Creates the window and returns immediately; Swing owns later events. */
    @Override
    public void start(GameController controller) {
        this.controller = controller;
        this.game = controller.game();
        runOnEdt(this::createWindow);
    }

    @Override
    public PieceType promotionChoice(Game game) {
        return PieceType.QUEEN;
    }

    @Override
    public void close() {
        runOnEdt(() -> {
            if (frame != null) {
                frame.dispose();
            }
        });
    }

    private void createWindow() {
        if (frame != null) {
            frame.toFront();
            return;
        }

        frame = new JFrame("CSC 413 Chess");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        boardPanel = new BoardPanel();
        frame.add(boardPanel, BorderLayout.CENTER);

        JPanel sidePanel = new JPanel(new BorderLayout(8, 8));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 8));
        turnLabel = new JLabel();
        turnLabel.setFont(turnLabel.getFont().deriveFont(Font.BOLD, 16f));
        statusLabel = new JLabel();
        JPanel info = new JPanel(new GridBagLayout());
        GridBagConstraints infoConstraints = new GridBagConstraints();
        infoConstraints.gridx = 0;
        infoConstraints.gridy = 0;
        infoConstraints.anchor = GridBagConstraints.WEST;
        infoConstraints.insets = new Insets(0, 0, 4, 0);
        info.add(turnLabel, infoConstraints);
        infoConstraints.gridy++;
        info.add(statusLabel, infoConstraints);
        sidePanel.add(info, BorderLayout.NORTH);

        messageArea = new JTextArea(10, 22);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        sidePanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(event -> {
            selected = null;
            controller.handle(new PlayerAction.Undo());
        });
        sidePanel.add(undoButton, BorderLayout.SOUTH);
        frame.add(sidePanel, BorderLayout.EAST);

        updateLabels();
        refreshMessages();
        frame.pack();
        frame.setMinimumSize(new Dimension(850, 650));
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    private void handleSquareClick(int file, int rank) {
        if (game == null || game.isGameOver()) {
            return;
        }

        Position clicked = new Position(file, rank);
        Piece clickedPiece = game.board().pieceAt(clicked);

        if (selected == null) {
            if (clickedPiece != null && clickedPiece.color() == game.sideToMove()) {
                selected = clicked;
                boardPanel.repaint();
            }
            return;
        }

        if (clicked.equals(selected)) {
            selected = null;
            boardPanel.repaint();
            return;
        }

        if (clickedPiece != null && clickedPiece.color() == game.sideToMove()) {
            selected = clicked;
            boardPanel.repaint();
            return;
        }

        String notation = selected.toString() + clicked;
        selected = null;
        controller.handle(new PlayerAction.Move(notation));
    }

    private void updateLabels() {
        if (turnLabel == null || game == null) {
            return;
        }
        turnLabel.setText(game.sideToMove() + " to move");
        statusLabel.setText("Status: " + statusText(game.status()));
    }

    private void announceCheckIfNeeded() {
        if (game == null) {
            return;
        }
        GameStatus status = game.status();
        if (status == GameStatus.CHECK && status != lastStatus) {
            showMessage("Check: " + game.sideToMove() + " is in check.");
        }
        lastStatus = status;
    }

    private void refreshMessages() {
        if (messageArea == null) {
            return;
        }
        messageArea.setText(String.join("\n", messages));
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }

    private static String statusText(GameStatus status) {
        return switch (status) {
            case IN_PROGRESS -> "In progress";
            case CHECK -> "CHECK";
            case CHECKMATE -> "CHECKMATE";
            case STALEMATE -> "STALEMATE";
        };
    }

    private static void runOnEdt(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }

    private final class BoardPanel extends JPanel {
        private static final int BOARD_SIZE = 640;

        BoardPanel() {
            setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
            setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    int squareSize = Math.min(getWidth(), getHeight()) / 8;
                    int file = event.getX() / squareSize;
                    int rank = 7 - event.getY() / squareSize;
                    if (file >= 0 && file < 8 && rank >= 0 && rank < 8) {
                        handleSquareClick(file, rank);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (game == null) {
                return;
            }

            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int squareSize = Math.min(getWidth(), getHeight()) / 8;
            drawSquares(g, squareSize);
            drawHighlights(g, squareSize);
            drawPieces(g, squareSize);
            drawCoordinates(g, squareSize);
            g.dispose();
        }

        private void drawSquares(Graphics2D g, int squareSize) {
            for (int row = 0; row < 8; row++) {
                for (int file = 0; file < 8; file++) {
                    g.setColor((file + row) % 2 == 0
                            ? new Color(240, 217, 181)
                            : new Color(181, 136, 99));
                    g.fillRect(file * squareSize, row * squareSize,
                            squareSize, squareSize);
                }
            }
        }

        private void drawHighlights(Graphics2D g, int squareSize) {
            if (selected == null) {
                return;
            }
            // M7: by hand edit - changing highlight color from ugly green to purple
            g.setColor(new Color(150, 80, 200, 220));
            for (Move move : game.legalMoves()) {
                if (move.from().equals(selected)) {
                    int x = move.to().file() * squareSize + squareSize / 2;
                    int y = (7 - move.to().rank()) * squareSize + squareSize / 2;
                    int diameter = Math.max(12, squareSize / 5);
                    g.fillOval(x - diameter / 2, y - diameter / 2,
                            diameter, diameter);
                }
            }
            g.setColor(new Color(30, 120, 220, 220));
            g.drawRect(selected.file() * squareSize + 2,
                    (7 - selected.rank()) * squareSize + 2,
                    squareSize - 4, squareSize - 4);
        }

        private void drawPieces(Graphics2D g, int squareSize) {
            g.setFont(new Font("Serif", Font.BOLD, Math.max(34, squareSize - 10)));
            for (int file = 0; file < 8; file++) {
                for (int rank = 0; rank < 8; rank++) {
                    Piece piece = game.board().pieceAt(new Position(file, rank));
                    if (piece == null) {
                        continue;
                    }
                    String glyph = glyphFor(piece);
                    int centerX = file * squareSize + squareSize / 2;
                    int baseline = (7 - rank) * squareSize
                            + (squareSize - g.getFontMetrics().getHeight()) / 2
                            + g.getFontMetrics().getAscent();
                    Color fill = piece.color() == edu.sfsu.csc413.chess.model.Color.WHITE
                            ? Color.WHITE : new Color(25, 25, 25);
                    Color outline = piece.color() == edu.sfsu.csc413.chess.model.Color.WHITE
                            ? new Color(25, 25, 25) : Color.WHITE;
                    g.setColor(outline);
                    for (int dx = -2; dx <= 2; dx += 2) {
                        for (int dy = -2; dy <= 2; dy += 2) {
                            g.drawString(glyph, centerX - g.getFontMetrics().stringWidth(glyph) / 2
                                    + dx, baseline + dy);
                        }
                    }
                    g.setColor(fill);
                    g.drawString(glyph, centerX - g.getFontMetrics().stringWidth(glyph) / 2,
                            baseline);
                }
            }
        }

        private void drawCoordinates(Graphics2D g, int squareSize) {
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            for (int file = 0; file < 8; file++) {
                g.setColor((file % 2 == 0) ? new Color(181, 136, 99)
                        : new Color(240, 217, 181));
                g.drawString(String.valueOf((char) ('a' + file)),
                        file * squareSize + 4, getHeight() - 4);
            }
        }
    }

    private static String glyphFor(Piece piece) {
        int index = switch (piece.type()) {
            case PAWN -> 0;
            case KNIGHT -> 1;
            case BISHOP -> 2;
            case ROOK -> 3;
            case QUEEN -> 4;
            case KING -> 5;
        };
        return String.valueOf(GLYPHS[index].charAt(
                piece.color() == edu.sfsu.csc413.chess.model.Color.WHITE ? 0 : 1));
    }
}
