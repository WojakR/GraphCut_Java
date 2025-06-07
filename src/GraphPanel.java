import javax.swing.*;
import java.awt.*;

public class GraphPanel extends JPanel {
    private Graph graph;

    public void setGraph(Graph g) {
        this.graph = g;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graph == null) return;

        // Draw edges first
        g.setColor(Color.BLACK);
        for (Vertex v : graph.getVertices()) {
            for (Vertex u : v.getNeighbours()) {
                g.drawLine(v.getX(), v.getY(), u.getX(), u.getY());
            }
        }
        
        // Now draw vertices
        for (Vertex v : graph.getVertices()) {
            g.setColor(getColorForPartition(v.getPartitionID()));
            g.fillOval(v.getX() - 5, v.getY() - 5, 10, 10);
            g.setColor(Color.BLACK);
            g.drawOval(v.getX() - 5, v.getY() - 5, 10, 10);
        }
    }

    private Color getColorForPartition(int id) {
        Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.YELLOW };
        return colors[id % colors.length];
    }
}
