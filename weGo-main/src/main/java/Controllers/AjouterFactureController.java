package Controllers;

import Entites.Facture;
import Entites.Paiement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import services.ServiceFacture;
import services.ServicePaiement;

import java.time.format.DateTimeFormatter;

public class AjouterFactureController {

    @FXML private TextField txtIdFacture;
    @FXML private TextField txtReservationId;
    @FXML private TextField txtIdPaiement;
    @FXML private TextField txtMontantTotal;
    @FXML private TextField txtMontantPaye;
    @FXML private DatePicker dateEmissionPicker;
    @FXML private TextField txtDescription;

    private final ServiceFacture serviceFacture = new ServiceFacture();
    private final ServicePaiement servicePaiement = new ServicePaiement(); // Ajouté

    @FXML
    void ajouterFacture() {
        try {
            // Vérifier que tous les champs sont remplis
            if (txtIdFacture.getText().isEmpty() || txtReservationId.getText().isEmpty() || txtIdPaiement.getText().isEmpty()
                    || txtMontantTotal.getText().isEmpty() || txtMontantPaye.getText().isEmpty()
                    || dateEmissionPicker.getValue() == null || txtDescription.getText().isEmpty()) {
                showAlert("Veuillez remplir tous les champs", Alert.AlertType.WARNING);
                return;
            }

            // Récupérer l'ID de la facture saisi manuellement
            int idFacture;
            try {
                idFacture = Integer.parseInt(txtIdFacture.getText());  // ID de la facture, saisi par l'utilisateur
            } catch (NumberFormatException e) {
                showAlert("L'ID de la facture doit être un entier valide.", Alert.AlertType.ERROR);
                return;
            }

            // Créer la facture avec les données saisies
            Facture facture = new Facture(
                    idFacture,                                   // idFacture spécifié manuellement
                    Integer.parseInt(txtReservationId.getText()),  // ID de la réservation
                    Integer.parseInt(txtIdPaiement.getText()),     // ID de paiement
                    Double.parseDouble(txtMontantTotal.getText()), // Montant total
                    Double.parseDouble(txtMontantPaye.getText()),  // Montant payé
                    dateEmissionPicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), // Date d'émission
                    txtDescription.getText()                      // Description
            );

            // Appeler le service pour ajouter la facture
            boolean success = serviceFacture.ajouterFacture(facture);
            if (success) {
                showAlert("Facture ajoutée avec succès!", Alert.AlertType.INFORMATION);
                clearFields();
            } else {
                showAlert("Échec de l'ajout de la facture", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("Veuillez entrer des valeurs numériques valides pour les IDs et montants", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur lors de l'ajout : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAutoFillMontantTotal() {
        String idPaiementStr = txtIdPaiement.getText();

        if (idPaiementStr.isEmpty()) {
            showAlert("Veuillez entrer un ID de paiement valide.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int idPaiement = Integer.parseInt(idPaiementStr);
            Paiement paiement = servicePaiement.getById(idPaiement); // Correction ici

            if (paiement != null) {
                txtMontantTotal.setText(String.valueOf(paiement.getMontant()));
            } else {
                showAlert("Aucun paiement trouvé avec cet ID !", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("L'ID de paiement doit être un nombre entier valide.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleBackToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuFacture.fxml"));
            Parent root = loader.load();
            txtIdFacture.getScene().setRoot(root);
        } catch (Exception e) {
            showAlert("Erreur lors du retour au menu : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void clearFields() {
        txtIdFacture.clear();
        txtReservationId.clear();
        txtIdPaiement.clear();
        txtMontantTotal.clear();
        txtMontantPaye.clear();
        dateEmissionPicker.setValue(null);
        txtDescription.clear();
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
