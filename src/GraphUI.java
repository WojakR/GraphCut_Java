// Plik: GraphUI.java (KOMPLETNY I POPRAWIONY)

import javax.swing.*;
import java.awt.BorderLayout;
import java.util.Arrays; // Potrzebne do Arrays.fill
import java.util.Random; // Potrzebne do runPartitioning

public class GraphUI extends JFrame {
    private Graph graph;
    private GraphPanel graphPanel;
    private JTextField divisionsField;
    private JTextField marginField;

    public GraphUI() {
        setTitle("Graph Partitioning");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Menu (bez zmian)
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

        // Panel grafu (bez zmian)
        graphPanel = new GraphPanel();
        add(graphPanel, BorderLayout.CENTER);

        // Panel kontrolny (bez zmian)
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

        // Obsługa zdarzeń (bez zmian)
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
                // Dla plików binarnych często nie ma wielu grafów, więc index 1 może być właściwy, ale zależy to od pliku.
                graph = TempGraphIO.loadGraph(fc.getSelectedFile().getAbsolutePath(), 1);
                graphPanel.setGraph(graph);
                repaint();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "An error occured while loading binary file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- POPRAWIONA METODA ZAPISU ---
    private void saveGraph() {
        if (graph == null) {
            JOptionPane.showMessageDialog(this, "No graph loaded to save.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fc.getSelectedFile().getAbsolutePath();
                // Format zapisu (binarny/tekstowy) na podstawie rozszerzenia pliku
                boolean isBinary = filePath.endsWith("bin");

                // Stwórz tymczasową tablicę 'assignment', gdzie cały graf to jedna partycja (grupa 0)
                int[] fullGraphAssignment = new int[graph.numVertices()];
                Arrays.fill(fullGraphAssignment, 0);

                // Wywołaj metodę zapisu partycji, aby zapisać cały graf jako jedną całość
                TempGraphIO.savePartition(graph, fullGraphAssignment, 0, filePath, isBinary);

                JOptionPane.showMessageDialog(this, "Graph saved successfully to " + filePath);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "An error occured while saving the file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace(); // Pomocne przy debugowaniu
            }
        }
    }

    private void runPartitioning() {
        if (graph == null) {
            JOptionPane.showMessageDialog(this, "Load a graph before partitioning.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            int k = Integer.parseInt(divisionsField.getText());
            int margin = (int)(Double.parseDouble(marginField.getText()));
            int[] assignment = new int[graph.numVertices()];

            // UWAGA: Używamy logiki z Partition.java, aby podział był spójny z aplikacją konsolową
            Partition.cutGraph(graph, k, margin, assignment);

            graph.setPartitions(assignment);
            graphPanel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "An error occured during graph partition: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Usunięto metody assignRandomCoordinates() i isTooClose(), ponieważ powodowały problemy.
    // Logika pozycjonowania jest teraz w GraphPanel.

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GraphUI::new);
    }
}