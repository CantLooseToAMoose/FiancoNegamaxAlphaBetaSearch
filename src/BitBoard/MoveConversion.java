package BitBoard;

import FiancoGameEngine.MoveCommand;

/**
 * This class helps with converting two different types of Move Representations. You can either have a Move be represented as a long array or a short value.For the long array the first two
 * longs represent a board with just a single piece, that is the starting position. And the last two longs representing the landing position of the piece. If the array is of size 6 a capture happened and
 * the middle two longs represent the position of the piece that got captured.
 * <p>
 * In general you can represent positions on the board using just a number between 1 and 81 with 1 being row 1 and column 1 and 81 being row nine and column 9.
 * The short move now represents the starting positon and the landing position as the first 4 bytes for the starting position and the last 4 bytes as the landing position.
 */
public class MoveConversion {

    public static short getShortTypeMoveFromLongArrayMove(long[] move) {
        int fromPosition = 0;
        int toPosition = 0;
        if (move.length == 4) {
            if (move[0] == 0) {
                fromPosition = 64 - Long.numberOfTrailingZeros(move[1]) + 64;
            } else if (move[1] == 0) {
                fromPosition = 64 - Long.numberOfTrailingZeros(move[0]);
            }
            if (move[2] == 0) {
                toPosition = 64 - Long.numberOfTrailingZeros(move[3]) + 64;
            } else if (move[3] == 0) {
                toPosition = 64 - Long.numberOfTrailingZeros(move[2]);
            }
        } else if (move.length == 6) {
            if (move[0] == 0) {
                fromPosition = 64 - Long.numberOfTrailingZeros(move[1]) + 64;
            } else if (move[1] == 0) {
                fromPosition = 64 - Long.numberOfTrailingZeros(move[0]);
            }
            if (move[4] == 0) {
                toPosition = 64 - Long.numberOfTrailingZeros(move[5]) + 64;
            } else if (move[5] == 0) {
                toPosition = 64 - Long.numberOfTrailingZeros(move[4]);
            }
        } else {
            System.out.println("Received a long array move with different sizes of 4 and 6");
            return 0;
        }
        return pack(fromPosition, toPosition);
    }

    public static MoveCommand getMoveCommandFromShortMove(short move, boolean isPlayerOne) {
        int from = unpackFirstNumber(move);
        int to = unpackSecondNumber(move);
        int from_row = (from - 1) / 9;
        int from_col = (from - 1) % 9;
        int to_row = (to - 1) / 9;
        int to_col = (to - 1) % 9;
        return new MoveCommand(from_row, from_col, to_row, to_col, isPlayerOne ? 1 : 2);
    }

    public static short getShortMoveFromMoveCommand(MoveCommand move) {
        int from = move.getFrom_row() * 9 + move.getFrom_col() + 1;
        int to = move.getTo_row() * 9 + move.getTo_col() + 1;
        return MoveConversion.pack(from, to);
    }

    /**
     * I really hate this function. Why does it have to be so nested? I want to change it but it works
     *
     * @param move
     * @return
     */
    public static long[] getLongArrayTypeMoveFromShortArrayMove(short move) {
        int fromPosition = unpackFirstNumber(move);
        int toPosition = unpackSecondNumber(move);
        int temp = fromPosition - toPosition;
        temp = switch (temp) {
            case -20 -> fromPosition + 10;
            case -16 -> fromPosition + 8;
            case 16 -> fromPosition - 8;
            case 20 -> fromPosition - 10;
            default -> 0;
        };
        if (temp == 0) {
            if (fromPosition > 64) {
                if (toPosition > 64) {
                    return new long[]{0, 1L << fromPosition - 2, 0, 1L << toPosition - 2};
                } else {
                    return new long[]{0, 1L << fromPosition - 2, 1L << 63 - (toPosition - 1), 0};
                }
            } else {
                if (toPosition > 64) {
                    return new long[]{1L << 63 - (fromPosition - 1), 0, 0, 1L << toPosition - 2};
                } else {
                    return new long[]{1L << 63 - (fromPosition - 1), 0, 1L << 63 - (toPosition - 1), 0};
                }
            }
        } else {
            if (temp > 64) {
                if (fromPosition > 64) {
                    if (toPosition > 64) {
                        return new long[]{0, 1L << fromPosition - 2, 0, 1L << temp - 2, 0, 1L << toPosition - 2};
                    } else {
                        return new long[]{0, 1L << fromPosition - 2, 0, 1L << temp - 2, 1L << 63 - (toPosition - 1), 0};
                    }
                } else {
                    if (toPosition > 64) {
                        return new long[]{1L << 63 - (fromPosition - 1), 0, 0, 1L << temp - 2, 0, 1L << toPosition - 2};
                    } else {
                        return new long[]{1L << 63 - (fromPosition - 1), 0, 0, 1L << temp - 2, 1L << 63 - (toPosition - 1), 0};
                    }
                }

            } else {
                if (fromPosition > 64) {
                    if (toPosition > 64) {
                        return new long[]{0, 1L << fromPosition - 2, 1L << 63 - (temp - 1), 0, 0, 1L << toPosition - 2};
                    } else {
                        return new long[]{0, 1L << fromPosition - 2, 1L << 63 - (temp - 1), 0, 1L << 63 - (toPosition - 1), 0};
                    }
                } else {
                    if (toPosition > 64) {
                        return new long[]{1L << 63 - (fromPosition - 1), 0, 1L << 63 - (temp - 1), 0, 0, 1L << toPosition - 2};
                    } else {
                        return new long[]{1L << 63 - (fromPosition - 1), 0, 1L << 63 - (temp - 1), 0, 1L << 63 - (toPosition - 1), 0};
                    }
                }
            }
        }
    }

    // Method to pack two numbers into a short
    public static short pack(int num1, int num2) {
        if (num1 < 1 || num1 > 81 || num2 < 1 || num2 > 81) {
            return 0;
        }
        // Pack the numbers into a short: num1 goes in the higher byte, num2 in the lower byte
        return (short) ((num1 << 8) | (num2 & 0xFF));
    }

    // Method to unpack the first number from the short
    public static int unpackFirstNumber(short packed) {
        return (packed >> 8) & 0xFF; // Extract the higher byte
    }

    // Method to unpack the second number from the short
    public static int unpackSecondNumber(short packed) {
        return packed & 0xFF; // Extract the lower byte
    }


    public static void main(String[] args) {
        BitmapFianco bitmapFianco = new BitmapFianco();
        int[][] moveFrom = new int[][]{
                {1, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0}};
        int[][] moveTo = new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0}};
        int[][] moveCapture = new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0}};
        int[][] moveToCapture = new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0}};

        bitmapFianco.populateBoardBitmapsFrom2DIntArray(moveFrom);
        long[] moveFromLong = bitmapFianco.getPlayer1Board();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(moveTo);
        long[] moveToLong = bitmapFianco.getPlayer1Board();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(moveCapture);
        long[] moveCaptureLong = bitmapFianco.getPlayer1Board();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(moveToCapture);
        long[] moveToCaptureLong = bitmapFianco.getPlayer1Board();

        long[] simpleMove = new long[]{moveFromLong[0], moveFromLong[1], moveToLong[0], moveToLong[1]};
        long[] captureMove = new long[]{moveFromLong[0], moveFromLong[1], moveCaptureLong[0], moveCaptureLong[1], moveToCaptureLong[0], moveToCaptureLong[1]};

        short simpleMoveShort = getShortTypeMoveFromLongArrayMove(simpleMove);
        short captureMoveShort = getShortTypeMoveFromLongArrayMove(captureMove);

        simpleMove = getLongArrayTypeMoveFromShortArrayMove(simpleMoveShort);
        captureMove = getLongArrayTypeMoveFromShortArrayMove(captureMoveShort);
        System.out.println("Simple Move From");
        bitmapFianco.setPlayer1Board(new long[]{simpleMove[0], simpleMove[1]});
        System.out.println(bitmapFianco);
        System.out.println("Simple Move To");
        bitmapFianco.setPlayer1Board(new long[]{simpleMove[2], simpleMove[3]});
        System.out.println(bitmapFianco);
        System.out.println("Capture Move From");
        bitmapFianco.setPlayer1Board(new long[]{captureMove[0], captureMove[1]});
        System.out.println(bitmapFianco);
        System.out.println("Capture Move Capture");
        bitmapFianco.setPlayer1Board(new long[]{captureMove[2], captureMove[3]});
        System.out.println(bitmapFianco);
        System.out.println("Capture Move To");
        bitmapFianco.setPlayer1Board(new long[]{captureMove[4], captureMove[5]});
        System.out.println(bitmapFianco);
    }

}
