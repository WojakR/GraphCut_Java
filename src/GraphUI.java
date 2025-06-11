import javax.swing.*;
import java.awt.BorderLayout;
import java.util.Arrays;

public class GraphUI extends JFrame {
    private Graph graph;
    private GraphPanel graphPanel;
    private JTextField divisionsField;
    private JTextField marginField;

    public GraphUI() {
        setTitle("Graph Partitioning");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

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

        graphPanel = new GraphPanel();
        add(graphPanel, BorderLayout.CENTER);

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
                graphPanel.setGraph(graph); // Przekazujemy graf do panelu
                repaint(); // Odświeżamy widok
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "An error occured while loading text file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadGraphFromBin() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                graph = TempGraphIO.loadGraph(fc.getSelectedFile().getAbsolutePath(), 1);
                graphPanel.setGraph(graph);
                repaint();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "An error occured while loading binary file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveGraph() {
        if (graph == null) {
            JOptionPane.showMessageDialog(this, "No graph to save.");
            return;
        }

        int maxGroup = Arrays.stream(graph.getVertices())
                             .mapToInt(Vertex::getPartitionID)
                             .max().orElse(0);

        Integer[] groupOptions = new Integer[maxGroup + 1];
        for (int i = 0; i <= maxGroup; i++) groupOptions[i] = i;

        Integer selectedGroup = (Integer) JOptionPane.showInputDialog(
            this,
            "Which group do you want to save ?",
            "Choose Partition",
            JOptionPane.QUESTION_MESSAGE,
            null,
            groupOptions, 
            groupOptions[0]);

        if (selectedGroup == null) return;

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose location to save the graph");

        int result = fc.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;
        
        String path = fc.getSelectedFile().getAbsolutePath();

        // Ask about binary/text mode
        Object[] options = {"Text", "Binary"};
        int mode = JOptionPane.showOptionDialog(this,
                "Choose output format:",
                "Save Format",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (mode == -1) return;

        boolean isBinary = (mode == 1);

        // .csrrg extension
        if (!path.endsWith(".csrrg")) path += ".csrrg";

        int[] assignment = new int[graph.numVertices()];
        for (int i = 0; i < assignment.length; i++) {
            assignment[i] = graph.getVertex(i).getPartitionID();
        }
                
        TempGraphIO.savePartition(graph, assignment, selectedGroup, path, isBinary);
        JOptionPane.showMessageDialog(this, "Group " + selectedGroup + " saved to:\n" + path);
    }

    private void runPartitioning() {
        if (graph == null) return;

        try {
            int parts = Integer.parseInt(divisionsField.getText());
            double margin = Double.parseDouble(marginField.getText());

            int[] assignment = new int[graph.numVertices()];
            StringBuilder infoBuilder = new StringBuilder();
            double[] finalMargin = {margin};

            boolean success = Partition.cutGraph(graph, parts, margin, assignment, m -> {
                infoBuilder.append("Margin increased to: ").append(m).append("%\n");
                finalMargin[0] = m;
            });

            if (!success) {
                graph.clearGroups();
                JOptionPane.showMessageDialog(this,
                    "Failed to find the correct graph partition.\n" +
                    infoBuilder.toString(),
                    "Partitioning Error",
                    JOptionPane.ERROR_MESSAGE);
            } else if (finalMargin[0] > margin) {
                JOptionPane.showMessageDialog(this,
                    "Warning: To get the correct partition, margin has been increased to " + finalMargin[0] + "%",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            }

            graph.removeCutEdges(assignment);
            graphPanel.setGraph(graph);
            graphPanel.repaint();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Error: Input correct whole or floating point numbers.",
                "Input data error",
                JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(GraphUI::new);
    }
}