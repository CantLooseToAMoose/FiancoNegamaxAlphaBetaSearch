public class GameController {
    private Fianco fianco;
    private BoardGUI gui;
    private int activePlayer;
    private boolean gameIsRunning;

    public GameController(Fianco fianco) {
        this.fianco = fianco;
    }

    public void startGame() {
        activePlayer = 1;
        gameIsRunning = true;
        fianco.Restart();
    }

    public boolean move(int from_row, int from_col, int to_row, int to_col) {
        if (fianco.Move(new MoveCommand(from_row, from_col, to_row, to_col, activePlayer))) {
            switchActivePlayer();
            return true;
        }
        return false;
    }

    public boolean move(int[][] newBoardState) {
        MoveCommand moveCommand = MoveCommand.CreateMoveCommandFromConsecutiveBoardStates(fianco.getBoardState(), newBoardState, activePlayer);
        if (moveCommand == null) {
            return false;
        }
        if (fianco.Move(moveCommand)) {
            switchActivePlayer();
            return true;
        }
        return false;
    }

    public void undo() {

        if (fianco.Undo()) {// Only switch players when successfully undoing a move
            switchActivePlayer();
        }
    }

    public int getActivePlayer() {
        return activePlayer;
    }

    public boolean gameIsRunning() {
        return gameIsRunning;
    }

    private boolean isGameOver() {
        return fianco.checkForGameOver();
    }

    private void switchActivePlayer() {
        if (activePlayer == 1) {
            activePlayer = 2;
        } else {
            activePlayer = 1;
        }
    }

    public int[][] getBoardState() {
        return fianco.getBoardState();
    }

}
