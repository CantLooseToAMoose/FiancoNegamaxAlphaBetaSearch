import java.util.ArrayList;

public class Fianco {
    private static final int BOARD_SIZE = 9;  // 9x9 board

    public int[][] getBoardState() {
        return boardState;
    }

    public void setBoardState(int[][] boardState) {
        this.boardState = boardState;
    }

    // 9x9 board state: 0 = empty, 1 = white piece, 2 = black piece
    private int[][] boardState = new int[BOARD_SIZE][BOARD_SIZE];

    private ArrayList<MoveCommand> moveCommands;


    public Fianco() {
        initializeBoardState();
        moveCommands = new ArrayList<>();
    }

    private void initializeBoardState() {
        boardState = new int[][]{
                {1, 1, 1, 1, 1, 1, 1, 1, 1},
                {0, 1, 0, 0, 0, 0, 0, 1, 0},
                {0, 0, 1, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 1, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 2, 0, 2, 0, 0, 0},
                {0, 0, 2, 0, 0, 0, 2, 0, 0},
                {0, 2, 0, 0, 0, 0, 0, 2, 0},
                {2, 2, 2, 2, 2, 2, 2, 2, 2}};
    }

    public void Restart() {
        initializeBoardState();
        moveCommands = new ArrayList<>();
    }

    public boolean Move(MoveCommand moveCommand) {

        if (moveCommand.doMove(this)) {
            moveCommands.add(moveCommand);
            return true;
        }
        return false;
    }

    public void Undo() {
        MoveCommand moveCommand = moveCommands.remove(-1);
        moveCommand.undoMove(this);
    }

    public boolean checkGameOver() {
        for (int i = 0; i < 9; i++) {
            if (boardState[0][i] == 2) {
                return true;
            }
            if (boardState[8][i] == 1) {
                return true;
            }
        }
        return false;
    }


}
