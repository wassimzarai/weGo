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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ModifierPaiementControl {

    @FXML private TextField txtid;
    @FXML private TextField txtidres;
    @FXML private TextField txtmontant;
    @FXML private TextField txtEmail;  // Champ Email ajouté

    @FXML private ComboBox<String> comboMethode;
    @FXML private DatePicker datePickerPaiement;

    @FXML private CheckBox cbPaye;
    @FXML private CheckBox cbEnAttente;
    @FXML private CheckBox cbAnnule;

    @FXML private TextField txtRechercheId;
    @FXML private Button btnModifier;
    @FXML private Button btnRechercher;
    @FXML private Button btnRetourMenu;

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
    void btnmodifier(ActionEvent event) {
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
                    txtEmail.getText()  // Ajout email
            );

            if (service.modifierPaiement(p)) {
                afficherSucces("Paiement modifié avec succès !");
                handleEnvoyerEmail(p.getEmail());
            } else {
                afficherErreur("Échec de la modification du paiement.");
            }
        } catch (NumberFormatException e) {
            afficherErreur("Veuillez entrer des valeurs valides.");
        }
    }

    @FXML
    void rechercherPaiement(ActionEvent event) {
        try {
            int idRecherche = Integer.parseInt(txtRechercheId.getText());
            Paiement p = service.rechercherPaiementParId(idRecherche);

            if (p != null) {
                txtid.setText(String.valueOf(p.getId()));
                txtidres.setText(String.valueOf(p.getReservationId()));
                txtmontant.setText(String.valueOf(p.getMontant()));
                txtEmail.setText(p.getEmail());  // Remplir email

                LocalDate date = LocalDate.parse(p.getDatePaiement(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                datePickerPaiement.setValue(date);
                comboMethode.setValue(p.getMethodePaiement());

                cbPaye.setSelected(false);
                cbEnAttente.setSelected(false);
                cbAnnule.setSelected(false);
                switch (p.getStatut()) {
                    case "Payé":
                        cbPaye.setSelected(true);
                        break;
                    case "En attente":
                        cbEnAttente.setSelected(true);
                        break;
                    case "Annulé":
                        cbAnnule.setSelected(true);
                        break;
                }

                afficherSucces("Paiement trouvé !");
            } else {
                afficherErreur("Aucun paiement trouvé avec cet ID.");
            }

        } catch (NumberFormatException e) {
            afficherErreur("Veuillez entrer un ID valide.");
        }
    }

    @FXML
    private void handleBackToMenu(ActionEvent event) {
        try {
            Stage stage = (Stage) btnRetourMenu.getScene().getWindow();
            Scene menuScene = new Scene(FXMLLoader.load(getClass().getResource("/menu.fxml")));
            stage.setScene(menuScene);
        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur lors du chargement du menu.");
        }
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

    private void handleEnvoyerEmail(String emailClient) {
        String subject = "Modification de Paiement";
        String body = "Bonjour,\n\nNous vous informons que votre paiement a été modifié avec succès.\n\nCordialement, L'équipe WeGo.";

        if (!emailClient.isEmpty()) {
            EmailSender.sendEmail(emailClient, subject, body);
            afficherSucces("Email envoyé avec succès !");
        } else {
            afficherErreur("L'email du client est vide. Veuillez vérifier les informations.");
        }
    }
}
