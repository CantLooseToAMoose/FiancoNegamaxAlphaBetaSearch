package search;

import BitBoard.BitMapMoveGenerator;
import BitBoard.BitmapFianco;
import BitBoard.MoveConversion;
import FiancoGameEngine.MoveCommand;
import search.TT.TranspositionTable;
import search.TT.TranspositionTableEntry;
import search.TT.Zobrist;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AlphaBetaSearch {
    //Constants
    public static final int MAX_NUMBER_OF_MOVES = 15 * 4;
    public static final int MAX_NUMBER_OF_ACTUAL_DEPTH = 50;
    public static final int MAX_NUMBER_OF_MOVES_SINCE_LAST_CONVERSION = 15;
    public static final int TRANSPOSITION_TABLE_SIZE = 134_217_728 * 5; //20 GB\

//    public static final int TRANSPOSITION_TABLE_SIZE = 134_217_728*2;//8 GB
//    public static final int TRANSPOSITION_TABLE_SIZE = 134_217_728;//4 GB
//    public static final int TRANSPOSITION_TABLE_SIZE = 134_217_728 /2 GB;

    public static final int WIN_EVAL = 300;
    public static final int DRAW_EVAL = -15;
    //For Debugging
    public AtomicInteger nodes = new AtomicInteger(0);
    //For Repetition Detection
    private long[] boardHistory = new long[MAX_NUMBER_OF_ACTUAL_DEPTH + MAX_NUMBER_OF_MOVES_SINCE_LAST_CONVERSION];
    private Zobrist zobrist = new Zobrist();
    private int[][] lastBoard;
    private int gameMoves = 0;

    //Transposition Table
    private final TranspositionTableEntry[] table = new TranspositionTableEntry[TRANSPOSITION_TABLE_SIZE];

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
        zobrist.updateHash(move, isPlayerOne);
        if (doMove) {
            boardHistory[gameMoves + actualDepth] = zobrist.getZobristHash();
        } else {
            boardHistory[gameMoves + actualDepth] = zobrist.getZobristHash();
        }
    }

    //Alpha Beta
    public int AlphaBeta(long[] board, boolean isPlayerOneTurn, int depth, int actualDepth, int lastConversionMove, short[] moveArray, int alpha, int beta) {
        //Debugging
        nodes.incrementAndGet();
        //Transposition Table
        long zobristHash = zobrist.getZobristHash();
        int oldAlpha = alpha;
        TranspositionTableEntry entry = TranspositionTable.retrieve(table, zobristHash, TRANSPOSITION_TABLE_SIZE);
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
            TranspositionTable.store(table, zobristHash, TRANSPOSITION_TABLE_SIZE, depth, score, type, bestMove);
        }
        return score;
    }

    public long[] GetBestAlphaBetaMove(long[] board, boolean isPlayerOne, int depth, int alpha, int beta) {
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
        System.out.println("Score:" + bestScore);
        //Updating the board state history
        updateBoardHistory(lastBoard, new BitmapFianco(bestBoard).convertBitmapTo2DIntArray(), isPlayerOne);
        return bestBoard;
    }

    public long[] GetBestAlphaBetaMoveParallel(long[] board, boolean isPlayerOne, int depth, int alpha, int beta) {
        // Update History with the new Board received
        if (this.lastBoard != null) {
            updateBoardHistory(lastBoard, new BitmapFianco(board).convertBitmapTo2DIntArray(), !isPlayerOne);
        } else {
            this.lastBoard = new BitmapFianco(board).convertBitmapTo2DIntArray();
        }
        System.out.println("Evaluation of this Position: " + Evaluate.combinedEvaluate(board, isPlayerOne));

        long startTime = System.nanoTime();
        nodes.set(0);

        // Move array and depth initialization
        short[] moveArray = new short[MAX_NUMBER_OF_MOVES * MAX_NUMBER_OF_ACTUAL_DEPTH];
        int actualDepth = 0;
        int numberOfMoves = BitMapMoveGenerator.populateShortArrayWithAllPossibleMoves(board, isPlayerOne, moveArray, actualDepth);

        // Shared variables
        AtomicInteger sharedAlpha = new AtomicInteger(alpha);
        AtomicReference<Long[]> bestBoard = new AtomicReference<>(null);
        AtomicInteger bestScore = new AtomicInteger(-Integer.MAX_VALUE);
        AtomicInteger moveIndex = new AtomicInteger(0); // To track which move is being processed

        // Create an ExecutorService with 4 threads
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Define the task each thread will perform
        Runnable task = () -> {
            while (true) {
                int currentMoveIndex = moveIndex.getAndIncrement(); // Atomically get the next move
                if (currentMoveIndex > numberOfMoves) {
                    break; // No more moves to process
                }

                short move = moveArray[currentMoveIndex];
                updateBoardHistory(move, true, 0, isPlayerOne);
                long[] syncedBoard = board.clone();
//                BitmapFianco.ShowBitBoard(syncedBoard);
//                System.out.println("Move is valid: " + BitMapMoveGenerator.isMoveValid(syncedBoard, move, isPlayerOne) + "move is: " + MoveConversion.unpackFirstNumber(move) + "->" + MoveConversion.unpackSecondNumber(move));
                BitMapMoveGenerator.makeOrUnmakeMoveInPlace(syncedBoard, move, isPlayerOne);

                int value = -AlphaBeta(syncedBoard, !isPlayerOne, depth - 1, actualDepth + 1, gameMoves, moveArray.clone(), -beta, -sharedAlpha.get());

                // Synchronize access to update alpha, bestScore, and bestBoard
                synchronized (this) {
                    if (value > bestScore.get()) {
                        bestScore.set(value);
                        bestBoard.set(new Long[]{syncedBoard[0], syncedBoard[1], syncedBoard[2], syncedBoard[3]});
                    }
                    if (bestScore.get() > sharedAlpha.get()) {
                        sharedAlpha.set(bestScore.get());
                    }
                    if (bestScore.get() >= beta) {
                        break; // Prune further searches
                    }
                }

                // Undo the move
                updateBoardHistory(move, false, 0, isPlayerOne);
                BitMapMoveGenerator.makeOrUnmakeMoveInPlace(syncedBoard, move, isPlayerOne);
            }
        };

        // Submit the tasks to the executor
        for (int i = 0; i < 4; i++) {
            executor.submit(task);
        }

        // Shut down the executor and wait for all threads to finish
        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all threads to complete
        }

        long endTime = System.nanoTime();
        System.out.println("Nodes: " + nodes);
        System.out.println("Time: " + (endTime - startTime) / 1_000_000_000.0 + "s");
        System.out.println("Nodes per second: " + nodes.get() / ((endTime - startTime) / 1_000_000_000.0));
        System.out.println("Score: " + bestScore.get());

        // Update board history
        long[] longArrayBestBoard = new long[]{bestBoard.get()[0], bestBoard.get()[1], bestBoard.get()[2], bestBoard.get()[3]};
        updateBoardHistory(lastBoard, new BitmapFianco(longArrayBestBoard).convertBitmapTo2DIntArray(), isPlayerOne);
        return longArrayBestBoard;
    }
}