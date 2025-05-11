package utils;

import Entites.Reclamation;
import Entites.Reponse;
import Entites.utilisateur;
import Entites.UtilisateurRole;
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
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Service de notification pour les emails.
 * Utilise JavaMail pour les emails réels ou sauvegarde dans des fichiers si en mode simulation.
 */
public class NotificationService {
    
    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final String ADMIN_EMAIL = "boxeurbigboss@gmail.com";
    private static final String SENDER_EMAIL = "aycembougattaya357@gmail.com";
    private static final String PASSWORD_EMAIL = "ovdx wncv sjjd nwal";

    // Mettre à false pour envoyer réellement les emails
    private static final boolean SIMULATION_MODE = false;
    
    private utilisateur currentUser;
    
    public void setCurrentUser(utilisateur user) {
        this.currentUser = user;
    }
    
    /**
     * Vérifie si le service est en mode simulation
     * @return true si le service est en mode simulation, false sinon
     */
    public static boolean isSimulationMode() {
        return SIMULATION_MODE;
    }
    
    // Envoyer un email
    public static boolean envoyerEmailNotification(utilisateur user, Reclamation reclamation, String type) {
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
            // En mode simulation, simplement enregistrer dans un fichier
            if (SIMULATION_MODE) {
                LOGGER.log(Level.INFO, "Mode simulation activé: sauvegarde de l'email dans un fichier");
                return sauvegarderEmailDansFichier(destinataire, sujet, corps);
            }
            
            // Configuration pour Gmail
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            
            // Créer une session avec authentification
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, PASSWORD_EMAIL);
                }
            });
            
            try {
                // Créer un message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(destinataire));
            message.setSubject(sujet);
            message.setText(corps);
            
                // Envoyer le message
                Transport.send(message);
                LOGGER.log(Level.INFO, "Email envoyé avec succès à {0}", destinataire);
                return true;
            } catch (MessagingException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de l'envoi de l'email: {0}", e.getMessage());
                // En cas d'échec, sauvegarder dans un fichier
                LOGGER.log(Level.INFO, "Tentative de sauvegarde de l'email dans un fichier...");
                return sauvegarderEmailDansFichier(destinataire, sujet, corps);
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
    public static void envoyerNotifications(utilisateur user, Reclamation reclamation, String type) {
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
    public static boolean envoyerEmailReclamationAdmin(utilisateur user, Reclamation reclamation, boolean isResolutionRequest) {
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
    
    private static String genererCorpsEmailCreation(utilisateur user, Reclamation reclamation) {
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
    
    private static String genererCorpsEmailStatut(utilisateur user, Reclamation reclamation) {
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
    
    private static String genererCorpsEmailResolution(utilisateur user, Reclamation reclamation) {
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
     * @param utilisateur L'utilisateur à saluer
     * @return La salutation personnalisée
     */
    private static String getPersonalizedGreeting(utilisateur utilisateur) {
        if (utilisateur == null) {
            return "Bonjour";
        }
        
        // Vérifier si le nom complet est disponible
        boolean hasFirstName = utilisateur.getPrenom() != null && !utilisateur.getPrenom().trim().isEmpty();
        boolean hasLastName = utilisateur.getNom() != null && !utilisateur.getNom().trim().isEmpty();
        
        if (hasFirstName && hasLastName) {
            return "Bonjour " + utilisateur.getPrenom() + " " + utilisateur.getNom();
        } else if (hasFirstName) {
            return "Bonjour " + utilisateur.getPrenom();
        } else if (hasLastName) {
            return "Bonjour " + utilisateur.getNom();
        } else if (utilisateur.getUsername() != null && !utilisateur.getUsername().trim().isEmpty()) {
            // Utiliser le nom d'utilisateur si disponible
            return "Bonjour " + utilisateur.getUsername();
        } else {
            // Cas où aucun nom n'est disponible
            return "Bonjour";
        }
    }
    
    /**
     * Génère le corps de l'email pour l'administrateur (notification automatique)
     */
    private static String genererCorpsEmailAdmin(utilisateur utilisateur, Reclamation reclamation) {
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
        sb.append("Nom d'utilisateur : ").append(utilisateur.getUsername()).append("\n");
        sb.append("Email : ").append(utilisateur.getEmail()).append("\n");
        if (utilisateur.getTelephone() != null && !utilisateur.getTelephone().isEmpty()) {
            sb.append("Téléphone : ").append(utilisateur.getTelephone()).append("\n");
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
    private static String genererCorpsEmailAdminResolution(utilisateur utilisateur, Reclamation reclamation) {
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
        sb.append("Nom d'utilisateur : ").append(utilisateur.getUsername()).append("\n");
        sb.append("Email : ").append(utilisateur.getEmail()).append("\n");
        if (utilisateur.getTelephone() != null && !utilisateur.getTelephone().isEmpty()) {
            sb.append("Téléphone : ").append(utilisateur.getTelephone()).append("\n");
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