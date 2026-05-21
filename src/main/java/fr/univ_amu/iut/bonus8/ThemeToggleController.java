package fr.univ_amu.iut.bonus8;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Region;

/**
 * Contrôleur du bonus 8 - bascule de thème.
 *
 * <p>Le clic sur le {@link ToggleButton} remplace la feuille CSS courante de la {@link
 * javafx.scene.Scene} : {@code theme-clair.css} si non sélectionné, {@code theme-sombre.css} sinon.
 */
public class ThemeToggleController {

  static final String CSS_CLAIR = "theme-clair.css";
  static final String CSS_SOMBRE = "theme-sombre.css";

  @FXML private ToggleButton boutonTheme;

  @FXML private Region racine;

  @FXML
  private void initialize() {
    // TODO bonus 8 : à chaque changement de sélection du ToggleButton, remplacer la
    // feuille
    // CSS active sur la Scene par theme-clair.css (non sélectionné) ou
    // theme-sombre.css
    // (sélectionné). On localise les URL via
    // getClass().getResource("nom.css").toExternalForm().
    // Astuce : utiliser scene.getStylesheets().setAll(url) pour remplacer toutes
    // les feuilles
    // en une seule opération.
    // Charger le thème clair par défaut sur la Scene actuelle.
    if (racine.getScene() == null) {
      racine
          .sceneProperty()
          .addListener(
              (obs, oldScene, newScene) -> {
                if (newScene != null) {
                  newScene
                      .getStylesheets()
                      .setAll(getClass().getResource(CSS_CLAIR).toExternalForm());
                }
              });
    } else {
      racine.getScene().getStylesheets().setAll(getClass().getResource(CSS_CLAIR).toExternalForm());
    }

    boutonTheme
        .selectedProperty()
        .addListener(
            (obs, oldValue, selected) -> {
              String css = selected ? CSS_SOMBRE : CSS_CLAIR;
              boutonTheme
                  .getScene()
                  .getStylesheets()
                  .setAll(getClass().getResource(css).toExternalForm());
              boutonTheme.setText(selected ? "Mode clair" : "Mode sombre");
            });
  }
}
