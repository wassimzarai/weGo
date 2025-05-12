package gui;

import entities.Reservation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

public class ModifierController {

    @FXML
    private TextField arriveeField;

    @FXML
    private TextArea commentaireField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField departField;

    @FXML
    private TextField nomField;

    @FXML
    private ComboBox<String> statutComboBox;

    @FXML
    private TextField typeField;

    // La réservation à modifier (doit être fournie par l'appelant)
    private Reservation selectedReservation;
    private ListeReservationsController listeController;

    @FXML
    public void initialize() {
        // Initialiser la ComboBox avec les statuts possibles
        statutComboBox.getItems().addAll("Confirmée", "En attente", "Annulée");
    }

    public void setReservation(Reservation reservation) {
        this.selectedReservation = reservation;

        // Pré-remplissage des champs
        nomField.setText(reservation.getNomPassager());
        departField.setText(reservation.getPointDepart());
        arriveeField.setText(reservation.getPointArrivee());
        commentaireField.setText(reservation.getCommentaire());
        typeField.setText(reservation.getTypeTrajet());
        statutComboBox.setValue(reservation.getStatut());
        datePicker.setValue(reservation.getDateReservation());
    }
    
    public void setListeController(ListeReservationsController listeController) {
        this.listeController = listeController;
    }

    @FXML
    void modifierReservation(ActionEvent event) {
        if (selectedReservation != null && validerChamps()) {
            // Mise à jour des propriétés de la réservation
            selectedReservation.setNomPassager(nomField.getText());
            selectedReservation.setPointDepart(departField.getText());
            selectedReservation.setPointArrivee(arriveeField.getText());
            selectedReservation.setCommentaire(commentaireField.getText());
            selectedReservation.setTypeTrajet(typeField.getText());
            selectedReservation.setStatut(statutComboBox.getValue());
            selectedReservation.setDateReservation(datePicker.getValue());

            try {
                // Enregistrer les modifications dans la base de données
                services.ReserService reservationService = new services.ReserService();
                reservationService.modifierReservation(selectedReservation);
                
                // Mettre à jour dans la liste principale
                listeController.mettreAJourReservation(selectedReservation);
                
                // Fermer la fenêtre
                ((Stage) nomField.getScene().getWindow()).close();
            } catch (Exception e) {
                e.printStackTrace();
                afficherErreur("Erreur lors de la modification : " + e.getMessage());
            }
        }
    }
    
    private boolean validerChamps() {
        String nom = nomField.getText();
        LocalDate date = datePicker.getValue();
        String depart = departField.getText();
        String arrivee = arriveeField.getText();
        String statut = statutComboBox.getValue();
        String type = typeField.getText();

        // Validation rapide des champs obligatoires
        if (nom.isEmpty() || date == null || depart.isEmpty() || arrivee.isEmpty() || statut == null || type.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs obligatoires");
            return false;
        }
        
        return true;
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void annulerModification(ActionEvent event) {
        // Fermer la fenêtre sans sauvegarder
        ((Stage) nomField.getScene().getWindow()).close();
    }
}
