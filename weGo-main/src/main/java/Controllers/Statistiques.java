package Controllers;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import services.ServiceTrajet;

import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class Statistiques implements Initializable {
    @FXML
    private BarChart<String, Number> barChart;

    private ServiceTrajet serviceTrajet = new ServiceTrajet();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Map<String, Integer> stats = serviceTrajet.getNombreTrajetsParArrivee();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Trajets fréquents");

            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            barChart.getData().add(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
