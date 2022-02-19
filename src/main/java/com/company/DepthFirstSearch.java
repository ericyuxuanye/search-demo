package com.company;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Stack;

/**
 * Searches the next node on the stack until there are no more paths. Then, it backtracks. DFS does not guarantee
 * the shortest path, and is typically not a good algorithm to use for pathfinding.
 */
public class DepthFirstSearch extends SearchAlgorithm {

    ArrayDeque<State> locations = new ArrayDeque<>();
    boolean[][] visited;
    public DepthFirstSearch(boolean[][] g, int x1, int y1, int x2, int y2) {
        super(g, x1, y1, x2, y2);
        locations.push(new State(new Point(x1, y1), null));
        visited = new boolean[g.length][g[0].length];
    }

    @Override
    public Point next() {
        State curr;
        // loop because the current one might already be visited
        do {
            curr = locations.pop();
        } while (visited[curr.point.x][curr.point.y]);
        if (curr.point.equals(dest)) {
            notDone = false;
            currentState = curr;
        } else {
            visited[curr.point.x][curr.point.y] = true;
            State finalCurr = curr;
            // add neighbors
            applyLambdaToNeighbors(curr.point.x, curr.point.y, (x, y) -> {
                if (!visited[x][y]) {
                    locations.push(new State(new Point(x, y), finalCurr));
                }
            });
        }
        return curr.point;
    }
}
