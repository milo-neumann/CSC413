package edu.sfsu.csc413.chess.model;

public enum PieceType {
    PAWN('P'),
    KNIGHT('N'),
    BISHOP('B'),
    ROOK('R'),
    QUEEN('Q'),
    KING('K');

    private final char symbol;

    // constructor
    PieceType(char symbol) {
        this.symbol = symbol;
    }

    // getter
    public char symbol() {
        return symbol;
    }

    public static PieceType fromSymbol(char letter){

        // ensure letter is uppercase
        char upper = Character.toUpperCase(letter);

        // check if input letter is a piece type
        for (PieceType type : PieceType.values()) {
            if (type.symbol == upper){
                return type;
            }
        }
        throw new IllegalArgumentException("Not a valid piece symbol: " + letter);
    }
}
