import java.util.*;

public class Vertex {
    public int id;
    public List<Vertex> neighbors = new ArrayList<>();
    public int groupId;
    public int x, y;

    public Vertex(int id) {
        this.id = id;
    }

    public List<Vertex> getNeighbours() {
        return neighbors;
    }

    public int getPartitionID() {
        return groupId;
    }

    public void setPartitionId(int id) {
        this.groupId = id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setCoordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
