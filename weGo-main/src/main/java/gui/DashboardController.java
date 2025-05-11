package gui;

import Entites.Reclamation;
import Entites.UtilisateurRole;
import Entites.utilisateur;
import Entites.Avis;
import services.ReclamationService;
import services.AvisService;
import utils.GeoLocationService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.chart.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.util.Duration;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.sql.SQLException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class DashboardController {
    
    @FXML
    private Label totalReclamationsCount;
    @FXML
    private Label pendingReclamationsCount;
    @FXML
    private Label resolvedReclamationsCount;
    @FXML
    private Label urgentReclamationsCount;
    
    @FXML
    private PieChart statusChart;
    @FXML
    private BarChart<String, Number> categoriesChart;
    
    @FXML
    private VBox mapContainer;
    
    @FXML
    private TableView<Reclamation> recentReclamationsTable;
    @FXML
    private TableColumn<Reclamation, String> ticketIdColumn;
    @FXML
    private TableColumn<Reclamation, String> titreColumn;
    @FXML
    private TableColumn<Reclamation, LocalDate> dateColumn;
    @FXML
    private TableColumn<Reclamation, String> statutColumn;
    @FXML
    private TableColumn<Reclamation, Integer> prioriteColumn;
    @FXML
    private TableColumn<Reclamation, String> categorieColumn;
    @FXML
    private TableColumn<Reclamation, String> userColumn;
    
    @FXML
    private LineChart<String, Number> resolutionTimeChart;
    
    @FXML
    private ProgressIndicator satisfactionIndicator;
    @FXML
    private Label satisfactionLabel;
    
    private ReclamationService reclamationService;
    private AvisService avisService;
    private List<Reclamation> allReclamations;
    private List<Avis> allAvis;
    private ImageView mapImageView;
    private utilisateur currentUser;
    
    @FXML
    public void initialize() {
        try {
            // Initialiser les compteurs à 0
            totalReclamationsCount.setText("0");
            pendingReclamationsCount.setText("0");
            resolvedReclamationsCount.setText("0");
            urgentReclamationsCount.setText("0");
            
            // Initialiser les services et récupérer l'utilisateur courant
            reclamationService = new ReclamationService();
            avisService = new AvisService(); // Initialiser le service d'avis
            
            try {
                currentUser = MainController.getInstance().getCurrentUser();
            } catch (Exception e) {
                System.out.println("Erreur lors de la récupération de l'utilisateur courant: " + e.getMessage());
                // Créer un utilisateur par défaut si nécessaire
                currentUser = new utilisateur();
                currentUser.setId(1);
                currentUser.setUsername("Utilisateur");
                currentUser.setRole(UtilisateurRole.ADMIN.CLIENT);
            }
            
            // Vérifier que les contrôles FXML sont correctement injectés
            if (recentReclamationsTable == null) {
                System.out.println("Erreur: TableView n'est pas correctement injecté");
                showAlert("Erreur d'initialisation", "Les contrôles de l'interface ne sont pas correctement chargés");
                return;
            }
            
            // Initialiser les colonnes de la table
            try {
                initializeTableColumns();
            } catch (Exception e) {
                System.out.println("Erreur lors de l'initialisation des colonnes: " + e.getMessage());
            }
            
            // Appliquer des styles personnalisés aux graphiques
            try {
                applyCustomChartStyles();
            } catch (Exception e) {
                System.out.println("Erreur lors de l'application des styles: " + e.getMessage());
            }
            
            // Afficher un message de chargement
            Label loadingLabel = new Label("Chargement des données...");
            loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
            recentReclamationsTable.setPlaceholder(loadingLabel);
            
            // Utiliser un Thread séparé pour charger les données
            Thread dataLoadingThread = new Thread(() -> {
                try {
                    // Délai court pour permettre à l'interface de s'initialiser
                    Thread.sleep(500);
                    
                    // Charger les données dans le thread JavaFX
                    javafx.application.Platform.runLater(() -> {
                        try {
                            loadData();
                            
                            // Ajouter un placeholder plus approprié après le chargement
                            recentReclamationsTable.setPlaceholder(new Label("Aucune réclamation trouvée"));
                            
                            // Initialiser la carte seulement après le chargement des données
                            initializeMap();
                        } catch (Exception e) {
                            System.out.println("Erreur lors du chargement des données: " + e.getMessage());
                            e.printStackTrace();
                            showAlert("Erreur", "Une erreur est survenue lors du chargement des données: " + e.getMessage());
                        }
                    });
                } catch (InterruptedException e) {
                    System.out.println("Thread de chargement interrompu: " + e.getMessage());
                    javafx.application.Platform.runLater(() -> {
                        showAlert("Erreur", "Une erreur est survenue lors du chargement: " + e.getMessage());
                    });
                } catch (Exception e) {
                    System.out.println("Erreur générale dans le thread de chargement: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            // Démarrer le thread de chargement
            dataLoadingThread.setDaemon(true);
            dataLoadingThread.start();
        } catch (Exception e) {
            System.out.println("Erreur critique lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur critique", "Une erreur est survenue lors de l'initialisation du tableau de bord: " + e.getMessage());
        }
    }
    
    private void initializeTableColumns() {
        // Configurer les colonnes de la table
        ticketIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTicketId()));
        titreColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitre()));
        dateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDate()));
        statutColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut()));
        prioriteColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getPriorite()).asObject());
        categorieColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategorie()));
        userColumn.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getUser() != null ? data.getValue().getUser().getUsername() : "Inconnu"
        ));
        
        // Colorer les cellules de statut selon leur valeur
        statutColumn.setCellFactory(column -> new TableCell<Reclamation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    switch (item.toLowerCase()) {
                        case "ouvert":
                            setTextFill(Color.valueOf("#1a73e8"));
                            break;
                        case "en cours":
                            setTextFill(Color.valueOf("#FFA726"));
                            break;
                        case "resolu":
                            setTextFill(Color.valueOf("#4CAF50"));
                            break;
                        case "rejete":
                            setTextFill(Color.valueOf("#F44336"));
                            break;
                        default:
                            setTextFill(Color.BLACK);
                            break;
                    }
                }
            }
        });
    }
    
    private void applyCustomChartStyles() {
        // Styles pour le graphique en camembert
        statusChart.setLabelLineLength(20);
        statusChart.setLegendVisible(true);
        statusChart.setStartAngle(90);
        statusChart.setAnimated(true);
        
        // Styles pour le graphique à barres
        categoriesChart.setAnimated(true);
        categoriesChart.setTitle("");
        categoriesChart.setLegendVisible(false);
    }
    
    private void loadData() {
        try {
            // Initialiser les listes vides avant toute opération
            allReclamations = new ArrayList<>();
            allAvis = new ArrayList<>();
            
            // Vérifier que les services sont initialisés
            if (reclamationService == null) {
                reclamationService = new ReclamationService();
            }
            
            if (avisService == null) {
                avisService = new AvisService();
            }
            
            // Récupérer toutes les réclamations
            try {
                System.out.println("Tentative de récupération des réclamations...");
                allReclamations = reclamationService.voir();
                System.out.println("Réclamations récupérées: " + allReclamations.size());
                
                // Récupérer tous les avis pour le calcul de satisfaction
                try {
                    allAvis = avisService.getAll();
                    System.out.println("Avis récupérés: " + allAvis.size());
                } catch (Exception e) {
                    System.out.println("Erreur lors de la récupération des avis: " + e.getMessage());
                    e.printStackTrace();
                    // En cas d'erreur, on continue avec une liste vide
                }
            } catch (Exception e) {
                System.out.println("Erreur lors de la récupération des réclamations: " + e.getMessage());
                e.printStackTrace();
                
                // Si une erreur se produit lors de la récupération des réclamations, 
                // utiliser des données de démonstration
                System.out.println("Utilisation des données de démonstration...");
                createDemoData();
            }
            
            // Si aucune réclamation n'a été trouvée, créer des données de démonstration
            if (allReclamations.isEmpty()) {
                System.out.println("Aucune réclamation trouvée, création de données de démonstration...");
                createDemoData();
            }
            
            // Mettre à jour les compteurs
            try {
                updateCounters();
            } catch (Exception e) {
                System.out.println("Erreur lors de la mise à jour des compteurs: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Mettre à jour les graphiques
            try {
                updateCharts();
            } catch (Exception e) {
                System.out.println("Erreur lors de la mise à jour des graphiques: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Mettre à jour la table des réclamations récentes
            try {
                updateRecentReclamationsTable();
            } catch (Exception e) {
                System.out.println("Erreur lors de la mise à jour de la table: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Mettre à jour les métriques de performance
            try {
                updatePerformanceMetrics();
            } catch (Exception e) {
                System.out.println("Erreur lors de la mise à jour des métriques: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            System.out.println("Erreur générale lors du chargement des données: " + e.getMessage());
            e.printStackTrace();
            
            try {
                // En cas d'erreur critique, créer au moins des données de démonstration
                createDemoData();
                
                // Essayer de mettre à jour les éléments de l'interface avec ces données minimales
                updateCounters();
                updateCharts();
                updateRecentReclamationsTable();
                updatePerformanceMetrics();
                
                showAlert("Avertissement", 
                         "Les données n'ont pas pu être chargées correctement. Affichage de données de démonstration.");
            } catch (Exception ex) {
                System.out.println("Erreur critique pendant la récupération: " + ex.getMessage());
                ex.printStackTrace();
                showAlert("Erreur critique", 
                         "Impossible de charger ou d'afficher les données du tableau de bord.");
            }
        }
    }
    
    // Créer des données de démonstration si aucune réclamation n'existe
    private void createDemoData() {
        // Utilisateur par défaut
        utilisateur demoUser = new utilisateur();
        demoUser.setId(1);
        demoUser.setUsername("Demo Utilisateur");
        
        // Créer quelques réclamations de démonstration
        Reclamation reclamation1 = new Reclamation();
        reclamation1.setId(1);
        reclamation1.setTitre("Problème d'éclairage public");
        reclamation1.setDescription("Les lampadaires de la rue principale ne fonctionnent plus depuis trois jours.");
        reclamation1.setDate(LocalDate.now().minusDays(5));
        reclamation1.setStatut("en cours");
        reclamation1.setTicketId("2024-06-ABC1");
        reclamation1.setPriorite(3);
        reclamation1.setCategorie("Éclairage");
        reclamation1.setGravite("moyenne");
        reclamation1.setUser(demoUser);
        
        Reclamation reclamation2 = new Reclamation();
        reclamation2.setId(2);
        reclamation2.setTitre("Nid de poule dangereux");
        reclamation2.setDescription("Un nid de poule important s'est formé à l'intersection des rues A et B.");
        reclamation2.setDate(LocalDate.now().minusDays(2));
        reclamation2.setStatut("en cours");
        reclamation2.setTicketId("2024-06-ABC2");
        reclamation2.setPriorite(4);
        reclamation2.setCategorie("Voirie");
        reclamation2.setGravite("élevée");
        reclamation2.setUser(demoUser);
        
        // Ajouter ces réclamations à la liste
        allReclamations = new ArrayList<>();
        allReclamations.add(reclamation1);
        allReclamations.add(reclamation2);
        
        // Créer des avis de démonstration
        Avis avis1 = new Avis();
        avis1.setId(1);
        avis1.setComment("Service réactif, merci !");
        avis1.setRating("4");
        avis1.setDate(new java.sql.Date(System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000L)); // 10 jours avant
        avis1.setUser(demoUser);
        
        Avis avis2 = new Avis();
        avis2.setId(2);
        avis2.setComment("Problème résolu rapidement");
        avis2.setRating("5");
        avis2.setDate(new java.sql.Date(System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L)); // 5 jours avant
        avis2.setUser(demoUser);
        
        // Ajouter ces avis à la liste
        allAvis = new ArrayList<>();
        allAvis.add(avis1);
        allAvis.add(avis2);
    }
    
    private void updateCounters() {
        // Si aucune réclamation n'existe, simuler des données pour le développement
        if (allReclamations == null || allReclamations.isEmpty()) {
            totalReclamationsCount.setText("0");
            pendingReclamationsCount.setText("0");
            resolvedReclamationsCount.setText("0");
            urgentReclamationsCount.setText("0");
            return;
        }
        
        int total = allReclamations.size();
        
        // Compter les réclamations par statut
        int pending = 0;
        int resolved = 0;
        int urgent = 0;
        
        for (Reclamation r : allReclamations) {
            // Vérifier le statut
            if (r.getStatut() != null) {
                String statut = r.getStatut().toLowerCase();
                if (statut.equals("ouvert") || statut.equals("en cours")) {
                    pending++;
                } else if (statut.equals("resolu")) {
                    resolved++;
                }
            }
            
            // Vérifier la priorité
            if (r.getPriorite() >= 4) { // Priorité 4 ou 5 = urgent
                urgent++;
            }
        }
        
        System.out.println("Statistiques des réclamations: " + total + " total, " + pending + " en attente, " + 
                          resolved + " résolues, " + urgent + " urgentes");
        
        // Mettre à jour les compteurs avec des animations
        try {
            int oldTotalValue = totalReclamationsCount.getText().isEmpty() ? 0 : Integer.parseInt(totalReclamationsCount.getText());
            int oldPendingValue = pendingReclamationsCount.getText().isEmpty() ? 0 : Integer.parseInt(pendingReclamationsCount.getText());
            int oldResolvedValue = resolvedReclamationsCount.getText().isEmpty() ? 0 : Integer.parseInt(resolvedReclamationsCount.getText());
            int oldUrgentValue = urgentReclamationsCount.getText().isEmpty() ? 0 : Integer.parseInt(urgentReclamationsCount.getText());
            
            animateCounterUpdate(totalReclamationsCount, oldTotalValue, total);
            animateCounterUpdate(pendingReclamationsCount, oldPendingValue, pending);
            animateCounterUpdate(resolvedReclamationsCount, oldResolvedValue, resolved);
            animateCounterUpdate(urgentReclamationsCount, oldUrgentValue, urgent);
        } catch (NumberFormatException e) {
            // En cas d'erreur de format, mettre à jour directement sans animation
            totalReclamationsCount.setText(String.valueOf(total));
            pendingReclamationsCount.setText(String.valueOf(pending));
            resolvedReclamationsCount.setText(String.valueOf(resolved));
            urgentReclamationsCount.setText(String.valueOf(urgent));
            
            System.err.println("Erreur de format lors de l'animation des compteurs: " + e.getMessage());
        }
    }
    
    // Méthode pour animer l'augmentation d'un compteur
    private void animateCounterUpdate(Label counterLabel, int oldValue, int newValue) {
        // Si la valeur n'a pas changé, pas besoin d'animation
        if (oldValue == newValue) {
            counterLabel.setText(String.valueOf(newValue));
            return;
        }
        
        // Durée totale de l'animation en ms
        final int animationDuration = 1500;
        // Nombre d'étapes pour l'animation
        final int steps = 20;
        // Durée entre chaque étape
        final int stepDuration = animationDuration / steps;
        
        Timeline timeline = new Timeline();
        
        // Créer une animation qui augmente le compteur progressivement
        for (int i = 1; i <= steps; i++) {
            final int step = i;
            
            // Calcul de la valeur intermédiaire du compteur
            int stepValue = oldValue + (int)((newValue - oldValue) * ((double) step / steps));
            
            KeyFrame keyFrame = new KeyFrame(
                Duration.millis(step * stepDuration),
                event -> counterLabel.setText(String.valueOf(stepValue))
            );
            
            timeline.getKeyFrames().add(keyFrame);
        }
        
        // Ajouter une dernière frame pour s'assurer d'avoir la valeur finale exacte
        KeyFrame lastFrame = new KeyFrame(
            Duration.millis(animationDuration),
            event -> counterLabel.setText(String.valueOf(newValue))
        );
        timeline.getKeyFrames().add(lastFrame);
        
        timeline.play();
    }
    
    private void updateCharts() {
        // Graphique de statut (PieChart)
        Map<String, Integer> statusCounts = new HashMap<>();
        // Initialiser tous les statuts possibles avec 0 pour s'assurer qu'ils apparaissent même sans données
        statusCounts.put("ouvert", 0);
        statusCounts.put("en cours", 0);
        statusCounts.put("resolu", 0);
        statusCounts.put("rejete", 0);
        
        // Compter les réclamations par statut
        if (allReclamations != null && !allReclamations.isEmpty()) {
            for (Reclamation r : allReclamations) {
                if (r.getStatut() != null) {
                    String status = r.getStatut().toLowerCase();
                    statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
                }
            }
        } else {
            // Si pas de données, ajouter des exemples pour le développement
            statusCounts.put("ouvert", 1);
            statusCounts.put("en cours", 2);
            statusCounts.put("resolu", 0);
            statusCounts.put("rejete", 0);
        }
        
        // Créer les données pour le graphique
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            // Ajouter même les statuts avec 0 réclamation si aucune donnée n'est disponible
            if (entry.getValue() > 0 || (allReclamations == null || allReclamations.isEmpty())) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
        }
        
        // Mettre à jour le graphique avec les données
        statusChart.setData(pieChartData);
        
        // Colorer les sections du graphique
        for (final PieChart.Data data : pieChartData) {
            String status = data.getName().toLowerCase();
            
            final String color;
            switch (status) {
                case "ouvert":
                    color = "#1a73e8";
                    break;
                case "en cours":
                    color = "#FFA726";
                    break;
                case "resolu":
                    color = "#4CAF50";
                    break;
                case "rejete":
                    color = "#F44336";
                    break;
                default:
                    color = "#9E9E9E";
                    break;
            }
            
            try {
                // Vérifier si le nœud existe avant d'appliquer le style
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-pie-color: " + color + ";");
                }
                
                // Ajouter un écouteur pour appliquer la couleur après l'initialisation
                data.nodeProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        newValue.setStyle("-fx-pie-color: " + color + ";");
                    }
                });
            } catch (Exception e) {
                System.err.println("Erreur lors de l'application des styles au graphique: " + e.getMessage());
            }
        }
        
        // Graphique de catégories (BarChart)
        Map<String, Integer> categoryCounts = new HashMap<>();
        
        // Catégories par défaut
        String[] defaultCategories = {"Voirie", "Éclairage", "Propreté", "Sécurité", "Transport"};
        for (String category : defaultCategories) {
            categoryCounts.put(category, 0);
        }
        
        // Compter les réclamations par catégorie
        if (allReclamations != null && !allReclamations.isEmpty()) {
            for (Reclamation r : allReclamations) {
                if (r.getCategorie() != null && !r.getCategorie().isEmpty()) {
                    String category = r.getCategorie();
                    categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
                }
            }
        } else {
            // Ajouter des données d'exemple si aucune réclamation n'existe
            categoryCounts.put("Voirie", 1);
            categoryCounts.put("Éclairage", 0);
            categoryCounts.put("Propreté", 0);
            categoryCounts.put("Sécurité", 0);
            categoryCounts.put("Transport", 1);
        }
        
        // Créer la série de données
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre de réclamations");
        
        // Ajouter les données à la série
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        // Effacer les anciennes données et ajouter la nouvelle série
        categoriesChart.getData().clear();
        categoriesChart.getData().add(series);
        
        // Appliquer un style aux barres
        for (XYChart.Series<String, Number> s : categoriesChart.getData()) {
            int colorIndex = 0;
            String[] colors = {"#4285F4", "#34A853", "#FBBC05", "#EA4335", "#8F44AD"};
            
            for (XYChart.Data<String, Number> data : s.getData()) {
                // Appliquer la couleur
                String color = colors[colorIndex % colors.length];
                
                // Appliquer le style une fois disponible
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                }
                
                // Ajouter un écouteur pour appliquer le style après initialisation
                data.nodeProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        newValue.setStyle("-fx-bar-fill: " + color + ";");
                        
                        // Ajouter une infobulle
                        Tooltip tooltip = new Tooltip(
                            data.getXValue() + ": " + data.getYValue() + " réclamation(s)"
                        );
                        Tooltip.install(newValue, tooltip);
                    }
                });
                
                colorIndex++;
            }
        }
    }
    
    private void updateRecentReclamationsTable() {
        // Trier les réclamations par date (plus récentes d'abord)
        List<Reclamation> recentReclamations = allReclamations.stream()
            .sorted((r1, r2) -> r2.getDate().compareTo(r1.getDate()))
            .limit(20) // Limiter aux 20 plus récentes
            .collect(Collectors.toList());
        
        recentReclamationsTable.setItems(FXCollections.observableArrayList(recentReclamations));
    }
    
    private void updatePerformanceMetrics() {
        // 1. Mettre à jour le graphique de temps de résolution
        updateResolutionTimeChart();
        
        // 2. Mettre à jour le taux de satisfaction basé sur les avis
        updateSatisfactionRate();
    }
    
    private void updateResolutionTimeChart() {
        // Calculer le temps moyen de résolution
        Map<String, Double> monthlyResolutionTimes = new TreeMap<>(); // Utiliser TreeMap pour trier par clé
        Map<String, Integer> monthlyResolutionCounts = new TreeMap<>();
        
        // Liste des derniers 6 mois pour s'assurer qu'ils sont tous présents
        List<String> last6Months = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM/yyyy");
        LocalDate currentDate = LocalDate.now();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = currentDate.minusMonths(i);
            String month = monthDate.format(monthFormatter);
            last6Months.add(month);
            monthlyResolutionTimes.put(month, 0.0);
            monthlyResolutionCounts.put(month, 0);
        }
        
        // Trouver les réclamations résolues
        List<Reclamation> resolvedReclamations = allReclamations.stream()
            .filter(r -> r.getStatut().equalsIgnoreCase("resolu"))
            .collect(Collectors.toList());
        
        // Simuler les dates de résolution et calculer le temps de résolution pour chaque réclamation
        Map<Integer, LocalDate> simulatedResolutionDates = new HashMap<>();
        for (Reclamation r : resolvedReclamations) {
            // Simuler une date de résolution entre la date de création et aujourd'hui
            LocalDate creationDate = r.getDate();
            LocalDate now = LocalDate.now();
            long daysBetween = ChronoUnit.DAYS.between(creationDate, now);
            
            // La résolution a pris entre 1 et daysBetween jours (ou 14 jours maximum pour des données plus réalistes)
            long resolutionDays = Math.min(daysBetween, 14);
            resolutionDays = Math.max(1, (long)(Math.random() * resolutionDays));
            
            LocalDate resolutionDate = creationDate.plusDays(resolutionDays);
            simulatedResolutionDates.put(r.getId(), resolutionDate);
            
            // Ajouter au mois correspondant
            String month = resolutionDate.format(monthFormatter);
            if (monthlyResolutionTimes.containsKey(month)) {
                monthlyResolutionTimes.put(month, monthlyResolutionTimes.get(month) + resolutionDays);
                monthlyResolutionCounts.put(month, monthlyResolutionCounts.get(month) + 1);
            }
        }
        
        // Calculer les moyennes par mois
        XYChart.Series<String, Number> resolutionSeries = new XYChart.Series<>();
        resolutionSeries.setName("Temps moyen (jours)");
        
        for (String month : last6Months) {
            double totalDays = monthlyResolutionTimes.get(month);
            int count = monthlyResolutionCounts.get(month);
            
            // Calculer la moyenne ou utiliser une valeur par défaut
            double average = count > 0 ? totalDays / count : 0;
            
            // Si aucune donnée pour ce mois, utiliser une valeur aléatoire entre 3 et 10 jours
            if (average == 0) {
                average = 3 + Math.random() * 7;
            }
            
            // Formater le mois pour l'affichage (MM/YYYY -> MMM)
            try {
                LocalDate date = LocalDate.parse("01/" + month, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                String displayMonth = date.format(DateTimeFormatter.ofPattern("MMM"));
                resolutionSeries.getData().add(new XYChart.Data<>(displayMonth, average));
            } catch (Exception e) {
                // En cas d'erreur, utiliser le mois tel quel
                resolutionSeries.getData().add(new XYChart.Data<>(month, average));
            }
        }
        
        // Mettre à jour le graphique
        resolutionTimeChart.getData().clear();
        resolutionTimeChart.getData().add(resolutionSeries);
        
        // Personnaliser l'apparence du graphique
        for (XYChart.Data<String, Number> data : resolutionSeries.getData()) {
            // Ajouter un tooltip pour montrer la valeur exacte
            Tooltip tooltip = new Tooltip(String.format("%.1f jours", data.getYValue().doubleValue()));
            Tooltip.install(data.getNode(), tooltip);
            
            // Colorer les points du graphique
            data.getNode().setStyle("-fx-background-color: #4285F4;");
        }
    }
    
    private void updateSatisfactionRate() {
        // Calculer le taux de satisfaction basé sur les avis si disponible, sinon simuler
        double satisfactionRate;
        
        if (allAvis != null && !allAvis.isEmpty()) {
            // Utiliser les notes des avis pour calculer la satisfaction
            // On suppose que les notes sont sur une échelle de 1 à 5
            double totalRating = 0;
            int validRatings = 0;
            
            for (Avis avis : allAvis) {
                try {
                    String ratingStr = avis.getRating();
                    // Si la note est stockée sous forme de texte (ex: "4/5"), extraire le nombre
                    if (ratingStr != null && ratingStr.contains("/")) {
                        ratingStr = ratingStr.split("/")[0].trim();
                    }
                    
                    if (ratingStr != null && !ratingStr.isEmpty()) {
                        int rating = Integer.parseInt(ratingStr);
                        totalRating += rating;
                        validRatings++;
                    }
                } catch (NumberFormatException e) {
                    // Ignorer les notes qui ne peuvent pas être converties en nombre
                    System.out.println("Impossible de convertir la note: " + avis.getRating());
                }
            }
            
            if (validRatings > 0) {
                // Convertir la moyenne des notes (1-5) en pourcentage (0-100%)
                double averageRating = totalRating / validRatings;
                satisfactionRate = averageRating / 5.0;  // 5 étant la note maximale
            } else {
                // Si aucune note valide, utiliser la valeur par défaut
                satisfactionRate = calculateDefaultSatisfactionRate();
            }
        } else {
            // Si aucun avis n'est disponible, calculer en fonction des réclamations résolues
            satisfactionRate = calculateDefaultSatisfactionRate();
        }
        
        // Si le taux est trop bas ou non calculé, utiliser une valeur minimale pour le visuel
        if (satisfactionRate < 0.05) {
            satisfactionRate = 0.65; // Valeur par défaut pour la démo (65%)
        }
        
        // Appliquer un effet de transition pour l'indicateur
        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(satisfactionIndicator.progressProperty(), satisfactionRate);
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1.5), keyValue);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
        
        // Mettre à jour le label avec le pourcentage
        int percentage = (int) Math.round(satisfactionRate * 100);
        
        // Changer la couleur en fonction du taux de satisfaction
        String color;
        if (percentage >= 75) {
            color = "#4CAF50"; // Vert
        } else if (percentage >= 50) {
            color = "#FFA726"; // Orange
        } else {
            color = "#F44336"; // Rouge
        }
        
        satisfactionLabel.setText(percentage + "%");
        satisfactionLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        satisfactionIndicator.setStyle("-fx-progress-color: " + color + ";");
    }
    
    private double calculateDefaultSatisfactionRate() {
        // Simuler un taux de satisfaction basé sur le pourcentage de réclamations résolues
        double baseRate = 0.5; // Taux de base de 50%
        
        if (allReclamations != null && !allReclamations.isEmpty()) {
            long resolvedCount = allReclamations.stream()
                .filter(r -> r.getStatut().equalsIgnoreCase("resolu"))
                .count();
            
            double resolvedRatio = (double) resolvedCount / allReclamations.size();
            
            // Formule pour calculer le taux de satisfaction:
            // 50% de base + jusqu'à 50% supplémentaires basés sur le ratio de réclamations résolues
            return baseRate + (resolvedRatio * 0.5);
        }
        
        // En l'absence de données, retourner une valeur entre 60% et 80%
        return 0.65 + (Math.random() * 0.15);
    }
    
    private void initializeMap() {
        try {
            // Vérifier si le conteneur de carte est disponible
            if (mapContainer == null) {
                System.out.println("Le conteneur de carte n'est pas disponible.");
                return;
            }
            
            // Effacer le contenu précédent
            mapContainer.getChildren().clear();
            
            // Créer un message de chargement
            Label loadingLabel = new Label("Chargement de la carte...");
            loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
            ProgressIndicator progress = new ProgressIndicator();
            progress.setPrefSize(40, 40);
            
            VBox loadingBox = new VBox(10, progress, loadingLabel);
            loadingBox.setAlignment(javafx.geometry.Pos.CENTER);
            mapContainer.getChildren().add(loadingBox);
            
            // Utiliser un thread séparé pour charger la carte
            Thread mapThread = new Thread(() -> {
                try {
                    // Générer le HTML de la carte
                    String mapHTML = buildMapHTML();
                    
                    // Charger la carte dans le thread JavaFX
                    javafx.application.Platform.runLater(() -> {
                        try {
                            // Nettoyer le conteneur
                            mapContainer.getChildren().clear();
                            
                            // Générer la carte avec WebView
                            loadMapInWebView(mapContainer, mapHTML);
                            
                            // Ajouter une légende sous la carte
                            mapContainer.getChildren().add(createMapLegend());
                            
                            // Ajouter un message de statut
                            Label statusLabel = new Label(getMapStatusText());
                            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575; -fx-padding: 5 0 0 0;");
                            mapContainer.getChildren().add(statusLabel);
                            
                        } catch (Exception e) {
                            System.out.println("Erreur lors du chargement de la carte: " + e.getMessage());
                            e.printStackTrace();
                            
                            // Afficher un message d'erreur dans le conteneur
                            mapContainer.getChildren().clear();
                            Label errorLabel = new Label("Impossible de charger la carte.");
                            errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #d32f2f;");
                            Button retryButton = new Button("Réessayer");
                            retryButton.setOnAction(event -> initializeMap());
                            
                            VBox errorBox = new VBox(10, errorLabel, retryButton);
                            errorBox.setAlignment(javafx.geometry.Pos.CENTER);
                            mapContainer.getChildren().add(errorBox);
                        }
                    });
                } catch (Exception e) {
                    System.out.println("Erreur lors de la création de la carte: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Afficher un message d'erreur dans le thread JavaFX
                    javafx.application.Platform.runLater(() -> {
                        mapContainer.getChildren().clear();
                        Label errorLabel = new Label("Impossible de créer la carte: " + e.getMessage());
                        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #d32f2f;");
                        Button retryButton = new Button("Réessayer");
                        retryButton.setOnAction(event -> initializeMap());
                        
                        VBox errorBox = new VBox(10, errorLabel, retryButton);
                        errorBox.setAlignment(javafx.geometry.Pos.CENTER);
                        mapContainer.getChildren().add(errorBox);
                    });
                }
            });
            
            mapThread.setDaemon(true);
            mapThread.start();
            
        } catch (Exception e) {
            System.out.println("Erreur critique lors de l'initialisation de la carte: " + e.getMessage());
            e.printStackTrace();
            
            // En cas d'erreur critique, afficher une image statique de secours si disponible
            try {
                mapContainer.getChildren().clear();
                Label errorLabel = new Label("Impossible de charger la carte interactive.");
                errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #d32f2f;");
                
                // Ajouter une image statique en secours
                try {
                    ImageView fallbackMap = new ImageView(new Image(getClass().getResourceAsStream("/images/fallback_map.png")));
                    fallbackMap.setFitWidth(mapContainer.getPrefWidth() - 20);
                    fallbackMap.setPreserveRatio(true);
                    mapContainer.getChildren().addAll(errorLabel, fallbackMap);
                } catch (Exception ex) {
                    // Si l'image de secours n'est pas disponible, afficher juste l'erreur
                    mapContainer.getChildren().add(errorLabel);
                }
            } catch (Exception ex) {
                System.out.println("Erreur fatale lors de l'affichage du message d'erreur: " + ex.getMessage());
            }
        }
    }
    
    // Méthode séparée pour charger la WebView (assure que c'est exécuté sur le thread JavaFX)
    private void loadMapInWebView(VBox mapBox, String mapHTML) {
        try {
            // Créer une WebView pour afficher la carte
            WebView webView = new WebView();
            webView.setPrefSize(850, 320);
            WebEngine webEngine = webView.getEngine();
            
            // Activer le débogage JavaScript
            webEngine.setJavaScriptEnabled(true);
            
            // Configurer l'écouteur pour surveiller le chargement
            webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    System.out.println("Carte chargée avec succès");
                    
                    try {
                        // Supprimer l'indicateur de chargement
                        mapBox.getChildren().clear();
                        mapBox.getChildren().add(webView);
                        
                        // Ajouter une légende pour les statuts
                        HBox legend = createMapLegend();
                        mapBox.getChildren().add(legend);
                        
                        // Ajouter une note explicative et le nombre de marqueurs
                        Label noteLabel = new Label(getMapStatusText());
                        noteLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666666; -fx-font-size: 11px;");
                        mapBox.getChildren().add(noteLabel);
                    } catch (Exception e) {
                        System.out.println("Erreur après le chargement de la carte: " + e.getMessage());
                    }
                } else if (newValue == Worker.State.FAILED) {
                    System.out.println("Échec du chargement de la carte: " + 
                        (webEngine.getLoadWorker().getException() != null ? 
                         webEngine.getLoadWorker().getException().getMessage() : "Erreur inconnue"));
                    
                    try {
                        // Supprimer l'indicateur de chargement
                        mapBox.getChildren().clear();
                        
                        // Afficher un message d'erreur
                        Label errorLabel = new Label("Impossible de charger la carte. Vérifiez votre connexion internet.");
                        errorLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                        mapBox.getChildren().add(errorLabel);
                        
                        // Ajouter un bouton pour réessayer
                        Button retryButton = new Button("Réessayer");
                        retryButton.setOnAction(e -> initializeMap());
                        mapBox.getChildren().add(retryButton);
                    } catch (Exception e) {
                        System.out.println("Erreur lors de l'affichage de l'erreur de chargement: " + e.getMessage());
                    }
                }
            });
            
            // Charger le HTML directement depuis une chaîne
            webEngine.loadContent(mapHTML);
        } catch (Exception e) {
            System.out.println("Erreur lors de la création de la WebView sur le thread JavaFX: " + e.getMessage());
            e.printStackTrace();
            
            try {
                // Afficher l'erreur dans l'interface
                mapBox.getChildren().clear();
                Label errorLabel = new Label("Erreur: " + e.getMessage());
                errorLabel.setStyle("-fx-text-fill: #F44336;");
                Button retryButton = new Button("Réessayer");
                retryButton.setOnAction(event -> initializeMap());
                mapBox.getChildren().addAll(errorLabel, retryButton);
            } catch (Exception ex) {
                System.out.println("Erreur critique lors de l'affichage de l'erreur: " + ex.getMessage());
            }
        }
    }
    
    private String buildMapHTML() {
            // Construire le HTML pour la carte OpenStreetMap avec Leaflet
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n")
                .append("  <title>Carte des réclamations</title>\n")
                .append("  <meta charset=\"utf-8\">\n")
                .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
                .append("  <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.css\"\n")
                .append("   integrity=\"sha512-xodZBNTC5n17Xt2atTPuE1HxjVMSvLVW9ocqUKLsCC5CXdbqCmblAshOMAS6/keqq/sMZMZ19scR4PsZChSR7A==\"\n")
                .append("   crossorigin=\"\"/>\n")
                .append("  <script src=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.js\"\n")
                .append("   integrity=\"sha512-XQoYMqMTK8LvdxXYG3nZ448hOEQiglfqkJs1NOQV44cWnUrBc8PkAOcXy20w0vlaXaVUearIOBhiXZ5V3ynxwA==\"\n")
                .append("   crossorigin=\"\"></script>\n")
                .append("  <style>\n")
                .append("    html, body {\n")
                .append("      height: 100%;\n")
                .append("      margin: 0;\n")
                .append("      padding: 0;\n")
                .append("    }\n")
                .append("    #map {\n")
                .append("      height: 100%;\n")
                .append("      width: 100%;\n")
            .append("    }\n")
            .append("    .status-marker {\n")
            .append("      border-radius: 50%;\n")
            .append("      text-align: center;\n")
            .append("      box-shadow: 0 0 5px rgba(0,0,0,0.5);\n")
            .append("    }\n")
            .append("    .status-ouvert { background-color: #1a73e8; }\n")
            .append("    .status-en-cours { background-color: #FFA726; }\n")
            .append("    .status-resolu { background-color: #4CAF50; }\n")
            .append("    .status-rejete { background-color: #F44336; }\n")
            .append("    .custom-popup .leaflet-popup-content-wrapper {\n")
            .append("      background: white;\n")
            .append("      color: #333;\n")
            .append("      box-shadow: 0 3px 10px rgba(0,0,0,0.15);\n")
            .append("      border-radius: 5px;\n")
                .append("    }\n")
                .append("  </style>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("  <div id=\"map\"></div>\n")
                .append("  <script>\n")
            .append("    function initMap() {\n")
            .append("      var map = L.map('map', {\n")
            .append("        attributionControl: false,\n")
            .append("        zoomControl: true\n")
            .append("      }).setView([36.8065, 10.1815], 11);\n")
            .append("\n")
            .append("      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n")
            .append("        maxZoom: 19,\n")
            .append("        attribution: '© OpenStreetMap'\n")
            .append("      }).addTo(map);\n")
            .append("\n");
        
        // Vérifier si nous avons des réclamations
        final boolean hasReclamations = allReclamations != null && !allReclamations.isEmpty();
                Random random = new Random();
        
        // Créer une classe pour encapsuler la variable du compteur de marqueurs
        final class MarkerCounter {
            private int count = 0;
            
            public int getAndIncrement() {
                return count++;
            }
            
            public int get() {
                return count;
            }
        }
        
        final MarkerCounter markerCounter = new MarkerCounter();
        
        // Si nous n'avons pas de réclamations, créer des données de démonstration
        List<Map<String, Object>> demoMarkers = new ArrayList<>();
        if (!hasReclamations) {
            // Créer des marqueurs de démonstration
            String[] statuts = {"ouvert", "en cours", "resolu", "rejete"};
            String[] titres = {
                "Problème d'éclairage", "Nid de poule", "Déchets non collectés", 
                "Fuite d'eau", "Graffiti", "Signalisation endommagée", 
                "Trottoir cassé", "Arbre tombé", "Banc public cassé"
            };
            
            // Centrer sur Tunis
            double baseLat = 36.8065;
            double baseLng = 10.1815;
            
            // Créer 15 marqueurs de démonstration
            for (int i = 0; i < 15; i++) {
                Map<String, Object> marker = new HashMap<>();
                marker.put("lat", baseLat + (random.nextDouble() - 0.5) * 0.1);
                marker.put("lng", baseLng + (random.nextDouble() - 0.5) * 0.1);
                marker.put("status", statuts[random.nextInt(statuts.length)]);
                marker.put("title", titres[random.nextInt(titres.length)]);
                marker.put("date", LocalDate.now().minusDays(random.nextInt(30)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                demoMarkers.add(marker);
            }
        }
        
        // Ajouter des marqueurs pour les réclamations réelles ou simulées
        if (hasReclamations) {
            int total = Math.min(allReclamations.size(), 20); // Limiter à 20 marqueurs max
                
                for (int i = 0; i < total; i++) {
                    Reclamation r = allReclamations.get(i);
                    
                    // Utiliser les coordonnées réelles si disponibles, sinon générer aléatoirement
                    double latitude = r.getLatitude();
                    double longitude = r.getLongitude();
                    
                    // Si les coordonnées ne sont pas définies ou sont à 0, générer des coordonnées aléatoires
                    if (latitude == 0 || longitude == 0) {
                        latitude = 36.8065 + (random.nextDouble() - 0.5) * 0.2;  // Autour de Tunis
                        longitude = 10.1815 + (random.nextDouble() - 0.5) * 0.2;
                    }
                    
                    // Déterminer la couleur du marqueur selon le statut
                    String color;
                    switch (r.getStatut().toLowerCase()) {
                        case "ouvert":
                            color = "blue";
                            break;
                        case "en cours":
                            color = "orange";
                            break;
                        case "resolu":
                            color = "green";
                            break;
                        case "rejete":
                            color = "red";
                            break;
                        default:
                            color = "gray";
                            break;
                    }
                    
                    String popupContent = "<b>" + r.getTitre() + "</b><br>" +
                                         "Statut: " + r.getStatut() + "<br>" +
                                         "Date: " + r.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                                     (r.getAdresse() != null && !r.getAdresse().isEmpty() ? "<br>Adresse: " + r.getAdresse().replace("'", "\\'") : "");
                
                int currentMarker = markerCounter.getAndIncrement();
                htmlBuilder.append("      var marker" + currentMarker + " = L.marker([" + latitude + ", " + longitude + "], {\n")
                        .append("        icon: L.divIcon({\n")
                        .append("          className: 'status-marker status-" + r.getStatut().toLowerCase().replace(" ", "-") + "',\n")
                        .append("          html: '<div style=\"width: 12px; height: 12px; border-radius: 50%; background-color: " + color + "; border: 2px solid white;\"></div>',\n")
                        .append("          iconSize: [16, 16],\n")
                        .append("          iconAnchor: [8, 8]\n")
                        .append("        })\n")
                        .append("      }).addTo(map);\n")
                        .append("      marker" + currentMarker + ".bindPopup('" + popupContent + "');\n\n");
            }
        } else {
            // Ajouter les marqueurs de démonstration
            for (int i = 0; i < demoMarkers.size(); i++) {
                Map<String, Object> marker = demoMarkers.get(i);
                double latitude = (double) marker.get("lat");
                double longitude = (double) marker.get("lng");
                String status = (String) marker.get("status");
                String title = (String) marker.get("title");
                String date = (String) marker.get("date");
                
                // Déterminer la couleur du marqueur selon le statut
                String color;
                switch (status) {
                    case "ouvert":
                        color = "blue";
                        break;
                    case "en cours":
                        color = "orange";
                        break;
                    case "resolu":
                        color = "green";
                        break;
                    case "rejete":
                        color = "red";
                        break;
                    default:
                        color = "gray";
                        break;
                }
                
                String popupContent = "<b>" + title.replace("'", "\\'") + "</b><br>" +
                                     "Statut: " + status + "<br>" +
                                     "Date: " + date;
                
                int currentMarker = markerCounter.getAndIncrement();
                htmlBuilder.append("      var marker" + currentMarker + " = L.marker([" + latitude + ", " + longitude + "], {\n")
                        .append("        icon: L.divIcon({\n")
                        .append("          className: 'status-marker status-" + status.replace(" ", "-") + "',\n")
                        .append("          html: '<div style=\"width: 12px; height: 12px; border-radius: 50%; background-color: " + color + "; border: 2px solid white;\"></div>',\n")
                        .append("          iconSize: [16, 16],\n")
                        .append("          iconAnchor: [8, 8]\n")
                        .append("        })\n")
                        .append("      }).addTo(map);\n")
                        .append("      marker" + currentMarker + ".bindPopup('" + popupContent + "');\n\n");
            }
        }
        
        // Finaliser le HTML
        htmlBuilder.append("    }\n")
                  .append("    try {\n")
                  .append("      initMap();\n")
                  .append("      console.log('Carte initialisée avec succès');\n")
                  .append("    } catch (error) {\n")
                  .append("      console.error('Erreur l\\'initialisation de la carte: ' + error.message);\n")
                  .append("    }\n")
                  .append("  </script>\n")
                      .append("</body>\n")
                      .append("</html>");
            
        return htmlBuilder.toString();
    }
    
    private HBox createMapLegend() {
                    // Ajouter une légende pour les statuts
                    HBox legend = new HBox(15);
                    legend.setAlignment(Pos.CENTER);
                    legend.setPadding(new Insets(10, 0, 5, 0));
                    
                    // Créer des entrées de légende pour chaque statut
                    String[][] statusInfo = {
                        {"Ouvert", "blue"},
                        {"En cours", "orange"},
                        {"Résolu", "green"},
                        {"Rejeté", "red"}
                    };
                    
                    for (String[] status : statusInfo) {
                        HBox item = new HBox(5);
                        item.setAlignment(Pos.CENTER);
                        
                        Rectangle colorRect = new Rectangle(12, 12);
                        colorRect.setFill(Color.web(status[1]));
                        colorRect.setStroke(Color.WHITE);
                        colorRect.setStrokeWidth(1);
                        
                        Label label = new Label(status[0]);
                        label.setStyle("-fx-font-size: 12px;");
                        
                        item.getChildren().addAll(colorRect, label);
                        legend.getChildren().add(item);
                    }
                    
        return legend;
    }
    
    private String getMapStatusText() {
        try {
            // Vérifier si nous avons des réclamations
            final boolean hasReclamations = allReclamations != null && !allReclamations.isEmpty();
            
            // Compter le nombre de marqueurs réels ou simulés
            int markerCount = 0;
            if (hasReclamations) {
                markerCount = Math.min(allReclamations.size(), 20);
            } else {
                markerCount = 15; // Nombre par défaut de marqueurs de démonstration
            }
            
            // Ajouter une note explicative et le nombre de marqueurs
            return hasReclamations 
                ? "Carte interactive des réclamations. " + markerCount + " réclamations affichées."
                : "Carte de démonstration. " + markerCount + " exemples affichés.";
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du texte de statut de la carte: " + e.getMessage());
            return "Carte de démonstration.";
        }
    }
    
    @FXML
    public void exportToPDF() {
        try {
            // Créer le sélecteur de fichier
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            
            // Générer un nom de fichier basé sur la date
            String defaultFileName = "rapport_reclamations_" + 
                                     LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
                                     ".pdf";
            fileChooser.setInitialFileName(defaultFileName);
        
        // Montrer la boîte de dialogue
            File file = fileChooser.showSaveDialog(statusChart.getScene().getWindow());
        
        if (file != null) {
                // Afficher une barre de progression
                Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
                progressAlert.setTitle("Export en cours");
                progressAlert.setHeaderText("Génération du rapport PDF");
                progressAlert.setContentText("Veuillez patienter pendant la génération du rapport...");
                
                // Créer une barre de progression
                ProgressBar progressBar = new ProgressBar();
                progressBar.setPrefWidth(progressAlert.getWidth() - 40);
                
                // Remplacer le contenu par la barre de progression
                progressAlert.getDialogPane().setContent(progressBar);
                
                // Générer réellement le PDF avec iText
                Thread exportThread = new Thread(() -> {
                    try {
                        // Mettre à jour la barre de progression
                        javafx.application.Platform.runLater(() -> progressBar.setProgress(0.1));
                        
                        // Créer le document PDF
                        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                        com.itextpdf.text.pdf.PdfWriter.getInstance(document, new FileOutputStream(file));
                        document.open();
                        
                        // Ajouter un titre
                        javafx.application.Platform.runLater(() -> progressBar.setProgress(0.2));
                        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
                        document.add(new com.itextpdf.text.Paragraph("Rapport des Réclamations", titleFont));
                        document.add(new com.itextpdf.text.Paragraph("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
                        document.add(new com.itextpdf.text.Paragraph(" "));
                        
                        // Ajouter un résumé
                        javafx.application.Platform.runLater(() -> progressBar.setProgress(0.3));
                        document.add(new com.itextpdf.text.Paragraph("Résumé des Réclamations"));
                        document.add(new com.itextpdf.text.Paragraph("Nombre total: " + totalReclamationsCount.getText()));
                        document.add(new com.itextpdf.text.Paragraph("En attente: " + pendingReclamationsCount.getText()));
                        document.add(new com.itextpdf.text.Paragraph("Résolues: " + resolvedReclamationsCount.getText()));
                        document.add(new com.itextpdf.text.Paragraph("Urgentes: " + urgentReclamationsCount.getText()));
                        document.add(new com.itextpdf.text.Paragraph(" "));
                        
                        // Créer un tableau pour les réclamations
                        javafx.application.Platform.runLater(() -> progressBar.setProgress(0.5));
                        com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(5);
                        table.setWidthPercentage(100);
                        
                        // Ajouter les en-têtes du tableau
                        table.addCell("ID Ticket");
                        table.addCell("Titre");
                        table.addCell("Date");
                        table.addCell("Statut");
                        table.addCell("Priorité");
                        
                        // Remplir le tableau avec les données
                        javafx.application.Platform.runLater(() -> progressBar.setProgress(0.7));
                        if (allReclamations != null && !allReclamations.isEmpty()) {
                            int count = 0;
                            int total = Math.min(allReclamations.size(), 50); // Limiter à 50 réclamations max
                            
                            for (Reclamation r : allReclamations) {
                                if (count++ >= total) break;
                                
                                table.addCell(r.getTicketId());
                                table.addCell(r.getTitre());
                                table.addCell(r.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                                table.addCell(r.getStatut());
                                table.addCell(String.valueOf(r.getPriorite()));
                            }
                        }
                        
                        // Ajouter le tableau au document
                        document.add(table);
                        javafx.application.Platform.runLater(() -> progressBar.setProgress(0.9));
                        
                        // Ajouter un pied de page
                        document.add(new com.itextpdf.text.Paragraph(" "));
                        document.add(new com.itextpdf.text.Paragraph("Rapport généré automatiquement par l'application WeGo"));
                        
                        // Fermer le document
                        document.close();
                        javafx.application.Platform.runLater(() -> progressBar.setProgress(1.0));
                        
                        // Fermer l'alerte de progression
                        javafx.application.Platform.runLater(() -> {
                            progressAlert.close();
                            showAlert("Export PDF", "Le rapport a été exporté avec succès vers:\n" + file.getAbsolutePath());
                        });
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        javafx.application.Platform.runLater(() -> {
                            progressAlert.close();
                            showAlert("Erreur", "L'export a échoué: " + e.getMessage());
                        });
                    }
                });
                
                exportThread.setDaemon(true);
                exportThread.start();
                
                // Afficher l'alerte de progression
                progressAlert.show();
            }
        } catch (Exception e) {
            showAlert("Erreur d'export", "Une erreur est survenue lors de l'export: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void exportToExcel() {
        try {
            // Vérifier d'abord si des données sont disponibles
            if (allReclamations == null || allReclamations.isEmpty()) {
                showAlert("Aucune donnée", "Il n'y a aucune réclamation à exporter.");
                return;
            }
            
            // Créer le sélecteur de fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les données");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
                
            // Générer un nom de fichier basé sur la date
            String defaultFileName = "donnees_reclamations_" + 
                                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
                                    ".csv";
            fileChooser.setInitialFileName(defaultFileName);
                
            // Définir le répertoire initial (dossier Documents de l'utilisateur)
            try {
                String userHome = System.getProperty("user.home");
                File documentsDir = new File(userHome, "Documents");
                if (documentsDir.exists()) {
                    fileChooser.setInitialDirectory(documentsDir);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la définition du répertoire initial: " + e.getMessage());
                // Continuer sans définir de répertoire initial
            }
            
            // Montrer la boîte de dialogue
            File file = null;
            try {
                file = fileChooser.showSaveDialog(statusChart.getScene().getWindow());
            } catch (Exception e) {
                showAlert("Erreur", "Impossible d'afficher la boîte de dialogue de sauvegarde: " + e.getMessage());
                return;
            }
            
            if (file != null) {
                final File selectedFile = file; // Créer une référence finale pour le thread
                
                // Afficher une barre de progression
                Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
                progressAlert.setTitle("Export en cours");
                progressAlert.setHeaderText("Génération du fichier CSV");
                progressAlert.setContentText("Veuillez patienter pendant la génération du fichier...");
                
                // Créer une barre de progression
                ProgressBar progressBar = new ProgressBar();
                progressBar.setPrefWidth(progressAlert.getWidth() - 40);
                
                // Remplacer le contenu par la barre de progression
                progressAlert.getDialogPane().setContent(progressBar);
                
                // Générer réellement le fichier CSV
                Thread exportThread = new Thread(() -> {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                        // Écrire l'en-tête
                        writer.write("ID,Ticket ID,Titre,Description,Date,Statut,Priorité,Catégorie,Gravité,Utilisateur");
                        writer.newLine();
                        
                        // Écrire les données
                        int rowNum = 0;
                        int totalRows = allReclamations.size();
                        for (Reclamation reclamation : allReclamations) {
                            // Mettre à jour la progression
                            final double progress = totalRows > 0 ? (double) ++rowNum / totalRows : 0;
                            javafx.application.Platform.runLater(() -> progressBar.setProgress(progress));
                            
                            try {
                                // Échapper les textes qui contiennent des virgules
                                String titre = escapeCsv(reclamation.getTitre());
                                String description = escapeCsv(reclamation.getDescription());
                                String statut = escapeCsv(reclamation.getStatut());
                                String categorie = escapeCsv(reclamation.getCategorie());
                                String gravite = escapeCsv(reclamation.getGravite());
                                String username = reclamation.getUser() != null ? 
                                                escapeCsv(reclamation.getUser().getUsername()) : "Inconnu";
                                
                                // Formatter la date
                                String dateStr = reclamation.getDate() != null ? 
                                                reclamation.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                                
                                // Construire la ligne CSV
                                StringBuilder line = new StringBuilder();
                                line.append(reclamation.getId()).append(",");
                                line.append(escapeCsv(reclamation.getTicketId())).append(",");
                                line.append(titre).append(",");
                                line.append(description).append(",");
                                line.append(dateStr).append(",");
                                line.append(statut).append(",");
                                line.append(reclamation.getPriorite()).append(",");
                                line.append(categorie).append(",");
                                line.append(gravite).append(",");
                                line.append(username);
                                
                                // Écrire la ligne
                                writer.write(line.toString());
                                writer.newLine();
                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'écriture de la ligne " + rowNum + ": " + e.getMessage());
                                // Continuer avec la prochaine ligne
                            }
                        }
                        
                        // Fermer l'alerte de progression
                        javafx.application.Platform.runLater(() -> {
                            try {
                                progressAlert.close();
                                
                                // Demander à l'utilisateur s'il souhaite ouvrir le fichier
                                Alert confirmOpen = new Alert(Alert.AlertType.CONFIRMATION);
                                confirmOpen.setTitle("Export terminé");
                                confirmOpen.setHeaderText("Le fichier a été exporté avec succès");
                                confirmOpen.setContentText("Voulez-vous ouvrir le fichier maintenant ?");
                                
                                Optional<ButtonType> result = confirmOpen.showAndWait();
                                if (result.isPresent() && result.get() == ButtonType.OK) {
                                    // Ouvrir le fichier avec l'application par défaut
                                    try {
                                        java.awt.Desktop.getDesktop().open(selectedFile);
                                    } catch (Exception e) {
                                        showAlert("Impossible d'ouvrir le fichier", 
                                                "Le fichier a été créé à " + selectedFile.getAbsolutePath() + 
                                                " mais ne peut pas être ouvert automatiquement.");
                                    }
                                }
                            } catch (Exception e) {
                                showAlert("Export terminé",
                                        "Le fichier a été créé à " + selectedFile.getAbsolutePath());
                            }
                        });
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        javafx.application.Platform.runLater(() -> {
                            try {
                                progressAlert.close();
                            } catch (Exception ex) {
                                // Ignorer l'erreur si l'alerte est déjà fermée
                            }
                            showAlert("Erreur d'export", 
                                    "Une erreur est survenue lors de l'export du fichier: " + e.getMessage());
                        });
                    }
                });
                
                exportThread.setDaemon(true);
                exportThread.start();
                
                // Afficher l'alerte de progression
                progressAlert.show();
            }
        } catch (Exception e) {
            showAlert("Erreur d'export", "Une erreur est survenue lors de l'export: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Méthode pour échapper les champs CSV
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        
        // Si le texte contient des virgules, des guillemets ou des sauts de ligne, l'entourer de guillemets
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            // Échapper les guillemets en les doublant
            value = value.replace("\"", "\"\"");
            // Entourer de guillemets
            return "\"" + value + "\"";
        }
        
        return value;
    }
    
    @FXML
    public void refreshDashboard() {
        try {
            System.out.println("Rafraîchissement du tableau de bord...");
            
            // Afficher un indicateur de chargement
            Label loadingLabel = new Label("Actualisation en cours...");
            loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
            recentReclamationsTable.setPlaceholder(loadingLabel);
            
            // Animation pour indiquer le rechargement
            if (statusChart != null) statusChart.setOpacity(0.5);
            if (categoriesChart != null) categoriesChart.setOpacity(0.5);
            if (recentReclamationsTable != null) recentReclamationsTable.setOpacity(0.5);
            
            // Réinitialiser les données des graphiques pour éviter les problèmes d'affichage
            try {
                if (statusChart != null) statusChart.getData().clear();
                if (categoriesChart != null) categoriesChart.getData().clear();
                if (resolutionTimeChart != null) resolutionTimeChart.getData().clear();
            } catch (Exception e) {
                System.out.println("Erreur lors de la réinitialisation des graphiques: " + e.getMessage());
            }
            
            // Créer un thread pour charger les données en arrière-plan
            Thread refreshThread = new Thread(() -> {
                // Initialiser avec des listes vides par défaut
                List<Reclamation> reclamations = new ArrayList<>();
                List<Avis> avis = new ArrayList<>();
                
                try {
                    // Réinitialiser les services pour forcer un rechargement complet
                    reclamationService = new ReclamationService();
                    avisService = new AvisService();
                    
                    // Essayer de récupérer les données
                    try {
                        reclamations = reclamationService.voir();
                        System.out.println("Réclamations récupérées avec succès: " + reclamations.size());
                    } catch (Exception e) {
                        System.out.println("Erreur lors de la récupération des réclamations: " + e.getMessage());
                    }
                    
                    try {
                        avis = avisService.getAll();
                        System.out.println("Avis récupérés avec succès: " + avis.size());
                    } catch (Exception e) {
                        System.out.println("Erreur lors de la récupération des avis: " + e.getMessage());
                    }
                    
                    // Capture finale des données pour le lambda
                    final List<Reclamation> finalReclamations = reclamations;
                    final List<Avis> finalAvis = avis;
                    
                    // Mettre à jour les données sur le thread JavaFX
                    javafx.application.Platform.runLater(() -> {
                        try {
                            // Mettre à jour les listes de données
                            allReclamations = finalReclamations;
                            allAvis = finalAvis;
                            
                            System.out.println("Données rechargées: " + allReclamations.size() + " réclamations, " + 
                                             allAvis.size() + " avis");
                            
                            // Mettre à jour tous les composants de l'interface
                            updateCounters();
                            updateCharts();
                            updateRecentReclamationsTable();
                            updatePerformanceMetrics();
                            
                            // Réinitialiser la carte
                            try {
                                initializeMap();
                            } catch (Exception e) {
                                System.out.println("Erreur lors de la réinitialisation de la carte: " + e.getMessage());
                            }
                            
                            // Restaurer l'opacité avec une animation
                            FadeTransition fadeTransition1 = new FadeTransition(Duration.millis(500), statusChart);
                            fadeTransition1.setFromValue(0.5);
                            fadeTransition1.setToValue(1.0);
                            fadeTransition1.play();
                            
                            FadeTransition fadeTransition2 = new FadeTransition(Duration.millis(500), categoriesChart);
                            fadeTransition2.setFromValue(0.5);
                            fadeTransition2.setToValue(1.0);
                            fadeTransition2.play();
                            
                            FadeTransition fadeTransition3 = new FadeTransition(Duration.millis(500), recentReclamationsTable);
                            fadeTransition3.setFromValue(0.5);
                            fadeTransition3.setToValue(1.0);
                            fadeTransition3.play();
                            
                            // Afficher un message de confirmation discret
                            recentReclamationsTable.setPlaceholder(new Label("Aucune réclamation trouvée"));
                            
                        } catch (Exception e) {
                            System.out.println("Erreur lors de la mise à jour de l'interface: " + e.getMessage());
                            e.printStackTrace();
                            
                            // Restaurer l'opacité en cas d'erreur
                            if (statusChart != null) statusChart.setOpacity(1.0);
                            if (categoriesChart != null) categoriesChart.setOpacity(1.0);
                            if (recentReclamationsTable != null) recentReclamationsTable.setOpacity(1.0);
                            
                            showAlert("Erreur", "Une erreur est survenue lors de l'actualisation: " + e.getMessage());
                        }
                    });
                    
                } catch (Exception e) {
                    System.out.println("Erreur lors du rechargement des données: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Mettre à jour l'interface en cas d'erreur
                    javafx.application.Platform.runLater(() -> {
                        // Restaurer l'opacité
                        if (statusChart != null) statusChart.setOpacity(1.0);
                        if (categoriesChart != null) categoriesChart.setOpacity(1.0);
                        if (recentReclamationsTable != null) recentReclamationsTable.setOpacity(1.0);
                        
                        recentReclamationsTable.setPlaceholder(new Label("Erreur lors du chargement des données"));
                        
                        showAlert("Erreur", "Impossible de recharger les données: " + e.getMessage());
                    });
                }
            });
            
            refreshThread.setDaemon(true);
            refreshThread.start();
            
        } catch (Exception e) {
            System.out.println("Erreur critique lors du rafraîchissement: " + e.getMessage());
            e.printStackTrace();
            
            // Restaurer l'opacité en cas d'erreur
            if (statusChart != null) statusChart.setOpacity(1.0);
            if (categoriesChart != null) categoriesChart.setOpacity(1.0);
            if (recentReclamationsTable != null) recentReclamationsTable.setOpacity(1.0);
            
            showAlert("Erreur", "Une erreur est survenue lors de l'actualisation du tableau de bord: " + e.getMessage());
        }
    }
    
    @FXML
    public void backToHome(javafx.event.ActionEvent event) {
        try {
            System.out.println("Retour à la page d'accueil...");
            
            // Charger directement la page d'accueil
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/home_page.fxml"));
                Parent view = loader.load();
                
                // Obtenir la scène actuelle depuis l'événement bouton
                Button button = (Button) event.getSource();
                Scene currentScene = button.getScene();
                
                if (currentScene != null) {
                    currentScene.setRoot(view);
                    System.out.println("Navigation vers la page d'accueil réussie");
                } else {
                    System.out.println("Impossible d'obtenir la scène actuelle, tentative d'ouvrir une nouvelle fenêtre");
                    // Tenter d'ouvrir dans une nouvelle fenêtre
                    Stage stage = new Stage();
                    stage.setTitle("Page d'accueil");
                    stage.setScene(new Scene(view));
                    stage.show();
                }
            } catch (Exception e) {
                System.out.println("Erreur lors du chargement de la page d'accueil: " + e.getMessage());
                e.printStackTrace();
                showAlert("Erreur de navigation", "Impossible de retourner à la page d'accueil");
            }
        } catch (Exception e) {
            System.out.println("Erreur générale lors du retour à la page d'accueil: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur de navigation", "Impossible de retourner à la page d'accueil");
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 