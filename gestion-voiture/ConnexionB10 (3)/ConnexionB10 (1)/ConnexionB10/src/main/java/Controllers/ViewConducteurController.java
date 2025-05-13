package Controllers;

import Entities.Conducteur;
import Service.ConducteurService;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ViewConducteurController {

    @FXML private TableView<Conducteur> conducteurTable;
    @FXML private TableColumn<Conducteur, Integer> idColumn;
    @FXML private TableColumn<Conducteur, Integer> idVoitureColumn;
    @FXML private TableColumn<Conducteur, String> dateDepartColumn;
    @FXML private TableColumn<Conducteur, String> dateArretColumn;
    @FXML private TableColumn<Conducteur, String> nbDaysColumn;
    @FXML private TableColumn<Conducteur, Void> actionsColumn;
    @FXML private Button addButton;

    @FXML private RadioButton sortByIdAsc;
    @FXML private RadioButton sortByIdDesc;
    @FXML private RadioButton sortByIdVoitureAsc;
    @FXML private RadioButton sortByIdVoitureDesc;

    private ConducteurService conducteurService;

    private static ViewConducteurController instance;

    public ViewConducteurController() {
        instance = this;
    }

    public static void refreshConducteurTable() {
        if (instance != null) {
            instance.reloadConducteurs();
        }
    }

    @FXML
    public void initialize() {
        // Initialize the columns for the TableView
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idVoitureColumn.setCellValueFactory(new PropertyValueFactory<>("idVoiture"));
        dateDepartColumn.setCellValueFactory(new PropertyValueFactory<>("dateDepart"));
        dateArretColumn.setCellValueFactory(new PropertyValueFactory<>("dateArret"));

        // Calculate the number of days between date_depart and date_arret
        nbDaysColumn.setCellValueFactory(param -> {
            if (param.getValue().getDateDepart() != null && param.getValue().getDateArret() != null) {
                long nbDays = ChronoUnit.DAYS.between(param.getValue().getDateDepart(), param.getValue().getDateArret());
                return new javafx.beans.property.SimpleStringProperty(String.valueOf(nbDays));
            } else {
                return new javafx.beans.property.SimpleStringProperty("N/A"); // Handle null dates
            }
        });

        // Set up the action buttons for Update/Delete in each row
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button updateButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttons = new HBox(10, updateButton, deleteButton);

            {
                updateButton.setOnAction(event -> handleUpdate(getIndex()));
                deleteButton.setOnAction(event -> handleDelete(getIndex()));
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

        // Initialize the service and populate the table
        conducteurService = new ConducteurService();
        try {
            List<Conducteur> conducteurs = conducteurService.getAll();
            conducteurTable.getItems().setAll(conducteurs);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors du chargement des conducteurs: " + e.getMessage());
        }

        // Action for the "Ajouter Conducteur" button
        addButton.setOnAction(e -> navigateToAddConducteur());

        // Handle sorting
        ToggleGroup sortGroup = new ToggleGroup();
        sortByIdAsc.setToggleGroup(sortGroup);
        sortByIdDesc.setToggleGroup(sortGroup);
        sortByIdVoitureAsc.setToggleGroup(sortGroup);
        sortByIdVoitureDesc.setToggleGroup(sortGroup);

        // Listen for selection changes
        sortGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> handleSort());
    }

    private void handleSort() {
        if (sortByIdAsc.isSelected()) {
            // Sort by ID Ascending
            conducteurTable.getItems().sort((c1, c2) -> Integer.compare(c1.getId(), c2.getId()));
        } else if (sortByIdDesc.isSelected()) {
            // Sort by ID Descending
            conducteurTable.getItems().sort((c1, c2) -> Integer.compare(c2.getId(), c1.getId()));
        } else if (sortByIdVoitureAsc.isSelected()) {
            // Sort by ID Voiture Ascending
            conducteurTable.getItems().sort((c1, c2) -> Integer.compare(c1.getIdVoiture(), c2.getIdVoiture()));
        } else if (sortByIdVoitureDesc.isSelected()) {
            // Sort by ID Voiture Descending
            conducteurTable.getItems().sort((c1, c2) -> Integer.compare(c2.getIdVoiture(), c1.getIdVoiture()));
        }
    }

    // Handle Update (Modify) button click
    private void handleUpdate(int index) {
        Conducteur c = conducteurTable.getItems().get(index);
        showAlert(Alert.AlertType.INFORMATION, "Modifier le conducteur ID " + c.getId());
    }

    // Handle Delete button click
    private void handleDelete(int index) {
        Conducteur c = conducteurTable.getItems().get(index);
        try {
            boolean ok = conducteurService.delete(c.getId());
            if (ok) {
                conducteurTable.getItems().remove(index);
                showAlert(Alert.AlertType.INFORMATION, "Conducteur supprimé.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec de la suppression.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur SQL : " + ex.getMessage());
        }
    }

    // Navigate to the Add Conducteur page
    private void navigateToAddConducteur() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddConducteur.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Conducteur");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur lors de la navigation : " + e.getMessage());
        }
    }

    // Show alerts for errors, information, etc.
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void reloadConducteurs() {
        try {
            List<Conducteur> conducteurs = conducteurService.getAll();
            conducteurTable.getItems().setAll(conducteurs);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors du rafraîchissement des conducteurs: " + e.getMessage());
        }
    }
}
