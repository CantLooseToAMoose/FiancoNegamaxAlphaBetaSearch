package FiancoGameEngine;

import ServerStructure.GameServer;
import ServerStructure.MessageLib;

import javax.swing.*;
import java.util.ArrayList;

public class GameController {
    private Fianco fianco;
    private BoardGUI gui;
    private int activePlayer;
    private boolean gameIsRunning;

    // Run Game with active Server
    private GameServer gameServer;
    private boolean calledUndo = false;
    private boolean calledRestart = false;


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
        if (calledUndo) {
            if (activePlayer == 1 && gameServer.isPlayer1Connected()) {
                gameServer.callUndoOnAiPlayer(gameServer.getPlayer1Socket(), "player1");
            } else if (activePlayer == 2 && gameServer.isPlayer2Connected()) {
                gameServer.callUndoOnAiPlayer(gameServer.getPlayer2Socket(), "player2");
            }
            calledUndo = false;
            return;
        }
        if (calledRestart) {
            if (gameServer.isPlayer1Connected()) {
                gameServer.callRestartOnAiPlayer(gameServer.getPlayer1Socket(), "player1");
            }
            if (gameServer.isPlayer2Connected()) {
                gameServer.callRestartOnAiPlayer(gameServer.getPlayer2Socket(), "player2");
            }
            calledRestart = false;
            return;
        }
        if (!gameIsRunning) {
            return;
        }
        if (activePlayer == 1 && gameServer.isPlayer1Connected()) {
            String move = gameServer.getMoveFromPlayer(gameServer.getPlayer1Socket(), "player1");
            MoveCommand moveCommand = MessageLib.convertMoveCommandStringToMoveCommand(move);
            if (!move(moveCommand)) {
                System.out.println("Stop Game after Wrong Move! (in GameController)");
                gameIsRunning = false;
                Logger.getInstance().log("AIPlayer1 tried invalid Move,try Again");
            }
        } else if (activePlayer == 2 && gameServer.isPlayer2Connected()) {
            String move = gameServer.getMoveFromPlayer(gameServer.getPlayer2Socket(), "player2");
            MoveCommand moveCommand = MessageLib.convertMoveCommandStringToMoveCommand(move);
            if (!move(moveCommand)) {
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
        calledRestart = true;
    }


    public synchronized boolean move(int from_row, int from_col, int to_row, int to_col) {
        if (fianco.Move(new MoveCommand(from_row, from_col, to_row, to_col, activePlayer))) {
            moveMisc();
            return true;
        }
        return false;
    }

    public synchronized boolean move(MoveCommand moveCommand) {
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
        SwingUtilities.invokeLater(() -> gui.redrawBoard(getBoardState()));
        if (someOneWon()) {
            gameIsRunning = false;
            Logger.getInstance().log("Game over. Player " + activePlayer + " won the game! \n The game lasted: " + fianco.getMoveCommands().size() + " moves.");
        }
        if (isDraw()) {
            gameIsRunning = false;
            Logger.getInstance().log("Game over. It is a draw!");
        }
        switchActivePlayer();
    }

    public void undo() {

        if (fianco.Undo()) {//
            gui.redrawBoard(getBoardState());// Only switch players when successfully undoing a move
            switchActivePlayer();
            this.calledUndo = true;
        }
        gameIsRunning = false;
    }

    public int getActivePlayer() {
        return activePlayer;
    }

    public boolean gameIsRunning() {
        return gameIsRunning;
    }

    private boolean someOneWon() {
        return fianco.checkForWin();
    }

    private boolean isDraw() {
        return fianco.checkForDraw();
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

    public synchronized MoveCommand getLastMoveCommand() {
        ArrayList<MoveCommand> moveCommands = fianco.getMoveCommands();
        if (moveCommands.isEmpty()) {
            return null;
        }
        return moveCommands.get(moveCommands.size() - 1);
    }

    public void AIPlayerJoined(int aiPlayer) {
        if (aiPlayer == 1) {
            gui.setPlayerOneTypeLabel(true);
        } else if (aiPlayer == 2) {
            gui.setPlayerTwoTypeLabel(true);
        }
    }

}
