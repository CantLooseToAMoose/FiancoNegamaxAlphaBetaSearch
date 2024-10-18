package AI;

import BitBoard.BitMapMoveGenerator;
import BitBoard.BitmapFianco;
import BitBoard.MoveConversion;
import FiancoGameEngine.Fianco;
import FiancoGameEngine.MoveCommand;
import search.AlphaBeta.SequentialAlphaBetaSearch;

import java.util.ArrayList;

public class SequentialAlphaBetaSearchAgent implements IAgent {
    private BitmapFianco fianco;
    private final int player;
    private SequentialAlphaBetaSearch alphaBetaSearch;
    private long[] board;
    private ArrayList<MoveCommand> moveHistory = new ArrayList<>();

    @Override
    public void resetBoard(int[][] board) {
        fianco.populateBoardBitmapsFrom2DIntArray(board);
        long[] player1Board = fianco.getPlayer1Board();
        long[] player2Board = fianco.getPlayer2Board();
        this.board = new long[]{player1Board[0], player1Board[1], player2Board[0], player2Board[1]};
        alphaBetaSearch = new SequentialAlphaBetaSearch();
    }

    @Override
    public MoveCommand generateMove(MoveCommand move) {
        if (move != null) {
            short shortMove = MoveConversion.getShortMoveFromMoveCommand(move);
            BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, shortMove, !(player == 1));
            moveHistory.add(move);
        }
//        System.out.println(fianco);
        short newMove = alphaBetaSearch.GetBestAlphaBetaMove(this.board, player == 1, 9, -Integer.MAX_VALUE, Integer.MAX_VALUE);
//        System.out.println("Board after generated Move:");
//        BitmapFianco.ShowBitBoard(newBoard);
        BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, newMove, (player == 1));
        return MoveConversion.getMoveCommandFromShortMove(newMove, (player == 1));
    }

    @Override
    public void undoMove() {
        MoveCommand move = moveHistory.remove(moveHistory.size() - 1);
        BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, MoveConversion.getShortMoveFromMoveCommand(move), move.getPlayer() == 1);
    }

    public SequentialAlphaBetaSearchAgent(BitmapFianco fianco, int player) {
        this.fianco = fianco;
        this.player = player;
        this.alphaBetaSearch = new SequentialAlphaBetaSearch();
        fianco.populateBoardBitmapsFrom2DIntArray(new Fianco().getBoardState());
        long[] player1Board = fianco.getPlayer1Board();
        long[] player2Board = fianco.getPlayer2Board();
        board = new long[]{player1Board[0], player1Board[1], player2Board[0], player2Board[1]};

    }


}
