package AI;

import BitBoard.BitMapMoveGenerator;
import BitBoard.BitmapFianco;
import search.AlphaBetaSearch;
import search.MetaSearchHolder;

import java.util.LinkedList;
import java.util.Random;

public class SimpleAlphaBetaSearchAgent implements IAgent {
    private BitmapFianco fianco;
    private final int player;
    private AlphaBetaSearch alphaBetaSearch;


    @Override
    public void resetBoard(int[][] boardState) {
        fianco.populateBoardBitmapsFrom2DIntArray(boardState);
        alphaBetaSearch = new AlphaBetaSearch();
    }

    @Override
    public int[][] generateMove(int[][] boardState) {
        resetBoard(boardState);
        long[] player1Board = fianco.getPlayer1Board();
        long[] player2Board = fianco.getPlayer2Board();
        long[] board = new long[]{player1Board[0], player1Board[1], player2Board[0], player2Board[1]};
//        System.out.println(fianco);
        long[] newBoard = alphaBetaSearch.GetBestAlphaBetaMoveParallel(board, player == 1, 11, -Integer.MAX_VALUE, Integer.MAX_VALUE);
//        System.out.println("Board after generated Move:");
//        BitmapFianco.ShowBitBoard(newBoard);

        fianco.setPlayer1Board(new long[]{newBoard[0], newBoard[1]});
        fianco.setPlayer2Board(new long[]{newBoard[2], newBoard[3]});
        return fianco.convertBitmapTo2DIntArray();
    }

    public SimpleAlphaBetaSearchAgent(BitmapFianco fianco, int player) {
        this.fianco = fianco;
        this.player = player;
        this.alphaBetaSearch = new AlphaBetaSearch();
    }


}
