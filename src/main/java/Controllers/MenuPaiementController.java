package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MenuPaiementController {

    // Méthode pour ouvrir l'écran Ajouter Paiement
    public void ouvrirAjouter(ActionEvent event) {
        chargerInterface(event, "/AjouterPaiement.fxml", "Impossible de charger l'interface Ajouter Paiement");
    }

    // Méthode pour ouvrir l'écran Afficher Paiement
    public void ouvrirAfficher(ActionEvent event) {
        chargerInterface(event, "/AfficherPaiement.fxml", "Impossible de charger l'interface Afficher Paiement");
    }

    // Méthode pour ouvrir l'écran Modifier Paiement
    public void ouvrirModifier(ActionEvent event) {
        chargerInterface(event, "/ModifierPaiement.fxml", "Impossible de charger l'interface Modifier Paiement");
    }

    // Méthode pour ouvrir l'écran Supprimer Paiement
    public void ouvrirSupprimer(ActionEvent event) {
        chargerInterface(event, "/SupprimerPaiement.fxml", "Impossible de charger l'interface Supprimer Paiement");
    }

    // Méthode pour revenir au menu principal
    public void revenirAuMenu(ActionEvent event) {
        chargerInterface(event, "/MenuPrincipal.fxml", "Impossible de charger l'interface Menu Principal");
    }

    // Méthode générique pour charger une interface
    private void chargerInterface(ActionEvent event, String cheminFXML, String messageErreur) {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource(cheminFXML));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showError("Erreur de chargement", messageErreur);
        }
    }

    // Méthode pour afficher un message d'erreur en cas de problème
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
