package search;

import BitBoard.BitMapMoveGenerator;

import java.util.LinkedList;

public class AlphaBetaSearch {
//    Todo: Check for repetetive Movements
    public static int nodes = 0;

    public static int AlphaBeta(long[] board, boolean isPlayerOne, boolean isPlayerOneTurn, int depth, int alpha, int beta) {
        nodes++;
        LinkedList<long[]> succesorMoves = BitMapMoveGenerator.fasterCreateQueueWithAllPossibleMoves(board, isPlayerOneTurn);
        int win = Evaluate.evaluateWin(board, isPlayerOneTurn);
        if (win != 0) {
            return (300 + depth) * win;
        }
        if (succesorMoves.isEmpty() || depth == 0) {
            return Evaluate.combinedEvaluate(board, isPlayerOneTurn);
        }
        int score = -Integer.MAX_VALUE;
        while (!succesorMoves.isEmpty()) {
            long[] move = succesorMoves.remove();
            long[] newBoard = BitMapMoveGenerator.createNewBoardStateFromMove(board, move, isPlayerOneTurn);
            int value = -AlphaBeta(newBoard, isPlayerOne, !isPlayerOneTurn, depth - 1, -beta, -alpha);

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
        int bestScore = -Integer.MAX_VALUE;
        long[] bestBoard = null;
        LinkedList<long[]> succesorMoves = BitMapMoveGenerator.fasterCreateQueueWithAllPossibleMoves(board, isPlayerOneTurn);
        while (!succesorMoves.isEmpty()) {
            long[] move = succesorMoves.remove();
            long[] newBoard = BitMapMoveGenerator.createNewBoardStateFromMove(board, move, isPlayerOneTurn);
            int value = -AlphaBeta(newBoard, isPlayerOne, !isPlayerOneTurn, depth - 1, -beta, -alpha);
            if (value > bestScore) {
                bestScore = value;
                bestBoard = newBoard;
            }
        }
        long endTime = System.nanoTime();
        System.out.println("Nodes: " + nodes);
        System.out.println("Time: " + (endTime - startTime) / 1_000_000_000.0 + "s");
        System.out.println("Nodes per second:" + nodes / ((endTime - startTime) / 1_000_000_000.0));
        System.out.println("Score:" + bestScore);
        return bestBoard;
    }
}
