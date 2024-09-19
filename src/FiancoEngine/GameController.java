package FiancoEngine;

import FiancoEngine.BoardGUI;
import FiancoEngine.Fianco;
import FiancoEngine.Logger;
import ServerStructure.GameServer;
import ServerStructure.MessageLib;

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

    public synchronized void askForAiMoves() {
        if (!gameIsRunning) {
            return;
        }
        if (activePlayer == 1 && gameServer.isPlayer1Connected()) {
            String move = gameServer.getMoveFromPlayer(gameServer.getPlayer1Socket(), "player1");
            int[][] newBoardState = MessageLib.convertBoardStringToArray(move);
            if (!move(newBoardState)) {
                System.out.println("Stop Game after Wrong Move! (in GameController)");
                gameIsRunning = false;
                Logger.getInstance().log("AIPlayer1 tried invalid Move,try Again");
            }
        } else if (activePlayer == 2 && gameServer.isPlayer2Connected()) {
            String move = gameServer.getMoveFromPlayer(gameServer.getPlayer2Socket(), "player2");
            int[][] newBoardState = MessageLib.convertBoardStringToArray(move);
            if (!move(newBoardState)) {
                System.out.println("Stop Game after Wrong Move! (in GameController)");
                gameIsRunning = false;
                Logger.getInstance().log("AIPlayer 2 tried invalid Move, try Again");
            }
        }
    }

    public void continueGame() {
        gameIsRunning = true;
    }

    public void restartGame() {
        activePlayer = 1;
        System.out.println("Game started!");
        gameIsRunning = true;
        fianco.Restart();
    }


    public synchronized boolean move(int from_row, int from_col, int to_row, int to_col) {
        if (fianco.Move(new MoveCommand(from_row, from_col, to_row, to_col, activePlayer))) {
            moveMisc();
            return true;
        }
        return false;
    }

    public synchronized boolean move(int[][] newBoardState) {
        MoveCommand moveCommand = MoveCommand.CreateMoveCommandFromConsecutiveBoardStates(fianco.getBoardState(), newBoardState, activePlayer);
        if (moveCommand == null) {
            return false;
        }
        if (fianco.Move(moveCommand)) {
            moveMisc();
            return true;
        } else {
            System.out.println("Illegal Movecommand with Move: " + moveCommand);
            return false;
        }
    }

    private synchronized void moveMisc() {
        switchActivePlayer();
        gui.redrawBoard(getBoardState());
        if (isGameOver()) {
            gameIsRunning = false;
        }
    }

    public void undo() {

        if (fianco.Undo()) {// Only switch players when successfully undoing a move
            switchActivePlayer();
        }
        gameIsRunning = false;
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

    public synchronized int[][] getBoardState() {
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
