import java.util.*;

public class Dijkstra {
    public static void partition(Graph g, int[] assignment, int parts) {
        int n = g.numVertices();
        int[] seeds = new int[parts];
        int spacing = n / parts;
        for (int i = 0; i < parts; i++) seeds[i] = Math.min(i * spacing, n - 1);

        int[][] dists = new int[parts][];
        for (int i = 0; i < parts; i++) dists[i] = shortestPaths(g, seeds[i]);

        int[] partSizes = new int[parts];
        int maxSize = (int)Math.ceil((double) n / parts);

        for (int v = 0; v < n; v++) {
            int best = -1;
            int minDist = Integer.MAX_VALUE;
            for (int i = 0; i < parts; i++) {
                if (partSizes[i] < maxSize && dists[i][v] < minDist) {
                    minDist = dists[i][v];
                    best = i;
                }
            }
            
            if (best == -1) best = 0;
            assignment[v] = best;
            partSizes[best]++;
        }
    }

    private static int[] shortestPaths(Graph g, int src) {
        int n = g.numVertices();
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.add(new int[]{src, 0});
        boolean[] visited = new boolean[n];

        while (!pq.isEmpty()) {
            int[] u = pq.poll();
            if (visited[u[0]]) continue;
            visited[u[0]] = true;
            for (Vertex v : g.vertexData[u[0]].neighbors) {
                if (dist[u[0]] + 1 < dist[v.id]) {
                    dist[v.id] = dist[u[0]] + 1;
                    pq.add(new int[]{v.id, dist[v.id]});
                }
            }
        }
        return dist;
    }
}
