package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.Position;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

/** A small event-driven Swing view of the current game. */
public class SwingView implements BoardView {

    private static final int BOARD_PIXELS = 640;
    private static final java.awt.Color LIGHT_SQUARE = new java.awt.Color(240, 217, 181);
    private static final java.awt.Color DARK_SQUARE = new java.awt.Color(181, 136, 99);
    private static final java.awt.Color DESTINATION_DOT = new java.awt.Color(35, 125, 75);

    private JFrame frame;
    private BoardPanel boardPanel;
    private JLabel statusLabel;
    private JLabel messageLabel;
    private Game game;
    private GameController controller;
    private Position selected;
    private String lastMessage = "";

    @Override
    public void render(Game game) {
        this.game = game;
        if (boardPanel == null) {
            return;
        }
        Runnable repaint = () -> {
            statusLabel.setText(statusText(game));
            boardPanel.repaint();
        };
        if (SwingUtilities.isEventDispatchThread()) {
            repaint.run();
        } else {
            SwingUtilities.invokeLater(repaint);
        }
    }

    @Override
    public void showMessage(String message) {
        lastMessage = message == null ? "" : message;
        if (messageLabel == null) {
            return;
        }
        Runnable update = () -> messageLabel.setText(lastMessage);
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        } else {
            SwingUtilities.invokeLater(update);
        }
    }

    /** Creates the window and returns immediately; clicks drive the controller. */
    @Override
    public void start(GameController controller) {
        this.controller = controller;
        SwingUtilities.invokeLater(this::createWindow);
    }

    @Override
    public void close() {
        Runnable dispose = () -> {
            if (frame != null) {
                frame.dispose();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            dispose.run();
        } else {
            SwingUtilities.invokeLater(dispose);
        }
    }

    @Override
    public edu.sfsu.csc413.chess.model.PieceType promotionChoice(Game game) {
        return edu.sfsu.csc413.chess.model.PieceType.QUEEN;
    }

    private void createWindow() {
        frame = new JFrame("CSC 413 Chess");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent event) {
                controller.handle(new PlayerAction.Quit());
            }
        });

        boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(BOARD_PIXELS, BOARD_PIXELS));
        boardPanel.setBorder(BorderFactory.createLineBorder(java.awt.Color.DARK_GRAY, 2));

        statusLabel = new JLabel(statusText(game), SwingConstants.CENTER);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 15f));
        messageLabel = new JLabel(lastMessage, SwingConstants.CENTER);
        messageLabel.setPreferredSize(new Dimension(260, 42));

        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(event -> {
            selected = null;
            controller.handle(new PlayerAction.Undo());
        });

        JPanel controls = new JPanel(new GridLayout(4, 1, 8, 8));
        controls.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        controls.add(statusLabel);
        controls.add(messageLabel);
        controls.add(undoButton);

        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(controls, BorderLayout.EAST);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        boardPanel.requestFocusInWindow();
    }

    private String statusText(Game currentGame) {
        if (currentGame == null) {
            return "Starting...";
        }
        GameStatus status = currentGame.status();
        String detail = switch (status) {
            case CHECK -> " (in check)";
            case CHECKMATE -> " (checkmate)";
            case STALEMATE -> " (stalemate)";
            case IN_PROGRESS -> "";
        };
        return "Turn: " + currentGame.sideToMove() + "  |  Status: " + status + detail;
    }

    private void clickSquare(int file, int rank) {
        Position clicked = new Position(file, rank);
        if (selected == null) {
            Piece piece = game.board().pieceAt(clicked);
            if (piece != null && piece.color() == game.sideToMove()) {
                selected = clicked;
                boardPanel.repaint();
            } else if (piece != null) {
                showMessage("It is " + game.sideToMove() + "'s turn.");
            }
            return;
        }

        if (selected.equals(clicked)) {
            selected = null;
            boardPanel.repaint();
            return;
        }

        Piece replacement = game.board().pieceAt(clicked);
        if (replacement != null && replacement.color() == game.sideToMove()) {
            selected = clicked;
            boardPanel.repaint();
            return;
        }

        String notation = selected + clicked.toString();
        boolean legalDestination = game.legalMoves().stream()
                .anyMatch(move -> move.from().equals(selected) && move.to().equals(clicked));
        selected = null;
        if (!legalDestination) {
            controller.handle(new PlayerAction.Move(notation));
            return;
        }
        controller.handle(new PlayerAction.Move(notation));
    }

    private Set<Position> legalDestinations() {
        Set<Position> destinations = new HashSet<>();
        if (selected != null && game != null) {
            for (Move move : game.legalMoves()) {
                if (move.from().equals(selected)) {
                    destinations.add(move.to());
                }
            }
        }
        return destinations;
    }

    private final class BoardPanel extends JPanel {
        BoardPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    if (game == null || game.isGameOver()) {
                        return;
                    }
                    int square = Math.min(getWidth(), getHeight()) / 8;
                    int file = event.getX() / square;
                    int rank = 7 - event.getY() / square;
                    if (Position.isOnBoard(file, rank)) {
                        clickSquare(file, rank);
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
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int square = Math.min(getWidth(), getHeight()) / 8;
            Set<Position> destinations = legalDestinations();
            for (int file = 0; file < 8; file++) {
                for (int rank = 0; rank < 8; rank++) {
                    int x = file * square;
                    int y = (7 - rank) * square;
                    Position position = new Position(file, rank);
                    g.setColor((file + rank) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE);
                    g.fillRect(x, y, square, square);
                    if (position.equals(selected)) {
                        g.setColor(DESTINATION_DOT);
                        g.setStroke(new BasicStroke(4));
                        g.drawRect(x + 4, y + 4, square - 8, square - 8);
                    } else if (destinations.contains(position)) {
                        g.setColor(DESTINATION_DOT);
                        int diameter = Math.max(12, square / 5);
                        g.fillOval(x + (square - diameter) / 2, y + (square - diameter) / 2,
                                diameter, diameter);
                    }
                    drawPiece(g, game.board().pieceAt(position), x, y, square);
                }
            }
            g.dispose();
        }

        private void drawPiece(Graphics2D g, Piece piece, int x, int y, int square) {
            if (piece == null) {
                return;
            }
            String glyph = String.valueOf(piece.type().symbol());
            Font font = new Font("Serif", Font.BOLD, (int) (square * 0.62));
            g.setFont(font);
            int textWidth = g.getFontMetrics().stringWidth(glyph);
            int textX = x + (square - textWidth) / 2;
            int textY = y + (square + g.getFontMetrics().getAscent()) / 2 - 5;
            java.awt.Color fill = piece.color() == Color.WHITE
                    ? java.awt.Color.WHITE : java.awt.Color.BLACK;
            java.awt.Color outline = piece.color() == Color.WHITE
                    ? java.awt.Color.BLACK : java.awt.Color.WHITE;
            g.setColor(outline);
            for (int dx = -2; dx <= 2; dx += 2) {
                for (int dy = -2; dy <= 2; dy += 2) {
                    g.drawString(glyph, textX + dx, textY + dy);
                }
            }
            g.setColor(fill);
            g.drawString(glyph, textX, textY);
        }
    }
}
