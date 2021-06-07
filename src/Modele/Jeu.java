package Modele;

import Patterns.Observateur;
import Reseau.Reseau;
import Vue.LecteurSon;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

import static Modele.Constante.*;

/**
 * Classe permettant de gérer tout le processus d'une partie en éditant les valeurs du plateau.
 *
 * @see Plateau
 */
public class Jeu {
    private final LecteurSon construction_son;

    private Plateau plateau;
    private int situation, nombre_batisseurs, vitesse_ia, nb_tours,i_joueurs;
    private boolean ia_statut,in_simulation, jeu_fini;
    private Point batisseur_en_cours;
    private Observateur observateur;
    private Historique histo;
    private Commande cmd;
    private Joueur gagnant;
    private Joueur[] joueurs;
    Reseau netUser;

    ConfigurationPartie configurationPartie;

    /**
     * Instantie une classe jeu.
     *
     * @param o observateur
     */
    public Jeu(Observateur o, ConfigurationPartie config) {
        joueurs = new Joueur[2];
        plateau = new Plateau();
        jeu_fini = false;
        ia_statut = true;
        batisseur_en_cours = null;
        gagnant = null;

        vitesse_ia = 1;
        configurationPartie = config;
        situation = PLACEMENT;
        nombre_batisseurs = nb_tours = 0;
        construction_son = new LecteurSon("boulder_drop.wav");

        observateur = o;
        histo = new Historique(this);

        i_joueurs = config.getIndexJoueurCommence();

        if (config.getIaMode2() != 0) {
            joueurs[0] = new JoueurIA(this, JOUEUR1, setIA(config.getIaMode1()), vitesse_ia);
            joueurs[1] = new JoueurIA(this, JOUEUR2, setIA(config.getIaMode2()), vitesse_ia);
        } else if (config.getIaMode1() != 0) {
            joueurs[0] = new JoueurHumain(this, JOUEUR1);
            joueurs[1] = new JoueurIA(this, JOUEUR2, setIA(config.getIaMode1()), vitesse_ia);
        } else {
            joueurs[0] = new JoueurHumain(this, JOUEUR1);
            joueurs[1] = new JoueurHumain(this, JOUEUR2);
        }
        iaJoue();
    }

    public Jeu(Observateur o) {
        this(o, new ConfigurationPartie(0, 0));
    }

    public void setSimulation(boolean statut) {
        in_simulation = statut;
    }

    private IA setIA(int ia_mode) {
        switch (ia_mode) {
            case 1 -> {
                return new IAFacile(this);
            }
            case 2 -> {
                return new IANormale(this);
            }
            case 3 -> {
                return new IADifficile(this);
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Effectue des actions selon la situation du jeu parmi placement des batisseurs, selection de batisseur, déplacement des batisseurs et construction des bâtiments.
     */
    public void jouer(Point position) {
        cmd = null;
        situation = plateau.estBatisseur(position, getJoueurEnCours()) && situation == DEPLACEMENT ? SELECTION : situation;
        switch (situation) {
            case PLACEMENT -> jouePlacement(position);
            case SELECTION -> joueSelection(position);
            case DEPLACEMENT -> joueDeplacement(position);
            case CONSTRUCTION -> joueConstruction(position);
            default -> System.err.println("Unknown situation");
        }
        histo.stocker(cmd);
    }

    public void jouePlacement(Point position) {
        if (plateau.estLibre(position)) {
            cmd = new CoupDeplacer(joueurs[i_joueurs], null, position);
            plateau.ajouterJoueur(position, joueurs[i_joueurs]);
            if(netUser != null) netUser.envoieCoup(position);
            getJoueurEnCours().addBatisseur(position);
            nombre_batisseurs++;
            verificationNbBatisseur();
        }
    }

    public void setBatisseursJoueur(ArrayList<Point> batisseursJoueur) {
        getJoueurEnCours().batisseurs = batisseursJoueur;
    }



    public void joueSelection(Point position) {
        batisseur_en_cours = choisirBatisseur(position);
        situation = batisseur_en_cours == null ? SELECTION : DEPLACEMENT;
        sendMove(position);
    }

    public void joueDeplacement(Point position) {
        Point prevPos = batisseur_en_cours;
        if (avancer(position, batisseur_en_cours)) {
            ArrayList<Point> batisseurs_en_cours = getJoueurEnCours().getBatisseurs();
            batisseurs_en_cours.set(batisseurs_en_cours.indexOf(prevPos), position);

            cmd = new CoupDeplacer(joueurs[i_joueurs], prevPos, batisseur_en_cours);
            situation = CONSTRUCTION;
            sendMove(position);
        }
        victoireJoueur();
    }

    public void joueConstruction(Point position) {
        if (!jeu_fini && construire(position, batisseur_en_cours)) {
            if(!in_simulation) construction_son.joueSon(false);
            cmd = new CoupConstruire(joueurs[i_joueurs], position, batisseur_en_cours);
            finTour();
            situation = SELECTION;
            sendMove(position);
        }
    }

    public void iaJoue() {
        if (getJoueurEnCours().getClass() == JoueurIA.class) {
            ((JoueurIA) getJoueurEnCours()).timerIaSet(ia_statut);
        }
    }

    public void verificationNbBatisseur() {
//        if (nombre_batisseurs == 4) {
//            plateau.cases = new int[][]{
//                    {8, 2, 17, 1, 0},
//                    {17, 2, 9, 0, 0},
//                    {0, 3, 0, 0, 0},
//                    {0, 0, 0, 0, 0},
//                    {0, 0, 0, 0, 0}};
////            plateau.cases = new int[][]{
////                    {8, 1, 16, 0, 0},
////                    {0, 0, 2, 1, 1},
////                    {0, 0, 3, 18, 9},
////                    {0, 0, 0, 4, 3},
////                    {0, 0, 0, 0, 0}};
//        }
        if (nombre_batisseurs % 2 == 0) {
            finTour();
        }
        if (nombre_batisseurs >= 4) {
            situation = SELECTION;
        }
    }

    /**
     * Choisi le batisseur à la position (l, c).
     *
     * @return le batisseur du joueur s'il existe à cette position
     */
    private Point choisirBatisseur(Point position) {
        return plateau.estBatisseur(position, joueurs[i_joueurs]) ? position : null;
    }

    /**
     * Vérifie que la case (l, c) est atteignable sur la grille (selon la situation)
     *
     * @return vrai s'il on peut atteindre la case
     */
    public boolean estAtteignable(Point position) {
        if (situation == DEPLACEMENT)
            return (batisseur_en_cours != null) && plateau.deplacementPossible(position, batisseur_en_cours);
        else if (situation == CONSTRUCTION)
            return (batisseur_en_cours != null) && plateau.peutConstruire(position, batisseur_en_cours);
        else if (situation == SELECTION) return plateau.estBatisseur(position, joueurs[i_joueurs]);
        else return nombre_batisseurs < 4;
    }

    public void changerJoueur() {
        i_joueurs = (i_joueurs + 1) % joueurs.length;
    }

    public void MAJObservateur() {
        if(in_simulation) return;
        observateur.miseAjour();
    }

    /**
     * Fini le tour pour le joueur en cours.
     */
    public void finTour() {
        nb_tours++;
        changerJoueur();
        batisseur_en_cours = null;
        MAJObservateur();
        if (checkPerdu()) {
            return;
        }
        iaJoue();
    }

    private boolean checkPerdu() {
        ArrayList<Point> batisseur_joueur = getJoueurEnCours().getBatisseurs();
        if(batisseur_joueur.size() < 2) return false;
        if (plateau.getCasesAccessibles(batisseur_joueur.get(0)).isEmpty() &&
                plateau.getCasesAccessibles(batisseur_joueur.get(1)).isEmpty()
        ) {
            gagnant = joueurs[(i_joueurs + 1) % joueurs.length];
            jeu_fini = true;
            observateur.miseAjour();
            return true;
        }
        return false;
    }

    /**
     * Déplace le batisseur vers les coordonnées l et c si c'est possible.
     *
     * @param batisseur position (x;y) d'un batisseur
     * @return vrai s'il a été possible de se déplacer
     */
    private boolean avancer(Point position, Point batisseur) {
        if (plateau.deplacementPossible(position, batisseur)) {
            plateau.enleverJoueur(batisseur); // enlève le batisseurs de la case du point batisseurs
            plateau.ajouterJoueur(position, joueurs[i_joueurs]); // ajoute un batisseurs à la case en position l,c
            batisseur_en_cours = position;
            return true;
        }
        return false;
    }

    /**
     * Construit aux coordonnées l et c une hauteur de bâtiment si c'est possible.
     *
     * @param batisseur position (x;y) d'un batisseur
     * @return vrai si la construction a bien eu lieu
     * @see Plateau#peutConstruire(Point, Point batisseur)
     * @see Plateau#ameliorerBatiment(Point)
     */
    private boolean construire(Point position, Point batisseur) {
        if (plateau.peutConstruire(position, batisseur)) {
            plateau.ameliorerBatiment(position);
            return true;
        }
        return false;
    }

    /**
     * Vérifie si le batisseur du joueur en cours est sur un bâtiment d'hauteur 3.
     * Si c'est le cas, le jeu s'arrête et l'observateur est notifié de la victoire.
     */
    public void victoireJoueur() {
        if (batisseur_en_cours != null && plateau.getTypeBatiments(batisseur_en_cours) == TOIT) {
            gagnant = getJoueurEnCours();
            jeu_fini = true;
            observateur.miseAjour();
        }
    }

    private void sendMove(Point position) {
        if(netUser != null && !netUser.estEnModificationsLocal()) netUser.envoieCoup(position);
    }

    public void accelererIA(double index_acceleration) {
        vitesse_ia = (int) index_acceleration;
        if (joueurs[0].getClass() == JoueurIA.class) {
            ((JoueurIA) joueurs[0]).setVitesseIA(vitesse_ia);
        }
        if (joueurs[1].getClass() == JoueurIA.class) {
            ((JoueurIA) joueurs[1]).setVitesseIA(vitesse_ia);
        }
    }

    public String sauvegarder() {
        return histo.sauvegarder();
    }

    public void charger(String filename) {
        RAZ();
        histo.charger(filename);
    }

    public void RAZ() {
        plateau.RAZ();
        situation = PLACEMENT;
        batisseur_en_cours = null;
        nombre_batisseurs = i_joueurs = 0;
    }

    public boolean annuler() {
        if (histo.peutAnnuler()) {
            histo.annuler();
            return true;
        } else {
            return false;
        }
    }

    public boolean refaire() {
        if (histo.peutRefaire()) {
            histo.refaire();
            return true;
        } else {
            return false;
        }
    }

    public boolean estJeufini() {
        return jeu_fini;
    }

    public Plateau getPlateau() {
        return plateau;
    }

    public Point getBatisseurEnCours() {
        return batisseur_en_cours;
    }
    public int getSituation() {
        return situation;
    }

    public Joueur getJoueurEnCours() {
        return joueurs[i_joueurs];
    }

    public void updateBatisseur(int index_batisseur, Point newPos, int numJoueur) {
        if (numJoueur == joueurs[0].getNum_joueur()) {
            joueurs[0].getBatisseurs().set(index_batisseur, newPos);
        } else if (numJoueur == joueurs[1].getNum_joueur()) {
            joueurs[1].getBatisseurs().set(index_batisseur, newPos);
        }
    }

    public int getNombreBatisseurs() {
        return nombre_batisseurs;
    }

    public void setSituation(int situation) {
        this.situation = situation;
    }

    public void setBatisseurEnCours(Point batisseur) {
        this.batisseur_en_cours = batisseur;
    }

    public void setNombreBatisseurs(int nombre_batisseurs) {
        this.nombre_batisseurs = nombre_batisseurs;
    }

    public void setJeuFini(boolean value) {
        jeu_fini = value;
    }

    public void iaSwitch() {
        this.ia_statut = !ia_statut;
        if (getJoueurEnCours().getClass() == JoueurIA.class) {
            ((JoueurIA) getJoueurEnCours()).timerIaSet(ia_statut);
        }
    }

    public Joueur getGagnant() {
        return gagnant;
    }

    public ArrayList<Point> getBatisseurs() {
        return getJoueurEnCours().getBatisseurs();
    }

    public ArrayList<Point> getBatisseursJoueur(int joueur) {
        if(joueur == JOUEUR1){
            return getJ1().getBatisseurs();
        }
        else {
            return getJ2().getBatisseurs();
        }
    }

    public boolean getIaStatut() {
        return ia_statut;
    }

    public Historique getHistorique() {
        return histo;
    }

    public Joueur getJ1() {
        return joueurs[0];
    }

    public Joueur getJ2() {
        return joueurs[1];
    }

    public int getNbTours() {
        return nb_tours;
    }

    public void setNetUser(Reseau netUser) {
        this.netUser = netUser;
        netUser.setJeu(this);
    }

    public ConfigurationPartie getConfigurationPartie() {
        return configurationPartie;
    }

    public Reseau getNetUser() {
        return netUser;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Jeu)) return false;

        Jeu j = (Jeu) o;

        return getJoueurEnCours().equals(j.getJoueurEnCours()) &&
                situation == j.situation &&
                nombre_batisseurs == j.nombre_batisseurs &&
                Objects.equals(batisseur_en_cours, j.batisseur_en_cours) &&
                plateau.equals(j.plateau);
    }

    public static int getAutreJoueur(int joueur){
        return (joueur == JOUEUR2 ? JOUEUR1: JOUEUR2);
    }
}