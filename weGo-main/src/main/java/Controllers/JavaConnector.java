package Controllers; // mets le bon package selon ton projet

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class JavaConnector {
    private TextField textField;
    private Stage mapStage;

    public JavaConnector(TextField textField, Stage mapStage) {
        this.textField = textField;
        this.mapStage = mapStage;
    }

    public void receiveCoordinates(String coords) {
        Platform.runLater(() -> {
            System.out.println("Coordonnée reçue : " + coords);
            if (textField != null) {
                textField.setText(coords);
            } else {
                System.err.println("TextField est null !");
            }
            mapStage.close();
        });
    }
}

