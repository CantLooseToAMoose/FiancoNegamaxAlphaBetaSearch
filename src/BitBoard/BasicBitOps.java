package BitBoard;

public class BasicBitOps {


    public static final long[] EAST_BORDER_MASK = BitMaskCreationHelper.getEastBorderMask();
    public static final long[] WEST_BORDER_MASK = BitMaskCreationHelper.getWestBorderMask();
    public static final long[] leftDoubleBorderMask = BitMaskCreationHelper.getWestDoubleBorderMask();
    public static final long[] rightDoubleBorderMask = BitMaskCreationHelper.getEastDoubleBorderMask();

    public static final long[] NORTH_BORDER_MASK = BitMaskCreationHelper.getNorthBorderMaskArray();
    public static final long[] SOUTH_BORDER_MASK = BitMaskCreationHelper.getSouthBorderMaskArray();

    public static final long[] northDoubleBorderMask = BitMaskCreationHelper.getNorthDoubleBorderMask();

    public static final long[] southDoubleBorderMask = BitMaskCreationHelper.getSouthDoubleBorderMask();


    public static final long[] CAPTURE_SOUTH_WEST_MASK = BitMaskCreationHelper.getCaptureSouthWestMaskArray();
    public static final long[] CAPTURE_SOUTH_EAST_MASK = BitMaskCreationHelper.getCaptureSouthEastMaskArray();
    public static final long[] CAPTURE_NORTH_EAST_MASK = BitMaskCreationHelper.getCaptureNorthEastMask();
    public static final long[] CAPTURE_NORTH_WEST_MASK = BitMaskCreationHelper.getCaptureNorthWestMask();

    public static final long[] BIT_MAP_MASK = BitMaskCreationHelper.getBitMapMask();


    public static long[] bitShiftL(long[] board, int shiftBy) {
        long[] shifted = new long[]{board[0] << shiftBy | board[1] >>> 64 - shiftBy, board[1] << shiftBy};
        return and(shifted, BIT_MAP_MASK);
    }

    public static long[] bitShiftR(long[] board, int shiftBy) {
        long[] shifted = new long[]{board[0] >>> shiftBy, board[1] >>> shiftBy | board[0] << 64 - shiftBy};
        return and(shifted, BIT_MAP_MASK);
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
        long[] masked = and(board, EAST_BORDER_MASK);
        masked = bitShiftR(masked, 1);
        if (steps == 1) {
            return masked;
        } else {
            return shiftEast(masked, steps - 1);
        }
    }

    public static long[] shiftWest(long[] board, int steps) {
        long[] masked = and(board, WEST_BORDER_MASK);
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