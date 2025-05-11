package Controllers;
import java.io.IOException;
import java.sql.*;

import Entites.user;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import utils.database;

import java.net.URL;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelloController implements Initializable {

    public static String currentUsername;  // Changed from username to currentUsername

    @FXML
    private TextField verifymail;

    @FXML
    private Button verifymailbutton;

    @FXML
    private CheckBox show_password;
    @FXML
    private Hyperlink create_account;

    @FXML
    private TextField email;

    @FXML
    private Hyperlink login;

    @FXML
    private Button login_btn;

    @FXML
    private ImageView wego;

    @FXML
    private ImageView wego1;

    @FXML
    private AnchorPane verificationbikeform;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField password1;

    @FXML
    private Button signup_btn;

    @FXML
    private TextField username;

    @FXML
    private TextField username1;

    @FXML
    private AnchorPane login_form;

    @FXML
    private AnchorPane signup_form;

    @FXML
    private AnchorPane signup_page;

    @FXML
    private TextField code;

    @FXML
    private Button confirm;

    @FXML
    private AnchorPane verificationform;

    @FXML
    private Hyperlink forget_password;

    @FXML
    private AnchorPane login_forget;
    @FXML
    private AnchorPane verify_forget;

    private Connection connect;
    private Statement statement;
    private PreparedStatement prepare;
    private ResultSet result;

    private double x = 0;
    private double y = 0;


    public void exit(){

        System.exit(0);

    }

    public void textfieldDesign(){
        if(username.isFocused()){

            username.setStyle("-fx-background-color:#fff;"
                    + "-fx-border-width:2px");

            password.setStyle("-fx-background-color:transparent;"
                    + "-fx-border-width:1px");

        }else if(password.isFocused()){

            username.setStyle("-fx-background-color:transparent;"
                    + "-f-border-width:1px");

            password.setStyle("-fx-background-color:#fff;"
                    + "-fx-border-width:2px");

        }

    }
    public void textfieldDesign1(){

        if(username1.isFocused()){

            username1.setStyle("-fx-background-color:#fff;"
                    + "-fx-border-width:2px");

            password1.setStyle("-fx-background-color:transparent;"
                    + "-fx-border-width:1px");

            password1.setStyle("-fx-background-color:transparent;"
                    + "-fx-border-width:1px");

        }else if(email.isFocused()){

            username1.setStyle("-fx-background-color:transparent;"
                    + "-fx-border-width:1px");

            password1.setStyle("-fx-background-color:transparent;"
                    + "-fx-border-width:1px");

            email.setStyle("-fx-background-color:#fff;"
                    + "-fx-border-width:2px");

        }else if(password1.isFocused()){

            username1.setStyle("-fx-background-color:transparent;"
                    + "-fx-border-width:1px");

            password1.setStyle("-fx-background-color:#fff;"
                    + "-fx-border-width:2px");

            email.setStyle("-fx-background-color:transparent;"
                    + "-fx-border-width:1px");

        }

    }
    public void dropShadowEffect(){

        DropShadow original = new DropShadow(30, Color.valueOf("#ae44a5"));

        original.setRadius(30);

        wego.setEffect(original);

        wego1.setEffect(original);

        wego.setOnMouseEntered((MouseEvent event) ->{

            DropShadow shadow = new DropShadow(60, Color.valueOf("#e03ed5"));

            wego.setCursor(Cursor.HAND);
            wego.setStyle("-fx-text-fill:#ee5fe4");
            wego.setEffect(shadow);

        });

        wego.setOnMouseExited((MouseEvent event) ->{

            DropShadow shadow = new DropShadow(20, Color.valueOf("#ae44a5"));

            shadow.setRadius(30);

            wego.setStyle("-fx-text-fill:#000");
            wego.setEffect(shadow);

        });

        wego1.setOnMouseEntered((MouseEvent event) ->{

            DropShadow shadow = new DropShadow(60, Color.valueOf("#e03ed5"));

            wego1.setCursor(Cursor.HAND);
            wego1.setStyle("-fx-text-fill:#ee5fe4");
            wego1.setEffect(shadow);

        });

        wego1.setOnMouseExited((MouseEvent event) ->{

            DropShadow shadow = new DropShadow(20, Color.valueOf("#ae44a5"));

            shadow.setRadius(30);

            wego1.setStyle("-fx-text-fill:#000");
            wego1.setEffect(shadow);

        });

    }
    public void changeForm(ActionEvent event){

        if(event.getSource() == create_account){

            signup_form.setVisible(true);
            login_form.setVisible(false);

            username1.setText("");
            password1.setText("");
            email.setText("");

        }else if(event.getSource() == login){

            login_form.setVisible(true);
            signup_form.setVisible(false);

            username.setText("");
            password.setText("");

        }
        else if(event.getSource() == forget_password){

            login_forget.setVisible(false);
            verify_forget.setVisible(true);

            username.setText("");
            password.setText("");

        }
        else if(event.getSource() == confirm){

            login_form.setVisible(true);
            verificationbikeform.setVisible(false);
            verificationform.setVisible(false);

            username.setText("");
            password.setText("");

        }


    }


    public void login() {
        connect = database.connectDb();
        String sql = "SELECT * FROM utilisateur WHERE username = ? and password = ?";

        try {
            prepare = connect.prepareStatement(sql);
            prepare.setString(1, username.getText());
            prepare.setString(2, password.getText());

            result = prepare.executeQuery();

            if (result.next()) {
                HelloController.currentUsername = username.getText();  // Updated to use currentUsername
                user.username = result.getString("username");
                user.path = result.getString("image");
                user.date = result.getString("date");
                user.role = result.getString("role");
                user.email = result.getString("email");
                user.gender = result.getString("gender");

                login_btn.getScene().getWindow().hide();

                if (isAdminEmail(user.email)) {
                    loadScene("backoffice.fxml");
                } else {
                    loadScene("Dashboard.fxml");
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Message d'erreur");
                alert.setHeaderText(null);
                alert.setContentText("Nom d'utilisateur/mot de passe incorrect !");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Login error: " + e.getMessage());
            alert.showAndWait();
        }
    }
    public static void loadScene(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloController.class.getResource("/fxml/" + fxmlFileName));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            
            // Store scene coordinates for window dragging
            final double[] xOffset = {0};
            final double[] yOffset = {0};
            
            root.setOnMousePressed(event -> {
                xOffset[0] = event.getSceneX();
                yOffset[0] = event.getSceneY();
            });
            
            root.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - xOffset[0]);
                stage.setY(event.getScreenY() - yOffset[0]);
                stage.setOpacity(0.8);
            });
            
            root.setOnMouseReleased(event -> stage.setOpacity(1));
            
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load " + fxmlFileName + ": " + e.getMessage());
            alert.showAndWait();
        }
    }
    public boolean validationEmail(){
//
        Pattern pattern = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9._]*@[a-zA-Z0-9]+([.][a-zA-Z]+)+");

        Matcher match = pattern.matcher(email.getText());

        if(match.find() && match.group().equals(email.getText())){

            return true;

        }else{

            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Invalid Email");
            alert.showAndWait();

            return false;

        }

    }
    private String codeDeVerification;
    public String genererCodeVerification() {
        // Définir la longueur du code de vérification (par exemple, 6 chiffres)
        int longueurCode = 6;

        String caracteresPossibles = "0123456789";

        // Créer un objet Random
        Random random = new Random();

        StringBuilder codeVerification = new StringBuilder();


        for (int i = 0; i < longueurCode; i++) {

            int indiceAleatoire = random.nextInt(caracteresPossibles.length());

            codeVerification.append(caracteresPossibles.charAt(indiceAleatoire));
        }

        // Convertir le code de vérification en chaîne et le renvoyer
        codeDeVerification=codeVerification.toString();
        return codeDeVerification;
    }
    public void sendVerificationCodeByEmail(String destinataire, String codeDeVerification, String usernameGmail, String passwordGmail) {
        final String username = usernameGmail; // Utilisez votre adresse e-mail Gmail ici
        final String password = passwordGmail; // Utilisez votre mot de passe Gmail ici
        String host = "smtp.gmail.com"; // Serveur SMTP de Gmail
        int port = 587; // Port SMTP de Gmail (587 est le port TLS)

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
                protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new jakarta.mail.PasswordAuthentication(username, password);
                }
            });

            // Créez le contenu HTML de l'e-mail
            String htmlContent = "<html><body>";
            htmlContent += "<table style=\"width:538px;background-color:#393836\" align=\"center\" cellspacing=\"0\" cellpadding=\"0\">";
            htmlContent +="<tbody><tr>";
            htmlContent +="<td style=\"height:65px;background-color:#171a21;border-bottom:1px solid #4d4b48\">\n" +
                    "\t\t\t<img src=\"\" width=\"538\" height=\"65\" alt=\"Steam\" class=\"CToWUd\" data-bit=\"iit\">\n" +
                    "\t\t</td>";
            htmlContent += "</tr>";
            htmlContent +="<tr>";
            htmlContent +="<td bgcolor=\"#17212e\">\n" +
                    "\t\t\t<table width=\"470\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding-left:5px;padding-right:5px;padding-bottom:10px\">\n" +
                    "\n" +
                    "\t\t\t\t<tbody><tr bgcolor=\"#17212e\">\n" +
                    "\t\t\t\t\t<td style=\"padding-top:32px\">\n" +
                    "\t\t\t\t\t<span style=\"font-size:24px;color:#66c0f4;font-family:Arial,Helvetica,sans-serif;font-weight:bold\">\n" +
                    "\t\t\t\t\t\tHello \t\t\t\t\t</span><br>\n" +
                    "\t\t\t\t\t</td>\n" +
                    "\t\t\t\t</tr>\n" +
                    "\n" +
                    "\t\t\t\t<tr>\n" +
                    "\t\t\t\t\t<td style=\"padding-top:12px;font-size:17px;color:#c6d4df;font-family:Arial,Helvetica,sans-serif;font-weight:bold\">\n" +
                    "\t\t\t\t\t\tHere is the <span class=\"il\">code</span> you need to sign up \t\t\t\t\t</td>\n" +
                    "\t\t\t\t</tr>\n" +
                    "\n" +
                    "\t\t\t\t<tr>\n" +
                    "\t\t\t\t\t<td style=\"padding-top:24px;padding-bottom:24px\">\n" +
                    "\t\t\t\t\t\t<div>\n" +
                    "\t\t\t\t\t\t\t<span style=\"font-size:24px;color:#66c0f4;font-family:Arial,Helvetica,sans-serif;font-weight:bold\">"+codeDeVerification+"</span>\n" +
                    "\t\t\t\t\t\t\t\n" +
                    "\t\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t</td>\n" +
                    "\t\t\t\t</tr>\n" +
                    "\n" +
                    "\t\t\t\t<tr bgcolor=\"#121a25\">\n" +
                    "\t\t\t\t\t<td style=\"padding:20px;font-size:12px;line-height:17px;color:#c6d4df;font-family:Arial,Helvetica,sans-serif\">\n" +
                    "\t\t\t\t\t\tIf you are not trying to change your <span class=\"il\">wego</span> login credentials, please ignore this email. It is possible that another user entered their login information incorrectly.\t\t\t\t\t</td>\n" +
                    "\t\t\t\t</tr>\n" +
                    "\n" +
                    "\t\t\t\t<tr>\n" +
                    "\t\t\t\t\t<td style=\"font-size:12px;color:#6d7880;padding-top:16px;padding-bottom:60px\">\n" +
                    "\t\t\t\t\t\tThe <span class=\"il\">wego</span> Support Team<br>\n" +
                    "\t\t\t\t\t\t<a style=\"color:#8f98a0\" href=\"wego.esprit@esprit.tn/\" target=\"_blank\" ;/</a><br>\n" +
                    "\t\t\t\t\t</td>\n" +
                    "\t\t\t\t</tr>\n" +
                    "\n" +
                    "\t\t\t</tbody></table>\n" +
                    "\t\t</td>\n" +
                    "\t</tr>";
            htmlContent += "</body></html>";


            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject("SIGN UP TO wego");
            message.setContent(htmlContent, "text/html"); // Définir le contenu comme HTML

            Transport.send(message);
            System.out.println("E-mail de vérification envoyé avec succès à : " + destinataire);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'envoi de l'e-mail de vérification : " + e.getMessage());
        }
    }

    public void signup() {


        String usernameText = username1.getText();
        String passwordText = password1.getText();
        String emailText = email.getText();

        if (usernameText.isEmpty() || passwordText.isEmpty() || emailText.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Message d'erreur");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs !");
            alert.showAndWait();
        } else if (passwordText.length() < 8) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Message d'erreur");
            alert.setHeaderText(null);
            alert.setContentText("Mot de passe invalide (doit avoir au moins 8 caractères) !");
            alert.showAndWait();
        } else {
            if (validationEmail()) {

                signup_page.setVisible(false);
                verificationform.setVisible(true);

                codeDeVerification = genererCodeVerification();

                // Utilisez votre nom d'utilisateur Gmail et votre mot de passe Gmail ici
                String usernameGmail = "wassimzarai28@gmail.com";
                String passwordGmail = "nana jnzn vhcj fisp";

                sendVerificationCodeByEmail(emailText, codeDeVerification, usernameGmail, passwordGmail);
                this.codeDeVerification = codeDeVerification;

            }
        }
    }
    public void confirmVerificationCode() {
        connect = database.connectDb();

        String sql = "INSERT INTO utilisateur (username, password, email, image) VALUES (?, ?, ?, ?)";
        String codeSaisi = code.getText();

        if (codeSaisi.equals(codeDeVerification) && validationEmail()) {
            String uri = "C:\\Users\\wassi\\Pictures\\487673136_3987121521573655_8977661362410615366_n (1).jpg";

            user.path = uri;

            String usernameText = username1.getText();
            String passwordText = password1.getText();
            String emailText = email.getText();

            // String hashedPassword = BCrypt.hashpw(passwordText, BCrypt.gensalt());


            try {
                prepare = connect.prepareStatement(sql);
                prepare.setString(1, usernameText);
                prepare.setString(2, passwordText);
                prepare.setString(3, emailText);
                prepare.setString(4, user.path);

                prepare.executeUpdate();

                email.setText("");
                username1.setText("");
                password1.setText("");

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Message de réussite");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Compte créé avec succès !");
                successAlert.showAndWait();

                signup_page.setVisible(true);
                verificationform.setVisible(false);

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else {
            // Le code saisi est incorrect
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Message d'erreur");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Code de vérification incorrect. Veuillez réessayer.");
            errorAlert.showAndWait();
        }
    }



    public void showpass(CheckBox show_password, PasswordField password) {
        show_password.setOnAction(event -> {
            // Vérifiez si la case à cocher est cochée
            boolean isChecked = show_password.isSelected();

            // Changez le type de champ de texte en fonction de l'état de la case à cocher
            if (isChecked) {
                password.setDisable(false); // Activer le champ de mot de passe
                password.setStyle(""); // Supprimer les styles
                password.setPromptText(""); // Effacer le texte de l'invite
            } else {
                password.setDisable(false); // Activer le champ de mot de passe
                password.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: black;"); // Appliquer les styles
                password.setPromptText("Mot de passe"); // Définir l'invite sur "Mot de passe" si nécessaire
            }
        });
    }





    public boolean isAdminEmail(String email) {

        String emailAdministrateur = "zarai.wassim@esprit.tn";
        return email.equals(emailAdministrateur);
    }

    public void verifyAndSendResetEmail() {
        String emailToReset = verifymail.getText(); // Obtenez l'e-mail à réinitialiser depuis votre champ de texte

        // Vérifiez si l'e-mail existe dans la base de données
        if (emailExistsInDatabase(emailToReset)) {
            // Générez un mot de passe aléatoire
            String randomPassword = generateRandomPassword();

            // Enregistrez le nouveau mot de passe dans la base de données (vous devrez implémenter cette partie)

            // Envoyez un e-mail avec le nouveau mot de passe
            sendResetEmail(emailToReset, randomPassword);
            updatePasswordInDatabase(emailToReset, randomPassword);

            // Affichez un message à l'utilisateur pour lui dire que l'e-mail de réinitialisation a été envoyé
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Réinitialisation réussie");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Un e-mail avec un nouveau mot de passe a été envoyé à " + emailToReset);
            successAlert.showAndWait();

            verify_forget.setVisible(false);
            login_forget.setVisible(true);
        } else {
            // L'e-mail n'existe pas dans la base de données, affichez un message d'erreur à l'utilisateur
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("L'adresse e-mail n'existe pas dans notre base de données.");
            errorAlert.showAndWait();
        }
    }
    private void updatePasswordInDatabase(String email, String newPassword) {
        // Mettez à jour le mot de passe dans la base de données en utilisant l'e-mail comme condition
        connect = database.connectDb();
        String sql = "UPDATE utilisateur SET password = ? WHERE email = ?";

        try {
            prepare = connect.prepareStatement(sql);
            prepare.setString(1, newPassword);
            prepare.setString(2, email);
            int rowsAffected = prepare.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Mot de passe mis à jour avec succès dans la base de données !");
            } else {
                System.err.println("Échec de la mise à jour du mot de passe dans la base de données.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la mise à jour du mot de passe dans la base de données : " + e.getMessage());
        }
    }
    private boolean emailExistsInDatabase(String email) {
        connect = database.connectDb();
        String sql = "SELECT * FROM utilisateur WHERE email = ?";
        try {
            prepare  = connect.prepareStatement(sql);
            prepare.setString(1, email);
            ResultSet resultSet = prepare.executeQuery();
            return resultSet.next(); // Si le résultat contient des lignes, l'e-mail existe
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // En cas d'erreur, assumez que l'e-mail n'existe pas
        }
    }

    private String generateRandomPassword() {
        // Générez un mot de passe aléatoire de 8 caractères (modifiable)
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 8; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }

        return password.toString();
    }

    public void sendResetEmail(String recipientEmail, String randomPassword) {
        // Paramètres du serveur SMTP
        String host = "smtp.gmail.com"; // Remplacez par le serveur SMTP de votre fournisseur de messagerie
        String port = "587"; // Port du serveur SMTP (peut varier en fonction du fournisseur)
        String username = "wassimzarai28@gmail.com"; // Adresse e-mail de l'expéditeur
        String password = "nana jnzn vhcj fisp"; // Mot de passe de l'expéditeur

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);

        // Créez une session pour l'envoi de l'e-mail
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Créez un objet MimeMessage
            Message message = new MimeMessage(session);

            // Configurez l'expéditeur et le destinataire
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));

            // Sujet de l'e-mail
            message.setSubject("Réinitialisation du mot de passe");

            // Corps de l'e-mail
            message.setText("Votre nouveau mot de passe est : " + randomPassword);

            // Envoyez l'e-mail
            Transport.send(message);

            System.out.println("E-mail envoyé avec succès !");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'envoi de l'e-mail : " + e.getMessage());
        }
    }









    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dropShadowEffect();
        showpass( show_password,  password);




    }
}