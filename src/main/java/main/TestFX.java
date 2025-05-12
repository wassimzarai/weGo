package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.DatabaseUpdater;
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
        System.out.println("Checking and updating database schema...");

        try {

            // Update with intelligent features
            DatabaseUpdater.updateReclamationTable();

            // Update user table to include profile columns
            DatabaseUpdater.updateUserTable();
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }

        launch(args);
    }
    


}
