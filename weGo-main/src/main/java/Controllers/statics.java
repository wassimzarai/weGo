package Controllers;
import utils.database;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class statics implements Initializable {
    private Connection connect;
    @FXML
    private Button EXIT;
    @FXML
    private Label totalReclamationsLabel;
    @FXML
    private BarChart<String, Number> statBarChart;
    @FXML
    private CategoryAxis statCategoryAxis;
    @FXML
    private NumberAxis statNumberAxis;
    @FXML
    private PieChart genderPieChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        displayStatisticsInBarChart();
        displayGenderDonutChart();
    }

    private int getTotalutilisateurFromDatabase() {
        int totalutilisateur = 0;

        try {
            connect = database.connectDb();
            String query = "SELECT COUNT(*) FROM utilisateur";
            PreparedStatement preparedStatement = connect.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                totalutilisateur = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalutilisateur;
    }

    public void EXIT() {
        System.exit(0);
    }

    private void displayStatisticsInBarChart() {
        statBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("utilisateur");

        int totalutilisateur = getTotalutilisateurFromDatabase();
        series.getData().add(new XYChart.Data<>("Total", totalutilisateur));

        statBarChart.getData().add(series);
    }

    private void displayGenderDonutChart() {
        int men = 0;
        int women = 0;
        int other = 0;
        try {
            connect = database.connectDb();
            String query = "SELECT gender, COUNT(*) as count FROM utilisateur WHERE gender IS NOT NULL AND gender <> '' GROUP BY gender";
            PreparedStatement preparedStatement = connect.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String gender = resultSet.getString("gender");
                int count = resultSet.getInt("count");
                if ("Male".equalsIgnoreCase(gender)) men = count;
                else if ("Female".equalsIgnoreCase(gender)) women = count;
                else other += count;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int total = men + women + other;
        double menPercent = total > 0 ? (men * 100.0 / total) : 0;
        double womenPercent = total > 0 ? (women * 100.0 / total) : 0;
        double otherPercent = total > 0 ? (other * 100.0 / total) : 0;

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Hommes (" + String.format("%.1f", menPercent) + "%)", men),
            new PieChart.Data("Femmes (" + String.format("%.1f", womenPercent) + "%)", women)
        );
        if (other > 0) {
            pieChartData.add(new PieChart.Data("Autres (" + String.format("%.1f", otherPercent) + "%)", other));
        }
        genderPieChart.setData(pieChartData);
        genderPieChart.setTitle("Répartition Hommes/Femmes");
        genderPieChart.setLabelsVisible(true);
        genderPieChart.setLegendVisible(true);
        // Style donut via CSS (à ajouter dans le CSS):
        // .chart-pie { -fx-pie-inner-radius: 60; }
    }
}
