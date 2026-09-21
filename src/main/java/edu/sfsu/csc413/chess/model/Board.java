package edu.sfsu.csc413.chess.model;
import java.util.List;
import java.util.ArrayList;

public class Board {

    private final Piece[][] squares;

    public Board() {
        squares = new Piece[8][8];
    }

    public Piece pieceAt(Position position) {
        return squares[position.file()][position.rank()];   // null if empty
    }

    public boolean isEmpty(Position position) {
        return pieceAt(position) == null;
    }

    // places a piece
    public void place(Position position, Piece piece) {
        squares[position.file()][position.rank()] = piece;
    }

    public List<Position> positionsOf(Color color) {
        List<Position> positions = new ArrayList<>();

        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                Piece piece = squares[file][rank];

                if (piece != null && piece.color() == color) {
                    Position pos = new Position(file, rank);
                    positions.add(pos);
                }
            }
        }
        return positions;
    }

    public String toString() {
        StringBuilder fen = new StringBuilder();

        for (int rank = 7; rank >= 0; rank--) {     // ranks print backwards

            int emptyCt = 0;

            for (int file = 0; file < 8; file++) {
                Piece piece = squares[file][rank];

                if (piece == null) {
                    emptyCt ++;
                } 
                else {
                    if (emptyCt > 0) {
                        fen.append(emptyCt);
                        emptyCt = 0;
                    }

                    fen.append(piece.symbol());
                }
            }

            // rank ends with empty square?
            if (emptyCt > 0) {
                fen.append(emptyCt);
            }

            if (rank > 0) {
                fen.append('/');
            }
        }

        return fen.toString();
    }

} 