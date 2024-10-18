package search.MoveOrdering;

import java.util.PriorityQueue;

public class MoveWithStats implements Comparable<MoveWithStats> {
    private final short move;
    private final double value;
    private final int gameMove;

    public MoveWithStats(short move, double meanValue, int gameMove) {
        this.move = move;
        this.value = meanValue;
        this.gameMove = gameMove;
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

    public static void AddMoveWithMeanToPriorityQueue(PriorityQueue<MoveWithStats> moveQueue, MoveStats[] moveStats, short move,int gameMove) {
        if (moveStats[move] == null) {
            moveStats[move] = new MoveStats();
        }
        double meanValue = moveStats[move].getMean();
        moveQueue.add(new MoveWithStats(move, meanValue,gameMove));
    }

    public static void AddMoveWithValueToPriorityQueue(PriorityQueue<MoveWithStats> moveQueue, short move, double value,int gameMove) {
        MoveWithStats moveWithStats = new MoveWithStats(move, value,gameMove);
        moveQueue.add(moveWithStats);
    }
}

