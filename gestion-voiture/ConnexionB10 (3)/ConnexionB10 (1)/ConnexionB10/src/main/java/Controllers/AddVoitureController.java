package Controllers;

import Entities.Voiture;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class AddVoitureController {

    @FXML private TextField marqueField;
    @FXML private TextField modeleField;
    @FXML private TextField typeField;

    private Voiture voiture;

    // Method to set the Voiture object
    public void setVoiture(Voiture voiture) {
        this.voiture = voiture;

        // Populate the fields with the car's current values
        marqueField.setText(voiture.getMarque());
        modeleField.setText(voiture.getModele());
        typeField.setText(voiture.getType().toString());
    }
}
