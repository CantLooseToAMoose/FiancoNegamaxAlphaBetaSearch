package BitBoard;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A board is represented as a long[4] with the first two entries being the two longs used for the player1 Bitmap and the second two longs used for the player2 Bitmap
 * A move is represented as either a long[4] or long[6] depending on if it is a capture move or not. It consists of Bitmaps with a single positions as 1. For no capturing moves the first 2 longs describe the position of the piece that gets moved and the second two bits are use for the landing position.
 * For a capturing move the first two longs get used for the position of the piece that gets moved the second two longs get used for the position of the piece that gets captured. And the third two longs are used for the landing position of the moved piece.
 */
public class BitMapMoveGenerator {

    /**
     * Generates all possible Capture Moves for a given BoardState as new BoardStates and populates a queue with this possible moves
     *
     * @param board       Long array of size 4 with first 2 arrays holding the board of player 1 and the last 2 arrays holding the board of player 2
     * @param moveQueue   Queue of possible moves saved as new Boardstates
     * @param isPlayerOne true if player is playerOne false if player is playerTwo
     * @return Queue of Long arrays of size 4 with first 2 arrays holding the board of player 1 and the last 2 arrays holding the board of player 2
     */
    private static void populateQueueWithCaptureMoves(long[] board, Queue<long[]> moveQueue, boolean isPlayerOne) {
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
        if (isPlayerOne) {
            capturePiecesEast = AdvancedBitOps.possibleSouthEastMovePieces(playerBoard, opponentBoard);
            capturePiecesWest = AdvancedBitOps.possibleSouthWestMovePieces(playerBoard, opponentBoard);
        } else {
            capturePiecesEast = AdvancedBitOps.possibleNorthEastMovePieces(playerBoard, opponentBoard);
            capturePiecesWest = AdvancedBitOps.possibleNorthWestMovePieces(playerBoard, opponentBoard);
        }
        populateQueueWithCapturePiecesBoard(capturePiecesEast, isPlayerOne, false, moveQueue);
        populateQueueWithCapturePiecesBoard(capturePiecesWest, isPlayerOne, true, moveQueue);
    }

    private static void fasterPopulateQueueWithCaptureMoves(long[] board, Queue<long[]> moveQueue, boolean isPlayerOne) {
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
        populateQueueWithCapturePiecesBoard(capturePiecesEast, isPlayerOne, false, moveQueue);
        populateQueueWithCapturePiecesBoard(capturePiecesWest, isPlayerOne, true, moveQueue);
    }

    private static void fasterPopulateShortQueueWithCaptureMoves(long[] board, Queue<Short> moveQueue, boolean isPlayerOne) {
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
        populateShortQueueWithCapturePiecesBoard(capturePiecesEast, isPlayerOne, false, moveQueue);
        populateShortQueueWithCapturePiecesBoard(capturePiecesWest, isPlayerOne, true, moveQueue);
    }

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

    /**
     * Helping Method for populating the Queue with the correct new BoardStates after generating all possible Pieces for capturing in a specific Direction
     *
     * @param capturePieces the board of the player, with only the pieces that can capture in the specified direction
     * @param isPlayerOne   boolean if player is player one or two
     * @param captureWest   checks wether the capture is in direction west or east
     * @param moves         the queue for the moves which holds the new boardstates after the move
     */
    private static void populateQueueWithCapturePiecesBoard(long[] capturePieces, boolean isPlayerOne, boolean captureWest, Queue<long[]> moves) {
        for (int i = 0; i < capturePieces.length; i++) {
            long temp = capturePieces[i];
            while (temp != 0) {
                long lowestSetBit = temp & -temp;    // Isolate the lowest set bit
                long[] isolatedPieceOnBoard;
                if (i == 0) {
                    isolatedPieceOnBoard = new long[]{lowestSetBit, 0L};
                } else if (i == 1) {
                    isolatedPieceOnBoard = new long[]{0L, lowestSetBit};
                } else {
                    System.out.println("There is something wrong in the populateQueueWithCapturePiecesBoard Function, dont trust it!");
                    isolatedPieceOnBoard = new long[2];
                }
                long[] opponentPiece;
                long[] playerLandingSpot;
                if (isPlayerOne) {
                    if (captureWest) {
                        opponentPiece = BasicBitOps.shiftSouthWest(isolatedPieceOnBoard, 1);
                        playerLandingSpot = BasicBitOps.shiftSouthWest(isolatedPieceOnBoard, 2);
                    } else {
                        opponentPiece = BasicBitOps.shiftSouthEast(isolatedPieceOnBoard, 1);
                        playerLandingSpot = BasicBitOps.shiftSouthEast(isolatedPieceOnBoard, 2);
                    }
                } else {
                    if (captureWest) {
                        opponentPiece = BasicBitOps.shiftNorthWest(isolatedPieceOnBoard, 1);
                        playerLandingSpot = BasicBitOps.shiftNorthWest(isolatedPieceOnBoard, 2);
                    } else {
                        opponentPiece = BasicBitOps.shiftNorthEast(isolatedPieceOnBoard, 1);
                        playerLandingSpot = BasicBitOps.shiftNorthEast(isolatedPieceOnBoard, 2);
                    }

                }
                moves.add(new long[]{isolatedPieceOnBoard[0], isolatedPieceOnBoard[1], opponentPiece[0], opponentPiece[1], playerLandingSpot[0], playerLandingSpot[1]});
                temp &= (temp - 1);                 // Clear the lowest set bit
            }
        }
    }


    private static void populateShortQueueWithCapturePiecesBoard(long[] capturePieces, boolean isPlayerOne, boolean captureWest, Queue<Short> moves) {
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
                    return;  // Exit the function if something is wrong
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
                    moves.add(move);
                }
                // Flip the lowest set bit to move on to the next piece
                temp &= (temp - 1);
            }
        }
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


    private static void populateQueueWithQuietMoves(long[] board, Queue<long[]> moveQueue, boolean isPlayerOne) {
        long[] playerBoard;
        long[] opponentBoard;
        if (isPlayerOne) {
            playerBoard = new long[]{board[0], board[1]};
            opponentBoard = new long[]{board[2], board[3]};
        } else {
            playerBoard = new long[]{board[2], board[3]};
            opponentBoard = new long[]{board[0], board[1]};
        }
        long[] canMoveEast = AdvancedBitOps.possibleEastMovePieces(playerBoard, opponentBoard);
        long[] canMoveWest = AdvancedBitOps.possibleWestMovePieces(playerBoard, opponentBoard);
        long[] canMoveForward;

        if (isPlayerOne) {
            canMoveForward = AdvancedBitOps.possibleSouthMovePieces(playerBoard, opponentBoard);
        } else {
            canMoveForward = AdvancedBitOps.possibleNorthMovePieces(playerBoard, opponentBoard);
        }
        populateQueueWithForwardMovesBoard(canMoveForward, playerBoard, opponentBoard, isPlayerOne, moveQueue);
        populateQueueWithSideMovesBoard(canMoveEast, playerBoard, opponentBoard, isPlayerOne, false, moveQueue);
        populateQueueWithSideMovesBoard(canMoveWest, playerBoard, opponentBoard, isPlayerOne, true, moveQueue);

    }

    private static void fasterPopulateQueueWithQuietMoves(long[] board, Queue<long[]> moveQueue, boolean isPlayerOne) {
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
        populateQueueWithForwardMovesBoard(canMoveForward, playerBoard, opponentBoard, isPlayerOne, moveQueue);
        populateQueueWithSideMovesBoard(canMoveEast, playerBoard, opponentBoard, isPlayerOne, false, moveQueue);
        populateQueueWithSideMovesBoard(canMoveWest, playerBoard, opponentBoard, isPlayerOne, true, moveQueue);
    }


    private static void fasterPopulateShortQueueWithQuietMoves(long[] board, Queue<Short> moveQueue, boolean isPlayerOne) {
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
        populateShortQueueWithForwardMovesBoard(canMoveForward, isPlayerOne, moveQueue);
        populateShortQueueWithSideMovesBoard(canMoveEast, false, moveQueue);
        populateShortQueueWithSideMovesBoard(canMoveWest, true, moveQueue);
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

    private static void populateQueueWithForwardMovesBoard(long[] movePieces, long[] playerBoard, long[] opponentBoard, boolean isPlayerOne, Queue<long[]> moves) {
        for (int i = 0; i < movePieces.length; i++) {
            long temp = movePieces[i];
            while (temp != 0) {
                long lowestSetBit = temp & -temp;    // Isolate the lowest set bit
                long[] isolatedPieceOnBoard;
                if (i == 0) {
                    isolatedPieceOnBoard = new long[]{lowestSetBit, 0L};
                } else if (i == 1) {
                    isolatedPieceOnBoard = new long[]{0L, lowestSetBit};
                } else {
                    System.out.println("There is something wrong in the populateQueueWithForwardMovesBoard Function, dont trust it!");
                    isolatedPieceOnBoard = new long[2];
                }
                long[] playerLandingSpot;
                if (isPlayerOne) {
                    playerLandingSpot = BasicBitOps.shiftSouth(isolatedPieceOnBoard, 1);
                } else {
                    playerLandingSpot = BasicBitOps.shiftNorth(isolatedPieceOnBoard, 1);
                }
                moves.add(new long[]{isolatedPieceOnBoard[0], isolatedPieceOnBoard[1], playerLandingSpot[0], playerLandingSpot[1]});
                temp &= (temp - 1);                 // Clear the lowest set bit
            }
        }
    }

    private static void populateShortQueueWithForwardMovesBoard(long[] movePieces, boolean isPlayerOne, Queue<Short> moves) {
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
                    return;  // Exit the function if something is wrong
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
                moves.add(move);
                // Flip the lowest set bit to move on to the next piece
                temp &= (temp - 1);
            }
        }
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


    private static void populateQueueWithSideMovesBoard(long[] movePieces, long[] playerBoard, long[] opponentBoard, boolean isPlayerOne, boolean moveWest, Queue<long[]> moves) {
        for (int i = 0; i < movePieces.length; i++) {
            long temp = movePieces[i];
            while (temp != 0) {
                long lowestSetBit = temp & -temp;    // Isolate the lowest set bit
                long[] isolatedPieceOnBoard;
                if (i == 0) {
                    isolatedPieceOnBoard = new long[]{lowestSetBit, 0L};
                } else if (i == 1) {
                    isolatedPieceOnBoard = new long[]{0L, lowestSetBit};
                } else {
                    System.out.println("There is something wrong in the populateQueueWithSideMovesBoard Function, dont trust it!");
                    isolatedPieceOnBoard = new long[2];
                }
                long[] playerLandingSpot;
                if (moveWest) {
                    playerLandingSpot = BasicBitOps.shiftWest(isolatedPieceOnBoard, 1);
                } else {
                    playerLandingSpot = BasicBitOps.shiftEast(isolatedPieceOnBoard, 1);
                }
                moves.add(new long[]{isolatedPieceOnBoard[0], isolatedPieceOnBoard[1], playerLandingSpot[0], playerLandingSpot[1]});
                temp &= (temp - 1);                 // Clear the lowest set bit
            }
        }
    }

    private static void populateShortQueueWithSideMovesBoard(long[] movePieces, boolean moveWest, Queue<Short> moves) {
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
                    return;  // Exit the function if something is wrong
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
                moves.add(move);
                // Flip the lowest set bit to move on to the next piece
                temp &= (temp - 1);
            }
        }
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


    public static LinkedList<long[]> createQueueWithAllPossibleMoves(long[] board, boolean isPlayerOne) {
        LinkedList<long[]> moveQueue = new LinkedList<long[]>();
        populateQueueWithCaptureMoves(board, moveQueue, isPlayerOne);
        if (moveQueue.isEmpty()) {
            populateQueueWithQuietMoves(board, moveQueue, isPlayerOne);
        }
        return moveQueue;
    }

    public static ArrayDeque<long[]> fasterCreateQueueWithAllPossibleMoves(long[] board, boolean isPlayerOne) {
        ArrayDeque<long[]> moveQueue = new ArrayDeque<>();
        fasterPopulateQueueWithCaptureMoves(board, moveQueue, isPlayerOne);
        if (moveQueue.isEmpty()) {
            fasterPopulateQueueWithQuietMoves(board, moveQueue, isPlayerOne);
        }
        return moveQueue;
    }


    public static ArrayDeque<Short> fasterCreateShortQueueWithAllPossibleMoves(long[] board, boolean isPlayerOne) {
        ArrayDeque<Short> moveQueue = new ArrayDeque<>();
        fasterPopulateShortQueueWithCaptureMoves(board, moveQueue, isPlayerOne);
        if (moveQueue.isEmpty()) {
            fasterPopulateShortQueueWithQuietMoves(board, moveQueue, isPlayerOne);
        }
        return moveQueue;
    }

    public static int populateShortArrayWithAllPossibleMoves(long[] board, boolean isPlayerOne, short[] moveArray, int arrayOffset) {
        int movesAdded = 0;
        movesAdded += populateShortArrayWithCaptureMoves(board, isPlayerOne, moveArray, arrayOffset);
        if (movesAdded == 0) {
            movesAdded += populateShortArrayWithQuietMoves(board, moveArray, isPlayerOne, arrayOffset + movesAdded);
        }
        return movesAdded;

    }

    public static long[] createNewBoardStateFromMove(long[] board, long[] move, boolean isPlayerOne) {
        long[] updatedPlayerBoard;
        long[] updatedOpponentBoard;
        if (isPlayerOne) {
            updatedPlayerBoard = new long[]{board[0], board[1]};
            updatedOpponentBoard = new long[]{board[2], board[3]};
        } else {
            updatedPlayerBoard = new long[]{board[2], board[3]};
            updatedOpponentBoard = new long[]{board[0], board[1]};
        }
        long[] fromPieceBoard;
        long[] toPieceBoard;
        long[] capturePieceBoard;
        if (move.length == 4) {
            fromPieceBoard = new long[]{move[0], move[1]};
            toPieceBoard = new long[]{move[2], move[3]};

            updatedPlayerBoard = BasicBitOps.XOR(updatedPlayerBoard, fromPieceBoard);
            updatedPlayerBoard = BasicBitOps.XOR(updatedPlayerBoard, toPieceBoard);

        } else if (move.length == 6) {
            fromPieceBoard = new long[]{move[0], move[1]};
            capturePieceBoard = new long[]{move[2], move[3]};
            toPieceBoard = new long[]{move[4], move[5]};
            updatedPlayerBoard = BasicBitOps.XOR(updatedPlayerBoard, fromPieceBoard);
            updatedPlayerBoard = BasicBitOps.XOR(updatedPlayerBoard, toPieceBoard);
            updatedOpponentBoard = BasicBitOps.XOR(updatedOpponentBoard, capturePieceBoard);
        }
        if (isPlayerOne) {
            return new long[]{updatedPlayerBoard[0], updatedPlayerBoard[1], updatedOpponentBoard[0], updatedOpponentBoard[1]};
        } else {
            return new long[]{updatedOpponentBoard[0], updatedOpponentBoard[1], updatedPlayerBoard[0], updatedPlayerBoard[1]};
        }
    }

    public static void makeOrUnmakeMoveInPlace(long[] board, long[] move, boolean isPlayerOne) {
        if (isPlayerOne) {
            if (move.length == 4) {
                board[0] = board[0] ^ move[0];
                board[1] = board[1] ^ move[1];
                board[0] = board[0] ^ move[2];
                board[1] = board[1] ^ move[3];
            } else if (move.length == 6) {
                board[0] = board[0] ^ move[0];
                board[1] = board[1] ^ move[1];
                board[0] = board[0] ^ move[4];
                board[1] = board[1] ^ move[5];
                board[2] = board[2] ^ move[2];
                board[3] = board[3] ^ move[3];
            }
        } else {
            if (move.length == 4) {
                board[2] = board[2] ^ move[0];
                board[3] = board[3] ^ move[1];
                board[2] = board[2] ^ move[2];
                board[3] = board[3] ^ move[3];
            } else if (move.length == 6) {
                board[2] = board[2] ^ move[0];
                board[3] = board[3] ^ move[1];
                board[2] = board[2] ^ move[4];
                board[3] = board[3] ^ move[5];
                board[0] = board[0] ^ move[2];
                board[1] = board[1] ^ move[3];
            }
        }
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
            default -> 0;
        };
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
