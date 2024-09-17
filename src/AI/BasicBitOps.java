package AI;

public class BasicBitOps {


    public static final long[] rightBorderMask = BitMaskCreationHelper.getRightBorderMask();
    public static final long[] leftBorderMask = BitMaskCreationHelper.getLeftBorderMask();
    public static final long[] leftDoubleBorderMask = BitMaskCreationHelper.getLeftDoubleBorderMask();
    public static final long[] rightDoubleBorderMask = BitMaskCreationHelper.getRightDoubleBorderMask();

    public static final long[] northBorderMask = BitMaskCreationHelper.getNorthBorderMask();
    public static final long[] southBorderMask = BitMaskCreationHelper.getSouthBorderMask();

    public static final long[] northDoubleBorderMask = BitMaskCreationHelper.getNorthDoubleBorderMask();

    public static final long[] southDoubleBorderMask = BitMaskCreationHelper.getSouthDoubleBorderMask();

    public static final long[] bitMapMask = BitMaskCreationHelper.getBitMapMask();


    public static long[] bitShiftL(long[] board, int shiftBy) {
        long[] shifted = new long[]{board[0] << shiftBy | board[1] >>> 64 - shiftBy, board[1] << shiftBy};
        return and(shifted, bitMapMask);
    }

    public static long[] bitShiftR(long[] board, int shiftBy) {
        long[] shifted = new long[]{board[0] >>> shiftBy, board[1] >>> shiftBy | board[0] << 64 - shiftBy};
        return and(shifted, bitMapMask);
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

    public static long[] shiftNorth(long[] board, int steps) {
        long[] masked = and(board, northBorderMask);
        masked = bitShiftL(masked, 9);
        if (steps == 1) {
            return masked;
        } else {
            return shiftNorth(masked, steps - 1);
        }
    }

    public static long[] shiftSouth(long[] board, int steps) {
        long[] masked = and(board, southBorderMask);
        masked = bitShiftR(masked, 9);
        if (steps == 1) {
            return masked;
        } else {
            return shiftSouth(masked, steps - 1);
        }
    }

    public static long[] shiftRight(long[] board, int steps) {
        long[] masked = and(board, rightBorderMask);
        masked = bitShiftR(masked, 1);
        if (steps == 1) {
            return masked;
        } else {
            return shiftRight(masked, steps - 1);
        }
    }

    public static long[] shiftLeft(long[] board, int steps) {
        long[] masked = and(board, leftBorderMask);
        masked = bitShiftL(masked, 1);
        if (steps == 1) {
            return masked;
        } else {
            return shiftLeft(masked, steps - 1);
        }
    }

}