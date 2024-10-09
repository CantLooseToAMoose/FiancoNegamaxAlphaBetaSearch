package AI;

import BitBoard.BitMapMoveGenerator;
import BitBoard.BitmapFianco;
import BitBoard.MoveConversion;
import FiancoGameEngine.Fianco;
import FiancoGameEngine.MoveCommand;
import search.AlphaBeta.AlphaBetaSearchWithQuiescence;
import search.TT.Zobrist;

import java.util.ArrayList;

public class IterativeAlphaBetaSearchAgentQuiescence implements IAgent {
    public static final int MAX_GAME_MOVES = 600;

    private BitmapFianco fianco;
    private final int player;
    private AlphaBetaSearchWithQuiescence alphaBetaSearch;
    private long[] board;
    private ArrayList<MoveCommand> moveHistory = new ArrayList<>();
    private long[] boardHistory = new long[MAX_GAME_MOVES];
    private long zobristHash = 0;
    private int gameMoves = 0;
    private ArrayList<Integer> lastConversionMoves = new ArrayList<>();

    @Override
    public void resetBoard() {
        fianco.populateBoardBitmapsFrom2DIntArray(new Fianco().getBoardState());
        long[] player1Board = fianco.getPlayer1Board();
        long[] player2Board = fianco.getPlayer2Board();
        board = new long[]{player1Board[0], player1Board[1], player2Board[0], player2Board[1]};
        alphaBetaSearch = new AlphaBetaSearchWithQuiescence();
        lastConversionMoves = new ArrayList<>();
        lastConversionMoves.add(0);
        zobristHash = 0;
        boardHistory = new long[MAX_GAME_MOVES];
        gameMoves = 0;
        boardHistory[gameMoves] = zobristHash;
    }

    @Override
    public MoveCommand generateMove(MoveCommand move) {
        if (move != null) {
            short shortMove = MoveConversion.getShortMoveFromMoveCommand(move);
            BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, shortMove, !(player == 1));
            zobristHash = Zobrist.updateHash(zobristHash, shortMove, player == 1);
            moveHistory.add(move);
            boardHistory[gameMoves] = zobristHash;
            if (BitMapMoveGenerator.checkIfConversionMove(shortMove)) {
                lastConversionMoves.add(gameMoves);
            }
            gameMoves++;
        }

        short newMove = alphaBetaSearch.GetBestMoveIterativeDeepening(this.board, zobristHash, boardHistory.clone(), gameMoves, lastConversionMoves.get(lastConversionMoves.size() - 1), player == 1, 30, -Integer.MAX_VALUE, Integer.MAX_VALUE, 15_000_000_000L);

        BitMapMoveGenerator.makeOrUnmakeMoveInPlace(board, newMove, (player == 1));
        MoveCommand moveCommand = MoveConversion.getMoveCommandFromShortMove(newMove, (player == 1));
        moveHistory.add(moveCommand);
        zobristHash = Zobrist.updateHash(zobristHash, newMove, player == 1);
        boardHistory[gameMoves] = zobristHash;
        if (BitMapMoveGenerator.checkIfConversionMove(newMove)) {
            lastConversionMoves.add(gameMoves);
        }
        gameMoves++;
        return moveCommand;
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

    public IterativeAlphaBetaSearchAgentQuiescence(BitmapFianco fianco, int player) {
        this.fianco = fianco;
        this.player = player;
        this.alphaBetaSearch = new AlphaBetaSearchWithQuiescence();
        fianco.populateBoardBitmapsFrom2DIntArray(new Fianco().getBoardState());
        long[] player1Board = fianco.getPlayer1Board();
        long[] player2Board = fianco.getPlayer2Board();
        board = new long[]{player1Board[0], player1Board[1], player2Board[0], player2Board[1]};
        lastConversionMoves.add(0);

    }


}
