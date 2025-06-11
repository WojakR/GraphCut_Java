// Plik: GraphPanel.java (KOMPLETNY I POPRAWIONY)

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GraphPanel extends JPanel {
    private Graph graph;
    private final int PADDING = 40; // Margines w pikselach od krawędzi panelu
    private final int NODE_SIZE = 10; // Średnica wierzchołka

    public void setGraph(Graph g) {
        this.graph = g;
        repaint(); // Poproś o przerysowanie, gdy tylko graf zostanie ustawiony
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graph == null || graph.numVertices() == 0) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Krok 1: Znajdź zakres koordynatów logicznych (min/max row i col)
        int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;
        for (Vertex v : graph.getVertices()) {
            if (v.y < minRow) minRow = v.y;
            if (v.y > maxRow) maxRow = v.y;
            if (v.x < minCol) minCol = v.x;
            if (v.x > maxCol) maxCol = v.x;
        }

        // Krok 2: Oblicz współczynniki skalowania, aby dopasować graf do panelu
        int availableWidth = getWidth() - 2 * PADDING;
        int availableHeight = getHeight() - 2 * PADDING;
        double hScale = (maxCol - minCol == 0) ? 1 : (double) availableWidth / (maxCol - minCol);
        double vScale = (maxRow - minRow == 0) ? 1 : (double) availableHeight / (maxRow - minRow);

        // Użyj mniejszego ze współczynników, aby zachować proporcje
        double scale = Math.min(hScale, vScale);

        // Krok 3: Stwórz mapę pozycji na ekranie dla każdego wierzchołka
        Map<Integer, Point> positions = new HashMap<>();
        for (Vertex v : graph.getVertices()) {
            int screenX = PADDING + (int) ((v.x - minCol) * scale);
            int screenY = PADDING + (int) ((v.y - minRow) * scale);
            positions.put(v.id, new Point(screenX, screenY));
        }

        // Krok 4: Narysuj krawędzie
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(1.5f));
        for (Vertex v : graph.getVertices()) {
            Point p1 = positions.get(v.id);
            for (Vertex u : v.getNeighbours()) {
                // Rysuj krawędź tylko raz (np. gdy id v < id u), aby uniknąć podwójnego rysowania
                if (v.id < u.id) {
                    Point p2 = positions.get(u.id);
                    g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }

        // Krok 5: Narysuj wierzchołki
        for (Vertex v : graph.getVertices()) {
            Point p = positions.get(v.id);
            int x = p.x - NODE_SIZE / 2;
            int y = p.y - NODE_SIZE / 2;

            g2d.setColor(getColorForPartition(v.getPartitionID()));
            g2d.fillOval(x, y, NODE_SIZE, NODE_SIZE);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x, y, NODE_SIZE, NODE_SIZE);
        }
    }

    private Color getColorForPartition(int id) {
        if (id < 0) return Color.GRAY; // Kolor dla nieprzypisanych wierzchołków
        Color[] colors = {
                new Color(31, 119, 180), new Color(255, 127, 14), new Color(44, 160, 44),
                new Color(214, 39, 40), new Color(148, 103, 189), new Color(140, 86, 75),
                new Color(227, 119, 194), new Color(127, 127, 127), new Color(188, 189, 34),
                new Color(23, 190, 207)
        };
        return colors[id % colors.length];
    }
}