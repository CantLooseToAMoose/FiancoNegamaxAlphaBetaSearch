package search;

public class TranspositionTable {

    private final TranspositionTableEntry[] table;
    private final int size;

    public TranspositionTable(int size) {
        this.size = size;
        this.table = new TranspositionTableEntry[size];
    }

    // Store a position in the transposition table
    public void store(long hash, int depth, int score, byte type, short bestMove) {
        int index = (int) (hash % size); // Reduce hash to fit in array
        TranspositionTableEntry entry = new TranspositionTableEntry(hash, depth, score, type, bestMove);
        table[index] = entry;
    }

    // Retrieve a position from the transposition table
    public TranspositionTableEntry retrieve(long hash) {
        int index = (int) (hash % size);
        TranspositionTableEntry entry = table[index];

        // Check if the entry's hash matches the current board's hash
        if (entry != null && entry.hash == hash) {
            return entry;
        }
        return null; // No valid entry
    }

    // Check if the table contains the given position
    public boolean contains(long hash) {
        return retrieve(hash) != null;
    }
}

