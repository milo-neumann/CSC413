package edu.sfsu.csc413.chess.model;

public record Position(int file, int rank) {

    @Override
    public String toString() {
        return "" + (char) ('a' + file) + (char) ('1' + rank);
    }

    /** Files and ranks both run 0..7. */
    public static final int BOARD_SIZE = 8;

    /** True when these raw coordinates name a real square. */
    public static boolean isOnBoard(int file, int rank) {
        return file >= 0 && file < BOARD_SIZE && rank >= 0 && rank < BOARD_SIZE;
    }

    /** Don't allow an instance of position not on board to be made */
    public Position {
        if (!isOnBoard(file, rank)) {
            throw new IllegalArgumentException(
                    "Position off board: file=" + file + ", rank=" + rank);
        }
    }

    public static Position parse(String algebraic) {
        if (algebraic.equals(null)) {
            throw new IllegalArgumentException("Position is: " + algebraic);
        }
        if (algebraic.length() != 2) {
            throw new IllegalArgumentException("Position " + algebraic + " is an invalid length");
        }
        int parsedFile = Character.getNumericValue(algebraic.charAt(0));
        int parsedRank = Character.getNumericValue(algebraic.charAt(1));

        // values adjusted for board coordinate offset
        return new Position(parsedFile - 10, parsedRank - 1);
    }

    public Position offsetOrNull(int fileDelta, int rankDelta) {
        int newFile = file + fileDelta;
        int newRank = rank + rankDelta;

        if (!isOnBoard(newFile, newRank)) {
            return null;
        }
        else {
            return new Position(newFile, newRank);
        }
    }

}