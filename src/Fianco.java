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
        moveCommands.add(moveCommand);
        moveCommand.DoMove(this);
        return true;
//        TODO: check for legal move
    }

    public boolean Undo() {
        MoveCommand moveCommand = moveCommands.remove(-1);
        moveCommand.UndoMove(this);
        return true;
//        TODO: check if everything worked e.g. there is a piece at the position etc.
    }


    public static class MoveCommand {
        private int from_row;
        private int from_col;
        private int to_row;
        private int to_col;

        public MoveCommand(int from_row, int from_col, int to_row, int to_col) {
            this.from_row = from_row;
            this.from_col = from_col;
            this.to_row = to_row;
            this.to_col = to_col;
        }

        public void DoMove(Fianco fianco) {
            System.out.println("DoMove not implemented yet");
        }

        public void UndoMove(Fianco fianco) {
            System.out.println("UndoMove not implemented yet");

        }

    }
}
