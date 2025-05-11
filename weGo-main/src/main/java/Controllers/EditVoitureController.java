package Controllers;

import Entites.Voiture;
import Entites.TypeVoiture;
import services.VoitureService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

public class EditVoitureController {

    @FXML
    private TextField marqueField;
    @FXML
    private TextField modeleField;
    @FXML
    private TextField typeLibelleField;
    @FXML
    private TextField matriculeField;

    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private Voiture voiture;
    private VoitureService voitureService = new VoitureService();

    // Set the voiture details to the fields for editing
    public void setVoiture(Voiture voiture) {
        this.voiture = voiture;
        // Initialize the fields with the car's current data
        if (voiture != null) {
            marqueField.setText(voiture.getMarque());
            modeleField.setText(voiture.getModele());
            typeLibelleField.setText(voiture.getType().name());
            matriculeField.setText(voiture.getMatricule());
        }
    }

    @FXML
    public void initialize() {
        // Gestion du bouton pour enregistrer la voiture modifiée
        saveButton.setOnAction(e -> saveVoiture());

        // Gestion du bouton pour annuler l'édition et revenir à la vue précédente
        cancelButton.setOnAction(e -> navigateToViewVoiture());
    }

    private void saveVoiture() {
        String marque = marqueField.getText().trim();
        String modele = modeleField.getText().trim();
        String typeLibelle = typeLibelleField.getText().trim();
        String matricule = matriculeField.getText().trim();

        if (marque.isEmpty() || modele.isEmpty() || typeLibelle.isEmpty() || matricule.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Tous les champs sont obligatoires.");
            return;
        }

        try {
            TypeVoiture type = TypeVoiture.valueOf(typeLibelle.toUpperCase());
            voiture.setMarque(marque);
            voiture.setModele(modele);
            voiture.setType(type);
            voiture.setMatricule(matricule);
            voitureService.update(voiture);
            showAlert(Alert.AlertType.INFORMATION, "Voiture mise à jour avec succès !");
            navigateToViewVoiture();
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Le type de voiture est invalide.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL : " + e.getMessage());
        }
    }

    private void navigateToViewVoiture() {
        try {
            // Charger la page principale pour la liste des voitures
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/fxmlfile.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene); // Revenir à la page de gestion des voitures
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur lors du chargement de la page de gestion des voitures.");
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
