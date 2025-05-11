package Controllers;

import Entites.Trajet;
import Entites.Pointrdv;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.ServiceTrajet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Insets;

public class RechercherTrajet {

    @FXML
    private ScrollPane mapsScrollPane;
    @FXML
    private Button idhomee;

    @FXML
    private TextField searchField;

    @FXML
    private VBox resultsVBox;
    @FXML
    private Button btnStats;


    private ServiceTrajet st = new ServiceTrajet();

    @FXML
    void boutonrechercher(ActionEvent event) {
        String searchText = searchField.getText();
        resultsVBox.getChildren().clear(); // Nettoyer les anciens résultats

        try {
            List<Trajet> trajets = st.getByArrivee(searchText);
            if (trajets.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("");
                alert.setHeaderText(null);
                alert.setContentText("La ville " + searchText + " n'a pas de trajet pour le moment");
                alert.showAndWait();
            }
            for (Trajet trajet : trajets) {
                VBox card = createTrajetCard(trajet);
                resultsVBox.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createTrajetCard(Trajet trajet) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("""
        -fx-background-color: #ffffff;
        -fx-background-radius: 15;
        -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);
    """);


        // Titre
        Label title = new Label(trajet.getDepart() + " → " + trajet.getArrivee());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
//        title.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {});
        String adresse;
        if (trajet.getPointrdv() != null) {
            adresse = trajet.getPointrdv().getAdresse();
        } else {
            adresse = "Aucun point de rendez-vous";
        }

        Label rdv = new Label("📍 Lieu de rendez-vous: " + adresse);
        rdv.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");


        // Infos générales
        Label date = new Label("📅 Date : " + trajet.getDate());
        Label heure = new Label("⏰ Heure : " + trajet.getHeure());

        // Infos voiture
        Label voitureTitle = new Label("🚗 Voiture");
        voitureTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495E;");

        Label marque = new Label("🏷️ Marque : " + trajet.getVoiture().getMarque());
        Label modele = new Label("📘 Modèle : " + trajet.getVoiture().getModele());
        Label matricule = new Label("🔢 Matricule : " + trajet.getVoiture().getMatricule());

        VBox voitureInfo = new VBox(3, voitureTitle, marque, modele, matricule);

        // Infos restantes
        Label prix = new Label("💰 Prix : " + trajet.getPrix_place() + "€");
        Label places = new Label("👥 Places dispo : " + trajet.getNb_place());
        Label statut = new Label("🔒 Statut : " + trajet.getStatut());

        for (Label l : List.of(date, heure, marque, modele, matricule, prix, places, statut)) {
            l.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
        }
        Button btnMap = new Button("🗺️ Voir la carte");
        btnMap.setStyle("-fx-background-color: #2980B9; -fx-text-fill: white; -fx-font-weight: bold;");


        btnMap.setOnAction(e -> {
            try {
                String depart = trajet.getDepart().replace(" ", "+");
                String arrivee = trajet.getArrivee().replace(" ", "+");
                String url = "https://www.google.com/maps/dir/" + depart + "/" + arrivee;
//                String url = "https://fr.wikipedia.org" ;

                // Passe l’URL à CarteTrajet
                CarteTrajet.setMapUrl(url);

                // Charge la nouvelle interface
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CarteTrajet.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setTitle("Carte du Trajet");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });






        card.getChildren().addAll(title,rdv, date, heure, voitureInfo, prix, places, statut, btnMap);
        return card;
    }

    public void afficherStatistiques(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Statistiques.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Statistiques des Trajets");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


//    private void reserverTrajet(Trajet trajet) {
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.setTitle("Réservation");
//        alert.setHeaderText("Souhaitez-vous réserver ce trajet ?");
//        alert.setContentText(
//                "Départ : " + trajet.getDepart() + "\n" +
//                        "Arrivée : " + trajet.getArrivee() + "\n" +
//                        "Date : " + trajet.getDate() + "\n" +
//                        "Heure : " + trajet.getHeure() + "\n" +
//                        "Prix : " + trajet.getPrix_place() + "€"
//        );
//
//        ButtonType oui = new ButtonType("Oui", ButtonBar.ButtonData.OK_DONE);
//        ButtonType non = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);
//        alert.getButtonTypes().setAll(oui, non);
//
//        Optional<ButtonType> result = alert.showAndWait();
//        if (result.isPresent() && result.get() == oui) {
//            System.out.println("Trajet réservé avec succès !");
//            // ➕ tu peux ici appeler une méthode pour l'ajouter à la base
//        } else {
//            System.out.println("Réservation annulée.");
//        }
//    }
@FXML
void buttonhome(ActionEvent event) {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Principal.fxml"));
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
