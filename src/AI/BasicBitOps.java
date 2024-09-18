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

    public static long[] XOR(long[] board1, long[] board2) {
        return new long[]{board1[0] ^ board2[0], board1[1] ^ board2[1]};
    }

    public static long[] shiftNorth(long[] board, int steps) {
        long[] shifted = bitShiftL(board, 9);
        if (steps == 1) {
            return shifted;
        } else {
            return shiftNorth(shifted, steps - 1);
        }
    }

    public static long[] shiftSouth(long[] board, int steps) {
        long[] shifted = bitShiftR(board, 9);
        if (steps == 1) {
            return shifted;
        } else {
            return shiftSouth(shifted, steps - 1);
        }
    }

    public static long[] shiftEast(long[] board, int steps) {
        long[] masked = and(board, rightBorderMask);
        masked = bitShiftR(masked, 1);
        if (steps == 1) {
            return masked;
        } else {
            return shiftEast(masked, steps - 1);
        }
    }

    public static long[] shiftWest(long[] board, int steps) {
        long[] masked = and(board, leftBorderMask);
        masked = bitShiftL(masked, 1);
        if (steps == 1) {
            return masked;
        } else {
            return shiftWest(masked, steps - 1);
        }
    }

    public static long[] shiftNorthWest(long[] board, int steps) {
        // Shift North first, then shift West
        long[] shifted = shiftNorth(board, 1);
        shifted = shiftWest(shifted, 1);
        if (steps == 1) {
            return shifted;
        } else {
            return shiftNorthWest(shifted, steps - 1);
        }
    }

    public static long[] shiftNorthEast(long[] board, int steps) {
        // Shift North first, then shift East
        long[] shifted = shiftNorth(board, 1);
        shifted = shiftEast(shifted, 1);
        if (steps == 1) {
            return shifted;
        } else {
            return shiftNorthEast(shifted, steps - 1);
        }
    }

    public static long[] shiftSouthWest(long[] board, int steps) {
        // Shift South first, then shift West
        long[] shifted = shiftSouth(board, 1);
        shifted = shiftWest(shifted, 1);
        if (steps == 1) {
            return shifted;
        } else {
            return shiftSouthWest(shifted, steps - 1);
        }
    }

    public static long[] shiftSouthEast(long[] board, int steps) {
        // Shift South first, then shift East
        long[] shifted = shiftSouth(board, 1);
        shifted = shiftEast(shifted, 1);
        if (steps == 1) {
            return shifted;
        } else {
            return shiftSouthEast(shifted, steps - 1);
        }
    }

}