package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Client pour l'API de réservation automatique
 */
public class ReservationAutomatiqueClient {

    private static final String API_URL = "http://localhost:8080/api/reservations/automatique";
    
    /**
     * Représente une réponse de l'API
     */
    public static class ApiResponse {
        private boolean success;
        private String message;
        private int statusCode;
        private String rawJson;
        
        public ApiResponse(boolean success, String message, int statusCode, String rawJson) {
            this.success = success;
            this.message = message;
            this.statusCode = statusCode;
            this.rawJson = rawJson;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public String getRawJson() {
            return rawJson;
        }
    }
    
    /**
     * Exécute une requête de réservation automatique
     * @param passagerId L'ID du passager
     * @param utiliserItineraireHabituel Si true, utilise l'itinéraire habituel du passager
     * @param villeDepart Ville de départ (ignoré si utiliserItineraireHabituel=true)
     * @param villeArrivee Ville d'arrivée (ignoré si utiliserItineraireHabituel=true)
     * @param nombrePlaces Nombre de places à réserver
     * @return La réponse de l'API formatée
     */
    public static ApiResponse reserverAutomatiquement(int passagerId, boolean utiliserItineraireHabituel, 
                                                String villeDepart, String villeArrivee, int nombrePlaces) {
        try {
            // Préparation de la connexion HTTP
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            
            // Création du corps de la requête JSON
            String jsonRequest = String.format(
                "{\"passagerId\": %d, \"villeDepart\": \"%s\", \"villeArrivee\": \"%s\", \"nombrePlaces\": %d, \"utiliserItineraireHabituel\": %b}",
                passagerId, villeDepart, villeArrivee, nombrePlaces, utiliserItineraireHabituel);
            
            // Envoi de la requête
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Lecture de la réponse
            int responseCode = connection.getResponseCode();
            
            try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                    responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream(), 
                    StandardCharsets.UTF_8))) {
                
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                
                String rawJson = response.toString();
                String message = extraireValeur(rawJson, "message");
                boolean success = rawJson.contains("\"success\":true");
                
                return new ApiResponse(success, message, responseCode, rawJson);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse(false, "Erreur de connexion: " + e.getMessage(), 500, null);
        }
    }
    
    /**
     * Extrait une valeur d'une chaîne JSON basique
     */
    private static String extraireValeur(String json, String cle) {
        String recherche = "\"" + cle + "\":\"";
        int indexDebut = json.indexOf(recherche) + recherche.length();
        if (indexDebut < recherche.length()) return "";
        
        int indexFin = json.indexOf("\"", indexDebut);
        if (indexFin < 0) return "";
        
        return json.substring(indexDebut, indexFin);
    }
    
    /**
     * Exemple d'utilisation du client
     */
    public static void main(String[] args) {
        // Exemple 1: Réservation avec itinéraire habituel
        ApiResponse response1 = reserverAutomatiquement(1, true, null, null, 1);
        System.out.println("Réservation 1 (itinéraire habituel):");
        System.out.println("  Succès: " + response1.isSuccess());
        System.out.println("  Message: " + response1.getMessage());
        System.out.println("  Code: " + response1.getStatusCode());
        
        // Exemple 2: Réservation avec itinéraire spécifié
        ApiResponse response2 = reserverAutomatiquement(2, false, "Tunis", "Sfax", 2);
        System.out.println("\nRéservation 2 (itinéraire spécifié):");
        System.out.println("  Succès: " + response2.isSuccess());
        System.out.println("  Message: " + response2.getMessage());
        System.out.println("  Code: " + response2.getStatusCode());
    }
} 