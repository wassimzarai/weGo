package gui;

import Entites.Abonnement;
import Entites.Reservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.AbonnementService;
import Entites.Abonnement;
import services.ReserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import Entites.Reservation;

public class AjoutAbonnementController {

    @FXML private Label idLabel;
    @FXML private ComboBox<Reservation> reservationComboBox;
    @FXML private Button btnRefreshReservations;
    @FXML private Label passagerLabel;
    @FXML private Label dateLabel;
    @FXML private Label trajetLabel;
    @FXML private Label statutReservationLabel;
    @FXML private ComboBox<String> typeAbonnementComboBox;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField montantField;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private TextArea remarquesField;
    @FXML private Button btnAjouter;
    @FXML private Button btnAnnuler;

    private AbonnementService abonnementService;
    private ReserService reservationService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        abonnementService = new AbonnementService();
        reservationService = new ReserService();

        initComboBoxes();

        dateDebutPicker.setValue(LocalDate.now());

        reservationComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) updateReservationDetails(newVal);
            else clearReservationDetails();
        });

        reservationComboBox.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obs2, oldWin, newWin) -> {
                    if (newWin != null) loadReservations();
                });
            }
        });
    }

    private void initComboBoxes() {
        typeAbonnementComboBox.setItems(FXCollections.observableArrayList(
                "Mensuel", "Trimestriel", "Semestriel", "Annuel", "Sur mesure"
        ));
        statutComboBox.setItems(FXCollections.observableArrayList(
                "Actif", "En attente", "Expiré", "Suspendu"
        ));
        statutComboBox.setValue("Actif");
        loadReservations();
    }

    @FXML
    private void refreshReservations() {
        loadReservations();
    }

    private void loadReservations() {
        try {
            List<Entites.Reservation> reservations = reservationService.recupererToutesReservations();
            if (reservations.isEmpty()) {
                afficherErreur("Aucune réservation", "Pas de réservations disponibles",
                        "Veuillez créer des réservations avant d’ajouter un abonnement.");
            }
            ObservableList<Reservation> reservationsList = FXCollections.observableArrayList(reservations);
            reservationComboBox.setItems(reservationsList);

            reservationComboBox.setConverter(new javafx.util.StringConverter<>() {
                @Override
                public String toString(Reservation r) {
                    if (r == null) return null;
                    return String.format("#%d - %s (%s → %s)",
                            r.getId(), r.getNomPassager(), r.getPointDepart(), r.getPointArrivee());
                }
                @Override
                public Reservation fromString(String string) { return null; }
            });

            reservationComboBox.setVisibleRowCount(10);
            reservationComboBox.setEditable(true);

        } catch (Exception e) {
            afficherErreur("Erreur de chargement", "Impossible de charger les réservations", e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateReservationDetails(Reservation r) {
        passagerLabel.setText(r.getNomPassager());
        dateLabel.setText(r.getDateReservation().format(DATE_FORMATTER));
        trajetLabel.setText(r.getPointDepart() + " → " + r.getPointArrivee());
        statutReservationLabel.setText(r.getStatut());
    }

    private void clearReservationDetails() {
        passagerLabel.setText("--");
        dateLabel.setText("--");
        trajetLabel.setText("--");
        statutReservationLabel.setText("--");
    }

    @FXML
    private void ajouterAbonnement() {
        Reservation selectedReservation = reservationComboBox.getValue();
        String type = typeAbonnementComboBox.getValue();
        LocalDate debut = dateDebutPicker.getValue();
        LocalDate fin = dateFinPicker.getValue();
        String montantStr = montantField.getText();
        String statut = statutComboBox.getValue();
        String remarques = remarquesField.getText();

        if (selectedReservation == null || type == null || debut == null || montantStr.isEmpty() || statut == null) {
            afficherErreur("Champs manquants", "Veuillez remplir tous les champs obligatoires", "");
            return;
        }

        BigDecimal montant;
        try {
            montant = new BigDecimal(montantStr);
        } catch (NumberFormatException e) {
            afficherErreur("Format invalide", "Le montant doit être un nombre valide", "");
            return;
        }

        Abonnement abonnement = new Abonnement();
        abonnement.setReservation(selectedReservation);
        abonnement.setType(type);
        abonnement.setDateDebut(debut);
        abonnement.setDateFin(fin);
        abonnement.setMontant(montant);
        abonnement.setStatut(statut);
        abonnement.setRemarques(remarques);

        try {
            abonnementService.ajouter((Entites.Abonnement) abonnement);
            afficherInformation("Succès", "L'abonnement a été créé avec succès !");
            fermerFenetre();
        } catch (Exception e) {
            afficherErreur("Erreur d'ajout", "Impossible d'ajouter l'abonnement", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void annulerAjout() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private void afficherErreur(String titre, String header, String contenu) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(header);
        alert.setContentText(contenu);
        alert.showAndWait();
    }

    private void afficherInformation(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setListeAbonnementsController(ListeAbonnementsController listeAbonnementsController) {
    }

    public void setAbonnementForEdit(Abonnement abonnement) {
    }
}
