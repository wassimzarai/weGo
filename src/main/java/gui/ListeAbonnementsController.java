package gui;

import entities.Abonnement;
import entities.Reservation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import services.AbonnementService;
import javafx.scene.chart.PieChart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

// Import pour PDF (iText)
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ListeAbonnementsController {

    @FXML
    private TableView<Abonnement> abonnementsTable;
    
    @FXML
    private TableColumn<Abonnement, Circle> statusCol;
    
    @FXML
    private TableColumn<Abonnement, Integer> idCol;
    
    @FXML
    private TableColumn<Abonnement, String> typeCol;
    
    @FXML
    private TableColumn<Abonnement, LocalDate> dateDebutCol;
    
    @FXML
    private TableColumn<Abonnement, LocalDate> dateFinCol;
    
    @FXML
    private TableColumn<Abonnement, BigDecimal> montantCol;
    
    @FXML
    private TableColumn<Abonnement, String> reservationCol;
    
    @FXML
    private TableColumn<Abonnement, String> statutCol;
    
    @FXML
    private TableColumn<Abonnement, Abonnement> actionsCol;
    
    @FXML
    private TextField filterField;
    
    @FXML
    private Label totalAbonnementsLabel;
    
    @FXML
    private Label actifsLabel;
    
    @FXML
    private Label expiresLabel;
    
    @FXML
    private Label montantTotalLabel;
    
    @FXML
    private Button btnRafraichir;
    
    @FXML
    private Button btnReservations;
    
    @FXML
    private Button btnExportPDF;
    
    @FXML
    private VBox notificationArea;
    
    @FXML
    private Label notificationTitle;
    
    @FXML
    private Label notificationMessage;
    
    @FXML
    private PieChart reservationPieChart;
    
    @FXML
    private VBox statsBox;
    
    private AbonnementService abonnementService;
    private ObservableList<Abonnement> abonnementsList;
    private FilteredList<Abonnement> filteredAbonnements;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    // Timer pour faire disparaitre les notifications
    private Timeline notificationTimeline;
    
    @FXML
    public void initialize() {
        abonnementService = new AbonnementService();
        
        // Configuration des colonnes
        setupTableColumns();
        
        // Chargement des données
        rafraichirListe();
        
        // Configuration du filtre de recherche
        setupSearchFilter();
        
        // Initialisation de la zone de notification
        if (notificationArea != null) {
            notificationArea.setVisible(false);
            notificationArea.setManaged(false);
        }
        
        if (statsBox != null) {
            statsBox.setVisible(false);
            statsBox.setManaged(false);
        }
        
        if (reservationPieChart != null) {
            reservationPieChart.setLabelsVisible(true);
            reservationPieChart.setLegendVisible(true);
        }
    }
    
    private void setupTableColumns() {
        // Colonne de statut (cercle coloré)
        statusCol.setCellValueFactory(cellData -> {
            String statut = cellData.getValue().getStatut();
            Circle circle = new Circle(8);
            
            if ("Actif".equals(statut)) {
                circle.setFill(Color.web("#27ae60"));
            } else if ("En attente".equals(statut)) {
                circle.setFill(Color.web("#f39c12"));
            } else if ("Expiré".equals(statut)) {
                circle.setFill(Color.web("#e74c3c"));
            } else {
                circle.setFill(Color.GRAY);
            }
            
            return new SimpleObjectProperty<>(circle);
        });
        
        // Colonnes de base
        idCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getIdAbonnement()));
        typeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTypeAbonnement()));
        dateDebutCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateDebut()));
        dateFinCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateFin()));
        montantCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getMontant()));
        statutCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatut()));
        
        // Format des dates
        dateDebutCol.setCellFactory(column -> new TableCell<Abonnement, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(DATE_FORMATTER.format(date));
                }
            }
        });
        
        dateFinCol.setCellFactory(column -> new TableCell<Abonnement, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText("Sans limite");
                } else {
                    setText(DATE_FORMATTER.format(date));
                }
            }
        });
        
        // Format du montant
        montantCol.setCellFactory(column -> new TableCell<Abonnement, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal montant, boolean empty) {
                super.updateItem(montant, empty);
                if (empty || montant == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", montant));
                }
            }
        });
        
        // Colonne réservation liée
        reservationCol.setCellValueFactory(cellData -> {
            Reservation reservation = cellData.getValue().getReservation();
            if (reservation != null) {
                return new SimpleStringProperty(String.format("%s (%s → %s)", 
                        reservation.getNomPassager(),
                        reservation.getPointDepart(),
                        reservation.getPointArrivee()));
            } else {
                return new SimpleStringProperty("ID: " + cellData.getValue().getReservationId());
            }
        });
        
        // Colonne d'actions
        setupActionsColumn();
    }
    
    private void setupActionsColumn() {
        actionsCol.setCellFactory(new Callback<TableColumn<Abonnement, Abonnement>, TableCell<Abonnement, Abonnement>>() {
            @Override
            public TableCell<Abonnement, Abonnement> call(TableColumn<Abonnement, Abonnement> param) {
                return new TableCell<Abonnement, Abonnement>() {
                    private final Button btnEditer = new Button("✎");
                    private final Button btnSupprimer = new Button("✕");
                    private final Button btnVoir = new Button("ℹ");
                    private final HBox hbox = new HBox(5, btnEditer, btnSupprimer, btnVoir);
                    
                    {
                        btnEditer.getStyleClass().add("action-icon");
                        btnSupprimer.getStyleClass().add("action-icon");
                        btnVoir.getStyleClass().add("action-icon");
                        
                        hbox.setAlignment(javafx.geometry.Pos.CENTER);
                        
                        btnEditer.setOnAction(event -> {
                            Abonnement abonnement = getTableView().getItems().get(getIndex());
                            ouvrirModifierAbonnement(abonnement);
                        });
                        
                        btnSupprimer.setOnAction(event -> {
                            Abonnement abonnement = getTableView().getItems().get(getIndex());
                            confirmerSuppression(abonnement);
                        });
                        
                        btnVoir.setOnAction(event -> {
                            Abonnement abonnement = getTableView().getItems().get(getIndex());
                            afficherDetailsAbonnement(abonnement);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Abonnement abonnement, boolean empty) {
                        super.updateItem(abonnement, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });
    }
    
    private void setupSearchFilter() {
        filteredAbonnements = new FilteredList<>(abonnementsList, p -> true);
        abonnementsTable.setItems(filteredAbonnements);
        
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredAbonnements.setPredicate(abonnement -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                if (abonnement.getTypeAbonnement().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (abonnement.getStatut().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (abonnement.getReservation() != null && 
                        abonnement.getReservation().getNomPassager().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(abonnement.getIdAbonnement()).contains(lowerCaseFilter)) {
                    return true;
                }
                
                return false;
            });
            
            updateStatistics();
        });
    }
    
    @FXML
    public void rafraichirListe() {
        List<Abonnement> abonnements = abonnementService.recupererTousAbonnements();
        abonnementsList = FXCollections.observableArrayList(abonnements);
        
        if (filteredAbonnements != null) {
            filteredAbonnements = new FilteredList<>(abonnementsList, filteredAbonnements.getPredicate());
            abonnementsTable.setItems(filteredAbonnements);
        } else {
            abonnementsTable.setItems(abonnementsList);
        }
        
        updateStatistics();
        updateReservationPieChart();
    }
    
    private void updateStatistics() {
        int total = abonnementsList.size();
        int actifs = 0;
        int expires = 0;
        BigDecimal montantTotal = BigDecimal.ZERO;
        
        for (Abonnement abonnement : abonnementsList) {
            if ("Actif".equals(abonnement.getStatut())) {
                actifs++;
            } else if ("Expiré".equals(abonnement.getStatut())) {
                expires++;
            }
            
            if (abonnement.getMontant() != null) {
                montantTotal = montantTotal.add(abonnement.getMontant());
            }
        }
        
        totalAbonnementsLabel.setText("Total: " + total + " abonnement" + (total > 1 ? "s" : ""));
        actifsLabel.setText("Actifs: " + actifs);
        expiresLabel.setText("Expirés: " + expires);
        montantTotalLabel.setText(String.format("Montant total: %.2f €", montantTotal));
    }
    
    private void updateReservationPieChart() {
        if (reservationPieChart == null) return;
        int accepte = 0, refuse = 0, attente = 0;
        for (Abonnement ab : abonnementsList) {
            String statut = ab.getStatut();
            if (statut == null) continue;
            if (statut.equalsIgnoreCase("Acceptée") || statut.equalsIgnoreCase("Accepté")) {
                accepte++;
            } else if (statut.equalsIgnoreCase("Refusée") || statut.equalsIgnoreCase("Refusé")) {
                refuse++;
            } else {
                attente++;
            }
        }
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Acceptée", accepte),
            new PieChart.Data("Refusée", refuse),
            new PieChart.Data("En attente", attente)
        );
        reservationPieChart.setData(pieChartData);
    }
    
    @FXML
    public void ouvrirAjout() {
        try {
            // Localiser le fichier FXML
            URL resourceURL = findFxmlResource("AjoutAbonnement.fxml");
            
            // Initialiser le chargeur FXML
            FXMLLoader loader = new FXMLLoader(resourceURL);
            Parent root = loader.load();
            
            // Configurer la scène
            Scene scene = new Scene(root);
            
            // Ajouter les styles CSS
            URL cssURL = findFxmlResource("styles.css");
            if (cssURL != null) {
                scene.getStylesheets().add(cssURL.toExternalForm());
            }
            
            // Initialiser le contrôleur
            AjoutAbonnementController controller = loader.getController();
            controller.setListeAbonnementsController(this);
            
            // Créer et afficher la fenêtre
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nouvel abonnement");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Exception détaillée lors du chargement de la vue:");
            e.printStackTrace();
            afficherErreur("Erreur", "Erreur de navigation", 
                    "Impossible d'ouvrir la page d'ajout d'abonnement: " + e.getMessage());
        }
    }
    
    @FXML
    public void ouvrirModifier() {
        Abonnement selected = abonnementsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ouvrirModifierAbonnement(selected);
        } else {
            afficherInfo("Information", "Sélection requise", "Veuillez sélectionner un abonnement à modifier.");
        }
    }
    
    private void ouvrirModifierAbonnement(Abonnement abonnement) {
        try {
            // Localiser le fichier FXML
            URL resourceURL = findFxmlResource("AjoutAbonnement.fxml");
            
            // Initialiser le chargeur FXML
            FXMLLoader loader = new FXMLLoader(resourceURL);
            Parent root = loader.load();
            
            // Configurer la scène
            Scene scene = new Scene(root);
            
            // Ajouter les styles CSS
            URL cssURL = findFxmlResource("styles.css");
            if (cssURL != null) {
                scene.getStylesheets().add(cssURL.toExternalForm());
            }
            
            // Initialiser le contrôleur
            AjoutAbonnementController controller = loader.getController();
            controller.setListeAbonnementsController(this);
            controller.setAbonnementForEdit(abonnement);
            
            // Créer et afficher la fenêtre
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier un abonnement");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Exception détaillée lors du chargement de la vue:");
            e.printStackTrace();
            afficherErreur("Erreur", "Erreur de navigation", 
                    "Impossible d'ouvrir la page de modification d'abonnement: " + e.getMessage());
        }
    }
    
    @FXML
    public void supprimerAbonnement() {
        Abonnement selected = abonnementsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            confirmerSuppression(selected);
        } else {
            afficherInfo("Information", "Sélection requise", "Veuillez sélectionner un abonnement à supprimer.");
        }
    }
    
    private void confirmerSuppression(Abonnement abonnement) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'abonnement #" + abonnement.getIdAbonnement());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet abonnement ? Cette action est irréversible.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            abonnementService.supprimerAbonnement(abonnement.getIdAbonnement());
            rafraichirListe();
            afficherInfo("Succès", "Suppression réussie", "L'abonnement a été supprimé avec succès.");
        }
    }
    
    @FXML
    public void afficherDetails() {
        Abonnement selected = abonnementsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            afficherDetailsAbonnement(selected);
        } else {
            afficherInfo("Information", "Sélection requise", "Veuillez sélectionner un abonnement pour voir les détails.");
        }
    }
    
    private void afficherDetailsAbonnement(Abonnement abonnement) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de l'abonnement");
        alert.setHeaderText("Abonnement #" + abonnement.getIdAbonnement());
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        Reservation reservation = abonnement.getReservation();
        
        // Détails de l'abonnement
        grid.add(new Label("Type:"), 0, 0);
        grid.add(new Label(abonnement.getTypeAbonnement()), 1, 0);
        
        grid.add(new Label("Date début:"), 0, 1);
        grid.add(new Label(abonnement.getDateDebut().format(DATE_FORMATTER)), 1, 1);
        
        grid.add(new Label("Date fin:"), 0, 2);
        if (abonnement.getDateFin() != null) {
            grid.add(new Label(abonnement.getDateFin().format(DATE_FORMATTER)), 1, 2);
        } else {
            grid.add(new Label("Sans limite"), 1, 2);
        }
        
        grid.add(new Label("Montant:"), 0, 3);
        grid.add(new Label(String.format("%.2f €", abonnement.getMontant())), 1, 3);
        
        grid.add(new Label("Statut:"), 0, 4);
        grid.add(new Label(abonnement.getStatut()), 1, 4);
        
        // Séparateur
        Separator separator = new Separator();
        grid.add(separator, 0, 5, 2, 1);
        
        // Détails de la réservation
        grid.add(new Label("Réservation:"), 0, 6);
        grid.add(new Label("ID #" + abonnement.getReservationId()), 1, 6);
        
        if (reservation != null) {
            grid.add(new Label("Passager:"), 0, 7);
            grid.add(new Label(reservation.getNomPassager()), 1, 7);
            
            grid.add(new Label("Trajet:"), 0, 8);
            grid.add(new Label(reservation.getPointDepart() + " → " + reservation.getPointArrivee()), 1, 8);
            
            grid.add(new Label("Date trajet:"), 0, 9);
            grid.add(new Label(reservation.getDateReservation().format(DATE_FORMATTER)), 1, 9);
        }
        
        // Remarques
        if (abonnement.getRemarques() != null && !abonnement.getRemarques().isEmpty()) {
            grid.add(new Label("Remarques:"), 0, 10);
            
            TextArea remarques = new TextArea(abonnement.getRemarques());
            remarques.setEditable(false);
            remarques.setWrapText(true);
            remarques.setPrefHeight(100);
            remarques.setPrefWidth(300);
            
            grid.add(remarques, 0, 11, 2, 1);
        }
        
        alert.getDialogPane().setContent(grid);
        alert.showAndWait();
    }
    
    /**
     * Affiche un message de succès dans la zone de notification
     */
    public void afficherSucces(String titre, String sousTitre, String message) {
        afficherNotification(titre, message, "notification-success");
    }
    
    /**
     * Affiche un message de succès dans la zone de notification avec un titre simple
     */
    public void afficherSucces(String message) {
        afficherNotification("Opération réussie", message, "notification-success");
    }
    
    /**
     * Affiche un message d'information dans la zone de notification
     */
    public void afficherInfo(String titre, String sousTitre, String message) {
        afficherNotification(titre, message, "notification-info");
    }
    
    /**
     * Affiche un message d'information dans la zone de notification avec un titre simple
     */
    public void afficherInfo(String message) {
        afficherNotification("Information", message, "notification-info");
    }
    
    /**
     * Affiche un message d'erreur dans la zone de notification
     */
    public void afficherErreur(String titre, String sousTitre, String message) {
        afficherNotification(titre, message, "notification-error");
    }
    
    /**
     * Affiche un message d'erreur dans la zone de notification avec un titre simple
     */
    public void afficherErreur(String message) {
        afficherNotification("Erreur", message, "notification-error");
    }
    
    /**
     * Affiche une notification avec le style spécifié
     */
    private void afficherNotification(String titre, String message, String styleClass) {
        if (notificationArea == null || notificationTitle == null || notificationMessage == null) {
            // Fallback si les éléments d'interface ne sont pas disponibles
            afficherInfo(titre, null, message);
            return;
        }
        
        // Arrêter le timer précédent s'il existe
        if (notificationTimeline != null) {
            notificationTimeline.stop();
        }
        
        // Configurer le style de la notification
        notificationArea.getStyleClass().removeAll("notification-success", "notification-error", "notification-info");
        notificationArea.getStyleClass().add(styleClass);
        
        // Définir le titre et le message
        notificationTitle.setText(titre);
        notificationMessage.setText(message);
        
        // Afficher la notification avec animation
        notificationArea.setOpacity(0);
        notificationArea.setVisible(true);
        notificationArea.setManaged(true);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notificationArea);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        // Configurer le timer pour faire disparaître la notification après 5 secondes
        notificationTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(notificationArea.opacityProperty(), 1)),
            new KeyFrame(Duration.seconds(5), new KeyValue(notificationArea.opacityProperty(), 1)),
            new KeyFrame(Duration.seconds(5.5), e -> {
                notificationArea.setVisible(false);
                notificationArea.setManaged(false);
            }, new KeyValue(notificationArea.opacityProperty(), 0))
        );
        notificationTimeline.play();
    }
    
    /**
     * Exporte les abonnements affichés en PDF
     */
    @FXML
    public void exporterPDF() {
        try {
            // Ouvrir un dialogue pour choisir où enregistrer le fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );
            fileChooser.setInitialFileName("Liste_Abonnements_" + 
                                          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
                                          ".pdf");
            
            File file = fileChooser.showSaveDialog(abonnementsTable.getScene().getWindow());
            if (file == null) {
                return; // L'utilisateur a annulé
            }
            
            // Créer le document PDF
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            
            // Ajouter un en-tête
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(39, 174, 96)); // Vert au lieu de Violet
            Paragraph title = new Paragraph("Liste des Abonnements", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Ajouter la date d'exportation
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY);
            Paragraph datePara = new Paragraph("Exporté le: " + 
                                              LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm:ss")), 
                                              dateFont);
            datePara.setAlignment(Element.ALIGN_RIGHT);
            datePara.setSpacingAfter(20);
            document.add(datePara);
            
            // Ajouter les statistiques
            int total = filteredAbonnements.size();
            int actifs = 0;
            int expires = 0;
            BigDecimal montantTotal = BigDecimal.ZERO;
            
            for (Abonnement abo : filteredAbonnements) {
                if ("Actif".equals(abo.getStatut())) {
                    actifs++;
                } else if ("Expiré".equals(abo.getStatut())) {
                    expires++;
                }
                
                if (abo.getMontant() != null) {
                    montantTotal = montantTotal.add(abo.getMontant());
                }
            }
            
            Paragraph statsPara = new Paragraph();
            statsPara.add(new Chunk("Total: " + total + " abonnement" + (total > 1 ? "s" : "") + " | "));
            
            Font confirmedFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new BaseColor(46, 204, 113)); // Vert
            statsPara.add(new Chunk("Actifs: " + actifs + " | ", confirmedFont));
            
            Font canceledFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new BaseColor(231, 76, 60)); // Rouge
            statsPara.add(new Chunk("Expirés: " + expires + " | ", canceledFont));
            
            Font montantFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new BaseColor(39, 174, 96)); // Vert au lieu de Violet
            statsPara.add(new Chunk("Montant total: " + String.format("%.2f €", montantTotal), montantFont));
            
            statsPara.setSpacingAfter(20);
            document.add(statsPara);
            
            // Créer le tableau
            PdfPTable table = new PdfPTable(8); // 8 colonnes (sans la colonne des actions)
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            
            // Définir la largeur des colonnes
            float[] columnWidths = {0.5f, 2.0f, 1.2f, 1.2f, 1.2f, 2.5f, 1.2f, 1.2f};
            table.setWidths(columnWidths);
            
            // Style des en-têtes
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
            BaseColor headerBackground = new BaseColor(39, 174, 96); // Vert au lieu de Violet
            
            // Ajouter les en-têtes
            addPdfHeaderCell(table, "ID", headerFont, headerBackground);
            addPdfHeaderCell(table, "Type", headerFont, headerBackground);
            addPdfHeaderCell(table, "Date début", headerFont, headerBackground);
            addPdfHeaderCell(table, "Date fin", headerFont, headerBackground);
            addPdfHeaderCell(table, "Montant", headerFont, headerBackground);
            addPdfHeaderCell(table, "Réservation", headerFont, headerBackground);
            addPdfHeaderCell(table, "Statut", headerFont, headerBackground);
            addPdfHeaderCell(table, "Remarques", headerFont, headerBackground);
            
            // Style des cellules
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
            Font cellFontAlt = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);
            
            BaseColor altRowColor = new BaseColor(245, 246, 250);
            
            // Ajouter les données
            int rowNum = 0;
            for (Abonnement abo : filteredAbonnements) {
                boolean isAltRow = rowNum % 2 != 0;
                Font currentFont = isAltRow ? cellFontAlt : cellFont;
                BaseColor rowColor = isAltRow ? altRowColor : BaseColor.WHITE;
                
                addPdfCell(table, String.valueOf(abo.getIdAbonnement()), currentFont, rowColor);
                addPdfCell(table, abo.getTypeAbonnement(), currentFont, rowColor);
                addPdfCell(table, abo.getDateDebut().format(DATE_FORMATTER), currentFont, rowColor);
                
                String dateFin = abo.getDateFin() != null ? abo.getDateFin().format(DATE_FORMATTER) : "Sans limite";
                addPdfCell(table, dateFin, currentFont, rowColor);
                
                String montant = abo.getMontant() != null ? String.format("%.2f €", abo.getMontant()) : "0.00 €";
                addPdfCell(table, montant, currentFont, rowColor);
                
                // Réservation 
                Reservation reservation = abo.getReservation();
                String resInfo = reservation != null 
                    ? String.format("%s (%s → %s)", reservation.getNomPassager(), reservation.getPointDepart(), reservation.getPointArrivee())
                    : "ID: " + abo.getReservationId();
                addPdfCell(table, resInfo, currentFont, rowColor);
                
                // Cellule de statut avec couleur
                BaseColor statusColor;
                switch (abo.getStatut()) {
                    case "Actif":
                        statusColor = new BaseColor(46, 204, 113); // Vert
                        break;
                    case "En attente":
                        statusColor = new BaseColor(243, 156, 18); // Orange
                        break;
                    case "Expiré":
                        statusColor = new BaseColor(231, 76, 60); // Rouge
                        break;
                    default:
                        statusColor = BaseColor.GRAY;
                }
                
                Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, statusColor);
                addPdfCell(table, abo.getStatut(), statusFont, rowColor);
                
                // Remarques
                String remarques = abo.getRemarques() != null ? abo.getRemarques() : "";
                addPdfCell(table, remarques, currentFont, rowColor);
                
                rowNum++;
            }
            
            document.add(table);
            
            // Ajouter un pied de page
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8);
            Paragraph footer = new Paragraph("Système de Gestion des Abonnements - © " + LocalDate.now().getYear(), footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
            
            document.close();
            
            // Afficher un message de succès
            afficherSucces("Liste des abonnements exportée avec succès vers: " + file.getName());
            
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur d'exportation", "Une erreur est survenue", e.getMessage());
        }
    }
    
    /**
     * Ajoute une cellule d'en-tête au tableau PDF
     */
    private void addPdfHeaderCell(PdfPTable table, String text, Font font, BaseColor background) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(background);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);
    }
    
    /**
     * Ajoute une cellule au tableau PDF
     */
    private void addPdfCell(PdfPTable table, String text, Font font, BaseColor background) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBackgroundColor(background);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);
    }
    
    /**
     * Méthode utilitaire pour localiser une ressource FXML de manière fiable 
     * en essayant plusieurs stratégies
     */
    private URL findFxmlResource(String fxmlFileName) throws IOException {
        System.out.println("Recherche de la ressource: " + fxmlFileName);
        
        URL resourceURL = null;
        
        // Stratégies de recherche
        String[] paths = {
            "/" + fxmlFileName,             // Avec slash
            fxmlFileName,                   // Sans slash
            "/gui/" + fxmlFileName,         // Dans le package gui
            "gui/" + fxmlFileName           // Dans le package gui sans slash
        };
        
        // Essayer avec getClass().getResource()
        for (String path : paths) {
            resourceURL = getClass().getResource(path);
            if (resourceURL != null) {
                System.out.println("Trouvé avec getClass().getResource(\"" + path + "\"): " + resourceURL);
                return resourceURL;
            }
        }
        
        // Essayer avec ClassLoader
        for (String path : paths) {
            // Retirer le slash initial pour le ClassLoader
            String cleanPath = path.startsWith("/") ? path.substring(1) : path;
            resourceURL = getClass().getClassLoader().getResource(cleanPath);
            if (resourceURL != null) {
                System.out.println("Trouvé avec ClassLoader.getResource(\"" + cleanPath + "\"): " + resourceURL);
                return resourceURL;
            }
        }
        
        // Tentative de recherche manuelle dans les répertoires standard
        String[] folders = {
            "target/classes/",
            "src/main/resources/",
            "build/resources/main/"
        };
        
        for (String folder : folders) {
            java.io.File file = new java.io.File(folder + fxmlFileName);
            if (file.exists()) {
                resourceURL = file.toURI().toURL();
                System.out.println("Trouvé sur le disque à: " + resourceURL);
                return resourceURL;
            }
        }
        
        throw new IOException("Impossible de trouver la ressource: " + fxmlFileName);
    }

    @FXML
    public void ouvrirReservations() {
        try {
            // Localiser le fichier FXML
            URL resourceURL = findFxmlResource("ListeReservations.fxml");
            
            // Initialiser le chargeur FXML
            FXMLLoader loader = new FXMLLoader(resourceURL);
            Parent root = loader.load();
            
            // Configurer la scène
            Scene scene = new Scene(root);
            
            // Ajouter les styles CSS
            URL cssURL = findFxmlResource("styles.css");
            if (cssURL != null) {
                scene.getStylesheets().add(cssURL.toExternalForm());
            }
            
            // Remplacer la scène dans la fenêtre existante
            Stage stage = (Stage) btnRafraichir.getScene().getWindow();
            stage.setTitle("Gestion des Réservations");
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Exception détaillée lors du chargement de la vue:");
            e.printStackTrace();
            afficherErreur("Erreur", "Erreur de navigation", 
                    "Impossible d'ouvrir la page des réservations: " + e.getMessage());
        }
    }

    @FXML
    private void afficherStatistiques() {
        if (statsBox != null) {
            statsBox.setVisible(true);
            statsBox.setManaged(true);
            statsBox.requestFocus();
            FadeTransition ft = new FadeTransition(Duration.millis(400), statsBox);
            ft.setFromValue(0.5);
            ft.setToValue(1.0);
            ft.play();
        }
        if (reservationPieChart != null) {
            reservationPieChart.setScaleX(1.08);
            reservationPieChart.setScaleY(1.08);
            FadeTransition ft = new FadeTransition(Duration.millis(400), reservationPieChart);
            ft.setFromValue(0.5);
            ft.setToValue(1.0);
            ft.play();
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(400), new KeyValue(reservationPieChart.scaleXProperty(), 1)),
                new KeyFrame(Duration.millis(400), new KeyValue(reservationPieChart.scaleYProperty(), 1))
            );
            timeline.setDelay(Duration.millis(400));
            timeline.play();
        }
    }
} 