package search.TT;

import java.util.Random;

public class Zobrist {
    private static final long[][] zobristShortTable = getZobristShortMoveTable();

    private static long[][] getZobristShortMoveTable() {
        long[][] zobristMoveTable = new long[Short.MAX_VALUE][2];
        Random rand = new Random();
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            zobristMoveTable[i][0] = rand.nextLong() & Long.MAX_VALUE;
            zobristMoveTable[i][1] = rand.nextLong() & Long.MAX_VALUE;
        }
        return zobristMoveTable;
    }

    public static long updateHash(long zobristHash, short move, boolean isPlayerOne) {
        return zobristHash ^ zobristShortTable[move][isPlayerOne ? 0 : 1];
    }

}