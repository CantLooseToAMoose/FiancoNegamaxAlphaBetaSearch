package ServerStructure;

import com.google.gson.Gson;

import java.util.Arrays;

public class MessageLib {
    public static String convertBoardArrayToString(int[][] boardState) {

        Gson gson = new Gson();
        return gson.toJson(boardState);
    }

    public static int[][] convertBoardStringToArray(String message) {
        Gson gson = new Gson();
        return gson.fromJson(message, int[][].class);
    }
}
