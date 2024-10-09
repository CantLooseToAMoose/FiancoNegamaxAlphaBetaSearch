package search.AlphaBeta;

import BitBoard.BitMapMoveGenerator;
import BitBoard.BitmapFianco;
import BitBoard.MoveConversion;
import FiancoGameEngine.MoveCommand;
import search.Evaluation.Evaluate;
import search.TT.TranspositionTable;
import search.TT.TranspositionTableEntry;
import search.TT.Zobrist;

import java.util.concurrent.atomic.AtomicInteger;

public class SequentialAlphaBetaSearch {
    //Constants
    public static final int MAX_NUMBER_OF_MOVES = 15 * 4;
    public static final int MAX_NUMBER_OF_ACTUAL_DEPTH = 50;
    public static final int MAX_NUMBER_OF_MOVES_SINCE_LAST_CONVERSION = 15;
    public static final int PRIMARY_TRANSPOSITION_TABLE_SIZE = 16_384;
//    public static final int TRANSPOSITION_TABLE_SIZE = 134_217_728 * 4; //16 GB\

    //    public static final int TRANSPOSITION_TABLE_SIZE = 134_217_728*2;//8 GB
//    public static final int TRANSPOSITION_TABLE_SIZE = 134_217_728;//4 GB
    public static final int TRANSPOSITION_TABLE_SIZE = 134_217_728 / 2; //2GB;

    public static final int WIN_EVAL = 300;
    public static final int DRAW_EVAL = -15;
    //For Debugging
    public AtomicInteger nodes = new AtomicInteger(0);
    //For Repetition Detection
    private long[] boardHistory = new long[MAX_NUMBER_OF_ACTUAL_DEPTH + MAX_NUMBER_OF_MOVES_SINCE_LAST_CONVERSION];
    private long zobristHash = 0L;
    private int[][] lastBoard;
    private int gameMoves = 0;

    //Transposition Table
    private final TranspositionTableEntry[] primaryTranspositionTable = new TranspositionTableEntry[PRIMARY_TRANSPOSITION_TABLE_SIZE];
    private final TranspositionTableEntry[] transpositionTable = new TranspositionTableEntry[TRANSPOSITION_TABLE_SIZE];

    private boolean checkIfConversionMove(short move) {
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


    //For History
    public void updateBoardHistory(int[][] lastBoard, int[][] newBoard, boolean isPlayerOne) {

        MoveCommand moveCommand = MoveCommand.CreateMoveCommandFromConsecutiveBoardStates(lastBoard, newBoard, isPlayerOne ? 1 : 2);
        if (moveCommand != null) {
            short move = moveCommand.toShort();
            if (checkIfConversionMove(move)) {
                boardHistory = new long[MAX_NUMBER_OF_ACTUAL_DEPTH + MAX_NUMBER_OF_MOVES_SINCE_LAST_CONVERSION];
                gameMoves = 0;
            }
            updateBoardHistory(move, true, 0, isPlayerOne);
            this.lastBoard = newBoard;
        } else {
            System.out.println("Something went wrong when trying to generate a short move from the previous board state");
        }
    }

    private void updateBoardHistory(short move, boolean doMove, int actualDepth, boolean isPlayerOne) {
        zobristHash = Zobrist.updateHash(zobristHash, move, isPlayerOne);
        if (doMove) {
            boardHistory[gameMoves + actualDepth] = zobristHash;
        } else {
            boardHistory[gameMoves + actualDepth] = zobristHash;
        }
    }

    //Alpha Beta
    public int AlphaBeta(long[] board, boolean isPlayerOneTurn, int depth, int actualDepth, int lastConversionMove, short[] moveArray, int alpha, int beta) {
        //Debugging
        nodes.incrementAndGet();
        //Transposition Table
        int oldAlpha = alpha;
        TranspositionTableEntry entry = TranspositionTable.retrieve(primaryTranspositionTable, transpositionTable, zobristHash, PRIMARY_TRANSPOSITION_TABLE_SIZE, TRANSPOSITION_TABLE_SIZE,depth);
        short ttMove = 0;
        boolean store = false;
        if (entry == null) {
            store = true;
        } else {
            //if entry exists
            if (zobristHash != entry.hash || !BitMapMoveGenerator.isMoveValid(board, entry.bestMove, isPlayerOneTurn)) {
                store = true;
            } else {
                //the entry is of the same state
                if (depth > entry.depth) {
                    ttMove = entry.bestMove;
                    store = true;
                } else {
                    if (entry.type == 0) {
                        return entry.score;
                    } else if (entry.type == 1) {
                        alpha = Math.max(oldAlpha, entry.score);
                    } else if (entry.type == 2) {
                        beta = Math.min(beta, entry.score);
                    }
                    if (alpha >= beta) {
                        return entry.score;
                    }
                    ttMove = entry.bestMove;
                }
            }
        }
        //Populate Move array
        int numberOfMoves = BitMapMoveGenerator.populateShortArrayWithAllPossibleMoves(board, isPlayerOneTurn, moveArray, actualDepth * MAX_NUMBER_OF_MOVES);
        int win = Evaluate.evaluateWin(board, isPlayerOneTurn);
        //Check for win
        if (win != 0) {
            return (WIN_EVAL + depth) * win;
        }
        // Check for no more depth
        if (depth == 0) {
            return Evaluate.combinedEvaluate(board, isPlayerOneTurn);
        }
        //Check for no more moves
        if (numberOfMoves == 0) {
            return -(WIN_EVAL + depth);
        }
        //Check for draw
        byte boardRep = 0;
        for (int i = lastConversionMove + (gameMoves + actualDepth) % 2; i < gameMoves + actualDepth; i = i + 2) {
            if (zobristHash == boardHistory[i]) {
                boardRep++;
            }
        }
        //If this boardstate has been seen more than 2 times it is a draw
        if (boardRep > 1) {
            return DRAW_EVAL;
        }
        short bestMove = 0;


        int score = -Integer.MAX_VALUE;
        for (int i = -1; i < numberOfMoves; i++) {
            short move;
            // try out transposition table move first
            if (i == -1) {
                if (ttMove == 0) {
                    continue;
                }
                move = ttMove;
            } else {
                //get Move from move Array
                move = moveArray[actualDepth * MAX_NUMBER_OF_MOVES + i];
            }
            //dont search for the ttMove again
            if (move == ttMove) {
                continue;
            }
            //Do Move and add to history
            if (checkIfConversionMove(move)) {
                lastConversionMove = gameMoves;
            }
            updateBoardHistory(move, true, actualDepth, isPlayerOneTurn);
            BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, move, isPlayerOneTurn);
            //Go deeper
            int value = -AlphaBeta(board, !isPlayerOneTurn, depth - 1, actualDepth + 1, lastConversionMove, moveArray, -beta, -alpha);
            //Higher again undo move
            BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, move, isPlayerOneTurn);

            if (value > score) {
                bestMove = move;
                score = value;
            }
            if (score >= alpha) {
                alpha = score;
            }
            if (score >= beta) break;
        }
        byte type = 0;
        if (score <= oldAlpha) {
            type = 2;
        } else if (score >= beta) {
            type = 1;
        }
        if (store) {
            TranspositionTable.store(primaryTranspositionTable, transpositionTable, zobristHash, PRIMARY_TRANSPOSITION_TABLE_SIZE, TRANSPOSITION_TABLE_SIZE, depth, score, type, bestMove);
        }
        return score;
    }

    public short GetBestAlphaBetaMove(long[] board, boolean isPlayerOne, int depth, int alpha, int beta) {
//      TODO: Can you Parallelize this?
        //Update History with the new Board received
        if (this.lastBoard != null) {
            updateBoardHistory(lastBoard, new BitmapFianco(board).convertBitmapTo2DIntArray(), !isPlayerOne);
        } else {
            this.lastBoard = new BitmapFianco(board).convertBitmapTo2DIntArray();
        }
        System.out.println("Evaluation of this Position: " + Evaluate.combinedEvaluate(board, isPlayerOne));
        //For debugging:
        long startTime = System.nanoTime();
        nodes.set(0);

        //For all the moves
        short[] moveArray = new short[MAX_NUMBER_OF_MOVES * MAX_NUMBER_OF_ACTUAL_DEPTH];
        int actualDepth = 0;
        //For getting the best move
        long[] bestBoard = null;
        short bestMove = 0;
        int bestScore = -Integer.MAX_VALUE;
        //Populating move array
        int numberOfMoves = BitMapMoveGenerator.populateShortArrayWithAllPossibleMoves(board, isPlayerOne, moveArray, actualDepth);
        for (int i = 0; i < numberOfMoves; i++) {
            short move = moveArray[i];
            //Doing move and adding the new board state to the history
            updateBoardHistory(move, true, 0, isPlayerOne);
            BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, move, isPlayerOne);
            int value = -AlphaBeta(board, !isPlayerOne, depth - 1, actualDepth + 1, gameMoves, moveArray, -beta, -alpha);
            if (value > bestScore) {
                bestScore = value;
                bestMove = move;
                bestBoard = board.clone();
            }
            //Undoing the move and removing it from the history
            updateBoardHistory(move, false, 0, isPlayerOne);
            BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, move, isPlayerOne);
            if (bestScore > alpha) {
                alpha = bestScore;
            }
            if (bestScore >= beta) break;
        }
        long endTime = System.nanoTime();
        System.out.println("Nodes: " + nodes);
        System.out.println("Time: " + (endTime - startTime) / 1_000_000_000.0 + "s");
        System.out.println("Nodes per second:" + nodes.get() / ((endTime - startTime) / 1_000_000_000.0));
        System.out.println("Bestscore:" + bestScore);
        //Updating the board state history
        updateBoardHistory(lastBoard, new BitmapFianco(bestBoard).convertBitmapTo2DIntArray(), isPlayerOne);
        return bestMove;
    }
}