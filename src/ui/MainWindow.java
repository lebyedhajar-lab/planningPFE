package ui;

import javax.swing.*;

public class MainWindow {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EcranPrincipal().setVisible(true));
    }
}