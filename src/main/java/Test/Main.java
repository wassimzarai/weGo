package Test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/MenuPrincipal.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setTitle("Gestion des Paiements");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement de la vue: " + e.getMessage());
            e.printStackTrace();
        }
    }























    public static void main(String[] args) {
        launch(args);
    }
}