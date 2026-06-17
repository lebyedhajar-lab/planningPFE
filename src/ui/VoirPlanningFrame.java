package ui;

import model.*;
import repository.SoutenanceRepository;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VoirPlanningFrame extends JInternalFrame {

    private final SoutenanceRepository soutenanceRepo;
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public VoirPlanningFrame(SoutenanceRepository soutenanceRepo) {
        super("Planning des Soutenances", true, true, true, true);
        this.soutenanceRepo = soutenanceRepo;
        setSize(1200, 550);
        setLocation(60, 100);
        initUI();
    }

    private void initUI() {
        String[] cols = {
            "ID", "Encadrant", "Membre 1", "Membre 2",
            "Date", "Heure", "Salle",
            "Nom etudiant", "Prenom etudiant", "Filiere"
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

            String encadrant = s.getJury() != null
                && s.getJury().getEncadrant() != null
                ? s.getJury().getEncadrant().getNom() + " "
                  + s.getJury().getEncadrant().getPrenom() : "-";

            List<Enseignant> membres = s.getJury().getMembres();
            String membre1 = membres.size() > 0
                ? membres.get(0).getNom() + " "
                  + membres.get(0).getPrenom() : "-";
            String membre2 = membres.size() > 1
                ? membres.get(1).getNom() + " "
                  + membres.get(1).getPrenom() : "-";

            data[i][0] = i + 1;
            data[i][1] = encadrant;
            data[i][2] = membre1;
            data[i][3] = membre2;
            data[i][4] = s.getCreneau().getDateJour().format(FMT);
            data[i][5] = s.getCreneau().getHeureDebut().toString();
            data[i][6] = s.getSalle().getNom();
            data[i][7] = s.getEtudiant().getNom();
            data[i][8] = s.getEtudiant().getPrenom();
            data[i][9] = s.getEtudiant().getFiliere() != null
                ? s.getEtudiant().getFiliere().getNom() : "-";
        }

        DefaultTableModel model = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Calibri", Font.PLAIN, 12));
        table.setRowHeight(26);
        table.setShowGrid(true);
        table.setGridColor(new Color(200, 200, 210));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.getTableHeader().setFont(
            new Font("Calibri", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(31, 56, 100));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(180, 200, 240));

        // Alternance couleurs comme le PDF
        table.setDefaultRenderer(Object.class,
            new DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel,
                        boolean foc, int row, int col) {
                    super.getTableCellRendererComponent(
                        t, v, sel, foc, row, col);
                    setBorder(BorderFactory
                        .createEmptyBorder(0, 6, 0, 6));
                    if (!sel) {
                        setBackground(row % 2 == 0
                            ? new Color(217, 226, 243)
                            : Color.WHITE);
                        setForeground(Color.BLACK);
                    }
                    if (col == 0 || col == 4
                        || col == 5 || col == 6) {
                        setHorizontalAlignment(CENTER);
                    } else {
                        setHorizontalAlignment(LEFT);
                    }
                    return this;
                }
            });

        // Largeurs colonnes
        int[] widths = {
            40, 150, 150, 150, 90, 70, 70, 120, 100, 60
        };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i)
                .setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lbl = new JLabel(
            "Total : " + list.size() + " soutenance(s)");
        lbl.setFont(new Font("Calibri", Font.PLAIN, 12));
        lbl.setForeground(Color.GRAY);
        lbl.setBorder(
            BorderFactory.createEmptyBorder(0, 0, 8, 0));

        root.add(lbl, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        setContentPane(root);
    }
}