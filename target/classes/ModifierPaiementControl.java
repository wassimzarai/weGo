package Controllers;

import entities.Paiement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.ServicePaiement;

import java.io.IOException;

public class ModifierPaiementControl {
        @FXML private TextField txtid;
        @FXML private TextField txtidres;
        @FXML private TextField txtmontant;
        @FXML private TextField txtmethode;
        @FXML private TextField txtdatep;
        @FXML private TextField txtstatut;
        @FXML private TextField txtRechercheId;
        @FXML private Button btnModifier;
        @FXML private Button btnRechercher;
        @FXML private Button btnRetourMenu;  // Le bouton pour revenir au menu

        private final ServicePaiement service = new ServicePaiement();

        // Méthode pour modifier le paiement
        @FXML
        void btnmodifier(ActionEvent event) {
                try {
                        Paiement p = new Paiement(
                                Integer.parseInt(txtid.getText()),
                                Integer.parseInt(txtidres.getText()),
                                Double.parseDouble(txtmontant.getText()),
                                txtdatep.getText(),
                                txtmethode.getText(),
                                txtstatut.getText()
                        );

                        if (service.modifierPaiement(p)) {
                                afficherSucces("Paiement modifié avec succès !");
                        } else {
                                afficherErreur("Échec de la modification du paiement.");
                        }
                } catch (NumberFormatException e) {
                        afficherErreur("Veuillez entrer des valeurs valides.");
                }
        }

        // Méthode pour rechercher un paiement
        @FXML
        void rechercherPaiement(ActionEvent event) {
                try {
                        int idRecherche = Integer.parseInt(txtRechercheId.getText());
                        Paiement p = service.rechercherPaiementParId(idRecherche);

                        if (p != null) {
                                txtid.setText(String.valueOf(p.getId()));
                                txtidres.setText(String.valueOf(p.getReservationId()));
                                txtmontant.setText(String.valueOf(p.getMontant()));
                                txtdatep.setText(p.getDatePaiement());
                                txtmethode.setText(p.getMethodePaiement());
                                txtstatut.setText(p.getStatut());
                                afficherSucces("Paiement trouvé !");
                        } else {
                                afficherErreur("Aucun paiement trouvé avec cet ID.");
                        }
                } catch (NumberFormatException e) {
                        afficherErreur("Veuillez entrer un ID valide.");
                }
        }

        // Méthode pour revenir au menu
        @FXML
        private void handleBackToMenu(ActionEvent event) {
                try {
                        // Charger la scène du menu principal
                        Stage stage = (Stage) btnRetourMenu.getScene().getWindow();
                        Scene menuScene = new Scene(FXMLLoader.load(getClass().getResource("/path/to/menu.fxml")));  // Remplacer le chemin vers Menu.fxml
                        stage.setScene(menuScene);
                } catch (IOException e) {
                        e.printStackTrace();
                        afficherErreur("Erreur lors du chargement du menu.");
                }
        }

        // Méthode pour afficher un message de succès
        private void afficherSucces(String message) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
        }

        // Méthode pour afficher un message d'erreur
        private void afficherErreur(String message) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
        }
}
