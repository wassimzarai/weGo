package gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import utils.ConfigService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Contrôleur pour la configuration des services de notification (email et SMS)
 */
public class ConfigController {
    
    private static final Logger LOGGER = Logger.getLogger(ConfigController.class.getName());
    
    @FXML
    private TextField emailUsernameField;
    
    @FXML
    private PasswordField emailPasswordField;
    
    @FXML
    private TextField emailFromField;
    
    @FXML
    private TextField twilioAccountSidField;
    
    @FXML
    private PasswordField twilioAuthTokenField;
    
    @FXML
    private TextField twilioPhoneNumberField;
    
    @FXML
    private CheckBox showGmailInstructionsCheck;
    
    @FXML
    private TextArea gmailInstructionsArea;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    public void initialize() {
        loadCurrentConfig();
        setupListeners();
        
        // Texte d'instructions pour la configuration Gmail
        String gmailInstructions = "Comment obtenir un mot de passe d'application pour Gmail:\n\n"
                + "1. Activez l'authentification à 2 facteurs sur votre compte Google\n"
                + "   (Compte Google > Sécurité > Validation en deux étapes)\n\n"
                + "2. Créez un mot de passe d'application:\n"
                + "   - Allez dans Compte Google > Sécurité > Mots de passe d'application\n"
                + "   - Sélectionnez \"Application\" > \"Autre (nom personnalisé)\"\n"
                + "   - Entrez \"WeGo\" comme nom\n"
                + "   - Cliquez sur \"Générer\"\n\n"
                + "3. Copiez le mot de passe généré (16 caractères sans espaces)\n"
                + "   et collez-le dans le champ \"Mot de passe App\" ci-dessus.\n\n"
                + "IMPORTANT: Un compte Gmail standard est requis. Pour les comptes Gmail professionnels\n"
                + "ou G Suite, contactez votre administrateur pour autoriser les applications moins sécurisées\n"
                + "ou utiliser des mots de passe d'application.\n\n"
                + "Note: Gmail bloquera les connexions si vous n'utilisez pas un mot de passe d'application\n"
                + "avec l'authentification à 2 facteurs activée.";
        
        if (gmailInstructionsArea != null) {
            gmailInstructionsArea.setText(gmailInstructions);
            gmailInstructionsArea.setWrapText(true);
            gmailInstructionsArea.setVisible(false);
        }
    }
    
    private void setupListeners() {
        if (showGmailInstructionsCheck != null && gmailInstructionsArea != null) {
            showGmailInstructionsCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                gmailInstructionsArea.setVisible(newVal);
            });
        }
    }
    
    private void loadCurrentConfig() {
        // Charger les valeurs actuelles depuis le service de configuration
        emailUsernameField.setText(ConfigService.getProperty(ConfigService.EMAIL_USERNAME));
        emailPasswordField.setText(ConfigService.getProperty(ConfigService.EMAIL_PASSWORD));
        emailFromField.setText(ConfigService.getProperty(ConfigService.EMAIL_FROM));
        
        twilioAccountSidField.setText(ConfigService.getProperty(ConfigService.TWILIO_ACCOUNT_SID));
        twilioAuthTokenField.setText(ConfigService.getProperty(ConfigService.TWILIO_AUTH_TOKEN));
        twilioPhoneNumberField.setText(ConfigService.getProperty(ConfigService.TWILIO_PHONE_NUMBER));
        
        // Avertir si les valeurs sont encore les valeurs par défaut
        if (ConfigService.containsDefaultValues()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Configuration requise");
            alert.setHeaderText("Configuration des notifications non définie");
            alert.setContentText("Veuillez configurer les paramètres d'email et de SMS pour recevoir des notifications.");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleSave() {
        // Valider les champs obligatoires
        if (emailUsernameField.getText().trim().isEmpty() || 
            emailPasswordField.getText().trim().isEmpty() ||
            emailFromField.getText().trim().isEmpty()) {
            
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", 
                     "Tous les champs email sont requis pour l'envoi des notifications par email.");
            return;
        }
        
        // Sauvegarder les modifications
        ConfigService.setProperty(ConfigService.EMAIL_USERNAME, emailUsernameField.getText().trim());
        ConfigService.setProperty(ConfigService.EMAIL_PASSWORD, emailPasswordField.getText());
        ConfigService.setProperty(ConfigService.EMAIL_FROM, emailFromField.getText().trim());
        
        ConfigService.setProperty(ConfigService.TWILIO_ACCOUNT_SID, twilioAccountSidField.getText().trim());
        ConfigService.setProperty(ConfigService.TWILIO_AUTH_TOKEN, twilioAuthTokenField.getText());
        ConfigService.setProperty(ConfigService.TWILIO_PHONE_NUMBER, twilioPhoneNumberField.getText().trim());
        
        LOGGER.log(Level.INFO, "Configuration des notifications mise à jour");
        
        showAlert(Alert.AlertType.INFORMATION, "Configuration enregistrée", 
                 "Vos paramètres de notification ont été enregistrés avec succès.");
        
        // Fermer la fenêtre
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleCancel() {
        // Fermer sans sauvegarder
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleTestEmail() {
        // Récupérer les identifiants des champs (non encore sauvegardés)
        String username = emailUsernameField.getText().trim();
        String password = emailPasswordField.getText();
        String from = emailFromField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty() || from.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de test", 
                     "Tous les champs email sont requis pour tester l'envoi.");
            return;
        }
        
        // Sauvegarder temporairement pour le test
        String oldUsername = ConfigService.getProperty(ConfigService.EMAIL_USERNAME);
        String oldPassword = ConfigService.getProperty(ConfigService.EMAIL_PASSWORD);
        String oldFrom = ConfigService.getProperty(ConfigService.EMAIL_FROM);
        
        ConfigService.setProperty(ConfigService.EMAIL_USERNAME, username);
        ConfigService.setProperty(ConfigService.EMAIL_PASSWORD, password);
        ConfigService.setProperty(ConfigService.EMAIL_FROM, from);
        
        try {
            // Test d'envoi simple à l'adresse indiquée
            boolean success = sendTestEmail(username);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Test réussi", 
                         "L'email de test a été envoyé avec succès à " + username);
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec du test", 
                         "L'envoi de l'email de test a échoué. Vérifiez vos paramètres et réessayez.");
            }
        } finally {
            // Restaurer les anciennes valeurs si l'utilisateur n'a pas encore sauvegardé
            ConfigService.setProperty(ConfigService.EMAIL_USERNAME, oldUsername);
            ConfigService.setProperty(ConfigService.EMAIL_PASSWORD, oldPassword);
            ConfigService.setProperty(ConfigService.EMAIL_FROM, oldFrom);
        }
    }
    
    @FXML
    private void handleTestSMS() {
        // Récupérer les identifiants des champs (non encore sauvegardés)
        String accountSid = twilioAccountSidField.getText().trim();
        String authToken = twilioAuthTokenField.getText();
        String phoneNumber = twilioPhoneNumberField.getText().trim();
        
        if (accountSid.isEmpty() || authToken.isEmpty() || phoneNumber.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de test", 
                     "Tous les champs Twilio sont requis pour tester l'envoi SMS.");
            return;
        }
        
        // Demander le numéro de téléphone de destination
        TextInputDialog dialog = new TextInputDialog("+216");
        dialog.setTitle("Test SMS");
        dialog.setHeaderText("Entrez votre numéro de téléphone pour recevoir un SMS de test");
        dialog.setContentText("Numéro (format international, ex: +21612345678):");
        
        dialog.showAndWait().ifPresent(destinationNumber -> {
            if (destinationNumber.trim().isEmpty() || !destinationNumber.trim().startsWith("+")) {
                showAlert(Alert.AlertType.ERROR, "Erreur de format", 
                         "Le numéro doit être au format international (commençant par +)");
                return;
            }
            
            // Sauvegarder temporairement pour le test
            String oldAccountSid = ConfigService.getProperty(ConfigService.TWILIO_ACCOUNT_SID);
            String oldAuthToken = ConfigService.getProperty(ConfigService.TWILIO_AUTH_TOKEN);
            String oldPhoneNumber = ConfigService.getProperty(ConfigService.TWILIO_PHONE_NUMBER);
            
            ConfigService.setProperty(ConfigService.TWILIO_ACCOUNT_SID, accountSid);
            ConfigService.setProperty(ConfigService.TWILIO_AUTH_TOKEN, authToken);
            ConfigService.setProperty(ConfigService.TWILIO_PHONE_NUMBER, phoneNumber);
            
            try {
                // Test d'envoi simple au numéro indiqué
                boolean success = sendTestSMS(destinationNumber.trim());
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Test réussi", 
                             "Le SMS de test a été envoyé avec succès à " + destinationNumber);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Échec du test", 
                             "L'envoi du SMS de test a échoué. Vérifiez vos paramètres et réessayez.");
                }
            } finally {
                // Restaurer les anciennes valeurs si l'utilisateur n'a pas encore sauvegardé
                ConfigService.setProperty(ConfigService.TWILIO_ACCOUNT_SID, oldAccountSid);
                ConfigService.setProperty(ConfigService.TWILIO_AUTH_TOKEN, oldAuthToken);
                ConfigService.setProperty(ConfigService.TWILIO_PHONE_NUMBER, oldPhoneNumber);
            }
        });
    }
    
    private boolean sendTestEmail(String destination) {
        try {
            // Configurer les propriétés pour le serveur SMTP de Gmail avec SSL
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.debug", "true");
            
            // Créer une session avec authentification
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        ConfigService.getProperty(ConfigService.EMAIL_USERNAME),
                        ConfigService.getProperty(ConfigService.EMAIL_PASSWORD)
                    );
                }
            });
            
            // Créer et envoyer le message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(ConfigService.getProperty(ConfigService.EMAIL_FROM)));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destination));
            message.setSubject("Test de configuration WeGo");
            message.setText("Ceci est un email de test de l'application WeGo.\n\n" +
                           "Si vous recevez cet email, votre configuration email est correcte.");
            
            Transport.send(message);
            
            LOGGER.log(Level.INFO, "Email de test envoyé avec succès à {0}", destination);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du test d'email: " + e.getMessage(), e);
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean sendTestSMS(String destinationNumber) {
        try {
            // Initialiser Twilio et envoyer un SMS de test
            com.twilio.Twilio.init(
                ConfigService.getProperty(ConfigService.TWILIO_ACCOUNT_SID),
                ConfigService.getProperty(ConfigService.TWILIO_AUTH_TOKEN)
            );
            
            com.twilio.rest.api.v2010.account.Message message = 
                com.twilio.rest.api.v2010.account.Message.creator(
                    new com.twilio.type.PhoneNumber(destinationNumber),
                    new com.twilio.type.PhoneNumber(ConfigService.getProperty(ConfigService.TWILIO_PHONE_NUMBER)),
                    "Ceci est un SMS de test de l'application WeGo."
                ).create();
            
            return message.getSid() != null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du test SMS: " + e.getMessage(), e);
            return false;
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 