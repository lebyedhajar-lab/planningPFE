package ui;

import loader.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class ChargementExcelFrame extends JInternalFrame {

    private final MainFrame mainFrame;
    private JLabel labelFichier;
    private JTextArea logArea;
    private String cheminChoisi = null;

    private static final Color COLOR_PRIMARY = new Color(83, 74, 183);

    public ChargementExcelFrame(MainFrame mainFrame) {
        super("Charger fichier Excel", true, true, true, true);
        this.mainFrame = mainFrame;
        setSize(540, 420);
        setLocation(80, 60);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ── Sélection fichier ─────────────────────────────────
        JPanel topPanel = new JPanel(new BorderLayout(8, 0));
        labelFichier = new JLabel("Aucun fichier sélectionné");
        labelFichier.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelFichier.setForeground(Color.GRAY);

        JButton btnChoisir = new JButton("Parcourir...");
        btnChoisir.setBackground(COLOR_PRIMARY);
        btnChoisir.setForeground(Color.WHITE);
        btnChoisir.setFocusPainted(false);
        btnChoisir.addActionListener(e -> choisirFichier());

        topPanel.add(labelFichier, BorderLayout.CENTER);
        topPanel.add(btnChoisir,   BorderLayout.EAST);
        root.add(topPanel, BorderLayout.NORTH);

        // ── Log ───────────────────────────────────────────────
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(new Color(245, 245, 248));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Journal de chargement"));
        root.add(scroll, BorderLayout.CENTER);

        // ── Bouton charger ────────────────────────────────────
        JButton btnCharger = new JButton("Charger les données");
        btnCharger.setBackground(COLOR_PRIMARY);
        btnCharger.setForeground(Color.WHITE);
        btnCharger.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCharger.setFocusPainted(false);
        btnCharger.setPreferredSize(new Dimension(0, 40));
        btnCharger.addActionListener(e -> charger());
        root.add(btnCharger, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void choisirFichier() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Fichiers Excel (*.xlsx)", "xlsx"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            cheminChoisi = f.getAbsolutePath();
            labelFichier.setText(f.getName());
            labelFichier.setForeground(Color.DARK_GRAY);
        }
    }

    private void charger() {
        if (cheminChoisi == null) {
            JOptionPane.showMessageDialog(this,
                "Veuillez choisir un fichier Excel.", "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        logArea.setText("");
        log("Chargement depuis : " + cheminChoisi);

        try {
            log("\n[1/4] Chargement configuration...");
            mainFrame.chargerExcel(cheminChoisi);
            log("  Config chargée : " + mainFrame.getConfig());
            log("\n[2/4] Enseignants : "
                + mainFrame.getEnseignantRepo().chargerTous().size());
            log("[3/4] Étudiants : "
                + mainFrame.getEtudiantRepo().chargerTous().size());
            log("[4/4] Salles : "
                + mainFrame.getSalleRepo().chargerDisponibles().size());
            log("Chargement terminé avec succès !");

        } catch (Exception ex) {
            log("\n❌ Erreur : " +ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement :\n" + ex.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}