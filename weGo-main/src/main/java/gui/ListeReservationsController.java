package gui;

import Entites.Reservation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import client.ReservationAutomatiqueClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

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

public class ListeReservationsController {

    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, Circle> statusCol;
    @FXML private TableColumn<Reservation, Integer> idCol;
    @FXML private TableColumn<Reservation, String> nameCol;
    @FXML private TableColumn<Reservation, LocalDate> dateCol;
    @FXML private TableColumn<Reservation, String> routeCol;
    @FXML private TableColumn<Reservation, String> statusTextCol;
    @FXML private TableColumn<Reservation, String> typeCol;
    @FXML private TableColumn<Reservation, Reservation> actionsCol;
    
    @FXML private TextField filterField;
    @FXML private Label totalReservationsLabel;
    @FXML private Label confirmeeLabel;
    @FXML private Label enAttenteLabel;
    @FXML private Label annuleeLabel;
    @FXML private Label appTitleLabel;
    @FXML private HBox statsContainer;
    
    @FXML private VBox notificationArea;
    @FXML private Label notificationTitle;
    @FXML private Label notificationMessage;

    @FXML
    private Button btnRafraichir;
    
    @FXML
    private Button btnAbonnements;

    @FXML
    private Button btnReservationAuto;
    
    @FXML
    private Button btnExportPDF;

    private ObservableList<Reservation> reservations = FXCollections.observableArrayList();
    private FilteredList<Reservation> filteredReservations;
    private AtomicInteger idGenerator = new AtomicInteger(1); // Commence à 1
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    // Timer pour faire disparaitre les notifications
    private Timeline notificationTimeline;

    @FXML
    public void initialize() {
        // Configurer le titre de l'application
        if (appTitleLabel != null) {
            appTitleLabel.setText("Gestion des réservations");
        }
        
        // Initialiser la liste avec des données d'exemple
        chargerDonneesExemple();
        
        // Configurer le filtrage
        filteredReservations = new FilteredList<>(reservations, p -> true);
        
        // Configurer les colonnes de la TableView
        configureTableColumns();
        
        // Configurer le champ de recherche
        configureSearchField();
        
        // Actualiser l'affichage initial
        reservationsTable.setItems(filteredReservations);
        mettreAJourStatistiques();
        
        // Animation d'entrée pour les statistiques
        if (statsContainer != null) {
            statsContainer.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), statsContainer);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(300));
            fadeIn.play();
        }

        // Ajouter l'action pour le bouton de réservation automatique
        if (btnReservationAuto != null) {
            btnReservationAuto.setOnAction(event -> ouvrirReservationAutomatique());
        }
        
        // Initialiser la zone de notification
        if (notificationArea != null) {
            notificationArea.setVisible(false);
            notificationArea.setManaged(false);
        }
    }

    private void configureTableColumns() {
        // Colonne de statut (avec cercle de couleur)
        statusCol.setCellValueFactory(cellData -> {
            Circle circle = new Circle(8);
            String status = cellData.getValue().getStatut();
            switch (status) {
                case "Confirmée":
                    circle.setFill(Color.valueOf("#2ecc71"));
                    circle.getStyleClass().add("status-confirmed");
                    break;
                case "En attente":
                    circle.setFill(Color.valueOf("#f39c12"));
                    circle.getStyleClass().add("status-pending");
                    break;
                case "Annulée":
                    circle.setFill(Color.valueOf("#e74c3c"));
                    circle.getStyleClass().add("status-canceled");
                    break;
                default:
                    circle.setFill(Color.GRAY);
            }
            return new SimpleObjectProperty<>(circle);
        });
        
        // Colonne ID
        idCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        idCol.setCellFactory(column -> new TableCell<Reservation, Integer>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                } else {
                    setText("#" + id);
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });
        
        // Colonne Nom du passager
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNomPassager()));
        
        // Colonne Date
        dateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateReservation()));
        dateCol.setCellFactory(column -> new TableCell<Reservation, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(date));
                    // Surligner la date d'aujourd'hui
                    if (date.equals(LocalDate.now())) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        // Colonne Trajet (Départ → Arrivée)
        routeCol.setCellValueFactory(cellData -> {
            String route = cellData.getValue().getPointDepart() + " → " + cellData.getValue().getPointArrivee();
            return new SimpleStringProperty(route);
        });
        
        // Colonne Statut (texte)
        statusTextCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatut()));
        statusTextCol.setCellFactory(column -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(status);
                    statusLabel.getStyleClass().add("status-badge");
                    
                    switch (status) {
                        case "Confirmée":
                            statusLabel.getStyleClass().add("status-badge-confirmed");
                            break;
                        case "En attente":
                            statusLabel.getStyleClass().add("status-badge-pending");
                            break;
                        case "Annulée":
                            statusLabel.getStyleClass().add("status-badge-canceled");
                            break;
                    }
                    
                    setGraphic(statusLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        
        // Colonne Type
        typeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTypeTrajet()));
        
        // Colonne Actions (boutons)
        actionsCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        actionsCol.setCellFactory(param -> new TableCell<Reservation, Reservation>() {
            private final Button viewBtn = new Button("🔍");
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            
            {
                viewBtn.getStyleClass().add("action-icon");
                viewBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 14px;");
                viewBtn.setTooltip(new Tooltip("Voir les détails"));
                
                editBtn.getStyleClass().add("action-icon");
                editBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 14px;");
                editBtn.setTooltip(new Tooltip("Modifier"));
                
                deleteBtn.getStyleClass().add("action-icon");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 14px;");
                deleteBtn.setTooltip(new Tooltip("Supprimer"));
                
                viewBtn.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    afficherDetailsReservation(reservation);
                });
                
                editBtn.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    ouvrirModifierReservation(reservation);
                });
                
                deleteBtn.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    confirmerSuppression(reservation);
                });
            }
            
            @Override
            protected void updateItem(Reservation reservation, boolean empty) {
                super.updateItem(reservation, empty);
                
                if (empty || reservation == null) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(8, viewBtn, editBtn, deleteBtn);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });
    }
    
    private void configureSearchField() {
        // Ajouter un placeholder et une icône pour le champ de recherche
        filterField.setPromptText("Rechercher par nom, lieu, statut...");
        
        // Configurer le filtrage
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredReservations.setPredicate(reservation -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true; // Afficher toutes les réservations si la recherche est vide
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                // Recherche dans différents champs
                if (reservation.getNomPassager().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Correspond au nom
                } else if (reservation.getPointDepart().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Correspond au point de départ
                } else if (reservation.getPointArrivee().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Correspond au point d'arrivée
                } else if (reservation.getStatut().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Correspond au statut
                } else if (reservation.getTypeTrajet().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Correspond au type de trajet
                } else if (String.valueOf(reservation.getId()).contains(lowerCaseFilter)) {
                    return true; // Correspond à l'ID
                }
                return false; // Ne correspond à aucun critère
            });
            
            mettreAJourStatistiques();
        });
    }

    private void chargerDonneesExemple() {
        // Utiliser le service pour récupérer les données de la base de données
        try {
            // Créer l'instance du service
            services.ReserService reservationService = new services.ReserService();
            
            // Récupérer toutes les réservations depuis la BDD
            List<Reservation> reservationsFromDB = reservationService.recupererToutesReservations();
            
            // Debug
            System.out.println("Nombre de réservations chargées depuis la base de données: " + reservationsFromDB.size());
            for (Reservation res : reservationsFromDB) {
                System.out.println("Reservation #" + res.getId() + ": " + res.getNomPassager() + 
                                   " (" + res.getPointDepart() + " → " + res.getPointArrivee() + 
                                   "), Statut: " + res.getStatut());
            }
            
            // Vider la liste actuelle et ajouter les réservations de la BDD
            reservations.clear();
            reservations.addAll(reservationsFromDB);
            
            // Trouver l'ID maximum pour initialiser correctement l'idGenerator
            int maxId = 0;
            for (Reservation res : reservations) {
                maxId = Math.max(maxId, res.getId());
            }
            
            // Initialiser l'idGenerator avec le prochain ID disponible
            idGenerator.set(maxId + 1);
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des réservations depuis la base de données: " + e.getMessage());
            e.printStackTrace();
            
            // En cas d'erreur, charger des données d'exemple par défaut
            reservations.clear();
            reservations.addAll(
                new Reservation(1, "Ali Ben Mohamed", LocalDate.now(), "Tunis", "Sfax", "Confirmée", "Aller simple", "Départ à 7h00"),
                new Reservation(2, "Sara Trabelsi", LocalDate.now().plusDays(1), "Sousse", "Monastir", "En attente", "Aller-retour", "Préfère voiture climatisée"),
                new Reservation(3, "Mohamed Karim", LocalDate.now().plusDays(2), "Sfax", "Tunis", "Confirmée", "Aller simple", "Max 2 valises"),
                new Reservation(4, "Fatma Salah", LocalDate.now().plusDays(3), "Bizerte", "Nabeul", "En attente", "Aller-retour", "Avec un enfant"),
                new Reservation(5, "Ahmed Dridi", LocalDate.now().minusDays(1), "Kairouan", "Gabès", "Annulée", "Aller simple", "Annulation conducteur"),
                new Reservation(6, "Leila Mansour", LocalDate.now().minusDays(2), "Tabarka", "Hammamet", "Confirmée", "Aller-retour", "Non-fumeur")
            );
            
            // Initialiser l'idGenerator avec la valeur par défaut
            idGenerator.set(7);
        }
    }
    
    private void mettreAJourStatistiques() {
        int total = filteredReservations.size();
        int confirmees = 0;
        int enAttente = 0;
        int annulees = 0;
        
        for (Reservation res : filteredReservations) {
            switch (res.getStatut()) {
                case "Confirmée": confirmees++; break;
                case "En attente": enAttente++; break;
                case "Annulée": annulees++; break;
            }
        }
        
        // Mettre à jour les labels avec des icônes
        totalReservationsLabel.setText("Total: " + total + " trajet" + (total > 1 ? "s" : ""));
        confirmeeLabel.setText("✅ Confirmés: " + confirmees);
        enAttenteLabel.setText("⏳ En attente: " + enAttente);
        annuleeLabel.setText("❌ Annulés: " + annulees);
        
        // Appliquer des styles aux statistiques
        confirmeeLabel.getStyleClass().add("stats-label-confirmed");
        enAttenteLabel.getStyleClass().add("stats-label-pending");
        annuleeLabel.getStyleClass().add("stats-label-canceled");
    }

    @FXML
    public void ouvrirAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Ajout.fxml"));
            Parent root = loader.load();
            
            // Ajouter la feuille de style
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/fxml/styles.css").toExternalForm());
            
            // Passer la référence de la liste des réservations au contrôleur d'ajout
            AjoutController controller = loader.getController();
            controller.setListeController(this);
            controller.setIdGenerator(idGenerator);
            
            // Créer et afficher la fenêtre
            Stage stage = new Stage();
            stage.setTitle("Ajouter un trajet");
            stage.setScene(scene);
            stage.show();
            
            // Ajouter une animation d'ouverture
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
            
        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur lors de l'ouverture de la fenêtre d'ajout: " + e.getMessage());
        }
    }

    @FXML
    public void ouvrirModifier() {
        Reservation selectedReservation = reservationsTable.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            afficherErreur("Veuillez sélectionner un trajet à modifier");
            return;
        }
        
        ouvrirModifierReservation(selectedReservation);
    }
    
    private void ouvrirModifierReservation(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modif.fxml"));
            Parent root = loader.load();
            
            // Ajouter la feuille de style
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/fxml/styles.css").toExternalForm());
            
            // Passer la réservation sélectionnée au contrôleur de modification
            ModifierController controller = loader.getController();
            controller.setReservation(reservation);
            controller.setListeController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier un trajet");
            stage.setScene(scene);
            stage.show();
            
            // Ajouter une animation d'ouverture
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
            
        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur lors de l'ouverture de la fenêtre de modification: " + e.getMessage());
        }
    }

    @FXML
    public void supprimerReservation() {
        Reservation selectedReservation = reservationsTable.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            afficherErreur("Veuillez sélectionner un trajet à supprimer");
            return;
        }
        
        confirmerSuppression(selectedReservation);
    }
    
    private void confirmerSuppression(Reservation reservation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le trajet #" + reservation.getId());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le trajet de " + 
                reservation.getNomPassager() + " de " + reservation.getPointDepart() + 
                " à " + reservation.getPointArrivee() + " ?");
        
        // Ajouter les styles CSS à la boîte de dialogue
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/fxml/styles.css").toExternalForm());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Animation de suppression
            TableRow<Reservation> row = getRowByReservation(reservation);
            if (row != null) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), row);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    reservations.remove(reservation);
                    mettreAJourStatistiques();
                });
                fadeOut.play();
            } else {
                reservations.remove(reservation);
                mettreAJourStatistiques();
            }
        }
    }
    
    private TableRow<Reservation> getRowByReservation(Reservation reservation) {
        for (TableRow<Reservation> row : reservationsTable.lookupAll(".table-row-cell").stream()
                .filter(node -> node instanceof TableRow)
                .map(node -> (TableRow<Reservation>) node)
                .toList()) {
            if (row.getItem() == reservation) {
                return row;
            }
        }
        return null;
    }

    @FXML
    public void afficherDetails() {
        Reservation selectedReservation = reservationsTable.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            afficherErreur("Veuillez sélectionner un trajet pour voir les détails");
            return;
        }
        
        afficherDetailsReservation(selectedReservation);
    }
    
    private void afficherDetailsReservation(Reservation reservation) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du trajet");
        alert.setHeaderText("Trajet #" + reservation.getId());
        
        String statusEmoji = getStatusEmoji(reservation.getStatut());
        String details = String.format(
                "📋 Informations du passager\n" +
                "-----------------------------------\n" +
                "🧑 Nom: %s\n" +
                "📅 Date du trajet: %s\n" +
                "🚩 Statut: %s %s\n\n" +
                "🚗 Informations du trajet\n" +
                "-----------------------------------\n" +
                "🏁 Départ: %s\n" +
                "🏁 Arrivée: %s\n" +
                "🔄 Type: %s\n\n" +
                "💬 Notes supplémentaires\n" +
                "-----------------------------------\n" +
                "%s",
                reservation.getNomPassager(),
                reservation.getDateReservation().format(dateFormatter),
                reservation.getStatut(), statusEmoji,
                reservation.getPointDepart(),
                reservation.getPointArrivee(),
                reservation.getTypeTrajet(),
                reservation.getCommentaire().isEmpty() ? "Aucune note" : reservation.getCommentaire()
        );
        
        alert.setContentText(details);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/fxml/styles.css").toExternalForm());
        alert.showAndWait();
    }
    
    private String getStatusEmoji(String statut) {
        switch (statut) {
            case "Confirmée": return "✅";
            case "En attente": return "⏳";
            case "Annulée": return "❌";
            default: return "❓";
        }
    }

    @FXML
    public void rafraichirListe() {
        chargerDonneesExemple();
        mettreAJourStatistiques();
    }
    
    /**
     * Affiche un message de succès dans la zone de notification
     */
    public void afficherSucces(String message) {
        afficherNotification("Opération réussie", message, "notification-success");
    }
    
    /**
     * Affiche un message d'information dans la zone de notification
     */
    public void afficherInfo(String message) {
        afficherNotification("Information", message, "notification-info");
    }
    
    /**
     * Surcharge de la méthode pour accepter un seul paramètre
     */
    public void afficherErreur(String message) {
        afficherNotification("Erreur", message, "notification-error");
    }
    
    /**
     * Affiche un message d'erreur dans la zone de notification ou une alerte
     */
    public void afficherErreur(String titre, String message) {
        if (notificationArea != null && notificationTitle != null && notificationMessage != null) {
            afficherNotification(titre, message, "notification-error");
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(titre);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
    
    /**
     * Affiche une notification avec le style spécifié
     */
    private void afficherNotification(String titre, String message, String styleClass) {
        if (notificationArea == null || notificationTitle == null || notificationMessage == null) {
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
     * Exporte les réservations affichées en PDF
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
            fileChooser.setInitialFileName("Liste_Reservations_" + 
                                          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
                                          ".pdf");
            
            File file = fileChooser.showSaveDialog(reservationsTable.getScene().getWindow());
            if (file == null) {
                return; // L'utilisateur a annulé
            }
            
            // Créer le document PDF
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            
            // Ajouter un en-tête
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("Liste des Réservations", titleFont);
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
            Font statsFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.DARK_GRAY);
            int total = filteredReservations.size();
            int confirmees = 0;
            int enAttente = 0;
            int annulees = 0;
            
            for (Reservation res : filteredReservations) {
                switch (res.getStatut()) {
                    case "Confirmée": confirmees++; break;
                    case "En attente": enAttente++; break;
                    case "Annulée": annulees++; break;
                }
            }
            
            Paragraph statsPara = new Paragraph();
            statsPara.add(new Chunk("Total: " + total + " trajet" + (total > 1 ? "s" : "") + " | "));
            
            Font confirmedFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new BaseColor(46, 204, 113));
            statsPara.add(new Chunk("Confirmés: " + confirmees + " | ", confirmedFont));
            
            Font pendingFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new BaseColor(243, 156, 18));
            statsPara.add(new Chunk("En attente: " + enAttente + " | ", pendingFont));
            
            Font canceledFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new BaseColor(231, 76, 60));
            statsPara.add(new Chunk("Annulés: " + annulees, canceledFont));
            
            statsPara.setSpacingAfter(20);
            document.add(statsPara);
            
            // Créer le tableau
            PdfPTable table = new PdfPTable(7); // 7 colonnes (sans la colonne des actions)
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            
            // Définir la largeur des colonnes
            float[] columnWidths = {0.5f, 2.5f, 1.5f, 3.0f, 1.5f, 1.5f, 1.5f};
            table.setWidths(columnWidths);
            
            // Style des en-têtes
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
            BaseColor headerBackground = new BaseColor(52, 152, 219); // Bleu
            
            // Ajouter les en-têtes
            addPdfHeaderCell(table, "ID", headerFont, headerBackground);
            addPdfHeaderCell(table, "Passager", headerFont, headerBackground);
            addPdfHeaderCell(table, "Date", headerFont, headerBackground);
            addPdfHeaderCell(table, "Trajet", headerFont, headerBackground);
            addPdfHeaderCell(table, "Statut", headerFont, headerBackground);
            addPdfHeaderCell(table, "Type", headerFont, headerBackground);
            addPdfHeaderCell(table, "Commentaire", headerFont, headerBackground);
            
            // Style des cellules
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
            Font cellFontAlt = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);
            
            BaseColor altRowColor = new BaseColor(245, 246, 250);
            
            // Ajouter les données
            int rowNum = 0;
            for (Reservation res : filteredReservations) {
                boolean isAltRow = rowNum % 2 != 0;
                Font currentFont = isAltRow ? cellFontAlt : cellFont;
                BaseColor rowColor = isAltRow ? altRowColor : BaseColor.WHITE;
                
                addPdfCell(table, "#" + res.getId(), currentFont, rowColor);
                addPdfCell(table, res.getNomPassager(), currentFont, rowColor);
                addPdfCell(table, res.getDateReservation().format(dateFormatter), currentFont, rowColor);
                addPdfCell(table, res.getPointDepart() + " → " + res.getPointArrivee(), currentFont, rowColor);
                
                // Cellule de statut avec couleur
                BaseColor statusColor;
                switch (res.getStatut()) {
                    case "Confirmée":
                        statusColor = new BaseColor(46, 204, 113); // Vert
                        break;
                    case "En attente":
                        statusColor = new BaseColor(243, 156, 18); // Orange
                        break;
                    case "Annulée":
                        statusColor = new BaseColor(231, 76, 60); // Rouge
                        break;
                    default:
                        statusColor = BaseColor.GRAY;
                }
                
                Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, statusColor);
                addPdfCell(table, res.getStatut(), statusFont, rowColor);
                
                addPdfCell(table, res.getTypeTrajet(), currentFont, rowColor);
                addPdfCell(table, res.getCommentaire(), currentFont, rowColor);
                
                rowNum++;
            }
            
            document.add(table);
            
            // Ajouter un pied de page
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8);
            Paragraph footer = new Paragraph("Système de Réservation de Trajets - © " + LocalDate.now().getYear(), footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
            
            document.close();
            
            // Afficher un message de succès
            afficherSucces("Liste des réservations exportée avec succès vers: " + file.getName());
            
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur d'exportation", "Impossible d'exporter les réservations: " + e.getMessage());
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
    
    // Méthodes pour interagir avec d'autres contrôleurs
    
    public void ajouterReservation(Reservation reservation) {
        // Animation d'ajout
        reservations.add(reservation);
        mettreAJourStatistiques();
        
        // Sélectionner et mettre en évidence la nouvelle réservation
        int index = reservations.indexOf(reservation);
        if (index >= 0) {
            reservationsTable.getSelectionModel().select(index);
            reservationsTable.scrollTo(index);
            
            // Animation de mise en évidence
            TableRow<Reservation> row = getRowByReservation(reservation);
            if (row != null) {
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), row);
                scale.setFromX(1.0);
                scale.setFromY(1.0);
                scale.setToX(1.05);
                scale.setToY(1.05);
                scale.setCycleCount(2);
                scale.setAutoReverse(true);
                scale.play();
            }
        }
    }
    
    public void mettreAJourReservation(Reservation reservation) {
        // Trouver et mettre à jour la réservation existante
        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).getId() == reservation.getId()) {
                reservations.set(i, reservation);
                
                // Animation de mise à jour
                TableRow<Reservation> row = getRowByReservation(reservation);
                if (row != null) {
                    FadeTransition fade = new FadeTransition(Duration.millis(300), row);
                    fade.setFromValue(0.5);
                    fade.setToValue(1.0);
                    fade.play();
                }
                
                break;
            }
        }
        mettreAJourStatistiques();
    }
    
    /**
     * Récupérer le générateur d'ID actuel
     */
    public AtomicInteger getIdGenerator() {
        return idGenerator;
    }

    @FXML
    public void ouvrirAbonnements(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListeAbonnements.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Gestion des Abonnements");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible d'ouvrir la fenêtre des abonnements : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirReservationAutomatique() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReservationAutomatique.fxml"));
            Parent root = loader.load();
            
            ReservationAutomatiqueController controller = loader.getController();
            controller.setListeController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Réservation Automatique");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherErreur("Erreur d'ouverture", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirMessagerie(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Messagerie.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Messagerie");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherErreur("Erreur lors de l'ouverture de la messagerie : " + e.getMessage());
        }
    }
}