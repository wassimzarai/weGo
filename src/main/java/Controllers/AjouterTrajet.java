package Controllers;

import Entities.Trajet;
import Entities.Pointrdv;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import netscape.javascript.JSObject;
import service.ServiceTrajet;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public class AjouterTrajet {

    @FXML
    private TextField arriveeTextField;

    @FXML
    private ComboBox<Integer> comboboxHeure;

    @FXML
    private ComboBox<Integer> comboboxMinute;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField departTextField;

    @FXML
    private TextField nombrePlacesTextField;
    @FXML
    private Button idhome;

    @FXML
    private Spinner<Integer> prixSpinner;
    @FXML
    private TextField textfieldrdv;


    @FXML
    public void initialize() {
        // Remplissage des ComboBox pour l'heure et les minutes
        for (int i = 0; i < 24; i++) {
            comboboxHeure.getItems().add(i);
        }
        for (int i = 0; i < 60; i++) {
            comboboxMinute.getItems().add(i);
        }

        // Valeur par défaut (par exemple, première valeur de chaque ComboBox)
        comboboxHeure.getSelectionModel().selectFirst();
        comboboxMinute.getSelectionModel().selectFirst();
    }

    @FXML
    void ajouterTrajet(ActionEvent event) {
        ServiceTrajet st = new ServiceTrajet();
        Pointrdv p = new Pointrdv(textfieldrdv.getText());


        try {
            // Récupération et validation des heures et minutes
            Integer heure = comboboxHeure.getValue();
            Integer minute = comboboxMinute.getValue();
            if (heure == null || minute == null) {
                throw new IllegalArgumentException("Veuillez sélectionner une valeur pour les heures et les minutes.");
            }
            String Horraire = String.format("%02d:%02d", heure, minute);
            LocalTime HorraireLocalTime = LocalTime.parse(Horraire);

            // Récupération et validation du prix
            Integer prix_place = prixSpinner.getValue();
            if (prix_place == null || prix_place <= 0) {
                throw new IllegalArgumentException("Veuillez entrer un prix valide.");
            }

            // Validation de la date
            LocalDate date = datePicker.getValue();
            if (date == null) {
                throw new IllegalArgumentException("Veuillez choisir une date valide.");
            }

            // Validation et conversion du champ 'nombre de places'
            String nombrePlacesText = nombrePlacesTextField.getText();
            if (nombrePlacesText == null || nombrePlacesText.trim().isEmpty()) {
                throw new IllegalArgumentException("Le champ 'nombre de places' ne peut pas être vide.");
            }
            int nombrePlaces;
            try {
                nombrePlaces = Integer.parseInt(nombrePlacesText);
                if (nombrePlaces <= 0) {
                    throw new IllegalArgumentException("Le nombre de places doit être supérieur à zéro.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Veuillez entrer un nombre entier valide pour le champ 'nombre de places'.");
            }

            // Création de l'objet Trajet
            Trajet t = new Trajet(
                    departTextField.getText(),
                    arriveeTextField.getText(),
                    date,
                    HorraireLocalTime,
                    prix_place,
                    nombrePlaces,p
            );

            // Ajout du trajet via le service
            if(st.ajouter(t)){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Trajet ajouté avec succès !");
                alert.showAndWait();
            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors de l'ajout du trajet.");
            }

        } catch (IllegalArgumentException e) {
            // Affiche un message d'erreur utilisateur
            System.err.println("Erreur : " + e.getMessage());
        } catch (SQLException e) {
            // Gestion des exceptions SQL
            System.err.println("Impossible d'ajouter le trajet : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void ouvrircarte(ActionEvent event) {
        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);
        webView.setPrefSize(800, 600);

        WebEngine webEngine = webView.getEngine();

        Stage mapStage = new Stage();
        mapStage.setTitle("Choisir un emplacement");
        mapStage.setScene(new Scene(webView));
        mapStage.show();

        URL mapURL = getClass().getResource("/map.html");
        if (mapURL == null) {
            System.err.println("Fichier map.html introuvable !");
            return;
        }

        webEngine.load(mapURL.toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                final Timeline[] polling = new Timeline[1];
                polling[0] = new Timeline(
                        new KeyFrame(Duration.seconds(1), ev -> {
                            try {
                                Object result = webEngine.executeScript(
                                        "document.getElementById('coordonnees') && document.getElementById('coordonnees').value"
                                );

                                if (result instanceof String adresse && !adresse.isEmpty()) {
                                    textfieldrdv.setText(adresse);
                                    polling[0].stop();
                                    webEngine.executeScript("document.getElementById('coordonnees').value = ''");
                                    mapStage.close();
                                }
                            } catch (Exception e) {
                                System.err.println("Erreur lors du polling : " + e.getMessage());
                            }
                        })
                );

                polling[0].setCycleCount(Animation.INDEFINITE);
                polling[0].play();
            }
        });
    }
    @FXML
    void buttonhome(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/principal.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // 4. Créer une nouvelle scène et l'afficher
        Stage stage = new Stage();
        stage.setTitle("Modifier Trajet");
        stage.setScene(new Scene(root));
        stage.show();



    }

}
