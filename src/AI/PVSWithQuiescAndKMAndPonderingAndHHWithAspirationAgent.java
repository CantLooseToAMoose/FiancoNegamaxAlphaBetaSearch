package AI;

import BitBoard.BitMapMoveGenerator;
import BitBoard.BitmapFianco;
import BitBoard.MoveConversion;
import FiancoGameEngine.Fianco;
import FiancoGameEngine.MoveCommand;
import search.AlphaBeta.PVSWithQuiesAndKM;
import search.AlphaBeta.PVSWithQuiesAndKMAndHHAndAspiration;
import search.TT.Zobrist;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PVSWithQuiescAndKMAndPonderingAndHHWithAspirationAgent implements IAgent {
    public static final int MAX_GAME_MOVES = 6000;

    private BitmapFianco fianco;
    private final int player;
    private PVSWithQuiesAndKMAndHHAndAspiration alphaBetaSearch;
    private long[] board;
    private ArrayList<MoveCommand> moveHistory = new ArrayList<>();
    private long[] boardHistory = new long[MAX_GAME_MOVES];
    private long zobristHash = 0;
    private int gameMoves = 0;
    private ArrayList<Integer> lastConversionMoves = new ArrayList<>();
    private AtomicInteger newAtomicMove = new AtomicInteger(0);

    //for Pondering
    ExecutorService searchExecuter;
    short[] pvLine = new short[PVSWithQuiesAndKM.MAX_NUMBER_OF_ACTUAL_DEPTH];
    private int lastIterationDepth = 0;


    // Time management variables
    private static final long TOTAL_GAME_TIME_NANO = 10L * 60L * 1_000_000_000L; // 10 minutes in nanoseconds
    private long totalTimeRemaining;
    private int movesMadeByAgent = 0;
    private int estimatedTotalMoves = 60; // Estimated number of moves by the agent


    @Override
    public void resetBoard() {
        fianco.populateBoardBitmapsFrom2DIntArray(new Fianco().getBoardState());
        long[] player1Board = fianco.getPlayer1Board();
        long[] player2Board = fianco.getPlayer2Board();
        board = new long[]{player1Board[0], player1Board[1], player2Board[0], player2Board[1]};
        alphaBetaSearch = new PVSWithQuiesAndKMAndHHAndAspiration();
        lastConversionMoves = new ArrayList<>();
        lastConversionMoves.add(0);
        zobristHash = 0;
        boardHistory = new long[MAX_GAME_MOVES];
        gameMoves = 0;
        boardHistory[gameMoves] = zobristHash;
        totalTimeRemaining = TOTAL_GAME_TIME_NANO;
        movesMadeByAgent = 0;
    }

    @Override
    public synchronized MoveCommand generateMove(MoveCommand move) {
        long startTime = System.nanoTime();
        long maxTime = estimateTimeForNextMove();


        boolean isOnPVLine = false;
        if (move != null) {
            short shortMove = MoveConversion.getShortMoveFromMoveCommand(move);
            if (pvLine[1] == shortMove) {
                isOnPVLine = true;
            }
            BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, shortMove, !(player == 1));
            zobristHash = Zobrist.updateHash(zobristHash, shortMove, !(player == 1));
            moveHistory.add(move);
            boardHistory[gameMoves] = zobristHash;
            if (BitMapMoveGenerator.checkIfConversionMove(shortMove)) {
                lastConversionMoves.add(gameMoves);
            }
            gameMoves++;
        }
        short newMove;
        if (isOnPVLine) {
            System.out.println("Opponent did make PVLine move");
            newMove = getMoveIfOpponentOnPVLine(maxTime);
        } else {
            System.out.println("Opponent did not make PVLine move");
            if (searchExecuter != null) {
                alphaBetaSearch.stopSearchHard();
                try {
                    if (!searchExecuter.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                        searchExecuter.shutdownNow();
                        System.out.println("SearchExecutor did not time out properly.");
                    }
                } catch (InterruptedException e) {
                    searchExecuter.shutdownNow();
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
            newMove = alphaBetaSearch.GetBestMoveIterativeDeepening(this.board, zobristHash, boardHistory.clone(), new short[PVSWithQuiesAndKM.MAX_NUMBER_OF_ACTUAL_DEPTH], gameMoves, lastConversionMoves.get(lastConversionMoves.size() - 1), player == 1, 1, 40, -Integer.MAX_VALUE, Integer.MAX_VALUE, maxTime);
        }
        while(!BitMapMoveGenerator.isMoveValid(board,newMove,player==1)){
            System.out.println("We generated a wrong move! Try again.");
            newMove = alphaBetaSearch.GetBestMoveIterativeDeepening(this.board, zobristHash, boardHistory.clone(), new short[PVSWithQuiesAndKM.MAX_NUMBER_OF_ACTUAL_DEPTH], gameMoves, lastConversionMoves.get(lastConversionMoves.size() - 1), player == 1, 1, 40, -Integer.MAX_VALUE, Integer.MAX_VALUE, maxTime/2);

        }

        BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, newMove, (player == 1));
        MoveCommand moveCommand = MoveConversion.getMoveCommandFromShortMove(newMove, (player == 1));
        moveHistory.add(moveCommand);
        zobristHash = Zobrist.updateHash(zobristHash, newMove, player == 1);
        boardHistory[gameMoves] = zobristHash;
        if (BitMapMoveGenerator.checkIfConversionMove(newMove)) {
            lastConversionMoves.add(gameMoves);
        }
        gameMoves++;


        // Start pondering by assuming most promising move from opponent
        searchExecuter = null;
        short[] newPvLine = alphaBetaSearch.previousPVLine.clone();
        //when because of whatever reason there is no real pvLine after alphabeta, check if the moves were expected from the previous pvLine
        // and then continue to use the last pvLine rather the empty new one
        if (newPvLine[0] == 0 && newPvLine[1] == 0) {
            if (pvLine[2] == newMove && pvLine[3] != 0) {
                System.out.println("New PvLine is empty and previous one matches. So take the previous One.");
                System.arraycopy(pvLine, 2, pvLine, 0, pvLine.length - 2);
            } else {
                pvLine = alphaBetaSearch.previousPVLine.clone();
            }
        } else {
            pvLine = alphaBetaSearch.previousPVLine.clone();
        }
        lastIterationDepth = alphaBetaSearch.lastIterationDepth;
        System.out.println("Search finished and gave to agent PVLine:");
        PVSWithQuiesAndKM.printPVLine(pvLine, lastIterationDepth, player == 1);
        //Fake the new Move, dont worry if the move is real the values get actually updated
        if (pvLine[0] != 0 && pvLine[1] != 0) {
            searchExecuter = Executors.newSingleThreadExecutor();
            short[] newPVline = new short[pvLine.length];
            System.arraycopy(pvLine, 2, newPVline, 0, pvLine.length - 2);
            long[] boardClone = board.clone();
            BitMapMoveGenerator.makeOrUnmakeMoveInPlace(boardClone, pvLine[1], !(player == 1));
            long zobristHashClone = Zobrist.updateHash(zobristHash, pvLine[1], player == 1);
            long[] boardHistoryClone = boardHistory.clone();
            boardHistoryClone[gameMoves] = zobristHash;
            ArrayList<Integer> lastConversionMovesClone = new ArrayList<>(lastConversionMoves);
            if (BitMapMoveGenerator.checkIfConversionMove(pvLine[1])) {
                lastConversionMovesClone.add(gameMoves);
            }
            int gameMoveClone = gameMoves + 1;

            Runnable task = () -> {
                try {
                    System.out.println("");
                    System.out.println("Start searching with previous PVLine.");
                    alphaBetaSearch.GetBestMoveIterativeDeepening(boardClone, zobristHashClone, boardHistoryClone, newPVline, gameMoveClone, lastConversionMovesClone.get(lastConversionMoves.size() - 1), player == 1, 1, 40, -Integer.MAX_VALUE, Integer.MAX_VALUE, 15 * maxTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                newAtomicMove.set(alphaBetaSearch.afterIterationBestMove);
            };
            searchExecuter.submit(task);
            searchExecuter.shutdown();
        }

        long endTime = System.nanoTime();
        long timeTaken = endTime - startTime;
        totalTimeRemaining -= timeTaken;
        if (totalTimeRemaining < 0) {
            totalTimeRemaining = 0;
        }
        movesMadeByAgent++;
        movesMadeByAgent++;

        // Logging for debugging
        System.out.println("\n Time taken for move: " + timeTaken / 1_000_000 + " ms");
        System.out.println("Total time remaining: " + totalTimeRemaining / 1_000_000_000 + " s");
        System.out.println("Estimated moves left: " + (estimatedTotalMoves - movesMadeByAgent));
        System.out.println("Phase weight: " + getPhaseWeight());
        System.out.println("Max time for next move: " + maxTime / 1_000_000 + " ms");

        return moveCommand;
    }

    public short getMoveIfOpponentOnPVLine(long maxTime) {
        try {
            if (!searchExecuter.awaitTermination(maxTime, TimeUnit.NANOSECONDS)) {
                System.out.println("Time is Up. Stop Alpha Beta now.");
                alphaBetaSearch.stopSearchSoft();
                if (!searchExecuter.awaitTermination(4, TimeUnit.SECONDS)) {
                    System.out.println("Tried to soft Stop Alpha Beta but failed, so try to hard stop now.");
                    alphaBetaSearch.stopSearchHard();
                    if (!searchExecuter.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                        System.out.println("Hard Stop also failed");
                        searchExecuter.shutdownNow();
                    }
                }
                searchExecuter.shutdownNow();
            }
        } catch (InterruptedException e) {
            searchExecuter.shutdownNow();
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        short newMove = alphaBetaSearch.afterIterationBestMove;
        return newMove;
    }

    private long estimateTimeForNextMove() {
        int estimatedMovesLeft = estimatedTotalMoves - movesMadeByAgent;
        if (estimatedMovesLeft <= 0) {
            estimatedMovesLeft = 1;
        }
        long baseMaxTime = totalTimeRemaining / estimatedMovesLeft;
        double phaseWeight = getPhaseWeight();
        long maxTime = (long) (baseMaxTime * phaseWeight);
        // Set minimum maxTime per move to 1 second (1,000,000,000 nanoseconds)
        if (maxTime < 1_000_000_000L) {
            maxTime = 1_000_000_000L;
        }
        return maxTime;
    }

    private double getPhaseWeight() {
        double fraction = (double) movesMadeByAgent / estimatedTotalMoves;
        if (fraction < 1.0 / 3.0) {
            // Opening phase
            return 0.8;
        } else if (fraction < 2.0 / 3.0) {
            // Mid-game phase
            return 1.2;
        } else {
            // Endgame phase
            return 0.9;
        }
    }

    @Override
    public void undoMove() {
        MoveCommand move = moveHistory.remove(moveHistory.size() - 1);
        short shortMove = MoveConversion.getShortMoveFromMoveCommand(move);
        BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, shortMove, move.getPlayer() == 1);
        zobristHash = Zobrist.updateHash(zobristHash, shortMove, move.getPlayer() == 1);
        boardHistory[gameMoves] = 0;
        if (BitMapMoveGenerator.checkIfConversionMove(shortMove)) {
            lastConversionMoves.remove(lastConversionMoves.size() - 1);
        }
        gameMoves--;
    }

    public PVSWithQuiescAndKMAndPonderingAndHHWithAspirationAgent(BitmapFianco fianco, int player) {
        this.fianco = fianco;
        this.player = player;
        this.alphaBetaSearch = new PVSWithQuiesAndKMAndHHAndAspiration();
        fianco.populateBoardBitmapsFrom2DIntArray(new Fianco().getBoardState());
        long[] player1Board = fianco.getPlayer1Board();
        long[] player2Board = fianco.getPlayer2Board();
        board = new long[]{player1Board[0], player1Board[1], player2Board[0], player2Board[1]};
        lastConversionMoves.add(0);
        this.totalTimeRemaining = TOTAL_GAME_TIME_NANO;
    }


}
