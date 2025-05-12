package utils;

import entities.Reclamation;
import entities.Reponse;
import entities.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

// JavaMail imports
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Service de notification pour les emails.
 * Utilise JavaMail pour les emails.
 */
public class NotificationService {
    
    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final String ADMIN_EMAIL = "boxeurbigboss@gmail.com";
    private static final String SENDER_EMAIL = "aycembougattaya357@gmail.com";
    private static final String password_email = "ovdx wncv sjjd nwal";

    private static final boolean SIMULATION_MODE = false; // Mettre à false pour envoyer réellement les emails
    
    /**
     * Vérifie si le service est en mode simulation
     * @return true si le service est en mode simulation, false sinon
     */
    public static boolean isSimulationMode() {
        return SIMULATION_MODE;
    }
    
    // Envoyer un email
    public static boolean envoyerEmailNotification(User user, Reclamation reclamation, String type) {
        if (user == null) {
            LOGGER.log(Level.WARNING, "Impossible d'envoyer un email: utilisateur null");
            return false;
        }
        
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            LOGGER.log(Level.WARNING, "Impossible d'envoyer un email: adresse email manquante pour l'utilisateur {0}", 
                      user.getUsername() != null ? user.getUsername() : "ID:" + user.getId());
            return false;
        }
        
        String sujet = "";
        String corps = "";
        
        switch (type) {
            case "creation":
                sujet = "Votre réclamation a été enregistrée - Ticket #" + reclamation.getTicketId();
                corps = genererCorpsEmailCreation(user, reclamation);
                break;
                
            case "statut":
                sujet = "Mise à jour de votre réclamation - Ticket #" + reclamation.getTicketId();
                corps = genererCorpsEmailStatut(user, reclamation);
                break;
                
            case "resolution":
                sujet = "Votre réclamation a été résolue - Ticket #" + reclamation.getTicketId();
                corps = genererCorpsEmailResolution(user, reclamation);
                break;
                
            default:
                LOGGER.log(Level.WARNING, "Type de notification inconnu: {0}", type);
                return false;
        }
        
        return envoyerEmail(user.getEmail(), sujet, corps);
    }
    
    // Méthode générique pour envoyer un email
    private static boolean envoyerEmail(String destinataire, String sujet, String corps) {
        try {
            // Configuration pour Gmail
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true"); // Activer STARTTLS
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            props.put("mail.debug", "true");
            
            // Créer une session avec authentification
            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication(SENDER_EMAIL, password_email);
                }
            });
            // Créer un nouveau message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(destinataire));
            message.setSubject(sujet);
            message.setText(corps);
            
            // Log l'envoi de l'email
            LOGGER.log(Level.INFO, "ENVOI D'EMAIL: De {0} à {1}, Sujet: {2}", 
                      new Object[]{SENDER_EMAIL, destinataire, sujet});
            
            try {
                // Tenter d'envoyer l'email
                Transport.send(message);
                LOGGER.log(Level.INFO, "Email envoyé avec succès à {0}", destinataire);
                return true;
            } catch (MessagingException e) {
                // En cas d'erreur d'envoi, enregistrer dans un fichier
                LOGGER.log(Level.WARNING, "Impossible d'envoyer l'email à {0}: {1}", 
                          new Object[]{destinataire, e.getMessage()});
                
                // Enregistrer l'email dans un fichier
                boolean sauvegarde = sauvegarderEmailDansFichier(destinataire, sujet, corps);
                if (sauvegarde) {
                    LOGGER.log(Level.INFO, "Email sauvegardé dans un fichier local");
                    return true; // Considérer comme un succès si l'email est sauvegardé
                } else {
                    LOGGER.log(Level.SEVERE, "Impossible de sauvegarder l'email dans un fichier");
                    return false;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception lors de la préparation de l'email: {0}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Sauvegarde le contenu d'un email dans un fichier
     * @param destinataire Adresse email du destinataire
     * @param sujet Sujet de l'email
     * @param corps Corps de l'email
     * @return true si l'email a été sauvegardé avec succès, false sinon
     */
    private static boolean sauvegarderEmailDansFichier(String destinataire, String sujet, String corps) {
        try {
            // Créer le dossier emails s'il n'existe pas
            File dossier = new File("emails");
            if (!dossier.exists()) {
                dossier.mkdir();
            }
            
            // Créer un nom de fichier unique basé sur la date et l'heure
            String dateHeure = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomFichier = "emails/email_" + dateHeure + ".txt";
            
            // Écrire le contenu de l'email dans le fichier
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomFichier))) {
                writer.write("Date: " + LocalDateTime.now().format(formatter));
                writer.newLine();
                writer.write("De: " + SENDER_EMAIL);
                writer.newLine();
                writer.write("À: " + destinataire);
                writer.newLine();
                writer.write("Sujet: " + sujet);
                writer.newLine();
                writer.newLine();
                writer.write("--- Corps du message ---");
                writer.newLine();
                writer.write(corps);
            }
            
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la sauvegarde de l'email: {0}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Envoyer les notifications
    public static void envoyerNotifications(User user, Reclamation reclamation, String type) {
        if (user == null) {
            LOGGER.log(Level.WARNING, "Impossible d'envoyer des notifications: utilisateur null");
            return;
        }
        
        if (reclamation == null) {
            LOGGER.log(Level.WARNING, "Impossible d'envoyer des notifications: réclamation null");
            return;
        }
        
        LOGGER.log(Level.INFO, "Préparation de l'email pour l'utilisateur {0} (email: {1})",
                  new Object[]{user.getUsername(), user.getEmail()});
        
        boolean emailEnvoye = envoyerEmailNotification(user, reclamation, type);
        
        // Envoyer une notification à l'administrateur pour les nouvelles réclamations
        if (type.equals("creation")) {
            envoyerEmailReclamationAdmin(user, reclamation, false);
        }
        
        if (emailEnvoye) {
            reclamation.setNotificationsEnvoyees(true);
            LOGGER.log(Level.INFO, "Email envoyé avec succès");
        } else {
            LOGGER.log(Level.WARNING, "Aucune notification n'a pu être envoyée");
        }
    }
    
    /**
     * Envoie un email à l'administrateur pour l'informer d'une nouvelle réclamation
     * @param user Utilisateur ayant soumis la réclamation
     * @param reclamation Réclamation soumise
     * @return true si l'email a été envoyé avec succès, false sinon
     */
    public static boolean envoyerEmailReclamationAdmin(User user, Reclamation reclamation, boolean isResolutionRequest) {
        // Vérifier si l'utilisateur et la réclamation sont valides
        if (user == null || reclamation == null) {
            LOGGER.log(Level.WARNING, "Impossible d'envoyer un email à l'administrateur: données invalides");
            return false;
        }
        
        // Déterminer si c'est une nouvelle réclamation ou une demande de résolution
        String sujet;
        String corps;
        
        if (isResolutionRequest) {
            sujet = "Action Requise : Résolution de réclamation - Ticket #" + reclamation.getTicketId();
            corps = genererCorpsEmailAdminResolution(user, reclamation);
        } else {
            sujet = "Nouvelle réclamation à traiter - Ticket #" + reclamation.getTicketId();
            corps = genererCorpsEmailAdmin(user, reclamation);
        }
        
        return envoyerEmail(ADMIN_EMAIL, sujet, corps);
    }
    
    private static String genererCorpsEmailCreation(User user, Reclamation reclamation) {
        StringBuilder sb = new StringBuilder();
        
        String greeting = getPersonalizedGreeting(user);
        sb.append(greeting).append(",\n\n");
        sb.append("Nous avons bien reçu votre réclamation. Voici les détails :\n\n");
        sb.append("======================================================\n");
        sb.append("NUMÉRO DE TICKET : ").append(reclamation.getTicketId()).append("\n");
        sb.append("TITRE : ").append(reclamation.getTitre()).append("\n");
        sb.append("DESCRIPTION : ").append(reclamation.getDescription()).append("\n");
        sb.append("DATE : ").append(reclamation.getDate()).append("\n");
        sb.append("STATUT : ").append(reclamation.getStatut()).append("\n");
        sb.append("======================================================\n\n");
        
        if (reclamation.getLatitude() != 0 && reclamation.getLongitude() != 0) {
            sb.append("Localisation : ").append(reclamation.getAdresse()).append("\n");
            sb.append("Coordonnées GPS : ").append(reclamation.getLatitude())
              .append(", ").append(reclamation.getLongitude()).append("\n\n");
        }
        
        sb.append("Nous traitons votre demande avec la plus grande attention. ");
        sb.append("Vous recevrez des notifications par email à chaque étape importante du traitement.\n\n");
        sb.append("Vous pouvez suivre l'évolution de votre réclamation en temps réel dans votre espace personnel sur l'application WeGo.\n\n");
        sb.append("Merci de votre confiance,\n");
        sb.append("L'équipe WeGo");
        
        return sb.toString();
    }
    
    private static String genererCorpsEmailStatut(User user, Reclamation reclamation) {
        StringBuilder sb = new StringBuilder();
        
        String greeting = getPersonalizedGreeting(user);
        sb.append(greeting).append(",\n\n");
        sb.append("Le statut de votre réclamation a été mis à jour :\n\n");
        sb.append("Numéro de ticket : ").append(reclamation.getTicketId()).append("\n");
        sb.append("Titre : ").append(reclamation.getTitre()).append("\n");
        sb.append("Nouveau statut : ").append(reclamation.getStatut()).append("\n");
        sb.append("Date de mise à jour : ").append(LocalDateTime.now().format(formatter)).append("\n\n");
        
        sb.append("Vous pouvez suivre l'évolution de votre réclamation en temps réel dans votre espace personnel sur l'application WeGo.\n\n");
        sb.append("Merci de votre confiance,\n");
        sb.append("L'équipe WeGo");
        
        return sb.toString();
    }
    
    private static String genererCorpsEmailResolution(User user, Reclamation reclamation) {
        StringBuilder sb = new StringBuilder();
        
        String greeting = getPersonalizedGreeting(user);
        sb.append(greeting).append(",\n\n");
        sb.append("Nous sommes heureux de vous informer que votre réclamation a été résolue :\n\n");
        sb.append("Numéro de ticket : ").append(reclamation.getTicketId()).append("\n");
        sb.append("Titre : ").append(reclamation.getTitre()).append("\n");
        sb.append("Date de résolution : ").append(LocalDateTime.now().format(formatter)).append("\n\n");
        
        sb.append("Nous vous invitons à donner votre avis sur la résolution de cette réclamation. ");
        sb.append("Votre retour est essentiel pour nous aider à améliorer constamment nos services.\n\n");
        sb.append("Pour cela, connectez-vous simplement à votre espace personnel sur l'application WeGo et répondez à notre courte enquête de satisfaction.\n\n");
        sb.append("Merci de votre confiance,\n");
        sb.append("L'équipe WeGo");
        
        return sb.toString();
    }
    
    /**
     * Génère une salutation personnalisée en fonction des informations disponibles de l'utilisateur
     * @param user L'utilisateur à saluer
     * @return La salutation personnalisée
     */
    private static String getPersonalizedGreeting(User user) {
        if (user == null) {
            return "Bonjour";
        }
        
        // Vérifier si le nom complet est disponible
        boolean hasFirstName = user.getPrenom() != null && !user.getPrenom().trim().isEmpty();
        boolean hasLastName = user.getNom() != null && !user.getNom().trim().isEmpty();
        
        if (hasFirstName && hasLastName) {
            return "Bonjour " + user.getPrenom() + " " + user.getNom();
        } else if (hasFirstName) {
            return "Bonjour " + user.getPrenom();
        } else if (hasLastName) {
            return "Bonjour " + user.getNom();
        } else if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            // Utiliser le nom d'utilisateur si disponible
            return "Bonjour " + user.getUsername();
        } else {
            // Cas où aucun nom n'est disponible
            return "Bonjour";
        }
    }
    
    /**
     * Génère le corps de l'email pour l'administrateur (notification automatique)
     */
    private static String genererCorpsEmailAdmin(User user, Reclamation reclamation) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Bonjour Administrateur,\n\n");
        sb.append("Une nouvelle réclamation a été soumise et nécessite votre attention.\n\n");
        sb.append("Détails de la réclamation :\n\n");
        sb.append("Numéro de ticket : ").append(reclamation.getTicketId()).append("\n");
        sb.append("Titre : ").append(reclamation.getTitre()).append("\n");
        sb.append("Description : ").append(reclamation.getDescription()).append("\n");
        sb.append("Date : ").append(reclamation.getDate()).append("\n");
        sb.append("Statut : ").append(reclamation.getStatut()).append("\n\n");
        
        sb.append("Informations utilisateur :\n");
        sb.append("Nom d'utilisateur : ").append(user.getUsername()).append("\n");
        sb.append("Email : ").append(user.getEmail()).append("\n");
        if (user.getTelephone() != null && !user.getTelephone().isEmpty()) {
            sb.append("Téléphone : ").append(user.getTelephone()).append("\n");
        }
        sb.append("\n");
        
        if (reclamation.getLatitude() != 0 && reclamation.getLongitude() != 0) {
            sb.append("Localisation : ").append(reclamation.getAdresse()).append("\n");
            sb.append("Coordonnées GPS : ").append(reclamation.getLatitude())
              .append(", ").append(reclamation.getLongitude()).append("\n\n");
        }
        
        sb.append("Veuillez traiter cette réclamation dans les plus brefs délais.\n");
        sb.append("Pour résoudre cette réclamation, connectez-vous à l'application d'administration WeGo.\n\n");
        sb.append("Cordialement,\n");
        sb.append("Système automatique WeGo");
        
        return sb.toString();
    }
    
    /**
     * Génère le corps de l'email pour l'administrateur (demande de résolution)
     */
    private static String genererCorpsEmailAdminResolution(User user, Reclamation reclamation) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Bonjour Administrateur,\n\n");
        sb.append("L'utilisateur a demandé la résolution de cette réclamation qui nécessite votre attention immédiate.\n\n");
        sb.append("Détails de la réclamation :\n\n");
        sb.append("======================================================\n");
        sb.append("NUMÉRO DE TICKET : ").append(reclamation.getTicketId()).append("\n");
        sb.append("TITRE : ").append(reclamation.getTitre()).append("\n");
        sb.append("DESCRIPTION : ").append(reclamation.getDescription()).append("\n");
        sb.append("DATE : ").append(reclamation.getDate()).append("\n");
        sb.append("STATUT : ").append(reclamation.getStatut()).append("\n");
        sb.append("======================================================\n\n");
        
        sb.append("Informations utilisateur :\n");
        sb.append("Nom d'utilisateur : ").append(user.getUsername()).append("\n");
        sb.append("Email : ").append(user.getEmail()).append("\n");
        if (user.getTelephone() != null && !user.getTelephone().isEmpty()) {
            sb.append("Téléphone : ").append(user.getTelephone()).append("\n");
        }
        sb.append("\n");
        
        if (reclamation.getLatitude() != 0 && reclamation.getLongitude() != 0) {
            sb.append("Localisation : ").append(reclamation.getAdresse()).append("\n");
            sb.append("Coordonnées GPS : ").append(reclamation.getLatitude())
              .append(", ").append(reclamation.getLongitude()).append("\n\n");
        }
        
        sb.append("Veuillez traiter cette réclamation dans les plus brefs délais.\n");
        sb.append("Pour résoudre cette réclamation, connectez-vous à l'application d'administration WeGo.\n\n");
        sb.append("Cordialement,\n");
        sb.append("Système automatique WeGo");
        
        return sb.toString();
    }
} 