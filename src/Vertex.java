import java.util.*;

public class Vertex {
    public int id;
    public List<Vertex> neighbors = new ArrayList<>();
    public int groupId;

    public Vertex(int id) {
        this.id = id;
    }
}
