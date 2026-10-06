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

    /*
        apply checks nothing. It trusts the Move it is given, because deciding whether a move is allowed is Game's job,
        not storage's. undo needs no bookkeeping because the Move carries the piece it captured; that is why Move has a
        captured field.

        One wrinkle: if the move is a promotion, the piece set down on to is a new piece of promotesTo()'s type
        and the mover's color, not the pawn. You need to build a piece from a PieceType. The one switch on PieceType
        in the project is PieceFactory.create, and calling it from Board makes model depend on factory, an arrow
        pointing the wrong way. A second, private switch in Board duplicates four lines. Either is accepted this
        milestone; say in a comment which you chose and what it costs. No M3 test promotes, so this is graded by
        reading.
    */

    public void apply(Move move) {
        place(move.from(), null);

        if (move.isPromotion()) {
            Piece promotedPiece;

            switch (move.promotesTo()) {
                case QUEEN:
                    promotedPiece = new Queen(move.moved().color());
                    break;
                case ROOK:
                    promotedPiece = new Rook(move.moved().color());
                    break;
                case BISHOP:
                    promotedPiece = new Bishop(move.moved().color());
                    break;
                case KNIGHT:
                    promotedPiece = new Knight(move.moved().color());
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Invalid promotion type: " + move.promotesTo()
                    );
            }

            place(move.to(), promotedPiece);
        } else {
            place(move.to(), move.moved());
        }
    }

    public void undo(Move move) {
        place(move.from(), move.moved());
        place(move.to(), move.captured());
    }

} 