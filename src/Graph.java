import java.util.*;

public class Graph {
    public Vertex[] vertexData;
    public int maxDim;

    public Graph(int numVertices) {
        vertexData = new Vertex[numVertices];
        for (int i = 0; i < numVertices; i++) {
            vertexData[i] = new Vertex(i);
        }
    }

    public int numVertices() {
        return vertexData.length;
    }

    public void print() {
        for (Vertex v : vertexData) {
            System.out.printf("Vertex %d:\n", v.id);
            for (Vertex neighbor : v.neighbors) {
                System.out.printf("  -> %d\n", neighbor.id);
            }
        }
    }

    public Graph extractSubgraph(int[] assignment, int parts, int targetGroup) {
        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < assignment.length; i++) {
            if (assignment[i] == targetGroup) selected.add(i);
        }
        Graph sub = new Graph(selected.size());
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < selected.size(); i++) map.put(selected.get(i), i);

        for (int i = 0; i < selected.size(); i++) {
            int oldIndex = selected.get(i);
            for (Vertex n: vertexData[oldIndex].neighbors) {
                if (map.containsKey(n.id)) {
                    sub.vertexData[i].neighbors.add(sub.vertexData[map.get(n.id)]);
                }
            }
        }
        return sub;
    }

    public Vertex[] getVertices() {
        return vertexData;
    }

    public Vertex getVertex(int i) {
        return vertexData[i];
    }

    public void setVertex(int i, Vertex v) {
        vertexData[i] = v;
    }

    public void setPartitions(int[] assignment) {
        for (int i = 0; i < vertexData.length; i++) {
            vertexData[i].setPartitionId(assignment[i]);
        }
    }

    public void addEdge(int src, int dest) {
        if (src < 0 || src >= numVertices() || dest < 0 || dest >= numVertices()) return;
        if (!Arrays.stream(vertexData[src].neighbors.toArray()).anyMatch(v -> ((Vertex)v).id == dest)) {
            vertexData[src].neighbors.add(vertexData[dest]);
        }
    }

    public void clearGroups() {
        for (int i = 0; i < numVertices(); i++) {
            vertexData[i].groupId = -1; // or 0
        }
    }

    public void removeCutEdges(int[] assignment) {
        for (Vertex v : vertexData) {
            Iterator<Vertex> it = v.neighbors.iterator();
            while (it.hasNext()) {
                Vertex neighbor = it.next();
                if (assignment[v.id] != assignment[neighbor.id]) {
                    it.remove();
                    neighbor.neighbors.remove(v);
                }
            }
        }
    }
    
}
