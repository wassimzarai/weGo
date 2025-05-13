package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.chart.PieChart;
import Service.VoitureService;
import javafx.scene.control.Alert;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class StatisticsPageController {

    @FXML private Label brandCountLabel;
    @FXML private PieChart brandDistributionChart;  // Reference to the PieChart

    private VoitureService voitureService;

    @FXML
    public void initialize() {
        voitureService = new VoitureService();

        try {
            // Get the number of unique brands
            int brandCount = voitureService.getUniqueBrandCount();
            brandCountLabel.setText("Number of Brands: " + brandCount);

            // Get the brand distribution and display it on the PieChart
            Map<String, Double> brandDistribution = voitureService.getBrandDistribution();
            updatePieChart(brandDistribution);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load brand statistics: " + e.getMessage());
        }
    }

    // Method to update the PieChart with the brand distribution data
    private void updatePieChart(Map<String, Double> brandDistribution) {
        for (Map.Entry<String, Double> entry : brandDistribution.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
            brandDistributionChart.getData().add(slice);
        }
    }

    // Method to show alert in case of an error
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
