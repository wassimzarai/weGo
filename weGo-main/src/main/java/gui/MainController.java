package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import Entites.Reclamation;
import Entites.utilisateur;
import javafx.scene.control.Alert;
import Entites.UtilisateurRole;

public class MainController {
    @FXML
    private StackPane mainContainer;
    
    private static MainController instance;
    private utilisateur currentUser;
    
    // Constructeur qui définit l'instance
    public MainController() {
        synchronized(MainController.class) {
            if (instance == null) {
                instance = this;
                System.out.println("MainController instance créée via constructeur");
            }
        }
    }
    
    @FXML
    public void initialize() {
        synchronized(MainController.class) {
            instance = this;
            System.out.println("MainController instance initialisée via FXML");
        }
        
        // Set a default user for testing
        setupDefaultUser();
        
        // Show the home page initially instead of reclamations
        showHomePage();
    }
    
    private void setupDefaultUser() {
        // Creating a default user for testing purposes
        currentUser = new utilisateur();
        currentUser.setId(1);
        currentUser.setUsername("aycem");
        currentUser.setPassword("password123");
        currentUser.setEmail("aycembougattaya357@gmail.com");
        currentUser.setRole(UtilisateurRole.CLIENT);
        currentUser.setPrenom("Aycem");
        currentUser.setNom("Bougattaya");
    }
    
    public static MainController getInstance() {
        // Si l'instance n'existe pas, on en crée une nouvelle (sans conteneur)
        if (instance == null) {
            instance = new MainController();
            System.out.println("MainController instance créée via getInstance");
        }
        return instance;
    }
    
    public void showHomePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/home_page.fxml"));
            Parent view = loader.load();

            if (mainContainer != null) {
                mainContainer.getChildren().clear();
                mainContainer.getChildren().add(view);
            } else {
                // Si mainContainer est null, ouvrir dans une nouvelle fenêtre
                openInNewStage(view, "Page d'accueil");
            }
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
            
            if (mainContainer != null) {
                mainContainer.getChildren().clear();
                mainContainer.getChildren().add(view);
            } else {
                // Si mainContainer est null, ouvrir dans une nouvelle fenêtre
                openInNewStage(view, "Mes Réclamations");
            }
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
            
            if (mainContainer != null) {
                mainContainer.getChildren().clear();
                mainContainer.getChildren().add(view);
            } else {
                // Si mainContainer est null, ouvrir dans une nouvelle fenêtre
                openInNewStage(view, "Mes Avis");
            }
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
            
            if (mainContainer != null) {
                mainContainer.getChildren().clear();
                mainContainer.getChildren().add(view);
            } else {
                // Si mainContainer est null, ouvrir dans une nouvelle fenêtre
                openInNewStage(view, "Tableau de bord");
            }
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
            
            if (mainContainer != null) {
                mainContainer.getChildren().clear();
                mainContainer.getChildren().add(view);
            } else {
                // Si mainContainer est null, ouvrir dans une nouvelle fenêtre
                openInNewStage(view, "Détails de la réclamation");
            }
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
            
            if (mainContainer != null) {
                mainContainer.getChildren().clear();
                mainContainer.getChildren().add(view);
            } else {
                // Si mainContainer est null, ouvrir dans une nouvelle fenêtre
                openInNewStage(view, "Nouvelle réclamation");
            }
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
            
            if (mainContainer != null) {
                mainContainer.getChildren().clear();
                mainContainer.getChildren().add(view);
            } else {
                // Si mainContainer est null, ouvrir dans une nouvelle fenêtre
                openInNewStage(view, "Modifier réclamation");
            }
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
    
    public utilisateur getCurrentUser() {
        return currentUser;
    }
    
    public void setCurrentUser(utilisateur user) {
        this.currentUser = user;
    }

    public boolean isAdmin() {
        if (currentUser != null && currentUser.getRole() == UtilisateurRole.ADMIN) {
            return true;
        }
        return false;
    }
    
    /**
     * Ouvre une vue dans une nouvelle fenêtre
     * Utilisé quand mainContainer est null (instance créée programmatiquement)
     */
    private void openInNewStage(Parent view, String title) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle(title);
        stage.setScene(new javafx.scene.Scene(view));
        stage.show();
    }

    /**
     * Rafraîchit les statistiques des réclamations sur la page d'accueil
     * Méthode appelée après qu'un statut de réclamation a été modifié
     */
    public void refreshReclamationStats() {
        try {
            // Vérifier si nous sommes sur la page d'accueil
            if (mainContainer != null && !mainContainer.getChildren().isEmpty()) {
                // Essayer de récupérer le contrôleur HomeController actuel
                for (javafx.scene.Node node : mainContainer.getChildren()) {
                    if (node.getId() != null && node.getId().equals("mainAnchorPane")) {
                        // Nous sommes probablement sur la page d'accueil
                        // Recharger complètement la page d'accueil
                        showHomePage();
                        System.out.println("Page d'accueil rechargée pour mettre à jour les statistiques");
                        return;
                    }
                }
            }
            
            // Si nous ne sommes pas sur la page d'accueil, pas besoin de rafraîchir
            System.out.println("Mise à jour des statistiques différée (page d'accueil non affichée)");
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des statistiques: " + e.getMessage());
        }
    }
} 