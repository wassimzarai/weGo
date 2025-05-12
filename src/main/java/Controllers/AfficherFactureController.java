package Controllers;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import entities.Facture;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import services.ServiceFacture;
import services.ServicePaiement;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AfficherFactureController {

    @FXML
    private TableView<Facture> tableFactures;
    @FXML
    private TableColumn<Facture, Integer> colId;
    @FXML
    private TableColumn<Facture, Integer> colReservationId;
    @FXML
    private TableColumn<Facture, Integer> colPaiementId;
    @FXML
    private TableColumn<Facture, Double> colMontantTotal;
    @FXML
    private TableColumn<Facture, Double> colMontantPaye;
    @FXML
    private TableColumn<Facture, String> colDateEmission;
    @FXML
    private TableColumn<Facture, String> colDescription;

    @FXML
    private Button btnActualiser;
    @FXML
    private Button btnDetails;
    @FXML
    private Button btnRetour;

    private final ServiceFacture serviceFacture = new ServiceFacture();
    private final ServicePaiement servicePaiement = new ServicePaiement();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idFacture"));
        colReservationId.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        colPaiementId.setCellValueFactory(new PropertyValueFactory<>("paiementId"));
        colMontantTotal.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        colMontantPaye.setCellValueFactory(new PropertyValueFactory<>("montantPaye"));
        colDateEmission.setCellValueFactory(new PropertyValueFactory<>("dateEmission"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        actualiserFactures();
    }

    @FXML
    public void actualiserFactures() {
        try {
            ObservableList<Facture> data = FXCollections.observableArrayList(serviceFacture.getList());
            tableFactures.setItems(data);

            if (data.isEmpty()) {
                showAlert("Aucune facture trouvée", AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Erreur lors du chargement: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void afficherDetailsFacture() {
        Facture selected = tableFactures.getSelectionModel().getSelectedItem();
        if (selected != null) {
            StringBuilder details = new StringBuilder();
            details.append("ID Facture: ").append(selected.getIdFacture()).append("\n")
                    .append("Réservation ID: ").append(selected.getReservationId()).append("\n")
                    .append("Paiement ID: ").append(selected.getPaiementId()).append("\n")
                    .append("Montant Total: ").append(selected.getMontantTotal()).append("\n")
                    .append("Montant Payé: ").append(selected.getMontantPaye()).append("\n")
                    .append("Date: ").append(selected.getDateEmission()).append("\n")
                    .append("Description: ").append(selected.getDescription());

            showAlert(details.toString(), AlertType.INFORMATION);
        } else {
            showAlert("Veuillez sélectionner une facture", AlertType.WARNING);
        }
    }

    @FXML
    private void handleBackToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MenuFacture.fxml"));
            Parent root = loader.load();
            btnRetour.getScene().setRoot(root);
        } catch (Exception e) {
            showAlert("Erreur lors du retour au menu : " + e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void exporterFacturePDF(ActionEvent event) {
        Facture facture = tableFactures.getSelectionModel().getSelectedItem();
        if (facture != null) {
            exporterFactureEnPDF(facture);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Avertissement");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une facture à exporter.");
            alert.showAndWait();
        }
    }

    private void exporterFactureEnPDF(Facture facture) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer la facture en PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf"));
        fileChooser.setInitialFileName("facture_" + facture.getIdFacture() + ".pdf");

        Window stage = tableFactures.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            Document document = new Document();

            try {
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                document.add(new Paragraph("FACTURE"));
                document.add(new Paragraph("----------------------------"));
                document.add(new Paragraph("ID Facture : " + facture.getIdFacture()));
                document.add(new Paragraph("ID Réservation : " + facture.getReservationId()));
                document.add(new Paragraph("ID Paiement : " + facture.getPaiementId()));
                document.add(new Paragraph("Montant Total : " + facture.getMontantTotal() + " TND"));
                document.add(new Paragraph("Montant Payé : " + facture.getMontantPaye() + " TND"));
                document.add(new Paragraph("Date d'émission : " + facture.getDateEmission()));
                document.add(new Paragraph("Description : " + facture.getDescription()));

                document.close();

                showAlert("Facture exportée avec succès !", AlertType.INFORMATION);

            } catch (DocumentException | IOException e) {
                e.printStackTrace();
                showAlert("Erreur lors de l'exportation du PDF : " + e.getMessage(), AlertType.ERROR);
            }
        }
    }


    private void afficherGraphiqueFacture(Facture facture) {
        double montantPaye = facture.getMontantPaye();
        double montantTotal = facture.getMontantTotal();
        double montantRestant = montantTotal - montantPaye;

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Répartition Paiement");

        PieChart.Data paye = new PieChart.Data("Payé : " + montantPaye + " TND", montantPaye);
        PieChart.Data restant = new PieChart.Data("Restant : " + montantRestant + " TND", montantRestant);

        pieChart.getData().addAll(paye, restant);

        VBox vbox = new VBox(pieChart);
        Scene scene = new Scene(vbox, 400, 300);

        Stage stage = new Stage();
        stage.setTitle("Détails Paiement Facture");
        stage.setScene(scene);
        stage.show();
    }
    @FXML
    private void exporterGraphiqueFacture(ActionEvent event) {
        Facture facture = tableFactures.getSelectionModel().getSelectedItem();
        if (facture != null) {
            afficherGraphiqueFacture(facture);
        } else {
            showAlert("Veuillez sélectionner une facture pour afficher le graphique.", AlertType.WARNING);
        }
    }

    private void showAlert(String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
