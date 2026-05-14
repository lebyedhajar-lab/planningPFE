package ui;

import model.*;
import export.ExportDocx;
import repository.SoutenanceRepository;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class EcranPlanning extends JFrame {

    private JTable tableau;
    private DefaultTableModel modele;
    private SoutenanceRepository soutenanceRepo;

    public EcranPlanning() {
        setTitle("Planning des Soutenances");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        soutenanceRepo = new SoutenanceRepository();

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titre = new JLabel("Planning des Soutenances", SwingConstants.CENTER);
        titre.setFont(new Font("Calibri", Font.BOLD, 20));
        titre.setForeground(new Color(31, 56, 100));
        titre.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titre, BorderLayout.NORTH);

        String[] colonnes = { "Étudiant", "Filière", "Encadrant", "Membres du Jury", "Date", "Heure", "Salle", "Langue" };
        modele = new DefaultTableModel(colonnes, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tableau = new JTable(modele);
        tableau.setRowHeight(28);
        tableau.setFont(new Font("Calibri", Font.PLAIN, 13));
        tableau.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 13));
        tableau.getTableHeader().setBackground(new Color(31, 56, 100));
        tableau.getTableHeader().setForeground(Color.WHITE);
        tableau.setSelectionBackground(new Color(217, 225, 242));
        tableau.setGridColor(new Color(200, 200, 200));

        tableau.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? new Color(217, 225, 242) : Color.WHITE);
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableau);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel panelBas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBas.setBackground(Color.WHITE);

        JButton btnActualiser = creerBouton("Actualiser", new Color(46, 117, 182));
        JButton btnExporter   = creerBouton("Exporter Word", new Color(31, 56, 100));
        JButton btnFermer     = creerBouton("Fermer", new Color(150, 40, 40));

        panelBas.add(btnActualiser);
        panelBas.add(btnExporter);
        panelBas.add(btnFermer);
        panel.add(panelBas, BorderLayout.SOUTH);

        btnActualiser.addActionListener(e -> chargerDonnees());

        btnExporter.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File("planning.doc"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    ExportDocx exporter = new ExportDocx();
                    exporter.generer(soutenanceRepo.chargerTous(), fc.getSelectedFile().getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "Fichier exporté avec succès !");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnFermer.addActionListener(e -> dispose());

        add(panel);
        chargerDonnees();
    }

    private void chargerDonnees() {
        modele.setRowCount(0);
        List<Soutenance> soutenances = soutenanceRepo.chargerTous();

        if (soutenances.isEmpty()) {
            modele.addRow(new Object[]{ "Aucune soutenance planifiée", "", "", "", "", "", "", "" });
            return;
        }

        for (Soutenance s : soutenances) {
            String etudiant  = s.getEtudiant() != null
                    ? s.getEtudiant().getNom() + " " + s.getEtudiant().getPrenom() : "—";
            String filiere   = (s.getEtudiant() != null && s.getEtudiant().getFiliere() != null)
                    ? s.getEtudiant().getFiliere().getNom() : "—";
            String encadrant = (s.getJury() != null && s.getJury().getEncadrant() != null)
                    ? s.getJury().getEncadrant().getNom() + " " + s.getJury().getEncadrant().getPrenom() : "—";
            String membres   = construireListeMembres(s.getJury());
            String date      = s.getCreneau() != null ? s.getCreneau().getDateJour().toString() : "—";
            String heure     = s.getCreneau() != null
                    ? s.getCreneau().getHeureDebut() + " – " + s.getCreneau().getHeureFin() : "—";
            String salle     = s.getSalle() != null ? s.getSalle().getNom() : "—";
            String langue    = s.getLangue() != null ? s.getLangue() : "—";

            modele.addRow(new Object[]{ etudiant, filiere, encadrant, membres, date, heure, salle, langue });
        }
    }

    private String construireListeMembres(Jury jury) {
        if (jury == null || jury.getMembres() == null || jury.getMembres().isEmpty()) return "—";
        StringBuilder sb = new StringBuilder();
        for (Enseignant m : jury.getMembres()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(m.getNom()).append(" ").append(m.getPrenom());
        }
        return sb.toString();
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
