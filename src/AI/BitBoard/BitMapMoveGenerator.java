package AI.BitBoard;

import GameEngine.MoveCommand;

/**
 * A board is represented as a long[4] with the first two entries being the two longs used for the player1 Bitmap and the second two longs used for the player2 Bitmap
 * A move is represented as either a long[4] or long[6] depending on if it is a capture move or not. It consists of Bitmaps with a single positions as 1. For no capturing moves the first 2 longs describe the position of the piece that gets moved and the second two bits are use for the landing position.
 * For a capturing move the first two longs get used for the position of the piece that gets moved the second two longs get used for the position of the piece that gets captured. And the third two longs are used for the landing position of the moved piece.
 */
public class BitMapMoveGenerator {


    public static int populateShortArrayWithCaptureMoves(long[] board, boolean isPlayerOne, short[] moveArray, int arrayDepthOffset) {
        int maxMovesAdded = 0;
        long[] playerBoard = new long[2];
        long[] opponentBoard = new long[2];

        // Inline the logic instead of branching:
        int index1 = isPlayerOne ? 0 : 2;
        int index2 = isPlayerOne ? 1 : 3;

        playerBoard[0] = board[index1];
        playerBoard[1] = board[index2];
        opponentBoard[0] = board[index1 ^ 2]; // Toggle between 0 <-> 2
        opponentBoard[1] = board[index2 ^ 2]; // Toggle between 1 <-> 3
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
        maxMovesAdded += populateShortArrayWithCapturePiecesBoard(capturePiecesEast, isPlayerOne, false, moveArray, arrayDepthOffset);

        maxMovesAdded += populateShortArrayWithCapturePiecesBoard(capturePiecesWest, isPlayerOne, true, moveArray, arrayDepthOffset);
        return maxMovesAdded;
    }


    private static int populateShortArrayWithCapturePiecesBoard(long[] capturePieces, boolean isPlayerOne, boolean captureWest, short[] moves, int arrayDepthOffset) {
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
                    if (captureWest) {
                        moves[arrayDepthOffset + movesAdded * 5] = move;
                    } else {
                        moves[arrayDepthOffset + movesAdded * 5 + 1] = move;
                    }
                    movesAdded++;
                }
                // Flip the lowest set bit to move on to the next piece
                temp &= (temp - 1);
            }
        }
        return movesAdded;
    }


    public static int populateShortArrayWithQuietMoves(long[] board, short[] moveArray, boolean isPlayerOne, int arrayOffset) {
        int maxMovesAdded = 0;
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
//        BitmapFianco.ShowBitBoard(canMoveWest);
        long[] canMoveForward;

        if (isPlayerOne) {
            canMoveForward = AdvancedBitOps.fasterPossibleSouthMovePieces(playerBoard, freeTileBitMask);
        } else {
            canMoveForward = AdvancedBitOps.fasterPossibleNorthMovePieces(playerBoard, freeTileBitMask);
        }
//        System.out.println("Can Move Forward:");
//        BitmapFianco.ShowBitBoard(canMoveForward);
        maxMovesAdded = Math.max(maxMovesAdded, populateShortArrayWithForwardMovesBoard(canMoveForward, isPlayerOne, moveArray, arrayOffset));
        maxMovesAdded = Math.max(maxMovesAdded, populateShortArrayWithSideMovesBoard(canMoveEast, isPlayerOne, false, moveArray, arrayOffset));
        maxMovesAdded = Math.max(maxMovesAdded, populateShortArrayWithSideMovesBoard(canMoveWest, isPlayerOne, true, moveArray, arrayOffset));
        return maxMovesAdded;

    }


    private static int populateShortArrayWithForwardMovesBoard(long[] movePieces, boolean isPlayerOne, short[] moves, int arrayOffset) {
        int movesAdded = 0;
        for (int i = 0; i < movePieces.length; i++) {
            long temp;
            if (isPlayerOne) {
                temp = movePieces[i == 0 ? 1 : 0];
            } else {
                temp = movePieces[i];
            }

            // For Player One: iterate from least significant bits
            if (isPlayerOne) {
                while (temp != 0) {
                    // Isolate the lowest set bit
                    int fromPosition;
                    if (i == 1) {
                        fromPosition = 64 - Long.numberOfTrailingZeros(temp);  // First part of the board (0-63)
                    } else if (i == 0) {
                        fromPosition = 64 - Long.numberOfTrailingZeros(temp) + 64;  // Second part of the board (64-127)
                    } else {
                        System.out.println("Error in populateQueueWithForwardMovesBoard Function!");
                        return movesAdded;
                    }

                    // Move south (downwards)
                    int toPosition = fromPosition + 9;

                    // Pack and add the move
                    short move = MoveConversion.pack(fromPosition, toPosition);
                    moves[arrayOffset + movesAdded * 5 + 2] = move;
                    movesAdded++;

                    // Flip the lowest set bit
                    temp &= (temp - 1);
                }
            }
            // For Player Two: iterate from most significant bits
            else {
                while (temp != 0) {
                    // Isolate the highest set bit
                    int fromPosition;
                    if (i == 0) {
                        fromPosition = Long.numberOfLeadingZeros(temp) + 1;  // First part of the board (0-63)
                    } else if (i == 1) {
                        fromPosition = 65 + Long.numberOfLeadingZeros(temp);  // Second part of the board (64-127)
                    } else {
                        System.out.println("Error in populateQueueWithForwardMovesBoard Function!");
                        return movesAdded;
                    }

                    // Move north (upwards)
                    int toPosition = fromPosition - 9;

                    // Pack and add the move
                    short move = MoveConversion.pack(fromPosition, toPosition);
                    moves[arrayOffset + movesAdded * 5 + 2] = move;
                    movesAdded++;

                    // Flip the highest set bit using Long.highestOneBit
                    temp &= ~(1L << (63 - Long.numberOfLeadingZeros(temp)));
                }
            }
        }
        return movesAdded;
    }


    private static int populateShortArrayWithSideMovesBoard(long[] movePieces, boolean isPlayerOne, boolean moveWest, short[] moves, int arrayOffset) {
        int movesAdded = 0;
        for (int i = 0; i < movePieces.length; i++) {
            long temp;
            if (isPlayerOne) {
                temp = movePieces[i == 0 ? 1 : 0];
            } else {
                temp = movePieces[i];
            }
            if (isPlayerOne) {
                // Iterate through the set bits (pieces) in movePieces[i]
                while (temp != 0) {
                    // Isolate the lowest set bit and get the fromPosition
                    int fromPosition;
                    if (i == 1) {
                        fromPosition = 64 - Long.numberOfTrailingZeros(temp);  // For the first part of the board (0-63)
                    } else if (i == 0) {
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
                    if (moveWest) {
                        moves[arrayOffset + movesAdded * 5 + 3] = move;
                    } else {
                        moves[arrayOffset + movesAdded * 5 + 4] = move;
                    }
                    movesAdded++;
                    // Flip the lowest set bit to move on to the next piece
                    temp &= (temp - 1);
                }
            } else {
                // Iterate through the set bits (pieces) in movePieces[i]
                while (temp != 0) {
                    // Isolate the lowest set bit and get the fromPosition
                    int fromPosition;
                    if (i == 0) {
                        fromPosition = Long.numberOfLeadingZeros(temp) + 1;  // First part of the board (0-63)
                    } else if (i == 1) {
                        fromPosition = 65 + Long.numberOfLeadingZeros(temp);  // Second part of the board (64-127)
                    } else {
                        System.out.println("Error in populateQueueWithForwardMovesBoard Function!");
                        return movesAdded;
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
                    if (moveWest) {
                        moves[arrayOffset + movesAdded * 5 + 3] = move;
                    } else {
                        moves[arrayOffset + movesAdded * 5 + 4] = move;
                    }
                    movesAdded++;
                    // Flip the highest set bit using Long.highestOneBit
                    temp &= ~(1L << (63 - Long.numberOfLeadingZeros(temp)));
                }
            }

        }
        return movesAdded;
    }


    public static int populateShortArrayWithAllPossibleMoves(long[] board, boolean isPlayerOne, short[] moveArray, int arrayOffset) {
        int maxMovesAdded = 0;
        maxMovesAdded = Math.max(maxMovesAdded, populateShortArrayWithCaptureMoves(board, isPlayerOne, moveArray, arrayOffset));
        if (maxMovesAdded == 0) {
            maxMovesAdded = Math.max(maxMovesAdded, populateShortArrayWithQuietMoves(board, moveArray, isPlayerOne, arrayOffset));
        }
        return maxMovesAdded;

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
                checkIfToPositionIsNotFilled = (board[1] | board[3]) & (1L << 64 - toPosition + 64);
            } else {
                checkIfToPositionIsNotFilled = (board[0] | board[2]) & (1L << 64 - toPosition);
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
                checkIfToPositionIsNotFilled = (board[3] | board[1]) & (1L << 64 - toPosition + 64);
            } else {
                checkIfToPositionIsNotFilled = (board[2] | board[0]) & (1L << 64 - toPosition);
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


    public static boolean checkIfConversionMove(short move) {
        int from = MoveConversion.unpackFirstNumber(move);
        int diff = from - MoveConversion.unpackSecondNumber(move);
        switch (diff) {
            case 1:
                switch (from) {
                    case 10, 19, 28, 37, 46, 55, 64, 73:
                        return true;
                }
            case -1:
                switch (from) {
                    case 9, 18, 27, 36, 45, 54, 63, 72:
                        return true;
                }
        }
        if (diff > 1) {
            return true;
        } else if (diff < -1) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isCaptureMove(short move) {
        int diff = MoveConversion.unpackFirstNumber(move) - MoveConversion.unpackSecondNumber(move);
        return diff > 9 || diff < -9;
    }

    public static void main(String[] args) {
        short[] possibleP1Moves = new short[300];
        short[] possibleP2Moves = new short[300];
        int[][] boardState = new int[][]{
                {1, 1, 1, 1, 1, 1, 1, 1, 1},
                {0, 1, 0, 0, 0, 0, 0, 1, 0},
                {0, 0, 1, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 1, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 2, 0, 2, 0, 0, 0},
                {0, 0, 2, 0, 0, 0, 2, 0, 0},
                {0, 2, 0, 0, 0, 0, 0, 2, 0},
                {2, 2, 2, 2, 2, 2, 2, 2, 2}};

        BitmapFianco bitmapFianco = new BitmapFianco();
        bitmapFianco.populateBoardBitmapsFrom2DIntArray(boardState);
        long[] board = bitmapFianco.getFullBoard();
        System.out.println(bitmapFianco);
        populateShortArrayWithAllPossibleMoves(board, true, possibleP1Moves, 0);
        populateShortArrayWithAllPossibleMoves(board, false, possibleP2Moves, 0);
        System.out.println("Show all Possible Player 1 Moves ");
        printPossibleMoves(possibleP1Moves, 30, true);
        System.out.println("Show all Possible Player 2 Moves ");
        printPossibleMoves(possibleP2Moves, 30, false);

    }

    public static void printPossibleMoves(short[] pvLine, int depth, boolean isPlayerOne) {
        for (int i = 0; i < depth; i++) {
            short move = pvLine[i];
            if (move != 0) {
                MoveCommand moveCommand = MoveConversion.getMoveCommandFromShortMove(move, isPlayerOne);
                System.out.print(moveCommand + "; ");
            } else {
                System.out.print(move + "; ");
            }
        }
    }
}
