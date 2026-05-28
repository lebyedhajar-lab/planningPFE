package ui;

import Config.ConfigPlanning;
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
            // Config
            log("\n[1/4] Chargement configuration...");
            new ExcelConfigLoader(cheminChoisi)
                .chargerConfig();
            // On recharge dans le config existant via loader
            ConfigPlanning cfg = mainFrame.getConfig();
            ExcelConfigLoader cfgLoader = new ExcelConfigLoader(cheminChoisi);
            // On recharge et on copie les valeurs
            ConfigPlanning tmp = cfgLoader.chargerConfig();
            copyConfig(tmp, cfg);
            log("     ✅ Config chargée : " + cfg);

            // Enseignants
            log("\n[2/4] Chargement enseignants...");
            new ExcelEnseignantLoader(cheminChoisi)
                .charger(mainFrame.getEnseignantRepo());
            log("     ✅ " + mainFrame.getEnseignantRepo()
                .chargerTous().size() + " enseignants chargés.");

            // Étudiants
            log("\n[3/4] Chargement étudiants...");
            new ExcelEtudiantLoader(cheminChoisi)
                .charger(mainFrame.getEtudiantRepo(),
                         mainFrame.getEnseignantRepo());
            log("     ✅ " + mainFrame.getEtudiantRepo()
                .chargerTous().size() + " étudiants chargés.");

            // Salles
            log("\n[4/4] Chargement salles...");
            new ExcelSalleLoader(cheminChoisi)
                .charger(mainFrame.getSalleRepo());
            log("     ✅ " + mainFrame.getSalleRepo()
                .chargerDisponibles().size() + " salles chargées.");

            mainFrame.setCheminExcel(cheminChoisi);
            log("\n✅ Chargement terminé avec succès !");

        } catch (Exception ex) {
            log("\n❌ Erreur : " +ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement :\n" + ex.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copyConfig(ConfigPlanning src, ConfigPlanning dst) {
        dst.setDureeSoutenanceMin(src.getDureeSoutenanceMin());
        dst.setHeureDebutJournee(src.getHeureDebutJournee());
        dst.setHeureFinJournee(src.getHeureFinJournee());
        dst.setHeureDebutPause(src.getHeureDebutPause());
        dst.setHeureFinPause(src.getHeureFinPause());
        dst.setPauseMinimale(src.getPauseMinimale());
        dst.setMinSoutenancesParProfParJour(src.getMinSoutenanceParProfParJour());
        dst.setMaxSoutenancesParProfParJour(src.getMaxSoutenanceParProfParJour());
        dst.setNbMembresJury(src.getNbMembresJury());
        dst.setNbJoursSoutenances(src.getNbJoursSoutenances());
        dst.setDateDebut(src.getDateDebut());
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}