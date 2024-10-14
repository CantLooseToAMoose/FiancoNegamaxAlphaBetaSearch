package search.Evaluation;

import BitBoard.BasicBitOps;
import BitBoard.BitmapFianco;

import java.util.*;

public class BlockPieceEvaluation {
    private static final int player1BlockMaskRow = (5 - 1) / 9;
    private static final int player2BlockMaskRow = (77 - 1) / 9;
    private static final int player1BlockMaskCol = (5 - 1) % 9;
    private static final int player2BlockMaskCol = (77 - 1) % 9;


    public static long[] BoardOfEnemyBlocksForPiece(long[] player1Board, long[] player2Board, int piecePosition, boolean isPlayerOne) {
        long[] mask;
        int rowTo = (piecePosition - 1) / 9;
        int colTo = (piecePosition - 1) % 9;
        int rowDiff = 0;
        int colDiff = 0;
        if (isPlayerOne) {
            mask = BasicBitOps.CAN_BLOCK_PLAYER_1_PIECE_MASK.clone();
            rowDiff = player1BlockMaskRow - rowTo;
            colDiff = player1BlockMaskCol - colTo;
            if (rowDiff == 0 && colDiff == 0) {
                BasicBitOps.andInPlace(mask, player2Board);
            }
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
            BasicBitOps.andInPlace(mask, player2Board);
        } else {
            mask = BasicBitOps.CAN_BLOCK_PLAYER_2_PIECE_MASK.clone();
            rowDiff = player2BlockMaskRow - rowTo;
            colDiff = player2BlockMaskCol - colTo;
            if (rowDiff == 0 && colDiff == 0) {
                BasicBitOps.andInPlace(mask, player1Board);
            }
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
            BasicBitOps.andInPlace(mask, player1Board);
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


    public static int thereIsAnUnblockedPiece(long[] board, boolean isPlayerOne) {

        // First of all Populate these arrays to know which blockers there are for each piece
        int[] pieces = new int[82];
        int[] blockers = new int[82];
        int uniquePieces = 0;
        int uniqueBlockers = 0;
        int[] uniquePiecePosition = new int[16];
        int[] pieceBlockers = new int[16];  // Bitmask of blockers for each piece


        long[] player1Board = new long[]{board[0], board[1]};
        long[] player2Board = new long[]{board[2], board[3]};
        long[] playerBoard;
        if (isPlayerOne) {
            playerBoard = player1Board;
        } else {
            playerBoard = player2Board;
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
                        uniquePiecePosition[uniquePieces] = piecePosition;
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
                        uniquePiecePosition[uniquePieces] = piecePosition;
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


        int piece = findUnblockedPiece(pieceBlockers, uniquePieces, 0, 0);
        if (piece == -1) {
            return -1;
        } else {
            return uniquePiecePosition[piece];
        }
    }


    public static int findUnblockedPiece(int[] pieceBlockers, int numberOfPieces, int pieceIndex, int usedBlockers) {
        // Base case: If all pieces have been considered, return empty
        if (pieceIndex == numberOfPieces) {
            return -1;
        }

        // Get the available blockers for the current piece (not already used)
        int availableBlockers = pieceBlockers[pieceIndex] & ~usedBlockers;

        // If no blockers are available for this piece, it can reach the goal
        if (availableBlockers == 0) {
            return pieceIndex;
        }

        // Try to assign a blocker to this piece
        while (availableBlockers != 0) {
            int blocker = availableBlockers & -availableBlockers;  // Extract the rightmost set bit
            availableBlockers &= availableBlockers - 1;  // Remove this blocker from consideration

            // Recursively check for the next piece using the assigned blocker
            int result = findUnblockedPiece(pieceBlockers, numberOfPieces, pieceIndex + 1, usedBlockers | blocker);
            if (result != -1) {
                return result;  // If valid, propagate the result upwards
            }
        }
        return -1;
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
