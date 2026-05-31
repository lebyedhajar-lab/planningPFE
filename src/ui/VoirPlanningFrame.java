package ui;

import model.*;
import repository.SoutenanceRepository;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VoirPlanningFrame extends JInternalFrame{

    private final SoutenanceRepository soutenanceRepo;
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public VoirPlanningFrame(SoutenanceRepository soutenanceRepo) {
        super("Planning des Soutenances", true, true, true, true);
        this.soutenanceRepo = soutenanceRepo;
        setSize(900, 500);
        setLocation(60, 100);
        initUI();
    }

    private void initUI() {
    	String[] cols = {
    		    "#", "Étudiant", "Filière", "Date", "Heure", "Salle",
    		    "Encadrant", "Membre 1", "Membre 2", "Langue"
    		};
        List<Soutenance> list = soutenanceRepo.chargerTous();
        list.sort((a, b) -> {
            int cmp = a.getCreneau().getDateJour()
                       .compareTo(b.getCreneau().getDateJour());
            if (cmp != 0) return cmp;
            cmp = a.getCreneau().getHeureDebut()
                   .compareTo(b.getCreneau().getHeureDebut());
            if (cmp != 0) return cmp;
            return a.getSalle().getNom()
                    .compareTo(b.getSalle().getNom());
        });
        Object[][] data = new Object[list.size()][cols.length];

        for (int i = 0; i < list.size(); i++) {
            Soutenance s = list.get(i);
            data[i][0] = i+1;
            data[i][1] = s.getEtudiant().getNom()
                       + " " + s.getEtudiant().getPrenom();
            data[i][2] = s.getEtudiant().getFiliere() != null
                       ? s.getEtudiant().getFiliere().getNom() : "—";
            data[i][3] = s.getCreneau().getDateJour().format(FMT);
            data[i][4] = s.getCreneau().getHeureDebut()
                       + " - " + s.getCreneau().getHeureFin();
            data[i][5] = s.getSalle().getNom();
            data[i][6] = s.getJury().getEncadrant().getNom()
                    + " " + s.getJury().getEncadrant().getPrenom();

            List<Enseignant> membres = s.getJury().getMembres();
            data[i][7] = membres.size() > 0
             ? membres.get(0).getNom() + " " + membres.get(0).getPrenom() : "—";
            data[i][8] = membres.size() > 1
             ? membres.get(1).getNom() + " " + membres.get(1).getPrenom() : "—";
            data[i][9] = s.getLangue();
        }

        DefaultTableModel model = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(
            new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(83, 74, 183));
        table.getTableHeader().setForeground(Color.WHITE);

        // Alternance couleurs
        table.setDefaultRenderer(Object.class,
            new DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel,
                        boolean foc, int row, int col) {
                    super.getTableCellRendererComponent(
                        t, v, sel, foc, row, col);
                    setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                    if (!sel) {
                        setBackground(row % 2 == 0
                            ? Color.WHITE
                            : new Color(245, 244, 255));
                        setForeground(Color.DARK_GRAY);
                    }
                    return this;
                }
            });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lbl = new JLabel("Total : " + list.size() + " soutenance(s)");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Color.GRAY);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        root.add(lbl, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        setContentPane(root);
    }
}