package Utlis;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.regex.*;

public class EmailSender {

    // Méthode pour envoyer un e-mail
    public static void sendEmail(String to, String subject, String message) {
        // Vérification de la validité de l'adresse email
        if (!isValidEmail(to)) {
            System.out.println("Adresse e-mail invalide : " + to);
            return;
        }

        // Propriétés du serveur SMTP (Gmail)
        String host = "smtp.gmail.com";
        String from = "soulaymaboukhadra@gmail.com"; // Ton adresse Gmail
        String password = "nxfrxgmvqshqjypq"; // Mot de passe d'application (sans espaces)

        // Configuration des propriétés SMTP
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Session avec authentification
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            // Création du message
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(from)); // Ton adresse email
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to)); // Email du destinataire
            mimeMessage.setSubject(subject); // Sujet du message
            mimeMessage.setText(message); // Contenu du message

            // Envoi du message
            Transport.send(mimeMessage);
            System.out.println("Email envoyé avec succès !");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'envoi de l'email.");
        }
    }

    // Méthode pour vérifier si l'email est valide
    public static boolean isValidEmail(String email) {
        // Expression régulière pour vérifier l'adresse e-mail
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
