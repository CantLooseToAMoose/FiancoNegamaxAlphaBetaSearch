package search.TT;

public class TranspositionTable {

    private final TranspositionTableEntry[] table;
    private final int size;

    public TranspositionTable(int size) {
        this.size = size;
        this.table = new TranspositionTableEntry[size];
    }

    // Store a position in the transposition table
    public static void storeAlwaysNewOne(TranspositionTableEntry[] table, long hash, int size, int depth, int score, byte type, short bestMove) {
        int index = (int) (hash & (size - 1)); // Reduce hash to fit in array
        TranspositionTableEntry entry = retrieve(table, hash, size);
        if (entry == null) {
            //If there is no existing entry
            entry = new TranspositionTableEntry(hash, depth, score, type, bestMove);
            table[index] = entry;
        } else {
            //If there is an entry but the boardstate does not match, save the new one
            if (entry.hash != hash) {
                entry.setValuesSynced(hash, depth, score, type, bestMove);
            } else {
                //If there is an entry and the boardstate matches save the one with the higher depth
                if (entry.depth < depth) {
                    entry.setValuesSynced(hash, depth, score, type, bestMove);
                }
            }
        }
    }

    public static void storeDeeper(TranspositionTableEntry[] table, long hash, int size, int depth, int score, byte type, short bestMove) {
        int index = (int) (hash & (size - 1)); // Reduce hash to fit in array
        TranspositionTableEntry entry = retrieve(table, hash, size);
        if (entry == null) {
            //If there is no existing entry
            entry = new TranspositionTableEntry(hash, depth, score, type, bestMove);
            table[index] = entry;
        } else {
            //If there is an entry save the one with the higher depth
            if (entry.depth < depth) {
                entry.setValuesSynced(hash, depth, score, type, bestMove);
            }
        }
    }

    public static void store(TranspositionTableEntry[] primaryTable, TranspositionTableEntry[] table, long hash, int primarySize, int size, int depth, int score, byte type, short bestMove) {
        storeAlwaysNewOne(primaryTable, hash, primarySize, depth, score, type, bestMove);//Always store most recent in the table
        storeDeeper(table, hash, size, depth, score, type, bestMove);
    }

    // Retrieve a position from the transposition table
    public static TranspositionTableEntry retrieve(TranspositionTableEntry[] table, long hash, int size) {
        int index = (int) (hash & (size - 1));
        return table[index];
    }

    public static TranspositionTableEntry retrieve(TranspositionTableEntry[] primaryTable, TranspositionTableEntry[] table, long hash, int primarySize, int size) {
        TranspositionTableEntry entry = retrieve(primaryTable, hash, primarySize);
        if (entry != null) {
            if (entry.hash == hash) {
                return entry;
            }
        }
        return retrieve(table, hash, size);
    }

}

