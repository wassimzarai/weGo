package gui;

import entities.Reservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class AffichageController {

    @FXML private TableView<Reservation> table;
    @FXML private TableColumn<Reservation, Integer> idCol;
    @FXML private TableColumn<Reservation, String> nomCol;
    @FXML private TableColumn<Reservation, LocalDate> dateCol;
    @FXML private TableColumn<Reservation, String> departCol;
    @FXML private TableColumn<Reservation, String> arriveeCol;
    @FXML private TableColumn<Reservation, String> statutCol;
    @FXML private TableColumn<Reservation, String> typeCol;
    @FXML private TableColumn<Reservation, String> commentaireCol;

    @FXML private TextField nomField;
    @FXML private DatePicker datePicker;
    @FXML private TextField departField;
    @FXML private TextField arriveeField;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private TextField typeField;
    @FXML private TextField commentaireField;

    private ObservableList<Reservation> reservations = FXCollections.observableArrayList();
    private AtomicInteger idGenerator = new AtomicInteger(3);

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        departCol.setCellValueFactory(new PropertyValueFactory<>("depart"));
        arriveeCol.setCellValueFactory(new PropertyValueFactory<>("arrivee"));
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        commentaireCol.setCellValueFactory(new PropertyValueFactory<>("commentaire"));

        reservations.addAll(
                new Reservation(1, "Ali", LocalDate.now(), "Tunis", "Sfax", "Confirmée", "Aller", "RAS"),
                new Reservation(2, "Sara", LocalDate.now().plusDays(1), "Sousse", "Monastir", "En attente", "Aller-retour", "Bagage spécial")
        );

        table.setItems(reservations);
        statutComboBox.getItems().addAll("Confirmée", "En attente", "Annulée");

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nomField.setText(newSelection.getNom());
                datePicker.setValue(newSelection.getDate());
                departField.setText(newSelection.getDepart());
                arriveeField.setText(newSelection.getArrivee());
                statutComboBox.setValue(newSelection.getStatut());
                typeField.setText(newSelection.getType());
                commentaireField.setText(newSelection.getCommentaire());
            }
        });
    }

    @FXML
    public void ajouterReservation() {
        String nom = nomField.getText();
        LocalDate date = datePicker.getValue();
        String depart = departField.getText();
        String arrivee = arriveeField.getText();
        String statut = statutComboBox.getValue();
        String type = typeField.getText();
        String commentaire = commentaireField.getText();

        if (nom.isEmpty() || date == null || depart.isEmpty() || arrivee.isEmpty() || statut == null || type.isEmpty()) {
            showAlert("Veuillez remplir tous les champs obligatoires !");
            return;
        }

        int newId = idGenerator.getAndIncrement();
        Reservation newRes = new Reservation(newId, nom, date, depart, arrivee, statut, type, commentaire);
        reservations.add(newRes);
        clearFields();
    }

    @FXML
    public void modifierReservation(ActionEvent actionEvent) {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner une réservation à modifier.");
            return;
        }

        selected.setNom(nomField.getText());
        selected.setDate(datePicker.getValue());
        selected.setDepart(departField.getText());
        selected.setArrivee(arriveeField.getText());
        selected.setStatut(statutComboBox.getValue());
        selected.setType(typeField.getText());
        selected.setCommentaire(commentaireField.getText());

        table.refresh();
        clearFields();
    }

    @FXML
    public void supprimerReservation(ActionEvent actionEvent) {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner une réservation à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Suppression de réservation");
        confirm.setContentText("Voulez-vous vraiment supprimer cette réservation ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            reservations.remove(selected);
            clearFields();
        }
    }

    @FXML
    public void rafraichirTable(ActionEvent actionEvent) {
        table.refresh();
    }

    @FXML
    public void ouvrirMessagerie(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Messagerie.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Messagerie");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur lors de l'ouverture de la messagerie : " + e.getMessage());
        }
    }

    private void clearFields() {
        nomField.clear();
        datePicker.setValue(null);
        departField.clear();
        arriveeField.clear();
        statutComboBox.setValue(null);
        typeField.clear();
        commentaireField.clear();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
