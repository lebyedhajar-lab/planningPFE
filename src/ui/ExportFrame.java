package ui;

import export.PlanningExporter;
import model.Soutenance;
import repository.SoutenanceRepository;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ExportFrame extends JInternalFrame {

    private final SoutenanceRepository soutenanceRepo;
    private JLabel labelDossier;
    private String dossierChoisi = null;
    private static final Color COLOR_PRIMARY = new Color(83, 74, 183);

    public ExportFrame(SoutenanceRepository soutenanceRepo) {
        super("Exporter le Planning", true, true, true, true);
        this.soutenanceRepo = soutenanceRepo;
        setSize(460, 280);
        setLocation(200, 150);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Choix dossier
        JPanel dossierPanel = new JPanel(new BorderLayout(8, 0));
        labelDossier = new JLabel("Aucun dossier sélectionné");
        labelDossier.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton btnDossier = new JButton("Choisir dossier");
        btnDossier.setBackground(new Color(100, 100, 120));
        btnDossier.setForeground(Color.WHITE);
        btnDossier.setFocusPainted(false);
        btnDossier.addActionListener(e -> choisirDossier());

        dossierPanel.add(labelDossier, BorderLayout.CENTER);
        dossierPanel.add(btnDossier,   BorderLayout.EAST);
        root.add(dossierPanel);
        root.add(Box.createVerticalStrut(16));

        // Boutons export
        JButton btnPlanning = buildExportBtn(
            " Exporter Planning (.docx)",
            () -> exporter(false));
        JButton btnFiches = buildExportBtn(
            " Exporter Fiches de notation (.docx)",
            () -> exporter(true));
        JButton btnTout = buildExportBtn(
            " Tout exporter",
            () -> exporterTout());

        root.add(btnPlanning);
        root.add(Box.createVerticalStrut(8));
        root.add(btnFiches);
        root.add(Box.createVerticalStrut(8));
        root.add(btnTout);

        setContentPane(root);
    }

    private JButton buildExportBtn(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setBackground(COLOR_PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void choisirDossier() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            dossierChoisi = fc.getSelectedFile().getAbsolutePath();
            labelDossier.setText(dossierChoisi);
        }
    }

    private void exporter1(boolean fichesOnly) {
        if (dossierChoisi == null) {
            JOptionPane.showMessageDialog(this,
                "Choisissez un dossier de destination.",
                "Erreur", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            PlanningExporter exporter = new PlanningExporter();
            List<Soutenance> list = soutenanceRepo.chargerTous();
            if (fichesOnly)
                exporter.exporterFiches(list, dossierChoisi + File.separator);
            else
                exporter.exporterPlanning(list, dossierChoisi);
            JOptionPane.showMessageDialog(this,
                "Export réussi dans : " + dossierChoisi,
                "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erreur export : " + ex.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void exporter(boolean fichesOnly) {
        if (dossierChoisi == null) {
            JOptionPane.showMessageDialog(this,
                "Choisissez un dossier de destination.",
                "Erreur", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            List<Soutenance> list = soutenanceRepo.chargerTous();
            
            // LOG pour déboguer
            System.out.println("=== Export : " + list.size() + " soutenances ===");
            for (int i = 0; i < Math.min(3, list.size()); i++) {
                Soutenance s = list.get(i);
                System.out.println("  [" + s.getId() + "] " 
                    + s.getEtudiant().getNom() 
                    + " | " + s.getCreneau().getHeureDebut()
                    + " | salle " + s.getSalle().getNom());
            }
            
            PlanningExporter exporter = new PlanningExporter();
            if (fichesOnly)
                exporter.exporterFiches(list, dossierChoisi + File.separator);
            else
                exporter.exporterPlanning(list, dossierChoisi);
            JOptionPane.showMessageDialog(this,
                "Export réussi dans : " + dossierChoisi,
                "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erreur export : " + ex.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exporterTout() {
        if (dossierChoisi == null) {
            JOptionPane.showMessageDialog(this,
                "Choisissez un dossier de destination.",
                "Erreur", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            new PlanningExporter().exporterTout(
                soutenanceRepo.chargerTous(), dossierChoisi);
            JOptionPane.showMessageDialog(this,
                "Export complet réussi !",
                "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erreur : " + ex.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}