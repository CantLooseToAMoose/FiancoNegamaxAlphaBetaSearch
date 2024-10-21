package Fianco.AI.Search.TT;

public class TranspositionTable {
    //Transposition Table Sizes
    public static final int PRIMARY_TRANSPOSITION_TABLE_SIZE = 16_384; //Needs to be really small to optimally fit completely in Cache
    public static final int PRIMARY_TRANSPOSITION_TABLE_SIZE_LARGE=65_536;
    public static final int TRANSPOSITION_TABLE_SIZE_4GB = 134_217_728;//4 GB
    public static final int TRANSPOSITION_TABLE_SIZE_16GB = 134_217_728 * 4; //16 GB
    public static final int TRANSPOSITION_TABLE_SIZE_8GB = 134_217_728 * 2;//8 GB
    public static final int TRANSPOSITION_TABLE_SIZE_2GB = 134_217_728 / 2;//2GB
    public static final int TRANSPOSITION_TABLE_SIZE_1GB = 134_217_728 / 4;//1 GB;

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
                table[index] = new TranspositionTableEntry(hash, depth, score, type, bestMove);
            } else {
                //If there is an entry and the boardstate matches save the one with the higher depth
                if (entry.depth < depth) {
                    table[index] = new TranspositionTableEntry(hash, depth, score, type, bestMove);
                }
            }
        }
    }

    public static void storeDeeper(TranspositionTableEntry[] table, long hash, int size, int depth, int score, byte type, short bestMove) {
        int index = (int) (hash & (size - 1)); // Reduce hash to fit in array
        TranspositionTableEntry entry = retrieve(table, hash, size);
        if (entry == null) {
            //If there is no existing entry
            table[index] = new TranspositionTableEntry(hash, depth, score, type, bestMove);
        } else {
            //If there is an entry save the one with the higher depth
            if (entry.depth < depth) {
                table[index] = new TranspositionTableEntry(hash, depth, score, type, bestMove);
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

    public static TranspositionTableEntry retrieve(TranspositionTableEntry[] primaryTable, TranspositionTableEntry[] table, long hash, int primarySize, int size, int depth) {
        //Retrieve primary Entry
        TranspositionTableEntry primaryEntry = retrieve(primaryTable, hash, primarySize);
        //Check if not null
        if (primaryEntry != null) {
            //if not null check if hash is correct
            if (primaryEntry.hash != hash) {
                //If hash is not correct use other transposition table
                TranspositionTableEntry entry = retrieve(table, hash, size);
                if (entry != null) {
                    //check if it has the correct hash otherwise return null
                    if (entry.hash == hash) {
                        return entry;
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
            //if correct check if the depth is bigger than Fianco.AI.search depth if so just return
            if (primaryEntry.depth >= depth) {
                return primaryEntry;
            } else {
                //if entry depth is smaller than Fianco.AI.search depth check if there is a better entry in the other transposition table
                TranspositionTableEntry entry = retrieve(table, hash, size);
                if (entry == null) {
                    return null;
                }
                if (entry.hash == hash) {
                    return primaryEntry.depth > entry.depth ? primaryEntry : entry;
                } else {
                    return null;
                }
            }
        } else {
            // If there is no primary entry try to retrieve from the other one
            TranspositionTableEntry entry = retrieve(table, hash, size);
            if (entry != null) {
                if (entry.hash == hash) {
                    return entry;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

}

