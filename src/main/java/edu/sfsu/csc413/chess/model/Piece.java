package edu.sfsu.csc413.chess.model;

import java.util.ArrayList;
import java.util.List;


public abstract class Piece{     // abstract?

    private final Color color;
    private final PieceType type;

    // constructor
    protected Piece(Color color, PieceType type){      // protected?
        this.color = color;
        this.type = type;
    }

    // getters
    public Color color(){
        return color;
    }
    public PieceType type(){
        return type;
    }
    public char symbol(){
        char symbol = type.symbol();

        if (color == Color.BLACK) {
            return Character.toLowerCase(symbol);
        }
        return symbol;
    }

    public String toString() {
        return String.valueOf(symbol());
    }
    public abstract List<Move> pseudoLegalMoves(Board board, Position from);

    public boolean attacks(Board board, Position from, Position target) {
        // determine if any pseudoLegalMoves move onto the target square
        for (Move i : pseudoLegalMoves(board, from)) {
            if(i.to().equals(target)){
                return true;    // found a move that attacks target
            }
        }
        return false;   // did not find a move that attacks target
    }

    protected List<Move> slidingMoves(Board board, Position from, int[][] directions) {

        List<Move> moves = new ArrayList<>();

        // for each direction, look one move 'forward'
        for (int[] direction : directions) {
            Position to = from.offsetOrNull(direction[0], direction[1]);

            while (to != null) {
                Piece target = board.pieceAt(to);

                // if empty, add move and 'step' forward
                if (target == null) {   // note capture = null
                    moves.add(new Move(from, to, this, null, null));
                }
                else {
                    // if has enemy, add move (capture) and stop
                    if (target.color() != color) {
                        moves.add(new Move(from, to, this, target, null));
                    }

                    // else has friend, stop only
                    break;
                }

                to = to.offsetOrNull(direction[0], direction[1]);
            }
        }

        return moves;
    }

    protected List<Move> steppingMoves(Board board, Position from, int [][] offsets) {

        List<Move> moves = new ArrayList<>();

        for(int[] offset : offsets) {
            Position to = from.offsetOrNull(offset[0], offset[1]);

            // if off board, skip
            if (to == null) {
                continue;
            }

            Piece target = board.pieceAt(to);

            // empty square
            if (target == null || target.color() != color) {
                moves.add(new Move(from, to, this, target, null));
            }
        }

        return moves;
    }

}

