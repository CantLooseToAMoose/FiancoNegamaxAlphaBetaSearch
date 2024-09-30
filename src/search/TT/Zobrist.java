package search.TT;

import BitBoard.MoveConversion;

import java.util.Random;

public class Zobrist {
    private static final long[][] zobristTable = getZobristTable();
    private long zobristHash;

    // Board size of 81 (9x9 board)
    private static final int BOARD_SIZE = 81;
    private static final int WHITE = 0;
    private static final int BLACK = 1;


    public static long[][] getZobristTable() {
        long[][] zobristTable = new long[BOARD_SIZE][2];  // 2 for black and white pieces
        Random rand = new Random();

        // Fill the Zobrist table with random long numbers
        for (int i = 0; i < BOARD_SIZE; i++) {
            zobristTable[i][WHITE] = rand.nextLong() & Long.MAX_VALUE;
            ;
            zobristTable[i][BLACK] = rand.nextLong() & Long.MAX_VALUE;
            ;
        }
        return zobristTable;
    }

    // Update the hash when placing or removing a piece
    public static long updateHash(long zobristHash, int position, int piece) {
        // XOR the corresponding random number for the piece and position
        zobristHash ^= zobristTable[position - 1][piece];
        return zobristHash;
    }

    public static long updateHash(long zobristHash, short move, boolean isPlayerOne) {
        int num1 = MoveConversion.unpackFirstNumber(move);
        int num2 = MoveConversion.unpackSecondNumber(move);
        zobristHash = updateHash(zobristHash, num1, isPlayerOne ? WHITE : BLACK);
        zobristHash = updateHash(zobristHash, num2, isPlayerOne ? WHITE : BLACK);
        switch (num1 - num2) {
            case -20 -> zobristHash = updateHash(zobristHash, num1 + 10, !isPlayerOne ? WHITE : BLACK);
            case -16 -> zobristHash = updateHash(zobristHash, num1 + 8, !isPlayerOne ? WHITE : BLACK);
            case 16 -> zobristHash = updateHash(zobristHash, num1 - 8, !isPlayerOne ? WHITE : BLACK);
            case 20 -> zobristHash = updateHash(zobristHash, num1 - 10, !isPlayerOne ? WHITE : BLACK);
        }
        return zobristHash;
    }
}