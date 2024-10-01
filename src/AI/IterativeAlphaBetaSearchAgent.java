package AI;

import BitBoard.BitMapMoveGenerator;
import BitBoard.BitmapFianco;
import BitBoard.MoveConversion;
import FiancoGameEngine.Fianco;
import FiancoGameEngine.MoveCommand;
import search.AlphaBetaSearch;

import java.util.ArrayList;

public class IterativeAlphaBetaSearchAgent implements IAgent {
    private BitmapFianco fianco;
    private final int player;
    private AlphaBetaSearch alphaBetaSearch;
    private long[] board;
    private ArrayList<MoveCommand> moveHistory = new ArrayList<>();

    @Override
    public void resetBoard() {
        fianco.populateBoardBitmapsFrom2DIntArray(new Fianco().getBoardState());
        long[] player1Board = fianco.getPlayer1Board();
        long[] player2Board = fianco.getPlayer2Board();
        board = new long[]{player1Board[0], player1Board[1], player2Board[0], player2Board[1]};
        alphaBetaSearch = new AlphaBetaSearch();
    }

    @Override
    public MoveCommand generateMove(MoveCommand move) {
        if (move != null) {
            short shortMove = MoveConversion.getShortMoveFromMoveCommand(move);
            BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, shortMove, !(player == 1));
            moveHistory.add(move);
        }
//        System.out.println(fianco);
        short newMove = alphaBetaSearch.GetBestMoveIterativeDeepening(this.board, player == 1, 30, -Integer.MAX_VALUE, Integer.MAX_VALUE, 15_000_000_000L);
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

    public IterativeAlphaBetaSearchAgent(BitmapFianco fianco, int player) {
        this.fianco = fianco;
        this.player = player;
        this.alphaBetaSearch = new AlphaBetaSearch();
        fianco.populateBoardBitmapsFrom2DIntArray(new Fianco().getBoardState());
        long[] player1Board = fianco.getPlayer1Board();
        long[] player2Board = fianco.getPlayer2Board();
        board = new long[]{player1Board[0], player1Board[1], player2Board[0], player2Board[1]};

    }


}
