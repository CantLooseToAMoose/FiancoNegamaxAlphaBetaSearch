package Fianco.GameEngine;

import Fianco.ServerStructure.GameServer;

import javax.swing.*;

public class GameEngine {
    public static void main(String[] args) throws Exception {

        Fianco fianco = new Fianco();
        GameController controller = new GameController(fianco);
        GameServer gameServer = new GameServer(12345, controller);
        SwingUtilities.invokeLater(() -> {
            BoardGUI app = new BoardGUI(controller, fianco.getBoardState());
            controller.addGUI(app);
            app.setVisible(true);
        });
        gameServer.start();
    }
}