package BitBoard;

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
        populateQueueWithCapturePiecesBoard(capturePiecesEast, playerBoard, opponentBoard, isPlayerOne, false, moveQueue);
        populateQueueWithCapturePiecesBoard(capturePiecesWest, playerBoard, opponentBoard, isPlayerOne, true, moveQueue);
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
        populateQueueWithCapturePiecesBoard(capturePiecesEast, playerBoard, opponentBoard, isPlayerOne, false, moveQueue);
        populateQueueWithCapturePiecesBoard(capturePiecesWest, playerBoard, opponentBoard, isPlayerOne, true, moveQueue);
    }

    /**
     * Helping Method for populating the Queue with the correct new BoardStates after generating all possible Pieces for capturing in a specific Direction
     *
     * @param capturePieces the board of the player, with only the pieces that can capture in the specified direction
     * @param playerBoard   the board of the player
     * @param opponentBoard the board of the opponent
     * @param isPlayerOne   boolean if player is player one or two
     * @param captureWest   checks wether the capture is in direction west or east
     * @param moves         the queue for the moves which holds the new boardstates after the move
     */
    private static void populateQueueWithCapturePiecesBoard(long[] capturePieces, long[] playerBoard, long[] opponentBoard, boolean isPlayerOne, boolean captureWest, Queue<long[]> moves) {
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
        long[] freeTileBitMask = BitMaskCreationHelper.getFreeTilesBitMask(playerBoard, opponentBoard);
        long[] canMoveEast = AdvancedBitOps.fasterPossibleEastMovePieces(playerBoard, freeTileBitMask);
        long[] canMoveWest = AdvancedBitOps.fasterPossibleWestMovePieces(playerBoard, freeTileBitMask);
        long[] canMoveForward;

        if (isPlayerOne) {
            canMoveForward = AdvancedBitOps.fasterPossibleEastMovePieces(playerBoard, freeTileBitMask);
        } else {
            canMoveForward = AdvancedBitOps.fasterPossibleNorthMovePieces(playerBoard, freeTileBitMask);
        }
        populateQueueWithForwardMovesBoard(canMoveForward, playerBoard, opponentBoard, isPlayerOne, moveQueue);
        populateQueueWithSideMovesBoard(canMoveEast, playerBoard, opponentBoard, isPlayerOne, false, moveQueue);
        populateQueueWithSideMovesBoard(canMoveWest, playerBoard, opponentBoard, isPlayerOne, true, moveQueue);

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

    public static LinkedList<long[]> createQueueWithAllPossibleMoves(long[] board, boolean isPlayerOne) {
        LinkedList<long[]> moveQueue = new LinkedList<long[]>();
        populateQueueWithCaptureMoves(board, moveQueue, isPlayerOne);
        if (moveQueue.isEmpty()) {
            populateQueueWithQuietMoves(board, moveQueue, isPlayerOne);
        }
        return moveQueue;
    }

    public static LinkedList<long[]> fasterCreateQueueWithAllPossibleMoves(long[] board, boolean isPlayerOne) {
        LinkedList<long[]> moveQueue = new LinkedList<long[]>();
        fasterPopulateQueueWithCaptureMoves(board, moveQueue, isPlayerOne);
        if (moveQueue.isEmpty()) {
            fasterPopulateQueueWithQuietMoves(board, moveQueue, isPlayerOne);
        }
        return moveQueue;
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
}
