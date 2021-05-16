package Vue;

import Modele.Constante;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Ce panel permet de modifier les options du jeu
 *
 * @TODO : Changer les images des boutons selectionnés
 */

class PanelOptions extends JPanel {
    private Bouton bRetour, bCommencer;

    public PanelOptions(int largeur, int hauteur) {
        initialiserPanel(largeur, hauteur);
    }

    public void initialiserPanel(int largeur, int hauteur) {
        setBackground(new Color(47, 112, 162));
        BoxLayout boxlayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(boxlayout);

        /* Label */
        JLabel titre = new JLabel(new ImageIcon("src/Ressources/logo/logo.png"));
        JLabel versus_texte = new JLabel("Mode de jeu");
        JLabel IA_texte = new JLabel("Difficulté de l'IA");
        JLabel IA2_texte = new JLabel("Difficulté de l'IA 2");

        titre.setAlignmentX(CENTER_ALIGNMENT);
        titre.setMaximumSize(new Dimension(415, 100));

        /* JPanel */
        OptionPanel contenu = new OptionPanel();
        JPanel versus_panel = new JPanel();
        JPanel IA_panel = new JPanel();
        JPanel IA2_panel = new JPanel();
        JPanel boutons_principaux_panel = new JPanel();

        contenu.setAlignmentX(CENTER_ALIGNMENT);
        contenu.setMaximumSize(new Dimension((int) (largeur * 0.55), hauteur * 2/3));

        boutons_principaux_panel.setOpaque(false);
        boutons_principaux_panel.setMaximumSize(new Dimension(largeur, hauteur / 10));
        boutons_principaux_panel.setBorder(null);

        /* Boutons*/
        ButtonGroup adversaires_boutons = new ButtonGroup();
        ButtonGroup boutons_IA = new ButtonGroup();
        ButtonGroup boutons_IA2 = new ButtonGroup();

        JRadioButton joueur_joueur = new BoutonRadio(Constante.CHEMIN_RESSOURCE + "/bouton/joueur_contre_joueur",largeur/6, largeur / 30, adversaires_boutons);
        JRadioButton joueur_ia = new BoutonRadio(Constante.CHEMIN_RESSOURCE + "/bouton/joueur_contre_ia",largeur/6, largeur / 30, adversaires_boutons);
        JRadioButton ia_ia = new BoutonRadio(Constante.CHEMIN_RESSOURCE + "/bouton/ia_contre_ia", largeur/6, largeur / 30, adversaires_boutons);

        JRadioButton facile = new BoutonRadio(Constante.CHEMIN_RESSOURCE + "/bouton/facile",largeur/6, largeur / 30, boutons_IA);
        JRadioButton normale = new BoutonRadio(Constante.CHEMIN_RESSOURCE + "/bouton/normale",largeur/6, largeur / 30, boutons_IA);
        JRadioButton difficile = new BoutonRadio(Constante.CHEMIN_RESSOURCE + "/bouton/difficile",largeur/6, largeur / 30, boutons_IA);

        JRadioButton facile2 = new BoutonRadio(Constante.CHEMIN_RESSOURCE + "/bouton/facile", largeur/6, largeur / 30, boutons_IA2);
        JRadioButton normale2 = new BoutonRadio(Constante.CHEMIN_RESSOURCE + "/bouton/normale", largeur/6, largeur / 30, boutons_IA2);
        JRadioButton difficile2 = new BoutonRadio(Constante.CHEMIN_RESSOURCE + "/bouton/difficile", largeur/6, largeur / 30, boutons_IA2);

        bCommencer = new Bouton("src/Ressources/bouton/commencer_la_partie.png", "src/Ressources/bouton/commencer_la_partie_hover.png", largeur / 4, largeur / 20);
        bRetour = new Bouton("src/Ressources/bouton/retour.png", "src/Ressources/bouton/retour_hover.png", largeur / 4, largeur / 20);

        /* Evenements */
        bRetour.addActionListener(this::actionBoutonRetourMenu);

        joueur_joueur.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IA_texte.setVisible(false);
                IA2_texte.setVisible(false);
                IA_panel.setVisible(false);
                IA2_panel.setVisible(false);
            }
        });

        joueur_ia.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IA_texte.setVisible(true);
                IA2_texte.setVisible(false);
                IA_panel.setVisible(true);
                IA2_panel.setVisible(false);
            }
        });

        ia_ia.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IA_texte.setVisible(true);
                IA2_texte.setVisible(true);
                IA_panel.setVisible(true);
                IA2_panel.setVisible(true);

            }
        });
        /* Add dans les sous-panel */
        versus_panel.add(joueur_joueur);
        versus_panel.add(joueur_ia);
        versus_panel.add(ia_ia);

        IA_panel.add(facile);
        IA_panel.add(normale);
        IA_panel.add(difficile);

        IA2_panel.add(facile2);
        IA2_panel.add(normale2);
        IA2_panel.add(difficile2);

        contenu.add(Box.createRigidArea(new Dimension(largeur, hauteur / 30)));
        contenu.add(versus_texte);
        contenu.add(Box.createRigidArea(new Dimension(largeur, 0)));
        contenu.add(versus_panel);
        contenu.add(Box.createRigidArea(new Dimension(largeur, hauteur / 30)));
        contenu.add(IA_texte);
        contenu.add(Box.createRigidArea(new Dimension(largeur, 0)));
        contenu.add(IA_panel);
        contenu.add(Box.createRigidArea(new Dimension(largeur, hauteur / 30)));
        contenu.add(IA2_texte);
        contenu.add(Box.createRigidArea(new Dimension(largeur, 0)));
        contenu.add(IA2_panel);
        contenu.add(Box.createRigidArea(new Dimension(largeur, hauteur / 30)));

        boutons_principaux_panel.add(bRetour);
        boutons_principaux_panel.add(bCommencer);

        /* Selection par défaut des boutons radio*/
        joueur_joueur.setSelected(true);
        facile.setSelected(true);
        facile2.setSelected(true);
        IA_panel.setVisible(false);
        IA2_panel.setVisible(false);
        IA_texte.setVisible(false);
        IA2_texte.setVisible(false);

        /* Adding */
        add(Box.createRigidArea(new Dimension(largeur, hauteur / 15)));
        add(titre);
        add(Box.createRigidArea(new Dimension(largeur, hauteur / 30)));
        add(contenu);
        add(Box.createRigidArea(new Dimension(largeur, hauteur / 30)));
        add(boutons_principaux_panel);
        add(Box.createRigidArea(new Dimension(largeur, hauteur / 30)));

    }

    public void actionBoutonRetourMenu(ActionEvent e) {
        Fenetre f2 = (Fenetre) SwingUtilities.getWindowAncestor(this);
        f2.getCardLayout().show(f2.mainPanel, "menu");
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //Chargement de l"image de fond
        try {
            BufferedImage img_colonnes = ImageIO.read(new File("src/Ressources/artwork/columns.png"));
            g2d.drawImage(
                    img_colonnes,
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    this
            );
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur image de fond: " + e.getMessage());
        }
    }

    private class OptionPanel extends JPanel {
        public OptionPanel() {
            super();
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            try {
                BufferedImage bg_panel = ImageIO.read(new File("src/Ressources/artwork/bg_choix_jeu.png"));
                g2d.drawImage(
                        bg_panel,
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        this
                );
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Erreur image de fond: " + e.getMessage());
            }
        }
    }

    public Dimension conserverRatio(Dimension imgSize, Dimension boundary) {
        int largeur_original = imgSize.width;
        int hauteur_original = imgSize.height;
        int largeur_max = boundary.width;
        int hauteur_max = boundary.height;
        int nouvelle_largeur = largeur_original;
        int nouvelle_hauteur = hauteur_original;

        // Vérifie si on doit redimensionner la largeur
        if (largeur_original > largeur_max) {
            // redimensionne la largeur
            nouvelle_largeur = largeur_max;
            // redimensionne la hauteur pour preserver le ratio
            nouvelle_hauteur = (nouvelle_largeur * hauteur_original) / largeur_original;
        }
        // Verifie si on doit redimensionner la hauteur
        if (nouvelle_hauteur > hauteur_max) {
            // redimensionne la hauteur
            nouvelle_hauteur = hauteur_max;
            // redimensionne la largeur pour preserver le ratio
            nouvelle_largeur = (nouvelle_hauteur * largeur_original) / hauteur_original;
        }

        return new Dimension(nouvelle_largeur, nouvelle_hauteur);
    }
}