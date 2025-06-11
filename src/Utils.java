import java.util.*;

import javax.swing.*;
import java.awt.GraphicsEnvironment;

public class Utils {
    public static void handleError(int code, String msg) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.printf("Error [%d]: %s%n", code, msg);
            System.exit(code);
        } else {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, "Error [" + code + "]: " + msg,
                    "Partitioning Error", JOptionPane.ERROR_MESSAGE);
            });
            throw new RuntimeException("Partitioning failed: " + msg); // breaks only an operation
        }
    }

    public static void printHelp() {
        System.out.println("Uzycie: java Main <liczba_czesci> <margines_%> --input <plik.csrrg> [opcje]");
        System.out.println("--output <nazwa>   --binary   --verbose   --graph <nr>   --help");
    }

    public static int[] createAssignmentArray(int n) {
        int[] arr = new int[n];
        Arrays.fill(arr, -1);
        return arr;
    }

    public static boolean checkBalance(Graph g, int[] assign, int parts, double margin) {
        int[] counts = new int[parts];
        for (int i = 0; i < g.numVertices(); i++) counts[assign[i]]++;
        int avg = g.numVertices() / parts;
        int min = (int)Math.floor(avg * (1 - margin / 100.0));
        int max = (int)Math.ceil(avg * (1 + margin / 100.0));
        for (int c : counts) if (c < min || c > max) return false;
        return true;
    }
}
