package Fianco.ServerStructure;

import Fianco.GameEngine.MoveCommand;
import com.google.gson.Gson;

public class MessageLib {
    public static String convertBoardArrayToString(int[][] boardState) {
        Gson gson = new Gson();
        return gson.toJson(boardState);
    }

    public static String convertMoveCommandToString(MoveCommand moveCommand) {
        Gson gson = new Gson();
        return gson.toJson(moveCommand);
    }

    public static int[][] convertBoardStringToArray(String message) {
        Gson gson = new Gson();
        return gson.fromJson(message, int[][].class);
    }

    public static MoveCommand convertMoveCommandStringToMoveCommand(String message) {
        Gson gson = new Gson();
        return gson.fromJson(message, MoveCommand.class);
    }
}
