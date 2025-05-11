package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Principal {

    @FXML
    private Button boutonproposer;

    @FXML
    private Button bouttonvoirtr;

    @FXML
    private ImageView logoImage;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private Button searchButton;

    @FXML
    void handleContact(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SupprimerTrajet.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 4. Créer une nouvelle scène et l'afficher
        Stage stage = new Stage();
        stage.setTitle("Affichage Trajet");
        stage.setScene(new Scene(root));
        stage.show();

    }

    @FXML
    void handlePropose(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjouterTrajet.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 4. Créer une nouvelle scène et l'afficher
        Stage stage = new Stage();
        stage.setTitle("Ajouter Trajet");
        stage.setScene(new Scene(root));
        stage.show();


    }

    @FXML
    void handleSearch(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RechercherTrajet.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 4. Créer une nouvelle scène et l'afficher
        Stage stage = new Stage();
        stage.setTitle("Rechercher Trajet");
        stage.setScene(new Scene(root));
        stage.show();

    }

}
