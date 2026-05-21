package fr.univ_amu.iut.bonus10;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

/**
 * Bonus 10 - étape 3 : plateau de jeu complet (modèle + logique).
 *
 * <p>L'othellier est un composant Java auto-suffisant : il étend {@link GridPane} et instancie les
 * 64 {@link Case} dans son constructeur. Toute la logique du jeu (validité d'un coup, capture dans
 * les huit directions, fin de partie) est encapsulée ici, ce qui en fait un beau cas d'usage MVC :
 * la vue FXML n'a qu'à inclure cet othellier, le contrôleur n'aura qu'à câbler quelques bindings
 * sur les propriétés exposées (étape 4).
 *
 * <p><b>Méthodes fournies :</b> le moteur de capture ({@link #casesCapturable(Case)} et {@link
 * #casesCapturable(Case, Point2D)}) ainsi que {@link #estIndicesValides(int, int)} sont livrés tels
 * quels. Ils parcourent les huit directions pour identifier les pions adverses encadrés. Tout le
 * reste (initialisation, démarrage de partie, gestion du tour) s'appuie dessus et est à votre
 * charge.
 */
public class Othellier extends GridPane {

  /** Les huit directions de propagation (horizontales, verticales, diagonales). */
  private static final Point2D[] DIRECTIONS = {
    new Point2D(1, 0),
    new Point2D(1, 1),
    new Point2D(0, 1),
    new Point2D(-1, 1),
    new Point2D(-1, 0),
    new Point2D(-1, -1),
    new Point2D(0, -1),
    new Point2D(1, -1)
  };

  /** Taille du plateau (8x8 dans la version standard du jeu). */
  public static final int TAILLE = 8;

  // TODO bonus 10 étape 3.1 : déclarer les données membres privées suivantes :
  // - cases : une matrice Case[TAILLE][TAILLE] qui représente le plateau de jeu
  // - joueurCourant : un ObjectProperty<Joueur> initialisé à Joueur.NOIR (NOIR
  // commence toujours)
  // - partieTerminee : un BooleanProperty initialisé à false
  private final Case[][] cases = new Case[TAILLE][TAILLE];

  private final ObjectProperty<Joueur> joueurCourant =
      new SimpleObjectProperty<>(this, "joueurCourant", Joueur.NOIR);

  private final BooleanProperty partieTerminee =
      new SimpleBooleanProperty(this, "partieTerminee", false);

  /**
   * Gestionnaire d'événement partagé par toutes les cases du plateau.
   *
   * <p>Une seule instance est réutilisée pour les 64 boutons : c'est le motif courant en JavaFX
   * pour ne pas multiplier inutilement les écouteurs.
   */
  private final EventHandler<ActionEvent> caseListener =
      event -> {
        // TODO bonus 10 étape 3.10 : implémenter ce gestionnaire avec une expression
        // lambda :
        // 1. récupérer la case ayant produit l'événement avec (Case) event.getSource()
        // 2. vérifier que la position choisie est jouable avec estPositionJouable(...)
        // 3. si oui, appeler jouer(...) pour poser le pion, déclencher les captures et
        // passer la
        // main au joueur suivant. Sinon, on ignore le clic (le joueur courant reste le
        // même).
        Case source = (Case) event.getSource();
        if (estPositionJouable(source)) {
          jouer(source);
        }
      };

  /**
   * Construit un othellier neuf : applique les contraintes de la grille, instancie les 64 cases,
   * branche l'écouteur et démarre une nouvelle partie.
   */
  public Othellier() {
    // TODO bonus 10 étape 3.2 : initialiser le composant graphique :
    // 1. fixer setHgap(1) et setVgap(1) pour aérer le plateau
    // 2. (optionnel) appeler setStyle("-fx-background-color: #145830;") pour le
    // fond vert foncé
    // 3. appeler adapterLesLignesEtColonnes() pour fixer les contraintes de la
    // grille
    // 4. appeler remplirOthellier() pour créer les 64 cases et brancher l'écouteur
    // partagé
    // 5. appeler nouvellePartie() pour positionner la configuration de départ
    setHgap(1);
    setVgap(1);
    setStyle("-fx-background-color: #145830;");
    adapterLesLignesEtColonnes();
    remplirOthellier();
    nouvellePartie();
  }

  // -----------------------------------------------------------------
  // Propriétés observables exposées au contrôleur (étape 4)
  // -----------------------------------------------------------------

  /** Propriété observable du joueur dont c'est le tour. */
  public ObjectProperty<Joueur> joueurCourantProperty() {
    return joueurCourant;
  }

  /** Valeur courante du joueur dont c'est le tour. */
  public Joueur getJoueurCourant() {
    return joueurCourant.get();
  }

  /** Propriété observable du drapeau de fin de partie. */
  public BooleanProperty partieTermineeProperty() {
    return partieTerminee;
  }

  /** Accès à la case située à la position {@code (ligne, colonne)}. */
  public Case getCase(int ligne, int colonne) {
    return cases[ligne][colonne];
  }

  // -----------------------------------------------------------------
  // Construction du plateau
  // -----------------------------------------------------------------

  /**
   * Fixe les contraintes des lignes et colonnes pour que la grille soit régulière et extensible.
   */
  private void adapterLesLignesEtColonnes() {
    for (int i = 0; i < TAILLE; i++) {
      ColumnConstraints column = new ColumnConstraints();
      column.setHgrow(Priority.ALWAYS);
      column.setPercentWidth(100.0 / TAILLE);
      getColumnConstraints().add(column);

      RowConstraints row = new RowConstraints();
      row.setVgrow(Priority.ALWAYS);
      row.setPercentHeight(100.0 / TAILLE);
      getRowConstraints().add(row);
    }
  }

  /** Instancie les 64 cases, leur branche l'écouteur partagé et les ajoute à la grille. */
  private void remplirOthellier() {
    for (int ligne = 0; ligne < TAILLE; ligne++) {
      for (int colonne = 0; colonne < TAILLE; colonne++) {
        Case c = new Case(ligne, colonne);
        c.setOnAction(caseListener);
        cases[ligne][colonne] = c;
        add(c, colonne, ligne);
      }
    }
  }

  /**
   * Configuration de départ classique : deux pions noirs en (m-1, m) et (m, m-1) et deux pions
   * blancs en (m-1, m-1) et (m, m) où m = TAILLE / 2.
   */
  private void positionnerPionsDebutPartie() {
    // TODO bonus 10 étape 3.4 : placer les quatre pions du début de partie sur le
    // plateau.
    // Indice : utiliser la méthode placer(Case, Joueur) (à écrire à l'étape 3.7)
    // qui pose le pion
    // ET incrémente le score du joueur correspondant. Avec m = TAILLE / 2, on place
    // :

    int m = TAILLE / 2;
    placer(cases[m - 1][m - 1], Joueur.BLANC);
    placer(cases[m - 1][m], Joueur.NOIR);
    placer(cases[m][m - 1], Joueur.NOIR);
    placer(cases[m][m], Joueur.BLANC);
  }

  /**
   * Démarre une nouvelle partie : vide le plateau, remet les scores à zéro, place les quatre pions
   * du début et redonne la main au joueur NOIR.
   */
  public void nouvellePartie() {
    // TODO bonus 10 étape 3.5 : enchaîner les étapes suivantes :
    // 1. vider() pour effacer toutes les cases
    // 2. Joueur.initialiserScores() pour remettre les scores à zéro
    // 3. positionnerPionsDebutPartie() pour la configuration de départ
    // 4. joueurCourant.set(Joueur.NOIR) car NOIR commence toujours
    // 5. partieTerminee.set(false) pour relancer l'éventuel binding de fin
    vider();
    Joueur.initialiserScores();
    positionnerPionsDebutPartie();
    joueurCourant.set(Joueur.NOIR);
    partieTerminee.set(false);
  }

  /** Vide toutes les cases du plateau (chaque case repasse au joueur {@link Joueur#PERSONNE}). */
  private void vider() {
    for (int i = 0; i < TAILLE; i++) {
      for (int j = 0; j < TAILLE; j++) {
        cases[i][j].setPossesseur(Joueur.PERSONNE);
      }
    }
  }

  // -----------------------------------------------------------------
  // Logique d'un coup (orchestration)
  // -----------------------------------------------------------------

  /** Joue le coup demandé : pose le pion, capture les pions adverses, passe la main. */
  private void jouer(Case caseSelectionnee) {
    // TODO bonus 10 étape 3.7 : orchestrer un coup en trois temps :
    // 1. appeler placer(caseSelectionnee, joueurCourant.get()) pour poser le pion
    // du joueur
    // 2. pour chaque case dans casesCapturable(caseSelectionnee) (méthode fournie
    // plus bas),
    // appeler capturer(case) afin de retourner ce pion adverse
    // 3. appeler tourSuivant() pour passer la main au joueur suivant
    placer(caseSelectionnee, joueurCourant.get());
    for (Case c : casesCapturable(caseSelectionnee)) {
      capturer(c);
    }
    tourSuivant();
  }

  /**
   * Pose un pion du joueur indiqué sur la case (et incrémente son score).
   *
   * <p>Méthode utilitaire utilisée à la fois par {@link #positionnerPionsDebutPartie()} et par
   * {@link #jouer(Case)} pour ne pas dupliquer la logique « pose + score ».
   */
  private void placer(Case c, Joueur joueur) {
    // TODO bonus 10 étape 3.7bis : changer le possesseur de la case
    // (c.setPossesseur(joueur))
    // et incrémenter le score du joueur (joueur.incrementerScore()).
    c.setPossesseur(joueur);
    joueur.incrementerScore();
  }

  /**
   * Retourne un pion : il change de couleur et les scores s'ajustent en miroir (l'ancien
   * propriétaire perd un point, le nouveau en gagne un).
   */
  private void capturer(Case caseCapturee) {
    // TODO bonus 10 étape 3.8 : effectuer la capture d'un pion :
    // 1. récupérer l'ancien propriétaire : Joueur ancien =
    // caseCapturee.getPossesseur()
    // 2. décrémenter son score : ancien.decrementerScore()
    // 3. calculer le nouveau propriétaire : Joueur nouveau = ancien.suivant()
    // 4. mettre à jour la case : caseCapturee.setPossesseur(nouveau)
    // 5. incrémenter le score du nouveau : nouveau.incrementerScore()
    Joueur ancien = caseCapturee.getPossesseur();
    ancien.decrementerScore();
    Joueur nouveau = ancien.suivant();
    caseCapturee.setPossesseur(nouveau);
    nouveau.incrementerScore();
  }

  /**
   * Donne la main au joueur suivant.
   *
   * <p>Cas particulier : si le joueur suivant ne peut pas jouer, on redonne la main au précédent ;
   * si aucun des deux ne peut jouer, la partie est terminée.
   */
  private void tourSuivant() {
    // TODO bonus 10 étape 3.9 : implémenter la rotation des joueurs :
    // 1. calculer le joueur suivant : Joueur prochain =
    // joueurCourant.get().suivant()
    // 2. positionner joueurCourant à prochain
    // 3. si peutJouer() est faux (le prochain ne peut pas jouer) : on tente de
    // redonner la
    // main au précédent en faisant joueurCourant.set(prochain.suivant())
    // 4. si peutJouer() est encore faux : la partie est terminée, faire
    // partieTerminee.set(true)
    Joueur prochain = joueurCourant.get().suivant();
    joueurCourant.set(prochain);
    if (!peutJouer()) {
      // tenter de redonner la main au précédent
      joueurCourant.set(prochain.suivant());
      if (!peutJouer()) {
        partieTerminee.set(true);
      }
    }
  }

  /** Une position est jouable si elle est vide et si elle capture au moins un pion adverse. */
  public boolean estPositionJouable(Case caseSelectionnee) {
    // TODO bonus 10 étape 3.11 : retourner true si la case est vide
    // (caseSelectionnee.getPossesseur() == Joueur.PERSONNE) ET si la liste
    // retournée par
    // casesCapturable(caseSelectionnee) n'est pas vide (au moins un pion à
    // capturer).
    return (caseSelectionnee.getPossesseur() == Joueur.PERSONNE)
        && (!casesCapturable(caseSelectionnee).isEmpty());
  }

  /**
   * Liste des cases sur lesquelles le joueur courant peut jouer (utilisée par {@code peutJouer}).
   */
  public List<Case> casesJouables() {
    // TODO bonus 10 étape 3.12 : parcourir toutes les cases et retourner la liste
    // de celles qui
    // sont jouables par le joueur courant. Indice : utiliser
    // estPositionJouable(...).
    List<Case> jouables = new ArrayList<>();
    for (int i = 0; i < TAILLE; i++) {
      for (int j = 0; j < TAILLE; j++) {
        if (estPositionJouable(cases[i][j])) {
          jouables.add(cases[i][j]);
        }
      }
    }
    return jouables;
  }

  /** Le joueur courant peut-il jouer au moins un coup ? */
  public boolean peutJouer() {
    return !casesJouables().isEmpty();
  }

  // -----------------------------------------------------------------
  // Moteur de capture (FOURNI - aucune ligne à écrire ici)
  //
  // Ces méthodes constituent le coeur algorithmique du jeu : dans une
  // direction donnée, on collecte les pions adverses alignés à partir
  // de la case sélectionnée jusqu'à rencontrer un pion de notre couleur
  // (qui ferme la capture). Si on tombe sur une case vide ou si on sort
  // du plateau avant de fermer, rien n'est capturable dans cette
  // direction.
  // -----------------------------------------------------------------

  /** Cases adverses capturables depuis {@code caseSelectionnee}, agrégées sur les 8 directions. */
  public List<Case> casesCapturable(Case caseSelectionnee) {
    List<Case> resultat = new ArrayList<>();
    for (Point2D direction : DIRECTIONS) {
      resultat.addAll(casesCapturable(caseSelectionnee, direction));
    }
    return resultat;
  }

  /** Cases capturables dans une direction donnée à partir de {@code caseSelectionnee}. */
  private List<Case> casesCapturable(Case caseSelectionnee, Point2D direction) {
    List<Case> casesCapturable = new ArrayList<>();

    int indiceLigne = caseSelectionnee.getLigne() + (int) direction.getY();
    int indiceColonne = caseSelectionnee.getColonne() + (int) direction.getX();

    while (estIndicesValides(indiceLigne, indiceColonne)) {
      Joueur possesseur = cases[indiceLigne][indiceColonne].getPossesseur();
      if (possesseur != joueurCourant.get().suivant()) {
        break;
      }
      casesCapturable.add(cases[indiceLigne][indiceColonne]);
      indiceLigne += direction.getY();
      indiceColonne += direction.getX();
    }

    if (estIndicesValides(indiceLigne, indiceColonne)
        && cases[indiceLigne][indiceColonne].getPossesseur() == joueurCourant.get()) {
      return casesCapturable;
    }
    return new ArrayList<>();
  }

  private boolean estIndicesValides(int indiceLigne, int indiceColonne) {
    return estIndiceValide(indiceLigne) && estIndiceValide(indiceColonne);
  }

  private boolean estIndiceValide(int indice) {
    return indice >= 0 && indice < TAILLE;
  }
}
