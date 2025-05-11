package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.DatabaseUpdater;
import utils.DatabaseInitializer;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.Statement;

public class TestFX extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main_container.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/reclamation.css").toExternalForm());

        primaryStage.setTitle("Gestion des Réclamations");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Update database schema before starting the application
        System.out.println("Initialisation et mise à jour de la base de données...");

        try {
            // Initialiser les tables nécessaires au démarrage
            DatabaseInitializer.initializeDatabase();

            // Mettre à jour les tables existantes si nécessaire
            DatabaseUpdater.updateReclamationTable();
            DatabaseUpdater.updateUserTable();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de la base de données: " + e.getMessage());
            e.printStackTrace();
        }

        launch(args);
    }
}
