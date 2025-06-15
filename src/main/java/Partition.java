package main.java;
import java.util.function.Consumer;

public class Partition {
    public static boolean cutGraph(Graph g, int parts, double margin, int[] assignment, Consumer<Double> onMarginIncrease) {
        int attempts = 0;
        double currentMargin = margin;

        while (attempts < 8) {
            Dijkstra.partition(g, assignment, parts);
            KernighanLin.refine(g, assignment, parts, 5);
            if (Utils.checkBalance(g, assignment, parts, currentMargin)) {
                for (int i = 0; i < g.numVertices(); i++) {
                    g.vertexData[i].groupId = assignment[i];
                }
                return true;
            }

            currentMargin += 5.0;
            if (onMarginIncrease != null)
                onMarginIncrease.accept(currentMargin);
            attempts++;
        }

        return false;
    }
}
