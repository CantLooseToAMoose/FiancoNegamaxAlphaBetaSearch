package Fianco.AI.BitBoard;

import Fianco.GameEngine.Fianco;

import java.util.Arrays;

public class BitmapFianco {
    private long board_full_1;
    private long board_full_2;
    private long board_partial_1;
    private long board_partial_2;

    private static final int fillUpUntil = 64;

    public BitmapFianco() {
    }

    public BitmapFianco(long board_full_1, long board_full_2, long board_partial_1, long board_partial_2) {
        this.board_full_1 = board_full_1;
        this.board_full_2 = board_full_2;
        this.board_partial_1 = board_partial_1;
        this.board_partial_2 = board_partial_2;
    }

    public BitmapFianco(long[] player1, long[] player2) {
        this.board_full_1 = player1[0];
        this.board_partial_1 = player1[1];
        this.board_full_2 = player2[0];
        this.board_partial_2 = player2[1];
    }

    public BitmapFianco(long[] board) {
        this.board_full_1 = board[0];
        this.board_partial_1 = board[1];
        this.board_full_2 = board[2];
        this.board_partial_2 = board[3];
    }

    public long[] getPlayer1Board() {
        return new long[]{this.board_full_1, this.board_partial_1};
    }

    public long[] getPlayer2Board() {
        return new long[]{this.board_full_2, this.board_partial_2};
    }

    public long[] getFullBoard() {
        return new long[]{this.board_full_1, this.board_partial_1, this.board_full_2, this.board_partial_2};
    }

    public void setPlayer1Board(long[] player1Board) {
        this.board_full_1 = player1Board[0];
        this.board_partial_1 = player1Board[1];
    }

    public void setPlayer2Board(long[] player2Board) {
        this.board_full_2 = player2Board[0];
        this.board_partial_2 = player2Board[1];
    }


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
                } else {
                    System.out.println("There is a different value than 0,1,2 on the int[][] Board");
                }
            }
        }
        String player1Full = player1Pieces.substring(0, fillUpUntil);
//        System.out.println("String created from Int Array for Player1Full: "+player1Full);
        String player2Full = player2Pieces.substring(0, fillUpUntil);
//        System.out.println("String created from Int Array for Player2Full: "+player2Full);
        String player1Partial = player1Pieces.substring(fillUpUntil, 81);
//        System.out.println("String created from Int Array for Player1Partial: "+player1Partial);
        String player2Partial = player2Pieces.substring(fillUpUntil, 81);
//        System.out.println("String created from Int Array for Player2Partial: "+player2Partial);
//        System.out.println("And now filled with 0s from the right side:");
//        System.out.println("String created from Int Array for Player1Partial: "+String.format("%-64s", player1Partial).replace(' ', '0'));
//        System.out.println("String created from Int Array for Player2Partial: "+String.format("%-64s", player2Partial).replace(' ', '0'));
        this.board_full_1 = Long.parseUnsignedLong(player1Full, 2);
        this.board_full_2 = Long.parseUnsignedLong(player2Full, 2);
        this.board_partial_1 = Long.parseUnsignedLong(String.format("%-64s", player1Partial).replace(' ', '0'), 2);
        this.board_partial_2 = Long.parseUnsignedLong(String.format("%-64s", player2Partial).replace(' ', '0'), 2);
    }

    public int[][] convertBitmapTo2DIntArray() {
//        System.out.println("FullBinaryLong Player1:" + returnFullBinaryString(board_full_1));
//        System.out.println("PartialBinaryLong Player1:" + returnFullBinaryString(board_partial_1));
//        System.out.println("FullBinaryLong Player2:" + returnFullBinaryString(board_full_2));
//        System.out.println("PartialBinaryLong Player2:" + returnFullBinaryString(board_partial_2));
        int[][] array = new int[9][9];
        int string_position;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                string_position = 9 * i + j;
                int piece1;
                int piece2;
                if (string_position < fillUpUntil) {
//                    string_position += fillUpUntil;
                    piece1 = Character.getNumericValue(returnFullBinaryString(board_full_1).charAt(string_position));
                    piece2 = Character.getNumericValue(returnFullBinaryString(board_full_2).charAt(string_position));
                } else {
                    string_position -= fillUpUntil;
//                    string_position += 64 - (81 - fillUpUntil);
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
        String ret = Long.toBinaryString(number); // Convert number to binary
        ret = String.format("%64s", ret).replace(' ', '0'); // Left-justify and fill with zeros on the right
//        System.out.println("BinaryStringLength:" + ret.length() + " String:" + ret);
        return ret;
    }


    @Override
    public String toString() {
        String ret = "";
        for (int[] row : convertBitmapTo2DIntArray()) {
            ret += Arrays.toString(row) + "\n";
        }
        ret = ret.replace("0", ".");
        ret = ret.replace("[", "");
        ret = ret.replace("]", "");
        ret = ret.replace(",", "");
        return ret;
    }

    public static void ShowBitBoard(long[] board) {
        BitmapFianco bitmapFianco = new BitmapFianco();
        if (board.length == 2) {
            bitmapFianco.setPlayer1Board(board);
            System.out.println("Player1Board");
            System.out.println(bitmapFianco);
        } else if (board.length == 4) {
            bitmapFianco.setPlayer1Board(new long[]{board[0], board[1]});
            System.out.println("Player1Board");
            System.out.println(bitmapFianco);
            bitmapFianco.setPlayer1Board(new long[]{board[2], board[3]});
            System.out.println("Player2Board");
            System.out.println(bitmapFianco);
            bitmapFianco.setPlayer1Board(new long[]{board[0], board[1]});
            bitmapFianco.setPlayer2Board(new long[]{board[2], board[3]});
            System.out.println("Complete Board");
            System.out.println(bitmapFianco);

        }
    }


    public static void main(String[] args) {
        BitmapFianco bitmapFianco = new BitmapFianco();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(new Fianco().getBoardState());
//        ShiftPlayersDown1Row
        long[] player1 = bitmapFianco.getPlayer1Board();
        player1 = BasicBitOps.shiftSouth(player1, 1);
        bitmapFianco.setPlayer1Board(player1);

//        long[] player2 = bitmapFianco.getPlayer2Board();
//        player2 = BitOps.shiftR(player2, 9);
//        bitmapFianco.setPlayer2Board(player2);
        bitmapFianco.setPlayer2Board(new long[2]);


        System.out.println(bitmapFianco);
    }
}


