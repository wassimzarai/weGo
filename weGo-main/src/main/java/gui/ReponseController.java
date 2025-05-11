package gui;

import Entites.utilisateur;
import Entites.Reponse;
import Entites.Reclamation;
import services.ReponseService;
import services.ReclamationService;
import services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.time.LocalDate;

public class ReponseController {
    @FXML
    private TableView<Reponse> reponseTable;
    @FXML
    private TableColumn<Reponse, Integer> idColumn;
    @FXML
    private TableColumn<Reponse, String> messageColumn;
    @FXML
    private TableColumn<Reponse, LocalDate> dateColumn;
    @FXML
    private TableColumn<Reponse, String> reclamationColumn;
    @FXML
    private TableColumn<Reponse, String> adminColumn;
    
    @FXML
    private TextArea messageField;
    @FXML
    private ComboBox<Reclamation> reclamationComboBox;
    @FXML
    private ComboBox<utilisateur> adminComboBox;
    @FXML
    private ComboBox<String> statutComboBox;
    
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;

    private ReponseService reponseService;
    private ReclamationService reclamationService;
    private UserService userService;
    private ObservableList<Reponse> reponseList;

    @FXML
    public void initialize() {
        reponseService = new ReponseService();
        reclamationService = new ReclamationService();
        userService = new UserService();
        reponseList = FXCollections.observableArrayList();

        // Initialize columns
        idColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        messageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMessage()));
        dateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateReponse()));
        reclamationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getReclamation() != null ? cellData.getValue().getReclamation().getTitre() : ""
        ));
        adminColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getAdmin() != null ? cellData.getValue().getAdmin().getUsername() : ""
        ));

        // Initialiser le ComboBox de statut
        statutComboBox.setItems(FXCollections.observableArrayList("ouvert", "en cours", "resolu", "rejete"));
        statutComboBox.setValue("en cours");

        // Load reclamations and admins into combo boxes
        loadReclamations();
        loadAdmins();
        
        // Load reponse data
        loadReponses();

        // Add listener for table selection
        reponseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showReponseDetails(newSelection);
            }
        });
    }

    private void loadReclamations() {
        ObservableList<Reclamation> reclamations = FXCollections.observableArrayList(reclamationService.voir());
        reclamationComboBox.setItems(reclamations);
    }

    private void loadAdmins() {
        ObservableList<utilisateur> admins = FXCollections.observableArrayList(userService.getAdmins());
        adminComboBox.setItems(admins);
    }

    private void loadReponses() {
        reponseList.clear();
        reponseList.addAll(reponseService.voir());
        reponseTable.setItems(reponseList);
    }

    private void showReponseDetails(Reponse reponse) {
        messageField.setText(reponse.getMessage());
        reclamationComboBox.setValue(reponse.getReclamation());
        adminComboBox.setValue(reponse.getAdmin());
    }

    @FXML
    private void handleAddReponse() {
        if (validateInput()) {
            // Créer et enregistrer la réponse
            Reponse reponse = new Reponse();
            reponse.setMessage(messageField.getText());
            reponse.setDateReponse(LocalDate.now());
            reponse.setReclamation(reclamationComboBox.getValue());
            reponse.setAdmin(adminComboBox.getValue());

            reponseService.ajouter(reponse);
            
            // Mettre à jour le statut de la réclamation selon la sélection
            Reclamation reclamation = reclamationComboBox.getValue();
            String nouveauStatut = statutComboBox.getValue();
            
            if (reclamation != null && nouveauStatut != null) {
                // Mettre à jour le statut de la réclamation
                reclamation.setStatut(nouveauStatut);
                reclamationService.modifier(reclamation);
                
                // Afficher une confirmation à l'utilisateur
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Réponse envoyée");
                alert.setHeaderText(null);
                alert.setContentText("Réponse enregistrée et statut de la réclamation mis à jour en '" + nouveauStatut + "'");
                alert.showAndWait();
            }
            
            // Rafraîchir la liste des réponses
            loadReponses();
            clearFields();
        }
    }

    @FXML
    private void handleUpdateReponse() {
        Reponse selectedReponse = reponseTable.getSelectionModel().getSelectedItem();
        if (selectedReponse != null && validateInput()) {
            selectedReponse.setMessage(messageField.getText());
            selectedReponse.setReclamation(reclamationComboBox.getValue());
            selectedReponse.setAdmin(adminComboBox.getValue());

            reponseService.modifier(selectedReponse);
            loadReponses();
            clearFields();
        }
    }

    @FXML
    private void handleDeleteReponse() {
        Reponse selectedReponse = reponseTable.getSelectionModel().getSelectedItem();
        if (selectedReponse != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText(null);
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                reponseService.supprimer(selectedReponse.getId());
                loadReponses();
                clearFields();
            }
        }
    }

    private boolean validateInput() {
        String errorMessage = "";

        if (messageField.getText() == null || messageField.getText().trim().isEmpty()) {
            errorMessage += "Le message ne peut pas être vide!\n";
        }
        if (reclamationComboBox.getValue() == null) {
            errorMessage += "Veuillez sélectionner une réclamation!\n";
        }
        if (adminComboBox.getValue() == null) {
            errorMessage += "Veuillez sélectionner un administrateur!\n";
        }
        if (statutComboBox.getValue() == null) {
            errorMessage += "Veuillez sélectionner un statut!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de validation");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }

    private void clearFields() {
        messageField.clear();
        reclamationComboBox.setValue(null);
        adminComboBox.setValue(null);
        statutComboBox.setValue("en cours");
    }
} 