package Controllers;

import Entities.TypeVoiture;
import Entities.Voiture;
import Service.VoitureService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class AjouterVoitureController {

    @FXML
    private Button ajouterButton;
    @FXML
    private Button annulerButton;  // The "Annuler" (Cancel) button

    @FXML
    private TextField marqueField;
    @FXML
    private TextField modeleField;
    @FXML
    private TextField typeLibelleField;
    @FXML
    private TextField matriculeField;  // Add TextField for matricule

    private final VoitureService voitureService = new VoitureService();

    @FXML
    public void initialize() {
        // When the add button is clicked, call the method to add the car
        ajouterButton.setOnAction(e -> ajouterVoitureAvecType());

        // When the cancel button is clicked, navigate back to the previous page
        annulerButton.setOnAction(e -> navigateToViewVoiture());
    }

    private void ajouterVoitureAvecType() {
        String marque = marqueField.getText().trim();
        String modele = modeleField.getText().trim();
        String typeLibelle = typeLibelleField.getText().trim();
        String matricule = matriculeField.getText().trim();  // Get matricule from input field

        if (marque.isEmpty() || modele.isEmpty() || typeLibelle.isEmpty() || matricule.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Tous les champs sont obligatoires.");
            return;
        }

        try {
            TypeVoiture type = TypeVoiture.valueOf(typeLibelle.toUpperCase());
            Voiture voiture = new Voiture(marque, modele, type, matricule);  // Include matricule in constructor
            voitureService.insert(voiture);
            showAlert(Alert.AlertType.INFORMATION, "Voiture ajoutée avec succès !");
            clearFields();

            // After adding the car, navigate back to the car list
            navigateToViewVoiture();

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Le type de voiture est invalide.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL : " + e.getMessage());
        }
    }

    private void navigateToViewVoiture() {
        try {
            // Load the previous page (ViewVoiture.fxml)
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/fxmlfile.fxml"));  // Update path if needed

            // Get the current stage (window)
            Stage stage = (Stage) annulerButton.getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        marqueField.clear();
        modeleField.clear();
        typeLibelleField.clear();
        matriculeField.clear();  // Clear matricule field
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
