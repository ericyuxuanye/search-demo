package com.company;

import java.awt.*;
import java.util.PriorityQueue;

/**
 * Greedy best first search is similar to A star, but does not take distance from start into account. As a result,
 * it only chooses the node that is estimated to be the closest to the destination each step. It usually requires less
 * search steps than A star, but does not guarantee the shortest path.
 * @see AStar
 */
public class Greedy extends SearchAlgorithm {

    private static class Node extends State implements Comparable<Node> {

        public int cost;

        public Node(Point point, int cost, State prev) {
            super(point, prev);
            this.cost = cost;
        }

        @Override
        public int compareTo(Node o) {
            return cost - o.cost;
        }
    }

    private final PriorityQueue<Node> pq = new PriorityQueue<>();
    private final boolean[][] visited;

    public Greedy(boolean[][] grid, int x1, int y1, int x2, int y2) {
        super(grid, x1, y1, x2, y2);
        visited = new boolean[grid.length][grid[0].length];
        pq.add(new Node(new Point(x1, y1), 0, null));
    }

    private int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(y2 - y1) + Math.abs(x2 - x1);
    }

    private int distanceSquared(int x1, int y1, int x2, int y2) {
        int xDist = x2 - x1;
        int yDist = y2 - y1;
        return xDist * xDist + yDist * yDist;
    }

    @Override
    public Point next() {
        Node curr;
        do {
            curr = pq.remove();
        } while (visited[curr.point.x][curr.point.y]);
        if (curr.point.equals(dest)) {
            notDone = false;
            currentState = curr;
        } else {
            visited[curr.point.x][curr.point.y] = true;
            State finalCurr = curr;
            applyLambdaToNeighbors(curr.point.x, curr.point.y, (x, y) -> {
                if (!visited[x][y]) {
                        int fScore = manhattanDistance(x, y, dest.x, dest.y);
                        pq.add(new Node(new Point(x, y), fScore, finalCurr));
                    }
            });
        }
        return curr.point;
    }
}
