package gui;

import Entites.Reservation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.CalendarView;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

public class AjoutController {

    @FXML private TextField arriveeField;
    @FXML private TextArea commentaireField;
    @FXML private DatePicker datePicker;
    @FXML private TextField departField;
    @FXML private TextField nomField;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private VBox datePickerContainer;
    @FXML private Label idLabel;

    private ListeReservationsController listeController;
    private AtomicInteger idGenerator;
    private int newId;
    private CalendarView calendarView;

    @FXML
    public void initialize() {
        // Initialiser les ComboBox avec les options
        statutComboBox.getItems().addAll("Confirmée", "En attente", "Annulée");
        typeComboBox.getItems().addAll("Aller simple", "Aller-retour", "Trajet régulier");

        // Configurer le calendrier personnalisé
        setupCalendarView();
        
        // Configurer les champs avec des valeurs par défaut
        datePicker.setValue(LocalDate.now());
        
        // Ajouter des tooltips pour améliorer l'UX
        nomField.setTooltip(new Tooltip("Nom complet du passager"));
        departField.setTooltip(new Tooltip("Ville ou lieu de départ"));
        arriveeField.setTooltip(new Tooltip("Ville ou lieu d'arrivée"));
        statutComboBox.setTooltip(new Tooltip("Statut actuel de la réservation"));
        typeComboBox.setTooltip(new Tooltip("Type de trajet"));
        
        // Ajouter des écouteurs pour valider dynamiquement
        nomField.textProperty().addListener((obs, oldVal, newVal) -> checkFieldsValidity());
        departField.textProperty().addListener((obs, oldVal, newVal) -> checkFieldsValidity());
        arriveeField.textProperty().addListener((obs, oldVal, newVal) -> checkFieldsValidity());
        statutComboBox.valueProperty().addListener((obs, oldVal, newVal) -> checkFieldsValidity());
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> checkFieldsValidity());
    }

    /**
     * Configure le calendrier personnalisé
     */
    private void setupCalendarView() {
        // Créer le composant calendrier ultra-compact
        calendarView = new CalendarView();
        calendarView.setSelectedDate(LocalDate.now());
        
        // Style supplémentaire pour mieux intégrer le calendrier
        calendarView.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 6;");
        
        // Gérer la sélection de date avec animation subtile
        calendarView.setOnDateSelected(event -> {
            LocalDate selectedDate = calendarView.getSelectedDate();
            datePicker.setValue(selectedDate);
            
            // Mettre à jour le DatePicker et le style du bouton
            checkFieldsValidity();
            
            // Animation subtile lors de la sélection
            FadeTransition fadeEffect = new FadeTransition(Duration.millis(200), calendarView);
            fadeEffect.setFromValue(0.8);
            fadeEffect.setToValue(1.0);
            fadeEffect.play();
        });
        
        // Ajouter le calendrier au conteneur
        if (datePickerContainer != null) {
            datePickerContainer.getChildren().add(calendarView);
            
            // Animation d'entrée du calendrier
            calendarView.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), calendarView);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1.0);
            fadeIn.setDelay(Duration.millis(300));
            fadeIn.play();
        }
    }

    public void setListeController(ListeReservationsController listeController) {
        this.listeController = listeController;
    }

    public void setIdGenerator(AtomicInteger idGenerator) {
        this.idGenerator = idGenerator;
        
        // Générer et afficher le prochain ID
        newId = idGenerator.get();
        if (idLabel != null) {
            idLabel.setText("ID: " + newId);
        }
    }

    @FXML
    void ajouterReservation(ActionEvent event) {
        if (validerChamps()) {
            // Créer une nouvelle réservation avec ID auto-incrémenté
            Reservation newReservation = new Reservation(
                    newId,
                    nomField.getText(),
                    datePicker.getValue(),
                    departField.getText(),
                    arriveeField.getText(),
                    statutComboBox.getValue(),
                    typeComboBox.getValue(),
                    commentaireField.getText()
            );
            
            // Incrémenter l'ID pour la prochaine réservation
            idGenerator.incrementAndGet();
            
            // Ajouter à la liste principale
            listeController.ajouterReservation(newReservation);
            
            // Fermer la fenêtre d'ajout
            ((Stage) nomField.getScene().getWindow()).close();
        }
    }

    private boolean validerChamps() {
        String nom = nomField.getText();
        LocalDate date = datePicker.getValue();
        String depart = departField.getText();
        String arrivee = arriveeField.getText();
        String statut = statutComboBox.getValue();
        String type = typeComboBox.getValue();

        // Validation rapide des champs obligatoires
        if (nom.isEmpty() || date == null || depart.isEmpty() || arrivee.isEmpty() || statut == null || type == null) {
            afficherErreur("Veuillez remplir tous les champs obligatoires");
            return false;
        }
        
        // Validations supplémentaires
        if (nom.length() < 3) {
            afficherErreur("Le nom doit contenir au moins 3 caractères");
            return false;
        }
        
        if (depart.equals(arrivee)) {
            afficherErreur("Le point de départ et d'arrivée ne peuvent pas être identiques");
            return false;
        }
        
        if (date.isBefore(LocalDate.now().minusDays(1))) {
            afficherErreur("La date ne peut pas être dans le passé");
            return false;
        }
        
        return true;
    }
    
    /**
     * Vérifie dynamiquement si tous les champs obligatoires sont remplis
     */
    private void checkFieldsValidity() {
        boolean allFieldsValid = 
                !nomField.getText().isEmpty() && 
                datePicker.getValue() != null && 
                !departField.getText().isEmpty() && 
                !arriveeField.getText().isEmpty() && 
                statutComboBox.getValue() != null &&
                typeComboBox.getValue() != null;
        
        // Mettre à jour le style du bouton d'ajout
        Button btnAjouter = (Button) nomField.getScene().lookup("#btnAjouter");
        if (btnAjouter != null) {
            btnAjouter.setDisable(!allFieldsValid);
            if (allFieldsValid) {
                btnAjouter.getStyleClass().add("carpool-add-button");
                btnAjouter.setOpacity(1.0);
            } else {
                btnAjouter.getStyleClass().remove("carpool-add-button");
                btnAjouter.setOpacity(0.7);
            }
        }
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de saisie");
        alert.setHeaderText("Veuillez corriger les erreurs suivantes");
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/fxml/styles.css").toExternalForm());
        alert.showAndWait();
    }

    @FXML
    public void annulerAjout(ActionEvent actionEvent) {
        // Fermer la fenêtre sans sauvegarder
        ((Stage) nomField.getScene().getWindow()).close();
    }
}