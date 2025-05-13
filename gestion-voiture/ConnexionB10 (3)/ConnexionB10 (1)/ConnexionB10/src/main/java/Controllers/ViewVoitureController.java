package Controllers;

import Entities.Voiture;
import Service.VoitureService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.stage.Stage;
import com.itextpdf.text.Document; // Ensure iText library is added to the classpath
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public class ViewVoitureController {

    @FXML
    private TableView<Voiture> voitureTable;
    @FXML
    private TableColumn<Voiture, Integer> idColumn;
    @FXML
    private TableColumn<Voiture, String> marqueColumn;
    @FXML
    private TableColumn<Voiture, String> modeleColumn;
    @FXML
    private TableColumn<Voiture, String> typeColumn;
    @FXML
    private TableColumn<Voiture, String> matriculeColumn; // Added matricule column
    @FXML
    private TableColumn<Voiture, Void> actionsColumn;
    @FXML
    private Button addButton; // "Ajouter une Voiture" button
    @FXML
    private TextField searchField; // Search field

    @FXML
    void handleModifierButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditVoiture.fxml"));
            Parent root = loader.load();

            EditVoitureController controller = loader.getController();
            Voiture selectedVoiture = voitureTable.getSelectionModel().getSelectedItem();

            if (selectedVoiture != null) {
                controller.setVoiture(selectedVoiture);

                // Switch to Edit Voiture screen
                Stage stage = (Stage) voitureTable.getScene().getWindow();
                stage.setScene(new Scene(root));
            } else {
                showAlert(Alert.AlertType.WARNING, "Please select a car to modify.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading Edit Voiture screen.");
        }
    }


    @FXML
    private VBox conducteurContainer; // Declare the VBox for conducteurContainer (for right section of SplitPane)

    private ObservableList<Voiture> voitureList = FXCollections.observableArrayList();
    private VoitureService voitureService;
    private PauseTransition searchDelay;

    @FXML
    private void initialize() {
        // Set the selection mode to single selection
        voitureTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Initialize the columns with the appropriate properties
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        marqueColumn.setCellValueFactory(new PropertyValueFactory<>("marque"));
        modeleColumn.setCellValueFactory(new PropertyValueFactory<>("modele"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        matriculeColumn.setCellValueFactory(new PropertyValueFactory<>("matricule"));

        // Set up action buttons for the actions column
        actionsColumn.setCellFactory(param -> new TableCell<Voiture, Void>() {
            private final Button updateButton = new Button("Update");
            private final Button deleteButton = new Button("Delete");
            private final Button blockButton = new Button("Block");
            private final HBox buttons = new HBox(10, updateButton, deleteButton, blockButton);

            {
                updateButton.setStyle("-fx-background-color: #00c9a7; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white;");
                blockButton.setStyle("-fx-background-color: #f0ad4e; -fx-text-fill: white;");

                // Update Button action: Fetch the item from the current row and pass it to handleUpdate
                updateButton.setOnAction(event -> {
                    Voiture voiture = getTableRow().getItem();
                    if (voiture != null) {
                        handleUpdate(voiture);
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Please select a car to update.");
                    }
                });

                // Delete and Block actions (already implemented)
                deleteButton.setOnAction(event -> handleDelete(getTableRow().getIndex()));
                blockButton.setOnAction(event -> handleBlock(getTableRow().getIndex()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });

        voitureService = new VoitureService();
        fetchDataFromDatabase();

        // Add action for Add Voiture button
        addButton.setOnAction(event -> navigateToAddVoiture());

        // Search functionality with delay for better user experience
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (searchDelay != null && searchDelay.getStatus() == javafx.animation.Animation.Status.RUNNING) {
                searchDelay.stop();
            }

            searchDelay = new PauseTransition(Duration.millis(500)); // Delay for 500ms
            searchDelay.setOnFinished(e -> filterList(newValue));
            searchDelay.play();
        });
    }

    // Fetch data from the database and populate the table
    private void fetchDataFromDatabase() {
        try {
            List<Voiture> voitures = voitureService.getAll();
            voitureList.clear();
            voitureList.addAll(voitures);
            voitureTable.setItems(voitureList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Filter the list based on the search query
    private void filterList(String searchQuery) {
        ObservableList<Voiture> filteredList = FXCollections.observableArrayList();

        if (searchQuery.isEmpty()) {
            voitureTable.setItems(voitureList);
        } else {
            for (Voiture voiture : voitureList) {
                if (voiture.getMarque().toLowerCase().contains(searchQuery.toLowerCase()) ||
                        voiture.getModele().toLowerCase().contains(searchQuery.toLowerCase())) {
                    filteredList.add(voiture);
                }
            }
            voitureTable.setItems(filteredList);
        }
    }

    // Handle update action
    private void handleUpdate(Voiture selectedVoiture) {
        if (selectedVoiture != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditVoiture.fxml"));
                Parent root = loader.load();
                EditVoitureController controller = loader.getController();
                controller.setVoiture(selectedVoiture);
                Stage stage = (Stage) voitureTable.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error loading Edit Voiture screen.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Please select a car to update.");
        }
    }

    // Handle delete action
    private void handleDelete(int index) {
        Voiture voiture = voitureTable.getItems().get(index);
        voitureTable.getItems().remove(voiture);

        try {
            voitureService.delete(voiture.getId());
            showAlert(Alert.AlertType.INFORMATION, "Voiture deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error deleting Voiture: " + e.getMessage());
        }
    }

    // Handle block action
    private void handleBlock(int index) {
        Voiture voiture = voitureTable.getItems().get(index);

        // Here, you can add the logic to "block" the voiture (e.g., change its status or update a field)
        // Assuming there is a boolean field `isBlocked` in the `Voiture` class, you can update that
        try {
            // Example: If the voiture is not already blocked, block it
            if (!voiture.isBlocked()) {
                voiture.setBlocked(true);  // Assuming a setBlocked method exists in Voiture
                voitureService.update(voiture);  // Update the blocked status in the database
                showAlert(Alert.AlertType.INFORMATION, "Voiture blocked successfully!");
            } else {
                showAlert(Alert.AlertType.WARNING, "This Voiture is already blocked.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error while blocking the voiture: " + e.getMessage());
        }
    }

    // Navigate to the add voiture screen
    private void navigateToAddVoiture() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/AddVoiture.fxml"));
            Stage stage = (Stage) addButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Navigate to the conducteur page
    @FXML
    private void navigateToConducteur() {
        try {
            // Load the Conducteur page dynamically
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Conducteur.fxml"));
            Parent conducteurRoot = loader.load();

            // Set the content into the conducteurContainer VBox
            conducteurContainer.getChildren().clear();  // Clear the previous content
            conducteurContainer.getChildren().add(conducteurRoot);  // Add new content dynamically
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Navigate to the statistics page
    @FXML
    private void navigateToStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StatisticsPage.fxml"));
            Parent root = loader.load();

            // Create a new Stage for the Statistics page
            Stage stage = new Stage();
            stage.setTitle("Statistics");

            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading statistics page: " + e.getMessage());
        }
    }

    // Show alert dialog with custom message
    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleVoirVoitureAction(ActionEvent actionEvent) {
        // Récupérer la voiture sélectionnée dans la TableView
        Voiture selectedVoiture = voitureTable.getSelectionModel().getSelectedItem();

        if (selectedVoiture != null) {
            // Extraire la marque et le modèle de la voiture sélectionnée
            String marque = selectedVoiture.getMarque();
            String modele = selectedVoiture.getModele();

            try {
                // Créer la chaîne de recherche pour Google Images et encoder les espaces et caractères spéciaux
                String searchQuery = marque + " " + modele;
                String encodedSearchQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString());

                // Créer l'URL de recherche d'image Google pour la voiture
                String googleSearchURL = "https://www.google.com/search?hl=en&tbm=isch&q=" + encodedSearchQuery;

                // Tenter d'ouvrir le navigateur avec l'URL de Google Images
                Desktop.getDesktop().browse(new URI(googleSearchURL));
            } catch (Exception e) {
                e.printStackTrace();
                // Afficher une alerte si l'ouverture du navigateur échoue
                showAlert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture du navigateur.");
            }
        } else {
            // Afficher une alerte si aucune voiture n'est sélectionnée
            showAlert(Alert.AlertType.WARNING, "Veuillez sélectionner une voiture.");
        }
    }

    @FXML
    private void handleModifierButton() {
        Voiture selectedVoiture = voitureTable.getSelectionModel().getSelectedItem();
        if (selectedVoiture != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditVoiture.fxml"));
                Parent root = loader.load();
                EditVoitureController controller = loader.getController();
                controller.setVoiture(selectedVoiture);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Modifier Voiture");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur lors du chargement de l'écran de modification.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Veuillez sélectionner une voiture à modifier.");
        }
    }

    @FXML
    private void handleExportPdf() {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("voitures_export.pdf"));
            document.open();
            document.add(new Paragraph("Liste des Voitures"));
            PdfPTable table = new PdfPTable(5); // ID, Marque, Modèle, Type, Matricule
            table.addCell("ID");
            table.addCell("Marque");
            table.addCell("Modèle");
            table.addCell("Type");
            table.addCell("Matricule");
            for (Voiture v : voitureTable.getItems()) {
                table.addCell(String.valueOf(v.getId()));
                table.addCell(v.getMarque());
                table.addCell(v.getModele());
                table.addCell(v.getType().toString());
                table.addCell(v.getMatricule());
            }
            document.add(table);
            document.close();
            showAlert(Alert.AlertType.INFORMATION, "PDF exporté avec succès dans le fichier voitures_export.pdf");
        } catch (DocumentException | java.io.IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur lors de l'export PDF : " + e.getMessage());
        }
    }

}