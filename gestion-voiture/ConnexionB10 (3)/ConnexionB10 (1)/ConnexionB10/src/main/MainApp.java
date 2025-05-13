package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/fxmlfile.fxml"));

        // Debug: Print the FXML path to confirm it's being found
        System.out.println("FXML Path: " + getClass().getResource("/fxml/fxmlfile.fxml"));

        AnchorPane root = loader.load();

        // Set the scene
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Ajouter Voiture");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
