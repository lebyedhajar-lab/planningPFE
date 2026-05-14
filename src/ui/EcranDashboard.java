package ui;

import model.*;
import repository.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class EcranDashboard extends JFrame {

    private SoutenanceRepository soutenanceRepo;
    private EnseignantRepository enseignantRepo;

    public EcranDashboard() {
        setTitle("Dashboard");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        soutenanceRepo = new SoutenanceRepository();
        enseignantRepo = new EnseignantRepository();

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titre = new JLabel("Dashboard — Statistiques", SwingConstants.CENTER);
        titre.setFont(new Font("Calibri", Font.BOLD, 20));
        titre.setForeground(new Color(31, 56, 100));
        titre.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titre, BorderLayout.NORTH);

        JTabbedPane onglets = new JTabbedPane();
        onglets.setFont(new Font("Calibri", Font.BOLD, 13));

        onglets.addTab("Soutenances par Prof",   creerTableauStats(getSoutenancesParProf()));
        onglets.addTab("Étudiants par Prof",     creerTableauStats(getEtudiantsParProf()));
        onglets.addTab("Soutenances par Filière", creerTableauStats(getSoutenancesParFiliere()));

        panel.add(onglets, BorderLayout.CENTER);

        JPanel panelBas = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBas.setBackground(Color.WHITE);
        JButton btnFermer = creerBouton("Fermer", new Color(150, 40, 40));
        btnFermer.addActionListener(e -> dispose());
        panelBas.add(btnFermer);
        panel.add(panelBas, BorderLayout.SOUTH);

        add(panel);
    }

    private Map<String, Integer> getSoutenancesParProf() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Soutenance s : soutenanceRepo.chargerTous()) {
            if (s.getJury() == null) continue;
            // Encadrant
            Enseignant enc = s.getJury().getEncadrant();
            if (enc != null) {
                String nom = enc.getNom() + " " + enc.getPrenom();
                map.put(nom, map.getOrDefault(nom, 0) + 1);
            }
            // Membres
            if (s.getJury().getMembres() != null) {
                for (Enseignant m : s.getJury().getMembres()) {
                    String nom = m.getNom() + " " + m.getPrenom();
                    map.put(nom, map.getOrDefault(nom, 0) + 1);
                }
            }
        }
        return map;
    }

    private Map<String, Integer> getEtudiantsParProf() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Soutenance s : soutenanceRepo.chargerTous()) {
            if (s.getJury() == null || s.getJury().getEncadrant() == null) continue;
            Enseignant enc = s.getJury().getEncadrant();
            String nom = enc.getNom() + " " + enc.getPrenom();
            map.put(nom, map.getOrDefault(nom, 0) + 1);
        }
        return map;
    }

    private Map<String, Integer> getSoutenancesParFiliere() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Soutenance s : soutenanceRepo.chargerTous()) {
            if (s.getEtudiant() == null || s.getEtudiant().getFiliere() == null) continue;
            String nom = s.getEtudiant().getFiliere().getNom();
            map.put(nom, map.getOrDefault(nom, 0) + 1);
        }
        return map;
    }

    private JScrollPane creerTableauStats(Map<String, Integer> donnees) {
        String[] colonnes = { "Nom", "Nombre" };
        DefaultTableModel modele = new DefaultTableModel(colonnes, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        if (donnees.isEmpty()) {
            modele.addRow(new Object[]{ "Aucune donnée disponible", "—" });
        } else {
            for (Map.Entry<String, Integer> entry : donnees.entrySet()) {
                modele.addRow(new Object[]{ entry.getKey(), entry.getValue() });
            }
        }

        JTable table = new JTable(modele);
        table.setRowHeight(28);
        table.setFont(new Font("Calibri", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(31, 56, 100));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(200, 200, 200));
        table.setSelectionBackground(new Color(217, 225, 242));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? new Color(217, 225, 242) : Color.WHITE);
                if (col == 1) setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        return new JScrollPane(table);
    }

    private JButton creerBouton(String texte, Color couleur) {
        JButton btn = new JButton(texte);
        btn.setFont(new Font("Calibri", Font.BOLD, 13));
        btn.setBackground(couleur);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
