package gui;

import entities.Avis;
import entities.Reclamation;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import services.AvisService;
import services.ReclamationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class HomeController {
    
    @FXML
    private Button reclamationsButton;
    
    @FXML
    private Button avisButton;
    
    @FXML
    private AnchorPane mainAnchorPane;
    
    @FXML
    private Rectangle overlay;
    
    @FXML
    private ImageView logoView;
    
    @FXML
    private Label pendingReclamationsCount;
    
    @FXML
    private Label resolvedReclamationsCount;
    
    @FXML
    private HBox starsContainer;
    
    @FXML
    private SVGPath reclamationIcon;
    
    @FXML
    private SVGPath avisIcon;
    
    @FXML
    private SVGPath dashboardIcon;
    
    @FXML
    private Label currentDateLabel;
    
    private ReclamationService reclamationService;
    private AvisService avisService;
    
    @FXML
    public void initialize() {
        // Initialiser les services
        reclamationService = new ReclamationService();
        avisService = new AvisService();
        
        // Mettre à jour la date du jour
        updateCurrentDate();
        
        // Définir l'image de fond
        try {
            // Utiliser la nouvelle image fournie
            String imagePath = "/images/logo (2).png";
            Image backgroundImage = new Image(getClass().getResourceAsStream(imagePath), 
                                           1280, 720, false, true);
            
            if (backgroundImage.isError()) {
                System.err.println("Erreur lors du chargement de l'image de fond: " + backgroundImage.getException().getMessage());
                // Essayer avec l'ancienne image comme fallback
                backgroundImage = new Image(getClass().getResourceAsStream("/images/448846939_456051487055604_4464203815480634210_n (1).png"));
                System.out.println("Utilisation de l'image de secours comme arrière-plan");
            } else {
                System.out.println("Image de fond chargée avec succès: " + backgroundImage.getWidth() + "x" + backgroundImage.getHeight());
            }
            
            BackgroundImage bgImage = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(
                    BackgroundSize.AUTO, 
                    BackgroundSize.AUTO, 
                    false, 
                    false, 
                    true, 
                    true
                )
            );
            mainAnchorPane.setBackground(new Background(bgImage));
            
            // Charger le même logo (utiliser une portion de l'image)
            try {
                // Charger le logo (utilisez une image dédiée pour le logo)
                Image logoImage = new Image(getClass().getResourceAsStream("/images/logo (2).png"));
                
                // Si le logo dédié ne se charge pas, extraire un logo de l'image principale
                if (logoImage == null || logoImage.isError()) {
                    // Fallback - utiliser la même image que le fond
                    System.out.println("Utilisation de l'image principale pour le logo");
                    logoImage = new Image(getClass().getResourceAsStream(imagePath));
                }
                
                logoView.setImage(logoImage);
                // Style du logo déjà défini dans le CSS
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement du logo: " + e.getMessage());
            }
            
            // Afficher le nombre total de réclamations sur la page d'accueil
            if (pendingReclamationsCount != null) {
                pendingReclamationsCount.setText("6"); // Exemple de valeur pour la démo
            }
            
            if (resolvedReclamationsCount != null) {
                resolvedReclamationsCount.setText("0"); // Exemple de valeur pour la démo
            }
            
            // Animations
            setupAnimations();
            
            // Charger les données
            loadReclamationsData();
            
            System.out.println("Interface initialisée avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Met à jour le label de la date du jour avec le format "Jour, J mois YYYY"
     */
    private void updateCurrentDate() {
        if (currentDateLabel != null) {
            LocalDate today = LocalDate.now();
            
            // Obtenir le nom du jour en français
            String dayName = today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH);
            
            // Formater la date (jour, mois, année)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);
            String formattedDate = today.format(formatter);
            
            // Mettre la première lettre en majuscule
            dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
            
            // Afficher le résultat
            currentDateLabel.setText(dayName + ", " + formattedDate);
        }
    }
    
    private void setupAnimations() {
        // Animation pour les icônes
        reclamationIcon.setScaleX(0);
        reclamationIcon.setScaleY(0);
        avisIcon.setScaleX(0);
        avisIcon.setScaleY(0);
        
        // Animation pour la nouvelle icône dashboard
        if (dashboardIcon != null) {
            dashboardIcon.setScaleX(0);
            dashboardIcon.setScaleY(0);
        }
        
        // Animation d'apparition principale avec fondu
        javafx.animation.FadeTransition fadeInMain = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(1800), mainAnchorPane);
        fadeInMain.setFromValue(0.0);
        fadeInMain.setToValue(1.0);
        fadeInMain.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        // Animation parallèle pour un effet plus dynamique
        javafx.animation.ParallelTransition parallelFade = new javafx.animation.ParallelTransition();
        parallelFade.getChildren().add(fadeInMain);
        
        // Effet de mouvement subtil pour toute la page
        javafx.animation.TranslateTransition moveIn = new javafx.animation.TranslateTransition(
            javafx.util.Duration.millis(1500), mainAnchorPane);
        moveIn.setFromY(30);
        moveIn.setToY(0);
        moveIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        parallelFade.getChildren().add(moveIn);
        
        // Ajouter un effet de zoom léger
        javafx.animation.ScaleTransition scaleIn = new javafx.animation.ScaleTransition(
            javafx.util.Duration.millis(1500), mainAnchorPane);
        scaleIn.setFromX(0.95);
        scaleIn.setFromY(0.95);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        parallelFade.getChildren().add(scaleIn);
        
        // Animation pour les icônes avec séquencement
        javafx.animation.SequentialTransition sequence = new javafx.animation.SequentialTransition();
        
        // Icône réclamation avec rebond
        javafx.animation.ScaleTransition st1 = new javafx.animation.ScaleTransition(
            javafx.util.Duration.millis(700), reclamationIcon);
        st1.setFromX(0);
        st1.setFromY(0);
        st1.setToX(1.5);
        st1.setToY(1.5);
        st1.setInterpolator(javafx.animation.Interpolator.SPLINE(0.215, 0.610, 0.355, 1.000)); // Effet rebond
        
        // Rotation de l'icône pour un effet plus dynamique
        javafx.animation.RotateTransition rt1 = new javafx.animation.RotateTransition(
            javafx.util.Duration.millis(700), reclamationIcon);
        rt1.setFromAngle(-45);
        rt1.setToAngle(0);
        rt1.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        // Parallèle pour combiner scale et rotation
        javafx.animation.ParallelTransition pt1 = new javafx.animation.ParallelTransition();
        pt1.getChildren().addAll(st1, rt1);
        
        // Icône avis avec des mouvements différents
        javafx.animation.ScaleTransition st2 = new javafx.animation.ScaleTransition(
            javafx.util.Duration.millis(700), avisIcon);
        st2.setFromX(0);
        st2.setFromY(0);
        st2.setToX(1.5);
        st2.setToY(1.5);
        st2.setInterpolator(javafx.animation.Interpolator.SPLINE(0.215, 0.610, 0.355, 1.000));
        
        javafx.animation.RotateTransition rt2 = new javafx.animation.RotateTransition(
            javafx.util.Duration.millis(700), avisIcon);
        rt2.setFromAngle(45);
        rt2.setToAngle(0);
        rt2.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        javafx.animation.ParallelTransition pt2 = new javafx.animation.ParallelTransition();
        pt2.getChildren().addAll(st2, rt2);
        
        // Icône dashboard
        if (dashboardIcon != null) {
            javafx.animation.ScaleTransition st3 = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(700), dashboardIcon);
            st3.setFromX(0);
            st3.setFromY(0);
            st3.setToX(1.5);
            st3.setToY(1.5);
            st3.setInterpolator(javafx.animation.Interpolator.SPLINE(0.215, 0.610, 0.355, 1.000));
            
            javafx.animation.RotateTransition rt3 = new javafx.animation.RotateTransition(
                javafx.util.Duration.millis(700), dashboardIcon);
            rt3.setFromAngle(-45);
            rt3.setToAngle(0);
            rt3.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
            
            javafx.animation.ParallelTransition pt3 = new javafx.animation.ParallelTransition();
            pt3.getChildren().addAll(st3, rt3);
            
            // Ajouter à la séquence
            sequence.getChildren().add(pt3);
        }
        
        // Ajouter les animations à la séquence
        sequence.getChildren().addAll(pt1, pt2);
        
        // Ajouter un délai avant l'animation des icônes
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(
            javafx.util.Duration.millis(400));
        
        // Animation finale séquentielle
        javafx.animation.SequentialTransition finalAnimation = new javafx.animation.SequentialTransition();
        finalAnimation.getChildren().addAll(parallelFade, delay, sequence);
        finalAnimation.play();
        
        // Animation pour les statistiques
        if (pendingReclamationsCount != null && resolvedReclamationsCount != null) {
            // Animation pour le compteur "en attente"
            animateCounter(pendingReclamationsCount, 0, 6, 2000);
            
            // Animation pour le compteur "résolues"
            animateCounter(resolvedReclamationsCount, 0, 0, 2000);
        }
        
        // Animation subtile et continue pour le logo
        if (logoView != null) {
            javafx.animation.ScaleTransition pulseLogo = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(2200), logoView);
            pulseLogo.setFromX(1.0);
            pulseLogo.setFromY(1.0);
            pulseLogo.setToX(1.08);
            pulseLogo.setToY(1.08);
            pulseLogo.setCycleCount(javafx.animation.Animation.INDEFINITE);
            pulseLogo.setAutoReverse(true);
            pulseLogo.play();
        }
        
        // Animation des badges et indicateurs de notification
        animateBadgesAndIndicators();
    }
    
    /**
     * Animation des badges, indicateurs et autres éléments décoratifs
     */
    private void animateBadgesAndIndicators() {
        // On anime tous les éléments de la classe "notification-indicator" avec un effet de pulsation
        mainAnchorPane.lookupAll(".notification-indicator").forEach(node -> {
            // Animation de pulsation
            javafx.animation.ScaleTransition pulse = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(1800), node);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.15);
            pulse.setToY(1.15);
            pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
            pulse.setAutoReverse(true);
            
            // Animation de rotation légère
            javafx.animation.RotateTransition rotate = new javafx.animation.RotateTransition(
                javafx.util.Duration.millis(3000), node);
            rotate.setFromAngle(-3);
            rotate.setToAngle(3);
            rotate.setCycleCount(javafx.animation.Animation.INDEFINITE);
            rotate.setAutoReverse(true);
            
            // Jouer les deux animations en parallèle
            javafx.animation.ParallelTransition parallel = new javafx.animation.ParallelTransition();
            parallel.getChildren().addAll(pulse, rotate);
            parallel.play();
        });
        
        // Animation pour les badges "nouveau"
        mainAnchorPane.lookupAll(".new-indicator").forEach(node -> {
            // Animation de fondu
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(1200), node);
            fadeIn.setFromValue(0.7);
            fadeIn.setToValue(1.0);
            fadeIn.setCycleCount(javafx.animation.Animation.INDEFINITE);
            fadeIn.setAutoReverse(true);
            
            // Animation de translation subtile
            javafx.animation.TranslateTransition translate = new javafx.animation.TranslateTransition(
                javafx.util.Duration.millis(2000), node);
            translate.setFromX(-2);
            translate.setToX(2);
            translate.setCycleCount(javafx.animation.Animation.INDEFINITE);
            translate.setAutoReverse(true);
            
            // Jouer les animations en parallèle
            javafx.animation.ParallelTransition parallel = new javafx.animation.ParallelTransition();
            parallel.getChildren().addAll(fadeIn, translate);
            parallel.play();
        });
        
        // Animation pour le statut en ligne
        mainAnchorPane.lookupAll(".status-indicator").forEach(node -> {
            // Animation de pulsation
            javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(1500), node);
            scale.setFromX(0.85);
            scale.setFromY(0.85);
            scale.setToX(1.15);
            scale.setToY(1.15);
            scale.setCycleCount(javafx.animation.Animation.INDEFINITE);
            scale.setAutoReverse(true);
            
            // Animation de fondu
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(1500), node);
            fade.setFromValue(0.7);
            fade.setToValue(1.0);
            fade.setCycleCount(javafx.animation.Animation.INDEFINITE);
            fade.setAutoReverse(true);
            
            // Jouer les animations en parallèle
            javafx.animation.ParallelTransition parallel = new javafx.animation.ParallelTransition();
            parallel.getChildren().addAll(scale, fade);
            parallel.play();
        });
        
        // Animation pour les éléments de cartes
        mainAnchorPane.lookupAll(".menu-box").forEach(node -> {
            // Délai aléatoire pour un effet plus naturel
            double delayTime = Math.random() * 500;
            
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(
                javafx.util.Duration.millis(delayTime));
            
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(800), node);
            fadeIn.setFromValue(0.2);
            fadeIn.setToValue(1.0);
            
            javafx.animation.TranslateTransition moveUp = new javafx.animation.TranslateTransition(
                javafx.util.Duration.millis(800), node);
            moveUp.setFromY(30);
            moveUp.setToY(0);
            moveUp.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
            
            javafx.animation.SequentialTransition sequence = new javafx.animation.SequentialTransition();
            javafx.animation.ParallelTransition parallel = new javafx.animation.ParallelTransition();
            parallel.getChildren().addAll(fadeIn, moveUp);
            
            sequence.getChildren().addAll(delay, parallel);
            sequence.play();
        });
        
        // Animation pour les étoiles d'avis
        if (starsContainer != null) {
            int delay = 0;
            for (javafx.scene.Node star : starsContainer.getChildren()) {
                javafx.animation.FadeTransition starFade = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), star);
                starFade.setFromValue(0);
                starFade.setToValue(1);
                
                javafx.animation.ScaleTransition starScale = new javafx.animation.ScaleTransition(
                    javafx.util.Duration.millis(300), star);
                starScale.setFromX(0.5);
                starScale.setFromY(0.5);
                starScale.setToX(1.2);
                starScale.setToY(1.2);
                
                javafx.animation.ScaleTransition starScale2 = new javafx.animation.ScaleTransition(
                    javafx.util.Duration.millis(150), star);
                starScale2.setFromX(1.2);
                starScale2.setFromY(1.2);
                starScale2.setToX(1.0);
                starScale2.setToY(1.0);
                
                javafx.animation.SequentialTransition starSequence = new javafx.animation.SequentialTransition();
                javafx.animation.PauseTransition starDelay = new javafx.animation.PauseTransition(
                    javafx.util.Duration.millis(delay));
                javafx.animation.ParallelTransition starParallel = new javafx.animation.ParallelTransition();
                starParallel.getChildren().addAll(starFade, starScale);
                
                starSequence.getChildren().addAll(starDelay, starParallel, starScale2);
                starSequence.play();
                
                delay += 150;
            }
        }
    }
    
    /**
     * Anime un compteur avec effet de défilement
     * @param label Le label à animer
     * @param fromValue Valeur de départ
     * @param toValue Valeur finale
     * @param duration Durée en ms
     */
    private void animateCounter(Label label, int fromValue, int toValue, int duration) {
        // Créer une animation temporelle
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        
        // Créer une propriété pour suivre la valeur
        javafx.beans.property.IntegerProperty frameProperty = new javafx.beans.property.SimpleIntegerProperty(fromValue);
        frameProperty.addListener((observable, oldValue, newValue) -> {
            label.setText(String.valueOf(newValue));
        });
        
        // Ajouter les frames d'animation
        timeline.getKeyFrames().add(
            new javafx.animation.KeyFrame(
                javafx.util.Duration.ZERO,
                new javafx.animation.KeyValue(frameProperty, fromValue)
            )
        );
        
        timeline.getKeyFrames().add(
            new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(duration),
                new javafx.animation.KeyValue(frameProperty, toValue, 
                    javafx.animation.Interpolator.EASE_BOTH)
            )
        );
        
        // Démarrer l'animation
        timeline.play();
    }
    
    private void loadReclamationsData() {
        // Charger le nombre de réclamations en cours et résolues
        try {
            int pendingCount = 0;
            int resolvedCount = 0;
            
            for (Reclamation rec : reclamationService.voir()) {
                    pendingCount++;
                }

            pendingReclamationsCount.setText(String.valueOf(pendingCount));
            resolvedReclamationsCount.setText(String.valueOf(resolvedCount));
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des données de réclamation: " + e.getMessage());
        }
    }
    
    @FXML
    public void showReclamations() {
        // Redirection vers la page des réclamations
        MainController.getInstance().showMyReclamations();
    }
    
    @FXML
    public void showAvis() {
        // Redirection vers la page des avis
        MainController.getInstance().showAvisList();
    }
    
    @FXML
    public void showDashboard() {
        // Redirection vers le tableau de bord
        MainController.getInstance().showDashboard();
    }
} 