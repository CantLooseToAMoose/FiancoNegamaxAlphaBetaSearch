package AI;

import FiancoEngine.Fianco;

import java.lang.reflect.Array;
import java.util.Arrays;

public class BitmapFianco {
    private long board_full_1;
    private long board_full_2;
    private long board_partial_1;
    private long board_partial_2;

    public void populateBoardBitmapsFrom2DIntArray(int[][] boardState) {
        StringBuilder player1Pieces = new StringBuilder();
        StringBuilder player2Pieces = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int piece = boardState[i][j];
                if (piece == 0) {
                    player1Pieces.append(0);
                    player2Pieces.append(0);
                } else if (piece == 1) {
                    player1Pieces.append(1);
                    player2Pieces.append(0);
                } else if (piece == 2) {
                    player1Pieces.append(0);
                    player2Pieces.append(1);
                }
            }
        }
        String player1Full = player1Pieces.substring(0, 64);
        String player2Full = player2Pieces.substring(0, 64);
        String player1Partial = player1Pieces.substring(64, 81);
        String player2Partial = player2Pieces.substring(64, 81);
        this.board_full_1 = Long.parseUnsignedLong(player1Full, 2);
        this.board_full_2 = Long.parseUnsignedLong(player2Full, 2);
        this.board_partial_1 = Long.parseUnsignedLong(player1Partial, 2);
        this.board_partial_2 = Long.parseUnsignedLong(player2Partial, 2);
    }

    public int[][] convertBitmapTo2DIntArray() {
        int[][] array = new int[9][9];
        int string_position;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                string_position = 9 * i + j;
                int piece1;
                int piece2;
                if (string_position < 64) {
                    piece1 = Character.getNumericValue(returnFullBinaryString(board_full_1).charAt(string_position));
                    piece2 = Character.getNumericValue(returnFullBinaryString(board_full_2).charAt(string_position));
                } else {
                    string_position += 64 - 81;
                    piece1 = Character.getNumericValue(returnFullBinaryString(board_partial_1).charAt(string_position));
                    piece2 = Character.getNumericValue(returnFullBinaryString(board_partial_2).charAt(string_position));
                }
                if (piece1 == 1 && piece2 == 0) {
                    array[i][j] = 1;
                } else if (piece1 == 0 && piece2 == 1) {
                    array[i][j] = 2;
                } else if (piece1 == 1 && piece2 == 1) {
                    System.out.println("There are two pieces on the same Place");
                }
            }
        }
        return array;
    }

    private String returnFullBinaryString(long number) {
        String ret = String.format("%64s", Long.toBinaryString(number)).replace(" ", "0");
        return ret;

    }

    public static void main(String[] args) {
        BitmapFianco bitmapFianco = new BitmapFianco();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(new Fianco().getBoardState());
        String board = Arrays.deepToString(bitmapFianco.convertBitmapTo2DIntArray());
        System.out.println(board);

    }
}


