package main.java;
import java.util.*;
import java.util.Arrays;

public class KernighanLin {
    public static void refine(Graph g, int[] assignment, int parts, int movesPerIter) {
        int n = g.numVertices();
        boolean[] locked = new boolean[n];

        for (int iter = 0; iter < 10; iter++) {
            Arrays.fill(locked, false);
            List<int[]> swaps = new ArrayList<>();
            for (int m = 0; m < movesPerIter; m++) {
                int bestGain = 0, bestU = -1, bestV = -1;

                for (int g1 = 0; g1 < parts; g1++) {
                    for (int g2 = 0; g2 < parts; g2++) {
                        if (g1 == g2) continue;
                        for (int u = 0; u < n; u++) {
                            if (assignment[u] != g1 || locked[u]) continue;
                            for (int v = 0; v < n; v++) {
                                if (assignment[v] != g2 || locked[v]) continue;
                                int gain = gain(g, assignment, u, v);
                                if (gain > bestGain) {
                                    bestGain = gain; bestU = u; bestV = v;
                                }
                            }
                        }
                    }
                }

            if (bestGain <= 0) break;

            // swap
            int temp = assignment[bestU];
            assignment[bestU] = assignment[bestV];
            assignment[bestV] = temp;

            locked[bestU] = true;
            locked[bestV] = true;
            swaps.add(new int[]{bestU, bestV});
            }

            if (swaps.isEmpty()) break;
        }
    }

    private static int gain(Graph g, int[] assign, int u, int v) {
        int g1 = assign[u], g2 = assign[v], gain = 0;
        for (Vertex n : g.vertexData[u].neighbors) gain += (assign[n.id] == g1) ? -1 : 1;
        for (Vertex n : g.vertexData[v].neighbors) gain += (assign[n.id] == g2) ? -1 : 1;
        return gain;
    }
}