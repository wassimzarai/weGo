package gui;

import entities.Avis;
import entities.User;
import services.AvisService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import javafx.scene.text.Text;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class AvisFormController {
    @FXML
    private ComboBox<String> noteComboBox;
    
    @FXML
    private TextArea commentArea;
    
    @FXML
    private Text star1, star2, star3, star4, star5;
    
    @FXML
    private Label ratingLabel;
    
    private User currentUser;
    private final AvisService avisService;
    private Avis avisToEdit;
    private boolean isEditMode = false;
    private int currentRating = 0;
    
    // Colors for stars
    private final String STAR_INACTIVE = "#dddddd";
    private final String STAR_ACTIVE = "#FFC107"; // Yellow/Gold
    
    public AvisFormController() {
        avisService = new AvisService();
    }
    
    @FXML
    public void initialize() {
        // Make sure ComboBox has all values (even though it's hidden)
        if (noteComboBox.getItems().isEmpty()) {
            noteComboBox.getItems().addAll("1", "2", "3", "4", "5");
        }
        noteComboBox.setValue("1");
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    public void setAvisToEdit(Avis avis) {
        this.avisToEdit = avis;
        this.isEditMode = true;
        
        // Remplir les champs avec les valeurs de l'avis à éditer
        if (avis != null) {
            try {
                int rating = Integer.parseInt(avis.getRating());
                setRating(rating);
                commentArea.setText(avis.getComment());
            } catch (NumberFormatException e) {
                // Fallback si la note n'est pas un nombre
                noteComboBox.setValue(avis.getRating());
                commentArea.setText(avis.getComment());
            }
        }
    }
    
    @FXML
    public void handleStarClick(MouseEvent event) {
        Text source = (Text) event.getSource();
        int rating = getRatingFromStar(source);
        setRating(rating);
    }
    
    @FXML
    public void handleStarHover(MouseEvent event) {
        Text source = (Text) event.getSource();
        int rating = getRatingFromStar(source);
        highlightStars(rating);
    }
    
    @FXML
    public void handleStarExit(MouseEvent event) {
        highlightStars(currentRating);
    }
    
    private int getRatingFromStar(Text star) {
        if (star == star1) return 1;
        if (star == star2) return 2;
        if (star == star3) return 3;
        if (star == star4) return 4;
        if (star == star5) return 5;
        return 0;
    }
    
    private void setRating(int rating) {
        currentRating = rating;
        noteComboBox.setValue(String.valueOf(rating));
        highlightStars(rating);
        updateRatingLabel(rating);
    }
    
    private void highlightStars(int rating) {
        star1.setStyle("-fx-font-size: 30px; -fx-fill: " + (rating >= 1 ? STAR_ACTIVE : STAR_INACTIVE));
        star2.setStyle("-fx-font-size: 30px; -fx-fill: " + (rating >= 2 ? STAR_ACTIVE : STAR_INACTIVE));
        star3.setStyle("-fx-font-size: 30px; -fx-fill: " + (rating >= 3 ? STAR_ACTIVE : STAR_INACTIVE));
        star4.setStyle("-fx-font-size: 30px; -fx-fill: " + (rating >= 4 ? STAR_ACTIVE : STAR_INACTIVE));
        star5.setStyle("-fx-font-size: 30px; -fx-fill: " + (rating >= 5 ? STAR_ACTIVE : STAR_INACTIVE));
    }
    
    private void updateRatingLabel(int rating) {
        switch (rating) {
            case 0:
                ratingLabel.setText("Cliquez sur une étoile pour noter");
                break;
            case 1:
                ratingLabel.setText("Très insatisfait");
                break;
            case 2:
                ratingLabel.setText("Insatisfait");
                break;
            case 3:
                ratingLabel.setText("Correct");
                break;
            case 4:
                ratingLabel.setText("Satisfait");
                break;
            case 5:
                ratingLabel.setText("Très satisfait");
                break;
        }
    }

    @FXML
    public void handleSubmit() {
        if (currentRating == 0) {
            showError("Validation Error", "Veuillez sélectionner une note");
            return;
        }

        try {
            Avis avis;
            
            if (isEditMode && avisToEdit != null) {
                avis = avisToEdit;
            } else {
                avis = new Avis();
                // Créer une date SQL avec la date actuelle
                java.util.Date utilDate = new java.util.Date();
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                avis.setDate(sqlDate);
            }
            
            avis.setRating(String.valueOf(currentRating));
            avis.setComment(commentArea.getText());
            
            // Utiliser l'utilisateur actuel si disponible
            if (currentUser != null) {
                avis.setUser(currentUser);
                
                if (isEditMode) {
                    avisService.modifier(avis);
                    showInfo("Succès", "Votre avis a été modifié avec succès");
                } else {
                    avisService.ajouter(avis);
                    showInfo("Succès", "Votre avis a été ajouté avec succès");
                }
                
                // Fermer la fenêtre
                closeWindow();
            } else {
                showError("Erreur", "Vous devez être connecté pour soumettre un avis");
            }
        } catch (SQLException e) {
            String action = isEditMode ? "modifier" : "ajouter";
            showError("Erreur", "Impossible de " + action + " l'avis: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }
    
    private void closeWindow() {
        // Récupérer la fenêtre actuelle et la fermer
        Stage stage = (Stage) commentArea.getScene().getWindow();
        stage.close();
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
} 