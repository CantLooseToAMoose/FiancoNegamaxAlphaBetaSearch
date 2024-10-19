package AI.BitBoard;

import GameEngine.Fianco;

public class BasicBitOps {

    public static final long[] BIT_MAP_MASK = BitMaskCreationHelper.createBitMapMaskLongArray();

    public static final long[] EAST_BORDER_MASK = BitMaskCreationHelper.getEastBorderMask();
    public static final long[] WEST_BORDER_MASK = BitMaskCreationHelper.getWestBorderMask();
    public static final long[] leftDoubleBorderMask = BitMaskCreationHelper.getWestDoubleBorderMask();
    public static final long[] rightDoubleBorderMask = BitMaskCreationHelper.getEastDoubleBorderMask();

    public static final long[] NORTH_BORDER_MASK = BitMaskCreationHelper.getNorthBorderMaskArray();
    public static final long[] SOUTH_BORDER_MASK = BitMaskCreationHelper.getSouthBorderMaskArray();

    public static final long[] northDoubleBorderMask = BitMaskCreationHelper.getNorthDoubleBorderMask();

    public static final long[] southDoubleBorderMask = BitMaskCreationHelper.getSouthDoubleBorderMask();

    public static final long[] ONLY_ROW_ONE_MASK = BasicBitOps.inv(NORTH_BORDER_MASK);
    public static final long[] ONLY_ROW_TWO_MASK = shiftSouth(BasicBitOps.inv(NORTH_BORDER_MASK), 1);
    public static final long[] ONLY_ROW_THREE_MASK = shiftSouth(BasicBitOps.inv(NORTH_BORDER_MASK), 2);
    public static final long[] ONLY_ROW_FOUR_MASK = shiftSouth(BasicBitOps.inv(NORTH_BORDER_MASK), 3);
    public static final long[] ONLY_ROW_FIVE_MASK = shiftSouth(BasicBitOps.inv(NORTH_BORDER_MASK), 4);
    public static final long[] ONLY_ROW_SIX_MASK = shiftSouth(BasicBitOps.inv(NORTH_BORDER_MASK), 5);
    public static final long[] ONLY_ROW_SEVEN_MASK = shiftSouth(BasicBitOps.inv(NORTH_BORDER_MASK), 6);
    public static final long[] ONLY_ROW_EIGHT_MASK = shiftSouth(BasicBitOps.inv(NORTH_BORDER_MASK), 7);
    public static final long[] ONLY_ROW_NINE_MASK = shiftSouth(BasicBitOps.inv(NORTH_BORDER_MASK), 8);

    public static final long[][] ROW_MASK_SET = new long[][]{ONLY_ROW_ONE_MASK, ONLY_ROW_TWO_MASK, ONLY_ROW_THREE_MASK, ONLY_ROW_FOUR_MASK, ONLY_ROW_FIVE_MASK, ONLY_ROW_SIX_MASK, ONLY_ROW_SEVEN_MASK, ONLY_ROW_EIGHT_MASK, ONLY_ROW_NINE_MASK};


    public static final long[] CAPTURE_SOUTH_WEST_MASK = BitMaskCreationHelper.getCaptureSouthWestMaskArray();
    public static final long[] CAPTURE_SOUTH_EAST_MASK = BitMaskCreationHelper.getCaptureSouthEastMaskArray();
    public static final long[] CAPTURE_NORTH_EAST_MASK = BitMaskCreationHelper.getCaptureNorthEastMask();
    public static final long[] CAPTURE_NORTH_WEST_MASK = BitMaskCreationHelper.getCaptureNorthWestMask();


    //For evaluation or Quiescence
    public static final long[] CAN_BLOCK_PLAYER_1_PIECE_MASK = BitMaskCreationHelper.getCanBlockPlayer1PieceMask();
    public static final long[] CAN_BLOCK_PLAYER_2_PIECE_MASK = BitMaskCreationHelper.getCanBlockPlayer2PieceMask();

    //For BlockedPiecesEvaluation
    public static final long[] ONLY_NORTH_HALF_OF_BOARD_MASK = BitMaskCreationHelper.getOnlyNorthHalfOfBoardMask();
    public static final long[] ONLY_SOUTH_HALF_OF_BOARD_MASK = BitMaskCreationHelper.getOnlySouthHalfOfTheBoardMask();


    public static long[] bitShiftL(long[] board, int shiftBy) {
        long[] shifted = new long[]{board[0] << shiftBy | board[1] >>> 64 - shiftBy, board[1] << shiftBy};
        return and(shifted, BIT_MAP_MASK);
    }

    public static void bitShiftLInPlace(long[] board, int shiftBy) {
        long temp = board[0] << shiftBy | board[1] >>> (64 - shiftBy);
        board[1] = board[1] << shiftBy;
        board[0] = temp;
        andInPlace(board, BIT_MAP_MASK);
    }

    public static long[] bitShiftR(long[] board, int shiftBy) {
        long[] shifted = new long[]{board[0] >>> shiftBy, board[1] >>> shiftBy | board[0] << 64 - shiftBy};
        return and(shifted, BIT_MAP_MASK);
    }

    public static void bitShiftRInPlace(long[] board, int shiftBy) {
        long temp = board[0] >>> shiftBy;
        board[1] = board[1] >>> shiftBy | board[0] << (64 - shiftBy);
        board[0] = temp;
        andInPlace(board, BIT_MAP_MASK);
    }

    public static long[] or(long[] board1, long[] board2) {
        return new long[]{board1[0] | board2[0], board1[1] | board2[1]};
    }

    public static long[] and(long[] board1, long[] board2) {
        return new long[]{board1[0] & board2[0], board1[1] & board2[1]};
    }

    public static long[] inv(long[] board) {
        long[] inv = new long[]{~board[0], ~board[1]};
        return BasicBitOps.and(inv, BIT_MAP_MASK);
    }

    public static long[] XOR(long[] board1, long[] board2) {
        return new long[]{board1[0] ^ board2[0], board1[1] ^ board2[1]};
    }

    public static long[] shiftNorth(long[] board, int steps) {
        long[] shifted = board;
        for (int i = 0; i < steps; i++) {
            if (i == 0) {
                shifted = bitShiftL(board, 9);
            } else {
                bitShiftLInPlace(shifted, 9);
            }
        }
        return shifted;
    }

    public static long[] shiftSouth(long[] board, int steps) {
        long[] shifted = board;
        for (int i = 0; i < steps; i++) {
            if (i == 0) {
                shifted = bitShiftR(board, 9);
            } else {
                bitShiftRInPlace(shifted, 9);
            }
        }
        return shifted;
    }

    public static long[] shiftEast(long[] board, int steps) {
        long[] masked = board;
        for (int i = 0; i < steps; i++) {
            if (i == 0) {
                masked = and(masked, EAST_BORDER_MASK);
            } else {
                andInPlace(masked, EAST_BORDER_MASK);
            }
            bitShiftRInPlace(masked, 1);
        }
        return masked;
    }

    public static long[] shiftWest(long[] board, int steps) {
        long[] masked = board;
        for (int i = 0; i < steps; i++) {
            if (i == 0) {
                masked = and(masked, WEST_BORDER_MASK);
            } else {
                andInPlace(masked, WEST_BORDER_MASK);
            }
            bitShiftLInPlace(masked, 1);
        }
        return masked;
    }

    public static long[] shiftNorthWest(long[] board, int steps) {
        long[] shifted = board;
        for (int i = 0; i < steps; i++) {
            if (i == 0) {
                shifted = shiftNorth(board, 1);
            } else {
                shiftNorthInPlace(shifted, 1);
            }
            shiftWestInPlace(shifted, 1);
        }
        return shifted;
    }

    public static long[] shiftNorthEast(long[] board, int steps) {
        long[] shifted = board;
        for (int i = 0; i < steps; i++) {
            if (i == 0) {
                shifted = shiftNorth(board, 1);
            } else {
                shiftNorthInPlace(shifted, 1);
            }
            shiftEastInPlace(shifted, 1);
        }
        return shifted;
    }

    public static long[] shiftSouthWest(long[] board, int steps) {
        long[] shifted = board;
        for (int i = 0; i < steps; i++) {
            if (i == 0) {
                shifted = shiftSouth(board, 1);
            } else {
                shiftSouthInPlace(shifted, 1);
            }
            shiftWestInPlace(shifted, 1);
        }
        return shifted;
    }

    public static long[] shiftSouthEast(long[] board, int steps) {
        long[] shifted = board;
        for (int i = 0; i < steps; i++) {
            if (i == 0) {
                shifted = shiftSouth(board, 1);
            } else {
                shiftSouthInPlace(shifted, 1);
            }
            shiftEastInPlace(shifted, 1);
        }
        return shifted;
    }

    public static void orInPlace(long[] board1, long[] board2) {
        board1[0] |= board2[0];
        board1[1] |= board2[1];
    }

    public static void andInPlace(long[] board1, long[] board2) {
        board1[0] &= board2[0];
        board1[1] &= board2[1];
    }

    public static void invInPlace(long[] board) {
        board[0] = ~board[0];
        board[1] = ~board[1];
        andInPlace(board, BIT_MAP_MASK);
    }

    public static void XORInPlace(long[] board1, long[] board2) {
        board1[0] ^= board2[0];
        board1[1] ^= board2[1];
    }

    public static void shiftNorthInPlace(long[] board, int steps) {
        for (int i = 0; i < steps; i++) {
            bitShiftLInPlace(board, 9);
        }
    }

    public static void shiftSouthInPlace(long[] board, int steps) {
        for (int i = 0; i < steps; i++) {
            bitShiftRInPlace(board, 9);
        }
    }

    public static void shiftEastInPlace(long[] board, int steps) {
        for (int i = 0; i < steps; i++) {
            andInPlace(board, EAST_BORDER_MASK);
            bitShiftRInPlace(board, 1);
        }
    }

    public static void shiftWestInPlace(long[] board, int steps) {
        for (int i = 0; i < steps; i++) {
            andInPlace(board, WEST_BORDER_MASK);
            bitShiftLInPlace(board, 1);
        }
    }

    public static void shiftNorthWestInPlace(long[] board, int steps) {
        for (int i = 0; i < steps; i++) {
            shiftNorthInPlace(board, 1);
            shiftWestInPlace(board, 1);
        }
    }

    public static void shiftNorthEastInPlace(long[] board, int steps) {
        for (int i = 0; i < steps; i++) {
            shiftNorthInPlace(board, 1);
            shiftEastInPlace(board, 1);
        }
    }

    public static void shiftSouthWestInPlace(long[] board, int steps) {
        for (int i = 0; i < steps; i++) {
            shiftSouthInPlace(board, 1);
            shiftWestInPlace(board, 1);
        }
    }

    public static void shiftSouthEastInPlace(long[] board, int steps) {
        for (int i = 0; i < steps; i++) {
            shiftSouthInPlace(board, 1);
            shiftEastInPlace(board, 1);
        }
    }


    public static int getNumberOfOnesInPlayerBoard(long[] board) {
        return Long.bitCount(board[0]) + Long.bitCount(board[1]);
    }

    public static int[] getNumberOfOnesOnBoard(long[] board) {
        return new int[]{Long.bitCount(board[0]) + Long.bitCount(board[1]), Long.bitCount(board[2]) + Long.bitCount(board[3])};
    }

    public static int getCombinedNumberOfOnesOnBoard(long[] board) {
        return Long.bitCount(board[0]) + Long.bitCount(board[1]) + Long.bitCount(board[2]) + Long.bitCount(board[3]);
    }

    private static long[] resetBoard(long[] original) {
        return original.clone();
    }

    public static void main(String[] args) {
        BitmapFianco bitmapFianco = new BitmapFianco();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(new Fianco().getBoardState());
        long[] bitBoard = bitmapFianco.getPlayer1Board().clone();

        // Test All BasicBitOps Operations:

        // 1. Test inv method:
        System.out.println("Test inv method:\n Original:");
        BitmapFianco.ShowBitBoard(bitBoard);

        // Test new array creation method
        System.out.println("Method for creating new long array");
        BitmapFianco.ShowBitBoard(BasicBitOps.inv(bitBoard));

        // Test in-place method
        System.out.println("In Place method:");
        long[] bitBoardCopy = bitmapFianco.getPlayer1Board().clone();
        BasicBitOps.invInPlace(bitBoardCopy);
        BitmapFianco.ShowBitBoard(bitBoardCopy);

        // 2. Test bitShiftL method:
        System.out.println("\nTest bitShiftL method:");
        long[] shiftedLeft = BasicBitOps.bitShiftL(bitBoard, 2);
        BitmapFianco.ShowBitBoard(shiftedLeft);

        // Test in-place bitShiftL
        System.out.println("Test bitShiftLInPlace method:");
        bitBoardCopy = bitmapFianco.getPlayer1Board().clone();
        BasicBitOps.bitShiftLInPlace(bitBoardCopy, 2);
        BitmapFianco.ShowBitBoard(bitBoardCopy);

        // 3. Test bitShiftR method:
        System.out.println("\nTest bitShiftR method:");
        long[] shiftedRight = BasicBitOps.bitShiftR(bitBoard, 2);
        BitmapFianco.ShowBitBoard(shiftedRight);

        // Test in-place bitShiftR
        System.out.println("Test bitShiftRInPlace method:");
        bitBoardCopy = bitmapFianco.getPlayer1Board().clone();
        BasicBitOps.bitShiftRInPlace(bitBoardCopy, 2);
        BitmapFianco.ShowBitBoard(bitBoardCopy);

        // 4. Test shiftNorth method:
        System.out.println("\nTest shiftNorth method:");
        long[] shiftedNorth = BasicBitOps.shiftNorth(bitBoard, 2);
        BitmapFianco.ShowBitBoard(shiftedNorth);

        // Test in-place shiftNorth
        System.out.println("Test shiftNorthInPlace method:");
        bitBoardCopy = bitmapFianco.getPlayer1Board().clone();
        BasicBitOps.shiftNorthInPlace(bitBoardCopy, 2);
        BitmapFianco.ShowBitBoard(bitBoardCopy);

        // 5. Test shiftSouth method:
        System.out.println("\nTest shiftSouth method:");
        long[] shiftedSouth = BasicBitOps.shiftSouth(bitBoard, 2);
        BitmapFianco.ShowBitBoard(shiftedSouth);

        // Test in-place shiftSouth
        System.out.println("Test shiftSouthInPlace method:");
        bitBoardCopy = bitmapFianco.getPlayer1Board().clone();
        BasicBitOps.shiftSouthInPlace(bitBoardCopy, 2);
        BitmapFianco.ShowBitBoard(bitBoardCopy);

        // 6. Test XOR method:
        System.out.println("\nTest XOR method:");
        long[] xorResult = BasicBitOps.XOR(bitBoard, BasicBitOps.inv(bitBoard));
        BitmapFianco.ShowBitBoard(xorResult);

        // Test in-place XOR
        System.out.println("Test XORInPlace method:");
        bitBoardCopy = bitmapFianco.getPlayer1Board().clone();
        BasicBitOps.XORInPlace(bitBoardCopy, BasicBitOps.inv(bitBoardCopy));
        BitmapFianco.ShowBitBoard(bitBoardCopy);

        // 7. Test shiftWest and shiftEast methods:
        System.out.println("\nTest shiftWest method:");
        long[] shiftedWest = BasicBitOps.shiftWest(bitBoard, 2);
        BitmapFianco.ShowBitBoard(shiftedWest);

        System.out.println("Test shiftWestInPlace method:");
        bitBoardCopy = bitmapFianco.getPlayer1Board().clone();
        BasicBitOps.shiftWestInPlace(bitBoardCopy, 2);
        BitmapFianco.ShowBitBoard(bitBoardCopy);

        System.out.println("\nTest shiftEast method:");
        long[] shiftedEast = BasicBitOps.shiftEast(bitBoard, 2);
        BitmapFianco.ShowBitBoard(shiftedEast);

        System.out.println("Test shiftEastInPlace method:");
        bitBoardCopy = bitmapFianco.getPlayer1Board().clone();
        BasicBitOps.shiftEastInPlace(bitBoardCopy, 2);
        BitmapFianco.ShowBitBoard(bitBoardCopy);


        System.out.println("Test shiftNorthEast method:");
        long[] shiftedNorthEast = BasicBitOps.shiftNorthEast(bitBoard, 2);
        BitmapFianco.ShowBitBoard(shiftedNorthEast);

        System.out.println("Test shiftNorthWest method:");
        long[] shiftedNorthWest = BasicBitOps.shiftNorthWest(bitBoard, 2);
        BitmapFianco.ShowBitBoard(shiftedNorthWest);

        System.out.println("Test shiftSouthEast method:");
        long[] shiftedSouthEast = BasicBitOps.shiftSouthEast(bitBoard, 2);
        BitmapFianco.ShowBitBoard(shiftedSouthEast);

        System.out.println("Test shiftSouthWest method:");
        long[] shiftedSouthWest = BasicBitOps.shiftSouthWest(bitBoard, 2);
        BitmapFianco.ShowBitBoard(shiftedSouthWest);


        System.out.println("Benchmarking methods vs. their in-place versions:");

        // Helper method to reset the board

        long start;
        long end;
        long[] board = {1234567890L, 987654321L};

        // Test 1: Benchmark bitShiftL vs bitShiftLInPlace
        System.out.println("\nBenchmark bitShiftL vs bitShiftLInPlace:");

        // In-place version
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            bitShiftLInPlace(board, 10);
        }
        end = System.nanoTime();
        System.out.println("bitShiftLInPlace: " + (end - start) + " ns");

        // Non-in-place version
        board = resetBoard(board);
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            long[] result = bitShiftL(board, 10);
        }
        end = System.nanoTime();
        System.out.println("bitShiftL: " + (end - start) + " ns");


        // Test 2: Benchmark bitShiftR vs bitShiftRInPlace
        System.out.println("\nBenchmark bitShiftR vs bitShiftRInPlace:");

        // In-place version
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            bitShiftRInPlace(board, 10);
        }
        end = System.nanoTime();
        System.out.println("bitShiftRInPlace: " + (end - start) + " ns");

        // Non-in-place version
        board = resetBoard(board);
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            long[] result = bitShiftR(board, 10);
        }
        end = System.nanoTime();
        System.out.println("bitShiftR: " + (end - start) + " ns");


        // Test 3: Benchmark inv vs invInPlace
        System.out.println("\nBenchmark inv vs invInPlace:");

        // In-place version
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            invInPlace(board);
        }
        end = System.nanoTime();
        System.out.println("invInPlace: " + (end - start) + " ns");

        // Non-in-place version
        board = resetBoard(board);
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            long[] result = inv(board);
        }
        end = System.nanoTime();
        System.out.println("inv: " + (end - start) + " ns");


        // Test 4: Benchmark XOR vs XORInPlace
        System.out.println("\nBenchmark XOR vs XORInPlace:");

        // In-place version
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            XORInPlace(board, BIT_MAP_MASK);
        }
        end = System.nanoTime();
        System.out.println("XORInPlace: " + (end - start) + " ns");

        // Non-in-place version
        board = resetBoard(board);
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            long[] result = XOR(board, BIT_MAP_MASK);
        }
        end = System.nanoTime();
        System.out.println("XOR: " + (end - start) + " ns");


        // Test 5: Benchmark shiftNorth vs shiftNorthInPlace
        System.out.println("\nBenchmark shiftNorth vs shiftNorthInPlace:");

        // In-place version
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            shiftNorthInPlace(board, 1);
        }
        end = System.nanoTime();
        System.out.println("shiftNorthInPlace: " + (end - start) + " ns");

        // Non-in-place version
        board = resetBoard(board);
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            long[] result = shiftNorth(board, 1);
        }
        end = System.nanoTime();
        System.out.println("shiftNorth: " + (end - start) + " ns");


        // Test 6: Benchmark shiftSouth vs shiftSouthInPlace
        System.out.println("\nBenchmark shiftSouth vs shiftSouthInPlace:");

        // In-place version
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            shiftSouthInPlace(board, 1);
        }
        end = System.nanoTime();
        System.out.println("shiftSouthInPlace: " + (end - start) + " ns");

        // Non-in-place version
        board = resetBoard(board);
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            long[] result = shiftSouth(board, 1);
        }
        end = System.nanoTime();
        System.out.println("shiftSouth: " + (end - start) + " ns");


        // Test 7: Benchmark shiftWest vs shiftWestInPlace
        System.out.println("\nBenchmark shiftWest vs shiftWestInPlace:");

        // In-place version
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            shiftWestInPlace(board, 1);
        }
        end = System.nanoTime();
        System.out.println("shiftWestInPlace: " + (end - start) + " ns");

        // Non-in-place version
        board = resetBoard(board);
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            long[] result = shiftWest(board, 1);
        }
        end = System.nanoTime();
        System.out.println("shiftWest: " + (end - start) + " ns");


        // Test 8: Benchmark shiftEast vs shiftEastInPlace
        System.out.println("\nBenchmark shiftEast vs shiftEastInPlace:");

        // In-place version
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            shiftEastInPlace(board, 1);
        }
        end = System.nanoTime();
        System.out.println("shiftEastInPlace: " + (end - start) + " ns");

        // Non-in-place version
        board = resetBoard(board);
        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            long[] result = shiftEast(board, 1);
        }
        end = System.nanoTime();
        System.out.println("shiftEast: " + (end - start) + " ns");


    }


}