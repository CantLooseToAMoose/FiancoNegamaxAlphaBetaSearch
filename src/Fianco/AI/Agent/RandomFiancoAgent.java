package Fianco.AI.Agent;

import Fianco.GameEngine.Fianco;
import Fianco.GameEngine.MoveCommand;

import java.util.ArrayList;
import java.util.Random;

public class RandomFiancoAgent implements IAgent {
    private Fianco fianco;
    private final int player;


    @Override
    public void resetBoard(int[][] board) {
        fianco.initializeBoardState();
    }

    @Override
    public MoveCommand generateMove(MoveCommand move) {
        //Create All Possible Moves
        if (move != null) {
            fianco.Move(move);
        }
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
            return generateMove(move);
        }
        return moveCommand;


    }

    @Override
    public void undoMove() {
        fianco.Undo();
    }

    public RandomFiancoAgent(Fianco fianco, int player) {
        this.fianco = fianco;
        this.player = player;
    }


}
