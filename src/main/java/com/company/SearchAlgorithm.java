package com.company;

import java.awt.*;
import java.util.function.BiConsumer;

/**
 * An algorithm that allows searching on a grid. It can do the following:
 * <ol>
 *     <li>get next square to search</li>
 *     <li>Check if algorithm is done</li>
 *     <li>Retrace steps that algorithm took</li>
 * </ol>
 *
 * Classes that extend this class must implement the next method, to find the next point to search on,
 * connect all States to the start State each step, and update the currentState variable when done
 */
public abstract class SearchAlgorithm {

    /**
     * Location of destination
     */
    protected final Point dest;
    /**
     * The grid to search
     */
    protected final boolean[][] grid;
    /**
     * Current State object, set to the destination object when done. This should link to previous states,
     * which in turn link to previous states, until the start state is reached.
     */
    protected State currentState;
    /**
     * Whether the search is done
     */
    protected boolean notDone = true;

    public SearchAlgorithm(boolean[][] grid, int x1, int y1, int x2, int y2) {
        dest = new Point(x2, y2);
        this.grid = grid;
    }

    /**
     * Applies a lambda function to all the neighboring squares that is not a wall
     * @param x x coordinate of square to search from
     * @param y y coordinate of square to search from
     * @param bc a lambda function that accepts two integers: the x and y coordinate of the neighboring square
     */
    public void applyLambdaToNeighbors(int x, int y, BiConsumer<Integer, Integer> bc) {
        if (!grid[x-1][y]) bc.accept(x-1, y);
        if (!grid[x][y-1]) bc.accept(x, y-1);
        if (!grid[x][y+1]) bc.accept(x, y+1);
        if (!grid[x+1][y]) bc.accept(x+1, y);
    }

    /**
     * To store the current point and previous state. Sub-classes requiring more functionality should extend this
     */
    protected static class State {
        public Point point;
        public State prev;
        public State(Point point, State prev) {
            this.point = point;
            this.prev = prev;
        }
    }

    /**
     * This should tell us the next point the algorithm visits
     * @return a Point object
     */
    public abstract Point next();

    /**
     * Tells us whether there are more points to explore
     * @return true if there is
     */
    public boolean hasNext() {
        return notDone;
    }

    /**
     * Checks if there are more steps to retrace
     * @return true if there are more steps to retrace
     */
    public boolean hasMoreStepsToRetrace() {
        return currentState != null;
    }

    /**
     * Retraces one step
     * @return A Point object representing the previous step
     */
    public Point retrace() {
        Point temp = currentState.point;
        currentState = currentState.prev;
        return temp;
    }
}
