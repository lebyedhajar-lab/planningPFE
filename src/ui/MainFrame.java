package ui ; 

import javax.swing.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import Config.ConfigPlanning;
import loader.*;
import repository.*;
import algorithm.*;
import statistiques.DashboardService;

public class MainFrame extends JFrame{

    //  Repositories 
	private ConfigPlanning config = new ConfigPlanning();
    private final EnseignantRepository  enseignantRepo  = new EnseignantRepository();
    private final EtudiantRepository    etudiantRepo    = new EtudiantRepository();
    private final SalleRepository       salleRepo       = new SalleRepository();
    private final SoutenanceRepository  soutenanceRepo  = new SoutenanceRepository();
    private final ContrainteRepository  contrainteRepo  = new ContrainteRepository();
    private final CreneauRepository     creneauRepo     = new CreneauRepository();

    private DashboardService dashboardService;

    //  Desktop
    private JDesktopPane desktop;

    //  Chemin Excel 
    private String cheminExcel = null;

    //  Couleurs 
    private static final Color COLOR_SIDEBAR  = new Color(30, 30, 45);
    private static final Color COLOR_HEADER   = new Color(50, 50, 70);
    private static final Color COLOR_BTN      = new Color(83, 74, 183);
    private static final Color COLOR_BTN_HVR  = new Color(103, 94, 210);
    private static final Color COLOR_DESKTOP  = new Color(240, 240, 248);
    private static final Color COLOR_WHITE    = Color.WHITE;
    private static final Font  FONT_MENU      = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_TITLE     = new Font("Segoe UI", Font.BOLD, 14);

    public MainFrame() {
        setTitle("Planning PFE - Système de gestion");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        initUI();
        setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        // Sidebar
        add(buildSidebar(), BorderLayout.WEST);
        //Desktop 
        desktop = new JDesktopPane();
        desktop.setBackground(COLOR_DESKTOP);
        add(desktop, BorderLayout.CENTER);
        //  Header 
        add(buildHeader(), BorderLayout.NORTH);
    }

    //  Header
    private JPanel buildHeader(){
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_HEADER);
        header.setPreferredSize(new Dimension(0, 50));
        header.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel title = new JLabel("Planificateur PFE — Système de gestion des soutenances");
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_WHITE); 
        header.add(title, BorderLayout.WEST);

        JLabel status = new JLabel("Prêt");
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.setForeground(new Color(180, 180, 200));
        header.add(status, BorderLayout.EAST);
        return header;
    }

    // ── Sidebar ───────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COLOR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 10));

        // Logo
        JLabel logo = new JLabel("PFE Planner", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logo.setForeground(COLOR_WHITE);
        logo.setAlignmentX(CENTER_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        sidebar.add(logo);

        // Boutons
        sidebar.add(buildSidebarBtn(" Charger Excel",      this::ouvrirChargementExcel));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(buildSidebarBtn(" Générer Planning",   this::ouvrirGenerationPlanning));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(buildSidebarBtn(" Voir Planning",      this::ouvrirVoirPlanning));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(buildSidebarBtn(" Enseignants",       this::ouvrirEnseignants));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(buildSidebarBtn(" Étudiants",          this::ouvrirEtudiants));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(buildSidebarBtn(" Dashboard",          this::ouvrirDashboard));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(buildSidebarBtn(" Vérification", this::ouvrirVerification)); // ← ici
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(buildSidebarBtn(" Historique",       this::ouvrirHistorique));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(buildSidebarBtn(" Exporter",           this::ouvrirExport));

        sidebar.add(Box.createVerticalGlue());

        JLabel version = new JLabel("v1.0", SwingConstants.CENTER);
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(new Color(120, 120, 140));
        version.setAlignmentX(CENTER_ALIGNMENT);
        sidebar.add(version);

        return sidebar;
    }

    private JButton buildSidebarBtn(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_MENU);
        btn.setForeground(new Color(200, 200, 220));
        btn.setBackground(COLOR_SIDEBAR);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(55, 55, 75));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(COLOR_SIDEBAR);
            }
        });
        btn.addActionListener(e -> action.run());
        return btn;
    }
   

    private void addInternalFrame(JInternalFrame frame) {
        desktop.add(frame);
        frame.setVisible(true);
        try { frame.setSelected(true); } catch (Exception ignored) {}
    }

    private void ouvrirChargementExcel() {
        addInternalFrame(new ChargementExcelFrame(this));
    }

    private void ouvrirGenerationPlanning() {
        if (cheminExcel == null) {
            JOptionPane.showMessageDialog(this,
                "Veuillez d'abord charger le fichier Excel.",
                "Données manquantes", JOptionPane.WARNING_MESSAGE);
            return;
        }
        addInternalFrame(new GenerationPlanningFrame(
            this, config, enseignantRepo, etudiantRepo,
            salleRepo, soutenanceRepo, contrainteRepo));
    }

    private void ouvrirVoirPlanning() {
        if (soutenanceRepo.chargerTous().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Aucune soutenance générée. Générez d'abord le planning.",
                "Planning vide", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        addInternalFrame(new VoirPlanningFrame(soutenanceRepo));
    }

    private void ouvrirEnseignants() {
        addInternalFrame(new EnseignantsFrame(enseignantRepo, soutenanceRepo)); 
    }

    private void ouvrirEtudiants() {
        addInternalFrame(new EtudiantsFrame(etudiantRepo));
    }

    private void ouvrirDashboard() {
    	dashboardService = new DashboardService(
    		    soutenanceRepo, etudiantRepo, enseignantRepo, salleRepo, config);
        addInternalFrame(new DashboardFrame( dashboardService, enseignantRepo));
    }
    private void ouvrirVerification() {
        if (soutenanceRepo.chargerTous().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Aucune soutenance générée.",
                "Planning vide", JOptionPane.WARNING_MESSAGE);
            return;
        }
        addInternalFrame(new EcranVerification(soutenanceRepo, config));
    }
    private void ouvrirHistorique() {
        addInternalFrame(new HistoriquePlanningFrame(this));
    }

    private void ouvrirExport() {
        if (soutenanceRepo.chargerTous().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Aucune soutenance à exporter.",
                "Export impossible", JOptionPane.WARNING_MESSAGE);
            return;
        }
        addInternalFrame(new ExportFrame(soutenanceRepo));
    }

    /** Charge config, profs, étudiants et salles depuis un fichier Excel. */
    public void chargerExcel(String chemin) throws IOException {
        ConfigPlanning tmp = new ExcelConfigLoader(chemin).chargerConfig();
        copyConfig(tmp, config);

        new ExcelEnseignantLoader(chemin).charger(enseignantRepo);
        new ExcelEtudiantLoader(chemin).charger(etudiantRepo, enseignantRepo);
        new ExcelSalleLoader(chemin).charger(salleRepo);

        cheminExcel = chemin;
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

    /** Restaure un planning depuis l'historique. */
    public void appliquerPlanning(java.util.List<model.Soutenance> soutenances) {
        soutenanceRepo.vider();
        for (model.Enseignant ens : enseignantRepo.chargerTous()) {
            ens.reinitialiserSoutenances();
        }
        for (model.Soutenance s : soutenances) {
            soutenanceRepo.sauvegarder(s);
            s.getJury().getEncadrant().incrementerSoutenances();
            for (model.Enseignant m : s.getJury().getMembres()) {
                m.incrementerSoutenances();
            }
        }
    }

    // ── Getters pour sous-fenêtres ────────────────────────────
    public ConfigPlanning       getConfig()         { return config; }
    public EnseignantRepository getEnseignantRepo() { return enseignantRepo; }
    public EtudiantRepository   getEtudiantRepo()   { return etudiantRepo; }
    public SalleRepository      getSalleRepo()       { return salleRepo; }
    public SoutenanceRepository getSoutenanceRepo()  { return soutenanceRepo; }
    public String getCheminExcel()                   { return cheminExcel; }
    public void setCheminExcel(String chemin)        { this.cheminExcel = chemin; }
    public void setConfig(ConfigPlanning config) {this.config = config;}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}

