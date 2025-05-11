package Entites;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Voiture {

    private IntegerProperty id;
    private StringProperty marque;
    private StringProperty modele;
    private StringProperty matricule;
    private TypeVoiture type;
    private boolean isBlocked;

    public Voiture() {
        this.id = new SimpleIntegerProperty();
        this.marque = new SimpleStringProperty();
        this.modele = new SimpleStringProperty();
        this.matricule = new SimpleStringProperty();
        this.isBlocked = false;
    }

    public Voiture(String marque, String modele, TypeVoiture type, String matricule) {
        this();
        this.marque.set(marque);
        this.modele.set(modele);
        this.type = type;
        this.matricule.set(matricule);
    }

    public Voiture(int id, String marque, String modele, TypeVoiture type, String matricule) {
        this(marque, modele, type, matricule);
        this.id.set(id);
    }

    public Voiture( String marque, String modele, String matricule) {
        this();                     // initialise id, marque, modele, matricule

        this.marque.set(marque);
        this.modele.set(modele);
        this.matricule.set(matricule);
    }
//
public Voiture(int id, String marque, String modele, String matricule) {
    this();                     // initialise id, marque, modele, matricule
    this.id.set(id);
    this.marque.set(marque);
    this.modele.set(modele);
    this.matricule.set(matricule);
}



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

    public StringProperty typeProperty() {
        return new SimpleStringProperty(type.name());
    }

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

    public TypeVoiture getType() {
        return type;
    }

    public void setType(TypeVoiture type) {
        this.type = type;
    }

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
                ", type=" + type +
                ", isBlocked=" + isBlocked +
                '}';
    }
}
