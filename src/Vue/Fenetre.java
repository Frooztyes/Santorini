package Vue;

import Listener.EcouteurDeMouvementDeSouris;
import Utile.*;
import Vue.PanelPartie.PanelTutoriel.PanelTutoriel;

import static Utile.Constante.*;

import javax.swing.*;
import java.awt.*;

/**
 * Crée une fenêtre pour le menu principal du jeu.
 * Cette fenêtre permet de démarrer le jeu, de lire les règles, de suivre un tutoriel.
 */
public class Fenetre extends JFrame {

    private final CardLayout pileCarte;
    private final JPanel panelPrincipal;
    private JPanel shown;

    /**
     * Initialise les JPanels ne nécessitant pas d'être créer selon des options.
     */
    public Fenetre() {
        setTitle(NOM_JEU);
        setMinimumSize(DEFAULT_FENETRE_TAILLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        pileCarte = new CardLayout();
        panelPrincipal = new JPanel(pileCarte);

        LecteurSon musique = new LecteurSon("musiqueBGtest.wav");
        PanelMenu menu = new PanelMenu(getSize());
        PanelOptions options = new PanelOptions(getSize());
        PanelRegles regles = new PanelRegles(getSize());
        PanelTutoriel tutoriel = new PanelTutoriel(getSize());
        PanelMultijoueur multi = new PanelMultijoueur();

        panelPrincipal.add(menu, "menu");
        panelPrincipal.add(options, "options");
        panelPrincipal.add(regles, "regles");
        panelPrincipal.add(tutoriel, "tutoriel");
        panelPrincipal.add(multi, "multi");

        add(panelPrincipal);
        pileCarte.show(panelPrincipal, "menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Change le curseur de la fenêtre
        setCursor(EcouteurDeMouvementDeSouris.creerCurseurGenerique("defaut_gris", new Point(0, 0)));
        Utile.chargerFont();

        Image icone = Toolkit.getDefaultToolkit().getImage(CHEMIN_RESSOURCE + "/icone/logo-icone.png");
        setIconImage(icone);

        setVisible(true);

        musique.joueSon(true);
    }

    /**
     * Affiche un JPanel passé en paramètre sur la fenêtre.
     * @param p le JPanel à afficher
     */
    public void setPanel(JPanel p) {
        JPanel tmp_shown = shown;
        shown = p;
        panelPrincipal.add(p);
        pileCarte.show(panelPrincipal, "");
        if(tmp_shown != null) panelPrincipal.remove(tmp_shown);
    }

    /**
     * Affiche un JPanel déjà présent dans le CardLayout.
     * @param name nom du JPanel
     */
    public void displayPanel(String name) {
        pileCarte.show(panelPrincipal, name);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Fenetre::new);
    }

}
