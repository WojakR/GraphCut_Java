import javax.swing.*;
import java.awt.BorderLayout;
// import java.awt.Color;
// import java.awt.Graphics;
import java.awt.Point;
// import java.awt.event.*;
// import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GraphUI extends JFrame {
    private Graph graph;
    private GraphPanel graphPanel;
    private JTextField divisionsField;
    private JTextField marginField;

    public GraphUI() {
        setTitle("Graph Partitioning");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem loadTxt = new JMenuItem("Load from text");
        JMenuItem loadBin = new JMenuItem("Load from binary");
        JMenuItem save = new JMenuItem("Save graph");
        fileMenu.add(loadTxt);
        fileMenu.add(loadBin);
        fileMenu.add(save);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Graph panel
        graphPanel = new GraphPanel();
        add(graphPanel, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel();
        controlPanel.add(new JLabel("Number of divisions:"));
        divisionsField = new JTextField("2", 5);
        controlPanel.add(divisionsField);

        controlPanel.add(new JLabel("Margin (%):"));
        marginField = new JTextField("10", 5);
        controlPanel.add(marginField);

        JButton partitionBtn = new JButton("Divide");
        controlPanel.add(partitionBtn);
        add(controlPanel, BorderLayout.SOUTH);

        // Input handling
        loadTxt.addActionListener(e -> loadGraphFromTxt());
        loadBin.addActionListener(e -> loadGraphFromBin());
        save.addActionListener(e -> saveGraph());
        partitionBtn.addActionListener(e -> runPartitioning());

        setVisible(true);
    }

    private void loadGraphFromTxt() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                graph = TempGraphIO.loadGraph(fc.getSelectedFile().getAbsolutePath(), 1);
                graphPanel.setGraph(graph);
                assignRandomCoordinates();
                repaint();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "An error occured while loading text file");
            }
        }
    }

    private void loadGraphFromBin() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                graph = TempGraphIO.loadGraph(fc.getSelectedFile().getAbsolutePath(), 0);
                graphPanel.setGraph(graph);
                assignRandomCoordinates();
                repaint();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "An error occured while loading binary file");
            }
        }
    }

    private void saveGraph() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                TempGraphIO.saveGraph(graph, fc.getSelectedFile().getAbsolutePath(), true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "An error occured while saving the file");
            }
        }
    }

    private void runPartitioning() {
        try {
            int k = Integer.parseInt(divisionsField.getText());
            int margin = (int)(Double.parseDouble(marginField.getText()));
            int[] assignment = new int[graph.numVertices()];
            Random rand = new Random();
            for (int i = 0; i < assignment.length; i++) {
                assignment[i] = rand.nextInt(k);
            }
            KernighanLin.refine(graph, assignment, margin);
            graph.setPartitions(assignment);
            graphPanel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "An error occured during graph partition: " + e.getMessage());
        }
    }

    private void assignRandomCoordinates() {
        int width = 700;
        int height = 450;
        Random rand = new Random();
        List<Point> used = new ArrayList<>();

        for (Vertex v : graph.getVertices()) {
            int tries = 0;
            Point p;
            do {
                int x = rand.nextInt(width - 40) + 20;
                int y = rand.nextInt(height - 40) + 20;
                p = new Point(x, y);
                tries++;
            } while (isTooClose(p, used) && tries < 100);

            used.add(p);
            v.setCoordinates(p.x, p.y);
        }
    }

    private boolean isTooClose(Point p, List<Point> used) {
        for (Point q : used) {
            if (p.distance(q) < 20) return true;
        }
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GraphUI::new);
    }
}