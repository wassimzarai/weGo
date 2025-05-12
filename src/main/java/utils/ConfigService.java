package utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service de configuration pour gérer les paramètres de l'application
 * Permet de stocker et récupérer des informations sensibles comme les identifiants email et SMS
 */
public class ConfigService {
    
    private static final Logger LOGGER = Logger.getLogger(ConfigService.class.getName());
    private static final String CONFIG_FILE = "config.properties";
    private static Properties properties;
    
    // Clés de configuration
    public static final String EMAIL_USERNAME = "email.username";
    public static final String EMAIL_PASSWORD = "email.password";
    public static final String EMAIL_FROM = "email.from";
    public static final String TWILIO_ACCOUNT_SID = "twilio.account.sid";
    public static final String TWILIO_AUTH_TOKEN = "twilio.auth.token";
    public static final String TWILIO_PHONE_NUMBER = "twilio.phone.number";
    
    // Valeurs par défaut
    private static final String DEFAULT_EMAIL_USERNAME = "votre.email@gmail.com";
    private static final String DEFAULT_EMAIL_PASSWORD = "votre_mot_de_passe_app";
    private static final String DEFAULT_EMAIL_FROM = "WeGo Support <votre.email@gmail.com>";
    private static final String DEFAULT_TWILIO_ACCOUNT_SID = "AC00000000000000000000000000000000";
    private static final String DEFAULT_TWILIO_AUTH_TOKEN = "00000000000000000000000000000000";
    private static final String DEFAULT_TWILIO_PHONE_NUMBER = "+15555555555";
    
    static {
        loadProperties();
    }
    
    /**
     * Charge les propriétés depuis le fichier de configuration.
     * Si le fichier n'existe pas, crée un fichier avec des valeurs par défaut.
     */
    private static void loadProperties() {
        properties = new Properties();
        
        try {
            FileInputStream fis = new FileInputStream(CONFIG_FILE);
            properties.load(fis);
            fis.close();
            LOGGER.log(Level.INFO, "Configuration chargée depuis {0}", CONFIG_FILE);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Fichier de configuration non trouvé, création d'un fichier par défaut");
            
            // Définir les valeurs par défaut
            properties.setProperty(EMAIL_USERNAME, DEFAULT_EMAIL_USERNAME);
            properties.setProperty(EMAIL_PASSWORD, DEFAULT_EMAIL_PASSWORD);
            properties.setProperty(EMAIL_FROM, DEFAULT_EMAIL_FROM);
            properties.setProperty(TWILIO_ACCOUNT_SID, DEFAULT_TWILIO_ACCOUNT_SID);
            properties.setProperty(TWILIO_AUTH_TOKEN, DEFAULT_TWILIO_AUTH_TOKEN);
            properties.setProperty(TWILIO_PHONE_NUMBER, DEFAULT_TWILIO_PHONE_NUMBER);
            
            try {
                FileOutputStream fos = new FileOutputStream(CONFIG_FILE);
                properties.store(fos, "Configuration WeGo - Remplacez ces valeurs par vos informations réelles");
                fos.close();
                LOGGER.log(Level.INFO, "Fichier de configuration créé avec succès");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Impossible de créer le fichier de configuration", ex);
            }
        }
    }
    
    /**
     * Récupère une valeur de configuration.
     * @param key Clé de la propriété
     * @return Valeur de la propriété, ou null si non trouvée
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Définit une valeur de configuration.
     * @param key Clé de la propriété
     * @param value Nouvelle valeur
     */
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
        
        try {
            FileOutputStream fos = new FileOutputStream(CONFIG_FILE);
            properties.store(fos, "Configuration WeGo");
            fos.close();
            LOGGER.log(Level.INFO, "Configuration mise à jour: {0}", key);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Impossible de sauvegarder la configuration", e);
        }
    }
    
    /**
     * Vérifie si la configuration contient des valeurs par défaut qui doivent être remplacées
     * @return true si des valeurs par défaut sont présentes, false sinon
     */
    public static boolean containsDefaultValues() {
        return getProperty(EMAIL_USERNAME).equals(DEFAULT_EMAIL_USERNAME) ||
               getProperty(EMAIL_PASSWORD).equals(DEFAULT_EMAIL_PASSWORD) ||
               getProperty(TWILIO_ACCOUNT_SID).equals(DEFAULT_TWILIO_ACCOUNT_SID) ||
               getProperty(TWILIO_AUTH_TOKEN).equals(DEFAULT_TWILIO_AUTH_TOKEN);
    }
    
    /**
     * Recharge les propriétés depuis le fichier.
     * Utile après une modification externe du fichier.
     */
    public static void reloadProperties() {
        loadProperties();
    }
} 