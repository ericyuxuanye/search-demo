package com.company;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Class used to display a grid that is used to visualize pathfinding algorithms. Includes methods to search with
 * a particular algorithm, generate mazes, and do operations such as clearing the path/board.
 */
public class GridPanel extends JComponent implements MouseListener, MouseMotionListener {

    /////////////
    // constants
    /////////////

    /**
     * Size of each cell in grid
     */
    private static final int GRID_SIZE = 10;

    /**
     * Width of grid
     */
    private static final int WIDTH = 101;

    /**
     * Height of grid
     */
    private static final int HEIGHT = 51;

    /**
     * Color used to highlight the cells the search algorithm has looked at
     */
    private static final Color SEARCH_COLOR = new Color(0xFF2A9FFF, true);

    /**
     * Random number generator
     */
    private static final Random rand = new Random();


    /////////////////////
    // search algorithms
    /////////////////////

    /**
     * Depth first search
     */
    public static final int DFS = 0;

    /**
     * Breadth first search
     */
    public static final int BFS = 1;

    /**
     * A* search
     */
    public static final int A_STAR = 2;

    /**
     * Greedy best first search
     */
    public static final int GREEDY = 3;


    /////////////////////////////////////
    // Storage used for generating mazes
    /////////////////////////////////////

    /**
     * To keep track of which cells are already visited when generating DFS maze
     */
    private final boolean[][] visited = new boolean[WIDTH+2][HEIGHT+2];

    /**
     * This array will be shuffled, so the neighbors will be visited in random order
     */
    private final int[] neighbors = {0, 1, 2, 3};

    /**
     * Table to get x direction given an index between 0 and 4 exclusive
     */
    private static final int[] X_DIR = {-2, 0, 0, 2};

    /**
     * Table to get y direction given an index between 0 and 4 exclusive
     */
    private static final int[] Y_DIR = {0, -2, 2, 0};


    //////////////////
    // Data variables
    //////////////////

    /**
     * Grid that is true if there is a wall
     */
    private final boolean[][] grid;

    /**
     * Cache the grid lines, so we don't have to draw it again
     */
    private final BufferedImage gridLines;

    /**
     * BufferedImage that the colored cells get drawn on
     */
    private final BufferedImage blocks =
            new BufferedImage(WIDTH * GRID_SIZE, HEIGHT * GRID_SIZE, BufferedImage.TYPE_INT_RGB);

    /**
     * Graphics object for the image
     */
    private final Graphics2D blocksGraphics = blocks.createGraphics();

    /**
     * X coordinate of starting cell
     */
    private int startX;

    /**
     * Y coordinate of starting cell
     */
    private int startY;

    /**
     * X coordinate of target cell
     */
    private int endX;

    /**
     * Y coordinate of target cell
     */
    private int endY;

    /**
     * The algorithm to use for searching that is chosen in search method
     */
    private SearchAlgorithm searchAlgorithm;


    ///////////////////////////
    // Mouse related variables
    ///////////////////////////

    /**
     * Whether the mouse is on the panel
     */
    private boolean mouseOn = false;

    /**
     * x coordinate of mouse (the unit is the cell size, not pixels)
     */
    private int mouseX;

    /**
     * y coordinate of mouse (the unit is the cell size, not pixels)
     */
    private int mouseY;

    /**
     * X coordinate of last toggled cell
     */
    private int lastTileX = -1;

    /**
     * Y coordinate of last toggled cell
     */
    private int lastTileY = -1;

    /**
     * Whether the mouse is on the start block
     */
    private boolean onStart = false;

    /**
     * Whether the mouse is on the end block
     */
    private boolean onEnd = false;

    /**
     * Whether the panel should be disabled because a search algorithm is running
     */
    private boolean disabled = false;


    //////////////////////////////////
    // Timers used to play out search
    //////////////////////////////////

    /**
     * Timer to retrace steps
     */
    private final Timer retraceTimer = new Timer(10, e -> {
        if (searchAlgorithm.hasMoreStepsToRetrace()) {
            Point np = searchAlgorithm.retrace();
            blocksGraphics.fillRect((np.x - 1) * GRID_SIZE, (np.y - 1) * GRID_SIZE, GRID_SIZE, GRID_SIZE);
            repaint();
        } else {
            ((Timer)e.getSource()).stop();
            disabled = false;
            Main.enableButtons(true);
        }
    });

    /**
     * Timer to get next step for the search algorithm
     */
    private final Timer searchTimer = new Timer(10, e -> {
        if (searchAlgorithm.hasNext()) {
            Point next;
            try {
                next = searchAlgorithm.next();
            } catch (Exception exception) {
                // The search algorithm probably ran out of places to visit.
                JOptionPane.showMessageDialog(this.getRootPane(),
                        "Search algorithm finished without reaching target", "Cannot find Target",
                        JOptionPane.ERROR_MESSAGE);
                exception.printStackTrace();
                ((Timer)e.getSource()).stop();
                disabled = false;
                Main.enableButtons(true);
                // exit
                return;
            }
            blocksGraphics.fillRect((next.x - 1) * GRID_SIZE, (next.y - 1) * GRID_SIZE, GRID_SIZE, GRID_SIZE);
            repaint();
        } else {
            // done searching and found target. Retrace steps
            ((Timer)e.getSource()).stop();
            blocksGraphics.setColor(Color.YELLOW);
            retraceTimer.start();
        }
    });

    /**
     * Constructs a new GridPanel object
     */
    public GridPanel() {
        super();
        addMouseListener(this);
        addMouseMotionListener(this);
        setPreferredSize(new Dimension(WIDTH * GRID_SIZE, HEIGHT * GRID_SIZE));
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());

        // initialize grid
        grid = new boolean[WIDTH+2][HEIGHT+2];
        // make borders a wall
        for (int i = 0; i < WIDTH + 2; i++) {
            grid[i][0] = true;
            grid[i][HEIGHT + 1] = true;
        }
        for (int i = 1; i < HEIGHT + 2; i++) {
            grid[0][i] = true;
            grid[WIDTH+1][i] = true;
        }

        // initialize grid lines buffered image
        gridLines = new BufferedImage(WIDTH * GRID_SIZE, HEIGHT * GRID_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics g = gridLines.getGraphics();
        g.setColor(Color.BLACK);
        for (int i = 0; i <= WIDTH * GRID_SIZE; i+=GRID_SIZE) {
            g.drawLine(i, 0, i, HEIGHT * GRID_SIZE);
        }
        for (int i = 0; i <= HEIGHT * GRID_SIZE; i+=GRID_SIZE) {
            g.drawLine(0, i, WIDTH * GRID_SIZE, i);
        }
        // initialize blocks
        blocksGraphics.setColor(Color.WHITE);
        blocksGraphics.fillRect(0, 0, WIDTH * GRID_SIZE, HEIGHT * GRID_SIZE);

        // initialize start and stop locations
        startX = 18;
        startY = 26;
        endX = 80;
        endY = 26;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // draw blocks
        g.drawImage(blocks, 0, 0, null);

        // draw grid lines
        g.drawImage(gridLines, 0, 0, null);

        // draw start location
        g.setColor(Color.GREEN);
        g.fillRect(startX * GRID_SIZE, startY * GRID_SIZE, GRID_SIZE, GRID_SIZE);

        // draw end location
        g.setColor(Color.RED);
        g.fillRect(endX * GRID_SIZE, endY * GRID_SIZE, GRID_SIZE, GRID_SIZE);

        // draw mouse position
        if (mouseOn) {
            g.setColor(Color.GRAY);
            g.fillRect(mouseX * GRID_SIZE + 3, mouseY * GRID_SIZE + 3, 5, 5);
        }
    }

    /**
     * Fills the grid randomly
     */
    public void fillRandom() {
        for (int i = 1; i <= WIDTH; i++) {
            for (int j = 1; j <= HEIGHT; j++) {
                grid[i][j] = rand.nextDouble() < 0.3;
            }
        }
        drawGridOnImage();
        repaint();
    }

    /**
     * Fills the grid with a maze generated using depth first search
     */
    public void dfsMaze() {
        for (int i = 1; i <= WIDTH; i++) {
            for (int j = 1; j <= HEIGHT; j++) {
                grid[i][j] = true;
                visited[i][j] = false;
            }
        }
        // to store next cell to visit
        ArrayDeque<Point> st = new ArrayDeque<>();
        // keep track of parent
        ArrayDeque<Point> prev = new ArrayDeque<>();
        st.push(new Point(1, 1));
        while (!st.isEmpty()) {
            Point curr = st.pop();
            Point last = prev.isEmpty() ? null : prev.pop();
            if (visited[curr.x][curr.y]) continue;
            visited[curr.x][curr.y] = true;
            grid[curr.x][curr.y] = false;
            // add neighbors randomly
            shuffleNeighborsArray();
            for (int n : neighbors) {
                int x = curr.x + X_DIR[n];
                int y = curr.y + Y_DIR[n];
                if (withinCenterOfGrid(x, y) && !visited[x][y]) {
                    st.push(new Point(x, y));
                    prev.push(curr);
                }
            }
            if (last != null) {
                // connect current and previous cell
                int xMid = (curr.x + last.x) / 2;
                int yMid = (curr.y + last.y) / 2;
                grid[xMid][yMid] = false;
            }
        }
        drawGridOnImage();
        repaint();
    }

    /**
     * Fisher Yates algorithm to shuffle the neighbors array
     */
    private void shuffleNeighborsArray() {
        for (int i = 3; i >= 0; i--) {
            int j = rand.nextInt(i+1);
            // swap
            int temp = neighbors[i];
            neighbors[i] = neighbors[j];
            neighbors[j] = temp;
        }
    }

    /**
     * Generate a maze using Eller's algorithm. Since the algorithm only looks at one row at a time, a cool property
     * is that it can generate infinitely long mazes forever!
     */
    public void ellerMaze() {
        // clear grid
        for (int i = 1; i <= WIDTH; i++) {
            for (int j = 1; j <= HEIGHT; j++) {
                grid[i][j] = false;
            }
        }
        final int size = HEIGHT / 2 + 1;
        int[] curr = new int[size];
        // number: indexes that are that number
        HashMap<Integer, ArrayList<Integer>> setElements = new HashMap<>();
        // make current unique
        for (int i = 0; i < size; i++) {
            curr[i] = i;
            setElements.put(i, new ArrayList<>(List.of(i)));
        }
        // to generate new set numbers
        int cnt = size;

        // process all rows but last
        for (int i = 1; i < WIDTH; i+=2) {
            //int prev = 0;
            for (int j = 1; j < size; j++) {
                if (curr[j - 1] != curr[j] && rand.nextInt(5) < 3) {
                    // join sets
                    ArrayList<Integer> temp = setElements.remove(curr[j]);
                    for (int element : temp) {
                        // relabel the id for every element in the hash map that matched the set we are going to join
                        curr[element] = curr[j-1];
                    }
                    // it will always be present
                    setElements.get(curr[j]).addAll(temp);
                } else {
                    // make wall
                    grid[i][j * 2] = true;
                    //prev = curr[j];
                }
            }
            Arrays.fill(curr, -1);
            // randomly add bottom walls for each set
            for (Map.Entry<Integer, ArrayList<Integer>> e : setElements.entrySet()){
                int k = e.getKey();
                ArrayList<Integer> v = e.getValue();
                // At least one bottom wall per set
                 int numToAdd = v.size() == 1 ? 1 : rand.nextInt(v.size() / 2) + 1;
                Collections.shuffle(v);
                for (int j = 0; j < numToAdd; j++) {
                    curr[v.get(j)] = k;
                }
                // trim
                List<Integer> temp = v.subList(numToAdd, v.size());
                for (int n : temp) {
                    grid[i+1][n * 2 + 1] = true;
                }
                temp.clear();
            }
            for (int j = 0; j < curr.length; j++) {
                if (curr[j] == -1) {
                    //System.out.println(j);
                    curr[j] = cnt;
                    //grid[i+1][j*2+1] = true;
                    setElements.put(cnt, new ArrayList<>(List.of(j)));
                    cnt++;
                }
            }
            // walls on bottom
            for (int j = 2; j < HEIGHT; j+=2) {
                grid[i+1][j] = true;
            }
        }
        // last iteration
        for (int i = 1; i < size; i++) {
            if (curr[i] == curr[i-1]) {
                // add wall
                grid[WIDTH][i * 2] = true;
            } else {
                // join sets
                ArrayList<Integer> temp = setElements.remove(curr[i]);
                for (int element : temp) {
                    curr[element] = curr[i-1];
                }
                setElements.get(curr[i]).addAll(temp);
            }
        }
        drawGridOnImage();
        repaint();
    }

    /**
     * To store two coordinate points
     */
    private record TwoPoints(int x1, int y1, int x2, int y2) {}

    /**
     * Generate a maze using randomized Kruskal's algorithm
     */
    public void kruskalMaze() {
        // fill grid with walls
        for (int i = 1; i <= WIDTH; i++) {
            for (int j = 1; j <= HEIGHT; j++) {
                grid[i][j] = true;
            }
        }
        ArrayList<TwoPoints> edges = new ArrayList<>();
        for (int i = 1; i <= WIDTH; i+=2) {
            for (int j = 1; j <= HEIGHT; j+=2) {
                if (withinCenterOfGrid(i + 2, j)) edges.add(new TwoPoints(i, j, i + 2, j));
                if (withinCenterOfGrid(i, j + 2)) edges.add(new TwoPoints(i, j, i, j + 2));
            }
        }
        Collections.shuffle(edges);
        DisjointSetUnion set = new DisjointSetUnion((WIDTH + 2) * (HEIGHT + 2));
        // carve it out
        for (TwoPoints edge : edges) {
            int id1 = edge.y1() * WIDTH + edge.x1();
            int id2 = edge.y2() * WIDTH + edge.x2();
            if (set.get(id1) != set.get(id2)) {
                set.unite(id1, id2);
                int midX = (edge.x1() + edge.x2()) / 2;
                int midY = (edge.y1() + edge.y2()) / 2;
                grid[midX][midY] = false;
                grid[edge.x1()][edge.y1()] = false;
                grid[edge.x2()][edge.y2()] = false;
            }
        }
        drawGridOnImage();
        repaint();
    }

    /**
     * Whether the coordinates are within the viewable area of the grid
     * @param x the x coordinate
     * @param y the y coordinate
     * @return whether x &gt;= 1 and x &lt;= WIDTH and y &gt;= 1 and y &lt;= HEIGHT
     */
    private boolean withinCenterOfGrid(int x, int y) {
        return x >= 1 && x <= WIDTH && y >= 1 && y <= HEIGHT;
    }

    /**
     * Clears the path that the search algorithm drew
     */
    public void clearPath() {
        drawGridOnImage();
        repaint();
    }

    /**
     * Draws the grid onto the blocksGraphics buffered image. true becomes black while false becomes white
     */
    private void drawGridOnImage() {
        blocksGraphics.setColor(Color.WHITE);
        blocksGraphics.fillRect(0, 0, GRID_SIZE * WIDTH, GRID_SIZE * HEIGHT);
        blocksGraphics.setColor(Color.BLACK);
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (grid[i+1][j+1]) blocksGraphics.fillRect(i * GRID_SIZE, j * GRID_SIZE, GRID_SIZE, GRID_SIZE);
            }
        }
    }

    /**
     * Initializes the search with the specified algorithm.
     * @param algorithm the algorithm to use: DFS, BFS, A_STAR, or GREEDY
     */
    public void search(int algorithm) {
        Main.enableButtons(false);
        disabled = true;
        // make start and end empty to make sure the user does not get confused why the algorithm cannot search
        grid[startX+1][startY+1] = false;
        grid[endX+1][endY+1] = false;
        // erase paths that were previously drawn
        clearPath();
        searchAlgorithm = switch (algorithm) {
            case DFS -> new DepthFirstSearch(grid, startX + 1, startY + 1, endX + 1, endY + 1);
            case BFS -> new BreadthFirstSearch(grid, startX + 1, startY + 1, endX + 1, endY + 1);
            case A_STAR -> new AStar(grid, startX + 1, startY + 1, endX + 1, endY + 1);
            case GREEDY -> new Greedy(grid, startX + 1, startY + 1, endX + 1, endY + 1);
            default -> throw new IllegalStateException("Unexpected value: " + algorithm);
        };
        blocksGraphics.setColor(SEARCH_COLOR);
        searchTimer.start();
    }

    /**
     * Stop searching
     */
    public void stop() {
        searchTimer.stop();
        retraceTimer.stop();
        disabled = false;
        Main.enableButtons(true);
    }

    /**
     * Clears the grid
     */
    public void reset() {
        for (int i = 1; i <= WIDTH; i++) {
            for (int j = 1; j <= HEIGHT; j++) {
                grid[i][j] = false;
            }
        }
        blocksGraphics.setColor(Color.WHITE);
        blocksGraphics.fillRect(0, 0, WIDTH * GRID_SIZE, HEIGHT * GRID_SIZE);
        repaint();
    }

    /**
     * Returns whether x is between 0 (inclusive) and the width of grid in cells (exclusive),
     * and y is between 0 (inclusive) and the height of grid in cells (exclusive)
     * @param x x coordinate
     * @param y y coordinate
     * @return whether x and y are valid values
     */
    public boolean withinBounds(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (disabled) return;
        if (mouseX == startX && mouseY == startY) {
            onStart = true;
            return;
        }
        if (mouseX == endX && mouseY == endY) {
            onEnd = true;
            return;
        }
        // flip the value of current block, and set color appropriately
        if (grid[mouseX +1][mouseY +1]) {
            grid[mouseX +1][mouseY +1] = false;
            blocksGraphics.setColor(Color.WHITE);
        } else {
            grid[mouseX +1][mouseY +1] = true;
            blocksGraphics.setColor(Color.BLACK);
        }
        blocksGraphics.fillRect(mouseX * GRID_SIZE, mouseY * GRID_SIZE, GRID_SIZE, GRID_SIZE);
        lastTileX = mouseX;
        lastTileY = mouseY;
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        lastTileX = lastTileY = -1;
        onStart = onEnd = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mouseOn = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouseOn = false;
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // set mouse location
        mouseX = e.getX() / GRID_SIZE;
        mouseY = e.getY() / GRID_SIZE;
        if (!withinBounds(mouseX, mouseY)) {
            // mouse is not on
            mouseOn = false;
            return;
        }
        // mouse is on
        mouseOn = true;
        // don't do anything further if disabled
        // (no need to repaint if disabled because the search algorithm timer is doing that)
        if (disabled) return;
        if (onStart) {
            startX = mouseX;
            startY = mouseY;
            repaint();
            return;
        }
        if (onEnd) {
            endX = mouseX;
            endY = mouseY;
            repaint();
            return;
        }
        if (mouseX != lastTileX || mouseY != lastTileY) {
            if (grid[mouseX + 1][mouseY + 1]) {
                grid[mouseX + 1][mouseY + 1] = false;
                blocksGraphics.setColor(Color.WHITE);
            } else {
                grid[mouseX + 1][mouseY + 1] = true;
                blocksGraphics.setColor(Color.BLACK);
            }
            blocksGraphics.fillRect(mouseX * GRID_SIZE, mouseY * GRID_SIZE, GRID_SIZE, GRID_SIZE);
            lastTileX = mouseX;
            lastTileY = mouseY;
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX() / GRID_SIZE;
        mouseY = e.getY() / GRID_SIZE;
        // no need to repaint if disabled because the search algorithm timer is doing that
        if (!disabled) repaint();
    }
}