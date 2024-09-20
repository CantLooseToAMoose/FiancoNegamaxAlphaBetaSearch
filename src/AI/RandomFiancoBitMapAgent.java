package AI;

import BitBoard.BitMapMoveGenerator;
import BitBoard.BitmapFianco;

import java.util.LinkedList;
import java.util.Random;

public class RandomFiancoBitMapAgent implements IAgent {
    private BitmapFianco fianco;
    private final int player;


    @Override
    public void resetBoard(int[][] boardState) {
        fianco.populateBoardBitmapsFrom2DIntArray(boardState);
    }

    @Override
    public int[][] generateMove(int[][] boardState) {
        resetBoard(boardState);
        //Create All Possible Moves
        long[] player1Board = fianco.getPlayer1Board();
        long[] player2Board = fianco.getPlayer2Board();
        long[] board = new long[]{player1Board[0], player1Board[1], player2Board[0], player2Board[1]};
        LinkedList<long[]> moves = BitMapMoveGenerator.populateQueueWithAllPossibleMoves(board, player == 1);
//        System.out.println(fianco);
        // Step 1: Get the size of the list
        int size = moves.size();

        // Step 2: Generate a random index between 0 and size-1
        Random random = new Random();
        int randomIndex = random.nextInt(size);

        // Step 3: Return the element at the randomly generated index
        long[] randomMove = moves.get(randomIndex);

        long[] newBoard = BitMapMoveGenerator.updateBoardStateFromMove(board, randomMove, player == 1);
//        System.out.println("Board after generated Move:");
//        BitmapFianco.ShowBitBoard(newBoard);

        fianco.setPlayer1Board(new long[]{newBoard[0], newBoard[1]});
        fianco.setPlayer2Board(new long[]{newBoard[2], newBoard[3]});
        return fianco.convertBitmapTo2DIntArray();

    }

    public RandomFiancoBitMapAgent(BitmapFianco fianco, int player) {
        this.fianco = fianco;
        this.player = player;
    }


}
