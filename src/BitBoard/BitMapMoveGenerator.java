package BitBoard;

import search.Evaluate;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A board is represented as a long[4] with the first two entries being the two longs used for the player1 Bitmap and the second two longs used for the player2 Bitmap
 * A move is represented as either a long[4] or long[6] depending on if it is a capture move or not. It consists of Bitmaps with a single positions as 1. For no capturing moves the first 2 longs describe the position of the piece that gets moved and the second two bits are use for the landing position.
 * For a capturing move the first two longs get used for the position of the piece that gets moved the second two longs get used for the position of the piece that gets captured. And the third two longs are used for the landing position of the moved piece.
 */
public class BitMapMoveGenerator {


    private static int populateShortArrayWithCaptureMoves(long[] board, boolean isPlayerOne, short[] moveArray, int arrayDepthOffset) {
        int movesAdded = 0;
        long[] playerBoard;
        long[] opponentBoard;
        if (isPlayerOne) {
            playerBoard = new long[]{board[0], board[1]};
            opponentBoard = new long[]{board[2], board[3]};
        } else {
            playerBoard = new long[]{board[2], board[3]};
            opponentBoard = new long[]{board[0], board[1]};
        }
        long[] capturePiecesWest;
        long[] capturePiecesEast;
        long[] freeTileBitMask = BitMaskCreationHelper.getFreeTilesBitMask(playerBoard, opponentBoard);

        if (isPlayerOne) {
            capturePiecesEast = AdvancedBitOps.fasterPossibleSouthEastMovePieces(playerBoard, opponentBoard, freeTileBitMask);
            capturePiecesWest = AdvancedBitOps.fasterPossibleSouthWestMovePieces(playerBoard, opponentBoard, freeTileBitMask);
        } else {
            capturePiecesEast = AdvancedBitOps.fasterPossibleNorthEastMovePieces(playerBoard, opponentBoard, freeTileBitMask);
            capturePiecesWest = AdvancedBitOps.fasterPossibleNorthWestMovePieces(playerBoard, opponentBoard, freeTileBitMask);
        }
        movesAdded += populateShortArrayWithCapturePiecesBoard(capturePiecesEast, isPlayerOne, false, moveArray, arrayDepthOffset, movesAdded);

        movesAdded += populateShortArrayWithCapturePiecesBoard(capturePiecesWest, isPlayerOne, true, moveArray, arrayDepthOffset, movesAdded);
        return movesAdded;
    }


    private static int populateShortArrayWithCapturePiecesBoard(long[] capturePieces, boolean isPlayerOne, boolean captureWest, short[] moves, int arrayDepthOffset, int arrayMoveOffset) {
        int movesAdded = 0;
        for (int i = 0; i < capturePieces.length; i++) {
            long temp = capturePieces[i];
            // Iterate through the set bits (pieces) in capturePieces[i]
            while (temp != 0) {
                // Isolate the lowest set bit and get the fromPosition
                int fromPosition;
                if (i == 0) {
                    fromPosition = 64 - Long.numberOfTrailingZeros(temp);  // For the first part of the board (0-63)
                } else if (i == 1) {
                    fromPosition = 64 - Long.numberOfTrailingZeros(temp) + 64;  // For the second part of the board (64-81)
                } else {
                    System.out.println("There is something wrong in the populateQueueWithCapturePiecesBoard Function, don't trust it!");
                    return 0;  // Exit the function if something is wrong
                }
                // Determine the positions for opponent piece and player landing based on the direction
                int opponentPosition;
                int landingPosition;
                if (isPlayerOne) {
                    if (captureWest) {
                        opponentPosition = fromPosition + 8;  // Move south-west (9 down, 1 left)
                        landingPosition = fromPosition + 16;  // Move two steps south-west
                    } else {
                        opponentPosition = fromPosition + 10;  // Move south-east (9 down, 1 right)
                        landingPosition = fromPosition + 20;  // Move two steps south-east
                    }
                } else {
                    if (captureWest) {
                        opponentPosition = fromPosition - 10;  // Move north-west (9 up, 1 left)
                        landingPosition = fromPosition - 20;  // Move two steps north-west
                    } else {
                        opponentPosition = fromPosition - 8;  // Move north-east (9 up, 1 right)
                        landingPosition = fromPosition - 16;  // Move two steps north-east
                    }
                }
                // Ensure both the opponentPosition and landingPosition are within valid range
                if (opponentPosition >= 1 && opponentPosition <= 81 &&
                        landingPosition >= 1 && landingPosition <= 81) {
                    // Pack the fromPosition and landingPosition into a short
                    short move = MoveConversion.pack(fromPosition, landingPosition);
                    // Add the packed move to the queue

                    moves[arrayDepthOffset + arrayMoveOffset + movesAdded] = move;
                    movesAdded++;
                }
                // Flip the lowest set bit to move on to the next piece
                temp &= (temp - 1);
            }
        }
        return movesAdded;
    }


    private static int populateShortArrayWithQuietMoves(long[] board, short[] moveArray, boolean isPlayerOne, int arrayOffset) {
        int movesAdded = 0;
        long[] playerBoard;
        long[] opponentBoard;
        if (isPlayerOne) {
            playerBoard = new long[]{board[0], board[1]};
            opponentBoard = new long[]{board[2], board[3]};
        } else {
            playerBoard = new long[]{board[2], board[3]};
            opponentBoard = new long[]{board[0], board[1]};
        }
//        System.out.println("Board to generate Moves from");
//        BitmapFianco.ShowBitBoard(board);
        long[] freeTileBitMask = BitMaskCreationHelper.getFreeTilesBitMask(playerBoard, opponentBoard);
//        System.out.println("Free Tiles BitMask");
//        BitmapFianco.ShowBitBoard(freeTileBitMask);
        long[] canMoveEast = AdvancedBitOps.fasterPossibleEastMovePieces(playerBoard, freeTileBitMask);
//        System.out.println("Can Move East:");
//        BitmapFianco.ShowBitBoard(canMoveEast);
        long[] canMoveWest = AdvancedBitOps.fasterPossibleWestMovePieces(playerBoard, freeTileBitMask);
//        System.out.println("Can Move West:");
//        BitmapFianco.ShowBitBoard(canMoveEast);
        long[] canMoveForward;

        if (isPlayerOne) {
            canMoveForward = AdvancedBitOps.fasterPossibleSouthMovePieces(playerBoard, freeTileBitMask);
        } else {
            canMoveForward = AdvancedBitOps.fasterPossibleNorthMovePieces(playerBoard, freeTileBitMask);
        }
//        System.out.println("Can Move Forward:");
//        BitmapFianco.ShowBitBoard(canMoveForward);
        movesAdded += populateShortArrayWithForwardMovesBoard(canMoveForward, isPlayerOne, moveArray, arrayOffset + movesAdded);
        movesAdded += populateShortArrayWithSideMovesBoard(canMoveEast, false, moveArray, arrayOffset + movesAdded);
        movesAdded += populateShortArrayWithSideMovesBoard(canMoveWest, true, moveArray, arrayOffset + movesAdded);
        return movesAdded;

    }


    private static int populateShortArrayWithForwardMovesBoard(long[] movePieces, boolean isPlayerOne, short[] moves, int arrayOffset) {
        int movesAdded = 0;
        for (int i = 0; i < movePieces.length; i++) {
            long temp = movePieces[i];
            // Iterate through the set bits (pieces) in movePieces[i]
            while (temp != 0) {
                // Isolate the lowest set bit
                int fromPosition;
                if (i == 0) {
                    fromPosition = 64 - Long.numberOfTrailingZeros(temp);  // For first part of the board (0-63)
                } else if (i == 1) {
                    fromPosition = 64 - Long.numberOfTrailingZeros(temp) + 64;  // For second part of the board (64-81)
                } else {
                    System.out.println("There is something wrong in the populateQueueWithForwardMovesBoard Function, don't trust it!");
                    return movesAdded;  // Exit the function if something is wrong
                }
                // Determine landing position based on the direction of movement
                int toPosition;
                if (isPlayerOne) {
                    toPosition = fromPosition + 9;  // Move south (downwards)
                } else {
                    toPosition = fromPosition - 9;  // Move north (upwards)
                }
                // Pack the from and to positions into a short
                short move = MoveConversion.pack(fromPosition, toPosition);
                // Add the packed move to the queue
                moves[arrayOffset + movesAdded] = move;
                movesAdded++;
                // Flip the lowest set bit to move on to the next piece
                temp &= (temp - 1);
            }
        }
        return movesAdded;
    }


    private static int populateShortArrayWithSideMovesBoard(long[] movePieces, boolean moveWest, short[] moves, int arrayOffset) {
        int movesAdded = 0;
        for (int i = 0; i < movePieces.length; i++) {
            long temp = movePieces[i];
            // Iterate through the set bits (pieces) in movePieces[i]
            while (temp != 0) {
                // Isolate the lowest set bit and get the fromPosition
                int fromPosition;
                if (i == 0) {
                    fromPosition = 64 - Long.numberOfTrailingZeros(temp);  // For the first part of the board (0-63)
                } else if (i == 1) {
                    fromPosition = 64 - Long.numberOfTrailingZeros(temp) + 64;  // For the second part of the board (64-81)
                } else {
                    System.out.println("There is something wrong in the populateQueueWithSideMovesBoard Function, don't trust it!");
                    return movesAdded;  // Exit the function if something is wrong
                }
                // Determine landing position based on whether moving west or east
                int toPosition;
                if (moveWest) {
                    toPosition = fromPosition - 1;  // Move west (left)
                } else {
                    toPosition = fromPosition + 1;  // Move east (right)
                }
                // Pack the from and to positions into a short
                short move = MoveConversion.pack(fromPosition, toPosition);
                // Add the packed move to the queue
                moves[arrayOffset + movesAdded] = move;
                movesAdded++;
                // Flip the lowest set bit to move on to the next piece
                temp &= (temp - 1);
            }
        }
        return movesAdded;
    }


    public static int populateShortArrayWithAllPossibleMoves(long[] board, boolean isPlayerOne, short[] moveArray, int arrayOffset) {
        int movesAdded = 0;
        movesAdded += populateShortArrayWithCaptureMoves(board, isPlayerOne, moveArray, arrayOffset);
        if (movesAdded == 0) {
            movesAdded += populateShortArrayWithQuietMoves(board, moveArray, isPlayerOne, arrayOffset + movesAdded);
        }
        return movesAdded;

    }

    /**
     * Father when did i leave the path of Light and created such Hell.
     *
     * @param board
     * @param move
     * @param isPlayerOne
     */
    public static void makeOrUnmakeMoveInPlace(long[] board, short move, boolean isPlayerOne) {
        int fromPosition = MoveConversion.unpackFirstNumber(move);
        int toPosition = MoveConversion.unpackSecondNumber(move);
        int temp = fromPosition - toPosition;
        temp = switch (temp) {
            case -20 -> fromPosition + 10;
            case -16 -> fromPosition + 8;
            case 16 -> fromPosition - 8;
            case 20 -> fromPosition - 10;
            default -> 0;
        };
        if (isPlayerOne) {
            if (fromPosition > 64) {
                board[1] = board[1] ^ (1L << 64 - fromPosition + 64);
            } else {
                board[0] = board[0] ^ (1L << 64 - fromPosition);
            }
            if (toPosition > 64) {
                board[1] = board[1] ^ (1L << 64 - toPosition + 64);
            } else {
                board[0] = board[0] ^ (1L << 64 - toPosition);
            }
            if (temp != 0) {
                if (temp > 64) {
                    board[3] = board[3] ^ (1L << 64 - temp + 64);
                } else {
                    board[2] = board[2] ^ (1L << 64 - temp);
                }
            }
        } else {
            if (fromPosition > 64) {
                board[3] = board[3] ^ (1L << 64 - fromPosition + 64);
            } else {
                board[2] = board[2] ^ (1L << 64 - fromPosition);
            }
            if (toPosition > 64) {
                board[3] = board[3] ^ (1L << 64 - toPosition + 64);
            } else {
                board[2] = board[2] ^ (1L << 64 - toPosition);
            }
            if (temp != 0) {
                if (temp > 64) {
                    board[1] = board[1] ^ (1L << 64 - temp + 64);
                } else {
                    board[0] = board[0] ^ (1L << 64 - temp);
                }
            }

        }
    }

    public static boolean isMoveValid(long[] board, short move, boolean isPlayerOne) {
        int fromPosition = MoveConversion.unpackFirstNumber(move);
        int toPosition = MoveConversion.unpackSecondNumber(move);
        long checkIfFromPositionIsFilled = 0L;
        long checkIfToPositionIsNotFilled = 0L;
        long checkIfCapturePositionIsFilled = 1L;
        int temp = fromPosition - toPosition;

        temp = switch (temp) {
            case -20 -> fromPosition + 10;
            case -16 -> fromPosition + 8;
            case 16 -> fromPosition - 8;
            case 20 -> fromPosition - 10;
            case 9, -9, -1, 1 -> 0;
            default -> -1;
        };
        if (temp == -1) {
            return false;
        } else if (temp == 0) {
            long[] player1Board;
            long[] player2Board;
            if (isPlayerOne) {
                player1Board = new long[]{board[0], board[1]};
                player2Board = new long[]{board[2], board[3]};
                long[] freeTilesBitMask = AdvancedBitOps.FreeTilesBoardMask(player1Board, player2Board);
                long[] southWestCaptureMoves = AdvancedBitOps.fasterPossibleSouthWestMovePieces(player1Board, player2Board, freeTilesBitMask);
                long[] southEastCaptureMoves = AdvancedBitOps.fasterPossibleSouthEastMovePieces(player1Board, player2Board, freeTilesBitMask);
                int count = BasicBitOps.getNumberOfOnesInPlayerBoard(southWestCaptureMoves) + BasicBitOps.getNumberOfOnesInPlayerBoard(southEastCaptureMoves);
                if (count != 0) {
                    return false;
                }

            } else {
                player1Board = new long[]{board[2], board[3]};
                player2Board = new long[]{board[0], board[1]};
                long[] freeTilesBitMask = AdvancedBitOps.FreeTilesBoardMask(player1Board, player2Board);
                long[] northWestCaptureMoves = AdvancedBitOps.fasterPossibleNorthWestMovePieces(player1Board, player2Board, freeTilesBitMask);
                long[] northEastCaptureMoves = AdvancedBitOps.fasterPossibleNorthEastMovePieces(player1Board, player2Board, freeTilesBitMask);
                int count = BasicBitOps.getNumberOfOnesInPlayerBoard(northWestCaptureMoves) + BasicBitOps.getNumberOfOnesInPlayerBoard(northEastCaptureMoves);
                if (count != 0) {
                    return false;
                }
            }
        }


        if (isPlayerOne) {
            if (fromPosition > 64) {
                checkIfFromPositionIsFilled = board[1] & (1L << 64 - fromPosition + 64);
            } else {
                checkIfFromPositionIsFilled = board[0] & (1L << 64 - fromPosition);
            }
            if (toPosition > 64) {
                checkIfToPositionIsNotFilled = board[1] & (1L << 64 - toPosition + 64);
            } else {
                checkIfToPositionIsNotFilled = board[0] & (1L << 64 - toPosition);
            }
            if (temp != 0) {
                if (temp > 64) {
                    checkIfCapturePositionIsFilled = board[3] & (1L << 64 - temp + 64);
                } else {
                    checkIfCapturePositionIsFilled = board[2] & (1L << 64 - temp);
                }
            }
        } else {
            if (fromPosition > 64) {
                checkIfFromPositionIsFilled = board[3] & (1L << 64 - fromPosition + 64);
            } else {
                checkIfFromPositionIsFilled = board[2] & (1L << 64 - fromPosition);
            }
            if (toPosition > 64) {
                checkIfToPositionIsNotFilled = board[3] & (1L << 64 - toPosition + 64);
            } else {
                checkIfToPositionIsNotFilled = board[2] & (1L << 64 - toPosition);
            }
            if (temp != 0) {
                if (temp > 64) {
                    checkIfCapturePositionIsFilled = board[1] & (1L << 64 - temp + 64);
                } else {
                    checkIfCapturePositionIsFilled = board[0] & (1L << 64 - temp);
                }
            }
        }
        return Long.bitCount(checkIfFromPositionIsFilled) == 1 && checkIfToPositionIsNotFilled == 0 && Long.bitCount(checkIfCapturePositionIsFilled) == 1;
    }
}
