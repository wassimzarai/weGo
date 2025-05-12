package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MenuFactureController {

    // Méthode pour ouvrir l'écran Ajouter Facture
    public void ouvrirAjouterFacture(ActionEvent event) {
        ouvrirInterface(event, "/AjouterFacture.fxml", "Impossible de charger l'interface Ajouter Facture");
    }

    // Méthode pour ouvrir l'écran Afficher Facture
    public void ouvrirAfficherFacture(ActionEvent event) {
        ouvrirInterface(event, "/AfficherFacture.fxml", "Impossible de charger l'interface Afficher Facture");
    }

    // Méthode pour ouvrir l'écran Modifier Facture
    public void ouvrirModifierFacture(ActionEvent event) {
        ouvrirInterface(event, "/ModifierFacture.fxml", "Impossible de charger l'interface Modifier Facture");
    }

    // Méthode pour ouvrir l'écran Supprimer Facture
    public void ouvrirSupprimerFacture(ActionEvent event) {
        ouvrirInterface(event, "/SupprimerFacture.fxml", "Impossible de charger l'interface Supprimer Facture");
    }

    // Méthode pour revenir au menu principal (corrigée)
    public void revenirAuMenuPrincipal(ActionEvent event) {
        ouvrirInterface(event, "/MenuPrincipal.fxml", "Impossible de charger l'interface Menu Principal");
    }

    // Méthode réutilisable pour ouvrir une interface
    private void ouvrirInterface(ActionEvent event, String fxmlPath, String errorMessage) {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showError("Erreur de chargement", errorMessage);
        }
    }

    // Affichage des erreurs
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
