package AI;

import FiancoGameEngine.Fianco;
import FiancoGameEngine.MoveCommand;

import java.util.ArrayList;
import java.util.Random;

public class RandomFiancoAgent implements IAgent {
    private Fianco fianco;
    private final int player;


    @Override
    public void resetBoard(int[][] boardState) {
        fianco.setBoardState(boardState);
    }

    @Override
    public int[][] generateMove(int[][] boardState) {
        resetBoard(boardState);
        //Create All Possible Moves
        ArrayList<MoveCommand> possibleMoves = new ArrayList<>();
        ArrayList<int[]> pieces = fianco.getAllPiecePositionsForPlayer(player);
        for (int[] piece : pieces) {
            ArrayList<MoveCommand> possibleMovesToAdd = MoveCommand.CreateAllPossibleMoveCommandsFromPosition(piece, player, fianco);
            possibleMoves.addAll(possibleMovesToAdd);
        }
        //Choose a Random Move
        Random rand = new Random();
        int randomInt = rand.nextInt(possibleMoves.size());
        MoveCommand moveCommand = possibleMoves.get(randomInt);
        if (!fianco.Move(moveCommand)) {
            return generateMove(boardState);
        }
        return fianco.getBoardState();


    }

    public RandomFiancoAgent(Fianco fianco, int player) {
        this.fianco = fianco;
        this.player = player;
    }


}
