package ui;

import historique.*;
import model.Soutenance;
import repository.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoriquePlanningFrame extends JInternalFrame {

    private static final Color COLOR_PRIMARY = new Color(83, 74, 183);
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final MainFrame mainFrame;
    private final PlanningHistoriqueService historiqueService;
    private DefaultTableModel modele;

    public HistoriquePlanningFrame(MainFrame mainFrame) {
        super("Historique des plannings", true, true, true, true);
        this.mainFrame = mainFrame;
        this.historiqueService = new PlanningHistoriqueService();
        setSize(720, 420);
        setLocation(100, 90);
        initUI();
        actualiserListe();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel info = new JLabel(
            "Chargez un planning sauvegardé sans le regénérer. "
            + "L'Excel doit être chargé au préalable.");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setForeground(Color.GRAY);
        root.add(info, BorderLayout.NORTH);

        String[] cols = { "Date", "Soutenances", "Fichier Excel", "Identifiant" };
        modele = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(modele);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(COLOR_PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) chargerSelection(table);
            }
        });

        root.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnActualiser = creerBouton("Actualiser", COLOR_PRIMARY);
        JButton btnCharger    = creerBouton("Charger", new Color(15, 110, 86));
        JButton btnSupprimer  = creerBouton("Supprimer", new Color(153, 60, 29));

        btnActualiser.addActionListener(e -> actualiserListe());
        btnCharger.addActionListener(e -> chargerSelection(table));
        btnSupprimer.addActionListener(e -> supprimerSelection(table));

        boutons.add(btnActualiser);
        boutons.add(btnCharger);
        boutons.add(btnSupprimer);
        root.add(boutons, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JButton creerBouton(String texte, Color couleur) {
        JButton btn = new JButton(texte);
        btn.setBackground(couleur);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private void actualiserListe() {
        modele.setRowCount(0);
        try {
            List<PlanningHistorique> liste = historiqueService.lister();
            for (PlanningHistorique h : liste) {
                modele.addRow(new Object[]{
                    h.getDateCreation().format(FMT),
                    h.getNbSoutenances(),
                    h.getNomFichierExcel(),
                    h.getId()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erreur : " + ex.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chargerSelection(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Sélectionnez un planning dans la liste.",
                "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (mainFrame.getEtudiantRepo().chargerTous().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Chargez d'abord le fichier Excel.",
                "Données manquantes", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = modele.getValueAt(row, 3).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
            "Charger le planning \"" + id + "\" ?\n"
            + "Le planning actuel sera remplacé.",
            "Confirmer", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            List<Soutenance> soutenances = historiqueService.charger(
                id,
                mainFrame.getEtudiantRepo(),
                mainFrame.getEnseignantRepo(),
                mainFrame.getSalleRepo());
            mainFrame.appliquerPlanning(soutenances);
            JOptionPane.showMessageDialog(this,
                soutenances.size() + " soutenance(s) chargée(s).",
                "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Impossible de charger :\n" + ex.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimerSelection(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Sélectionnez un planning à supprimer.",
                "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = modele.getValueAt(row, 3).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer définitivement \"" + id + "\" ?",
            "Confirmer", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            historiqueService.supprimer(id);
            actualiserListe();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erreur : " + ex.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
