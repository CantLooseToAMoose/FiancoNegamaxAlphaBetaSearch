package search.TT;

import BitBoard.MoveConversion;
import FiancoGameEngine.MoveCommand;

import java.util.Random;

public class Zobrist {
    private static final long[][] zobristPositionTable = Zobrist.getZobristPositionTable();
    private static final long[][] zobristShortTable = getZobristShortMoveTable();


    private static long[][] getZobristShortMoveTable() {
        long[][] zobristMoveTable = new long[Short.MAX_VALUE][2];
        Random rand = new Random();
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            zobristMoveTable[i][0] = rand.nextLong() & Long.MAX_VALUE;
            zobristMoveTable[i][1] = rand.nextLong() & Long.MAX_VALUE;
        }
        for (int i = 1; i <= 81; i++) {
            for (int j = 1; j <= 81; j++) {
                for (int k = 0; k < 2; k++) {
                    short move = MoveConversion.pack(i, j);
                    int temp = i - j;
                    temp = switch (temp) {
                        case -20 -> i + 10;
                        case -16 -> i + 8;
                        case 16 -> i - 8;
                        case 20 -> i - 10;
                        default -> 0;
                    };
                    zobristMoveTable[move][k] = zobristPositionTable[i][k] ^ zobristPositionTable[j][k];
                    if (temp != 0) {
                        zobristMoveTable[move][k] ^= zobristPositionTable[j][k == 0 ? 1 : 0];
                    }
                }
            }
        }
        return zobristMoveTable;
    }

    private static long[][] getZobristPositionTable() {
        long[][] zobristMoveTable = new long[Short.MAX_VALUE][2];
        Random rand = new Random();
        for (int i = 0; i < 81; i++) {
            zobristMoveTable[i][0] = rand.nextLong() & Long.MAX_VALUE;
            zobristMoveTable[i][1] = rand.nextLong() & Long.MAX_VALUE;
        }
        return zobristMoveTable;
    }

    public static long updateHash(long zobristHash, short move, boolean isPlayerOne) {
        return zobristHash ^ zobristShortTable[move][isPlayerOne ? 0 : 1];
    }

}