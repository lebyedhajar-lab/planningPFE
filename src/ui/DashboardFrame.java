package ui;

import statistiques.DashboardService;
import repository.*;
import model.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class DashboardFrame extends JInternalFrame {

    private final DashboardService dashboardService;
    private final EnseignantRepository enseignantRepo;
    
    private static final Color COLOR_PRIMARY = new Color(83, 74, 183);
    private static final Color COLOR_GREEN   = new Color(15, 110, 86);
    private static final Color COLOR_CORAL   = new Color(153, 60, 29);
    private static final Color COLOR_BG      = new Color(245, 245, 248);
    private static final Font  FONT_NORMAL   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font  FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font  FONT_STAT     = new Font("Segoe UI", Font.BOLD, 24);

    public DashboardFrame(DashboardService dashboardService,
                          EnseignantRepository enseignantRepo) {
        super("Dashboard", true, true, true, true);
        this.dashboardService = dashboardService;
        this.enseignantRepo   = enseignantRepo;
        setSize(860, 600);
        setLocation(60, 50);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(COLOR_BG);
        root.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));

        // Stats cards
        root.add(buildStatCards(), BorderLayout.NORTH);

        // Tabs
        root.add(buildTabs(), BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildStatCards() {
        JPanel panel =new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setBackground(COLOR_BG);
        panel.add(buildCard("Soutenances",
            String.valueOf(dashboardService.totalSoutenances()), COLOR_PRIMARY));
        panel.add(buildCard("Étudiants",
            String.valueOf(dashboardService.totalEtudiants()), COLOR_GREEN));
        panel.add(buildCard("Enseignants",
            String.valueOf(dashboardService.totalProfs()), COLOR_CORAL));
        panel.add(buildCard("Filières",
            String.valueOf(dashboardService.totalFilieres()),
            new Color(100, 80, 160)));
        return panel;
    }

    private JPanel buildCard(String label, String value, Color color) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 228)),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel num = new JLabel(value, SwingConstants.CENTER);
        num.setFont(FONT_STAT);
        num.setForeground(color);

        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(FONT_NORMAL);
        lbl.setForeground(Color.GRAY);

        card.add(num);
        card.add(lbl);
        return card;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(FONT_NORMAL);

        // Construire maps
        Map<String, Integer> soutenancesParProf = new LinkedHashMap<>();
        Map<String, Integer> etudiantsParProf   = new LinkedHashMap<>();
        for (Enseignant e : getEnseignants()) {
            String nom = e.getNom() + " " + e.getPrenom();
            soutenancesParProf.put(nom,
                dashboardService.nbSoutenancesParProf(e));
            etudiantsParProf.put(nom,
                dashboardService.nbEtudiantsParProf(e));
        }

        Map<String, Integer> soutenancesParFiliere = new LinkedHashMap<>();
        for (Filiere f : dashboardService.getFilieres()) {
            soutenancesParFiliere.put(f.getNom(),
                dashboardService.nbSoutenancesParFiliere(f));
        }

        tabs.addTab("Soutenances / prof",
            buildMixedPanel(soutenancesParProf,
                "Enseignant", "Soutenances", COLOR_PRIMARY));
        tabs.addTab("Étudiants / prof",
            buildMixedPanel(etudiantsParProf,
                "Enseignant", "Étudiants", COLOR_GREEN));
        tabs.addTab("Soutenances / filière",
            buildMixedPanel(soutenancesParFiliere,
                "Filière", "Soutenances", COLOR_CORAL));
        tabs.addTab("Équilibre",
            buildEquilibrePanel());

        return tabs;
    }

    private List<Enseignant> getEnseignants() {
        return enseignantRepo.chargerTous();
    }

    private JPanel buildMixedPanel(Map<String, Integer> data,
                                    String col1, String col2, Color color) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 6, 6, 6));
        panel.add(buildBarsPanel(data, color));
        panel.add(buildTablePanel(data, col1, col2));
        return panel;
    }

    private JPanel buildBarsPanel(Map<String, Integer> data, Color color) {
        int max = data.values().stream().mapToInt(i -> i).max().orElse(1);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 228)),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel title = new JLabel("Répartition");
        title.setFont(FONT_TITLE);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        wrapper.add(title, BorderLayout.NORTH);

        JPanel bars = new JPanel();
        bars.setLayout(new BoxLayout(bars, BoxLayout.Y_AXIS));
        bars.setBackground(Color.WHITE);

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int val = entry.getValue();
            int pct = max == 0 ? 0 : (int)(val * 100.0 / max);

            JPanel row = new JPanel(new BorderLayout(6, 0));
            row.setBackground(Color.WHITE);
            row.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));

            JLabel lbl = new JLabel(entry.getKey());
            lbl.setFont(FONT_NORMAL);
            lbl.setPreferredSize(new Dimension(130, 18));

            JPanel barWrap = new JPanel(new BorderLayout());
            barWrap.setBackground(new Color(235, 235, 242));

            final Color barColor = color;
            final int barPct = pct;
            JPanel bar = new JPanel() {
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(barColor);
                    g2.fillRoundRect(0, 0,
                        (int)(getWidth() * barPct / 100.0),
                        getHeight(), 4, 4);
                }
            };
            bar.setOpaque(false);
            bar.setPreferredSize(new Dimension(0, 14));
            barWrap.add(bar);

            JLabel valLbl = new JLabel(String.valueOf(val));
            valLbl.setFont(FONT_NORMAL);
            valLbl.setForeground(Color.GRAY);
            valLbl.setPreferredSize(new Dimension(22, 18));
            valLbl.setHorizontalAlignment(SwingConstants.RIGHT);

            row.add(lbl,    BorderLayout.WEST);
            row.add(barWrap, BorderLayout.CENTER);
            row.add(valLbl, BorderLayout.EAST);
            bars.add(row);
        }

        wrapper.add(new JScrollPane(bars) {{ setBorder(null); }},
            BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildTablePanel(Map<String, Integer> data,
                                    String col1, String col2) {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{ col1, col2 }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        data.forEach((k, v) -> model.addRow(new Object[]{ k, v }));

        JTable table = new JTable(model);
        table.setFont(FONT_NORMAL);
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.getTableHeader().setFont(FONT_TITLE);
        table.getTableHeader().setBackground(COLOR_BG);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 228)),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        JLabel title = new JLabel("Détail");
        title.setFont(FONT_TITLE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 0));
        wrapper.add(title, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(table) {{ setBorder(null); }},
            BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildEquilibrePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        addInfo(panel, "Moyenne soutenances / prof",
            String.format("%.1f", dashboardService.calculerMoyenneSoutenances()));
        addInfo(panel, "Écart type",
            String.format("%.2f", dashboardService.calculerEcartType()));
        addInfo(panel, "Taux de remplissage",
            String.format("%.1f%%", dashboardService.tauxRemplissage()));
        addInfo(panel, "Capacité totale",
            String.valueOf(dashboardService.capaciteTotale()));
        addInfo(panel, "Horaires journée",
            dashboardService.getHorairesJournee());
        addInfo(panel, "Planning équilibré ?",
            dashboardService.estPlanningEquilibre() ? " Oui" : " Non");

        return panel;
    }

    private void addInfo(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0,
                new Color(235, 235, 242)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_NORMAL);
        lbl.setForeground(Color.GRAY);

        JLabel val = new JLabel(value);
        val.setFont(FONT_TITLE);
        val.setForeground(new Color(30, 30, 40));

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        panel.add(row);
        panel.add(Box.createVerticalStrut(2));
    }
}