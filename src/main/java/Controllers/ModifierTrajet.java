package Controllers;

import Entities.Trajet;
import Entities.Pointrdv;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.ServiceTrajet;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;


public class ModifierTrajet {
    @FXML
    private TextField idtextfield;

    @FXML
    private TextField arriveeTextField;

    @FXML
    private ComboBox<Integer> comboboxHeure;

    @FXML
    private ComboBox<Integer> comboboxMinute;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField departTextField;

    @FXML
    private TextField nombrePlacesTextField;

    @FXML
    private Spinner<Integer> prixSpinner;
    @FXML
    private TextField textfieldrdv;

    @FXML
    public void initialize() {
        for (int i = 0; i < 24; i++) {
            comboboxHeure.getItems().add(i);
        }
        for (int i = 0; i < 60; i++) {
            comboboxMinute.getItems().add(i);
        }
    }

    public void setTrajet(Trajet trajetSelectionne) {


        departTextField.setText(trajetSelectionne.getDepart());
        arriveeTextField.setText(trajetSelectionne.getArrivee());
        datePicker.setValue(trajetSelectionne.getDate());

        // Remplir les ComboBox avec les valeurs d'heure et de minute
        comboboxHeure.setValue(trajetSelectionne.getHeure().getHour());
        comboboxMinute.setValue(trajetSelectionne.getHeure().getMinute());

        // Remplir le Spinner avec le prix
        prixSpinner.getValueFactory().setValue(trajetSelectionne.getPrix_place());

        // Remplir le champ 'nombre de places'
        nombrePlacesTextField.setText(String.valueOf(trajetSelectionne.getNb_place()));

        // Remplir le champ 'ID' (invisible mais utile pour les mises à jour)
        idtextfield.setText(String.valueOf(trajetSelectionne.getId_trajet()));
        try {
            ServiceTrajet prs = new ServiceTrajet();
            Pointrdv p = prs.getByIdTrajet(trajetSelectionne.getId_trajet());
            if (p != null) {
                textfieldrdv.setText(p.getAdresse());
            } else {
                textfieldrdv.setText(""); // ou un message par défaut
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    @FXML
    void modifierTrajet(ActionEvent event) {
        ServiceTrajet st = new ServiceTrajet();
        Trajet t = new Trajet();
        Pointrdv p = new Pointrdv(textfieldrdv.getText());

        try {
            // Récupération et validation des heures et minutes
            Integer heure = comboboxHeure.getValue();
            Integer minute = comboboxMinute.getValue();
            if (heure == null || minute == null) {
                throw new IllegalArgumentException("Veuillez sélectionner une valeur pour les heures et les minutes.");
            }
            String Horraire = String.format("%02d:%02d", heure, minute);
            LocalTime HorraireLocalTime = LocalTime.parse(Horraire);

            // Récupération et validation du prix
            Integer prix_place = prixSpinner.getValue();
            if (prix_place == null || prix_place <= 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de saisie");
                alert.setHeaderText(null);
                alert.setContentText("Veuillez entrer un prix valide.");
                alert.showAndWait();
                return; // Empêche la suite de s'exécuter
            }


            // Validation de la date
            LocalDate date = datePicker.getValue();
            if (date == null) {
                throw new IllegalArgumentException("Veuillez choisir une date valide.");
            }

            // Validation et conversion du champ 'nombre de places'
            String nombrePlacesText = nombrePlacesTextField.getText();
            if (nombrePlacesText == null || nombrePlacesText.trim().isEmpty()) {
                throw new IllegalArgumentException("Le champ 'nombre de places' ne peut pas être vide.");
            }
            int nombrePlaces;
            try {
                nombrePlaces = Integer.parseInt(nombrePlacesText);
                if (nombrePlaces <= 0) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur de saisie");
                    alert.setHeaderText(null);
                    alert.setContentText("Le nombre de places doit être supérieur à zéro.");
                    alert.showAndWait();
                    return;
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de saisie");
                alert.setHeaderText(null);
                alert.setContentText("Veuillez entrer un nombre entier valide pour le champ 'nombre de places'.");
                alert.showAndWait();
                return;
            }
            t.setId_trajet(Integer.parseInt(idtextfield.getText()));
            t.setDepart(departTextField.getText());
            t.setArrivee(arriveeTextField.getText());
            t.setDate(date);
            t.setHeure(HorraireLocalTime);
            t.setPrix_place(prix_place);
            t.setNb_place(nombrePlaces);
            t.setPointrdv(p);
            try {
                st.update(t);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            try {
                if (st.update(t)) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Succès");
                    alert.setHeaderText(null);
                    alert.setContentText("Trajet modifié avec succès !");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Erreur lors de l'ajout du trajet.");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


        } catch (IllegalArgumentException e) {
            // Affiche un message d'erreur utilisateur
            System.err.println("Erreur : " + e.getMessage());
        }
    }

//    public void setTrajet(Trajet trajetSelectionne) {
//        departTextField.setText(trajetSelectionne.getDepart());
//        arriveeTextField.setText(trajetSelectionne.getArrivee());
//        datePicker.setValue(trajetSelectionne.getDate());
//
//        // Remplir les ComboBox avec les valeurs d'heure et de minute
//        comboboxHeure.setValue(trajetSelectionne.getHeure().getHour());
//        comboboxMinute.setValue(trajetSelectionne.getHeure().getMinute());
//
//        // Remplir le Spinner avec le prix
//        prixSpinner.getValueFactory().setValue(trajetSelectionne.getPrix_place());
//
//        // Remplir le champ 'nombre de places'
//        nombrePlacesTextField.setText(String.valueOf(trajetSelectionne.getNb_place()));
//
//        // Remplir le champ 'ID' (invisible mais utile pour les mises à jour)
//        idtextfield.setText(String.valueOf(trajetSelectionne.getId_trajet()));
//    }

    @FXML
    void backbutton(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SupprimerTrajet.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle via l'event source et fermer la fenêtre
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            // Ouvrir la nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("Afficher Trajet");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            // En cas d'erreur de chargement
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de chargement");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger la vue : " + e.getMessage());
            alert.show();
            e.printStackTrace();
        }
    }
}