package gui;

import Entites.Avis;
import Entites.UtilisateurRole;
import Entites.utilisateur;
import services.AvisService;
import services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;
import java.time.LocalDate;
import java.sql.Date;
import java.sql.SQLException;
import javafx.scene.text.Text;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.stage.Modality;

public class AvisController {
    @FXML
    private TableView<Avis> avisTable;
    @FXML
    private TableColumn<Avis, Integer> idColumn;
    @FXML
    private TableColumn<Avis, String> noteColumn;
    @FXML
    private TableColumn<Avis, String> commentaireColumn;
    @FXML
    private TableColumn<Avis, LocalDate> dateColumn;
    @FXML
    private TableColumn<Avis, String> userColumn;
    @FXML
    private TableColumn<Avis, String> satisfactionColumn;
    
    @FXML
    private Button addAvisButton;
    
    @FXML
    private Text tableStar1, tableStar2, tableStar3, tableStar4, tableStar5;
    
    @FXML
    private Label tableRatingLabel;

    @FXML
    private TextField searchAvisField;
    
    @FXML
    private ComboBox<String> searchAvisFilterCombo;

    private final AvisService avisService;
    private final UserService userService;
    private final ObservableList<Avis> avisList;
    private final ObservableList<Avis> originalAvisList;
    
    private int currentRating = 0;
    private final String STAR_INACTIVE = "#dddddd";
    private final String STAR_ACTIVE = "#FFC107"; // Yellow/Gold

    private utilisateur currentUser;

    public AvisController() {
        avisService = new AvisService();
        userService = new UserService();
        avisList = FXCollections.observableArrayList();
        originalAvisList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        try {
            // Configurer les colonnes
            setupControls();
            
            // Setup context menu
            setupContextMenu();
            
            // Setup table selection listener
            setupTableSelectionListener();
            
            // Initialize search filter
            if (searchAvisFilterCombo != null) {
                searchAvisFilterCombo.getSelectionModel().select(0); // "Tous" par défaut
            }
            
            // Essayer de charger les avis
            try {
                loadAvis();
            } catch (Exception e) {
                showError("Avertissement", "Impossible de charger les avis: " + e.getMessage());
            }
        } catch (Exception e) {
            showError("Erreur d'initialisation", "Impossible d'initialiser l'interface: " + e.getMessage());
        }
    }

    private void setupControls() {
        // Initialize columns
        idColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        noteColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRating()));
        commentaireColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getComment()));
        dateColumn.setCellValueFactory(cellData -> {
            Avis avis = cellData.getValue();
            if (avis != null && avis.getDate() != null) {
                Date sqlDate = avis.getDate();
                return new SimpleObjectProperty<>(sqlDate.toLocalDate());
            }
            return new SimpleObjectProperty<>(null);
        });
        userColumn.setCellValueFactory((CellDataFeatures<Avis, String> cellData) -> {
            Avis avis = cellData.getValue();
            String username = "";
            if (avis != null && avis.getUser() != null) {
                username = avis.getUser().getUsername();
            }
            return new SimpleStringProperty(username);
        });
        
        // Configure the satisfaction column with stars
        satisfactionColumn.setCellValueFactory(cellData -> {
            Avis avis = cellData.getValue();
            int rating = 0;
            try {
                rating = Integer.parseInt(avis.getRating());
            } catch (NumberFormatException e) {
                // Handle if rating is not a number
            }
            
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < rating; i++) {
                stars.append("★");
            }
            return new SimpleStringProperty(stars.toString());
        });
        
        // Set the cell factory for the satisfaction column to style the stars
        satisfactionColumn.setCellFactory(column -> new TableCell<Avis, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #FFC107; -fx-font-size: 14px;");
                }
            }
        });
    }
    
    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem updateItem = new MenuItem("Modifier");
        updateItem.setOnAction(event -> handleUpdateAvis());
        
        MenuItem deleteItem = new MenuItem("Supprimer");
        deleteItem.setOnAction(event -> handleDeleteAvis());
        
        contextMenu.getItems().addAll(updateItem, deleteItem);
        
        // Set context menu on row selection
        avisTable.setRowFactory(tv -> {
            TableRow<Avis> row = new TableRow<>();
            
            // Add context menu
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });
            
            // Add double-click handler
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleUpdateAvis();
                }
            });
            
            return row;
        });
    }
    
    private void setupTableSelectionListener() {
        avisTable.getSelectionModel().selectedItemProperty().addListener(
            (ObservableValue<? extends Avis> obs, Avis oldSelection, Avis newSelection) -> {
                if (newSelection != null) {
                    try {
                        int rating = Integer.parseInt(newSelection.getRating());
                        highlightTableStars(rating);
                        updateTableRatingLabel(rating);
                    } catch (NumberFormatException e) {
                        highlightTableStars(0);
                        updateTableRatingLabel(0);
                    }
                } else {
                    highlightTableStars(0);
                    updateTableRatingLabel(0);
                }
            }
        );
    }
    
    private void highlightTableStars(int rating) {
        tableStar1.setStyle("-fx-font-size: 30px; -fx-fill: " + (rating >= 1 ? STAR_ACTIVE : STAR_INACTIVE));
        tableStar2.setStyle("-fx-font-size: 30px; -fx-fill: " + (rating >= 2 ? STAR_ACTIVE : STAR_INACTIVE));
        tableStar3.setStyle("-fx-font-size: 30px; -fx-fill: " + (rating >= 3 ? STAR_ACTIVE : STAR_INACTIVE));
        tableStar4.setStyle("-fx-font-size: 30px; -fx-fill: " + (rating >= 4 ? STAR_ACTIVE : STAR_INACTIVE));
        tableStar5.setStyle("-fx-font-size: 30px; -fx-fill: " + (rating >= 5 ? STAR_ACTIVE : STAR_INACTIVE));
    }
    
    private void updateTableRatingLabel(int rating) {
        switch (rating) {
            case 0:
                tableRatingLabel.setText("Sélectionnez un avis dans le tableau");
                break;
            case 1:
                tableRatingLabel.setText("Très insatisfait");
                break;
            case 2:
                tableRatingLabel.setText("Insatisfait");
                break;
            case 3:
                tableRatingLabel.setText("Correct");
                break;
            case 4:
                tableRatingLabel.setText("Satisfait");
                break;
            case 5:
                tableRatingLabel.setText("Très satisfait");
                break;
        }
    }

    private void loadAvis() throws Exception {
        avisList.clear();
        originalAvisList.clear();
        
        avisList.addAll(avisService.getAll());
        originalAvisList.addAll(avisList);
        
        avisTable.setItems(avisList);
    }
    
    @FXML
    private void handleUpdateAvis() {
        Avis selectedAvis = avisTable.getSelectionModel().getSelectedItem();
        if (selectedAvis == null) {
            showError("Sélection requise", "Veuillez sélectionner un avis à modifier");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/avis_form.fxml"));
            Parent root = loader.load();
            
            AvisFormController formController = loader.getController();
            formController.setAvisToEdit(selectedAvis);
            formController.setCurrentUser(currentUser);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier Avis");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            
            stage.showAndWait();
            
            // Refresh the table after editing
            try {
                loadAvis();
            } catch (Exception e) {
                System.err.println("Erreur lors du rechargement des avis: " + e.getMessage());
            }
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleDeleteAvis() {
        Avis selectedAvis = avisTable.getSelectionModel().getSelectedItem();
        if (selectedAvis == null) {
            showError("Sélection requise", "Veuillez sélectionner un avis à supprimer");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet avis ?");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                avisService.supprimer(selectedAvis.getId());
                showInfo("Succès", "L'avis a été supprimé avec succès");
                
                // Refresh the table
                loadAvis();
            } catch (Exception e) {
                showError("Erreur de suppression", "Impossible de supprimer l'avis: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void backToHome() {
        try {
            // Vérifier si l'instance de MainController existe
            if (MainController.getInstance() == null) {
                // Si non, charger directement la vue home_page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/home_page.fxml"));
                Parent view = loader.load();
                
                // Utiliser la scène actuelle pour afficher la vue
                Scene currentScene = avisTable.getScene();
                if (currentScene != null) {
                    currentScene.setRoot(view);
                } else {
                    // Créer une nouvelle scène si nécessaire
                    Scene scene = new Scene(view);
                    Stage stage = (Stage) avisTable.getScene().getWindow();
                    stage.setScene(scene);
                    stage.show();
                }
            } else {
                // Si oui, utiliser la méthode normale
        MainController.getInstance().showHomePage();
            }
            System.out.println("Retour à la page d'accueil...");
        } catch (Exception e) {
            e.printStackTrace();
            // Afficher une alerte en cas d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de revenir à la page d'accueil: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void showAddAvis() {
        try {
            // Charger le formulaire d'avis
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/avis_form.fxml"));
            Parent root = loader.load();
            
            // Configurer le contrôleur
            AvisFormController controller = loader.getController();
            
            // Passer l'utilisateur courant au contrôleur
            if (currentUser == null) {
                // Si aucun utilisateur n'est connecté, créer un utilisateur par défaut
                currentUser = new utilisateur();
                currentUser.setId(1);
                currentUser.setUsername("Utilisateur");
                currentUser.setEmail("utilisateur@exemple.com");
                currentUser.setRole(UtilisateurRole.CLIENT);
                System.out.println("Création d'un utilisateur par défaut pour l'affichage du formulaire d'avis");
            }
            controller.setCurrentUser(currentUser);
            
            // Créer une nouvelle scène
            Stage stage = new Stage();
            stage.setTitle("Ajouter un avis");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            
            // Montrer la fenêtre et attendre qu'elle soit fermée
            stage.showAndWait();
            
            // Après la fermeture de la fenêtre, recharger les avis
            try {
                loadAvis();
            } catch (Exception e) {
                showError("Erreur", "Impossible de recharger les avis: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire d'avis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setCurrentUser(utilisateur utilisateur) {
        this.currentUser = utilisateur;
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Handle search functionality for avis
     */
    @FXML
    public void handleSearchAvis() {
        if (searchAvisField == null) {
            return;
        }
        
        String searchText = searchAvisField.getText().toLowerCase().trim();
        String filterOption = null;
        
        if (searchAvisFilterCombo != null) {
            filterOption = searchAvisFilterCombo.getValue();
        }
        
        if (filterOption == null) {
            filterOption = "Tous"; // Option par défaut
        }
        
        // Si le texte de recherche est vide, afficher tous les avis
        if (searchText.isEmpty()) {
            avisTable.setItems(originalAvisList);
            avisTable.setPlaceholder(new Label("Aucun avis disponible"));
            return;
        }
        
        // Filtrer les avis selon le texte de recherche et l'option de filtre
        ObservableList<Avis> filteredList = FXCollections.observableArrayList();
        
        for (Avis avis : originalAvisList) {
            boolean matches = false;
            
            switch (filterOption) {
                case "Par commentaire":
                    matches = avis.getComment() != null && 
                             avis.getComment().toLowerCase().contains(searchText);
                    break;
                case "Par note":
                    matches = avis.getRating() != null && 
                             avis.getRating().toLowerCase().contains(searchText);
                    break;
                case "Par date":
                    matches = avis.getDate() != null && 
                             avis.getDate().toString().contains(searchText);
                    break;
                case "Par utilisateur":
                    matches = avis.getUser() != null && avis.getUser().getUsername() != null &&
                             avis.getUser().getUsername().toLowerCase().contains(searchText);
                    break;
                case "Tous":
                default:
                    matches = (avis.getComment() != null && 
                              avis.getComment().toLowerCase().contains(searchText)) ||
                             (avis.getRating() != null && 
                              avis.getRating().toLowerCase().contains(searchText)) ||
                             (avis.getDate() != null && 
                              avis.getDate().toString().contains(searchText)) ||
                             (avis.getUser() != null && avis.getUser().getUsername() != null &&
                              avis.getUser().getUsername().toLowerCase().contains(searchText));
                    break;
            }
            
            if (matches) {
                filteredList.add(avis);
            }
        }
        
        // Mettre à jour le TableView avec les résultats filtrés
        avisTable.setItems(filteredList);
        
        // Afficher un message si aucun résultat
        if (filteredList.isEmpty()) {
            avisTable.setPlaceholder(new Label("Aucun avis trouvé pour cette recherche"));
        }
        
        // Si nous avons des résultats mais aucune ligne sélectionnée, sélectionner la première
        if (!filteredList.isEmpty() && avisTable.getSelectionModel().getSelectedItem() == null) {
            avisTable.getSelectionModel().selectFirst();
        }
    }
} 