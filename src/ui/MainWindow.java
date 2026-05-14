package ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

// Importation de vos classes
import model.*;
import repository.*;
import loader.*;
import algorithm.PlanningVerificateur;

public class MainWindow extends JFrame {

    // 1. Initialisation des tiroirs de données
    private EnseignantRepository enseignantRepo = new EnseignantRepository();
    private SoutenanceRepository soutenanceRepo = new SoutenanceRepository();
    private SalleRepository      salleRepo      = new SalleRepository();
    private EtudiantRepository   etudiantRepo   = new EtudiantRepository();
    private Config.ConfigPlanning configPlanning = new Config.ConfigPlanning();

    public MainWindow() {
        setTitle("ENSAH - Importation et Test Planning");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton btnUpload    = new JButton("1. Sélectionner le fichier Excel");
        JButton btnDashboard = new JButton("2. Ouvrir le Dashboard (Tests)");
        JButton btnQuitter   = new JButton("Quitter");

        // --- ACTION 1 : UPLOAD DU FICHIER ---
        btnUpload.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(this);

            if (res == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();

                try {
                    // Utilisation de VOS chargeurs (Loaders)
                    this.configPlanning = new ExcelConfigLoader(path).chargerConfig();
                    new ExcelEnseignantLoader(path).charger(enseignantRepo);
                    new ExcelSalleLoader(path).charger(salleRepo);
                    new ExcelEtudiantLoader(path).charger(etudiantRepo, enseignantRepo);

                    JOptionPane.showMessageDialog(this, "✅ Fichier importé avec succès !");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "❌ Erreur de chargement : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        // --- ACTION 2 : VOIR LE DASHBOARD ET LES TESTS ---
        btnDashboard.addActionListener(e -> {
            // On ouvre le Dashboard avec les données chargées
            Dashboard db = new Dashboard(soutenanceRepo, enseignantRepo);
            db.setVisible(true);
        });

        btnQuitter.addActionListener(e -> System.exit(0));

        panel.add(btnUpload);
        panel.add(btnDashboard);
        panel.add(btnQuitter);
        add(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}