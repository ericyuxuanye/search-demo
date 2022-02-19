package com.company;

import java.awt.*;
import java.util.Queue;
import java.util.ArrayDeque;

/**
 * Searches all closer nodes before searching farther nodes. Guarantees the shortest path.
 */
public class BreadthFirstSearch extends SearchAlgorithm {

    Queue<State> q = new ArrayDeque<>();
    boolean[][] visited;

    public BreadthFirstSearch(boolean[][] grid, int startX, int startY, int endX, int endY) {
        super(grid, startX, startY, endX, endY);
        visited = new boolean[grid.length][grid[0].length];
        q.add(new State(new Point(startX, startY), null));
    }

    @Override
    public Point next() {
        State curr = q.remove();
        Point currPoint = curr.point;
        if (currPoint.equals(dest)) {
            notDone = false;
            currentState = curr;
        } else {
            applyLambdaToNeighbors(currPoint.x, currPoint.y, (x, y) -> {
                if (!visited[x][y]) {
                    q.add(new State(new Point(x, y), curr));
                    visited[x][y] = true;
                }
            });
        }
        return currPoint;
    }
}
