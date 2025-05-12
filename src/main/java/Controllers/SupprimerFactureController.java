package Controllers;

import entities.Facture;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.ServiceFacture;

public class SupprimerFactureController {

    @FXML
    private TextField txtIdFacture;

    @FXML
    private Label lblReservationId;

    @FXML
    private Label lblMontantTotal;

    @FXML
    private Label lblDateEmission;

    @FXML
    private VBox vboxDetails;

    @FXML
    private Label lblMessage;

    @FXML
    private Button btnSupprimer;

    @FXML
    void rechercherFacture() {
        String idStr = txtIdFacture.getText();

        if (idStr.isEmpty()) {
            showAlert("Veuillez saisir l'ID de la facture !");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);

            ServiceFacture serviceFacture = new ServiceFacture();
            Facture facture = serviceFacture.getById(id);  // <--- C'est bon comme ça

            if (facture != null) {
                lblReservationId.setText(String.valueOf(facture.getReservationId()));
                lblMontantTotal.setText(facture.getMontantTotal() + " €");
                lblDateEmission.setText(facture.getDateEmission());

                vboxDetails.setVisible(true);
                btnSupprimer.setVisible(true);
                lblMessage.setVisible(false);
            } else {
                vboxDetails.setVisible(false);
                btnSupprimer.setVisible(false);
                lblMessage.setText("Aucune facture trouvée avec cet ID.");
                lblMessage.setVisible(true);
            }

        } catch (NumberFormatException e) {
            showAlert("L'ID doit être un nombre entier !");
        }
    }


    @FXML
    void supprimerFacture() {
        String idStr = txtIdFacture.getText();

        if (idStr.isEmpty()) {
            showAlert("Veuillez saisir l'ID de la facture !");
            return;
        }

        int id = Integer.parseInt(idStr);

        // APPEL REEL DU SERVICE
        ServiceFacture serviceFacture = new ServiceFacture();
        boolean success = serviceFacture.supprimerFacture(id);

        if (success) {
            showAlert("Facture supprimée avec succès !");
            txtIdFacture.clear();
            vboxDetails.setVisible(false);
            btnSupprimer.setVisible(false);
            lblMessage.setVisible(false);
        } else {
            showAlert("Échec de la suppression : facture introuvable ou erreur.");
        }
    }


    @FXML
    void handleBackToMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/MenuFacture.fxml")); // Remplace par ton vrai chemin FXML
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Menu principal");
            stage.show();

            // Fermer la fenêtre actuelle
            txtIdFacture.getScene().getWindow().hide();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du retour au menu.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
