package com.company;

// use a modern looking look and feel
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

/**
 * Let's make everything static, so it is all really easy to access
 */
public class Main {

    private static GridPanel gp;
    private static JButton stop;
    private static JButton clearBoard;
    private static JButton clearPath;
    private static JButton fillRandom;
    private static JButton dfsMaze;
    private static JButton ellerMaze;
    private static JButton kruskalMaze;
    private static JButton depthFirstSearch;
    private static JButton breadthFirstSearch;
    private static JButton aStar;
    private static JButton greedy;


    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::run);
    }

    public static void run() {
        // make graphics efficient with gpu
        System.setProperty("sun.java2d.opengl", "true");
        // set look and feel
        FlatLightLaf.setup();
        gp = new GridPanel();
        JFrame f = new JFrame();
        f.setTitle("Search Algorithm Demo");
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(gp);

        JPanel controls = new JPanel();

        stop = new JButton("Stop");
        stop.setEnabled(false);
        clearBoard = new JButton("Clear Board");
        clearPath = new JButton ("Clear path");
        fillRandom = new JButton("Fill randomly");
        dfsMaze = new JButton("DFS Maze");
        kruskalMaze = new JButton("Kruskal Maze");
        ellerMaze = new JButton("Eller Maze");
        depthFirstSearch = new JButton("DFS");
        breadthFirstSearch = new JButton("BFS");
        aStar = new JButton("A*");
        greedy = new JButton("Greedy");

        stop.addActionListener(e -> gp.stop());
        clearBoard.addActionListener(e -> gp.reset());
        clearPath.addActionListener(e -> gp.clearPath());
        fillRandom.addActionListener(e -> gp.fillRandom());
        dfsMaze.addActionListener(e -> gp.dfsMaze());
        kruskalMaze.addActionListener(e -> gp.kruskalMaze());
        ellerMaze.addActionListener(e -> gp.ellerMaze());
        depthFirstSearch.addActionListener(e -> gp.search(GridPanel.DFS));
        breadthFirstSearch.addActionListener(e -> gp.search(GridPanel.BFS));
        aStar.addActionListener(e -> gp.search(GridPanel.A_STAR));
        greedy.addActionListener(e -> gp.search(GridPanel.GREEDY));

        controls.add(stop);
        controls.add(clearBoard);
        controls.add(clearPath);
        controls.add(fillRandom);
        controls.add(dfsMaze);
        controls.add(kruskalMaze);
        controls.add(ellerMaze);
        controls.add(depthFirstSearch);
        controls.add(breadthFirstSearch);
        controls.add(aStar);
        controls.add(greedy);

        mainPanel.add(controls);
        f.setContentPane(mainPanel);
        f.pack();
        f.setResizable(false);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    /**
     * Enable/Disable all the buttons. The stop button will be the opposite of the boolean.
     * @param isEnabled whether to enable the buttons (opposite of stop button)
     */
    public static void enableButtons(boolean isEnabled) {
        clearBoard.setEnabled(isEnabled);
        clearPath.setEnabled(isEnabled);
        fillRandom.setEnabled(isEnabled);
        dfsMaze.setEnabled(isEnabled);
        kruskalMaze.setEnabled(isEnabled);
        ellerMaze.setEnabled(isEnabled);
        depthFirstSearch.setEnabled(isEnabled);
        breadthFirstSearch.setEnabled(isEnabled);
        aStar.setEnabled(isEnabled);
        greedy.setEnabled(isEnabled);
        // stop button is tne one enabled when the others are disabled
        stop.setEnabled(!isEnabled);
    }
}
