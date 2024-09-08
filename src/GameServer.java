import java.io.*;
import java.net.*;

public class GameServer {
    private ServerSocket serverSocket;
    private Socket player1Socket;
    private Socket player2Socket;

    private GameController controller; // Your game board logic goes here
    private boolean player1Connected = false;
    private boolean player2Connected = false;

    public GameServer(int port, GameController controller) throws IOException {
        serverSocket = new ServerSocket(port);

    }

    public void start() {
        System.out.println("Waiting for AI clients...");
        new Thread(this::acceptClients).start();
        gameLoop();
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
                    System.out.println("Player 1 connected.");
                } else if (playerId.equals("player2") && !player2Connected) {
                    player2Socket = clientSocket;
                    player2Connected = true;
                    System.out.println("Player 2 connected.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void gameLoop() {
        while (!controller.gameIsRunning()) {
            if (controller.getActivePlayer() == 1 && player1Connected) {
                String move = getMoveFromPlayer(player1Socket, "player1");
                int[][] newBoardState = MessageLib.convertBoardStringToArray(move);
                controller.move(newBoardState);
            } else if (controller.getActivePlayer() == 2 && player2Connected) {
                String move = getMoveFromPlayer(player2Socket, "player2");
                int[][] newBoardState = MessageLib.convertBoardStringToArray(move);
                controller.move(newBoardState);
            }
            // Add game logic like checking if the game is over, switching turns, etc.
        }
    }

    private String getMoveFromPlayer(Socket playerSocket, String playerId) {
        if (playerSocket != null && !playerSocket.isClosed()) {
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
                String boardState = MessageLib.convertBoardArrayToString(controller.getBoardState());
                out.write("Your move:" + boardState);
                out.flush();

                BufferedReader in = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
                return in.readLine(); // Wait for the client's move
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


    public static void main(String[] args) throws IOException {
        GameServer server = new GameServer(12345, new GameController(null)); // Port 12345
        server.start();
    }
}
