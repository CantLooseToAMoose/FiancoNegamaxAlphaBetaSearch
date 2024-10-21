package Fianco.AI.Search.Evaluation;

import Fianco.AI.BitBoard.BasicBitOps;
import Fianco.AI.BitBoard.BitmapFianco;

public class BlockPieceEvaluation {
    private static final int player1BlockMaskRow = (5 - 1) / 9;
    private static final int player2BlockMaskRow = (77 - 1) / 9;
    private static final int player1BlockMaskCol = (5 - 1) % 9;
    private static final int player2BlockMaskCol = (77 - 1) % 9;
    private static final long[][][] player1BlockMaskDependingOnPieceRowAndCol = getPlayer1BlockMaskDependingOnPieceRowAndCol();
    private static final long[][][] player2BlockMaskDependingOnPieceRowAndCol = getPlayer2BlockMaskDependingOnPieceRowAndCol();


    private static long[][][] getPlayer1BlockMaskDependingOnPieceRowAndCol() {
        long[][][] masks = new long[9][9][2];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                long[] mask = BasicBitOps.CAN_BLOCK_PLAYER_1_PIECE_MASK.clone();
                int rowDiff = player1BlockMaskRow - i;
                int colDiff = player1BlockMaskCol - j;
                if (rowDiff < 0) {
                    BasicBitOps.shiftSouthInPlace(mask, Math.abs(rowDiff));
                } else if (rowDiff > 0) {
                    BasicBitOps.shiftNorthInPlace(mask, rowDiff);
                }
                if (colDiff < 0) {
                    BasicBitOps.shiftEastInPlace(mask, Math.abs(colDiff));
                } else {
                    BasicBitOps.shiftWestInPlace(mask, colDiff);
                }
                masks[i][j] = mask.clone();
            }
        }
        return masks;
    }

    private static long[][][] getPlayer2BlockMaskDependingOnPieceRowAndCol() {
        long[][][] masks = new long[9][9][2];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                long[] mask = BasicBitOps.CAN_BLOCK_PLAYER_2_PIECE_MASK.clone();
                int rowDiff = player2BlockMaskRow - i;
                int colDiff = player2BlockMaskCol - j;

                if (rowDiff < 0) {
                    BasicBitOps.shiftSouthInPlace(mask, Math.abs(rowDiff));
                } else if (rowDiff > 0) {
                    BasicBitOps.shiftNorthInPlace(mask, rowDiff);
                }
                if (colDiff < 0) {
                    BasicBitOps.shiftEastInPlace(mask, Math.abs(colDiff));
                } else {
                    BasicBitOps.shiftWestInPlace(mask, colDiff);
                }
                masks[i][j] = mask.clone();
            }
        }
        return masks;
    }


    public static long[] BoardOfEnemyBlocksForPiece(long[] player1Board, long[] player2Board, int piecePosition, boolean isPlayerOne) {
        long[] mask;
        int rowTo = (piecePosition - 1) / 9;
        int colTo = (piecePosition - 1) % 9;
        if (isPlayerOne) {
            mask = BasicBitOps.and(player1BlockMaskDependingOnPieceRowAndCol[rowTo][colTo], player2Board);
        } else {
            mask = BasicBitOps.and(player2BlockMaskDependingOnPieceRowAndCol[rowTo][colTo], player1Board);
        }
        return mask;
    }

    public static int NumberOfEnemyBlocksForPiece(long[] player1Board, long[] player2Board, int piecePosition, boolean isPlayerOne) {
        return BasicBitOps.getNumberOfOnesInPlayerBoard(BoardOfEnemyBlocksForPiece(player1Board, player2Board, piecePosition, isPlayerOne));
    }


    public static int LowestNumberOfEnemyBlocksForAllPieces(long[] board, boolean isPlayerOne) {
        int lowestBlocks = Integer.MAX_VALUE;
        long[] player1Board = new long[]{board[0], board[1]};
        long[] player2Board = new long[]{board[2], board[3]};
        long[] playerBoard;
        if (isPlayerOne) {
            playerBoard = player1Board;
        } else {
            playerBoard = player2Board;
        }
        for (int i = 0; i < playerBoard.length; i++) {
            long temp = playerBoard[i];
            // Iterate through the set bits (pieces)
            while (temp != 0) {
                // Isolate the lowest set bit and get the Position
                int piecePosition;
                if (i == 0) {
                    piecePosition = 64 - Long.numberOfTrailingZeros(temp);  // For the first part of the board (0-63)
                } else {
                    piecePosition = 64 - Long.numberOfTrailingZeros(temp) + 64;  // For the second part of the board (64-81)
                }
                int blocks = NumberOfEnemyBlocksForPiece(player1Board, player2Board, piecePosition, isPlayerOne);
                lowestBlocks = Math.min(lowestBlocks, blocks);
                // Flip the lowest set bit to move on to the next piece
                temp &= (temp - 1);
            }
        }
        return lowestBlocks;
    }


    public static boolean thereIsAnUnblockedPiece(long[] board, boolean isPlayerOne) {

        // First of all Populate these arrays to know which blockers there are for each piece
        int[] pieces = new int[82];
        int[] blockers = new int[82];
        int uniquePieces = 0;
        int uniqueBlockers = 0;
        int[] pieceBlockers = new int[16];  // Bitmask of blockers for each piece


        long[] player1Board = new long[]{board[0], board[1]};
        long[] player2Board = new long[]{board[2], board[3]};
        long[] playerBoard;
        if (isPlayerOne) {
            playerBoard = player1Board;
            playerBoard = BasicBitOps.and(playerBoard, BasicBitOps.ONLY_SOUTH_HALF_OF_BOARD_MASK);
        } else {
            playerBoard = player2Board;
            playerBoard = BasicBitOps.and(playerBoard, BasicBitOps.ONLY_NORTH_HALF_OF_BOARD_MASK);
        }
        for (int i = 0; i < playerBoard.length; i++) {
            long temp;
            if (isPlayerOne) {
                temp = playerBoard[i == 0 ? 1 : 0];
            } else {
                temp = playerBoard[i];
            }
            if (isPlayerOne) {
                // Iterate through the set bits (pieces)
                while (temp != 0) {
                    // Isolate the lowest set bit and get the Position
                    int piecePosition;
                    if (i == 1) {
                        piecePosition = 64 - Long.numberOfTrailingZeros(temp);  // For the first part of the board (0-63)
                    } else {
                        piecePosition = 64 - Long.numberOfTrailingZeros(temp) + 64;  // For the second part of the board (64-81)
                    }
                    long[] blockMask = BoardOfEnemyBlocksForPiece(player1Board, player2Board, piecePosition, isPlayerOne);

                    if (pieces[piecePosition] == 0) {
                        pieces[piecePosition] = uniquePieces++;
                    }
                    for (int j = 0; j < blockMask.length; j++) {
                        long temp2 = blockMask[j];
                        // Iterate through the set bits (pieces)
                        while (temp2 != 0) {
                            // Isolate the lowest set bit and get the Position
                            int piecePosition2;
                            if (j == 0) {
                                piecePosition2 = 64 - Long.numberOfTrailingZeros(temp2);  // For the first part of the board (0-63)
                            } else {
                                piecePosition2 = 64 - Long.numberOfTrailingZeros(temp2) + 64;  // For the second part of the board (64-81)
                            }
                            if (blockers[piecePosition2] == 0) {
                                blockers[piecePosition2] = ++uniqueBlockers;
                            }
                            pieceBlockers[pieces[piecePosition]] |= (1 << blockers[piecePosition2]);
                            // Flip the lowest set bit to move on to the next piece
                            temp2 &= (temp2 - 1);
                        }
                    }
                    // Flip the lowest set bit to move on to the next piece
                    temp &= (temp - 1);
                }
            } else {
                // Iterate through the set bits (pieces)
                while (temp != 0) {
                    // Isolate the lowest set bit and get the Position
                    int piecePosition;
                    if (i == 0) {
                        piecePosition = Long.numberOfLeadingZeros(temp) + 1;  // First part of the board (0-63)
                    } else {
                        piecePosition = 65 + Long.numberOfLeadingZeros(temp);  // For the second part of the board (64-81)
                    }
                    long[] blockMask = BoardOfEnemyBlocksForPiece(player1Board, player2Board, piecePosition, isPlayerOne);

                    if (pieces[piecePosition] == 0) {
                        pieces[piecePosition] = uniquePieces++;
                    }
                    for (int j = 0; j < blockMask.length; j++) {
                        long temp2 = blockMask[j];
                        // Iterate through the set bits (pieces)
                        while (temp2 != 0) {
                            // Isolate the lowest set bit and get the Position
                            int piecePosition2;
                            if (j == 0) {
                                piecePosition2 = 64 - Long.numberOfTrailingZeros(temp2);  // For the first part of the board (0-63)
                            } else {
                                piecePosition2 = 64 - Long.numberOfTrailingZeros(temp2) + 64;  // For the second part of the board (64-81)
                            }
                            if (blockers[piecePosition2] == 0) {
                                blockers[piecePosition2] = ++uniqueBlockers;
                            }
                            pieceBlockers[pieces[piecePosition]] |= (1 << blockers[piecePosition2]);
                            // Flip the lowest set bit to move on to the next piece
                            temp2 &= (temp2 - 1);
                        }
                    }
                    // Flip the lowest set bit to move on to the next piece
                    temp &= ~(1L << (63 - Long.numberOfLeadingZeros(temp)));
                }
            }
        }


        return findUnblockedPiece(pieceBlockers, uniquePieces, 0, 0);
    }


    public static boolean findUnblockedPiece(int[] pieceBlockers, int numberOfPieces, int pieceIndex, int usedBlockers) {
        // Base case: If all pieces have been considered, return empty
        if (pieceIndex == numberOfPieces) {
            return false;
        }

        // Get the available blockers for the current piece (not already used)
        int availableBlockers = pieceBlockers[pieceIndex] & ~usedBlockers;

        // If no blockers are available for this piece, it can reach the goal
        if (availableBlockers == 0) {
            return true;
        }

        // Try to assign a blocker to this piece
        while (availableBlockers != 0) {
            int blocker = availableBlockers & -availableBlockers;  // Extract the rightmost set bit
            availableBlockers &= availableBlockers - 1;  // Remove this blocker from consideration

            // Recursively check for the next piece using the assigned blocker
            boolean result = findUnblockedPiece(pieceBlockers, numberOfPieces, pieceIndex + 1, usedBlockers | blocker);
            if (result) {
                return result;  // If valid, propagate the result upwards
            }
        }
        return false;
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        int[][] boardState = new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 1, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 2, 2, 0, 0, 0, 0}};

        BitmapFianco bitmapFianco = new BitmapFianco();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(boardState);
        long[] board = bitmapFianco.getFullBoard();
        System.out.println(bitmapFianco);
        System.out.println("For Player1:" + thereIsAnUnblockedPiece(board, true));
        System.out.println("For Player2:" + thereIsAnUnblockedPiece(board, false));

        boardState = new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 1, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 2, 2, 2, 0, 0, 0, 0}};

        bitmapFianco = new BitmapFianco();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(boardState);
        board = bitmapFianco.getFullBoard();
        System.out.println(bitmapFianco);
        System.out.println("For Player1:" + thereIsAnUnblockedPiece(board, true));
        System.out.println("For Player2:" + thereIsAnUnblockedPiece(board, false));
        System.out.println((System.nanoTime() - startTime) / 1_000_000_000);

        boardState = new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 1, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0}};

        bitmapFianco = new BitmapFianco();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(boardState);
        board = bitmapFianco.getFullBoard();
        System.out.println(bitmapFianco);
        System.out.println("For Player1:" + thereIsAnUnblockedPiece(board, true));
        System.out.println("For Player2:" + thereIsAnUnblockedPiece(board, false));
        System.out.println((System.nanoTime() - startTime) / 1_000_000_000);

        boardState = new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 2, 2, 2, 0, 0, 0, 0}};

        bitmapFianco = new BitmapFianco();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(boardState);
        board = bitmapFianco.getFullBoard();
        System.out.println(bitmapFianco);
        System.out.println("For Player1:" + thereIsAnUnblockedPiece(board, true));
        System.out.println("For Player2:" + thereIsAnUnblockedPiece(board, false));
        System.out.println((System.nanoTime() - startTime) / 1_000_000_000);

        boardState = new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 1, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 0},
                {0, 0, 2, 2, 2, 0, 0, 0, 0}};

        bitmapFianco = new BitmapFianco();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(boardState);
        board = bitmapFianco.getFullBoard();
        System.out.println(bitmapFianco);
        System.out.println("For Player1:" + thereIsAnUnblockedPiece(board, true));
        System.out.println("For Player2:" + thereIsAnUnblockedPiece(board, false));
        System.out.println((System.nanoTime() - startTime) / 1_000_000_000);


        boardState = new int[][]{
                {0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 2, 0, 0},
                {0, 0, 0, 0, 2, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 1, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 2, 2, 2, 0, 0, 0, 0}};

        bitmapFianco = new BitmapFianco();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(boardState);
        board = bitmapFianco.getFullBoard();
        System.out.println(bitmapFianco);
        System.out.println("For Player1:" + thereIsAnUnblockedPiece(board, true));
        System.out.println("For Player2:" + thereIsAnUnblockedPiece(board, false));
        System.out.println((System.nanoTime() - startTime) / 1_000_000_000);


        boardState = new int[][]{
                {1, 0, 0, 0, 0, 0, 0, 1, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 1, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0}};

        bitmapFianco = new BitmapFianco();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(boardState);
        board = bitmapFianco.getFullBoard();
        System.out.println(bitmapFianco);
        System.out.println("For Player1:" + thereIsAnUnblockedPiece(board, true));
        System.out.println("For Player2:" + thereIsAnUnblockedPiece(board, false));
        System.out.println((System.nanoTime() - startTime) / 1_000_000_000);

        boardState = new int[][]{
                {1, 1, 1, 1, 1, 1, 1, 1, 1},
                {0, 1, 0, 0, 0, 0, 0, 1, 0},
                {0, 0, 1, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 1, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 2, 0, 2, 0, 0, 0},
                {0, 0, 2, 0, 0, 0, 2, 0, 0},
                {0, 2, 0, 0, 0, 0, 0, 2, 0},
                {2, 2, 2, 2, 2, 2, 2, 2, 2}};

        bitmapFianco = new BitmapFianco();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(boardState);
        board = bitmapFianco.getFullBoard();
        System.out.println(bitmapFianco);
        System.out.println("For Player1:" + thereIsAnUnblockedPiece(board, true));
        System.out.println("For Player2:" + thereIsAnUnblockedPiece(board, false));
        System.out.println((System.nanoTime() - startTime) / 1_000_000_000);
    }

}
