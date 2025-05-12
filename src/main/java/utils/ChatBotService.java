package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service de chatbot simple pour l'assistance utilisateur.
 * Dans une application réelle, utiliserait des services d'IA comme 
 * Dialogflow, IBM Watson, Azure Bot Service, ou OpenAI API.
 */
public class ChatBotService {
    
    private static final Map<Pattern, String> FAQ_RESPONSES = new HashMap<>();
    private static final List<String> GREETING_KEYWORDS = new ArrayList<>();
    private static final List<String> FAREWELL_KEYWORDS = new ArrayList<>();
    
    static {
        // Questions fréquentes et réponses
        FAQ_RESPONSES.put(
            Pattern.compile("(?i).*(?:comment|quoi|c'est quoi).*(?:réclamation|plainte).*"),
            "Une réclamation est un signalement que vous pouvez faire concernant un problème rencontré " +
            "avec nos services. Vous pouvez soumettre une réclamation en cliquant sur 'Mes Réclamations' " +
            "puis sur le bouton '+' pour créer une nouvelle réclamation."
        );
        
        FAQ_RESPONSES.put(
            Pattern.compile("(?i).*(?:comment|où).*(?:suivre|statut|état).*réclamation.*"),
            "Vous pouvez suivre l'état de vos réclamations dans la section 'Mes Réclamations'. " +
            "Chaque réclamation affiche son statut actuel (ouvert, en cours, résolu ou rejeté) " +
            "et vous recevrez des notifications par email et SMS à chaque changement important."
        );
        
        FAQ_RESPONSES.put(
            Pattern.compile("(?i).*(?:combien de temps|délai|attendre).*(?:résolution|traitement|réponse).*"),
            "Le délai de traitement dépend de la nature et de la complexité de votre réclamation. " +
            "En général, nous nous efforçons de résoudre les problèmes simples en 48 heures et " +
            "les problèmes plus complexes en moins de 7 jours. Vous pouvez suivre l'avancement " +
            "en temps réel dans votre espace personnel."
        );
        
        FAQ_RESPONSES.put(
            Pattern.compile("(?i).*(?:comment|puis-je).*(?:ajouter|joindre|attacher).*(?:photo|image|document|fichier).*"),
            "Pour joindre une photo ou un document à votre réclamation, cliquez sur le bouton 'Joindre un fichier' " +
            "lors de la création ou de la modification d'une réclamation. Vous pouvez ajouter des photos, " +
            "des documents PDF et d'autres fichiers pour faciliter la compréhension de votre problème."
        );
        
        FAQ_RESPONSES.put(
            Pattern.compile("(?i).*(?:comment|où).*(?:donner|laisser|écrire).*(?:avis|commentaire|feedback).*"),
            "Vous pouvez laisser un avis en accédant à la section 'Mes Avis' depuis la page d'accueil. " +
            "Cliquez sur le bouton '+' pour ajouter un nouvel avis. Vous pourrez attribuer une note et " +
            "laisser un commentaire détaillé sur votre expérience."
        );
        
        FAQ_RESPONSES.put(
            Pattern.compile("(?i).*(?:contact|joindre|parler).*(?:service client|support|conseiller|humain).*"),
            "Si vous souhaitez parler à un conseiller humain, vous pouvez nous contacter par téléphone " +
            "au 03 123 456 789 (du lundi au vendredi, de 9h à 18h) ou par email à support@wego.com. " +
            "Vous pouvez également demander un rappel en cliquant sur 'Demander un rappel' dans " +
            "les détails de votre réclamation."
        );
        
        FAQ_RESPONSES.put(
            Pattern.compile("(?i).*(?:récupérer|changer|oublié).*(?:mot de passe).*"),
            "Pour réinitialiser votre mot de passe, cliquez sur 'Mot de passe oublié' sur l'écran de connexion. " +
            "Vous recevrez un email avec un lien de réinitialisation à l'adresse associée à votre compte."
        );
        
        FAQ_RESPONSES.put(
            Pattern.compile("(?i).*(?:modifier|changer|mettre à jour).*(?:profil|informations personnelles|coordonnées).*"),
            "Pour modifier vos informations personnelles, accédez à votre profil en cliquant sur votre photo ou " +
            "votre nom en haut à gauche de l'écran. Vous pourrez alors modifier vos coordonnées, " +
            "votre photo de profil et vos préférences de notification."
        );
        
        // Mots-clés de salutation
        GREETING_KEYWORDS.add("bonjour");
        GREETING_KEYWORDS.add("salut");
        GREETING_KEYWORDS.add("hello");
        GREETING_KEYWORDS.add("hey");
        GREETING_KEYWORDS.add("coucou");
        GREETING_KEYWORDS.add("bonsoir");
        
        // Mots-clés de fin de conversation
        FAREWELL_KEYWORDS.add("merci");
        FAREWELL_KEYWORDS.add("au revoir");
        FAREWELL_KEYWORDS.add("bye");
        FAREWELL_KEYWORDS.add("à bientôt");
        FAREWELL_KEYWORDS.add("bonne journée");
    }
    
    /**
     * Génère une réponse automatique basée sur la question de l'utilisateur
     * @param question La question ou message de l'utilisateur
     * @return Une réponse automatique
     */
    public static String getResponse(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "Je suis là pour vous aider. N'hésitez pas à me poser une question concernant vos réclamations ou avis.";
        }
        
        // Convertir en minuscules pour la comparaison
        String lowerQuestion = question.toLowerCase().trim();
        
        // Vérifier si c'est une salutation
        for (String greeting : GREETING_KEYWORDS) {
            if (lowerQuestion.contains(greeting)) {
                return "Bonjour ! Comment puis-je vous aider aujourd'hui ? Je peux vous renseigner sur vos réclamations, " +
                       "vos avis, ou vous aider à naviguer dans l'application.";
            }
        }
        
        // Vérifier si c'est un au revoir
        for (String farewell : FAREWELL_KEYWORDS) {
            if (lowerQuestion.contains(farewell)) {
                return "Merci d'avoir utilisé notre assistant. N'hésitez pas à revenir si vous avez d'autres questions. " +
                       "Bonne journée !";
            }
        }
        
        // Parcourir les questions fréquentes pour trouver une correspondance
        for (Map.Entry<Pattern, String> entry : FAQ_RESPONSES.entrySet()) {
            if (entry.getKey().matcher(lowerQuestion).matches()) {
                return entry.getValue();
            }
        }
        
        // Réponse par défaut si aucune correspondance n'est trouvée
        return "Je ne suis pas sûr de comprendre votre question. Voici quelques sujets sur lesquels je peux vous aider :\n" +
               "- Comment soumettre une réclamation\n" +
               "- Comment suivre l'état d'une réclamation\n" +
               "- Comment ajouter des photos à une réclamation\n" +
               "- Comment laisser un avis\n" +
               "- Comment contacter le service client";
    }
    
    /**
     * Suggère des actions en fonction de l'entrée utilisateur
     * @param input L'entrée utilisateur
     * @return Une liste de suggestions d'actions
     */
    public static List<String> getSuggestions(String input) {
        List<String> suggestions = new ArrayList<>();
        
        String lowerInput = input.toLowerCase().trim();
        
        if (lowerInput.contains("réclamation")) {
            suggestions.add("Créer une réclamation");
            suggestions.add("Voir mes réclamations");
        }
        
        if (lowerInput.contains("avis") || lowerInput.contains("commentaire")) {
            suggestions.add("Ajouter un avis");
            suggestions.add("Voir les avis");
        }
        
        if (lowerInput.contains("problème") || lowerInput.contains("bug") || lowerInput.contains("erreur")) {
            suggestions.add("Signaler un problème technique");
        }
        
        if (lowerInput.contains("contact") || lowerInput.contains("humain") || lowerInput.contains("parler")) {
            suggestions.add("Contacter le service client");
        }
        
        // Ajouter des suggestions par défaut si aucune suggestion spécifique n'est trouvée
        if (suggestions.isEmpty()) {
            suggestions.add("Créer une réclamation");
            suggestions.add("Mes réclamations");
            suggestions.add("Laisser un avis");
            suggestions.add("Aide");
        }
        
        return suggestions;
    }
    
    /**
     * Aide à la saisie d'une réclamation en suggérant des informations à inclure
     * @param titreReclamation Le titre de la réclamation
     * @return Conseils pour compléter la réclamation
     */
    public static String getConseilsReclamation(String titreReclamation) {
        StringBuilder conseils = new StringBuilder();
        conseils.append("Conseils pour compléter votre réclamation :\n\n");
        
        // Conseils généraux
        conseils.append("• Soyez précis dans votre description\n");
        conseils.append("• Mentionnez quand l'incident s'est produit (date et heure)\n");
        conseils.append("• Décrivez l'impact que cela a eu pour vous\n");
        conseils.append("• Ajoutez des photos si possible pour illustrer le problème\n");
        
        // Conseils spécifiques selon le type de réclamation
        String titreLower = titreReclamation.toLowerCase();
        
        if (titreLower.contains("retard") || titreLower.contains("bus") || titreLower.contains("transport")) {
            conseils.append("\nPour une réclamation concernant les transports :\n");
            conseils.append("• Indiquez le numéro de ligne et la direction\n");
            conseils.append("• Mentionnez les arrêts de départ et d'arrivée\n");
            conseils.append("• Précisez la durée du retard\n");
        } else if (titreLower.contains("propreté") || titreLower.contains("déchet") || titreLower.contains("poubelle")) {
            conseils.append("\nPour une réclamation concernant la propreté :\n");
            conseils.append("• Décrivez précisément le type de problème (déchets, graffitis, etc.)\n");
            conseils.append("• Utilisez la géolocalisation pour marquer l'emplacement exact\n");
        } else if (titreLower.contains("application") || titreLower.contains("site") || titreLower.contains("bug")) {
            conseils.append("\nPour un problème technique :\n");
            conseils.append("• Décrivez les étapes pour reproduire le problème\n");
            conseils.append("• Mentionnez votre appareil et la version de l'application\n");
            conseils.append("• Faites une capture d'écran si possible\n");
        }
        
        return conseils.toString();
    }
} 