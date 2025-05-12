package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

public class TestFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Charger l'interface
            FXMLLoader loader = new FXMLLoader();
            URL fxmlUrl = getClass().getResource("/ListeReservations.fxml");
            
            if (fxmlUrl == null) {
                throw new IOException("Impossible de trouver le fichier FXML: /ListeReservations.fxml");
            }
            
            loader.setLocation(fxmlUrl);
            Parent root = loader.load();

            // Configurer la scène
            Scene scene = new Scene(root);
            
            // Charger le CSS
            URL cssUrl = getClass().getResource("/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Configurer la fenêtre principale
            primaryStage.setTitle("Gestion des Réservations");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(false);
            primaryStage.show();
            
        } catch (Exception e) {
            handleError("Erreur de démarrage", "Impossible de démarrer l'application", e);
            throw e;
        }
    }
    
    private void handleError(String title, String header, Exception e) {
        System.err.println(title + ": " + e.getMessage());
        e.printStackTrace();
        
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText("Erreur: " + e.getMessage());
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        
        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        
        alert.getDialogPane().setExpandableContent(textArea);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
