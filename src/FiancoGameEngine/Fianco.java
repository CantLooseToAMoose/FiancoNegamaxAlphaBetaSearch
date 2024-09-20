package FiancoGameEngine;

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
            Logger.getInstance().log("Move:" + moveCommand.toString());
            return true;
        }
        return false;
    }

    public boolean Undo() {
        if (moveCommands.isEmpty()) {
            return false;
        }
        MoveCommand moveCommand = moveCommands.remove(moveCommands.size() - 1);
        moveCommand.undoMove(this);
        Logger.getInstance().log("Undo:" + moveCommand.toString());
        return true;
    }

    public boolean checkForGameOver() {
        for (int i = 0; i < 9; i++) {
            if (boardState[0][i] == 2) {
                return true;
            }
            if (boardState[8][i] == 1) {
                return true;
            }
        }
        boolean thereIsAPlayer1Piece = false;
        boolean thereIsAPlayer2Piece = false;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (boardState[i][j] == 1) {
                    thereIsAPlayer1Piece = true;
                }
                if (boardState[i][j] == 2) {
                    thereIsAPlayer2Piece = true;
                }
                if (thereIsAPlayer1Piece && thereIsAPlayer2Piece) {
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<int[]> getAllPiecePositionsForPlayer(int player) {

        ArrayList<int[]> positions = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (boardState[i][j] == player) {
                    positions.add(new int[]{i, j});
                }
            }
        }
        return positions;
    }

    public ArrayList<MoveCommand> getMoveCommands() {
        return moveCommands;
    }

}
