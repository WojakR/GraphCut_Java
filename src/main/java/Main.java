package main.java;
public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new GraphUI().setVisible(true);
        });
    }
}