package AI.Search.Evaluation;

public class Quiescence {

    public static boolean isQuiet(long[] board) {
        int lowestNumberOfBlocksPlayer1 = BlockPieceEvaluation.LowestNumberOfEnemyBlocksForAllPieces(board, true);
        int lowestNumberOfBlocksPlayer2 = BlockPieceEvaluation.LowestNumberOfEnemyBlocksForAllPieces(board, false);
        return !(lowestNumberOfBlocksPlayer1 == 0 || lowestNumberOfBlocksPlayer2 == 0);
    }
}
