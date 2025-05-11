package utils;

import Entites.Reclamation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilitaire d'analyse NLP simple pour la classification des réclamations.
 * Dans une version réelle, intégrerait des bibliothèques comme OpenNLP, Stanford NLP, ou 
 * des services cloud comme Google Natural Language API ou Amazon Comprehend.
 */
public class NLPAnalyzer {
    
    // Mots-clés pour identifier la gravité
    private static final Map<String, List<String>> GRAVITE_KEYWORDS = new HashMap<>();
    
    // Mots-clés pour identifier la catégorie
    private static final Map<String, List<String>> CATEGORIE_KEYWORDS = new HashMap<>();
    
    static {
        // Initialisation des mots-clés de gravité
        GRAVITE_KEYWORDS.put("critique", Arrays.asList(
            "urgence", "urgent", "danger", "dangereux", "accident", "blessé",
            "catastrophe", "immédiat", "critique", "grave", "sécurité"));
            
        GRAVITE_KEYWORDS.put("élevée", Arrays.asList(
            "important", "sérieux", "problème", "majeur", "défaillance",
            "panne", "frustré", "inquiet", "difficile", "inacceptable"));
            
        GRAVITE_KEYWORDS.put("moyenne", Arrays.asList(
            "modéré", "gêne", "dérangement", "insatisfait", "amélioration",
            "déçu", "mécontentement", "erreur", "attente"));
            
        GRAVITE_KEYWORDS.put("basse", Arrays.asList(
            "mineur", "suggestion", "fonctionnel", "satisfaisant", "petit",
            "léger", "avis", "conseil", "recommandation"));
            
        // Initialisation des mots-clés de catégorie
        CATEGORIE_KEYWORDS.put("transport", Arrays.asList(
            "bus", "métro", "train", "transport", "voiture", "trajet", "voyage",
            "retard", "chauffeur", "conducteur", "arrêt", "station"));
            
        CATEGORIE_KEYWORDS.put("voirie", Arrays.asList(
            "rue", "route", "trottoir", "nid-de-poule", "signalisation", "feu",
            "passage", "piéton", "circulation", "embouteillage", "stationnement"));
            
        CATEGORIE_KEYWORDS.put("propreté", Arrays.asList(
            "déchets", "poubelle", "ordure", "saleté", "propre", "nettoyage",
            "encombrants", "recyclage", "tri", "pollution"));
            
        CATEGORIE_KEYWORDS.put("sécurité", Arrays.asList(
            "danger", "sécurité", "agression", "vol", "police", "surveillance",
            "caméra", "éclairage", "nuit", "sombre", "peur"));
            
        CATEGORIE_KEYWORDS.put("technique", Arrays.asList(
            "application", "site", "web", "erreur", "bug", "technique", "informatique",
            "connexion", "compte", "profil", "mot de passe", "login"));
    }
    
    /**
     * Analyse une réclamation pour déterminer sa gravité et sa catégorie
     * @param reclamation La réclamation à analyser
     */
    public static void analyseReclamation(Reclamation reclamation) {
        String texte = (reclamation.getTitre() + " " + reclamation.getDescription()).toLowerCase();
        
        // Déterminer la gravité
        String gravite = determinerGravite(texte);
        reclamation.setGravite(gravite);
        
        // Déterminer la catégorie
        String categorie = determinerCategorie(texte);
        reclamation.setCategorie(categorie);
        
        // Calculer la priorité (1-5) en fonction de la gravité
        int priorite = calculerPriorite(gravite);
        reclamation.setPriorite(priorite);
    }
    
    private static String determinerGravite(String texte) {
        Map<String, Integer> scores = new HashMap<>();
        
        // Initialiser les scores à 0
        for (String gravite : GRAVITE_KEYWORDS.keySet()) {
            scores.put(gravite, 0);
        }
        
        // Calculer les scores basés sur les mots-clés
        for (Map.Entry<String, List<String>> entry : GRAVITE_KEYWORDS.entrySet()) {
            String gravite = entry.getKey();
            List<String> keywords = entry.getValue();
            
            for (String keyword : keywords) {
                if (texte.contains(keyword)) {
                    scores.put(gravite, scores.get(gravite) + 1);
                }
            }
        }
        
        // Trouver la gravité avec le score le plus élevé
        String graviteMax = "moyenne"; // valeur par défaut
        int scoreMax = 0;
        
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > scoreMax) {
                scoreMax = entry.getValue();
                graviteMax = entry.getKey();
            }
        }
        
        return graviteMax;
    }
    
    private static String determinerCategorie(String texte) {
        Map<String, Integer> scores = new HashMap<>();
        
        // Initialiser les scores à 0
        for (String categorie : CATEGORIE_KEYWORDS.keySet()) {
            scores.put(categorie, 0);
        }
        
        // Calculer les scores basés sur les mots-clés
        for (Map.Entry<String, List<String>> entry : CATEGORIE_KEYWORDS.entrySet()) {
            String categorie = entry.getKey();
            List<String> keywords = entry.getValue();
            
            for (String keyword : keywords) {
                if (texte.contains(keyword)) {
                    scores.put(categorie, scores.get(categorie) + 1);
                }
            }
        }
        
        // Trouver la catégorie avec le score le plus élevé
        String categorieMax = "autre"; // valeur par défaut
        int scoreMax = 0;
        
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > scoreMax) {
                scoreMax = entry.getValue();
                categorieMax = entry.getKey();
            }
        }
        
        return categorieMax;
    }
    
    private static int calculerPriorite(String gravite) {
        switch (gravite) {
            case "critique": return 5;
            case "élevée": return 4;
            case "moyenne": return 3;
            case "basse": return 2;
            default: return 1;
        }
    }
} 