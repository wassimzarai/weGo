package Entities;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Voiture {

    private IntegerProperty id;
    private StringProperty marque;
    private StringProperty modele;
    private StringProperty matricule;  // Added matricule field
//    private TypeVoiture type;
    private Boolean isBlocked;  // Add isBlocked field to track blocked state

    public Voiture() {
        this.id = new SimpleIntegerProperty();
        this.marque = new SimpleStringProperty();
        this.modele = new SimpleStringProperty();
        this.matricule = new SimpleStringProperty();  // Initialize matricule field
        this.isBlocked = false; // Default value is false (not blocked)
    }

    // Constructor with matricule added
    public Voiture(String marque, String modele, String matricule) {
        this();
        this.marque.set(marque);
        this.modele.set(modele);
//        this.type = type;
        this.matricule.set(matricule);  // Initialize matricule
    }

    // Constructor with id and matricule added
    public Voiture(int id, String marque, String modele, String matricule) {
        this(marque, modele, matricule);
        this.id.set(id);
    }

    // Getters for JavaFX properties
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty marqueProperty() {
        return marque;
    }

    public StringProperty modeleProperty() {
        return modele;
    }

    public StringProperty matriculeProperty() {
        return matricule;
    }

//    public StringProperty typeProperty() {
//        return new SimpleStringProperty(type.name());
//    }

    // Getters and setters for regular access
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getMarque() {
        return marque.get();
    }

    public void setMarque(String marque) {
        this.marque.set(marque);
    }

    public String getModele() {
        return modele.get();
    }

    public void setModele(String modele) {
        this.modele.set(modele);
    }

    public String getMatricule() {
        return matricule.get();
    }

    public void setMatricule(String matricule) {
        this.matricule.set(matricule);
    }

//    public TypeVoiture getType() {
//        return type;
//    }

//    public void setType(TypeVoiture type) {
//        this.type = type;
//    }

    // Getter and Setter for isBlocked field
    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        this.isBlocked = blocked;
    }

    @Override
    public String toString() {
        return "Voiture{" +
                "id=" + id.get() +
                ", marque='" + marque.get() + '\'' +
                ", modele='" + modele.get() + '\'' +
                ", matricule='" + matricule.get() + '\'' +

                ", isBlocked=" + isBlocked +
                '}';
    }
}
