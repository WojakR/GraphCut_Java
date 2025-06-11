import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GraphPanel extends JPanel {
    private Graph graph;
    private final int PADDING = 10; // Margin in pixels from panel edge
    private final int NODE_BASE_SIZE = 4; // Vertice's diameter

    private double scale = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private int lastDragX, lastDragY;

    public GraphPanel() {
        addMouseWheelListener(e -> {
            if (e.getPreciseWheelRotation() < 0) {
                scale *= 1.1;
            } else {
                scale /= 1.1;
            }
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lastDragX = e.getX();
                lastDragY = e.getY();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                offsetX += e.getX() - lastDragX;
                offsetY += e.getY() - lastDragY;
                lastDragX = e.getX();
                lastDragY = e.getY();
                repaint();
            }
        });
    }

    public void setGraph(Graph g) {
        this.graph = g;
        repaint(); // Ask for redrawing when graph is set
    }

    public Consumer<Double> getMarginIncreaseCallback() {
        return increasedMargin -> SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
            "Balance failed. Retrying with increased margin: " + increasedMargin + "%",
            "Retry Info", JOptionPane.INFORMATION_MESSAGE));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graph == null || graph.numVertices() == 0) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.translate(offsetX, offsetY);
        g2d.scale(scale, scale);

        int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;
        for (Vertex v : graph.getVertices()) {
            if (v.y < minRow) minRow = v.y;
            if (v.y > maxRow) maxRow = v.y;
            if (v.x < minCol) minCol = v.x;
            if (v.x > maxCol) maxCol = v.x;
        }

        int availableWidth = getWidth() - 2 * PADDING;
        int availableHeight = getHeight() - 2 * PADDING;
        double hScale = (maxCol - minCol == 0) ? 1 : (double) availableWidth / (maxCol - minCol);
        double vScale = (maxRow - minRow == 0) ? 1 : (double) availableHeight / (maxRow - minRow);
        double baseScale = Math.min(hScale, vScale);

        Map<Integer, Point> positions = new HashMap<>();
        for (Vertex v : graph.getVertices()) {
            int screenX = PADDING + (int) ((v.x - minCol) * baseScale);
            int screenY = PADDING + (int) ((v.y - minRow) * baseScale);
            positions.put(v.id, new Point(screenX, screenY));
        }

        float edgeStroke = (float) Math.max(0.5, 1.5 / scale); // Scaled edge stroke
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(edgeStroke));
        for (Vertex v : graph.getVertices()) {
            Point p1 = positions.get(v.id);
            for (Vertex u : v.getNeighbours()) {
                if (v.id < u.id) {
                    Point p2 = positions.get(u.id);
                    g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }

        for (Vertex v : graph.getVertices()) {
            Point p = positions.get(v.id);
            int r = (int) Math.max(2, NODE_BASE_SIZE / scale); // Scaled radius
            int x = p.x - r;
            int y = p.y - r;

            g2d.setColor(getColorForPartition(v.getPartitionID()));
            g2d.fillOval(x, y, r * 2, r * 2);

            float strokeWidth = (float) Math.max(0.5, 1.5 / scale); // Scaled stroke
            g2d.setStroke(new BasicStroke(strokeWidth));
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x, y, r * 2, r * 2);
        }
    }

    private Color getColorForPartition(int id) {
        if (id < 0) return Color.GRAY; // Color for unbound vertices
        Color[] colors = {
                new Color(31, 119, 180), new Color(255, 127, 14), new Color(44, 160, 44),
                new Color(214, 39, 40), new Color(148, 103, 189), new Color(140, 86, 75),
                new Color(227, 119, 194), new Color(127, 127, 127), new Color(188, 189, 34),
                new Color(23, 190, 207)
        };
        return colors[id % colors.length];
    }
}