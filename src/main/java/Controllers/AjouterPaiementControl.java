package Controllers;

import Utlis.EmailSender;
import entities.Paiement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServicePaiement;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class AjouterPaiementControl {

        @FXML
        private TextField txtid;
        @FXML
        private TextField txtidres;
        @FXML
        private TextField txtmontant;
        @FXML
        private TextField txtEmail;

        @FXML
        private ComboBox<String> comboMethode;
        @FXML
        private DatePicker datePickerPaiement;

        @FXML
        private Button btnAjouter;
        @FXML
        private Button btnRetourMenu;
        @FXML
        private Button btnEnvoyerEmail;   // On garde ton nom btnEnvoyerEmail

        @FXML
        private CheckBox cbPaye;
        @FXML
        private CheckBox cbEnAttente;
        @FXML
        private CheckBox cbAnnule;

        private final ServicePaiement service = new ServicePaiement();

        @FXML
        void initialize() {
                comboMethode.getItems().addAll("Carte bancaire", "Espèces", "Virement", "Chèque");

                cbPaye.setOnAction(e -> {
                        if (cbPaye.isSelected()) {
                                cbEnAttente.setSelected(false);
                                cbAnnule.setSelected(false);
                        }
                });
                cbEnAttente.setOnAction(e -> {
                        if (cbEnAttente.isSelected()) {
                                cbPaye.setSelected(false);
                                cbAnnule.setSelected(false);
                        }
                });
                cbAnnule.setOnAction(e -> {
                        if (cbAnnule.isSelected()) {
                                cbPaye.setSelected(false);
                                cbEnAttente.setSelected(false);
                        }
                });
        }

        @FXML
        void btnajouter(ActionEvent event) {
                try {
                        if (txtid.getText().isEmpty() || txtidres.getText().isEmpty() || txtmontant.getText().isEmpty() ||
                                comboMethode.getValue() == null || datePickerPaiement.getValue() == null || txtEmail.getText().isEmpty()) {
                                afficherErreur("Tous les champs doivent être remplis.");
                                return;
                        }

                        String statut = "";
                        if (cbPaye.isSelected()) {
                                statut = "Payé";
                        } else if (cbEnAttente.isSelected()) {
                                statut = "En attente";
                        } else if (cbAnnule.isSelected()) {
                                statut = "Annulé";
                        } else {
                                afficherErreur("Veuillez sélectionner un état de paiement.");
                                return;
                        }

                        String datePaiement = datePickerPaiement.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                        Paiement p = new Paiement(
                                Integer.parseInt(txtid.getText()),
                                Integer.parseInt(txtidres.getText()),
                                Double.parseDouble(txtmontant.getText()),
                                datePaiement,
                                comboMethode.getValue(),
                                statut,
                                txtEmail.getText()
                        );

                        if (service.ajouterPaiement(p)) {
                                afficherSucces("Paiement ajouté avec succès !");
                                envoyerEmail(p.getEmail());  // Email auto après ajout
                                viderChamps();
                        } else {
                                afficherErreur("Échec de l'ajout du paiement.");
                        }

                } catch (NumberFormatException e) {
                        afficherErreur("Veuillez entrer des valeurs valides pour l'ID et le Montant.");
                }
        }

        // Méthode liée au bouton btnEnvoyerEmail (manuel)
        // Méthode liée au bouton btnEnvoyerEmail (manuel)
        @FXML
        private void handleEnvoyerEmail(ActionEvent event) {
                String email = txtEmail.getText();

                // Vérification que l'email n'est pas vide et est valide
                if (email.isEmpty() || !EmailSender.isValidEmail(email)) {
                        afficherErreur("Veuillez entrer une adresse email valide.");
                } else {
                        envoyerEmail(email);
                }
        }

        // Fonction commune d’envoi
        private void envoyerEmail(String emailClient) {
                String subject = "Confirmation de Paiement";
                String body = "Bonjour,\n\nNous vous confirmons que votre paiement a été effectué avec succès. Merci d'utiliser notre service !\n\nCordialement,\nL'équipe WeGo.";

                if (!emailClient.isEmpty() && EmailSender.isValidEmail(emailClient)) {
                        try {
                                EmailSender.sendEmail(emailClient, subject, body);  // Appel à la méthode d'envoi d'email
                                afficherSucces("Email envoyé avec succès à " + emailClient + " !");
                        } catch (Exception e) {
                                afficherErreur("Erreur lors de l'envoi de l'email. Veuillez réessayer.");
                        }
                } else {
                        afficherErreur("L'email du client est vide ou invalide. Veuillez vérifier les informations.");
                }
        }



        private void viderChamps() {
                txtid.clear();
                txtidres.clear();
                txtmontant.clear();
                comboMethode.setValue(null);
                datePickerPaiement.setValue(null);
                txtEmail.clear();
                cbPaye.setSelected(false);
                cbEnAttente.setSelected(false);
                cbAnnule.setSelected(false);
        }

        private void afficherSucces(String message) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
        }

        private void afficherErreur(String message) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
        }

        @FXML
        private void handleBackToMenu(ActionEvent event) {
                try {
                        Stage stage = (Stage) btnRetourMenu.getScene().getWindow();
                        Scene menuScene = new Scene(FXMLLoader.load(getClass().getResource("/MenuPaiement.fxml")));
                        stage.setScene(menuScene);
                } catch (IOException e) {
                        e.printStackTrace();
                        afficherErreur("Erreur lors du chargement du menu.");
                }
        }
}
