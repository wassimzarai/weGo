package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service pour gérer les fonctionnalités de géolocalisation.
 * Dans une application réelle, utiliserait l'API Google Maps ou une autre API
 * de géolocalisation.
 */
public class GeoLocationService {
    
    private static final Logger LOGGER = Logger.getLogger(GeoLocationService.class.getName());
    private static final String NOMINATIM_API = "https://nominatim.openstreetmap.org/";
    
    /**
     * Récupère les coordonnées GPS à partir d'une adresse
     * @param adresse L'adresse à géocoder
     * @return un tableau de double [latitude, longitude] ou null en cas d'erreur
     */
    public static double[] getCoordonnees(String adresse) {
        try {
            // Encoder l'adresse pour l'URL
            String encodedAddress = URLEncoder.encode(adresse, StandardCharsets.UTF_8.toString());
            String requestUrl = NOMINATIM_API + "search?q=" + encodedAddress + "&format=json&limit=1";
            
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "WeGo Application");
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // Analyse de réponse JSON basique
                // Exemple de réponse: [{"lat":"48.8566","lon":"2.3522",...}]
                String jsonString = response.toString();
                Pattern latPattern = Pattern.compile("\"lat\":\"([^\"]+)\"");
                Pattern lonPattern = Pattern.compile("\"lon\":\"([^\"]+)\"");
                
                Matcher latMatcher = latPattern.matcher(jsonString);
                Matcher lonMatcher = lonPattern.matcher(jsonString);
                
                if (latMatcher.find() && lonMatcher.find()) {
                    double lat = Double.parseDouble(latMatcher.group(1));
                    double lon = Double.parseDouble(lonMatcher.group(1));
                    return new double[] { lat, lon };
                }
            } else {
                LOGGER.log(Level.WARNING, "Échec de la requête géocodage: code {0}", responseCode);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du géocodage de l'adresse", e);
        }
        
        return null;
    }
    
    /**
     * Récupère l'adresse à partir des coordonnées GPS (géocodage inverse)
     * @param latitude La latitude
     * @param longitude La longitude
     * @return L'adresse correspondante ou null en cas d'erreur
     */
    public static String getAdresse(double latitude, double longitude) {
        try {
            String requestUrl = NOMINATIM_API + "reverse?lat=" + latitude + "&lon=" + longitude + "&format=json";
            
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "WeGo Application");
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // Analyse de réponse JSON basique
                // Exemple: {"display_name":"123 Example Street, City, Country",...}
                String jsonString = response.toString();
                Pattern displayNamePattern = Pattern.compile("\"display_name\":\"([^\"]+)\"");
                
                Matcher matcher = displayNamePattern.matcher(jsonString);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            } else {
                LOGGER.log(Level.WARNING, "Échec de la requête géocodage inverse: code {0}", responseCode);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du géocodage inverse", e);
        }
        
        return null;
    }
    
    /**
     * Calcule la distance entre deux points GPS en kilomètres (formule de Haversine)
     * @param lat1 Latitude du point 1
     * @param lon1 Longitude du point 1
     * @param lat2 Latitude du point 2
     * @param lon2 Longitude du point 2
     * @return La distance en kilomètres
     */
    public static double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        // Rayon de la Terre en kilomètres
        final int R = 6371;
        
        // Conversion en radians
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        // Formule de Haversine
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
} 