package AI;

public class AdvancedBitOps {
        public static long[] possibleWestMovePieces(long[] board1, long[] board2) {
        long[] board1_shifted = BasicBitOps.shiftEast(board1, 1);
        long[] board2_shifted = BasicBitOps.shiftEast(board2, 1);
        board1 = BasicBitOps.and(board1, BasicBitOps.WEST_BORDER_MASK);
        return BasicBitOps.and(board1, BitMaskCreationHelper.getFreeTilesBitMask(board1_shifted, board2_shifted));
    }

    public static long[] possibleEastMovePieces(long[] board1, long[] board2) {
        long[] board1_shifted = BasicBitOps.shiftWest(board1, 1);
        long[] board2_shifted = BasicBitOps.shiftWest(board2, 1);
        board1 = BasicBitOps.and(board1, BasicBitOps.EAST_BORDER_MASK);
        return BasicBitOps.and(board1, BitMaskCreationHelper.getFreeTilesBitMask(board1_shifted, board2_shifted));
    }

    public static long[] possibleNorthMovePieces(long[] board1, long[] board2) {
        long[] board1_shifted = BasicBitOps.shiftSouth(board1, 1);
        long[] board2_shifted = BasicBitOps.shiftSouth(board2, 1);
        board1 = BasicBitOps.and(board1, BasicBitOps.NORTH_BORDER_MASK);
        return BasicBitOps.and(board1, BitMaskCreationHelper.getFreeTilesBitMask(board1_shifted, board2_shifted));
    }

    public static long[] possibleSouthMovePieces(long[] board1, long[] board2) {
        long[] board1_shifted = BasicBitOps.shiftNorth(board1, 1);
        long[] board2_shifted = BasicBitOps.shiftNorth(board2, 1);
        board1 = BasicBitOps.and(board1, BasicBitOps.SOUTH_BORDER_MASK);
        return BasicBitOps.and(board1, BitMaskCreationHelper.getFreeTilesBitMask(board1_shifted, board2_shifted));
    }

    public static long[] possibleSouthEastMovePieces(long[] board1, long[] board2) {
        long[] captureShift = BasicBitOps.shiftNorthWest(board2, 1);
        long[] pseudoCapturePossible = BasicBitOps.and(board1, captureShift);
        long[] board1_shifted = BasicBitOps.shiftNorthWest(board1, 2);
        long[] board2_shifted = BasicBitOps.shiftNorthWest(board2, 2);
        pseudoCapturePossible = BasicBitOps.and(pseudoCapturePossible, BasicBitOps.CAPTURE_SOUTH_EAST_MASK);
        return BasicBitOps.and(pseudoCapturePossible, BitMaskCreationHelper.getFreeTilesBitMask(board1_shifted, board2_shifted));
    }

    public static long[] possibleSouthWestMovePieces(long[] board1, long[] board2) {
        long[] captureShift = BasicBitOps.shiftNorthEast(board2, 1);
        long[] pseudoCapturePossible = BasicBitOps.and(board1, captureShift);
        long[] board1_shifted = BasicBitOps.shiftNorthEast(board1, 2);
        long[] board2_shifted = BasicBitOps.shiftNorthEast(board2, 2);
        pseudoCapturePossible = BasicBitOps.and(pseudoCapturePossible, BasicBitOps.CAPTURE_SOUTH_WEST_MASK);
        return BasicBitOps.and(pseudoCapturePossible, BitMaskCreationHelper.getFreeTilesBitMask(board1_shifted, board2_shifted));
    }

    public static long[] possibleNorthEastMovePieces(long[] board1, long[] board2) {
        long[] captureShift = BasicBitOps.shiftSouthWest(board2, 1);
        long[] pseudoCapturePossible = BasicBitOps.and(board1, captureShift);
        long[] board1_shifted = BasicBitOps.shiftSouthWest(board1, 2);
        long[] board2_shifted = BasicBitOps.shiftSouthWest(board2, 2);
        pseudoCapturePossible = BasicBitOps.and(pseudoCapturePossible, BasicBitOps.CAPTURE_NORTH_EAST_MASK);
        return BasicBitOps.and(pseudoCapturePossible, BitMaskCreationHelper.getFreeTilesBitMask(board1_shifted, board2_shifted));
    }

    public static long[] possibleNorthWestMovePieces(long[] board1, long[] board2) {
        long[] captureShift = BasicBitOps.shiftSouthEast(board2, 1);
        long[] pseudoCapturePossible = BasicBitOps.and(board1, captureShift);
        long[] board1_shifted = BasicBitOps.shiftSouthEast(board1, 2);
        long[] board2_shifted = BasicBitOps.shiftSouthEast(board2, 2);
        pseudoCapturePossible = BasicBitOps.and(pseudoCapturePossible, BasicBitOps.CAPTURE_NORTH_WEST_MASK);
        return BasicBitOps.and(pseudoCapturePossible, BitMaskCreationHelper.getFreeTilesBitMask(board1_shifted, board2_shifted));
    }

}
