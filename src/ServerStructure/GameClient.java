package ServerStructure;

import AI.RandomFiancoAgent;
import AI.SimpleAlphaBetaSearchAgent;
import BitBoard.BitmapFianco;
import AI.IAgent;
import AI.RandomFiancoBitMapAgent;
import FiancoGameEngine.Fianco;

import java.io.*;
import java.net.*;

public class GameClient {
    private static final String movePrefix = "Your move:";

    private IAgent aiAgent;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    public GameClient(String host, int port, String playerId, IAgent aiAgent) throws IOException {
        this.aiAgent = aiAgent;
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
                System.out.println("Waiting for Server message");
                String serverMessage = in.readLine(); // Waiting for server's message (polling)
                if (serverMessage.startsWith(movePrefix)) {
                    String BoardArrayString = serverMessage.substring(movePrefix.length()); //Get only the Boardstate from Message
                    int[][] BoardArray = MessageLib.convertBoardStringToArray(BoardArrayString); // Convert to right Datatype
                    String move = MessageLib.convertBoardArrayToString(aiAgent.generateMove(BoardArray)); // AI logic to choose a move from
                    System.out.println("Send Board back to Server");
                    out.write(move + "\n");
                    out.flush(); // Send new Boardstate
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        int whichAi = 0;
        try {
            whichAi = Integer.parseInt(args[2]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        BitmapFianco bitmapFianco = new BitmapFianco();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(new Fianco().getBoardState());
        IAgent aiAgent = null;
        if (whichAi == 0) {
            aiAgent = new RandomFiancoAgent(new Fianco(), Integer.parseInt(args[1]));
        } else if (whichAi == 1) {
            aiAgent = new RandomFiancoBitMapAgent(bitmapFianco, Integer.parseInt(args[1]));
        } else if (whichAi == 2) {
            aiAgent = new SimpleAlphaBetaSearchAgent(bitmapFianco, Integer.parseInt(args[1]));
        }
        GameClient client = new GameClient("localhost", 12345, args[0], aiAgent);
        client.start();
    }
}
