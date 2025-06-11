import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class TempGraphIO {

    private static final byte[] GRAFO_SIEKACZ_SEPARATOR = {
            (byte) 0xBE, (byte) 0xBA, (byte) 0xFE, (byte) 0xCA,
            (byte) 0xEF, (byte) 0xBE, (byte) 0xAD, (byte) 0xDE
    };

    public static Graph loadGraph(String filename, int graphIndex) throws IOException {
        if (graphIndex <= 0) {
            System.err.println("Error: Target graph index must be a positive integer");
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            // Linia 1: maxDim
            String line1 = reader.readLine();
            if (line1 == null) throw new IOException("Failed to read Line 1 (maxDim).");
            int maxDim = Integer.parseInt(line1.trim());

            // Linia 2: colIndices
            String line2 = reader.readLine();
            if (line2 == null) throw new IOException("Failed to read Line 2 (colIndices).");
            int[] colIndices = parseLine(line2);
            int numVert = colIndices.length;
            if (numVert == 0) throw new IOException("No nodes found in graph file.");

            // Linia 3: rowPointers
            String line3 = reader.readLine();
            if (line3 == null) throw new IOException("Failed to read Line 3 (rowPointers).");
            int[] rowPointers = parseLine(line3);
            if (rowPointers.length == 0 || rowPointers[0] != 0 || rowPointers[rowPointers.length - 1] != numVert) {
                throw new IOException("Invalid rowPointers data in Line 3.");
            }

            // Linia 4: edgeListIndices
            String line4 = reader.readLine();
            if (line4 == null) throw new IOException("Failed to read Line 4 (edgeListIndices).");
            int[] edgeListIndices = parseLine(line4);
            for (int idx : edgeListIndices) {
                if (idx < 0 || idx >= numVert) throw new IOException("Invalid vertex index in Line 4.");
            }

            // Linie 5+: Znajdź wiersz docelowego grafu
            int[] edgeGroupPointers = null;
            String currentLine;
            int foundGraphIndex = 0;
            while ((currentLine = reader.readLine()) != null) {
                foundGraphIndex++;
                if (foundGraphIndex == graphIndex) {
                    edgeGroupPointers = parseLine(currentLine);
                    break;
                }
            }

            if (edgeGroupPointers == null) {
                throw new IOException("Graph index " + graphIndex + " not found in file.");
            }

            // Tworzenie i populacja grafu
            Graph graph = new Graph(numVert);
            graph.maxDim = maxDim;

            for (int r = 0; r < rowPointers.length - 1; r++) {
                int rowStartIndex = rowPointers[r];
                int rowEndIndex = rowPointers[r + 1];
                for (int k = rowStartIndex; k < rowEndIndex; k++) {
                    graph.vertexData[k].id = k;
                    graph.vertexData[k].y = r;
                    graph.vertexData[k].x = colIndices[k];
                }
            }

            // Budowanie listy sąsiedztwa
            for (int i = 0; i < edgeGroupPointers.length; i++) {
                int groupStartIndex = edgeGroupPointers[i];
                int groupEndIndex = (i == edgeGroupPointers.length - 1) ? edgeListIndices.length : edgeGroupPointers[i + 1];
                if (groupEndIndex - groupStartIndex < 2) continue;

                int mainNode = edgeListIndices[groupStartIndex];
                for (int k = groupStartIndex + 1; k < groupEndIndex; k++) {
                    int restNode = edgeListIndices[k];
                    if (mainNode != restNode) {
                        graph.addEdge(mainNode, restNode);
                        graph.addEdge(restNode, mainNode);
                    }
                }
            }
            return graph;

        } catch (IOException | NumberFormatException e) {
            throw new IOException("Error loading graph from " + filename + ": " + e.getMessage(), e);
        }
    }

    public static void savePartition(Graph originalGraph, int[] assignment, int targetGroup, String filename, boolean binaryFormat) {
        Graph subgraph = createSaveableSubgraph(originalGraph, assignment, targetGroup);

        if (subgraph == null || subgraph.numVertices() <= 0) {
            System.out.println("Info: Skipping save for empty partition " + targetGroup);
            return;
        }

        try {
            if (binaryFormat) {
                saveGraphBinary(subgraph, filename);
            } else {
                saveGraphText(subgraph, filename);
            }
        } catch (IOException e) {
            System.err.println("Error saving partition to " + filename + ": " + e.getMessage());
        }
    }


    private static Graph createSaveableSubgraph(Graph original, int[] assignment, int targetGroup) {
        int count = 0;
        for (int i = 0; i < original.numVertices(); i++) {
            if (assignment[i] == targetGroup) {
                count++;
            }
        }

        if (count == 0) return null;

        Graph subgraph = new Graph(count);
        subgraph.maxDim = original.maxDim;

        Map<Integer, Integer> oldToNewMap = new HashMap<>();
        int newIdx = 0;
        for (int i = 0; i < original.numVertices(); i++) {
            if (assignment[i] == targetGroup) {
                oldToNewMap.put(i, newIdx);
                subgraph.vertexData[newIdx].id = newIdx;
                subgraph.vertexData[newIdx].y = original.vertexData[i].y; // y to row
                subgraph.vertexData[newIdx].x = original.vertexData[i].x; // x to col
                newIdx++;
            }
        }

        for (int oldIndex = 0; oldIndex < original.numVertices(); oldIndex++) {
            if (assignment[oldIndex] != targetGroup) continue;
            int newIndex = oldToNewMap.get(oldIndex);
            for (Vertex neighbor : original.vertexData[oldIndex].getNeighbours()) {
                if (assignment[neighbor.id] == targetGroup) {
                    int newNeighborIndex = oldToNewMap.get(neighbor.id);
                    subgraph.addEdge(newIndex, newNeighborIndex);
                }
            }
        }
        return subgraph;
    }


    private static void saveGraphText(Graph graph, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Linia 1: maxDim
            writer.println(graph.maxDim);

            // Przygotowanie danych do Linii 2 i 3
            int maxRow = -1;
            for (Vertex v : graph.vertexData) {
                if (v.y > maxRow) maxRow = v.y; // y to row
            }
            int numRows = maxRow + 1;

            List<Integer> colIndices = new ArrayList<>();
            int[] rowPointers = new int[numRows + 1];
            rowPointers[0] = 0;

            for (int r = 0; r < numRows; r++) {
                final int currentRow = r;
                List<Integer> colsInRow = Arrays.stream(graph.vertexData)
                        .filter(v -> v.y == currentRow) // v.y to row
                        .map(v -> v.x)                 // v.x to col
                        .collect(Collectors.toList());
                colIndices.addAll(colsInRow);
                rowPointers[r + 1] = colIndices.size();
            }

            // Zapis Linii 2 i 3
            writer.println(colIndices.stream().map(String::valueOf).collect(Collectors.joining(";")));
            writer.println(Arrays.stream(rowPointers).mapToObj(String::valueOf).collect(Collectors.joining(";")));

            // Zapis Linii 4 i 5
            saveGraphEdgesText(graph, writer);
        }
    }


    private static void saveGraphBinary(Graph graph, String filename) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)))) {
            // Linia 1: maxDim
            writeCountAndArrayDeltas16Bit(dos, Collections.singletonList(graph.maxDim));

            // Przygotowanie danych do Linii 2 i 3
            int maxRow = -1;
            for (Vertex v : graph.vertexData) {
                if (v.y > maxRow) maxRow = v.y;
            }
            int numRows = maxRow + 1;
            List<Integer> colIndices = new ArrayList<>();
            List<Integer> rowPointers = new ArrayList<>();
            rowPointers.add(0);
            for (int r = 0; r < numRows; r++) {
                final int currentRow = r;
                List<Integer> colsInRow = Arrays.stream(graph.vertexData)
                        .filter(v -> v.y == currentRow)
                        .map(v -> v.x)
                        .collect(Collectors.toList());
                colIndices.addAll(colsInRow);
                rowPointers.add(colIndices.size());
            }

            // Zapis Linii 2 i 3
            writeCountAndArrayDeltas16Bit(dos, colIndices);
            writeCountAndArrayDeltas16Bit(dos, rowPointers);

            // Zapis Linii 4 i 5
            saveGraphEdgesBinary(graph, dos);
        }
    }

    private static int[] parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return new int[0];
        }
        String[] tokens = line.trim().split(";");
        return Arrays.stream(tokens)
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .toArray();
    }


    private static void saveGraphEdgesText(Graph graph, PrintWriter writer) {
        List<Integer> groupedNodeIndices = new ArrayList<>();
        List<Integer> groupPointers = new ArrayList<>();
        groupPointers.add(0);

        for (int mainId = 0; mainId < graph.numVertices(); mainId++) {
            List<Integer> neighborsToProcess = new ArrayList<>();
            for (Vertex neighbor : graph.vertexData[mainId].getNeighbours()) {
                if (neighbor.id > mainId) {
                    neighborsToProcess.add(neighbor.id);
                }
            }

            if (!neighborsToProcess.isEmpty()) {
                groupedNodeIndices.add(mainId);
                Collections.sort(neighborsToProcess);
                groupedNodeIndices.addAll(neighborsToProcess);
                groupPointers.add(groupedNodeIndices.size());
            }
        }

        // Linia 4
        if (groupedNodeIndices.isEmpty()) {
            writer.println("0");
        } else {
            writer.println(groupedNodeIndices.stream().map(String::valueOf).collect(Collectors.joining(";")));
        }

        // Linia 5
        groupPointers.remove(groupPointers.size() - 1);
        if (groupPointers.isEmpty()) {
            writer.print("0");
        } else {
            writer.print(groupPointers.stream().map(String::valueOf).collect(Collectors.joining(";")));
        }
    }

    private static void saveGraphEdgesBinary(Graph graph, DataOutputStream dos) throws IOException {
        List<Integer> groupedNodeIndices = new ArrayList<>();
        List<Integer> groupPointers = new ArrayList<>();
        groupPointers.add(0);

        for (int mainId = 0; mainId < graph.numVertices(); mainId++) {
            List<Integer> neighborsToProcess = new ArrayList<>();
            for (Vertex neighbor : graph.vertexData[mainId].getNeighbours()) {
                if (neighbor.id > mainId) {
                    neighborsToProcess.add(neighbor.id);
                }
            }

            if (!neighborsToProcess.isEmpty()) {
                groupedNodeIndices.add(mainId);
                Collections.sort(neighborsToProcess);
                groupedNodeIndices.addAll(neighborsToProcess);
                groupPointers.add(groupedNodeIndices.size());
            }
        }

        // Linia 4
        if (groupedNodeIndices.isEmpty()) groupedNodeIndices.add(0);
        writeCountAndArrayDeltas16Bit(dos, groupedNodeIndices);

        // Linia 5
        groupPointers.remove(groupPointers.size() - 1);
        if (groupPointers.isEmpty()) groupPointers.add(0);
        writeCountAndArrayDeltas16Bit(dos, groupPointers);
    }


    private static void writeCountAndArrayDeltas16Bit(DataOutputStream dos, List<Integer> data) throws IOException {
        dos.writeInt(data.size());
        if (data.isEmpty()) return;

        int prevValue = data.get(0);
        dos.writeShort((short) prevValue);

        for (int i = 1; i < data.size(); i++) {
            int currentValue = data.get(i);
            int delta = currentValue - prevValue;
            dos.writeShort((short) delta);
            prevValue = currentValue;
        }
    }

    public static Graph loadGrafoSiekaczBinary(String filename, int graphIndex) {
        if (graphIndex <= 0) {
            System.err.println("Error: Target graph index must be a positive integer");
            return null;
        }

        try (InputStream is = new BufferedInputStream(new FileInputStream(filename))) {

            List<Integer> line1Data = readVByteSection(is);
            if (line1Data.isEmpty()) throw new IOException("Failed to read Line 1 (maxDim).");
            int maxDim = line1Data.get(0);

            List<Integer> colIndicesList = readVByteSection(is);
            int[] colIndices = colIndicesList.stream().mapToInt(i -> i).toArray();
            int numVert = colIndices.length;
            if (numVert == 0) throw new IOException("No nodes found in binary graph file.");

            List<Integer> rowPointersList = readVByteSection(is);
            int[] rowPointers = rowPointersList.stream().mapToInt(i -> i).toArray();
            if (rowPointers.length == 0 || rowPointers[0] != 0 || rowPointers[rowPointers.length - 1] != numVert) {
                throw new IOException("Invalid rowPointers data in binary Line 3.");
            }

            List<Integer> edgeListIndicesList = readVByteSection(is);
            int[] edgeListIndices = edgeListIndicesList.stream().mapToInt(i -> i).toArray();
            for (int idx : edgeListIndices) {
                if (idx < 0 || idx >= numVert) throw new IOException("Invalid vertex index in binary Line 4.");
            }

            int[] edgeGroupPointers = null;
            int foundGraphIndex = 0;
            while (is.available() > 0) {
                foundGraphIndex++;
                List<Integer> edgeGroupPointersList = readVByteSection(is);
                if (foundGraphIndex == graphIndex) {
                    edgeGroupPointers = edgeGroupPointersList.stream().mapToInt(i -> i).toArray();
                    break;
                }
            }

            if (edgeGroupPointers == null) {
                throw new IOException("Graph index " + graphIndex + " not found in binary file.");
            }

            Graph graph = new Graph(numVert);
            graph.maxDim = maxDim;

            for (int r = 0; r < rowPointers.length - 1; r++) {
                int rowStartIndex = rowPointers[r];
                int rowEndIndex = rowPointers[r + 1];
                for (int k = rowStartIndex; k < rowEndIndex; k++) {
                    graph.vertexData[k].id = k;
                    graph.vertexData[k].y = r;            // y to row
                    graph.vertexData[k].x = colIndices[k]; // x to col
                }
            }

            for (int i = 0; i < edgeGroupPointers.length; i++) {
                int groupStartIndex = edgeGroupPointers[i];
                int groupEndIndex = (i == edgeGroupPointers.length - 1) ? edgeListIndices.length : edgeGroupPointers[i + 1];
                if (groupEndIndex - groupStartIndex < 2) continue;

                int mainNode = edgeListIndices[groupStartIndex];
                for (int k = groupStartIndex + 1; k < groupEndIndex; k++) {
                    int restNode = edgeListIndices[k];
                    if (mainNode != restNode) {
                        graph.addEdge(mainNode, restNode);
                        graph.addEdge(restNode, mainNode);
                    }
                }
            }
            return graph;

        } catch (IOException e) {
            System.err.println("Error loading Grafo-Siekacz binary file '" + filename + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static long decodeVByte(InputStream is) throws IOException {
        long value = 0;
        int shift = 0;
        while (true) {
            int b = is.read();
            if (b == -1) {
                throw new EOFException("Unexpected end of stream while decoding vByte.");
            }
            // Dodaj 7 bitów danych do wyniku
            value |= (long) (b & 0x7F) << shift;
            shift += 7;
            // Jeśli bit kontynuacji (MSB) jest 0, to koniec liczby
            if ((b & 0x80) == 0) {
                break;
            }
        }
        return value;
    }


    private static List<Integer> readVByteSection(InputStream is) throws IOException {
        List<Integer> numbers = new ArrayList<>();
        byte[] buffer = new byte[8];

        while (is.available() > 0) {
            is.mark(8);
            int bytesRead = is.read(buffer);

            if (bytesRead == 8 && Arrays.equals(buffer, GRAFO_SIEKACZ_SEPARATOR)) {
                // Znaleziono separator
                break;
            } else {
                // To nie separator
                is.reset();
                numbers.add((int) decodeVByte(is));
            }
        }
        return numbers;
    }


    private static void readUntilSeparator(InputStream is) throws IOException {
        byte[] buffer = new byte[8];
        while (is.available() > 0) {
            is.mark(8);
            int bytesRead = is.read(buffer);
            if (bytesRead == 8 && Arrays.equals(buffer, GRAFO_SIEKACZ_SEPARATOR)) {
                // Znaleziono i użyto separator
                return;
            }

            is.reset();
            is.read();
        }
    }
}