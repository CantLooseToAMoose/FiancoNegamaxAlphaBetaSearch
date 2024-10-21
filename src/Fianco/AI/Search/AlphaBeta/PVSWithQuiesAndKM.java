package Fianco.AI.Search.AlphaBeta;

import Fianco.AI.BitBoard.BitMapMoveGenerator;
import Fianco.AI.BitBoard.MoveConversion;
import Fianco.GameEngine.MoveCommand;
import Fianco.AI.Search.Evaluation.Evaluate;
import Fianco.AI.Search.TT.TranspositionTable;
import Fianco.AI.Search.TT.TranspositionTableEntry;
import Fianco.AI.Search.TT.Zobrist;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;


public class PVSWithQuiesAndKM {
    //Constants
    //
    public static final int MAX_NUMBER_OF_MOVES = 15 * 5;
    public static final int MAX_NUMBER_OF_ACTUAL_DEPTH = 3000;
    public static final int MAX_NUMBER_OF_MOVES_SINCE_LAST_CONVERSION = 15;

    private static final int PRIMARY_TRANSPOSITION_TABLE_SIZE = TranspositionTable.PRIMARY_TRANSPOSITION_TABLE_SIZE;
    private static final int TRANSPOSITION_TABLE_SIZE = TranspositionTable.TRANSPOSITION_TABLE_SIZE_8GB;

    //Parralelization
    public static final int NUMBER_OF_THREADS = 1;
    public ExecutorService executor;

    //Evaluation Constants
    public static final int WIN_EVAL = 1000;
    public static final int DRAW_EVAL = -150;


    //For Debugging
    public LongAdder nodes = new LongAdder();
//    public LongAdder ttEntriesFound = new LongAdder();
//    public LongAdder ttHashCollision = new LongAdder();
//    public LongAdder exactTTPruning = new LongAdder();
//    public LongAdder basicTTPruning = new LongAdder();
//    public LongAdder alphaBetaPruning = new LongAdder();

    //For Repetition Detection
    private long[] boardHistory = new long[MAX_NUMBER_OF_ACTUAL_DEPTH + MAX_NUMBER_OF_MOVES_SINCE_LAST_CONVERSION];

    //Transposition Table
    private final TranspositionTableEntry[] primaryTranspositionTable = new TranspositionTableEntry[PRIMARY_TRANSPOSITION_TABLE_SIZE];
    private TranspositionTableEntry[] transpositionTable = new TranspositionTableEntry[TRANSPOSITION_TABLE_SIZE];

    // Killer Moves
    private short[][] killerMoves = new short[MAX_NUMBER_OF_ACTUAL_DEPTH][2];

    //PVS
    public short[] previousPVLine = new short[MAX_NUMBER_OF_ACTUAL_DEPTH];
    private int maxActualDepth = 0;

    //Pondering
    public int lastIterationDepth = 0;
    private boolean stopSoft = false;
    private boolean stopHard = false;
    public short afterIterationBestMove;


    //Alpha Beta
    public int AlphaBeta(long[] board, long zobristHash, boolean isPlayerOneTurn, int depth, int actualDepth, long[] boardHistory, int gameMoves, int lastConversionMove, short[] moveArray, int alpha, int beta, short[] pvLine) {
        //Break Fianco.AI.search
        if (stopHard) {
            return -Integer.MAX_VALUE;
        }
        //Depth
        synchronized (this) {
            maxActualDepth = Math.max(maxActualDepth, actualDepth);
        }

        //Debugging
        nodes.increment();
        //Transposition Table
        int oldAlpha = alpha;
        TranspositionTableEntry entry = TranspositionTable.retrieve(primaryTranspositionTable, transpositionTable, zobristHash, PRIMARY_TRANSPOSITION_TABLE_SIZE, TRANSPOSITION_TABLE_SIZE, depth);
        short ttMove = 0;

        boolean store = false;
        if (entry == null) {
            store = true;
        } else {
//            ttEntriesFound.increment();
            //if entry exists
            if (!BitMapMoveGenerator.isMoveValid(board, entry.bestMove, isPlayerOneTurn)) {
//                ttHashCollision.increment();
                store = true;
            } else {
                //the entry is of the same board state
                if (depth > entry.depth) {
                    ttMove = entry.bestMove;
                    store = true;
                } else {
                    if (entry.type == 0) {
//                        exactTTPruning.increment();
                        pvLine[actualDepth] = entry.bestMove;
                        return entry.score;
                    } else if (entry.type == 1) {
                        alpha = Math.max(oldAlpha, entry.score);
                    } else if (entry.type == 2) {
                        beta = Math.min(beta, entry.score);
                    }
                    if (alpha >= beta) {
//                        basicTTPruning.increment();
                        pvLine[actualDepth] = entry.bestMove;
                        return entry.score;
                    }
                    ttMove = entry.bestMove;
                }
            }
        }


        //Populate Move array
        int win = Evaluate.evaluateWin(board, isPlayerOneTurn);
        //Check for win
        if (win != 0) {
            return (WIN_EVAL + depth) * win;
        }
        int maxCaptureMovesAdded = BitMapMoveGenerator.populateShortArrayWithCaptureMoves(board, isPlayerOneTurn, moveArray, actualDepth * MAX_NUMBER_OF_MOVES);
        int maxNumberOfMovesForAllTypes = maxCaptureMovesAdded;
        if (maxCaptureMovesAdded == 0) {
            maxNumberOfMovesForAllTypes = Math.max(maxCaptureMovesAdded, BitMapMoveGenerator.populateShortArrayWithQuietMoves(board, moveArray, isPlayerOneTurn, actualDepth * MAX_NUMBER_OF_MOVES));
        }

        //Check for no more moves
        if (maxNumberOfMovesForAllTypes == 0) {
            return -(WIN_EVAL + depth);
        }
        //Check for draw
        byte boardRep = 0;
        for (int i = gameMoves + actualDepth; i > lastConversionMove; i = i - 2) {
            if (zobristHash == boardHistory[i]) {
                boardRep++;
            }
        }
        //If this boardstate has been seen more than 2 times it is a draw
        if (boardRep > 1) {
            return DRAW_EVAL;
        }
        // Check for no more depth and maybe Quiescence


        if (depth == 0) {
            if ((maxCaptureMovesAdded == 0)) {
                return Evaluate.combinedEvaluateWithBlockedPieceEvaluation(board, isPlayerOneTurn);
            } else {
                depth++;
            }
        }
        short bestMove = 0;
        // PVS Stuff
        short pvLineMove = previousPVLine[actualDepth];
        int lBound;
        int uBound;
        short killerMove1 = killerMoves[actualDepth][0];
        short killerMove2 = killerMoves[actualDepth][1];
        int score = -Integer.MAX_VALUE;
        for (int i = -4; i < maxNumberOfMovesForAllTypes * 5; i++) {
            short move;
            // try out transposition table move first
            switch (i) {
                case -4 -> {
                    if (ttMove == 0) {
                        continue;
                    }
                    move = ttMove;
                }
                case -3 -> {
                    move = pvLineMove;
                    if (move == 0 || move == ttMove || !BitMapMoveGenerator.isMoveValid(board, move, isPlayerOneTurn)) {
                        continue;
                    }
                }
                case -2 -> {
                    move = killerMove1;
                    if (move == 0 || move == ttMove || move == pvLineMove || !BitMapMoveGenerator.isMoveValid(board, move, isPlayerOneTurn)) {
                        continue;
                    }
                }
                case -1 -> {
                    move = killerMove2;
                    if (move == 0 || move == ttMove || move == pvLineMove || move == killerMove1 || !BitMapMoveGenerator.isMoveValid(board, move, isPlayerOneTurn)) {
                        continue;
                    }
                }
                default -> {
                    //get Move from move Array
                    move = moveArray[actualDepth * MAX_NUMBER_OF_MOVES + i];
                    //dont Fianco.AI.search for the ttMove again
                    if (move == 0 || move == ttMove || move == pvLineMove || move == killerMove1 || move == killerMove2 || !BitMapMoveGenerator.isMoveValid(board, move, isPlayerOneTurn)) {
                        continue;
                    }
                }
            }
            //Do Move and add to history
            int previousLastConversionMove = lastConversionMove;
            if (BitMapMoveGenerator.checkIfConversionMove(move)) {
                lastConversionMove = gameMoves;
            }
            //Update Board
            zobristHash = Zobrist.updateHash(zobristHash, move, isPlayerOneTurn);
            boardHistory[gameMoves + actualDepth] = zobristHash;
            //Generate Moves
            //Debugging:
            BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, move, isPlayerOneTurn);
            short[] childPV = new short[MAX_NUMBER_OF_ACTUAL_DEPTH];
            int value;
            //Go deeper
            //If PVS then Fianco.AI.search with smaller window first
            if (score != -Integer.MAX_VALUE && i > -4) {
                lBound = Math.max(score, alpha);
                uBound = lBound + 1;
                value = -AlphaBeta(board, zobristHash, !isPlayerOneTurn, depth - 1, actualDepth + 1, boardHistory, gameMoves, lastConversionMove, moveArray, -uBound, -lBound, childPV);
                // If Fianco.AI.search with smaller value failed, research with broader window
                if (value >= uBound && value < beta) {
                    value = -AlphaBeta(board, zobristHash, !isPlayerOneTurn, depth - 1, actualDepth + 1, boardHistory, gameMoves, lastConversionMove, moveArray, -beta, -value, childPV);
                }
            } else {
                //If there was no pvLine move, just do regular Alpha Beta
                value = -AlphaBeta(board, zobristHash, !isPlayerOneTurn, depth - 1, actualDepth + 1, boardHistory, gameMoves, lastConversionMove, moveArray, -beta, -alpha, childPV);
            }
            //Higher again undo move also with zobrist Hash
            zobristHash = Zobrist.updateHash(zobristHash, move, isPlayerOneTurn);
            BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, move, isPlayerOneTurn);
            lastConversionMove = previousLastConversionMove;

            if (value > score) {
                bestMove = move;
                score = value;
                pvLine[actualDepth] = move; // Store the best move in this depth
                System.arraycopy(childPV, actualDepth + 1, pvLine, actualDepth + 1, maxActualDepth - actualDepth); // Copy child's PV into current PV
            }
            if (score > alpha) {
                alpha = score;
            }
            if (score >= beta) {
                if (!BitMapMoveGenerator.isCaptureMove(move)) {
                    killerMoves[actualDepth][1] = killerMoves[actualDepth][0];
                    killerMoves[actualDepth][0] = move;
                }
//                alphaBetaPruning.increment();
                break;
            }
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

    public short GetBestMoveIterativeDeepening(long[] board, long zobristHash, long[] boardHistory, short[] previousPVLine, int gameMoves, int lastConversionMove, boolean isPlayerOne, int minDepth, int maxDepth, int alpha, int beta, long maxTime) {
        System.out.println("Evaluation of this Position: " + Evaluate.combinedEvaluate(board, isPlayerOne));
        stopSoft = false;
        stopHard = false;

        //Update for potential new Opponent Move
        this.boardHistory = boardHistory;

        //Reset debug values
        long startTime = System.nanoTime();
        long passedTime = 0;
        nodes.reset();
//        basicTTPruning.reset();
//        exactTTPruning.reset();
//        alphaBetaPruning.reset();
//        ttHashCollision.reset();
//        ttEntriesFound.reset();

        afterIterationBestMove = 0;
        int afterIterationBestScore = 0;
        short[] afterIterationBestPVLine = previousPVLine.clone();
        AtomicInteger bestScore = new AtomicInteger(-Integer.MAX_VALUE);


        short[] moveArray = new short[MAX_NUMBER_OF_MOVES * MAX_NUMBER_OF_ACTUAL_DEPTH];
        int maxCaptureMovesAdded = BitMapMoveGenerator.populateShortArrayWithCaptureMoves(board, isPlayerOne, moveArray, 0);
        int maxNumberOfMovesForAllTypes;
        if (maxCaptureMovesAdded == 0) {
            maxNumberOfMovesForAllTypes = Math.max(maxCaptureMovesAdded, BitMapMoveGenerator.populateShortArrayWithQuietMoves(board, moveArray, isPlayerOne, 0));
        } else {
            maxNumberOfMovesForAllTypes = maxCaptureMovesAdded;
        }
        this.killerMoves = new short[MAX_NUMBER_OF_ACTUAL_DEPTH][2];
        if (maxCaptureMovesAdded == 1) {
            System.out.println("Only 1 Move, so take this one.");
            if (moveArray[0] != 0) {
                this.previousPVLine = new short[MAX_NUMBER_OF_ACTUAL_DEPTH];
                afterIterationBestMove = moveArray[0];
                afterIterationBestPVLine = previousPVLine.clone();
                TranspositionTableEntry entry = TranspositionTable.retrieve(primaryTranspositionTable, transpositionTable, Zobrist.updateHash(zobristHash, afterIterationBestMove, isPlayerOne), TranspositionTable.PRIMARY_TRANSPOSITION_TABLE_SIZE, TRANSPOSITION_TABLE_SIZE, minDepth);
                afterIterationBestPVLine[0] = afterIterationBestMove;
                if (entry != null) {
                    long[] boardClone = board.clone();
                    BitMapMoveGenerator.makeOrUnmakeMoveInPlace(boardClone, entry.bestMove, !isPlayerOne);
                    if (BitMapMoveGenerator.isMoveValid(boardClone, entry.bestMove, !isPlayerOne)) {
                        afterIterationBestPVLine[1] = entry.bestMove;
                    }
                }
                return moveArray[0];
            }
            if (moveArray[1] != 0) {
                this.previousPVLine = new short[MAX_NUMBER_OF_ACTUAL_DEPTH];
                afterIterationBestMove = moveArray[1];
                afterIterationBestPVLine = previousPVLine.clone();
                TranspositionTableEntry entry = TranspositionTable.retrieve(primaryTranspositionTable, transpositionTable, Zobrist.updateHash(zobristHash, afterIterationBestMove, isPlayerOne), TranspositionTable.PRIMARY_TRANSPOSITION_TABLE_SIZE, TRANSPOSITION_TABLE_SIZE, minDepth);
                afterIterationBestPVLine[0] = afterIterationBestMove;
                if (entry != null) {
                    long[] boardClone = board.clone();
                    BitMapMoveGenerator.makeOrUnmakeMoveInPlace(boardClone, entry.bestMove, !isPlayerOne);
                    if (BitMapMoveGenerator.isMoveValid(boardClone, entry.bestMove, !isPlayerOne)) {
                        afterIterationBestPVLine[1] = entry.bestMove;
                    }
                }
                return moveArray[1];
            }
        }
        // Reinitialize
        this.transpositionTable = new TranspositionTableEntry[TRANSPOSITION_TABLE_SIZE];

        for (int depth = minDepth; depth <= maxDepth; depth++) {
            if (stopSoft) {
                break;
            }
            // Create a final copy of depth for use in the lambda
            AtomicInteger sharedBestMove = new AtomicInteger(0);
            bestScore.set(-Integer.MAX_VALUE);
            System.out.println("");
            System.out.println("Starting Fianco.AI.search at depth: " + depth);

            // Reset shared variables for this depth
            short[] pvLine = afterIterationBestPVLine.clone();
            AtomicInteger moveIndex = new AtomicInteger(-1);  // Track move index for parallel threads
            AtomicInteger sharedAlpha = new AtomicInteger(alpha);

            // Executor for parallelization
            executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

            int iterativeDepth = depth;
            int actualDepth = 0;
            Runnable task = () -> {
                try {
                    while (true) {
                        int currentMoveIndex = moveIndex.getAndIncrement();  // Atomically get next move
                        if (currentMoveIndex >= maxNumberOfMovesForAllTypes * 5) break;  // No more moves to process
                        if (stopSoft) {
                            break;
                        }
                        short move;
                        if (currentMoveIndex == -1) {
                            move = pvLine[actualDepth];
                            if (move == 0) {
                                continue;
                            }
                        } else {
                            move = moveArray[currentMoveIndex];
                            if (move == 0) {
                                continue;
                            }
                            if(!BitMapMoveGenerator.isMoveValid(board,move,isPlayerOne)){
                                System.out.println("Move "+MoveConversion.getMoveCommandFromShortMove(move,isPlayerOne));
                            }
                        }
                        long[] boardCopy = board.clone();
                        long[] boardHistoryClone = this.boardHistory.clone();
                        long zobristHashClone = Zobrist.updateHash(zobristHash, move, isPlayerOne);

                        boardHistoryClone[gameMoves + actualDepth] = zobristHash;
                        BitMapMoveGenerator.makeOrUnmakeMoveInPlace(boardCopy, move, isPlayerOne);
                        short[] childPV = new short[MAX_NUMBER_OF_ACTUAL_DEPTH];
                        int value = -AlphaBeta(boardCopy, zobristHashClone, !isPlayerOne, iterativeDepth - 1, actualDepth + 1, boardHistoryClone, gameMoves, lastConversionMove, moveArray.clone(), -beta, -sharedAlpha.get(), childPV);

                        // Synchronize access to update alpha, bestScore, and bestBoard
                        synchronized (this) {
                            if (value > bestScore.get()) {
                                sharedBestMove.set(move);
                                bestScore.set(value);
                                pvLine[actualDepth] = move;  // Update the principal variation
                                System.arraycopy(childPV, actualDepth + 1, pvLine, actualDepth + 1, MAX_NUMBER_OF_ACTUAL_DEPTH - actualDepth - 1);
                            }
                            if (bestScore.get() > sharedAlpha.get()) {
                                sharedAlpha.set(bestScore.get());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            // Submit parallel tasks
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                executor.submit(task);
            }

            // Wait for threads to complete
            executor.shutdown();

            //Wait for Iteration to finish. If Iteration finishes succesfully the after Iteration Best move and Score is stored.
            //When the last Iteration gets cancelled due to time. Check if you found a better Move and use that instead.
            try {
                if (passedTime == 0) {
                    if (!executor.awaitTermination(maxTime, TimeUnit.NANOSECONDS)) {
                        executor.shutdownNow();
                        break;
                    } else {
                        if (!(bestScore.get() == -Integer.MAX_VALUE) && !stopHard && moveIndex.get() >= 4) {
                            afterIterationBestScore = bestScore.get();
                            afterIterationBestMove = (short) sharedBestMove.get();
                            afterIterationBestPVLine = pvLine.clone();
                            this.previousPVLine = afterIterationBestPVLine.clone();
                            lastIterationDepth = iterativeDepth;
                        }
                    }
                } else {
                    if (!executor.awaitTermination(maxTime - passedTime, TimeUnit.NANOSECONDS)) {
                        executor.shutdownNow();
                        break;
                    } else {
                        if (!(bestScore.get() == -Integer.MAX_VALUE) && !stopHard && moveIndex.get() >= 4) {
                            afterIterationBestScore = bestScore.get();
                            afterIterationBestMove = (short) sharedBestMove.get();
                            afterIterationBestPVLine = pvLine.clone();
                            this.previousPVLine = afterIterationBestPVLine.clone();
                            lastIterationDepth = iterativeDepth;
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Alpha Beta Search got interrupted.");
                if (!(bestScore.get() == -Integer.MAX_VALUE) && !stopHard && moveIndex.get() >= 4) {
                    afterIterationBestScore = bestScore.get();
                    afterIterationBestMove = (short) sharedBestMove.get();
                    afterIterationBestPVLine = pvLine.clone();
                    this.previousPVLine = afterIterationBestPVLine.clone();
                    lastIterationDepth = iterativeDepth;
                }
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                break;
            }

            // Output results of the current depth

            System.out.println("Depth: " + depth + " completed. Best move: " + afterIterationBestMove + " with score: " + afterIterationBestScore);
            printPVLine(pvLine, depth, isPlayerOne);


            passedTime = System.nanoTime() - startTime;
        }


        long endTime = System.nanoTime();
        System.out.println("");
        System.out.println("Nodes: " + nodes.sum());
//        System.out.println("Transposition Entries found: " + ttEntriesFound.sum() + ". Percent of Total nodes: " + (float) ttEntriesFound.sum() / nodes.sum() * 100 + "%");
//        System.out.println("Transposition Table Collision: " + ttHashCollision.sum() + ". Percent of entries found: " + (float) ttHashCollision.sum() / ttEntriesFound.sum() * 100 + "%");
//        System.out.println("Alpha Beta Pruning: " + alphaBetaPruning.sum() + ". Percent of Total nodes: " + (float) alphaBetaPruning.sum() / nodes.sum() * 100 + "%");
//        System.out.println("Transposition Table Exact Value Pruning: " + exactTTPruning.sum() + ". Percent of correct entries found: " + (float) exactTTPruning.sum() / (ttEntriesFound.sum() - ttHashCollision.sum()) * 100 + "%");
//        System.out.println("Transposition Table Alpha Beta Pruning: " + basicTTPruning.sum() + ". Percent of correct entries found: " + (float) basicTTPruning.sum() / (ttEntriesFound.sum() - ttHashCollision.sum()) * 100 + "%");
        System.out.println("Max Actual Depth: " + maxActualDepth);

        System.out.println("Time: " + (endTime - startTime) / 1_000_000_000.0 + "s");
        System.out.println("Nodes per second: " + nodes.sum() / ((endTime - startTime) / 1_000_000_000.0));
        // Final results after iterative deepening
        System.out.println("Final Best Move:" + afterIterationBestMove);
        System.out.println("Final Best Score: " + afterIterationBestScore);

        if (afterIterationBestScore == -Integer.MAX_VALUE) {
            System.out.println("Search !failed! return first generated Move.");
            return moveArray[0];
        }
        return afterIterationBestMove;
    }

    public void stopSearchSoft() {
        stopSoft = true;
    }

    public void stopSearchHard() {
        if (this.executor != null) {
            stopHard = true;
            stopSoft = true;
            executor.shutdownNow();
        }
    }

    // For Debugging Helper Methods
    public static void printPVLine(short[] pvLine, int depth, boolean isPlayerOne) {
        boolean playerTurn = isPlayerOne;
        for (int i = 0; i < depth; i++) {
            short move = pvLine[i];
            if (move != 0) {
                MoveCommand moveCommand = MoveConversion.getMoveCommandFromShortMove(move, playerTurn);
                System.out.print(moveCommand + "; ");
            } else {
                System.out.print(move + "; ");
            }
            playerTurn = !playerTurn;
        }
    }

    public boolean boardAreEqual(long[] board1, long[] board2) {
        for (int i = 0; i < board1.length; i++) {
            if (board1[i] != board2[i]) {
                return false;
            }
        }
        return true;
    }
}