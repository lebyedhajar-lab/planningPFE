package ui;

import javax.swing.*;
import java.awt.*;

public class EcranPrincipal extends JFrame {

    public EcranPrincipal() {
        setTitle("Planning PFE");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // ── Panel principal ──────────────────────────────────────
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(31, 56, 100));

        // ── Titre ────────────────────────────────────────────────
        JLabel titre = new JLabel("Planning des Soutenances PFE", SwingConstants.CENTER);
        titre.setFont(new Font("Calibri", Font.BOLD, 26));
        titre.setForeground(Color.WHITE);
        titre.setBorder(BorderFactory.createEmptyBorder(40, 0, 10, 0));
        panel.add(titre, BorderLayout.NORTH);

        JLabel sousTitre = new JLabel("Gestion et organisation des soutenances", SwingConstants.CENTER);
        sousTitre.setFont(new Font("Calibri", Font.ITALIC, 14));
        sousTitre.setForeground(new Color(200, 210, 230));

        // ── Boutons ──────────────────────────────────────────────
        JPanel panelBoutons = new JPanel();
        panelBoutons.setLayout(new GridLayout(4, 1, 10, 10));
        panelBoutons.setBackground(new Color(31, 56, 100));
        panelBoutons.setBorder(BorderFactory.createEmptyBorder(30, 120, 40, 120));

        JButton btnPlanning     = creerBouton("Planning des Soutenances");
        JButton btnDashboard    = creerBouton("Dashboard");
        JButton btnVerification = creerBouton("Vérification");
        JButton btnQuitter      = creerBouton("Quitter");
        btnQuitter.setBackground(new Color(150, 40, 40));

        panelBoutons.add(btnPlanning);
        panelBoutons.add(btnDashboard);
        panelBoutons.add(btnVerification);
        panelBoutons.add(btnQuitter);

        // ── Actions ──────────────────────────────────────────────
        btnPlanning.addActionListener(e -> {
            new EcranPlanning().setVisible(true);
        });

        btnDashboard.addActionListener(e -> {
            new EcranDashboard().setVisible(true);
        });

        btnVerification.addActionListener(e -> {
            new EcranVerification().setVisible(true);
        });

        btnQuitter.addActionListener(e -> System.exit(0));

        // ── Assemblage ───────────────────────────────────────────
        JPanel centre = new JPanel(new BorderLayout());
        centre.setBackground(new Color(31, 56, 100));
        centre.add(sousTitre, BorderLayout.NORTH);
        centre.add(panelBoutons, BorderLayout.CENTER);

        panel.add(centre, BorderLayout.CENTER);
        add(panel);
    }

    private JButton creerBouton(String texte) {
        JButton btn = new JButton(texte);
        btn.setFont(new Font("Calibri", Font.BOLD, 14));
        btn.setBackground(new Color(46, 117, 182));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EcranPrincipal().setVisible(true));
    }
}
