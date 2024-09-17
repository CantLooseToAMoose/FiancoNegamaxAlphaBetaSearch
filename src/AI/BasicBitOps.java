package AI;

public class BasicBitOps {



    public static long[] bitShiftL(long[] board, int shiftBy) {
        return new long[]{board[0] << shiftBy | board[1] >>> 64 - shiftBy, board[1] << shiftBy};
    }

    public static long[] bitShiftR(long[] board, int shiftBy) {
        return new long[]{board[0] >>> shiftBy, board[1] >>> shiftBy | board[0] << 64 - shiftBy};
    }

    public static long[] or(long[] board1, long[] board2) {
        return new long[]{board1[0] | board2[0], board1[1] | board2[1]};
    }

    public static long[] and(long[] board1, long[] board2) {
        return new long[]{board1[0] & board2[0], board1[1] & board2[1]};
    }

    public static long[] inv(long[] board) {
        return new long[]{~board[0], ~board[1]};
    }


}