package Controllers;

import entities.Paiement;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import services.ServicePaiement;
import java.io.IOException;

public class SupprimerPaiementControl {
    @FXML private TextField txtidsupp;
    @FXML private Button btnsupprimer;
    @FXML private Button btnRetourMenu;

    private final ServicePaiement service = new ServicePaiement();

    @FXML
    void btnsupprimer() {
        try {
            int id = Integer.parseInt(txtidsupp.getText());

            if (service.supprimerPaiement(id)) {
                afficherSucces("Paiement supprimé avec succès !");
                txtidsupp.clear();
            } else {
                afficherErreur("Aucun paiement trouvé avec cet ID.");
            }
        } catch (NumberFormatException e) {
            afficherErreur("Veuillez entrer un ID valide (nombre entier)");
        }
    }

    @FXML
    void handleBackToMenu() {
        try {
            // Charge la page du menu principal (remplace "Menu.fxml" par ton vrai fichier)
            Parent root = FXMLLoader.load(getClass().getResource("/MenuPaiement.fxml"));
            Stage stage = (Stage) btnRetourMenu.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherErreur("Impossible de retourner au menu.");
        }
    }

    private void afficherSucces(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
