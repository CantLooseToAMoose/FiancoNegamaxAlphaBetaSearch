package ServerStructure;

import AI.*;
import AI.Agent.*;
import AI.BitBoard.BitmapFianco;
import GameEngine.Fianco;
import GameEngine.MoveCommand;

import java.io.*;
import java.net.Socket;

public class GameClient {
    private static final String movePrefix = "Your move:";
    private static final String undoPrefix = "Undo";
    private static final String restartPrefix = "Restart: ";

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
                System.out.println("Received Message:" + serverMessage);
                if (serverMessage.startsWith(movePrefix)) {
                    String moveCommandString = serverMessage.substring(movePrefix.length()); //Get only the Boardstate from Message
                    MoveCommand moveCommand = MessageLib.convertMoveCommandStringToMoveCommand(moveCommandString);// Convert to right Datatype
                    String move = MessageLib.convertMoveCommandToString(aiAgent.generateMove(moveCommand)); // AI logic to choose a move from
                    System.out.println("Send Move back to Server");
                    out.write(move + "\n");
                    out.flush(); // Send new Boardstate
                } else if (serverMessage.startsWith(undoPrefix)) {
                    aiAgent.undoMove();
                } else if (serverMessage.startsWith(restartPrefix)) {
                    String boardString = serverMessage.substring(restartPrefix.length()); //Get only the Boardstate from Message
                    int[][] board = MessageLib.convertBoardStringToArray(boardString);// Convert to right Datatype
                    aiAgent.resetBoard(board);
                }
                Thread.sleep(50);
                System.gc();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
        Fianco fianco = new Fianco();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(new Fianco().getBoardState());
        IAgent aiAgent = null;
        if (whichAi == 0) {
            aiAgent = new RandomFiancoAgent(new Fianco(), Integer.parseInt(args[1]));
        } else if (whichAi == 1) {
            aiAgent = new RandomFiancoAgent(fianco, Integer.parseInt(args[1]));
        } else if (whichAi == 2) {
            aiAgent = new SequentialAlphaBetaSearchAgent(bitmapFianco, Integer.parseInt(args[1]));
        } else if (whichAi == 3) {
            aiAgent = new IterativeAlphaBetaSearchAgent(bitmapFianco, Integer.parseInt(args[1]));
        } else if (whichAi == 4) {
            aiAgent = new IterativeAlphaBetaSearchAgentQuiescence(bitmapFianco, Integer.parseInt(args[1]));
        } else if (whichAi == 5) {
            aiAgent = new IterativeAlphaBetaSearchAgentQuiescenceAndKillerMoves(bitmapFianco, Integer.parseInt(args[1]));
        } else if (whichAi == 6) {
            aiAgent = new PVSWithQuiescAndKMAndPonderingAgent(bitmapFianco, Integer.parseInt(args[1]));
        } else if (whichAi == 7) {
            aiAgent = new PVSWithQuiescAndKMAndPonderingAndHHWithAspirationAgent(bitmapFianco, Integer.parseInt(args[1]));
        }

        GameClient client = new GameClient("localhost", 12345, args[0], aiAgent);
        client.start();
    }
}
