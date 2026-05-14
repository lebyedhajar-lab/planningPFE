package ui;

import model.Enseignant;
import repository.EnseignantRepository;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class EnseignantsFrame extends JInternalFrame {

    private final EnseignantRepository enseignantRepo;

    public EnseignantsFrame(EnseignantRepository enseignantRepo) {
        super("Liste des Enseignants", true, true, true, true);
        this.enseignantRepo = enseignantRepo;
        setSize(700, 400);
        setLocation(100, 120);
        initUI();
    }

    private void initUI() {
        String[] cols = { "ID", "Nom", "Prénom", "Spécialité", "Anglophone", "Nb soutenances" };
        List<Enseignant> list = enseignantRepo.chargerTous();
        Object[][] data = new Object[list.size()][cols.length];

        for (int i = 0; i < list.size(); i++) {
            Enseignant e = list.get(i);
            data[i][0] = e.getId();
            data[i][1] = e.getNom();
            data[i][2] = e.getPrenom();
            data[i][3] = e.getSpecialite();
            data[i][4] = e.isAnglophone() ? "Oui" : "Non";
            data[i][5] = e.getNbSoutenance();
        }

        JTable table = buildTable(data, cols);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lbl = new JLabel(list.size() + " enseignant(s)");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Color.GRAY);

        root.add(lbl, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JTable buildTable(Object[][] data, String[] cols) {
        DefaultTableModel model = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(15, 110, 86));
        table.getTableHeader().setForeground(Color.WHITE);
        return table;
    }
}