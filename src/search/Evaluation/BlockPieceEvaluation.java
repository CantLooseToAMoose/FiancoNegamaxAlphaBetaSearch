package search.Evaluation;

import BitBoard.BasicBitOps;

public class BlockPieceEvaluation {
    private static final int player1BlockMaskRow = (5 - 1) / 9;
    private static final int player2BlockMaskRow = (77 - 1) / 9;
    private static final int player1BlockMaskCol = (5 - 1) % 9;
    private static final int player2BlockMaskCol = (77 - 1) % 9;


    public static int NumberOfEnemyBlocksForPiece(long[] player1Board, long[] player2Board, int piecePosition, boolean isPlayerOne) {
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
        return BasicBitOps.getNumberOfOnesInPlayerBoard(mask);
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
}
