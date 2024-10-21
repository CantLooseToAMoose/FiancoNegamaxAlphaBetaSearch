package Fianco.AI.BitBoard;

import Fianco.GameEngine.Fianco;

public class AdvancedBitOps {

    public static long[] FreeTilesBoardMask(long[] board1, long[] board2) {
        return BitMaskCreationHelper.getFreeTilesBitMask(board1, board2);
    }

    public static long[] possibleWestMovePieces(long[] board1, long[] board2) {
        long[] board1_shifted = BasicBitOps.shiftEast(board1, 1);
        long[] board2_shifted = BasicBitOps.shiftEast(board2, 1);
        board1 = BasicBitOps.and(board1, BasicBitOps.WEST_BORDER_MASK);
        return BasicBitOps.and(board1, BitMaskCreationHelper.getFreeTilesBitMask(board1_shifted, board2_shifted));
    }

    public static long[] fasterPossibleWestMovePieces(long[] board1, long[] freeTilesBoardMask) {
        long[] freeTilesBoardCopy = BasicBitOps.shiftEast(freeTilesBoardMask, 1);
        return BasicBitOps.and(board1, freeTilesBoardCopy);
    }

    public static long[] possibleEastMovePieces(long[] board1, long[] board2) {
        long[] board1_shifted = BasicBitOps.shiftWest(board1, 1);
        long[] board2_shifted = BasicBitOps.shiftWest(board2, 1);
        board1 = BasicBitOps.and(board1, BasicBitOps.EAST_BORDER_MASK);
        return BasicBitOps.and(board1, BitMaskCreationHelper.getFreeTilesBitMask(board1_shifted, board2_shifted));
    }

    public static long[] fasterPossibleEastMovePieces(long[] board1, long[] freeTilesBoardMask) {
        long[] freeTilesBoardCopy = BasicBitOps.shiftWest(freeTilesBoardMask, 1);
        return BasicBitOps.and(board1, freeTilesBoardCopy);
    }

    public static long[] possibleNorthMovePieces(long[] board1, long[] board2) {
        long[] board1_shifted = BasicBitOps.shiftSouth(board1, 1);
        long[] board2_shifted = BasicBitOps.shiftSouth(board2, 1);
        board1 = BasicBitOps.and(board1, BasicBitOps.NORTH_BORDER_MASK);
        return BasicBitOps.and(board1, BitMaskCreationHelper.getFreeTilesBitMask(board1_shifted, board2_shifted));
    }

    public static long[] fasterPossibleNorthMovePieces(long[] board1, long[] freeTilesBoardMask) {
        long[] freeTilesBoardCopy = BasicBitOps.shiftSouth(freeTilesBoardMask, 1);
        return BasicBitOps.and(board1, freeTilesBoardCopy);
    }

    public static long[] possibleSouthMovePieces(long[] board1, long[] board2) {
        long[] board1_shifted = BasicBitOps.shiftNorth(board1, 1);
        long[] board2_shifted = BasicBitOps.shiftNorth(board2, 1);
        board1 = BasicBitOps.and(board1, BasicBitOps.SOUTH_BORDER_MASK);
        return BasicBitOps.and(board1, BitMaskCreationHelper.getFreeTilesBitMask(board1_shifted, board2_shifted));
    }

    public static long[] fasterPossibleSouthMovePieces(long[] board1, long[] freeTilesBoardMask) {
        long[] freeTilesBoardCopy = BasicBitOps.shiftNorth(freeTilesBoardMask, 1);
        return BasicBitOps.and(board1, freeTilesBoardCopy);
    }

    public static long[] possibleSouthEastMovePieces(long[] board1, long[] board2) {
        long[] captureShift = BasicBitOps.shiftNorthWest(board2, 1);
        long[] pseudoCapturePossible = BasicBitOps.and(board1, captureShift);
        long[] board1_shifted = BasicBitOps.shiftNorthWest(board1, 2);
        long[] board2_shifted = BasicBitOps.shiftNorthWest(board2, 2);
        pseudoCapturePossible = BasicBitOps.and(pseudoCapturePossible, BasicBitOps.CAPTURE_SOUTH_EAST_MASK);
        return BasicBitOps.and(pseudoCapturePossible, BitMaskCreationHelper.getFreeTilesBitMask(board1_shifted, board2_shifted));
    }

    public static long[] fasterPossibleSouthEastMovePieces(long[] board1, long[] board2, long[] freeTileBoardMask) {
        long[] captureShift = BasicBitOps.shiftNorthWest(board2, 1);
        BasicBitOps.andInPlace(captureShift, board1);
        long[] freeTileBoardMaskShifted = BasicBitOps.shiftNorthWest(freeTileBoardMask, 2);
        BasicBitOps.andInPlace(captureShift, freeTileBoardMaskShifted);
        return captureShift;
    }

    public static long[] possibleSouthWestMovePieces(long[] board1, long[] board2) {
        long[] captureShift = BasicBitOps.shiftNorthEast(board2, 1);
        long[] pseudoCapturePossible = BasicBitOps.and(board1, captureShift);
        long[] board1_shifted = BasicBitOps.shiftNorthEast(board1, 2);
        long[] board2_shifted = BasicBitOps.shiftNorthEast(board2, 2);
        pseudoCapturePossible = BasicBitOps.and(pseudoCapturePossible, BasicBitOps.CAPTURE_SOUTH_WEST_MASK);
        return BasicBitOps.and(pseudoCapturePossible, BitMaskCreationHelper.getFreeTilesBitMask(board1_shifted, board2_shifted));
    }

    public static long[] fasterPossibleSouthWestMovePieces(long[] board1, long[] board2, long[] freeTileBoardMask) {
        long[] captureShift = BasicBitOps.shiftNorthEast(board2, 1);
        BasicBitOps.andInPlace(captureShift, board1);
        long[] freeTileBoardMaskShifted = BasicBitOps.shiftNorthEast(freeTileBoardMask, 2);
        BasicBitOps.andInPlace(captureShift, freeTileBoardMaskShifted);
        return captureShift;
    }

    public static long[] possibleNorthEastMovePieces(long[] board1, long[] board2) {
        long[] captureShift = BasicBitOps.shiftSouthWest(board2, 1);
        long[] pseudoCapturePossible = BasicBitOps.and(board1, captureShift);
        long[] board1_shifted = BasicBitOps.shiftSouthWest(board1, 2);
        long[] board2_shifted = BasicBitOps.shiftSouthWest(board2, 2);
        pseudoCapturePossible = BasicBitOps.and(pseudoCapturePossible, BasicBitOps.CAPTURE_NORTH_EAST_MASK);
        return BasicBitOps.and(pseudoCapturePossible, BitMaskCreationHelper.getFreeTilesBitMask(board1_shifted, board2_shifted));
    }

    public static long[] fasterPossibleNorthEastMovePieces(long[] board1, long[] board2, long[] freeTileBoardMask) {
        long[] captureShift = BasicBitOps.shiftSouthWest(board2, 1);
        BasicBitOps.andInPlace(captureShift, board1);
        long[] freeTileBoardMaskShifted = BasicBitOps.shiftSouthWest(freeTileBoardMask, 2);
        BasicBitOps.andInPlace(captureShift, freeTileBoardMaskShifted);
        return captureShift;
    }

    public static long[] possibleNorthWestMovePieces(long[] board1, long[] board2) {
        long[] captureShift = BasicBitOps.shiftSouthEast(board2, 1);
        long[] pseudoCapturePossible = BasicBitOps.and(board1, captureShift);
        long[] board1_shifted = BasicBitOps.shiftSouthEast(board1, 2);
        long[] board2_shifted = BasicBitOps.shiftSouthEast(board2, 2);
        pseudoCapturePossible = BasicBitOps.and(pseudoCapturePossible, BasicBitOps.CAPTURE_NORTH_WEST_MASK);
        return BasicBitOps.and(pseudoCapturePossible, BitMaskCreationHelper.getFreeTilesBitMask(board1_shifted, board2_shifted));
    }

    public static long[] fasterPossibleNorthWestMovePieces(long[] board1, long[] board2, long[] freeTileBoardMask) {
        long[] captureShift = BasicBitOps.shiftSouthEast(board2, 1);
        BasicBitOps.andInPlace(captureShift, board1);
        long[] freeTileBoardMaskShifted = BasicBitOps.shiftSouthEast(freeTileBoardMask, 2);
        BasicBitOps.andInPlace(captureShift, freeTileBoardMaskShifted);
        return captureShift;
    }

    public static void main(String[] args) {
        // Initialize the board states
        BitmapFianco bitmapFianco = new BitmapFianco();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(new Fianco().getBoardState());
        long[] board1 = bitmapFianco.getPlayer1Board().clone();
        long[] board2 = bitmapFianco.getPlayer2Board().clone();
        long[] freeBoardTileMask = FreeTilesBoardMask(board1, board2);

        // 1. Test possibleWestMovePieces method:
        System.out.println("Test possibleWestMovePieces method:");

        // Test new array creation method
        System.out.println("New array creation method:");
        long[] westMoves = possibleWestMovePieces(board1.clone(), board2.clone());
        BitmapFianco.ShowBitBoard(westMoves);

        // Test in-place method
        System.out.println("In-place method:");
        long[] board1Copy = bitmapFianco.getPlayer1Board().clone();
        long[] board2Copy = bitmapFianco.getPlayer2Board().clone();
        westMoves = AdvancedBitOps.fasterPossibleWestMovePieces(board1Copy, freeBoardTileMask);
        BitmapFianco.ShowBitBoard(westMoves);

        // 2. Test possibleEastMovePieces method:
        System.out.println("\nTest possibleEastMovePieces method:");
        long[] eastMoves = AdvancedBitOps.possibleEastMovePieces(board1.clone(), board2.clone());
        BitmapFianco.ShowBitBoard(eastMoves);

        // Test in-place method
        System.out.println("In-place method:");

        eastMoves = AdvancedBitOps.fasterPossibleEastMovePieces(board1Copy, freeBoardTileMask);
        BitmapFianco.ShowBitBoard(eastMoves);

        // 3. Test possibleNorthMovePieces method:
        System.out.println("\nTest possibleNorthMovePieces method:");
        long[] northMoves = AdvancedBitOps.possibleNorthMovePieces(board1.clone(), board2.clone());
        BitmapFianco.ShowBitBoard(northMoves);

        // Test in-place method
        System.out.println("In-place method:");
        northMoves = AdvancedBitOps.fasterPossibleNorthMovePieces(board1Copy, freeBoardTileMask);
        BitmapFianco.ShowBitBoard(northMoves);

        // 4. Test possibleSouthMovePieces method:
        System.out.println("\nTest possibleSouthMovePieces method:");
        long[] southMoves = AdvancedBitOps.possibleSouthMovePieces(board1.clone(), board2.clone());
        BitmapFianco.ShowBitBoard(southMoves);

        // Test in-place method
        System.out.println("In-place method:");
        southMoves = AdvancedBitOps.fasterPossibleSouthMovePieces(board1Copy, freeBoardTileMask);
        BitmapFianco.ShowBitBoard(southMoves);

        System.out.println("Capture Movements:");
        int[][] captureTestBoardState = new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 1, 1, 1, 1, 1},
                {2, 2, 2, 2, 2, 2, 2, 2, 2},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0}};

        bitmapFianco.populateBoardBitmapsFrom2DIntArray(captureTestBoardState);
        board1 = bitmapFianco.getPlayer1Board().clone();
        board2 = bitmapFianco.getPlayer2Board().clone();
        freeBoardTileMask = BitMaskCreationHelper.getFreeTilesBitMask(board1, board2);

        // 5. Test possibleSouthEastMovePieces method:
        System.out.println("\nTest possibleSouthEastMovePieces method:");
        long[] southEastMoves = AdvancedBitOps.possibleSouthEastMovePieces(board1.clone(), board2.clone());
        BitmapFianco.ShowBitBoard(southEastMoves);

        // Test in-place method
        System.out.println("In-place method:");
        board1Copy = bitmapFianco.getPlayer1Board().clone();

        board2Copy = bitmapFianco.getPlayer2Board().clone();
        southEastMoves = fasterPossibleSouthEastMovePieces(board1Copy, board2Copy, freeBoardTileMask);
        BitmapFianco.ShowBitBoard(southEastMoves);

        // 6. Test possibleSouthWestMovePieces method:
        System.out.println("\nTest possibleSouthWestMovePieces method:");
        long[] southWestMoves = AdvancedBitOps.possibleSouthWestMovePieces(board1.clone(), board2.clone());
        BitmapFianco.ShowBitBoard(southWestMoves);

        // Test in-place method
        System.out.println("In-place method:");
        board1Copy = bitmapFianco.getPlayer1Board().clone();

        board2Copy = bitmapFianco.getPlayer2Board().clone();
        southWestMoves = AdvancedBitOps.fasterPossibleSouthWestMovePieces(board1Copy, board2Copy, freeBoardTileMask);
        BitmapFianco.ShowBitBoard(southWestMoves);

        // 7. Test possibleNorthEastMovePieces method:
        System.out.println("\nTest possibleNorthEastMovePieces method:");
        long[] northEastMoves = AdvancedBitOps.possibleNorthEastMovePieces(board1.clone(), board2.clone());
        BitmapFianco.ShowBitBoard(northEastMoves);

        // Test in-place method
        System.out.println("In-place method:");
        board1Copy = bitmapFianco.getPlayer1Board().clone();

        board2Copy = bitmapFianco.getPlayer2Board().clone();
        northEastMoves = AdvancedBitOps.fasterPossibleNorthEastMovePieces(board1Copy, board2Copy, freeBoardTileMask);
        BitmapFianco.ShowBitBoard(northEastMoves);

        // 8. Test possibleNorthWestMovePieces method:
        System.out.println("\nTest possibleNorthWestMovePieces method:");
        long[] northWestMoves = AdvancedBitOps.possibleNorthWestMovePieces(board1.clone(), board2.clone());
        BitmapFianco.ShowBitBoard(northWestMoves);

        // Test in-place method
        System.out.println("In-place method:");
        board1Copy = bitmapFianco.getPlayer1Board().clone();

        board2Copy = bitmapFianco.getPlayer2Board().clone();
        northWestMoves = AdvancedBitOps.fasterPossibleNorthWestMovePieces(board1Copy, board2Copy, freeBoardTileMask);
        BitmapFianco.ShowBitBoard(northWestMoves);

        // 9. Benchmarking Performance Test:
        System.out.println("\nTest time for a single method executed 1000 times:");
        long[] testBoard1 = {1234567890L, 987654321L};
        long[] testBoard2 = {987654321L, 1234567890L};

        // Benchmark in-place version
        long start;
        long end;

        System.out.println("Test Performance for West Moves");
        start = System.nanoTime();
        for (int i = 0; i < 100000000; i++) {
            long[] result = AdvancedBitOps.fasterPossibleWestMovePieces(testBoard1, testBoard2);
        }
        end = System.nanoTime();
        System.out.println("In-place operation time: " + (end - start) + " ns");
        // Benchmark non-in-place version
        start = System.nanoTime();
        for (int i = 0; i < 100000000; i++) {
            long[] result = AdvancedBitOps.possibleWestMovePieces(testBoard1, testBoard2);
        }
        end = System.nanoTime();
        System.out.println("Non-in-place operation time: " + (end - start) + " ns");


        System.out.println("Test Performance for East Moves");
        start = System.nanoTime();
        for (int i = 0; i < 100000000; i++) {
            long[] result = AdvancedBitOps.fasterPossibleEastMovePieces(testBoard1, testBoard2);
        }
        end = System.nanoTime();
        System.out.println("In-place operation time: " + (end - start) + " ns");
        // Benchmark non-in-place version
        start = System.nanoTime();
        for (int i = 0; i < 100000000; i++) {
            long[] result = AdvancedBitOps.possibleEastMovePieces(testBoard1, testBoard2);
        }
        end = System.nanoTime();
        System.out.println("Non-in-place operation time: " + (end - start) + " ns");


        System.out.println("Test Performance for North Moves");
        start = System.nanoTime();
        for (int i = 0; i < 100000000; i++) {
            long[] result = AdvancedBitOps.fasterPossibleNorthMovePieces(testBoard1, testBoard2);
        }
        end = System.nanoTime();
        System.out.println("In-place operation time: " + (end - start) + " ns");
        // Benchmark non-in-place version
        start = System.nanoTime();
        for (int i = 0; i < 100000000; i++) {
            long[] result = AdvancedBitOps.possibleNorthMovePieces(testBoard1, testBoard2);
        }
        end = System.nanoTime();
        System.out.println("Non-in-place operation time: " + (end - start) + " ns");


        System.out.println("Test Performance for South Moves");
        start = System.nanoTime();
        for (int i = 0; i < 100000000; i++) {
            long[] result = AdvancedBitOps.fasterPossibleSouthMovePieces(testBoard1, testBoard2);
        }
        end = System.nanoTime();
        System.out.println("In-place operation time: " + (end - start) + " ns");
        // Benchmark non-in-place version
        start = System.nanoTime();
        for (int i = 0; i < 100000000; i++) {
            long[] result = AdvancedBitOps.possibleSouthMovePieces(testBoard1, testBoard2);
        }
        end = System.nanoTime();
        System.out.println("Non-in-place operation time: " + (end - start) + " ns");

        System.out.println("Test Performance for South East Moves");
        start = System.nanoTime();
        for (int i = 0; i < 100000000; i++) {
            long[] result = AdvancedBitOps.fasterPossibleSouthEastMovePieces(testBoard1, testBoard2,freeBoardTileMask);
        }
        end = System.nanoTime();
        System.out.println("In-place operation time: " + (end - start) + " ns");
        // Benchmark non-in-place version
        start = System.nanoTime();
        for (int i = 0; i < 100000000; i++) {
            long[] result = AdvancedBitOps.possibleSouthEastMovePieces(testBoard1, testBoard2);
        }
        end = System.nanoTime();
        System.out.println("Non-in-place operation time: " + (end - start) + " ns");

        System.out.println("Test Performance for South West Moves");
        start = System.nanoTime();
        for (int i = 0; i < 100000000; i++) {
            long[] result = AdvancedBitOps.fasterPossibleSouthWestMovePieces(testBoard1, testBoard2,freeBoardTileMask);
        }
        end = System.nanoTime();
        System.out.println("In-place operation time: " + (end - start) + " ns");
        // Benchmark non-in-place version
        start = System.nanoTime();
        for (int i = 0; i < 100000000; i++) {
            long[] result = AdvancedBitOps.possibleSouthWestMovePieces(testBoard1, testBoard2);
        }
        end = System.nanoTime();
        System.out.println("Non-in-place operation time: " + (end - start) + " ns");

        System.out.println("Test Performance for North East Moves");
        start = System.nanoTime();
        for (int i = 0; i < 100000000; i++) {
            long[] result = AdvancedBitOps.fasterPossibleNorthEastMovePieces(testBoard1, testBoard2,freeBoardTileMask);
        }
        end = System.nanoTime();
        System.out.println("In-place operation time: " + (end - start) + " ns");
        // Benchmark non-in-place version
        start = System.nanoTime();
        for (int i = 0; i < 100000000; i++) {
            long[] result = AdvancedBitOps.possibleNorthEastMovePieces(testBoard1, testBoard2);
        }
        end = System.nanoTime();
        System.out.println("Non-in-place operation time: " + (end - start) + " ns");


        System.out.println("Test Performance for North West Moves");
        start = System.nanoTime();
        for (int i = 0; i < 100000000; i++) {
            long[] result = AdvancedBitOps.fasterPossibleNorthWestMovePieces(testBoard1, testBoard2,freeBoardTileMask);
        }
        end = System.nanoTime();
        System.out.println("In-place operation time: " + (end - start) + " ns");
        // Benchmark non-in-place version
        start = System.nanoTime();
        for (int i = 0; i < 100000000; i++) {
            long[] result = AdvancedBitOps.possibleNorthWestMovePieces(testBoard1, testBoard2);
        }
        end = System.nanoTime();
        System.out.println("Non-in-place operation time: " + (end - start) + " ns");




    }


}
