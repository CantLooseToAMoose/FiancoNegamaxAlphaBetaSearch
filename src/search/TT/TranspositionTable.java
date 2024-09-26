package search.TT;

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
        if (index < 0) {
            index += size; // Fix for negative indices
        }
        TranspositionTableEntry entry = new TranspositionTableEntry(hash, depth, score, type, bestMove);
        table[index] = entry;
    }

    // Retrieve a position from the transposition table
    public TranspositionTableEntry retrieve(long hash) {
        int index = (int) (hash % size);
        if (index < 0) {
            index += size; // Fix for negative indices
        }
        TranspositionTableEntry entry = table[index];

        // Check if the entry's hash matches the current board's hash
        if (entry == null) {
            return null;
        }
        return entry;
    }

}

