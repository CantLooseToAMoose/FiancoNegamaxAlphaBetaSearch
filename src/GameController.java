public class GameController {
    private Fianco fianco;
    private BoardGUI gui;
    private int activePlayer;
    private boolean gameIsRunning;

    // Run Game with active Server
    private GameServer gameServer;

    // If undo AI Move wait a brief moment before requesting another move
    private float aiTimeOut;

    public GameController(Fianco fianco) {
        this.fianco = fianco;
    }

    public void AddGameServer(GameServer gameServer) {
        this.gameServer = gameServer;
    }

    public void addGUI(BoardGUI gui) {
        this.gui = gui;
    }

    public void gameLoop() {
        if (activePlayer == 1 && gameServer.isPlayer1Connected()) {
            String move = gameServer.getMoveFromPlayer(gameServer.getPlayer1Socket(), "player1");
            int[][] newBoardState = MessageLib.convertBoardStringToArray(move);
            if (!move(newBoardState)) {
                Logger.getInstance().log("AIPlayer1 tried invalid Move,try Again");
                gameLoop();
            }
        } else if (activePlayer == 2 && gameServer.isPlayer2Connected()) {
            String move = gameServer.getMoveFromPlayer(gameServer.getPlayer2Socket(), "player2");
            int[][] newBoardState = MessageLib.convertBoardStringToArray(move);
            if (!move(newBoardState)) {
                Logger.getInstance().log("AIPlayer 2 tried invalid Move, try Again");
                gameLoop();
            }
        }
    }

    public void continueGame() {
        gameIsRunning = true;
        gameLoop();
    }

    public void restartGame() {
        activePlayer = 1;
        gameIsRunning = true;
        fianco.Restart();
        gameLoop();
    }


    public boolean move(int from_row, int from_col, int to_row, int to_col) {
        if (fianco.Move(new MoveCommand(from_row, from_col, to_row, to_col, activePlayer))) {
            switchActivePlayer();
            gameLoop();
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
            gameLoop();
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

    public void AIPlayerJoined(int aiPlayer) {
        if (aiPlayer == 1) {
            gui.setPlayerOneTypeLabel(true);
        } else if (aiPlayer == 2) {
            gui.setPlayerTwoTypeLabel(true);
        }

    }

}
