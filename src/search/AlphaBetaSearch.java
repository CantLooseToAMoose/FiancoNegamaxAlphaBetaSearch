package search;

import BitBoard.BitMapMoveGenerator;
import BitBoard.BitmapFianco;
import BitBoard.MoveConversion;

import java.util.ArrayDeque;
import java.util.LinkedList;

public class AlphaBetaSearch {
    //    Todo: Check for repetetive Movements
    public static int nodes = 0;
    public static final int MAX_NUMBER_OF_MOVES = 15 * 4;
    public static final int MAX_NUMBER_OF_ACTUAL_DEPTH = 40;


    public static int AlphaBeta(long[] board, boolean isPlayerOneTurn, int depth, int actualDepth, short[] moveArray, int alpha, int beta) {
        nodes++;
        int numberOfMoves = BitMapMoveGenerator.populateShortArrayWithAllPossibleMoves(board, isPlayerOneTurn, moveArray, actualDepth * MAX_NUMBER_OF_MOVES);
        int win = Evaluate.evaluateWin(board, isPlayerOneTurn);
        if (win != 0) {
            return (300 + depth) * win;
        }
        if (numberOfMoves == 0 || depth == 0) {
            return Evaluate.combinedEvaluate(board, isPlayerOneTurn);
        }
        int score = -Integer.MAX_VALUE;
        for (int i = 0; i < numberOfMoves; i++) {
            short move = moveArray[actualDepth * MAX_NUMBER_OF_MOVES + i];
            BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, move, isPlayerOneTurn);
            int value = -AlphaBeta(board, !isPlayerOneTurn, depth - 1, actualDepth + 1, moveArray, -beta, -alpha);
            BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, move, isPlayerOneTurn);

            if (value > score) {
                score = value;
            }
            if (score > alpha) {
                alpha = score;
            }
            if (score >= beta) break;
        }
        return score;
    }

    public static long[] GetBestAlphaBetaMove(long[] board, boolean isPlayerOne, boolean isPlayerOneTurn, int depth, int alpha, int beta) {
//      TODO: Can you Parallelize this?
        System.out.println("Evaluate this Position: " + Evaluate.combinedEvaluate(board, isPlayerOne));
        long startTime = System.nanoTime();
        nodes = 0;
        short[] moveArray = new short[MAX_NUMBER_OF_MOVES * MAX_NUMBER_OF_ACTUAL_DEPTH];
        int actualDepth = 0;
        int bestScore = -Integer.MAX_VALUE;
        long[] bestBoard = null;
//        System.out.println("Board received:");
//        BitmapFianco.ShowBitBoard(board);
        int numberOfMoves = BitMapMoveGenerator.populateShortArrayWithAllPossibleMoves(board, isPlayerOne, moveArray, actualDepth);

        for (int i = 0; i < numberOfMoves; i++) {
            short move = moveArray[i];
//            System.out.println("Move from: " + MoveConversion.unpackFirstNumber(move));
//            System.out.println("Move to:" + MoveConversion.unpackSecondNumber(move));

            BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, move, isPlayerOneTurn);
            int value = -AlphaBeta(board, !isPlayerOneTurn, depth - 1, actualDepth + 1, moveArray, -beta, -alpha);
            if (value > bestScore) {
                bestScore = value;
                bestBoard = board.clone();
            }
            BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, move, isPlayerOneTurn);
            if (bestScore > alpha) {
                alpha = bestScore;
            }
            if (bestScore >= beta) break;
        }
//        System.out.println("Board generated:");
//        BitmapFianco.ShowBitBoard(bestBoard);
        long endTime = System.nanoTime();
        System.out.println("Nodes: " + nodes);
        System.out.println("Time: " + (endTime - startTime) / 1_000_000_000.0 + "s");
        System.out.println("Nodes per second:" + nodes / ((endTime - startTime) / 1_000_000_000.0));
        System.out.println("Score:" + bestScore);
        return bestBoard;
    }
}
