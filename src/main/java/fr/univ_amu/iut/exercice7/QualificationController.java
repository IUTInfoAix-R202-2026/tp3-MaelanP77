package fr.univ_amu.iut.exercice7;

import java.time.LocalTime;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

/**
 * Contrôleur de la pierre angulaire MVC (parcours P3 - vérification d'une nuit de capture par
 * échantillonnage).
 *
 * <p>L'instance possède son propre modèle ({@link NuitVerification}). Le FXML s'occupe de la
 * structure, le contrôleur du câblage modèle ↔ vue.
 */
public class QualificationController {

  @FXML private TableView<Sequence> tableView;

  @FXML private TableColumn<Sequence, LocalTime> colHorodatage;

  @FXML private TableColumn<Sequence, Number> colFrequence;

  @FXML private TableColumn<Sequence, Number> colDuree;

  @FXML private TableColumn<Sequence, String> colStatut;

  @FXML private Label labelSelection;

  @FXML private Button boutonEcouter;

  @FXML private Label labelLecture;

  @FXML private ChoiceBox<String> choiceBoxVerdict;

  @FXML private TextArea zoneCommentaire;

  @FXML private Label labelVerdictGlobal;

  private final NuitVerification nuit = NuitVerification.genererJeu(10);

  /**
   * Méthode appelée automatiquement après injection des champs {@code @FXML}. Tout le câblage MVC
   * se passe ici.
   */
  @FXML
  private void initialize() {
    // TODO exercice 7 (étape 1) : alimenter la TableView avec les séquences de la
    // nuit, et
    // associer chaque colonne à la propriété du modèle correspondante via
    // setCellValueFactory.
    colHorodatage.setCellValueFactory(c -> c.getValue().horodatageProperty());
    colFrequence.setCellValueFactory(c -> c.getValue().frequenceDominanteKHzProperty());
    colDuree.setCellValueFactory(c -> c.getValue().dureeSecondesProperty());
    colStatut.setCellValueFactory(c -> c.getValue().statutProperty());
    // Puis : lier la TableView à la liste observable du modèle.
    tableView.setItems(nuit.getSequences());

    // TODO exercice 7 (étape 2) : afficher dans labelSelection la séquence
    // sélectionnée.
    // - sans sélection : "(sélectionnez une séquence dans le tableau)"
    // - avec sélection : "Séquence <horodatage> - <freq> kHz" (1 décimale, ex.
    // "Séquence 21:30
    // - 45.2 kHz"). Utiliser String.format("%.1f kHz", ...).
    // Astuce : addListener((obs, ancien, nouveau) -> ...) sur
    // tableView.getSelectionModel().selectedItemProperty().
    labelSelection.setText("(sélectionnez une séquence dans le tableau)");
    labelLecture.setText("");
    boutonEcouter
        .disableProperty()
        .bind(tableView.getSelectionModel().selectedItemProperty().isNull());

    tableView
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, ancien, nouveau) -> {
              if (nouveau != null) {
                String horodatage = nouveau.getHorodatage().toString();
                double freq = nouveau.getFrequenceDominanteKHz();
                labelSelection.setText(String.format("Séquence %s - %.1f kHz", horodatage, freq));
              } else {
                labelSelection.setText("(sélectionnez une séquence dans le tableau)");
              }
            });

    // peupler la ChoiceBox avec les verdicts possibles.
    choiceBoxVerdict.getItems().setAll("OK", "Douteux", "À jeter");

    // labelVerdictGlobal reflète le verdict du modèle.
    labelVerdictGlobal
        .textProperty()
        .bind(
            Bindings.when(nuit.verdictGlobalProperty().isEmpty())
                .then("Verdict global : (à saisir)")
                .otherwise(Bindings.concat("Verdict global : ", nuit.verdictGlobalProperty())));

    // lier la TextArea de commentaire au modèle par binding bidirectionnel.
    zoneCommentaire.textProperty().bindBidirectional(nuit.commentaireProperty());
  }

  /** Action du bouton « Écouter ». Lecture audio simulée : statut → "Écoutée" + label éphémère. */
  @FXML
  private void ecouter() {
    Sequence selection = tableView.getSelectionModel().getSelectedItem();
    selection.setStatut("Écoutée");
    labelLecture.setText("Lecture en cours...");
    PauseTransition pause = new PauseTransition(Duration.millis(600));
    pause.setOnFinished(event -> labelLecture.setText(""));
    pause.play();
  }

  /** Action du bouton « Enregistrer le verdict ». Écrit le verdict choisi dans le modèle. */
  @FXML
  private void enregistrerVerdict() {
    String verdict = choiceBoxVerdict.getValue();
    if (verdict != null && !verdict.isEmpty()) {
      nuit.setVerdictGlobal(verdict);
    }
  }

  /** Exposé pour les tests : permet de vérifier l'état du modèle après actions sur la vue. */
  public NuitVerification getNuit() {
    return nuit;
  }
}
