package ui;

import javax.swing.*;
import java.awt.*;
import algorithm.PlanningGenerator;

public class MainWindow {
    public static void main(String[] args) {
        JFrame fenetre = new JFrame("Planning PFE");
        fenetre.setSize(800, 600);
        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenetre.setLocationRelativeTo(null);

        JPanel panel = new JPanel();

        JButton btnGenerer = new JButton("Générer le Planning");

        // Quand on clique sur le bouton → ton algorithme se lance
        btnGenerer.addActionListener(e -> {
            PlanningGenerator generator = new PlanningGenerator();
            //generator.generer(); // adapte selon le nom de ta méthode
            JOptionPane.showMessageDialog(fenetre, "Planning généré !");
        });

        panel.add(btnGenerer);
        fenetre.add(panel);
        fenetre.setVisible(true);
    }
}
