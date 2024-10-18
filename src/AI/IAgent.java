package AI;

import FiancoGameEngine.MoveCommand;

public interface IAgent {

    public void resetBoard(int[][] board);

    public MoveCommand generateMove(MoveCommand move);

    public void undoMove();

}
