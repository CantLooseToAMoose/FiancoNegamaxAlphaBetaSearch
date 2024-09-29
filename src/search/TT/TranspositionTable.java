package search.TT;

public class TranspositionTable {

    private final TranspositionTableEntry[] table;
    private final int size;

    public TranspositionTable(int size) {
        this.size = size;
        this.table = new TranspositionTableEntry[size];
    }

    // Store a position in the transposition table
    public static void store(TranspositionTableEntry[] table, long hash, int size, int depth, int score, byte type, short bestMove) {
        int index = (int) (hash & (size - 1)); // Reduce hash to fit in array
        TranspositionTableEntry entry = retrieve(table, hash, size);
        if (entry == null) {
            //If there is no existing entry
            entry = new TranspositionTableEntry(hash, depth, score, type, bestMove);
            table[index] = entry;
        } else {
            //If there is an entry but the boardstate does not match, save the new one
            if (entry.hash != hash) {
//                entry = new TranspositionTableEntry(hash, depth, score, type, bestMove);
                entry.setValuesSynced(hash, depth, score, type, bestMove);
            } else {
                //If there is an entry and the boardstate matches save the one with the higher depth
                if (entry.depth < depth) {
                    entry.setValuesSynced(hash, depth, score, type, bestMove);
                }
            }
        }
    }

    // Retrieve a position from the transposition table
    public static TranspositionTableEntry retrieve(TranspositionTableEntry[] table, long hash, int size) {
        int index = (int) (hash& (size - 1));
        TranspositionTableEntry entry = table[index];
        // Check if the entry's hash matches the current board's hash
        if (entry == null) {
            return null;
        }
        return entry;
    }

}

