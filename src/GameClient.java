import java.io.*;
import java.net.*;
import java.util.Arrays;

public class GameClient {
    private static final String movePrefix = "Your move:";

    private IAgent aiAgent;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    public GameClient(String host, int port, String playerId, IAgent aiAgent) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        // Identify as player 1 or player 2
        out.write(playerId + "\n");
        out.flush();
    }

    public void start() {
        try {
            while (true) {
                String serverMessage = in.readLine(); // Waiting for server's message (polling)
                if (serverMessage.startsWith(movePrefix)) {
                    String BoardArrayString = serverMessage.substring(movePrefix.length()); //Get only the Boardstate from Message
                    int[][] BoardArray = MessageLib.convertBoardStringToArray(BoardArrayString); // Convert to right Datatype
                    String move = MessageLib.convertBoardArrayToString(aiAgent.generateMove(BoardArray)); // AI logic to choose a move from

                    out.write(move);
                    out.flush(); // Send new Boardstate

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        GameClient client = new GameClient("localhost", 12345, "player1", null);
        client.start();
    }
}
