package Controllers;

import entities.Paiement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.ServicePaiement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class AfficherPaiementControl {
    @FXML
    private TableView<Paiement> tablePaiements;
    @FXML
    private TableColumn<Paiement, Integer> colId;
    @FXML
    private TableColumn<Paiement, Integer> colReservationId;
    @FXML
    private TableColumn<Paiement, Double> colMontant;
    @FXML
    private TableColumn<Paiement, String> colDate;
    @FXML
    private TableColumn<Paiement, String> colMethode;
    @FXML
    private TableColumn<Paiement, String> colStatut;
    @FXML
    private Button btnAfficher;
    @FXML
    private Button btnRetourMenu; // Nouveau bouton

    private final ServicePaiement servicePaiement = new ServicePaiement();

    @FXML
    public void initialize() {
        configurerColonnes();
    }

    private void configurerColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colReservationId.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("datePaiement"));
        colMethode.setCellValueFactory(new PropertyValueFactory<>("methodePaiement"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    @FXML
    private void handleAfficher() {
        ArrayList<Paiement> paiements = new ArrayList<>(servicePaiement.getList());
        paiements.sort(Comparator.comparingDouble(Paiement::getMontant));
        ObservableList<Paiement> list = FXCollections.observableArrayList(paiements);
        tablePaiements.setItems(list);
    }

    @FXML
    private void naviguerAjout() {
        chargerVue("/AjouterPaiement.fxml", "Ajouter Paiement");
    }

    @FXML
    private void naviguerModification() {
        Paiement selection = tablePaiements.getSelectionModel().getSelectedItem();
        if (selection != null) {
            chargerVue("/ModifierPaiement.fxml", "Modifier Paiement");
        } else {
            afficherAlerte("Aucun paiement sélectionné");
        }
    }

    @FXML
    private void handleBackToMenu() {
        // Adaptation ici : retourner à l'interface principale (menu)
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/MenuPaiement.fxml")); // Remplace "/Menu.fxml" si ton menu a un autre nom
            Stage stage = (Stage) btnRetourMenu.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Menu Principal");
        } catch (IOException e) {
            afficherErreur("Erreur Navigation", "Impossible de revenir au menu : " + e.getMessage());
        }
    }

    private void chargerVue(String fxmlPath, String titre) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherErreur("Erreur Navigation", e.getMessage());
        }
    }

    private void afficherAlerte(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
