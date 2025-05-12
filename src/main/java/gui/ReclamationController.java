package gui;

import entities.Reclamation;
import entities.User;
import services.ReclamationService;
import services.UserService;
import utils.NLPAnalyzer;
import utils.GeoLocationService;
import utils.ChatBotService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.List;

public class ReclamationController {
    @FXML
    private VBox reclamationsContainer;
    
    // Form components traditionnelles
    @FXML
    private TextField titreField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private Button submitButton;
    
    // Nouveaux composants pour le formulaire amélioré
    @FXML
    private DatePicker dateField;
    @FXML
    private TextField adresseField;
    @FXML
    private TextField latitudeField;
    @FXML
    private TextField longitudeField;
    @FXML
    private CheckBox useCurrentLocation;
    @FXML
    private Button btnChercher;
    @FXML
    private TextField categorieField;
    @FXML
    private TextField graviteField;
    @FXML
    private TextField prioriteField;
    @FXML
    private Text analyseTexte;
    @FXML
    private TextArea assistantText;
    @FXML
    private TextField questionField;
    @FXML
    private Button btnQuestion;
    @FXML
    private Button btnAnalyser;
    @FXML
    private Button btnAjouter;
    @FXML
    private Button BackHome;
    @FXML
    private VBox mapContainer;
    
    // Details panel components
    @FXML
    private Label detailTitreLabel;
    @FXML
    private TextArea detailDescriptionArea;
    @FXML
    private Label detailDateLabel;
    @FXML
    private Label detailStatutLabel;
    
    // Table view components (for my_reclamations.fxml)
    @FXML
    private TableView<Reclamation> reclamationTable;
    @FXML
    private TableColumn<Reclamation, Integer> idColumn;
    @FXML
    private TableColumn<Reclamation, String> titreColumn;
    @FXML
    private TableColumn<Reclamation, LocalDate> dateColumn;
    @FXML
    private TableColumn<Reclamation, String> statutColumn;
    @FXML
    private TableColumn<Reclamation, Void> actionColumn;

    // Add these new fields for search functionality
    @FXML
    private TextField searchReclamationField;
    
    @FXML
    private ComboBox<String> searchFilterCombo;
    
    // Original reclamation list to keep track of all items before filtering
    private ObservableList<Reclamation> originalReclamationList;

    private ReclamationService reclamationService;
    private UserService userService;
    private ObservableList<Reclamation> reclamationList;
    private Reclamation reclamationToEdit;
    private Stage currentStage;
    private boolean isEditMode = false;
    private User currentUser;
    private double currentLatitude = 36.8065; // Tunis par défaut
    private double currentLongitude = 10.1815;
    private ImageView mapImageView;

    @FXML
    public void initialize() {
        reclamationService = new ReclamationService();
        userService = new UserService();
        reclamationList = FXCollections.observableArrayList();
        originalReclamationList = FXCollections.observableArrayList();
        
        if (reclamationTable != null) {
            initializeTableView();
        }
        
        if (reclamationsContainer != null) {
            loadReclamations();
        }
        
        if (submitButton != null) {
            submitButton.setText(isEditMode ? "Modifier" : "Ajouter");
        }
        
        // Initialize search filter combo box if it exists
        if (searchFilterCombo != null) {
            searchFilterCombo.getSelectionModel().select(0); // Select "Tous" by default
        }
        
        currentUser = MainController.getInstance().getCurrentUser();
        
        // Initialiser une carte interactive
        if (mapContainer != null) {
            initializeInteractiveMap();
        }
        
        // Initialiser l'assistant IA
        if (assistantText != null) {
            assistantText.setText("Bonjour ! Je suis votre assistant virtuel pour vous aider à remplir votre réclamation. " +
                                  "N'hésitez pas à me poser des questions.");
        }
        
        // Définir la date d'aujourd'hui par défaut
        if (dateField != null) {
            dateField.setValue(LocalDate.now());
        }
        
        // Initialiser les coordonnées par défaut
        if (latitudeField != null && longitudeField != null) {
            // Formater les coordonnées selon la locale de l'utilisateur
            String localLatFormat = String.format(java.util.Locale.getDefault(), "%.6f", currentLatitude);
            String localLongFormat = String.format(java.util.Locale.getDefault(), "%.6f", currentLongitude);
            
            latitudeField.setText(localLatFormat);
            longitudeField.setText(localLongFormat);
        }
    }
    
    // Nouvelle méthode qui combine initializeSimpleMap et initializeSelectableMap
    private void initializeInteractiveMap() {
        try {
            // Créer un conteneur pour la carte
            mapContainer.getChildren().clear();
            
            // S'assurer que les coordonnées sont au bon format (point décimal)
            String latStr = String.format(java.util.Locale.US, "%.6f", currentLatitude);
            String longStr = String.format(java.util.Locale.US, "%.6f", currentLongitude);
            
            // Créer une WebView pour afficher la carte interactive
            javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
            webView.setPrefSize(600, 300);
            javafx.scene.web.WebEngine webEngine = webView.getEngine();
            
            // Construire le HTML pour la carte OpenStreetMap avec Leaflet
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n")
                .append("  <title>Sélection de localisation</title>\n")
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
                .append("    .info-box {\n")
                .append("      padding: 10px;\n")
                .append("      background: white;\n")
                .append("      border-radius: 5px;\n")
                .append("      box-shadow: 0 0 15px rgba(0,0,0,0.2);\n")
                .append("    }\n")
                .append("  </style>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("  <div id=\"map\"></div>\n")
                .append("  <script>\n")
                .append("    // Initialiser la carte\n");
            
            // Définir les coordonnées initiales
            htmlBuilder.append("    var map = L.map('map').setView([" + latStr + ", " + longStr + "], 15);\n")
                .append("    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n")
                .append("      attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors'\n")
                .append("    }).addTo(map);\n")
                .append("\n")
                .append("    // Créer un marqueur à la position initiale\n")
                .append("    var marker = L.marker([" + latStr + ", " + longStr + "], {draggable: true}).addTo(map);\n")
                .append("\n")
                .append("    // Ajouter un pop-up au marqueur avec l'adresse\n")
                .append("    marker.bindPopup('<b>Adresse sélectionnée</b><br>" + adresseField.getText().replace("'", "\\'") + "').openPopup();\n")
                .append("\n")
                .append("    // Zone d'information (affiche les coordonnées)\n")
                .append("    var info = L.control({position: 'bottomleft'});\n")
                .append("    info.onAdd = function(map) {\n")
                .append("      this._div = L.DomUtil.create('div', 'info-box');\n")
                .append("      this.update();\n")
                .append("      return this._div;\n")
                .append("    };\n")
                .append("    info.update = function() {\n")
                .append("      var latlng = marker.getLatLng();\n")
                .append("      this._div.innerHTML = '<b>Coordonnées</b><br>' +\n")
                .append("        'Latitude: ' + latlng.lat.toFixed(6) + '<br>' +\n")
                .append("        'Longitude: ' + latlng.lng.toFixed(6) + '<br>' +\n")
                .append("        '<button onclick=\"copyCoordinates()\" style=\"margin-top:5px;\">Appliquer ces coordonnées</button>';\n")
                .append("    };\n")
                .append("    info.addTo(map);\n")
                .append("\n")
                .append("    // Stocker les coordonnées actuelles pour les récupérer depuis JavaFX\n")
                .append("    var currentCoords = {lat: " + latStr + ", lng: " + longStr + "};\n")
                .append("\n")
                .append("    // Mise à jour des coordonnées lors du déplacement du marqueur\n")
                .append("    marker.on('dragend', function(e) {\n")
                .append("      currentCoords = marker.getLatLng();\n")
                .append("      info.update();\n")
                .append("    });\n")
                .append("\n")
                .append("    // Permettre de cliquer n'importe où sur la carte pour déplacer le marqueur\n")
                .append("    map.on('click', function(e) {\n")
                .append("      marker.setLatLng(e.latlng);\n")
                .append("      currentCoords = e.latlng;\n")
                .append("      info.update();\n")
                .append("    });\n")
                .append("\n")
                .append("    // Fonction pour copier les coordonnées\n")
                .append("    function copyCoordinates() {\n")
                .append("      var coordsJson = JSON.stringify(currentCoords);\n")
                .append("      document.title = 'COORDS:' + coordsJson;\n")
                .append("    }\n")
                .append("  </script>\n")
                .append("</body>\n")
                .append("</html>");
            
            // Charger le HTML dans la WebView
            webEngine.loadContent(htmlBuilder.toString());
            
            // Observer les changements de titre pour récupérer les coordonnées
            webEngine.titleProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && newValue.startsWith("COORDS:")) {
                    try {
                        String coordsJson = newValue.substring(7); // Enlever le préfixe "COORDS:"
                        
                        // Analyser le JSON manuellement
                        coordsJson = coordsJson.replaceAll("[{}\"]", "");
                        String[] parts = coordsJson.split(",");
                        double lat = 0, lng = 0;
                        
                        for (String part : parts) {
                            String[] keyValue = part.split(":");
                            if (keyValue.length == 2) {
                                if (keyValue[0].trim().equals("lat")) {
                                    lat = Double.parseDouble(keyValue[1].trim());
                                } else if (keyValue[0].trim().equals("lng")) {
                                    lng = Double.parseDouble(keyValue[1].trim());
                                }
                            }
                        }
                        
                        // Mettre à jour les champs
                        final double finalLat = lat;
                        final double finalLng = lng;
                        
                        // Mettre à jour l'interface utilisateur
                        javafx.application.Platform.runLater(() -> {
                            latitudeField.setText(String.format("%.6f", finalLat));
                            longitudeField.setText(String.format("%.6f", finalLng));
                            currentLatitude = finalLat;
                            currentLongitude = finalLng;
                            
                            // Chercher l'adresse correspondante
                            adresseField.setStyle("-fx-background-color: #f0f8ff; -fx-border-color: #1a73e8;");
                            adresseField.setPromptText("Recherche de l'adresse en cours...");
                            
                            CompletableFuture.runAsync(() -> {
                                String address = GeoLocationService.getAdresse(finalLat, finalLng);
                                if (address != null) {
                                    javafx.application.Platform.runLater(() -> {
                                        adresseField.setText(address);
                                        adresseField.setStyle("-fx-background-color: #e6ffea; -fx-border-color: #34a853;");
                                        
                                        // Animation pour attirer l'attention
                                        javafx.animation.FadeTransition fadeTransition = 
                                            new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), adresseField);
                                        fadeTransition.setFromValue(0.7);
                                        fadeTransition.setToValue(1.0);
                                        fadeTransition.setCycleCount(3);
                                        fadeTransition.play();
                                        
                                        // Revenir au style normal après 3 secondes
                                        javafx.animation.PauseTransition pause = 
                                            new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
                                        pause.setOnFinished(e -> adresseField.setStyle(""));
                                        pause.play();
                                        
                                        // Mise à jour du texte de confirmation
                                        if (mapContainer.getChildren().size() > 1 && mapContainer.getChildren().get(1) instanceof Label) {
                                            Label confirmationLabel = (Label) mapContainer.getChildren().get(1);
                                            confirmationLabel.setText("Adresse trouvée: " + address);
                                            confirmationLabel.setStyle("-fx-font-style: normal; -fx-text-fill: #1a73e8; -fx-font-weight: bold;");
                                        }
                                    });
                                } else {
                                    javafx.application.Platform.runLater(() -> {
                                        adresseField.setText("");
                                        adresseField.setPromptText("Adresse non trouvée pour ces coordonnées");
                                        adresseField.setStyle("-fx-background-color: #fff4e5; -fx-border-color: #fbbc04;");
                                        
                                        // Revenir au style normal après 3 secondes
                                        javafx.animation.PauseTransition pause = 
                                            new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
                                        pause.setOnFinished(e -> {
                                            adresseField.setStyle("");
                                            adresseField.setPromptText("Adresse de l'incident");
                                        });
                                        pause.play();
                                    });
                                }
                            });
                        });
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la récupération des coordonnées: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            
            // Ajouter la WebView au conteneur
            mapContainer.getChildren().add(webView);
            
            // Ajouter des instructions
            Label mapInstructions = new Label("Cliquez sur la carte pour ajuster l'emplacement");
            mapInstructions.setStyle("-fx-font-style: italic; -fx-text-fill: #666666;");
            mapContainer.getChildren().add(mapInstructions);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de la carte: " + e.getMessage());
            e.printStackTrace();
            
            // Afficher un message d'erreur à l'utilisateur
            Label errorLabel = new Label("Impossible de charger la carte. Veuillez saisir l'adresse manuellement.");
            errorLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
            mapContainer.getChildren().add(errorLabel);
        }
    }
    
    // Méthode pour la recherche d'adresse
    @FXML
    public void chercherAdresse() {
        if (adresseField.getText().trim().isEmpty()) {
            showAlert("Veuillez entrer une adresse à rechercher.");
            return;
        }
        
        String adresse = adresseField.getText();
        
        // Indicateur visuel de recherche en cours
        adresseField.setStyle("-fx-background-color: #f0f8ff; -fx-border-color: #1a73e8;");
        
        // Rechercher les coordonnées à partir de l'adresse
        double[] coordonnees = GeoLocationService.getCoordonnees(adresse);
        
        if (coordonnees != null) {
            currentLatitude = coordonnees[0];
            currentLongitude = coordonnees[1];
            
            // Mettre à jour les champs de coordonnées avec format local (virgule)
            String localLatFormat = String.format(java.util.Locale.getDefault(), "%.6f", currentLatitude);
            String localLongFormat = String.format(java.util.Locale.getDefault(), "%.6f", currentLongitude);
            
            latitudeField.setText(localLatFormat);
            longitudeField.setText(localLongFormat);
            
            // Mise en évidence du champ d'adresse pour montrer le succès
            adresseField.setStyle("-fx-background-color: #e6ffea; -fx-border-color: #34a853;");
            
            // Animation pour attirer l'attention sur les coordonnées
            javafx.animation.FadeTransition fadeLatitude = 
                new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), latitudeField);
            fadeLatitude.setFromValue(0.7);
            fadeLatitude.setToValue(1.0);
            fadeLatitude.setCycleCount(3);
            fadeLatitude.play();
            
            javafx.animation.FadeTransition fadeLongitude = 
                new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), longitudeField);
            fadeLongitude.setFromValue(0.7);
            fadeLongitude.setToValue(1.0);
            fadeLongitude.setCycleCount(3);
            fadeLongitude.play();
            
            // Revenir au style normal après 3 secondes
            javafx.animation.PauseTransition pause = 
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
            pause.setOnFinished(e -> adresseField.setStyle(""));
            pause.play();
            
            // Mise à jour d'un texte de confirmation
            if (mapContainer != null && mapContainer.getChildren().size() > 1) {
                Label confirmationLabel = (Label) mapContainer.getChildren().get(1);
                confirmationLabel.setText("Adresse localisée: " + adresse + "\nCoordonnées: " + 
                                       String.format("%.6f", currentLatitude) + ", " + 
                                       String.format("%.6f", currentLongitude));
                confirmationLabel.setStyle("-fx-font-style: normal; -fx-text-fill: #1a73e8; -fx-font-weight: bold;");
            }
            
            // Initialiser la carte interactive au lieu de la carte simple
            initializeInteractiveMap();
        } else {
            // Indication visuelle de l'échec
            adresseField.setStyle("-fx-background-color: #fff4e5; -fx-border-color: #fbbc04;");
            
            // Revenir au style normal après 3 secondes
            javafx.animation.PauseTransition pause = 
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
            pause.setOnFinished(e -> adresseField.setStyle(""));
            pause.play();
            
            showAlert("Adresse introuvable. Veuillez vérifier l'adresse saisie.");
        }
    }
    
    // Méthode pour activer le mode sélection sur la carte
    @FXML
    public void toggleMapSelection() {
        // Afficher un message pour guider l'utilisateur
        if (mapContainer != null && mapContainer.getChildren().size() > 1) {
            Label confirmationLabel = (Label) mapContainer.getChildren().get(1);
            confirmationLabel.setText("Mode sélection activé. Cliquez sur la carte pour choisir l'emplacement.");
            confirmationLabel.setStyle("-fx-font-style: normal; -fx-text-fill: #1a73e8;");
        } else {
            // Si le label n'existe pas encore, on le crée et on l'ajoute au conteneur
            Label confirmationLabel = new Label("Mode sélection activé. Cliquez sur la carte pour choisir l'emplacement.");
            confirmationLabel.setStyle("-fx-font-style: normal; -fx-text-fill: #1a73e8;");
            if (mapContainer != null) {
                mapContainer.getChildren().add(confirmationLabel);
            }
        }
        
        // Modifier l'initialisation de la carte pour permettre la sélection
        initializeSelectableMap();
    }
    
    // Nouvelle méthode pour initialiser une carte avec sélection
    private void initializeSelectableMap() {
        try {
            // Créer un conteneur pour la carte
            mapContainer.getChildren().clear();
            
            // Créer une WebView pour afficher la carte interactive
            javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
            webView.setPrefSize(600, 300);
            javafx.scene.web.WebEngine webEngine = webView.getEngine();
            
            // Construire le HTML pour la carte OpenStreetMap avec Leaflet
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n")
                .append("  <title>Sélection de localisation</title>\n")
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
                .append("    var map = L.map('map').setView([" + currentLatitude + ", " + currentLongitude + "], 13);\n")
                .append("    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n")
                .append("      attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors'\n")
                .append("    }).addTo(map);\n")
                .append("    var marker = L.marker([" + currentLatitude + ", " + currentLongitude + "]).addTo(map);\n")
                .append("    map.on('click', function(e) {\n")
                .append("      var lat = e.latlng.lat;\n")
                .append("      var lng = e.latlng.lng;\n")
                .append("      marker.setLatLng(e.latlng);\n")
                .append("      updateCoords(lat, lng);\n")
                .append("    });\n")
                .append("    function updateCoords(lat, lng) {\n")
                .append("      window.javaConnector.updateCoordinates(lat, lng);\n")
                .append("    }\n")
                .append("  </script>\n")
                .append("</body>\n")
                .append("</html>");
            
            // Charger le HTML dans la WebView
            webEngine.loadContent(htmlBuilder.toString());
            
            // Créer un objet bridge Java-JavaScript pour la communication
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    // Ajouter le bridge Java-JavaScript
                    webEngine.executeScript("var javaConnector = { " +
                                            "updateCoordinates: function(lat, lng) { " +
                                            "    window.javaConnector.updateCoordinates(lat, lng); " +
                                            "} " +
                                            "};");
                    
                    // Enregistrer l'objet Java dans le contexte JavaScript
                    final class JavaConnector {
                        public void updateCoordinates(double lat, double lng) {
                            javafx.application.Platform.runLater(() -> {
                                currentLatitude = lat;
                                currentLongitude = lng;
                                
                                // Formater les coordonnées selon la locale de l'utilisateur
                                String localLatFormat = String.format(java.util.Locale.getDefault(), "%.6f", lat);
                                String localLongFormat = String.format(java.util.Locale.getDefault(), "%.6f", lng);
                                
                                latitudeField.setText(localLatFormat);
                                longitudeField.setText(localLongFormat);
                                
                                // Rechercher l'adresse à partir des coordonnées
                                String adresseInverse = GeoLocationService.getAdresse(lat, lng);
                                if (adresseInverse != null) {
                                    adresseField.setText(adresseInverse);
                                    
                                    // Mise en évidence du champ d'adresse
                                    adresseField.setStyle("-fx-background-color: #e6ffea; -fx-border-color: #34a853;");
                                    
                                    // Animation pour attirer l'attention
                                    javafx.animation.FadeTransition fadeTransition = 
                                        new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), adresseField);
                                    fadeTransition.setFromValue(0.7);
                                    fadeTransition.setToValue(1.0);
                                    fadeTransition.setCycleCount(3);
                                    fadeTransition.play();
                                    
                                    // Revenir au style normal après 3 secondes
                                    javafx.animation.PauseTransition pause = 
                                        new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
                                    pause.setOnFinished(e -> adresseField.setStyle(""));
                                    pause.play();
                                } else {
                                    adresseField.setText("");
                                    adresseField.setPromptText("Adresse non trouvée pour ces coordonnées");
                                    adresseField.setStyle("-fx-background-color: #fff4e5; -fx-border-color: #fbbc04;");
                                    
                                    // Revenir au style normal après 3 secondes
                                    javafx.animation.PauseTransition pause = 
                                        new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
                                    pause.setOnFinished(e -> {
                                        adresseField.setStyle("");
                                        adresseField.setPromptText("Adresse de l'incident");
                                    });
                                    pause.play();
                                }
                                
                                // Mise à jour du texte de confirmation
                                Label confirmationLabel;
                                if (mapContainer.getChildren().size() > 1) {
                                    confirmationLabel = (Label) mapContainer.getChildren().get(1);
                                } else {
                                    confirmationLabel = new Label();
                                    mapContainer.getChildren().add(confirmationLabel);
                                }
                                
                                String adresseMessage = adresseInverse != null 
                                    ? "Adresse trouvée: " + adresseInverse 
                                    : "Coordonnées sélectionnées: " + lat + ", " + lng;
                                    
                                confirmationLabel.setText(adresseMessage);
                                confirmationLabel.setStyle("-fx-font-style: normal; -fx-text-fill: #34a853; -fx-font-weight: bold;");
                            });
                        }
                    }
                    
                    // Ajouter l'objet au contexte JavaScript de façon simplifiée
                    JavaConnector connector = new JavaConnector();
                    
                    // Utiliser un mécanisme compatible avec JavaFX WebEngine
                    try {
                        // Créer un membre global dans le contexte JavaScript
                        webEngine.executeScript(
                            "window.updateJavaCoordinates = function(lat, lng) { " +
                            "    // Cette fonction sera appelée par le JavaScript " +
                            "    console.log('Coordonnées sélectionnées:', lat, lng); " +
                            "};"
                        );
                        
                        // Mettre à jour le script de la carte pour appeler une fonction globale
                        webEngine.executeScript(
                            "function updateCoords(lat, lng) { " +
                            "    window.updateJavaCoordinates(lat, lng); " +
                            "}"
                        );
                        
                        // Configurer un listener pour les appels JavaScript
                        webEngine.setOnAlert(event -> {
                            String message = event.getData();
                            if (message.startsWith("COORDS:")) {
                                // Format: "COORDS:LAT,LNG"
                                String[] parts = message.substring(7).split(",");
                                if (parts.length == 2) {
                                    try {
                                        double lat = Double.parseDouble(parts[0]);
                                        double lng = Double.parseDouble(parts[1]);
                                        connector.updateCoordinates(lat, lng);
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                        
                        // Modifier le script de mise à jour pour utiliser alert()
                        webEngine.executeScript(
                            "function updateCoords(lat, lng) { " +
                            "    alert('COORDS:' + lat + ',' + lng); " +
                            "}"
                        );
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            
            // Ajouter la WebView au conteneur
            mapContainer.getChildren().add(webView);
            
            // Ajouter un label de confirmation
            Label confirmationLabel = new Label("Sélectionnez un emplacement sur la carte");
            confirmationLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666666;");
            mapContainer.getChildren().add(confirmationLabel);
            
        } catch (Exception e) {
            e.printStackTrace();
            // Revenir à la carte simple en cas d'erreur
            initializeInteractiveMap();
        }
    }
    
    // Méthode pour utiliser la position actuelle
    @FXML
    public void toggleUseCurrentLocation() {
        if (useCurrentLocation.isSelected()) {
            // Simuler la géolocalisation
            CompletableFuture.runAsync(() -> {
                try {
                    // Simuler un délai de géolocalisation
                    Thread.sleep(1000);
                    
                    // Coordonnées simulées (Tunis)
                    double lat = 36.8065;
                    double lng = 10.1815;
                    
                    // Mettre à jour l'UI sur le thread JavaFX
                    javafx.application.Platform.runLater(() -> {
                        currentLatitude = lat;
                        currentLongitude = lng;
                        
                        // Formater les coordonnées selon la locale de l'utilisateur
                        String localLatFormat = String.format(java.util.Locale.getDefault(), "%.6f", lat);
                        String localLongFormat = String.format(java.util.Locale.getDefault(), "%.6f", lng);
                        
                        latitudeField.setText(localLatFormat);
                        longitudeField.setText(localLongFormat);
                        
                        // Rechercher l'adresse à partir des coordonnées
                        String adresseInverse = GeoLocationService.getAdresse(lat, lng);
                        if (adresseInverse != null) {
                            adresseField.setText(adresseInverse);
                            
                            // Mise à jour du texte de confirmation
                            if (mapContainer != null && mapContainer.getChildren().size() > 1) {
                                Label confirmationLabel = (Label) mapContainer.getChildren().get(1);
                                confirmationLabel.setText("Position actuelle détectée: " + adresseInverse);
                                confirmationLabel.setStyle("-fx-font-style: normal; -fx-text-fill: #34a853;");
                            }
                        }
                        
                        // Réinitialiser la carte pour afficher le nouvel emplacement
                        initializeInteractiveMap();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
    
    // Méthode pour analyser la réclamation
    @FXML
    public void analyserReclamation() {
        if (titreField.getText().trim().isEmpty() || descriptionField.getText().trim().isEmpty()) {
            showAlert("Veuillez remplir au moins le titre et la description pour l'analyse.");
            return;
        }
        
        // Créer une réclamation temporaire pour l'analyse
        Reclamation tempReclamation = new Reclamation();
        tempReclamation.setTitre(titreField.getText());
        tempReclamation.setDescription(descriptionField.getText());
        
        // Utiliser l'analyseur NLP
        NLPAnalyzer.analyseReclamation(tempReclamation);
        
        // Afficher les résultats
        categorieField.setText(tempReclamation.getCategorie());
        graviteField.setText(tempReclamation.getGravite());
        prioriteField.setText(String.valueOf(tempReclamation.getPriorite()));
        
        // Mise à jour du texte d'analyse
        analyseTexte.setText("Analyse complétée ! Votre réclamation a été classée dans la catégorie \"" + 
                            tempReclamation.getCategorie() + "\" avec une gravité \"" + 
                            tempReclamation.getGravite() + "\" et une priorité de " + 
                            tempReclamation.getPriorite() + "/5.");
        
        // Mettre à jour l'assistant avec des conseils
        assistantText.setText(ChatBotService.getConseilsReclamation(titreField.getText()));
    }
    
    // Méthode pour poser une question à l'assistant
    @FXML
    public void poserQuestion() {
        if (questionField.getText().trim().isEmpty()) {
            return;
        }
        
        String question = questionField.getText();
        String reponse = ChatBotService.getResponse(question);
        
        // Afficher la conversation
        assistantText.setText(assistantText.getText() + "\n\nVous: " + question + "\n\nAssistant: " + reponse);
        
        // Effacer le champ de question
        questionField.clear();
        
        // Faire défiler vers le bas
        assistantText.positionCaret(assistantText.getText().length());
    }
    
    // Nouvelle méthode de soumission pour le formulaire amélioré
    @FXML
    public void ajouterReclamation() {
        if (!validateInput()) {
            return;
        }
        
        try {
            Reclamation reclamation;
            
            if (isEditMode && reclamationToEdit != null) {
                reclamation = reclamationToEdit;
            } else {
                reclamation = new Reclamation();
                reclamation.setUser(currentUser);
            }
            
            // Mettre à jour les champs de base
            reclamation.setTitre(titreField.getText());
            reclamation.setDescription(descriptionField.getText());
            reclamation.setDate(dateField.getValue());
            
            // Mettre à jour les champs de géolocalisation
            if (!latitudeField.getText().isEmpty() && !longitudeField.getText().isEmpty()) {
                try {
                    // Gérer les nombres avec virgules (format français) ou points (format anglais)
                    String latText = latitudeField.getText().replace(',', '.');
                    String longText = longitudeField.getText().replace(',', '.');
                    
                    double latitude = Double.parseDouble(latText);
                    double longitude = Double.parseDouble(longText);
                    
                    reclamation.setLatitude(latitude);
                    reclamation.setLongitude(longitude);
                    reclamation.setAdresse(adresseField.getText());
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Erreur de format de coordonnées. Assurez-vous qu'elles sont valides: " 
                                                  + e.getMessage());
                }
            }
            
            // Analyser si ce n'est pas déjà fait
            if (categorieField.getText().isEmpty()) {
                NLPAnalyzer.analyseReclamation(reclamation);
            } else {
                reclamation.setCategorie(categorieField.getText());
                reclamation.setGravite(graviteField.getText());
                reclamation.setPriorite(Integer.parseInt(prioriteField.getText()));
            }
            
            if (isEditMode) {
                reclamationService.modifier(reclamation);
                showAlert("Réclamation modifiée avec succès !");
            } else {
                reclamationService.ajouter(reclamation);
                showAlert("Réclamation ajoutée avec succès et ticket créé !");
            }
            
            // Après le traitement, utiliser MainController pour la navigation
            if (isEditMode) {
                MainController.getInstance().showMyReclamations();
            } else {
                // Pour une nouvelle réclamation, selon le besoin, rediriger vers la liste ou offrir d'en ajouter une autre
                Alert choixNavigation = new Alert(Alert.AlertType.CONFIRMATION);
                choixNavigation.setTitle("Réclamation enregistrée");
                choixNavigation.setHeaderText("Que souhaitez-vous faire maintenant ?");
                choixNavigation.setContentText("Choisissez votre action");
                
                ButtonType btnRetourListe = new ButtonType("Voir mes réclamations");
                ButtonType btnNouvelleReclamation = new ButtonType("Ajouter une autre réclamation");
                ButtonType btnAccueil = new ButtonType("Retour à l'accueil");
                
                choixNavigation.getButtonTypes().setAll(btnRetourListe, btnNouvelleReclamation, btnAccueil);
                
                choixNavigation.showAndWait().ifPresent(buttonType -> {
                    if (buttonType == btnRetourListe) {
                        MainController.getInstance().showMyReclamations();
                    } else if (buttonType == btnNouvelleReclamation) {
                        // Vider les champs pour une nouvelle réclamation
                        clearFields();
                    } else {
                        MainController.getInstance().showHomePage();
                    }
                });
            }
            
        } catch (Exception e) {
            showError("Erreur lors de la soumission", e);
        }
    }
    
    private void initializeTableView() {
        // Setup cell value factories
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        titreColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitre()));
        dateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDate()));
        statutColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatut()));
        
        // Configure action column with "Détail" button
        actionColumn.setCellFactory(col -> new TableCell<Reclamation, Void>() {
            private final Button detailButton = new Button("Détail");
            
            {
                detailButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                detailButton.setOnAction(event -> {
                    Reclamation reclamation = getTableView().getItems().get(getIndex());
                    showReclamationDetails(reclamation);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailButton);
                }
            }
        });
        
        loadTableData();
    }
    
    private void loadTableData() {
        try {
            // Clear and reload data
            reclamationList.clear();
            if (currentUser != null) {
                // Only load user's reclamations
                reclamationList.addAll(reclamationService.getByUser(currentUser.getId()));
            } else {
                // For testing, load all reclamations
                reclamationList.addAll(reclamationService.voir());
            }
            reclamationTable.setItems(reclamationList);
        } catch (Exception e) {
            showError("Erreur lors du chargement des réclamations", e);
        }
    }
    
    private void showSelectedReclamationDetails(Reclamation reclamation) {
        if (detailTitreLabel != null && detailDescriptionArea != null 
                && detailDateLabel != null && detailStatutLabel != null) {
            
            detailTitreLabel.setText(reclamation.getTitre());
            detailDescriptionArea.setText(reclamation.getDescription());
            detailDateLabel.setText(reclamation.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            detailStatutLabel.setText(reclamation.getStatut());
            
            // Style the status label based on status
            String status = reclamation.getStatut().toLowerCase();
            String statusColor;
            
            switch (status) {
                case "ouvert":
                    statusColor = "-fx-text-fill: #1a73e8;";
                    break;
                case "en cours":
                    statusColor = "-fx-text-fill: #FF9800;";
                    break;
                case "resolu":
                    statusColor = "-fx-text-fill: #4CAF50;";
                    break;
                case "rejete":
                    statusColor = "-fx-text-fill: #F44336;";
                    break;
                default:
                    statusColor = "";
                    break;
            }
            
            detailStatutLabel.setStyle(statusColor);
        }
    }

    private void createReclamationCard(Reclamation reclamation) {
        VBox card = new VBox(10);
        card.getStyleClass().add("reclamation-card");
        card.setUserData(reclamation);
        
        // Title
        Label titleLabel = new Label(reclamation.getTitre());
        titleLabel.getStyleClass().add("card-title");
        
        // Date
        Label dateLabel = new Label(reclamation.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateLabel.getStyleClass().add("card-date");
        
        // Ticket ID
        Label ticketLabel = new Label("Ticket #" + reclamation.getTicketId());
        ticketLabel.getStyleClass().add("card-ticket");
        
        // Status
        Label statusLabel = new Label(reclamation.getStatut());
        statusLabel.getStyleClass().addAll("card-status", "status-" + reclamation.getStatut().toLowerCase().replace(" ", "-"));
        
        // Description preview (first 100 characters)
        String descriptionPreview = reclamation.getDescription();
        if (descriptionPreview.length() > 100) {
            descriptionPreview = descriptionPreview.substring(0, 97) + "...";
        }
        Label descriptionLabel = new Label(descriptionPreview);
        descriptionLabel.setWrapText(true);
        
        // Ajouter les informations de catégorie et priorité si disponibles
        if (reclamation.getCategorie() != null && !reclamation.getCategorie().isEmpty()) {
            HBox infoBox = new HBox(10);
            
            Label categorieLabel = new Label("Catégorie: " + reclamation.getCategorie());
            categorieLabel.getStyleClass().add("card-info");
            
            Label prioriteLabel = new Label("Priorité: " + reclamation.getPriorite() + "/5");
            prioriteLabel.getStyleClass().add("card-info");
            
            infoBox.getChildren().addAll(categorieLabel, prioriteLabel);
            card.getChildren().addAll(titleLabel, dateLabel, ticketLabel, statusLabel, descriptionLabel, infoBox);
        } else {
            card.getChildren().addAll(titleLabel, dateLabel, ticketLabel, statusLabel, descriptionLabel);
        }
        
        // Add click handler to open details view
        card.setOnMouseClicked(event -> showReclamationDetails(reclamation));
        
        reclamationsContainer.getChildren().add(card);
    }

    private void loadReclamations() {
        if (reclamationsContainer != null) {
            reclamationsContainer.getChildren().clear();
        }
        
        reclamationList.clear();
        originalReclamationList.clear();
        
        try {
            // Récupérer les réclamations de l'utilisateur connecté
            reclamationList.addAll(reclamationService.getByUser(currentUser.getId()));
            originalReclamationList.addAll(reclamationList);
            
            for (Reclamation reclamation : reclamationList) {
                createReclamationCard(reclamation);
            }
        } catch (Exception e) {
            showError("Erreur de chargement", e);
        }
    }

    private void showReclamationDetails(Reclamation reclamation) {
        if (reclamation == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/reclamation_details.fxml"));
            Parent detailView = loader.load();
            
            ReclamationDetailsController controller = loader.getController();
            controller.setReclamation(reclamation);
            
            // Create new scene/stage for details view
            Stage detailStage = new Stage();
            detailStage.setTitle("Détails de la Réclamation #" + reclamation.getId());
            detailStage.setScene(new Scene(detailView));
            detailStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            detailStage.showAndWait();
            
            // Reload table data after dialog closes
            loadTableData();
        } catch (Exception e) {
            showError("Erreur lors de l'affichage des détails", e);
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void initializeFields(Reclamation reclamation) {
        if (reclamation != null && titreField != null && descriptionField != null) {
            titreField.setText(reclamation.getTitre());
            descriptionField.setText(reclamation.getDescription());
            if (submitButton != null) {
                submitButton.setText("Modifier");
            }
        }
    }

    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (titreField.getText().trim().isEmpty()) {
            errorMessage.append("- Le titre est obligatoire\n");
        }
        
        if (descriptionField.getText().trim().isEmpty()) {
            errorMessage.append("- La description est obligatoire\n");
        }
        
        if (dateField.getValue() == null) {
            errorMessage.append("- La date est obligatoire\n");
        }
        
        if (latitudeField.getText().isEmpty() || longitudeField.getText().isEmpty()) {
            errorMessage.append("- Veuillez sélectionner une localisation\n");
        }
        
        if (errorMessage.length() > 0) {
            showAlert("Veuillez corriger les erreurs suivantes:\n" + errorMessage.toString());
            return false;
        }
        
        return true;
    }

    @FXML
    public void handleEditReclamation() {
        Reclamation selectedReclamation;
        
        if (reclamationTable != null) {
            // If we're in table view mode
            selectedReclamation = reclamationTable.getSelectionModel().getSelectedItem();
        } else {
            // If we're in cards view mode
            selectedReclamation = reclamationsContainer.getChildren().stream()
                .filter(node -> node instanceof VBox)
                .map(node -> (VBox) node)
                .map(VBox::getUserData)
                .filter(Reclamation.class::isInstance)
                .map(Reclamation.class::cast)
                .findFirst()
                .orElse(null);
        }

        if (selectedReclamation == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une réclamation à modifier.");
            alert.showAndWait();
            return;
        }

        MainController.getInstance().showEditReclamation(selectedReclamation);
    }

    @FXML
    public void handleDeleteReclamation() {
        Reclamation selectedReclamation = null;
        
        if (reclamationTable != null) {
            selectedReclamation = reclamationTable.getSelectionModel().getSelectedItem();
        } else if (reclamationsContainer != null) {
            selectedReclamation = reclamationsContainer.getChildren().stream()
                .filter(node -> node instanceof VBox)
                .map(node -> (VBox) node)
                .map(VBox::getUserData)
                .filter(Reclamation.class::isInstance)
                .map(Reclamation.class::cast)
                .findFirst()
                .orElse(null);
        }

        if (selectedReclamation == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une réclamation à supprimer.");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            reclamationService.supprimer(selectedReclamation.getId());
            
            if (reclamationTable != null) {
                loadTableData();
            } else {
                loadReclamations();
            }
            
            // Clear details panel
            if (detailTitreLabel != null) {
                detailTitreLabel.setText("");
            }
            if (detailDescriptionArea != null) {
                detailDescriptionArea.setText("");
            }
            if (detailDateLabel != null) {
                detailDateLabel.setText("");
            }
            if (detailStatutLabel != null) {
                detailStatutLabel.setText("");
            }
        }
    }

    public void setStage(Stage stage) {
        this.currentStage = stage;
    }

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        if (submitButton != null) {
            submitButton.setText(editMode ? "Modifier" : "Ajouter");
        }
    }
    
    @FXML
    public void showAddReclamation() {
        MainController.getInstance().showAddReclamation();
    }

    @FXML
    public void clearFields() {
        // Réinitialiser les champs basiques
        if (titreField != null) {
            titreField.clear();
        }
        if (descriptionField != null) {
            descriptionField.clear();
        }
        if (dateField != null) {
            dateField.setValue(LocalDate.now());
        }
        
        // Réinitialiser les champs de géolocalisation
        if (adresseField != null) {
            adresseField.clear();
        }
        
        // Réinitialiser les coordonnées par défaut (Tunis)
        if (latitudeField != null && longitudeField != null) {
            // Formater les coordonnées selon la locale de l'utilisateur
            String localLatFormat = String.format(java.util.Locale.getDefault(), "%.6f", currentLatitude);
            String localLongFormat = String.format(java.util.Locale.getDefault(), "%.6f", currentLongitude);
            
            latitudeField.setText(localLatFormat);
            longitudeField.setText(localLongFormat);
        }
        
        // Réinitialiser les champs d'analyse
        if (categorieField != null) {
            categorieField.clear();
        }
        if (graviteField != null) {
            graviteField.clear();
        }
        if (prioriteField != null) {
            prioriteField.clear();
        }
        if (analyseTexte != null) {
            analyseTexte.setText("");
        }
        
        // Réinitialiser l'assistant
        if (assistantText != null) {
            assistantText.setText("Bonjour ! Je suis votre assistant virtuel pour vous aider à remplir votre réclamation. " +
                                 "N'hésitez pas à me poser des questions.");
        }
        if (questionField != null) {
            questionField.clear();
        }
    }

    // Add getters for the form fields
    public TextField getTitreField() {
        return titreField;
    }

    public TextArea getDescriptionField() {
        return descriptionField;
    }

    public void setReclamationToEdit(Reclamation reclamation) {
        this.reclamationToEdit = reclamation;
    }

    @FXML
    public void backToHome() {
        MainController.getInstance().showHomePage();
    }

    @FXML
    public void backToReclamations() {
        // Utiliser le MainController pour naviguer correctement
        MainController.getInstance().showMyReclamations();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    /**
     * Handle search functionality for reclamations
     */
    @FXML
    public void handleSearchReclamation() {
        if (searchReclamationField == null) {
            return;
        }
        
        String searchText = searchReclamationField.getText().toLowerCase().trim();
        String filterOption = null;
        
        if (searchFilterCombo != null) {
            filterOption = searchFilterCombo.getValue();
        }
        
        if (filterOption == null) {
            filterOption = "Tous"; // Default filter option
        }
        
        // If search text is empty, show all reclamations
        if (searchText.isEmpty()) {
            if (reclamationTable != null) {
                reclamationTable.setItems(originalReclamationList);
            } else if (reclamationsContainer != null) {
                loadReclamations(); // Reload all cards
            }
            return;
        }
        
        // Filter the reclamations based on search text and filter option
        ObservableList<Reclamation> filteredList = FXCollections.observableArrayList();
        
        for (Reclamation reclamation : originalReclamationList) {
            boolean matches = false;
            
            switch (filterOption) {
                case "Par titre":
                    matches = reclamation.getTitre().toLowerCase().contains(searchText);
                    break;
                case "Par description":
                    matches = reclamation.getDescription().toLowerCase().contains(searchText);
                    break;
                case "Par statut":
                    matches = reclamation.getStatut().toLowerCase().contains(searchText);
                    break;
                case "Par date":
                    matches = reclamation.getDate().toString().contains(searchText);
                    break;
                case "Tous":
                default:
                    matches = reclamation.getTitre().toLowerCase().contains(searchText) ||
                              reclamation.getDescription().toLowerCase().contains(searchText) ||
                              reclamation.getStatut().toLowerCase().contains(searchText) ||
                              reclamation.getDate().toString().contains(searchText);
                    
                    // Rechercher également dans la catégorie et le ticket ID si disponibles
                    if (reclamation.getCategorie() != null) {
                        matches = matches || reclamation.getCategorie().toLowerCase().contains(searchText);
                    }
                    if (reclamation.getTicketId() != null) {
                        matches = matches || reclamation.getTicketId().toLowerCase().contains(searchText);
                    }
                    break;
            }
            
            if (matches) {
                filteredList.add(reclamation);
            }
        }
        
        // Update UI with filtered results
        if (reclamationTable != null) {
            reclamationTable.setItems(filteredList);
            
            // Mise à jour du placeholder pour montrer un message si aucun résultat
            if (filteredList.isEmpty()) {
                reclamationTable.setPlaceholder(new Label("Aucune réclamation trouvée pour cette recherche"));
            }
        } else if (reclamationsContainer != null) {
            displayFilteredReclamations(filteredList);
        }
    }
    
    /**
     * Display filtered reclamations in the cards container
     */
    private void displayFilteredReclamations(ObservableList<Reclamation> filteredList) {
        if (reclamationsContainer == null) {
            return;
        }
        
        reclamationsContainer.getChildren().clear();
        
        if (filteredList.isEmpty()) {
            Label noResultsLabel = new Label("Aucune réclamation trouvée pour cette recherche");
            noResultsLabel.setStyle("-fx-text-fill: #757575; -fx-font-style: italic; -fx-padding: 20;");
            reclamationsContainer.getChildren().add(noResultsLabel);
        } else {
            for (Reclamation reclamation : filteredList) {
                createReclamationCard(reclamation);
            }
        }
    }
}

