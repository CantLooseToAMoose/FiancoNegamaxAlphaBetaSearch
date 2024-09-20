package ServerStructure;

import FiancoGameEngine.Fianco;
import FiancoGameEngine.GameController;

import java.io.*;
import java.net.*;

public class GameServer {
    private ServerSocket serverSocket;

    private Socket player1Socket;
    private Socket player2Socket;

    private GameController controller;
    private boolean player1Connected = false;
    private boolean player2Connected = false;

    public GameServer(int port, GameController controller) throws IOException {
        this.controller = controller;
        serverSocket = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"));

    }

    public void start() {
        System.out.println("Waiting for AI clients...");
        controller.AddGameServer(this);
        new Thread(this::acceptClients).start();
        new Thread(this::askForClientMoves).start();
    }

    public void askForClientMoves() {
        while (true) {
//            System.out.println("Ask for AI");
            controller.askForAiMoves();
        }
    }

    private void acceptClients() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String playerId = in.readLine(); // Assume first message from client is their player ID

                if (playerId.equals("player1") && !player1Connected) {
                    player1Socket = clientSocket;
                    player1Connected = true;
                    controller.AIPlayerJoined(1);
                    System.out.println("Player 1 connected.");
                } else if (playerId.equals("player2") && !player2Connected) {
                    player2Socket = clientSocket;
                    controller.AIPlayerJoined(2);
                    player2Connected = true;
                    System.out.println("Player 2 connected.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized String getMoveFromPlayer(Socket playerSocket, String playerId) {
//        System.out.println("getMoveFromPlayer called for: " + playerId);
        if (playerSocket != null && !playerSocket.isClosed()) {
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
                String boardState = MessageLib.convertBoardArrayToString(controller.getBoardState());
//                System.out.println("Send Boardstate" + boardState + "  to Player: " + playerId);
                out.write("Your move:" + boardState + "\n");
                out.flush();

                BufferedReader in = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
//                System.out.println("Waiting for move from Player: " + playerId);
                String answer = in.readLine();
//                System.out.println("Received " + answer + " from Player: " + playerId);
                return answer; // Wait for the client's move
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // If no client connected or failed to respond, return no move and make sure to close the connection for possible reconnection
        System.out.println(playerId + " not connected, making no move.");
        if (playerId.equals("player1")) {
            player1Connected = false;
            try {
                player1Socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (playerId.equals("player2")) {
            player2Connected = false;
            try {
                player2Socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return MessageLib.convertBoardArrayToString(controller.getBoardState());
    }

    public Socket getPlayer1Socket() {
        return player1Socket;
    }

    public Socket getPlayer2Socket() {
        return player2Socket;
    }

    public boolean isPlayer1Connected() {
        return player1Connected;
    }

    public boolean isPlayer2Connected() {
        return player2Connected;
    }


    public static void main(String[] args) throws IOException {
        GameServer server = new GameServer(12345, new GameController(new Fianco())); // Port 12345
        server.start();
    }
}
