package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class Carte {

    @FXML
    private WebView webView;

    private WebEngine webEngine;

    // Initialiser le WebView et charger la carte
    @FXML
    public void initialize() {
        // Récupérer le moteur de la carte
        webEngine = webView.getEngine();
        // Charger le fichier HTML qui contient la carte (remplacez le chemin par le bon)
        webEngine.load("/map.html");
    }

    // Méthode pour récupérer la localisation sélectionnée sur la carte
    @FXML
    void onSelectLocation(ActionEvent event) {
        // Exécuter un script JavaScript pour récupérer la localisation depuis la carte
        String script = "document.getElementById('selectedLocation').value";  // Assurez-vous que votre carte a un élément 'selectedLocation'
        Object location = webEngine.executeScript(script);

        // Si une localisation est trouvée
        if (location != null) {
            // Passer la localisation à l'interface principale (via un mécanisme comme une méthode setter)
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Emplacement sélectionné");
            alert.setHeaderText(null);
            alert.setContentText("Emplacement sélectionné: " + location.toString());
            alert.showAndWait();
        } else {
            // Si aucune localisation n'est trouvée
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Aucune localisation sélectionnée.");
            alert.showAndWait();
        }
    }
}
