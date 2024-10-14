package search.AlphaBeta;

import BitBoard.BitMapMoveGenerator;
import BitBoard.BitmapFianco;
import BitBoard.MoveConversion;
import FiancoGameEngine.MoveCommand;
import search.Evaluation.Evaluate;
import search.TT.TranspositionTable;
import search.TT.TranspositionTableEntry;
import search.TT.Zobrist;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

//TODO: Can you think on opponents turn?

public class AlphaBetaSearch {
    //Constants
    //
    public static final int MAX_NUMBER_OF_MOVES = 15 * 5;
    public static final int MAX_NUMBER_OF_ACTUAL_DEPTH = 300;
    public static final int MAX_NUMBER_OF_MOVES_SINCE_LAST_CONVERSION = 15;

    //Transposition Table Sizes
    public static final int PRIMARY_TRANSPOSITION_TABLE_SIZE = 16_384; //Needs to be really small to optimally fit completely in Cache
    public static final int TRANSPOSITION_TABLE_SIZE = 134_217_728;//4 GB
//    public static final int TRANSPOSITION_TABLE_SIZE = 134_217_728 * 4; //16 GB
//    public static final int TRANSPOSITION_TABLE_SIZE = 134_217_728*2;//8 GB
//    public static final int TRANSPOSITION_TABLE_SIZE = 134_217_728 / 2;//2GB
//    public static final int TRANSPOSITION_TABLE_SIZE = 134_217_728 / 4;//1 GB;

    //Parralelization
    public static final int NUMBER_OF_THREADS = 2;

    //Evaluation Constants
    public static final int WIN_EVAL = 300;
    public static final int DRAW_EVAL = -15;


    //For Debugging
    public LongAdder nodes = new LongAdder();
    public LongAdder ttEntriesFound = new LongAdder();
    public LongAdder ttHashCollision = new LongAdder();
    public LongAdder exactTTPruning = new LongAdder();
    public LongAdder basicTTPruning = new LongAdder();
    public LongAdder alphaBetaPruning = new LongAdder();

    //For Repetition Detection
    private long[] boardHistory = new long[MAX_NUMBER_OF_ACTUAL_DEPTH + MAX_NUMBER_OF_MOVES_SINCE_LAST_CONVERSION];
    private long zobristHash = 0;
    //Todo: Add mirrored transposition Table entry
    private long mirroredZobristHash = 0;
    private int[][] lastBoard;
    private int gameMoves = 0;

    //Transposition Table
    private final TranspositionTableEntry[] primaryTranspositionTable = new TranspositionTableEntry[PRIMARY_TRANSPOSITION_TABLE_SIZE];
    private TranspositionTableEntry[] transpositionTable = new TranspositionTableEntry[TRANSPOSITION_TABLE_SIZE];

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
    //This is only done at the beginning and end of search
    public void updateBoardHistory(int[][] lastBoard, int[][] newBoard, boolean isPlayerOne) {

        MoveCommand moveCommand = MoveCommand.CreateMoveCommandFromConsecutiveBoardStates(lastBoard, newBoard, isPlayerOne ? 1 : 2);
        if (moveCommand != null) {
            short move = moveCommand.toShort();
            if (checkIfConversionMove(move)) {
                boardHistory = new long[MAX_NUMBER_OF_ACTUAL_DEPTH + MAX_NUMBER_OF_MOVES_SINCE_LAST_CONVERSION];
                gameMoves = 0;
            }
            this.zobristHash = Zobrist.updateHash(this.zobristHash, move, isPlayerOne);
            this.mirroredZobristHash = Zobrist.updateHash(this.mirroredZobristHash, MoveConversion.getHorizontallyMirroredShort(move), isPlayerOne);
            updateBoardHistory(zobristHash, this.boardHistory, true, 0);
            this.lastBoard = newBoard;
        } else {
            //FIXME: Why are you called?
            System.out.println("Something went wrong when trying to generate a short move from the previous board state");
        }
    }

    //This is done during the search
    private void updateBoardHistory(long zobristHash, long[] boardHistory, boolean doMove, int actualDepth) {
        if (doMove) {
            boardHistory[gameMoves + actualDepth] = zobristHash;
        } else {
            boardHistory[gameMoves + actualDepth] = zobristHash;
        }
    }

    //Alpha Beta
    public int AlphaBeta(long[] board, long zobristHash, long mirroredZobristHash, boolean isPlayerOneTurn, int depth, int actualDepth, long[] boardHistory, int lastConversionMove, short[] moveArray, int alpha, int beta, short[] pvLine) {
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
            synchronized (entry) {
                ttEntriesFound.increment();
                //if entry exists
                if (zobristHash != entry.hash || !BitMapMoveGenerator.isMoveValid(board, entry.bestMove, isPlayerOneTurn)) {
                    ttHashCollision.increment();
                    store = true;
                } else {
                    //the entry is of the same board state
                    if (depth > entry.depth) {
                        ttMove = entry.bestMove;
                        store = true;
                    } else {
                        if (entry.type == 0) {
                            exactTTPruning.increment();
                            pvLine[actualDepth] = entry.bestMove;
                            return entry.score;
                        } else if (entry.type == 1) {
                            alpha = Math.max(oldAlpha, entry.score);
                        } else if (entry.type == 2) {
                            beta = Math.min(beta, entry.score);
                        }
                        if (alpha >= beta) {
                            basicTTPruning.increment();
                            pvLine[actualDepth] = entry.bestMove;
                            return entry.score;
                        }
                        ttMove = entry.bestMove;
                    }
                }
            }
        }

        //Populate Move array
        int win = Evaluate.evaluateWin(board, isPlayerOneTurn);
        //Check for win
        if (win != 0) {
            return (WIN_EVAL + depth) * win;
        }
        int maxNumberOfMovesForAllTypeOfMoves = BitMapMoveGenerator.populateShortArrayWithAllPossibleMoves(board, isPlayerOneTurn, moveArray, actualDepth * MAX_NUMBER_OF_MOVES);
        //Check for no more moves
        if (maxNumberOfMovesForAllTypeOfMoves == 0) {
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
        // Check for no more depth
        if (depth == 0) {
            return Evaluate.combinedEvaluate(board, isPlayerOneTurn);
        }
        short bestMove = 0;

        int score = -Integer.MAX_VALUE;
        for (int i = -2; i < maxNumberOfMovesForAllTypeOfMoves * 5; i++) {
            short move;

            // try out transposition table move first
            if (i == -2) {
                if (ttMove == 0 || ttMove == pvLine[actualDepth]) {
                    continue;
                }
                move = ttMove;
            } else if (i == -1) {
                move = pvLine[actualDepth];
                if (move == 0) {
                    continue;
                } else {
                    if (!BitMapMoveGenerator.isMoveValid(board, move, isPlayerOneTurn)) {
                        continue;
                    }
                }
            } else {
                //get Move from move Array
                move = moveArray[actualDepth * MAX_NUMBER_OF_MOVES + i];
                //dont search for the ttMove again
                if (move == 0 || move == ttMove || move == pvLine[actualDepth] || !BitMapMoveGenerator.isMoveValid(board, move, isPlayerOneTurn)) {
                    continue;
                }
            }
            //Do Move and add to history
            if (checkIfConversionMove(move)) {
                lastConversionMove = gameMoves;
            }
            //Update Board
            zobristHash = Zobrist.updateHash(zobristHash, move, isPlayerOneTurn);
            updateBoardHistory(zobristHash, boardHistory, true, actualDepth);
            //Generate Moves
            //Debugging:
            long[] prevBoard = board.clone();
            BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, move, isPlayerOneTurn);
            short[] childPV = pvLine.clone();
            //Go deeper
            int value = -AlphaBeta(board, zobristHash, mirroredZobristHash, !isPlayerOneTurn, depth - 1, actualDepth + 1, boardHistory, lastConversionMove, moveArray, -beta, -alpha, childPV);
            //Higher again undo move also with zobrist Hash
            zobristHash = Zobrist.updateHash(zobristHash, move, isPlayerOneTurn);
            BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, move, isPlayerOneTurn);
            if (!boardAreEqual(prevBoard, board)) {
                BitmapFianco.ShowBitBoard(prevBoard);
                BitmapFianco.ShowBitBoard(board);
                System.out.println("Something went wrong with make or unmake move in Place.");
            }


            if (value > score) {
                bestMove = move;
                score = value;
                pvLine[actualDepth] = move; // Store the best move in this depth
                System.arraycopy(childPV, actualDepth + 1, pvLine, actualDepth + 1, MAX_NUMBER_OF_ACTUAL_DEPTH - actualDepth - 1); // Copy child's PV into current PV
            }
            if (score >= alpha) {
                alpha = score;
            }
            if (score >= beta) {
                alphaBetaPruning.increment();
                break;
            }
        }
        byte type = 0;
        if (score <= oldAlpha) {
            type = 2;
        } else if (score >= beta) {
            type = 1;
        }
        if (store && depth > 2) {
            TranspositionTable.store(primaryTranspositionTable, transpositionTable, zobristHash, PRIMARY_TRANSPOSITION_TABLE_SIZE, TRANSPOSITION_TABLE_SIZE, depth, score, type, bestMove);
        }
        return score;
    }

    /**
     * @param board
     * @param isPlayerOne
     * @param maxDepth
     * @param alpha
     * @param beta
     * @param maxTime     maximum time in microseconds
     * @return
     */
    public short GetBestMoveIterativeDeepening(long[] board, boolean isPlayerOne, int maxDepth, int alpha, int beta, long maxTime) {
        // Update History with the new Board received
        if (this.lastBoard != null) {
            updateBoardHistory(lastBoard, new BitmapFianco(board).convertBitmapTo2DIntArray(), !isPlayerOne);
        } else {
            this.lastBoard = new BitmapFianco(board).convertBitmapTo2DIntArray();
        }
        System.out.println("Evaluation of this Position: " + Evaluate.combinedEvaluate(board, isPlayerOne));

        //Reset debug values
        long startTime = System.nanoTime();
        long passedTime = 0;
        nodes.reset();
        basicTTPruning.reset();
        exactTTPruning.reset();
        alphaBetaPruning.reset();
        ttHashCollision.reset();
        ttEntriesFound.reset();

        short afterIterationBestMove = 0;
        int afterIterationBestScore = 0;
        long[] afterIterationBestBoard = null;
        AtomicInteger bestScore = new AtomicInteger(-Integer.MAX_VALUE);


        // Reinitialize
        if (this.transpositionTable == null) {
            this.transpositionTable = new TranspositionTableEntry[TRANSPOSITION_TABLE_SIZE];
        } else {
            Arrays.fill(this.transpositionTable, null); // Clear previous entries
        }

        short[] pvLine = new short[MAX_NUMBER_OF_ACTUAL_DEPTH];


        for (int depth = 1; depth <= maxDepth; depth++) {
            // Create a final copy of depth for use in the lambda
            AtomicInteger sharedBestMove = new AtomicInteger(0);
            bestScore.set(-Integer.MAX_VALUE);
            System.out.println("Starting search at depth: " + depth);

            // Reset shared variables for this depth
            AtomicReference<Long[]> bestBoardRef = new AtomicReference<>(null);
            AtomicInteger moveIndex = new AtomicInteger(0);  // Track move index for parallel threads
            short[] moveArray = new short[MAX_NUMBER_OF_MOVES * MAX_NUMBER_OF_ACTUAL_DEPTH];
            int maxNumberOfMovesForAllTypesOfMoves = BitMapMoveGenerator.populateShortArrayWithAllPossibleMoves(board, isPlayerOne, moveArray, 0);
            AtomicInteger sharedAlpha = new AtomicInteger(alpha);

            // Executor for parallelization
            ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

            int iterativeDepth = depth;
            Runnable task = () -> {
                try {
                    while (true) {
                        int currentMoveIndex = moveIndex.getAndIncrement();  // Atomically get next move
                        if (currentMoveIndex >= maxNumberOfMovesForAllTypesOfMoves * 5)
                            break;  // No more moves to process

                        short move = moveArray[currentMoveIndex];
                        if (move == 0) {
                            continue;
                        }
                        int actualDepth = 0;
                        long[] boardCopy = board.clone();
                        long[] boardHistory = this.boardHistory.clone();
                        long zobristHash = Zobrist.updateHash(this.zobristHash, move, isPlayerOne);
                        long mirroredHash = Zobrist.updateHash(this.mirroredZobristHash, move, isPlayerOne);
                        updateBoardHistory(zobristHash, boardHistory, true, 0);
                        BitMapMoveGenerator.makeOrUnmakeMoveInPlace(boardCopy, move, isPlayerOne);
                        short[] childPV = new short[MAX_NUMBER_OF_ACTUAL_DEPTH];
                        int value = -AlphaBeta(boardCopy, zobristHash, mirroredZobristHash, !isPlayerOne, iterativeDepth - 1, actualDepth + 1, boardHistory, 0, moveArray.clone(), -beta, -sharedAlpha.get(), childPV);

                        // Synchronize access to update alpha, bestScore, and bestBoard
                        synchronized (this) {
                            if (value > bestScore.get()) {
                                bestScore.set(value);
                                sharedBestMove.set(move);
                                bestBoardRef.set(new Long[]{boardCopy[0], boardCopy[1], boardCopy[2], boardCopy[3]});
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
                        afterIterationBestScore = bestScore.get();
                        afterIterationBestMove = (short) sharedBestMove.get();
                        Long[] bestBoard = bestBoardRef.get();
                        afterIterationBestBoard = new long[]{bestBoard[0], bestBoard[1], bestBoard[2], bestBoard[3]};
                    }
                } else {
                    if (!executor.awaitTermination(maxTime - passedTime, TimeUnit.NANOSECONDS)) {
                        executor.shutdownNow();
                        break;
                    } else {
                        afterIterationBestScore = bestScore.get();
                        afterIterationBestMove = (short) sharedBestMove.get();
                        Long[] bestBoard = bestBoardRef.get();
                        afterIterationBestBoard = new long[]{bestBoard[0], bestBoard[1], bestBoard[2], bestBoard[3]};
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }

            // Output results of the current depth
            System.out.println("Depth: " + depth + " completed. Best move: " + afterIterationBestMove + " with score: " + bestScore.get());
            printPVLine(pvLine, depth, isPlayerOne);


            passedTime = System.nanoTime() - startTime;
        }


        long endTime = System.nanoTime();
        System.out.println("Nodes: " + nodes);
        System.out.println("Transposition Entries found: " + ttEntriesFound + ". Percent of Total nodes: " + (float) ttEntriesFound.sum() / nodes.sum() * 100 + "%");
        System.out.println("Transposition Table Collision: " + ttHashCollision + ". Percent of entries found: " + (float) ttHashCollision.sum() / ttEntriesFound.sum() * 100 + "%");
        System.out.println("Alpha Beta Pruning: " + alphaBetaPruning + ". Percent of Total nodes: " + (float) alphaBetaPruning.sum() / nodes.sum() * 100 + "%");
        System.out.println("Transposition Table Exact Value Pruning: " + exactTTPruning + ". Percent of correct entries found: " + (float) exactTTPruning.sum() / (ttEntriesFound.sum() - ttHashCollision.sum()) * 100 + "%");
        System.out.println("Transposition Table Alpha Beta Pruning: " + basicTTPruning + ". Percent of correct entries found: " + (float) basicTTPruning.sum() / (ttEntriesFound.sum() - ttHashCollision.sum()) * 100 + "%");


        System.out.println("Time: " + (endTime - startTime) / 1_000_000_000.0 + "s");
        System.out.println("Nodes per second: " + nodes.sum() / ((endTime - startTime) / 1_000_000_000.0));
        // Final results after iterative deepening
        System.out.println("Final Best Move:" + afterIterationBestMove);
        System.out.println("Final Best Score: " + afterIterationBestScore);


        // Update board history and return the best board found
        zobristHash = Zobrist.updateHash(zobristHash, afterIterationBestMove, isPlayerOne);
        mirroredZobristHash = Zobrist.updateHash(mirroredZobristHash, MoveConversion.getHorizontallyMirroredShort(afterIterationBestMove), isPlayerOne);
        updateBoardHistory(lastBoard, new BitmapFianco(afterIterationBestBoard).convertBitmapTo2DIntArray(), isPlayerOne);
        return afterIterationBestMove;
    }

    // For Debugging Helper Methods
    private void printPVLine(short[] pvLine, int depth, boolean isPlayerOne) {
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