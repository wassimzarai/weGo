package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import java.io.IOException;

public class MenuPrincipalController {

    @FXML
    private Button btnMenuPaiement;

    @FXML
    private Button btnMenuFacture;

    @FXML
    public void ouvrirMenuPaiement(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MenuPaiement.fxml"));
        Stage stage = (Stage) btnMenuPaiement.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    public void ouvrirMenuFacture(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MenuFacture.fxml"));
        Stage stage = (Stage) btnMenuFacture.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    public void onHoverCartePaiement(MouseEvent event) {
        ((Button) event.getSource()).setStyle("-fx-background-color: linear-gradient(to bottom right, #148F77, #117A65); -fx-background-radius: 15px; -fx-text-fill: white; -fx-font-size: 18px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 4);");
    }

    @FXML
    public void onExitCartePaiement(MouseEvent event) {
        ((Button) event.getSource()).setStyle("-fx-background-color: linear-gradient(to bottom right, #1ABC9C, #16A085); -fx-background-radius: 15px; -fx-text-fill: white; -fx-font-size: 18px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 4);");
    }

    @FXML
    public void onHoverCarteFacture(MouseEvent event) {
        ((Button) event.getSource()).setStyle("-fx-background-color: linear-gradient(to bottom right, #28B463, #239B56); -fx-background-radius: 15px; -fx-text-fill: white; -fx-font-size: 18px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 4);");
    }

    @FXML
    public void onExitCarteFacture(MouseEvent event) {
        ((Button) event.getSource()).setStyle("-fx-background-color: linear-gradient(to bottom right, #32D18A, #1ABC9C); -fx-background-radius: 15px; -fx-text-fill: white; -fx-font-size: 18px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 4);");
    }
}
