package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import entities.Reclamation;
import entities.User;
import javafx.scene.control.Alert;

public class MainController {
    @FXML
    private StackPane mainContainer;
    
    private static MainController instance;
    private User currentUser;
    
    @FXML
    public void initialize() {
        instance = this;
        
        // Set a default user for testing
        setupDefaultUser();
        
        // Show the home page initially instead of reclamations
        showHomePage();
    }
    
    private void setupDefaultUser() {
        // Creating a default user for testing purposes
        currentUser = new User();
        currentUser.setId(1);
        currentUser.setUsername("aycem");
        currentUser.setPassword("password123");
        currentUser.setEmail("aycembougattaya357@gmail.com");
        currentUser.setRole("USER");
        currentUser.setPrenom("Aycem");
        currentUser.setNom("Bougattaya");
    }
    
    public static MainController getInstance() {
        return instance;
    }
    
    public void showHomePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/home_page.fxml"));
            Parent view = loader.load();


            mainContainer.getChildren().clear();
            mainContainer.getChildren().add(view);

        } catch (IOException e) {
            showError("Erreur lors du chargement de la page d'accueil", e);
        }
    }
    
    public void showMyReclamations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/my_reclamations.fxml"));
            Parent view = loader.load();
            
            ReclamationController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.initialize();
            
            mainContainer.getChildren().clear();
            mainContainer.getChildren().add(view);
            
        } catch (IOException e) {
            showError("Erreur lors du chargement de la liste des réclamations", e);
        }
    }
    
    public void showAvisList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/avis_list.fxml"));
            Parent view = loader.load();
            
            // Initialiser le contrôleur AvisController
            AvisController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            mainContainer.getChildren().clear();
            mainContainer.getChildren().add(view);
            
        } catch (IOException e) {
            // Si le fichier FXML n'existe pas encore, on peut afficher un message temporaire
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Fonctionnalité en cours de développement");
            alert.setHeaderText(null);
            alert.setContentText("La liste des avis est en cours de développement et sera disponible prochainement!");
            alert.showAndWait();
            
            // Retourner à la page d'accueil
            showHomePage();
        }
    }
    
    public void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
            Parent view = loader.load();
            
            DashboardController controller = loader.getController();
            
            mainContainer.getChildren().clear();
            mainContainer.getChildren().add(view);
            
        } catch (IOException e) {
            showError("Erreur lors du chargement du tableau de bord", e);
        }
    }
    
    public void showReclamationDetails(Reclamation reclamation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/reclamation_details.fxml"));
            Parent view = loader.load();
            
            ReclamationDetailsController controller = loader.getController();
            controller.setReclamation(reclamation);
            
            mainContainer.getChildren().clear();
            mainContainer.getChildren().add(view);
            
        } catch (IOException e) {
            showError("Erreur lors du chargement des détails de la réclamation", e);
        }
    }
    
    public void showAddReclamation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add_reclamation.fxml"));
            Parent view = loader.load();
            
            ReclamationController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setEditMode(false);
            
            mainContainer.getChildren().clear();
            mainContainer.getChildren().add(view);
            
        } catch (IOException e) {
            showError("Erreur lors du chargement du formulaire d'ajout", e);
        }
    }
    
    public void showEditReclamation(Reclamation reclamation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add_reclamation.fxml"));
            Parent view = loader.load();
            
            ReclamationController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setEditMode(true);
            controller.setReclamationToEdit(reclamation);
            controller.initializeFields(reclamation);
            
            mainContainer.getChildren().clear();
            mainContainer.getChildren().add(view);
            
        } catch (IOException e) {
            showError("Erreur lors du chargement du formulaire de modification", e);
        }
    }
    
    public void showReclamationsList() {
        showMyReclamations();
    }
    
    public void showUserProfile() {
        // Fonctionnalité d'édition de profil supprimée
        // On retourne à la page d'accueil
        showHomePage();
    }
    
    private void showError(String message, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message + ": " + e.getMessage());
        alert.showAndWait();
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
} 