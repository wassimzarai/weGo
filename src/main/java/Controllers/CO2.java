package Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import Entities.Trajet;
import service.ServiceTrajet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;


import org.json.JSONObject;



public class CO2 implements Initializable {

    @FXML
    private AreaChart<String, Number> areaChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    private static final String API_KEY = "AIzaSyAu_dJ86YWTWZG2xfDERD4rcNpGS3Div1o";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        afficherImpactEcologique();
        System.out.println("xAxis = " + xAxis);
        System.out.println("yAxis = " + yAxis);
        System.out.println("areaChart = " + areaChart);


    }

    private void afficherImpactEcologique() {
        ServiceTrajet st = new ServiceTrajet();
        ArrayList<Trajet> trajets = null;

        try {
            trajets = st.recupererTousLesTrajets();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (trajets == null || trajets.isEmpty()) {
            System.out.println("Aucun trajet trouvé.");
            return;
        }

        System.out.println("Nombre de trajets : " + trajets.size());

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("CO2 par trajet");

        for (Trajet trajet : trajets) {
            double distanceEstimee = estimerDistance(trajet.getDepart(), trajet.getArrivee());
            double emission = distanceEstimee * 0.2;

            String label = trajet.getDepart() + " -> " + trajet.getArrivee();
            System.out.println("Ajout du trajet : " + label + " avec " + emission + " kg de CO2");
            series.getData().add(new XYChart.Data<>(label, emission));
        }

        areaChart.getData().clear();
        areaChart.getData().add(series);

        xAxis.setLabel("Trajets");
        yAxis.setLabel("Émissions de CO2 (kg)");

        // Couleur après affichage
        Platform.runLater(() -> {
            if (series.getNode() != null) {
                series.getNode().setStyle("-fx-stroke: #2a9d8f; -fx-fill: rgba(42, 157, 143, 0.3);");
            }
        });

        xAxis.setTickLabelRotation(45);
    }

    private double estimerDistance(String depart, String arrivee) {
        try {
            // Obtenir les coordonnées de départ et d'arrivée via géocodage
            double[] coordDepart = obtenirCoordonnees(depart); // Utiliser une méthode pour obtenir les coordonnées
            double[] coordArrivee = obtenirCoordonnees(arrivee);

            // Extraire les coordonnées GPS
            double lat1 = coordDepart[0];
            double lon1 = coordDepart[1];
            double lat2 = coordArrivee[0];
            double lon2 = coordArrivee[1];

            // Construire l'URL de la requête OSRM pour obtenir la distance
            String urlString = "http://router.project-osrm.org/route/v1/driving/"
                    + lon1 + "," + lat1 + ";" + lon2 + "," + lat2 + "?overview=false";

            // Envoyer la requête à OSRM
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Lire la réponse de l'API
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Analyser la réponse JSON
            JSONObject jsonResponse = new JSONObject(response.toString());
//            double distance = jsonResponse.getJSONArray("routes")
//                    .getJSONObject(0)
//                    .getJSONArray("legs")
//                    .getJSONObject(0)
//                    .getJSONObject("distance")
//                    .getDouble("value");
            double distance = jsonResponse.getJSONArray("routes")
                    .getJSONObject(0)
                    .getJSONArray("legs")
                    .getJSONObject(0)
                    .getDouble("distance"); // ✅ Corrigé



            // Retourner la distance en kilomètres (valeur retournée en mètres)
            return distance / 1000;  // Conversion en kilomètres
        } catch (Exception e) {
            e.printStackTrace();
            return -1;  // Retourner -1 en cas d'erreur
        }
    }

    // Méthode pour obtenir les coordonnées géographiques d'une adresse via l'API Nominatim (OpenStreetMap)
    private double[] obtenirCoordonnees(String adresse) {
        try {
            // Construire l'URL de la requête pour obtenir les coordonnées via Nominatim
            String urlString = "https://nominatim.openstreetmap.org/search?q=" + adresse + "&format=json&addressdetails=1";

            // Créer l'objet URL pour la requête
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Lire la réponse de l'API de géocodage
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // ✅ Corrigé
            org.json.JSONArray jsonArray = new org.json.JSONArray(response.toString());
            if (jsonArray.length() == 0) {
                System.out.println("Aucune coordonnée trouvée pour : " + adresse);
                return new double[]{0, 0};
            }
            org.json.JSONObject location = jsonArray.getJSONObject(0);
            double lat = Double.parseDouble(location.getString("lat"));
            double lon = Double.parseDouble(location.getString("lon"));


            // Retourner les coordonnées sous forme de tableau {latitude, longitude}
            return new double[]{lat, lon};
        } catch (Exception e) {
            e.printStackTrace();
            return new double[]{0, 0};  // Retourner des coordonnées par défaut en cas d'erreur
        }
    }

//    public static void main(String[] args) {
//        CO2 co2 = new CO2();
//
//        // Exemple d'utilisation
//        String depart = "Tunis";
//        String arrivee = "Sfax";
//        double distance = co2.estimerDistance(depart, arrivee);
//
//        if (distance != -1) {
//            System.out.println("La distance entre " + depart + " et " + arrivee + " est de : " + distance + " km");
//        } else {
//            System.out.println("Erreur lors du calcul de la distance.");
//        }
//    }
}
