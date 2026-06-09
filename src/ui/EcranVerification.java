package ui;

import Config.ConfigPlanning;
import model.*;
import repository.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class EcranVerification extends JInternalFrame {

    private SoutenanceRepository soutenanceRepo;
    private DefaultTableModel modele;
    private final int ecartMin;

    public EcranVerification(SoutenanceRepository soutenanceRepo,
                             ConfigPlanning config) {
        super("Vérification du Planning", true, true, true, true);
    	this.soutenanceRepo = soutenanceRepo;
    	this.ecartMin = config.getEcartMinEntreSoutenances();

    	setTitle("Vérification du Planning");
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //soutenanceRepo = new SoutenanceRepository();

        // ── Panel principal ──────────────────────────────────────
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ── Titre ────────────────────────────────────────────────
        JLabel titre = new JLabel("Vérification du Planning", SwingConstants.CENTER);
        titre.setFont(new Font("Calibri", Font.BOLD, 20));
        titre.setForeground(new Color(31, 56, 100));
        titre.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titre, BorderLayout.NORTH);

        // ── Tableau des erreurs ──────────────────────────────────
        String[] colonnes = { "Type de problème", "Détail", "Statut" };
        modele = new DefaultTableModel(colonnes, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tableau = new JTable(modele);
        tableau.setRowHeight(28);
        tableau.setFont(new Font("Calibri", Font.PLAIN, 13));
        tableau.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 13));
        tableau.getTableHeader().setBackground(new Color(31, 56, 100));
        tableau.getTableHeader().setForeground(Color.WHITE);
        tableau.setGridColor(new Color(200, 200, 200));

        // Colorer la colonne Statut
        tableau.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    if (col == 2) {
                        String statut = val != null ? val.toString() : "";
                        if (statut.equals("OK"))      c.setBackground(new Color(198, 239, 206));
                        else if (statut.equals("PROBLÈME")) c.setBackground(new Color(255, 199, 206));
                        else c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(row % 2 == 0 ? new Color(242, 242, 242) : Color.WHITE);
                    }
                }
                return c;
            }
        });

        // Taille colonne Statut
        tableau.getColumnModel().getColumn(2).setMaxWidth(100);
        tableau.getColumnModel().getColumn(2).setMinWidth(100);

        JScrollPane scrollPane = new JScrollPane(tableau);
        panel.add(scrollPane, BorderLayout.CENTER);

        // ── Label résumé ─────────────────────────────────────────
        JLabel labelResume = new JLabel("Cliquez sur 'Vérifier' pour lancer l'analyse.", SwingConstants.LEFT);
        labelResume.setFont(new Font("Calibri", Font.ITALIC, 12));
        labelResume.setForeground(new Color(100, 100, 100));
        labelResume.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        // ── Boutons ──────────────────────────────────────────────
        JPanel panelBas = new JPanel(new BorderLayout());
        panelBas.setBackground(Color.WHITE);

        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBoutons.setBackground(Color.WHITE);

        JButton btnVerifier = creerBouton("Vérifier", new Color(46, 117, 182));
        JButton btnFermer   = creerBouton("Fermer", new Color(150, 40, 40));

        panelBoutons.add(btnVerifier);
        panelBoutons.add(btnFermer);

        panelBas.add(labelResume, BorderLayout.WEST);
        panelBas.add(panelBoutons, BorderLayout.EAST);
        panel.add(panelBas, BorderLayout.SOUTH);

        // ── Actions ──────────────────────────────────────────────
        btnVerifier.addActionListener(e -> {
            lancerVerification();
            int nbProblemes = compterProblemes();
            if (nbProblemes == 0) {
                labelResume.setText("✔ Aucun problème détecté. Le planning est valide.");
                labelResume.setForeground(new Color(0, 120, 0));
            } else {
                labelResume.setText("✘ " + nbProblemes + " problème(s) détecté(s).");
                labelResume.setForeground(new Color(180, 0, 0));
            }
        });

        btnFermer.addActionListener(e -> dispose());

        add(panel);
    }

    // ── Lancer toutes les vérifications ─────────────────────────
    private void lancerVerification() {
        modele.setRowCount(0);
        List<Soutenance> soutenances = soutenanceRepo.chargerTous();

        verifierChevauchementSalles(soutenances);
        verifierProfDansDeux(soutenances);
        verifierEcartMinimum(soutenances);
        verifierRepartitionEncadrants(soutenances);
    }

    // Vérification 1 : deux soutenances dans la même salle au même moment
    private void verifierChevauchementSalles(List<Soutenance> soutenances) {
        boolean ok = true;
        for (int i = 0; i < soutenances.size(); i++) {
            for (int j = i + 1; j < soutenances.size(); j++) {
                Soutenance s1 = soutenances.get(i);
                Soutenance s2 = soutenances.get(j);
                if (s1.getSalle() == null || s2.getSalle() == null) continue;
                if (s1.getCreneau() == null || s2.getCreneau() == null) continue;
                if (s1.getSalle().getId() == s2.getSalle().getId()
                        && s1.getCreneau().getDateJour().equals(s2.getCreneau().getDateJour())
                        && s1.getCreneau().getHeureDebut().equals(s2.getCreneau().getHeureDebut())) {
                    modele.addRow(new Object[]{
                        "Chevauchement salle",
                        "Salle " + s1.getSalle().getNom() + " occupée 2 fois à " + s1.getCreneau().getHeureDebut(),
                        "PROBLÈME"
                    });
                    ok = false;
                }
            }
        }
        if (ok) modele.addRow(new Object[]{ "Chevauchement salles", "Aucun conflit de salle", "OK" });
    }

    // Vérification 2 : prof dans deux soutenances au même horaire
    private void verifierProfDansDeux(List<Soutenance> soutenances) {
        boolean ok = true;
        for (int i = 0; i < soutenances.size(); i++) {
            for (int j = i + 1; j < soutenances.size(); j++) {
                Soutenance s1 = soutenances.get(i);
                Soutenance s2 = soutenances.get(j);
                if (s1.getCreneau() == null || s2.getCreneau() == null) continue;
                if (!s1.getCreneau().getDateJour().equals(s2.getCreneau().getDateJour())) continue;
                if (!s1.getCreneau().getHeureDebut().equals(s2.getCreneau().getHeureDebut())) continue;
                if (s1.getJury() == null || s2.getJury() == null) continue;

                for (Enseignant e : getTousLesMembres(s1.getJury())) {
                    for (Enseignant e2 : getTousLesMembres(s2.getJury())) {
                        if (e.getId() == e2.getId()) {
                            modele.addRow(new Object[]{
                                "Prof dans 2 jurys",
                                e.getNom() + " " + e.getPrenom() + " présent dans 2 soutenances à " + s1.getCreneau().getHeureDebut(),
                                "PROBLÈME"
                            });
                            ok = false;
                        }
                    }
                }
            }
        }
        if (ok) modele.addRow(new Object[]{ "Prof dans 2 jurys", "Aucun conflit d'horaire prof", "OK" });
    }

    // Vérification 3 : écart minimum de 60 min entre soutenances d'un même prof
    private void verifierEcartMinimum(List<Soutenance> soutenances) {
        boolean ok = true;
        for (int i = 0; i < soutenances.size(); i++) {
            for (int j = i + 1; j < soutenances.size(); j++) {
                Soutenance s1 = soutenances.get(i);
                Soutenance s2 = soutenances.get(j);
                if (s1.getCreneau() == null || s2.getCreneau() == null) continue;
                if (!s1.getCreneau().getDateJour().equals(s2.getCreneau().getDateJour())) continue;
                if (s1.getJury() == null || s2.getJury() == null) continue;

                for (Enseignant e : getTousLesMembres(s1.getJury())) {
                    for (Enseignant e2 : getTousLesMembres(s2.getJury())) {
                        if (e.getId() != e2.getId()) continue;
                        long ecart = Math.abs(
                            s1.getCreneau().getHeureDebut().toSecondOfDay() -
                            s2.getCreneau().getHeureDebut().toSecondOfDay()
                        ) / 60;
                        if (ecart > 0 && ecart < ecartMin) {
                            modele.addRow(new Object[]{
                                "Écart minimum",
                                e.getNom() + " : écart de " + ecart
                                    + " min (minimum requis : " + ecartMin + " min)",
                                "PROBLÈME"
                            });
                            ok = false;
                        }
                    }
                }
            }
        }
        if (ok) modele.addRow(new Object[]{
            "Écart minimum",
            "Tous les écarts respectés (≥ " + ecartMin + " min)", "OK" });
    }

    // Vérification 4 : répartition déséquilibrée des encadrants
    private void verifierRepartitionEncadrants(List<Soutenance> soutenances) {
        Map<String, Integer> charges = new LinkedHashMap<>();
        for (Soutenance s : soutenances) {
            if (s.getJury() == null || s.getJury().getEncadrant() == null) continue;
            String nom = s.getJury().getEncadrant().getNom() + " " + s.getJury().getEncadrant().getPrenom();
            charges.put(nom, charges.getOrDefault(nom, 0) + 1);
        }

        if (charges.isEmpty()) {
            modele.addRow(new Object[]{ "Répartition encadrants", "Aucune donnée", "OK" });
            return;
        }

        int max = Collections.max(charges.values());
        int min = Collections.min(charges.values());

        if (max - min > 2) {
            modele.addRow(new Object[]{
                "Répartition encadrants",
                "Déséquilibre : max " + max + " soutenances, min " + min,
                "PROBLÈME"
            });
        } else {
            modele.addRow(new Object[]{ "Répartition encadrants", "Répartition équilibrée (écart ≤ 2)", "OK" });
        }
    }

    private List<Enseignant> getTousLesMembres(Jury jury) {
        List<Enseignant> liste = new ArrayList<>();
        if (jury.getEncadrant() != null) liste.add(jury.getEncadrant());
        if (jury.getMembres() != null) liste.addAll(jury.getMembres());
        return liste;
    }

    private int compterProblemes() {
        int count = 0;
        for (int i = 0; i < modele.getRowCount(); i++) {
            if ("PROBLÈME".equals(modele.getValueAt(i, 2))) count++;
        }
        return count;
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
