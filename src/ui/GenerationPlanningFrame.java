package ui;

import Config.ConfigPlanning;
import java.io.*;
import algorithm.*;
import model.*;
import repository.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GenerationPlanningFrame extends JInternalFrame {

    private final ConfigPlanning        config;
    private final EnseignantRepository  enseignantRepo;
    private final EtudiantRepository    etudiantRepo;
    private final SalleRepository       salleRepo;
    private final SoutenanceRepository  soutenanceRepo;
    private final ContrainteRepository  contrainteRepo;

    private JTextArea logArea;
    private static final Color COLOR_PRIMARY = new Color(83, 74, 183);

    public GenerationPlanningFrame(ConfigPlanning config,
                                    EnseignantRepository enseignantRepo,
                                    EtudiantRepository etudiantRepo,
                                    SalleRepository salleRepo,
                                    SoutenanceRepository soutenanceRepo,
                                    ContrainteRepository contrainteRepo) {
        super("Génération du Planning", true, true, true, true);
        this.config         = config;
        this.enseignantRepo = enseignantRepo;
        this.etudiantRepo   = etudiantRepo;
        this.salleRepo      = salleRepo;
        this.soutenanceRepo = soutenanceRepo;
        this.contrainteRepo = contrainteRepo;
        setSize(560, 440);
        setLocation(120, 80);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ── Résumé config ─────────────────────────────────────
        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 6, 6));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Configuration chargée"));
        infoPanel.add(new JLabel("Date début :"));
        infoPanel.add(new JLabel(config.getDateDebut() != null
            ? config.getDateDebut().toString() : "—"));
        infoPanel.add(new JLabel("Nb jours :"));
        infoPanel.add(new JLabel(String.valueOf(config.getNbJoursSoutenances())));
        infoPanel.add(new JLabel("Durée soutenance :"));
        infoPanel.add(new JLabel(config.getDureeSoutenanceMin() + " min"));
        infoPanel.add(new JLabel("Nb membres jury :"));
        infoPanel.add(new JLabel(String.valueOf(config.getNbMembresJury())));
        root.add(infoPanel, BorderLayout.NORTH);

        // ── Log ───────────────────────────────────────────────
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(new Color(245, 245, 248));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Journal de génération"));
        root.add(scroll, BorderLayout.CENTER);

        // ── Bouton ────────────────────────────────────────────
        JButton btnGenerer = new JButton("Générer le Planning");
        btnGenerer.setBackground(COLOR_PRIMARY);
        btnGenerer.setForeground(Color.WHITE);
        btnGenerer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGenerer.setFocusPainted(false);
        btnGenerer.setPreferredSize(new Dimension(0, 40));
        btnGenerer.addActionListener(e -> generer());
        root.add(btnGenerer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void generer() {
        logArea.setText("");

        //  Rediriger System.out vers le logArea
        PrintStream ps = new PrintStream(new OutputStream() {
            private StringBuilder sb = new StringBuilder();
            public void write(int b) {
                char c = (char) b;
                sb.append(c);
                if (c == '\n') {
                    final String line = sb.toString();
                    SwingUtilities.invokeLater(() -> {
                        logArea.append(line);
                        logArea.setCaretPosition(
                            logArea.getDocument().getLength());
                    });
                    sb.setLength(0);
                }
            }
        });
        System.setOut(ps);

        log("Démarrage de la génération...");
        log("  Étudiants  : " + etudiantRepo.chargerTous().size());
        log("  Enseignants: " + enseignantRepo.chargerTous().size());
        log("  Salles     : " + salleRepo.chargerDisponibles().size());

        try {
        	EncadrantAffectationService affectation = new EncadrantAffectationService();
            affectation.affecter(etudiantRepo.chargerTous(), enseignantRepo.chargerTous());
            log("✅ Encadrants affectés.");
            ContrainteValidator validator =
                new ContrainteValidator(contrainteRepo, soutenanceRepo);

            PlanningGenerator generator = new PlanningGenerator(
                config, validator,
                new DefaultFilierePlanningStrategy(
                    validator),
                etudiantRepo, enseignantRepo,
                salleRepo, soutenanceRepo);

            List<Soutenance> soutenances = generator.generer();
            log("\n✅ Planning généré : "
                + soutenances.size() + " soutenances.");
            generator.afficherRapport(soutenances);

        } catch (Exception ex) {
            log("\n❌ Erreur : " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Erreur : " + ex.getMessage(),
                "Génération échouée", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
