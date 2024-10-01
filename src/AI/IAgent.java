package AI;

import FiancoGameEngine.MoveCommand;

public interface IAgent {

    public void resetBoard();

    public MoveCommand generateMove(MoveCommand move);

    public void undoMove();

}
