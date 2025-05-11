package gui;

import Entites.Reclamation;
import Entites.Reponse;
import Entites.utilisateur;
import services.ReclamationService;
import services.ReponseService;
import utils.GeoLocationService;
import utils.NotificationService;
import utils.ConfigService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Hyperlink;
import javafx.collections.FXCollections;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class ReclamationDetailsController {
    @FXML
    private Label reclamationId;
    @FXML
    private Label ticketIdLabel;
    @FXML
    private Label titreLabel;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label dateLabel;
    @FXML
    private Label statutLabel;
    @FXML
    private Label prioriteLabel;
    @FXML
    private Label categorieLabel;
    @FXML
    private Label graviteLabel;
    @FXML
    private Label adresseLabel;
    @FXML
    private Label latitudeLabel;
    @FXML
    private Label longitudeLabel;
    @FXML
    private ListView<String> reponsesListView;
    @FXML
    private TextArea newResponseField;
    @FXML
    private Button sendResponseButton;
    @FXML
    private VBox locationMapContainer;
    @FXML
    private Label creationDateLabel;
    @FXML
    private Label lastUpdateLabel;
    @FXML
    private Label elapsedTimeLabel;
    @FXML
    private Label estimatedResolutionLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ComboBox<String> statutComboBox;

    private Reclamation reclamation;
    private ReclamationService reclamationService;
    private ReponseService reponseService;
    private utilisateur currentUser;
    private ImageView mapImageView;

    @FXML
    public void initialize() {
        reclamationService = new ReclamationService();
        reponseService = new ReponseService();
        currentUser = MainController.getInstance().getCurrentUser();
        
        // Initialiser le ComboBox de statut
        if (statutComboBox != null) {
            statutComboBox.setItems(FXCollections.observableArrayList("ouvert", "en cours", "resolu", "rejete"));
            statutComboBox.setValue("en cours");
        }
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
        updateUI();
    }

    private void updateUI() {
        if (reclamation != null) {
            // Information de base
            reclamationId.setText("Réclamation #" + reclamation.getId());
            ticketIdLabel.setText("Ticket #" + reclamation.getTicketId());
            titreLabel.setText(reclamation.getTitre());
            descriptionArea.setText(reclamation.getDescription());
            dateLabel.setText(reclamation.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            statutLabel.setText(reclamation.getStatut());
            
            // Mettre à jour le ComboBox avec le statut actuel de la réclamation
            if (statutComboBox != null) {
                statutComboBox.setValue(reclamation.getStatut());
            }
            
            // Information supplémentaire
            prioriteLabel.setText(reclamation.getPriorite() + "/5");
            categorieLabel.setText(reclamation.getCategorie() != null ? reclamation.getCategorie() : "Non catégorisé");
            graviteLabel.setText(reclamation.getGravite() != null ? reclamation.getGravite() : "Non évaluée");
            
            // Géolocalisation
            adresseLabel.setText(reclamation.getAdresse() != null ? reclamation.getAdresse() : "Non spécifiée");
            latitudeLabel.setText(String.valueOf(reclamation.getLatitude()));
            longitudeLabel.setText(String.valueOf(reclamation.getLongitude()));
            
            // Initialiser la carte si les coordonnées sont disponibles
            if (reclamation.getLatitude() != 0 && reclamation.getLongitude() != 0 && locationMapContainer != null) {
                initializeLocationMap(reclamation.getLatitude(), reclamation.getLongitude());
            }
            
            // Suivi du traitement
            updateTrackingInfo();
            
            // Appliquer une couleur selon le statut
            applyStatusStyle();
            
            // Charger les réponses si elles existent
            loadResponses();
        }
    }
    
    private void initializeLocationMap(double latitude, double longitude) {
        try {
            // Clear any existing content
            locationMapContainer.getChildren().clear();
            
            // Create an interactive map using WebView and OpenStreetMap with Leaflet
            javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
            webView.setPrefSize(500, 250);
            javafx.scene.web.WebEngine webEngine = webView.getEngine();
            
            // Build the HTML for OpenStreetMap with Leaflet
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n")
                .append("  <title>Localisation de la réclamation</title>\n")
                .append("  <meta charset=\"utf-8\">\n")
                .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
                .append("  <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.css\"\n")
                .append("   integrity=\"sha512-xodZBNTC5n17Xt2atTPuE1HxjVMSvLVW9ocqUKLsCC5CXdbqCmblAshOMAS6/keqq/sMZMZ19scR4PsZChSR7A==\"\n")
                .append("   crossorigin=\"\"/>\n")
                .append("  <script src=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.js\"\n")
                .append("   integrity=\"sha512-XQoYMqMTK8LvdxXYG3nZ448hOEQiglfqkJs1NOQV44cWnUrBc8PkAOcXy20w0vlaXaVUearIOBhiXZ5V3ynxwA==\"\n")
                .append("   crossorigin=\"\"></script>\n")
                .append("  <style>\n")
                .append("    html, body, #map {\n")
                .append("      height: 100%;\n")
                .append("      width: 100%;\n")
                .append("      margin: 0;\n")
                .append("      padding: 0;\n")
                .append("    }\n")
                .append("  </style>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("  <div id=\"map\"></div>\n")
                .append("  <script>\n")
                .append("    // Initialize the map\n")
                .append("    var map = L.map('map').setView([" + latitude + ", " + longitude + "], 15);\n")
                .append("    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n")
                .append("      attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors'\n")
                .append("    }).addTo(map);\n")
                .append("\n")
                .append("    // Add a marker at the location\n")
                .append("    var marker = L.marker([" + latitude + ", " + longitude + "]).addTo(map);\n")
                .append("    marker.bindPopup('<b>Emplacement de la réclamation</b><br>" + reclamation.getAdresse() + "').openPopup();\n")
                .append("  </script>\n")
                .append("</body>\n")
                .append("</html>");
                
            // Load the HTML content
            webEngine.loadContent(htmlBuilder.toString());
            
            // Add the WebView to the container
            locationMapContainer.getChildren().add(webView);
            
            // Add a position label
            Label positionLabel = new Label(String.format("Position: %.6f, %.6f", latitude, longitude));
            positionLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666666;");
            locationMapContainer.getChildren().add(positionLabel);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de la carte: " + e.getMessage());
            e.printStackTrace();
            
            // Display an error message if map can't be loaded
            Label errorLabel = new Label("Impossible de charger la carte. Vérifiez votre connexion internet.");
            errorLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
            locationMapContainer.getChildren().add(errorLabel);
        }
    }
    
    private void updateTrackingInfo() {
        try {
            LocalDate creationDate = reclamation.getDate();
            creationDateLabel.setText(creationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            // Supposer que la dernière mise à jour était aujourd'hui
            LocalDate lastUpdate = LocalDate.now();
            lastUpdateLabel.setText(lastUpdate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            // Calculer le temps écoulé
            long daysElapsed = ChronoUnit.DAYS.between(creationDate, LocalDate.now());
            elapsedTimeLabel.setText(daysElapsed + " jours");
            
            // Estimer la date de résolution en fonction de la priorité
            int estimatedDays = calculateEstimatedResolutionDays();
            LocalDate estimatedResolution = creationDate.plusDays(estimatedDays);
            estimatedResolutionLabel.setText(estimatedResolution.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            // Calculer la progression
            double progress = calculateProgress(creationDate, estimatedResolution);
            progressBar.setProgress(progress);
            
            // Changer la couleur de la barre de progression selon l'état
            if (reclamation.getStatut().equalsIgnoreCase("resolu")) {
                progressBar.setStyle("-fx-accent: #4CAF50;"); // Vert
            } else if (progress > 0.8) {
                progressBar.setStyle("-fx-accent: #F44336;"); // Rouge (en retard)
            } else if (progress > 0.5) {
                progressBar.setStyle("-fx-accent: #FFA726;"); // Orange (attention)
            } else {
                progressBar.setStyle("-fx-accent: #2196F3;"); // Bleu (normal)
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des informations de suivi: " + e.getMessage());
        }
    }
    
    private int calculateEstimatedResolutionDays() {
        // Calcul basé sur la priorité et la gravité
        int baseDays = 14; // Par défaut, 2 semaines
        
        switch (reclamation.getPriorite()) {
            case 5: // Urgence maximale
                baseDays = 1;
                break;
            case 4:
                baseDays = 3;
                break;
            case 3:
                baseDays = 7;
                break;
            case 2:
                baseDays = 14;
                break;
            case 1:
                baseDays = 21;
                break;
        }
        
        // Modifier en fonction de la gravité
        if (reclamation.getGravite() != null) {
            if (reclamation.getGravite().equals("critique")) {
                baseDays = Math.max(1, baseDays / 2);
            } else if (reclamation.getGravite().equals("basse")) {
                baseDays = baseDays * 2;
            }
        }
        
        return baseDays;
    }
    
    private double calculateProgress(LocalDate start, LocalDate estimatedEnd) {
        if (reclamation.getStatut().equalsIgnoreCase("resolu")) {
            return 1.0;
        }
        
        LocalDate now = LocalDate.now();
        long totalDays = ChronoUnit.DAYS.between(start, estimatedEnd);
        long elapsedDays = ChronoUnit.DAYS.between(start, now);
        
        if (totalDays <= 0) {
            return 0.0;
        }
        
        double progress = (double) elapsedDays / totalDays;
        return Math.min(1.0, Math.max(0.0, progress));
    }
    
    private void applyStatusStyle() {
        // Appliquer une couleur selon le statut
        String status = reclamation.getStatut().toLowerCase();
        
        switch (status) {
            case "ouvert":
                statutLabel.setTextFill(Color.valueOf("#1a73e8")); // Bleu
                break;
            case "en cours":
                statutLabel.setTextFill(Color.valueOf("#FFA726")); // Orange
                break;
            case "resolu":
                statutLabel.setTextFill(Color.valueOf("#4CAF50")); // Vert
                break;
            case "rejete":
                statutLabel.setTextFill(Color.valueOf("#F44336")); // Rouge
                break;
            default:
                statutLabel.setTextFill(Color.BLACK);
                break;
        }
    }
    
    private void loadResponses() {
        reponsesListView.getItems().clear();
        
        // Récupérer les réponses mises à jour de la base de données
        List<Reponse> reponses = reponseService.getByReclamation(reclamation.getId());
        
        if (reponses != null && !reponses.isEmpty()) {
            // Mettre à jour la liste des réponses dans l'objet réclamation
            reclamation.setReponses(reponses);
            
            // Ajouter chaque réponse à la liste d'affichage
            for (Reponse reponse : reponses) {
                reponsesListView.getItems().add(formatResponse(reponse));
            }
        } else {
            reponsesListView.getItems().add("Aucune réponse disponible pour cette réclamation.");
        }
    }
    
    private String formatResponse(Reponse reponse) {
        String username = reponse.getAdmin() != null ? reponse.getAdmin().getUsername() : "Inconnu";
        String date = reponse.getDateReponse() != null ? 
                      reponse.getDateReponse().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : 
                      "Date inconnue";
                      
        // Mettre en forme pour une meilleure lisibilité
        return "• " + username + " (" + date + "):\n  " + reponse.getMessage();
    }
    
    @FXML
    public void handleSendResponse() {
        if (newResponseField.getText().trim().isEmpty()) {
            showAlert("Veuillez saisir une réponse avant d'envoyer.");
            return;
        }
        
        try {
            // Vérifier si l'utilisateur actuel est défini
            if (currentUser == null) {
                // Créer un utilisateur par défaut
                currentUser = new utilisateur();
                currentUser.setId(1);
                currentUser.setUsername("Utilisateur");
                currentUser.setEmail("utilisateur@exemple.com");
                currentUser.setRole(Entites.UtilisateurRole.CLIENT);
                currentUser.setPrenom("Utilisateur");
                currentUser.setNom("Anonyme");
                
                System.out.println("Création d'un utilisateur par défaut pour l'envoi de réponse à une réclamation");
            }
            
            // Récupérer le message et le statut
            String message = newResponseField.getText().trim();
            String nouveauStatut = statutComboBox.getValue();
            String ancienStatut = reclamation.getStatut();
            
            // Créer et enregistrer la réponse
            Reponse reponse = new Reponse();
            reponse.setMessage(message);
            reponse.setDateReponse(LocalDate.now());
            reponse.setReclamation(reclamation);
            reponse.setAdmin(currentUser);
            
            // Enregistrer la réponse dans la base de données
            reponseService.ajouter(reponse);
            System.out.println("Réponse ajoutée avec ID: " + reponse.getId());
            
            // Mettre à jour le statut de la réclamation
            reclamation.setStatut(nouveauStatut);
            reclamationService.modifier(reclamation);
            
            // Effacer le champ de réponse
            newResponseField.clear();
            
            // Recharger la réclamation et les réponses
            reclamation = reclamationService.getById(reclamation.getId());
            loadResponses();
            updateUI();
            
            // Message de confirmation avec le changement de statut
            String confirmationMessage = "Réponse envoyée avec succès !";
            if (!ancienStatut.equals(nouveauStatut)) {
                confirmationMessage += "\nLe statut de la réclamation a été mis à jour de '" + 
                                      ancienStatut + "' à '" + nouveauStatut + "'.";
                                  
                // Mettre à jour les statistiques de la page d'accueil
                try {
                    // Essayer de récupérer le contrôleur de la page d'accueil via MainController
                    if (MainController.getInstance() != null) {
                        // Demander à MainController de rafraîchir les statistiques
                        MainController.getInstance().refreshReclamationStats();
                        System.out.println("Statistiques des réclamations mises à jour après changement de statut");
                    }
                } catch (Exception e) {
                    System.err.println("Impossible de mettre à jour les statistiques: " + e.getMessage());
                }
            }
            showAlert(confirmationMessage);
        } catch (Exception e) {
            showError("Erreur lors de l'envoi de la réponse", e);
        }
    }

    @FXML
    private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add_reclamation.fxml"));
            Parent root = loader.load();
            
            // Utiliser directement MainController car setupForEdit n'est peut-être pas disponible
            // dans ReclamationController
            MainController.getInstance().showEditReclamation(reclamation);
            
            // Fermer cette fenêtre
            closeWindow();
        } catch (Exception e) {
            showError("Impossible d'ouvrir la fenêtre d'édition", e);
        }
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                reclamationService.supprimer(reclamation.getId());
                closeWindow();
            } catch (Exception e) {
                showError("Erreur lors de la suppression", e);
            }
        }
    }

    @FXML
    private void handleBack() {
        closeWindow();
    }
    
    @FXML
    private void handleContactAdmin() {
        try {
            // Récupérer l'utilisateur actuel
            currentUser = MainController.getInstance().getCurrentUser();
            
            if (currentUser == null) {
                // Créer un utilisateur par défaut au lieu d'afficher une erreur
                currentUser = new utilisateur();
                currentUser.setId(1);
                currentUser.setUsername("Utilisateur");
                currentUser.setEmail("utilisateur@exemple.com");
                currentUser.setRole(Entites.UtilisateurRole.CLIENT);
                currentUser.setPrenom("Utilisateur");
                currentUser.setNom("Anonyme");
                
                System.out.println("Création d'un utilisateur par défaut pour l'envoi d'email de réclamation");
            }
            
            // Envoyer un email à l'administrateur avec le flag isResolutionRequest à true
            boolean emailSent = utils.NotificationService.envoyerEmailReclamationAdmin(currentUser, reclamation, true);
            
            if (emailSent) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Demande traitée");
                alert.setHeaderText(null);
                
                alert.setContentText("La demande de résolution pour la réclamation #" + reclamation.getTicketId() + 
                                   " a été traitée avec succès.\n\n" +
                                   "L'administrateur a été notifié et recevra les détails de cette réclamation.");
                alert.getDialogPane().setPrefSize(400, 150);
                alert.showAndWait();
            } else {
                showAlert("Erreur lors du traitement de la demande. Veuillez contacter directement l'administrateur.");
            }
            
        } catch (Exception e) {
            // Pour toutes les erreurs
            showError("Erreur lors du traitement de la réclamation", e);
        }
    }
    
    private void closeWindow() {
        // Récupérer la fenêtre actuelle et la fermer
        Stage stage = (Stage) titreLabel.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message + " : " + e.getMessage());
        alert.showAndWait();
    }
} 