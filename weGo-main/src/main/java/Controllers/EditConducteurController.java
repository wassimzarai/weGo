package Controllers;

import Entites.Chauffeur;
import services.Chauffeurservice;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.SQLException;

public class EditConducteurController {

    @FXML private TextField disponibiliteField;
    @FXML private ComboBox<String> typeVehiculeComboBox;
    @FXML private CheckBox availableCheckBox;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Chauffeur chauffeur;
    private Chauffeurservice conducteurService = new Chauffeurservice();

    // Setter for the Conducteur object
    public void setConducteur(Chauffeur Chauffeur) {
        this.chauffeur = chauffeur;
        if (chauffeur != null) {
            disponibiliteField.setText(String.valueOf(chauffeur.getDisponibilite()));  // Convert integer to string

            // You can add more fields here based on the attributes of Conducteur
        }
    }

    @FXML
    public void initialize() {
        saveButton.setOnAction(e -> saveConducteur());
        cancelButton.setOnAction(e -> navigateToViewConducteur());
    }

    private void saveConducteur() {
        String disponibilite = disponibiliteField.getText().trim();
        if (disponibilite.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Le champ de disponibilité est obligatoire.");
            return;
        }

        try {
            // Parse the String to an Integer
            int disponibiliteInt = Integer.parseInt(disponibilite);
            chauffeur.setDisponibilite(disponibiliteInt);  // Use Integer value here

            // Save the updated conductor data in the database
            conducteurService.update(chauffeur);
            showAlert(Alert.AlertType.INFORMATION, "Conducteur mis à jour avec succès !");

            navigateToViewConducteur();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Veuillez entrer un nombre valide pour la disponibilité.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL : " + e.getMessage());
        }
    }


    private void navigateToViewConducteur() {
        try {
            // Load the conducteur view (ViewConducteur.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Conducteur.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) cancelButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur lors du chargement de la page des conducteurs.");
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
