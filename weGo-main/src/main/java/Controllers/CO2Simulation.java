package Controllers;
import javafx.application.Application;

import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class CO2Simulation extends Application {


    @Override
    public void start(Stage primaryStage) {
        // Configuration des axes
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Trajet");
        yAxis.setLabel("Émissions CO2 (g)");

        // Création de l'AreaChart
        AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
        areaChart.setTitle("Émission de CO2 par Trajet");

        // Création de la série de données
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("CO2");

        // Simulation de données pour les trajets et leurs émissions de CO2
        series.getData().add(new XYChart.Data<>("Paris -> Lyon", 80));      // Trajet 1
        series.getData().add(new XYChart.Data<>("Lyon -> Marseille", 120)); // Trajet 2
        series.getData().add(new XYChart.Data<>("Marseille -> Toulouse", 100)); // Trajet 3
        series.getData().add(new XYChart.Data<>("Toulouse -> Bordeaux", 150)); // Trajet 4
        series.getData().add(new XYChart.Data<>("Bordeaux -> Paris", 110)); // Trajet 5

        // Ajout de la série de données au graphique
        areaChart.getData().add(series);

        // Personnalisation des couleurs pour chaque point de la série
        for (XYChart.Data<String, Number> data : series.getData()) {
            double value = data.getYValue().doubleValue();
            // Dynamique de couleur en fonction de la valeur de CO2
            if (value < 100) {
                data.getNode().setStyle("-fx-fill: rgba(0, 255, 0, 0.4);");  // Vert pour CO2 faible
            } else if (value < 150) {
                data.getNode().setStyle("-fx-fill: rgba(255, 165, 0, 0.4);");  // Orange pour CO2 moyen
            } else {
                data.getNode().setStyle("-fx-fill: rgba(255, 0, 0, 0.4);");  // Rouge pour CO2 élevé
            }
        }

        // Créer la scène et ajouter l'AreaChart
        StackPane root = new StackPane();
        root.getChildren().add(areaChart);
        Scene scene = new Scene(root, 800, 600);

        // Affichage de la scène
        primaryStage.setTitle("Graphique CO2");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
