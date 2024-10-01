package FiancoGameEngine;

import java.util.ArrayList;
import java.util.Arrays;

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

    private ArrayList<int[][]> boardHistory = new ArrayList<>();
    private ArrayList<MoveCommand> moveCommands;


    public Fianco() {
        initializeBoardState();
        moveCommands = new ArrayList<>();
    }

    public void initializeBoardState() {
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
            boardHistory.add(copyBoardState(boardState));
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
        boardHistory.remove(boardHistory.size() - 1);
        Logger.getInstance().log("Undo:" + moveCommand.toString());
        return true;
    }

    public boolean checkForWin() {
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

    public boolean checkForDraw() {
        if (boardHistory.isEmpty()) {
            return false;
        }

        int[][] lastBoard = boardHistory.get(boardHistory.size() - 1); // Get the most recent board state
        int count = 0;

        for (int[][] board : boardHistory) {
            if (areBoardsEqual(board, lastBoard)) {
                count++;
            }
            if (count >= 3) {
                for (int[][] board2 : boardHistory) {
                    System.out.println(Arrays.deepToString(board2));
                }
                return true; // A draw condition is met
            }
        }

        return false; // No draw detected
    }

    // Helper method to compare two 2D arrays (board states)
    private boolean areBoardsEqual(int[][] board1, int[][] board2) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board1[i][j] != board2[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    // Helper method to copy the board state
    private int[][] copyBoardState(int[][] originalBoard) {
        int[][] copiedBoard = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(originalBoard[i], 0, copiedBoard[i], 0, BOARD_SIZE);
        }
        return copiedBoard;
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
