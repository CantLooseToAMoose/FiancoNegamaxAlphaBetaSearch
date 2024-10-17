package search.MoveOrdering;

import java.util.PriorityQueue;

public class MoveWithStats implements Comparable<MoveWithStats> {
    private final short move;
    private final double value;

    public MoveWithStats(short move, double meanValue) {
        this.move = move;
        this.value = meanValue;
    }

    public short getMove() {
        return move;
    }

    @Override
    public int compareTo(MoveWithStats other) {
        return Double.compare(other.value, this.value); // Max-heap behavior
    }


    public static void UpdateMoveStats(MoveStats[] moveStats, short move, int value) {
        if (moveStats[move] == null) {
            moveStats[move] = new MoveStats();
        }
        moveStats[move].update(value);
    }

    public static void AddMoveWithMeanToPriorityQueue(PriorityQueue<MoveWithStats> moveQueue, MoveStats[] moveStats, short move) {
        if (moveStats[move] == null) {
            moveStats[move] = new MoveStats();
        }
        double meanValue = moveStats[move].getMean();
        moveQueue.add(new MoveWithStats(move, meanValue));
    }

    public static void AddMoveWithValueToPriorityQueue(PriorityQueue<MoveWithStats> moveQueue, short move, double value) {
        MoveWithStats moveWithStats = new MoveWithStats(move, value);
        moveQueue.add(moveWithStats);
    }
}

