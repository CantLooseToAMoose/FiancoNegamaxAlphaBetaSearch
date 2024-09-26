package search;

import BitBoard.AdvancedBitOps;
import BitBoard.BasicBitOps;
import BitBoard.BitmapFianco;

public class Evaluate {

    //    TODO: expand on rating the pieces by the number of pieces they can be blocked by. And by the number of pieces they are blocking.
//    Idea: Try to get a board with numbers that represent the number of pieces that can reach this position in n-rounds. Then compare to the places that a piece can reach in n rounds.
    public static int calculatePieceDifference(long[] board, boolean isPlayerOne) {
        int[] piecesOnBoard = BasicBitOps.getNumberOfOnesOnBoard(board);
        if (isPlayerOne) {
            return piecesOnBoard[0] - piecesOnBoard[1];
        } else {
            return piecesOnBoard[1] - piecesOnBoard[0];
        }
    }

    public static int calculateWeightedPieceDifference(long[] board, boolean isPlayerOne) {
        long[] player1Board = new long[]{board[0], board[1]};
        long[] player2Board = new long[]{board[2], board[3]};
        int score1 = 0;
        int score2 = 0;
        for (int i = 0; i < 9; i++) {
//            System.out.println("i:" + i);
//            System.out.println("player1Board:");
//            BitmapFianco.ShowBitBoard(player1Board);
            score1 += BasicBitOps.getNumberOfOnesInPlayerBoard(BasicBitOps.and(player1Board, BasicBitOps.ROW_MASK_SET[i])) * (i + 1);
//            System.out.println("score1: " + score1);
//            System.out.println("player2Board:");
//            BitmapFianco.ShowBitBoard(player2Board);
            score2 += BasicBitOps.getNumberOfOnesInPlayerBoard(BasicBitOps.and(player2Board, BasicBitOps.ROW_MASK_SET[i])) * (9 - i);
//            System.out.println(score2);
        }
        return isPlayerOne ? score1 - score2 : score2 - score1;

    }

    public static boolean checkForWin(long[] board, boolean isPlayerOne) {
        int[] piecesOnBoard = BasicBitOps.getNumberOfOnesOnBoard(board);
        if (isPlayerOne) {
            if (piecesOnBoard[1] == 0) {
                return true;
            }
            long[] lastRow = BasicBitOps.and(new long[]{board[0], board[1]}, BasicBitOps.ROW_MASK_SET[8]);
            if (BasicBitOps.getNumberOfOnesInPlayerBoard(lastRow) != 0) {
                return true;
            }
        } else {
            if (piecesOnBoard[0] == 0) {
                return true;
            }
            long[] firstRow = BasicBitOps.and(new long[]{board[2], board[3]}, BasicBitOps.ROW_MASK_SET[0]);
            if (BasicBitOps.getNumberOfOnesInPlayerBoard(firstRow) != 0) {
                return true;
            }
        }
        return false;

    }

    public static int evaluateWin(long[] board, boolean isPlayerOne) {
        if (checkForWin(board, isPlayerOne)) {
            return 1;
        } else if (checkForWin(board, !isPlayerOne)) {
            return -1;
        }
        return 0;
    }

    public static int randomValue(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }


    public static int combinedEvaluate(long[] board, boolean isPlayerOne) {
        return calculateWeightedPieceDifference(board, isPlayerOne) + randomValue(-1, 1);
    }
}
