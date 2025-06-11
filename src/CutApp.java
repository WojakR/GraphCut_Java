//import java.io.*;
import java.util.*;

public class CutApp {
    private int numPartitions;
    private double margin;
    private String inputFile;
    private String outputFileBase;
    private boolean verboseMode = false;
    private int targetGraphIndex = 1;
    private boolean binary = false;

    public void run(String[] args) {
        if (!parseArgs(args)) return;

        Graph graph = TempGraphIO.loadGraph(inputFile, targetGraphIndex);
        if (graph == null) {
            System.err.printf("Error: Failed to load graph %d\n", targetGraphIndex);
            return;
        }

        if (verboseMode) graph.print();

        int[] assignment = Utils.createAssignmentArray(graph.numVertices());
        Partition.cutGraph(graph, numPartitions, margin, assignment);

        for (int i = 0; i < numPartitions; ++i) {
            String outputFilename = outputFileBase + "_" + i + ".csrrg" + (binary ? "bin" : "");
            TempGraphIO.savePartition(graph, assignment, i, outputFilename, binary);

        }

        System.out.println("Finished successfully.");
    }

    private boolean parseArgs(String[] args) {
        List<String> argList = Arrays.asList(args);
        if (argList.contains("--help")) {
            Utils.printHelp();
            return false;
        }

        try {
            int pos = 0;

            // 1. Required arguments first
            if (args.length < 2) throw new IllegalArgumentException("Missing required args");
            numPartitions = Integer.parseInt(args[pos++]);
            margin = Double.parseDouble(args[pos++]);

            // 2. Later options in any order
            while (pos < args.length) {
                switch (args[pos]) {
                    case "--input":
                        inputFile = args[++pos]; break;
                    case "--output":
                        outputFileBase = args[++pos]; break;
                    case "--verbose":
                        verboseMode = true; break;
                    case "--graph":
                        targetGraphIndex = Integer.parseInt(args[++pos]); break;
                    case "--binary":
                        binary = true; break;
                    default:
                        throw new IllegalArgumentException("Unknown option: " + args[pos]);
                }
                pos++;
            }

            if (inputFile == null) 
                throw new IllegalArgumentException("Input not specified");

            if (outputFileBase == null) 
                outputFileBase = inputFile.replaceAll("\\.cssrg.*$", "");

        } catch (Exception e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            return false;
        }
        return true;
    }
}
