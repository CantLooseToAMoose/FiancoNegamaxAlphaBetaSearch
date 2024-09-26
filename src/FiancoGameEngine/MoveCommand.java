package FiancoGameEngine;

import BitBoard.MoveConversion;

import java.util.ArrayList;
import java.util.List;

public class MoveCommand {
    private static final char[] COLUMN_NAMES = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
    private int from_row;
    private int from_col;
    private int to_row;
    private int to_col;
    private int player;

    public MoveCommand(int from_row, int from_col, int to_row, int to_col, int player) {
        this.from_row = from_row;
        this.from_col = from_col;
        this.to_row = to_row;
        this.to_col = to_col;
        this.player = player;
    }

    public short toShort() {
        int from = from_row * 9 + from_col + 1;
        int to = to_row * 9 + to_col + 1;
        return MoveConversion.pack(from, to);
    }

    public boolean doMove(Fianco fianco) {
        //do nothing if the movement is not valid
        if (!checkIfMoveIsValid(fianco)) {
            return false;
        }
        int[][] boardState = fianco.getBoardState();

        // update the boardstate with the moving piece
        int movedPiece = boardState[from_row][from_col];
        boardState[from_row][from_col] = 0;
        boardState[to_row][to_col] = movedPiece;

        //if a piece was captured through diagonal movement remove the captured piece
        int delta_row = to_row - from_row;
        int delta_col = to_col - from_col;
        if (Math.abs(delta_col) > 1) {
            boardState[from_row + delta_row / 2][from_col + delta_col / 2] = 0;
        }
        fianco.setBoardState(boardState);
        return true;
    }

    public void undoMove(Fianco fianco) {

        int[][] boardState = fianco.getBoardState();

        // update the boardstate by moving the piece backwards
        int movedPiece = boardState[to_row][to_col];
        boardState[from_row][from_col] = movedPiece;
        boardState[to_row][to_col] = 0;

        //if a piece was captured through diagonal movement replace the captured piece
        int delta_row = to_row - from_row;
        int delta_col = to_col - from_col;
        if (delta_col > 1) {
            if (player == 1) {
                boardState[from_row + delta_row / 2][from_col + delta_col / 2] = 2;
            } else {
                boardState[from_row + delta_row / 2][from_col + delta_col / 2] = 2;
            }

        }
        fianco.setBoardState(boardState);
    }

    private boolean checkIfMoveIsValid(Fianco fianco) {
        // Is the Move inside the Board
        if (!checkIfIndexIsInBounds(from_row) || !checkIfIndexIsInBounds(from_col) || !checkIfIndexIsInBounds(to_row) || !checkIfIndexIsInBounds(to_col)) {
            Logger.getInstance().log("Movecommand out of bounds");
            return false;
        }
        // Does the Move Land on another Piece
        if (fianco.getBoardState()[to_row][to_col] != 0) {
            Logger.getInstance().log("Movecommand lands on another piece");
            return false;
        }
        //Check if Moving Piece is from same Player
        if (fianco.getBoardState()[from_row][from_col] != player) {
            Logger.getInstance().log("Movecommand tries to move piece from the other player");
            return false;
        }
        //Is the move from a valid move Set?
        // Regular Moves
        boolean moveToTheLeft = (to_row == from_row && from_col - 1 == to_col);
        boolean moveToTheRight = (to_row == from_row && from_col + 1 == to_col);
        boolean moveUpwards = (player == 1 && from_col == to_col && from_row + 1 == to_row);
        boolean moveDownwards = (player == 2 && from_col == to_col && from_row - 1 == to_row);
        //Captures
        boolean captureTopLeft = (player == 1 && to_row == from_row + 2 && from_col - 2 == to_col);
        boolean captureTopRight = (player == 1 && to_row == from_row + 2 && from_col + 2 == to_col);
        boolean captureBottomLeft = (player == 2 && to_row == from_row - 2 && from_col - 2 == to_col);
        boolean captureBottomRight = (player == 2 && to_row == from_row - 2 && from_col + 2 == to_col);

        // if not part of the valid move set return false
        if (!(moveToTheLeft || moveToTheRight || moveUpwards || moveDownwards || captureTopLeft || captureTopRight || captureBottomLeft || captureBottomRight)) {
            Logger.getInstance().log("Movecommand is not part of any valid move set");
            return false;
        }

        //if capture check if capture is valid
        boolean[] captures = {captureBottomLeft, captureBottomRight, captureTopLeft, captureTopRight};
        int[][] directions = {
                {-1, -1},  // Bottom-left
                {-1, +1},  // Bottom-right
                {+1, -1},  // Top-left
                {+1, +1}   // Top-right
        };
        for (int i = 0; i < captures.length; i++) {
            if (captures[i]) {
                if (!canCaptureInThisDirection(from_row, from_col, directions[i], player, fianco)) {
                    Logger.getInstance().log("Movecommand tries to capture but cant");
                    return false;
                }
            }
        }

        // if move check if you had to capture somewhere instead
        if (moveToTheLeft || moveToTheRight || moveUpwards || moveDownwards) {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (!(fianco.getBoardState()[i][j] == player)) {
                        continue;
                    }
                    if (canCapture(i, j, player, fianco)) {
                        Logger.getInstance().log("Movecommand tries to move but has to capture at: (" + i + "," + j + ")");
                        return false;
                    }
                }
            }
        }
        return true;

    }

    public static boolean canCapture(int row, int col, int player, Fianco fianco) {
        int[][] directions = {
                {-1, -1},  // Bottom-left
                {-1, +1},  // Bottom-right
                {+1, -1},  // Top-left
                {+1, +1}   // Top-right
        };

        // Loop over all possible diagonal directions depending on which player
        for (int i = 0; i < 4; i++) {
            if (player == 1 && (i == 0 || i == 1)) {
                continue;
            } else if (player == 2 && (i == 2 || i == 3)) {
                continue;
            }
            int[] direction = directions[i];
            if (canCaptureInThisDirection(row, col, direction, player, fianco)) {
                return true;
            }
        }

        return false;  // No capture is possible
    }

    private static boolean canCaptureInThisDirection(int row, int col, int[] direction, int player, Fianco fianco) {
        int di = direction[0];
        int dj = direction[1];

        int captureRow = row + di;
        int captureCol = col + dj;
        int landingRow = row + 2 * di;
        int landingCol = col + 2 * dj;
        int[][] board = fianco.getBoardState();
        // Check if the capture and landing positions are within the board bounds
        if (checkIfIndexIsInBounds(captureRow) && checkIfIndexIsInBounds(captureCol) && checkIfIndexIsInBounds(landingRow) && checkIfIndexIsInBounds(landingCol)) {
            // Check if there's an opponent piece diagonally adjacent and the next square is empty
            if (board[captureRow][captureCol] != 0 && board[captureRow][captureCol] != player && board[landingRow][landingCol] == 0) {
                return true;  // A capture is possible
            }
        }
        return false;
    }

    private static boolean checkIfIndexIsInBounds(int index) {
        return (index >= 0 && index < 9);
    }


    public static MoveCommand CreateMoveCommandFromConsecutiveBoardStates(int[][] prevBoardState, int[][] nextBoardState, int player) {
        int[][] deltaBoardState = SubtractBoardStates(nextBoardState, prevBoardState);
        List<int[]> nonZeros = findNonZeroElements(deltaBoardState);
        if (nonZeros.isEmpty()) { //There was no Movement
            System.out.println("Error: Both Boardstates are the same");
            return null;
        } else if (nonZeros.size() == 2) { //There was no capture involved so only Movement
            int[] deltaOne = nonZeros.get(0);
            int[] deltaTwo = nonZeros.get(1);
            return CreateCorrectMovecommandDirection(deltaOne, deltaTwo, player);
        } else if (nonZeros.size() == 3) { // There was a capture involved
            List<int[]> nonZeros2 = findElementsWithRowDifferenceOfTwo(nonZeros);
            if (nonZeros2 == null) {
                System.out.println("Did not find a fitting pair of boardstate changes for valid movement");
                return null;
            }
            int[] deltaOne = nonZeros2.get(0);
            int[] deltaTwo = nonZeros2.get(1);
            return CreateCorrectMovecommandDirection(deltaOne, deltaTwo, player);
        } else {
            System.out.println("Error: There should not be more than 3 different Elements between Consecutive Boardstates");
            return null;
        }
    }

    public static ArrayList<MoveCommand> CreateAllPossibleMoveCommandsFromPosition(int[] from, int player, Fianco fianco) {
        ArrayList<MoveCommand> moveCommands = new ArrayList<>();
        if (player == 1) {
            //Move
            MoveCommand left = new MoveCommand(from[0], from[1], from[0], from[1] - 1, player);
            MoveCommand right = new MoveCommand(from[0], from[1], from[0], from[1] + 1, player);
            MoveCommand up = new MoveCommand(from[0], from[1], from[0] + 1, from[1], player);
            //Capture
            MoveCommand top_left = new MoveCommand(from[0], from[1], from[0] + 2, from[1] - 2, player);
            MoveCommand top_right = new MoveCommand(from[0], from[1], from[0] + 2, from[1] + 2, player);
            if (left.checkIfMoveIsValid(fianco)) {
                moveCommands.add(left);
            }
            if (right.checkIfMoveIsValid(fianco)) {
                moveCommands.add(right);
            }
            if (up.checkIfMoveIsValid(fianco)) {
                moveCommands.add(up);
            }
            if (top_left.checkIfMoveIsValid(fianco)) {
                moveCommands.add(top_left);
            }
            if (top_right.checkIfMoveIsValid(fianco)) {
                moveCommands.add(top_right);
            }


        } else if (player == 2) {
            //Move
            MoveCommand left = new MoveCommand(from[0], from[1], from[0], from[1] - 1, player);
            MoveCommand right = new MoveCommand(from[0], from[1], from[0], from[1] + 1, player);
            MoveCommand down = new MoveCommand(from[0], from[1], from[0] - 1, from[1], player);
            //Capture
            MoveCommand bottom_left = new MoveCommand(from[0], from[1], from[0] - 2, from[1] - 2, player);
            MoveCommand bottom_right = new MoveCommand(from[0], from[1], from[0] - 2, from[1] + 2, player);
            if (left.checkIfMoveIsValid(fianco)) {
                moveCommands.add(left);
            }
            if (right.checkIfMoveIsValid(fianco)) {
                moveCommands.add(right);
            }
            if (down.checkIfMoveIsValid(fianco)) {
                moveCommands.add(down);
            }
            if (bottom_left.checkIfMoveIsValid(fianco)) {
                moveCommands.add(bottom_left);
            }
            if (bottom_right.checkIfMoveIsValid(fianco)) {
                moveCommands.add(bottom_right);
            }

        }
        return moveCommands;
    }


    private static int[][] SubtractBoardStates(int[][] A, int[][] B) {
        int i, j;
        int[][] C = new int[9][9];

        for (i = 0; i < 9; i++)
            for (j = 0; j < 9; j++)
                C[i][j] = A[i][j] - B[i][j];

        return C;
    }

    private static List<int[]> findNonZeroElements(int[][] arr) {
        List<int[]> nonZeroElements = new ArrayList<>();

        for (int i = 0; i < arr.length; i++) {  // Loop over rows
            for (int j = 0; j < arr[i].length; j++) {  // Loop over columns
                if (arr[i][j] != 0) {  // Check if the element is not equal to 0
                    nonZeroElements.add(new int[]{arr[i][j], i, j});  // Add {value, row_index, col_index} to the list
                }
            }
        }

        return nonZeroElements;
    }

    private static List<int[]> findElementsWithRowDifferenceOfTwo(List<int[]> elements) {
        // Check if the list size is exactly 3
        if (elements.size() == 3) {
            for (int i = 0; i < elements.size(); i++) {
                for (int j = i + 1; j < elements.size(); j++) {
                    int rowDiff = Math.abs(elements.get(i)[1] - elements.get(j)[1]); // Difference of row indices
                    if (rowDiff == 2) {
                        // Return the two elements as a list of arrays
                        List<int[]> result = new ArrayList<>();
                        result.add(elements.get(i));
                        result.add(elements.get(j));
                        return result;
                    }
                }
            }
        }
        return null;  // Return null if no elements have a row difference of 2
    }

    private static MoveCommand CreateCorrectMovecommandDirection(int[] deltaOne, int[] deltaTwo, int player) {

        if (deltaOne[0] < deltaTwo[0]) { // Check from where the Move was made
            return new MoveCommand(deltaOne[1], deltaOne[2], deltaTwo[1], deltaTwo[2], player);
        } else {
            return new MoveCommand(deltaTwo[1], deltaTwo[2], deltaOne[1], deltaOne[2], player);
        }
    }

    @Override
    public String toString() {
        return "Player " + player + ": (" + COLUMN_NAMES[from_col] + (from_row + 1) + ")->(" + COLUMN_NAMES[to_col] + (to_row + 1) + ")";
    }

}