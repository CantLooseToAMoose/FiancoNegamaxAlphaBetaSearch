package search;

public class TranspositionTableEntry {
    public long hash;     // Zobrist hash of the board position
    public int depth;     // Depth of the search when this value was stored
    public int score;     // Evaluated score of the position
    public byte type; // Node type (exact=0, alpha=1, beta=2)
    public short bestMove; // The best move in this position

    public TranspositionTableEntry(long hash, int depth, int score, byte type, short bestMove) {
        this.hash = hash;
        this.depth = depth;
        this.score = score;
        this.type = type;
        this.bestMove = bestMove;
    }
}