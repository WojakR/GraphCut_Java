public class Partition {
    public static void cutGraph(Graph g, int parts, double margin, int[] assignment) {
        int attempts = 0;
        double currentMargin = margin;

        while (attempts < 8) {
            Dijkstra.partition(g, assignment, parts);
            KernighanLin.refine(g, assignment, parts);
            if (Utils.checkBalance(g, assignment, parts, currentMargin)) break;
            currentMargin += 5.0;
            System.err.printf("Retrying with margin %.1f%%\n", currentMargin);
            attempts++;
        }
        if (attempts == 8) Utils.handleError(31, "Partitioning failed after retries");
        for (int i = 0; i < g.numVertices(); i++) g.vertexData[i].groupId = assignment[i];
    }
}
