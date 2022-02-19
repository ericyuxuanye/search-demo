package com.company;

import java.awt.*;
import java.util.PriorityQueue;

/**
 * Uses both the distance and heuristics (estimated distance to destination) to determine the next node to search. As a
 * result, it is usually able to be much faster than breadth first search, even though it guarantees the shortest path.
 * @see BreadthFirstSearch
 */
public class AStar extends SearchAlgorithm {

    private class Node extends State implements Comparable<Node> {

        public int cost;

        public Node(Point point, int cost, State prev) {
            super(point, prev);
            this.cost = cost;
        }

        @Override
        public int compareTo(Node o) {
            // prioritize nodes closer to the end if they are equal to speed up search
            if (cost == o.cost) return distances[o.point.x][o.point.y] - distances[point.x][point.y];
            return cost - o.cost;
        }
    }

    private final PriorityQueue<Node> pq = new PriorityQueue<>();
    private final int[][] distances;
    private final boolean[][] visited;

    public AStar(boolean[][] grid, int x1, int y1, int x2, int y2) {
        super(grid, x1, y1, x2, y2);
        distances = new int[grid.length][grid[0].length];
        for (int i = 0; i < distances.length; i++) {
            for (int j = 0; j < distances[0].length; j++) {
                distances[i][j] = Integer.MAX_VALUE;
            }
        }
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
            int distanceToCurr = distances[curr.point.x][curr.point.y];
            Node finalCurr = curr;
            applyLambdaToNeighbors(curr.point.x, curr.point.y, (x, y) -> {
                if (!visited[x][y]) {
                    int distance = distanceToCurr + 1;
                    if (distance < distances[x][y]) {
                        distances[x][y] = distance;
                        int fScore = distance + manhattanDistance(x, y, dest.x, dest.y);
                        pq.add(new Node(new Point(x, y), fScore, finalCurr));
                    }
                }
            });
        }
        return curr.point;
    }
}
