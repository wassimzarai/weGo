package gui;

import client.ReservationAutomatiqueClient;
import client.ReservationAutomatiqueClient.ApiResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.animation.FadeTransition;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;

public class ReservationAutomatiqueController {

    @FXML private TextField passagerIdField;
    @FXML private Spinner<Integer> nombrePlacesSpinner;
    @FXML private CheckBox utiliserItineraireHabituelCheckbox;
    @FXML private Button btnConfirmer;
    @FXML private Button btnAnnuler;
    @FXML private VBox messageBox;
    @FXML private Label messageLabel;
    @FXML private Button closeMessageButton;
    @FXML private ProgressIndicator progressIndicator;

    private ListeReservationsController listeController;

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        nombrePlacesSpinner.setValueFactory(valueFactory);

        utiliserItineraireHabituelCheckbox.setSelected(true);

        if (messageBox != null) {
            messageBox.setVisible(false);
            messageBox.setManaged(false);
        }
        if (closeMessageButton != null) {
            closeMessageButton.setVisible(true);
            closeMessageButton.setManaged(true);
        }
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
            progressIndicator.setManaged(false);
        }
    }

    public void setListeController(ListeReservationsController controller) {
        this.listeController = controller;
    }

    @FXML
    void confirmer() {
        cacherMessage();

        // Vérification de l'ID passager
        int passagerId;
        try {
            passagerId = Integer.parseInt(passagerIdField.getText());
        } catch (NumberFormatException e) {
            afficherMessage("L'ID du passager doit être un nombre entier.", true);
            afficherPopup("L'ID du passager doit être un nombre entier.", true);
            return;
        }

        boolean confirmation = demanderConfirmation(
                "Confirmer la réservation automatique",
                "Êtes-vous sûr de vouloir créer une réservation automatique ?",
                "Le système cherchera le prochain trajet disponible correspondant à vos critères."
        );

        if (!confirmation) {
            return;
        }

        setControlsEnabled(false);
        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
            progressIndicator.setManaged(true);
        }

        new Thread(() -> {
            try {
                final ApiResponse reponse = ReservationAutomatiqueClient.reserverAutomatiquement(
                        passagerId,
                        utiliserItineraireHabituelCheckbox.isSelected(),
                        null,  // villeDepart non utilisée
                        null,  // villeArrivee non utilisée
                        nombrePlacesSpinner.getValue()
                );

                Platform.runLater(() -> {
                    setControlsEnabled(true);
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                        progressIndicator.setManaged(false);
                    }
                    afficherMessage(reponse.getMessage(), !reponse.isSuccess());
                    afficherPopup(reponse.getMessage(), !reponse.isSuccess());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setControlsEnabled(true);
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                        progressIndicator.setManaged(false);
                    }
                    afficherMessage("Une erreur est survenue lors de la communication avec le serveur: " + e.getMessage(), true);
                    afficherPopup("Une erreur est survenue lors de la communication avec le serveur: " + e.getMessage(), true);
                    e.printStackTrace();
                });
            }
        }).start();
    }

    private void setControlsEnabled(boolean enabled) {
        passagerIdField.setDisable(!enabled);
        utiliserItineraireHabituelCheckbox.setDisable(!enabled);
        nombrePlacesSpinner.setDisable(!enabled);
        btnConfirmer.setDisable(!enabled);
        btnAnnuler.setDisable(!enabled);
    }

    private void afficherMessage(String message, boolean isError) {
        if (messageBox != null && messageLabel != null) {
            messageBox.getStyleClass().removeAll("notification-success", "notification-error", "notification-info");
            messageBox.getStyleClass().add(isError ? "notification-error" : "notification-success");

            messageLabel.setText(message);

            messageBox.setOpacity(0);
            messageBox.setVisible(true);
            messageBox.setManaged(true);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), messageBox);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            // Ajout d'une disparition automatique après 3 secondes
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), messageBox);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setDelay(Duration.seconds(3));
            fadeOut.setOnFinished(e -> cacherMessage());
            fadeOut.play();
        }
    }

    @FXML
    public void cacherMessage() {
        if (messageBox != null) {
            messageBox.setVisible(false);
            messageBox.setManaged(false);
        }
    }

    @FXML
    void annuler() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private boolean demanderConfirmation(String titre, String entete, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titre);
        alert.setHeaderText(entete);
        alert.setContentText(message);

        Optional<ButtonType> resultat = alert.showAndWait();
        return resultat.isPresent() && resultat.get() == ButtonType.OK;
    }

    private void afficherPopup(String message, boolean isError) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information réservation");
        alert.setHeaderText(null);
        if (message.contains("erreur") || message.contains("Erreur") || isError) {
            alert.setContentText("Nous vous annonçons qu'aucune réservation n'a pu être trouvée pour vos critères ou qu'une erreur est survenue.\n\nDétail : " + message);
        } else {
            alert.setContentText("Nous vous annonçons que votre réservation a été acceptée !\n\nDétail : " + message);
        }
        alert.showAndWait();
    }
}
