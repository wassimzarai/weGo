package Controllers;

import entities.Facture;
import entities.Paiement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import services.ServiceFacture;
import services.ServicePaiement;

public class ModifierFactureController {

    @FXML private TextField txtIdFacture;
    @FXML private TextField txtReservationId;
    @FXML private TextField txtMontantTotal;
    @FXML private TextField txtMontantPaye;
    @FXML private TextField txtDateEmission;
    @FXML private TextField txtDescription;
    @FXML private TextField txtIdPaiement;

    private final ServiceFacture serviceFacture = new ServiceFacture();
    private final ServicePaiement servicePaiement = new ServicePaiement();

    @FXML
    void rechercherFacture() {
        System.out.println("Bouton Rechercher cliqué");

        try {
            int idFacture = Integer.parseInt(txtIdFacture.getText());
            Facture facture = serviceFacture.getById(idFacture);

            if (facture != null) {
                txtReservationId.setText(String.valueOf(facture.getReservationId()));
                txtMontantTotal.setText(String.valueOf(facture.getMontantTotal()));
                txtMontantPaye.setText(String.valueOf(facture.getMontantPaye()));
                txtDateEmission.setText(facture.getDateEmission());
                txtDescription.setText(facture.getDescription());
                txtIdPaiement.setText(String.valueOf(facture.getPaiementId()));

                Paiement paiement = servicePaiement.getById(facture.getPaiementId());
                if (paiement != null && !"PAYE".equalsIgnoreCase(paiement.getStatut())) {
                    showAlert("Attention : Le paiement associé n'est pas marqué comme 'PAYE'", AlertType.WARNING);
                }
            } else {
                showAlert("Aucune facture trouvée avec l'ID : " + idFacture, AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("Veuillez entrer un ID valide (nombre entier)", AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur lors de la recherche : " + e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    void modifierFacture() {
        try {
            if (txtReservationId.getText().isEmpty() || txtIdPaiement.getText().isEmpty()) {
                showAlert("Veuillez d'abord rechercher une facture", AlertType.WARNING);
                return;
            }

            Facture facture = new Facture(
                    Integer.parseInt(txtIdFacture.getText()),
                    Integer.parseInt(txtReservationId.getText()),
                    Integer.parseInt(txtIdPaiement.getText()),
                    Double.parseDouble(txtMontantTotal.getText()),
                    Double.parseDouble(txtMontantPaye.getText()),
                    txtDateEmission.getText(),
                    txtDescription.getText()
            );

            if (serviceFacture.updateFacture(facture)) {
                showAlert("Facture modifiée avec succès!", AlertType.INFORMATION);
            } else {
                showAlert("Échec de la modification", AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Erreur: " + e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    void handleBackToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MenuFacture.fxml"));
            Parent root = loader.load();
            txtIdFacture.getScene().setRoot(root);
        } catch (Exception e) {
            showAlert("Erreur lors du retour au menu : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
