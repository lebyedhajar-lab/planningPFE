package ui;

import model.Etudiant;
import repository.EtudiantRepository;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class EtudiantsFrame extends JInternalFrame {

    private final EtudiantRepository etudiantRepo;

    public EtudiantsFrame(EtudiantRepository etudiantRepo) {
        super("Liste des Étudiants", true, true, true, true);
        this.etudiantRepo = etudiantRepo;
        setSize(750, 420);
        setLocation(140, 100);
        initUI();
    }

    private void initUI() {
        String[] cols = { "ID", "Nom", "Prénom", "Filière", "Encadrant", "Titre PFE" };
        List<Etudiant> list = etudiantRepo.chargerTous();
        Object[][] data = new Object[list.size()][cols.length];

        for (int i = 0; i < list.size(); i++) {
            Etudiant e = list.get(i);
            data[i][0] = e.getId();
            data[i][1] = e.getNom();
            data[i][2] = e.getPrenom();
            data[i][3] = e.getFiliere() != null ? e.getFiliere().getNom() : "—";
            data[i][4] = e.getEncadrant() != null
                       ? e.getEncadrant().getNom() + " " + e.getEncadrant().getPrenom()
                       : "—";
            data[i][5] = e.getTitrePFE();
        }

        DefaultTableModel model = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(153, 60, 29));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel lbl = new JLabel(list.size() + " étudiant(s)");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Color.GRAY);
        root.add(lbl, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        setContentPane(root);
    }
}