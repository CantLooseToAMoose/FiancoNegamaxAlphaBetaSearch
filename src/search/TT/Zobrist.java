package search.TT;

import BitBoard.MoveConversion;

import java.util.Random;

public class Zobrist {
    private long[][] zobristTable;
    private long zobristHash;

    // Board size of 81 (9x9 board)
    private static final int BOARD_SIZE = 81;
    private static final int WHITE = 0;
    private static final int BLACK = 1;

    public Zobrist() {
        // Initialize Zobrist Table
        zobristTable = new long[BOARD_SIZE][2];  // 2 for black and white pieces
        Random rand = new Random();

        // Fill the Zobrist table with random long numbers
        for (int i = 0; i < BOARD_SIZE; i++) {
            zobristTable[i][WHITE] = rand.nextLong()& Long.MAX_VALUE;;
            zobristTable[i][BLACK] = rand.nextLong()& Long.MAX_VALUE;;
        }
        // Initial Zobrist hash is 0
        zobristHash = 0;
    }

    // Update the hash when placing or removing a piece
    public void updateHash(int position, int piece) {
        // XOR the corresponding random number for the piece and position
        zobristHash ^= zobristTable[position-1][piece];
    }

    public void updateHash(short move, boolean isPlayerOne) {
        int num1 = MoveConversion.unpackFirstNumber(move);
        int num2 = MoveConversion.unpackSecondNumber(move);
        updateHash(num1, isPlayerOne ? WHITE : BLACK);
        updateHash(num2, isPlayerOne ? WHITE : BLACK);
        switch (num1 - num2) {
            case -20 -> updateHash(num1 + 10, !isPlayerOne ? WHITE : BLACK);
            case -16 -> updateHash(num1 + 8, !isPlayerOne ? WHITE : BLACK);
            case 16 -> updateHash(num1 - 8, !isPlayerOne ? WHITE : BLACK);
            case 20 -> updateHash(num1 - 10, !isPlayerOne ? WHITE : BLACK);
        }
    }

    // Getter for the current Zobrist hash
    public long getZobristHash() {
        return zobristHash;
    }
}