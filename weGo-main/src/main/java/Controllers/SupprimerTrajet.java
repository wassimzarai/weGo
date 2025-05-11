package Controllers;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.scene.control.*;


import Entites.Trajet;
import Entites.StatutTrajet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.ServiceTrajet;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;

public class SupprimerTrajet {

    @FXML
    private TableColumn<Trajet, String> arrCol;

    @FXML
    private TableColumn<Trajet, LocalDate> dateCol;

    @FXML
    private TableColumn<Trajet, String> depCol;

    @FXML
    private TableColumn<Trajet, LocalTime> heureCol;

    @FXML
    private TableColumn<Trajet, Integer> idCol;

    @FXML
    private Button modifierBtn;

    @FXML
    private TableColumn<Trajet, Integer> nbPlaceCol;

    @FXML
    private TableColumn<Trajet, Integer> prixCol;

    @FXML
    private TableColumn<Trajet, StatutTrajet> statCol;
    @FXML
    private Button idback;
    @FXML
    private Button buttonCO2;


    @FXML
    private Button supprimerBtn;
    @FXML
    private TableColumn<Trajet, Integer> colvoiture;

    @FXML
    private TableView<Trajet> trajetTable;
    @FXML
    private ProgressIndicator loadingIndicator;


    private ServiceTrajet sp = new ServiceTrajet();

    @FXML
    void initialize() throws SQLException {
        ArrayList<Trajet> list = sp.getList1();
        trajetTable.getItems().addAll(list);
        ObservableList<Trajet> obe = FXCollections.observableArrayList(list);
        trajetTable.setItems(obe);
        idCol.setCellValueFactory(new PropertyValueFactory<>("id_trajet"));
        depCol.setCellValueFactory(new PropertyValueFactory<>("depart"));
        arrCol.setCellValueFactory(new PropertyValueFactory<>("arrivee"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        heureCol.setCellValueFactory(new PropertyValueFactory<>("heure"));
        statCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prix_place"));
        nbPlaceCol.setCellValueFactory(new PropertyValueFactory<>("nb_place"));
        colvoiture.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getVoiture().getId()).asObject()
        );


    }


    @FXML
    void onModifier(ActionEvent event) {
        Trajet trajetSelectionne = trajetTable.getSelectionModel().getSelectedItem();
        if (trajetSelectionne == null) {
            // Afficher un message d'alerte si aucune ligne n'est sélectionnée
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Alerte");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un trajet à modifier.");
            alert.showAndWait();
            return;
        }

        try {
            // 2. Charger l'interface Modifier (FXML)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModifierTrajet.fxml"));
            Parent root = loader.load();

            // 3. Passer l'objet trajet au contrôleur Modifier
            ModifierTrajet modifierController = loader.getController();
            modifierController.setTrajet(trajetSelectionne);  // Remplir automatiquement les champs

            // 4. Créer une nouvelle scène et l'afficher
            Stage stage = new Stage();
            stage.setTitle("Modifier Trajet");
            stage.setScene(new Scene(root));
            stage.show();

            // Optionnel : Fermer la fenêtre actuelle (si nécessaire)
            // ((Node)(event.getSource())).getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @FXML
    void onSupprimer(ActionEvent event) {
        Trajet selectedTrajet = trajetTable.getSelectionModel().getSelectedItem();

        if (selectedTrajet == null) {
            // Alerte si aucun trajet n'est sélectionné
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un trajet à supprimer.");
            alert.show();
            return;
        }

        // Alerte de confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce trajet ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = sp.delete(selectedTrajet);

                if (success) {
                    // Supprimer de la liste observable
                    trajetTable.getItems().remove(selectedTrajet);

                    // Alerte succès
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Succès");
                    alert.setHeaderText(null);
                    alert.setContentText("Trajet supprimé avec succès !");
                    alert.show();
                } else {
                    // Alerte erreur suppression
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Erreur lors de la suppression du trajet.");
                    alert.show();
                }
            } catch (SQLException e) {
                // Alerte erreur SQL
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur SQL");
                alert.setHeaderText(null);
                alert.setContentText("Impossible de supprimer le trajet : " + e.getMessage());
                alert.show();
            }
        }
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void buttonback(ActionEvent event) {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Principal.fxml"));
//        Parent root;
//        try {
//            root = loader.load();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        // Créer une nouvelle scène et l'afficher
//        Stage stage = new Stage();
//        stage.setTitle("Accueil");
//        stage.setScene(new Scene(root));
//        stage.show();
//
//        // Fermer la fenêtre actuelle de manière générique
//        Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
//        currentStage.close();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Principal.fxml"));
            Stage stage = (Stage) idback.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Menu Principal");
        } catch (IOException e) {
            afficherErreur("Impossible de revenir au menu : " + e.getMessage());
        }


    }

    @FXML
    void buttonCO2(ActionEvent event) {
        Stage loadingStage = new Stage();
        ProgressIndicator progressIndicator = new ProgressIndicator();

        StackPane loadingRoot = new StackPane(progressIndicator);
        loadingRoot.setStyle("-fx-background-color: rgba(255, 255, 255, 0.85); -fx-padding: 20;");
        Scene loadingScene = new Scene(loadingRoot, 150, 150);
        loadingStage.setScene(loadingScene);
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setResizable(false);
        loadingStage.setTitle("Chargement en cours...");
        loadingStage.show();

        // Création du Task pour charger le fichier FXML
        Task<Parent> loadTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CO2.fxml"));
                return loader.load(); // C'est cette ligne qui peut prendre du temps selon les ressources
            }
        };

        // Quand le chargement est terminé
        loadTask.setOnSucceeded(workerStateEvent -> {
            loadingStage.close(); // Ferme la fenêtre de chargement

            Parent root = loadTask.getValue();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Impact écologique");

            // Animation d’apparition (facultatif)
            FadeTransition fade = new FadeTransition(Duration.millis(500), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();

            stage.show();
        });

        // En cas d’erreur
        loadTask.setOnFailed(workerStateEvent -> {
            loadingStage.close();
            loadTask.getException().printStackTrace();
        });

        // Lancement de la tâche en arrière-plan
        new Thread(loadTask).start();
    }
}