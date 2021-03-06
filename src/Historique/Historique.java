package Historique;

import Modele.Jeu;
import Utile.ConfigurationPartie;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

import static Utile.Constante.*;

/**
 * Classe composé de 2 piles de commande, l'une servant à stocker les commandes effectuées,
 * l'autre à stocker les commandes annulées.
 * Elle permet aussi de sauvegarder une partie ainsi que d'en charger une.
 */
public class Historique {
    private Stack<Commande> passe;
    private Stack<Commande> futur;
    private Jeu jeu;

    /**
     * Constructeur de l'historique.
     * @param jeu objet sur lequel les commandes sont effectuées
     */
    public Historique(Jeu jeu) {
        passe = new Stack<>();
        futur = new Stack<>();
        this.jeu = jeu;
    }

    /**
     * Stockage d'une commande.
     * Efface entièrement le contenu des commandes annulées.
     * @param cmd commande à stocker
     */
    public void stocker(Commande cmd) {
        if (cmd == null) return;
        passe.push(cmd);
        futur = new Stack<>();
    }

    public boolean peutAnnuler() {
        return !passe.isEmpty();
    }

    public boolean peutRefaire() {
        return !futur.isEmpty();
    }

    /**
     * Annulation de la dernière commande effectuée.
     */
    public void annuler() {
        Commande cmd = passe.pop();
        cmd.desexecute(jeu);
        futur.push(cmd);
    }

    /**
     * Exécution de la dernière commande annulée.
     */
    public void refaire() {
        Commande cmd = futur.pop();
        cmd.execute(jeu);
        passe.push(cmd);
    }

    /**
     * Sauvegarde l'avancement du jeu dans un fichier *.sav.
     * @return le nom du fichier créé
     */
    public String sauvegarder() {
        try {
            String passeStr = passe.toString();
            passeStr = passeStr.substring(1, passeStr.length() - 1);

            ArrayList<Commande> futurArray = new ArrayList<>(futur);
            Collections.reverse(futurArray);
            String futurStr = futurArray.toString();
            futurStr = futurStr.substring(1, futurStr.length() - 1);

            int ia1_mode = jeu.getConfigurationPartie().getIaMode1();
            int ia2_mode = jeu.getConfigurationPartie().getIaMode2();
            int index_start = jeu.getConfigurationPartie().getIndexJoueurCommence();
            boolean j1_blue = jeu.getConfigurationPartie().isJoueur1Bleu();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String prefix = "p_vs_p";
            if (ia1_mode > 0) {
                prefix = ia2_mode > 0 ? "ia_vs_ia" : "p_vs_ia";
            }
            String nom_fichier = prefix + "_" + formatter.format(new Date()) + ".sav";
            FileWriter fichier = new FileWriter(SAVES_PATH + nom_fichier);
            fichier.write(ia1_mode + " " + ia2_mode + " " + index_start + " " + j1_blue + "\n");
            fichier.write(passeStr + ", " + futurStr + "\n" + futur.size());
            fichier.close();
            return nom_fichier;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Chargement d'une partie à partir d'un fichier.
     * @param lecteur
     */
    public void charger(Scanner lecteur) {
        String[] points = lecteur.nextLine().split(", ");

        for (String point : points) {
            String[] coord = point.split(" ");
            jeu.jouer(new Point(Integer.parseInt(coord[0]), Integer.parseInt(coord[1])));
        }

        int nbAnnuler = Integer.parseInt(lecteur.nextLine());
        for (int i = 0; i < nbAnnuler; i++) annuler();

        lecteur.close();
    }

    public void charger(String filename) {
        try {
            File fichier = new File(SAVES_PATH + filename);
            Scanner lecteur = new Scanner(fichier);

            String[] param = lecteur.nextLine().split(" ");
            int ia1_mode = Integer.parseInt(param[0]);
            int ia2_mode = Integer.parseInt(param[1]);
            int index_start = Integer.parseInt(param[2]);
            boolean j1_blue = Boolean.parseBoolean(param[3]);

            ConfigurationPartie config = new ConfigurationPartie(ia1_mode, ia2_mode);
            config.setIndexJoueurCommence(index_start);
            config.setJoueur1Bleu(j1_blue);

            jeu.setConfigurationPartie(config);
            jeu.RAZ();

            charger(lecteur);

        } catch (FileNotFoundException ex) {
            System.err.println("Le fichier " + filename + " n'existe pas");
            ex.printStackTrace();
        } catch (Exception ex) {
            System.err.println("Le fichier n'a pas le bon format");
            ex.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Historique)) return false;

        Historique h = (Historique) o;

        return passe.equals(h.passe) && futur.equals(h.futur);
    }
}
