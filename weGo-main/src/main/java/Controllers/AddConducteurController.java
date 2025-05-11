package Controllers;

import Entites.Chauffeur;
import Entites.Chauffeur;
import services.Chauffeurservice;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import services.ConducteurService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.sql.SQLException;
import java.util.List;

public class AddConducteurController {

    @FXML private ComboBox<Integer> idVoitureComboBox;
    @FXML private DatePicker departDateComboBox;
    @FXML private DatePicker finalDateComboBox;
    @FXML private TextField numberOfDaysField;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    private Chauffeurservice chauffeurservice;
    private ConducteurService conducteurService;

    // Constructor
    public AddConducteurController() {
        chauffeurservice = new Chauffeurservice();  // Initialize Chauffeurservice
        conducteurService = new ConducteurService();  // Initialize ConducteurService
    }

    class ConducteurService {
        public List<Integer> fetchAllVoitureIds() throws SQLException {
            // This is a placeholder implementation. Replace it with actual database access logic.
            return List.of(1, 2, 3, 4, 5); // Example data
        }
    }

    @FXML
    public void initialize() {
        // Populate the ComboBox for ID Voiture with all ID values from the 'Voiture' table
        try {
            List<Integer> idVoitures = conducteurService.fetchAllVoitureIds();
            idVoitureComboBox.getItems().addAll(idVoitures);  // Populate ComboBox with all voiture IDs
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors du chargement des ID de voiture: " + e.getMessage());
        }

        // Set actions for buttons
        saveButton.setOnAction(e -> saveConducteur());
        cancelButton.setOnAction(e -> cancelAction());

        // Calculate the number of days when the date is changed
        departDateComboBox.setOnAction(e -> updateNumberOfDays());
        finalDateComboBox.setOnAction(e -> updateNumberOfDays());
    }

    // Updates the number of days between selected start and end date
    private void updateNumberOfDays() {
        if (departDateComboBox.getValue() != null && finalDateComboBox.getValue() != null) {
            long numberOfDays = ChronoUnit.DAYS.between(departDateComboBox.getValue(), finalDateComboBox.getValue());
            numberOfDaysField.setText(String.valueOf(numberOfDays));  // Set the number of days in the field
        }
    }

    // Save the conducteur
    private void saveConducteur() {
        // Retrieve data from form fields
        Integer idVoiture = idVoitureComboBox.getValue();
        LocalDate dateDepart = departDateComboBox.getValue();
        LocalDate dateArret = finalDateComboBox.getValue();
        String dispoText = numberOfDaysField.getText();

        // Validate inputs
        if (idVoiture == null || dateDepart == null || dateArret == null || dispoText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Tous les champs doivent être remplis.");
            return;
        }

        try {
            // Convert the number of days to an integer
            int nbDays = Integer.parseInt(dispoText);

            // Create a new conducteur entity
            Chauffeur ch = new Chauffeur(idVoiture, nbDays, dateDepart, dateArret);

            // Insert the conducteur into the database
            chauffeurservice.addConducteur(ch);
            showAlert(Alert.AlertType.INFORMATION, "Conducteur ajouté avec succès.");

            // Rafraîchir la table si ViewConducteurController est ouvert
            ViewConducteurController.refreshConducteurTable();

            // Optionally, close the window after saving
            cancelAction();
        } catch (NumberFormatException e) {
            // Log error for debugging
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Veuillez entrer un nombre valide pour le nombre de jours.");
        } catch (SQLException e) {
            // Log error for debugging
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur lors de l'ajout du conducteur dans la base de données : " + e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected errors
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Une erreur inattendue s'est produite : " + e.getMessage());
        }
    }

    // Cancel action (close the form)
    private void cancelAction() {
        // Close the current window (AddConducteur window)
        cancelButton.getScene().getWindow().hide();
    }

    // Show an alert message
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
