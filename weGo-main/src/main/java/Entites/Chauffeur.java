package Entites;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
      public class Chauffeur {

        private IntegerProperty id;
        private IntegerProperty idVoiture;
        private IntegerProperty disponibilite; // Change to IntegerProperty
        private StringProperty marqueVoiture;
        private StringProperty modeleVoiture;
        private StringProperty typeVoiture;
        private LocalDate dateDepart;
        private LocalDate dateArret;

        // Default constructor
        public Chauffeur() {
            this.id = new SimpleIntegerProperty();
            this.idVoiture = new SimpleIntegerProperty();
            this.disponibilite = new SimpleIntegerProperty();  // Set as IntegerProperty
            this.marqueVoiture = new SimpleStringProperty();
            this.modeleVoiture = new SimpleStringProperty();
            this.typeVoiture = new SimpleStringProperty();
            this.dateDepart = LocalDate.now();
            this.dateArret = LocalDate.now();
        }

        // Constructor with idVoiture and disponibilite
        public Chauffeur(Integer idVoiture, int disponibilite, LocalDate dateDepart, LocalDate dateArret) {
            this.id = new SimpleIntegerProperty();
            this.idVoiture = new SimpleIntegerProperty(idVoiture);
            this.disponibilite = new SimpleIntegerProperty(disponibilite);  // Correctly use IntegerProperty here
            this.marqueVoiture = new SimpleStringProperty();
            this.modeleVoiture = new SimpleStringProperty();
            this.typeVoiture = new SimpleStringProperty();
            this.dateDepart = dateDepart;
            this.dateArret = dateArret;
        }

        // Constructor with all fields
        public Chauffeur(int id, int idVoiture, String disponibilite, String marqueVoiture, String modeleVoiture, String typeVoiture, LocalDate dateDepart, LocalDate dateArret) {
            this.id = new SimpleIntegerProperty(id);
            this.idVoiture = new SimpleIntegerProperty(idVoiture);
            this.disponibilite = new SimpleIntegerProperty(Integer.parseInt(disponibilite)); // Handle string conversion to integer
            this.marqueVoiture = new SimpleStringProperty(marqueVoiture);
            this.modeleVoiture = new SimpleStringProperty(modeleVoiture);
            this.typeVoiture = new SimpleStringProperty(typeVoiture);
            this.dateDepart = dateDepart;
            this.dateArret = dateArret;
        }


        // Getters and Setters
        public IntegerProperty idProperty() {
            return id;
        }

        public IntegerProperty idVoitureProperty() {
            return idVoiture;
        }

        public IntegerProperty disponibiliteProperty() {
            return disponibilite; // Returns IntegerProperty
        }

        public StringProperty marqueVoitureProperty() {
            return marqueVoiture;
        }

        public StringProperty modeleVoitureProperty() {
            return modeleVoiture;
        }

        public StringProperty typeVoitureProperty() {
            return typeVoiture;
        }

        public LocalDate getDateDepart() {
            return dateDepart;
        }

        public void setDateDepart(LocalDate dateDepart) {
            this.dateDepart = dateDepart;
        }

        public LocalDate getDateArret() {
            return dateArret;
        }

        public void setDateArret(LocalDate dateArret) {
            this.dateArret = dateArret;
        }

        public int getId() {
            return id.get();
        }

        public void setId(int id) {
            this.id.set(id);
        }

        public int getIdVoiture() {
            return idVoiture.get();
        }

        public void setIdVoiture(int idVoiture) {
            this.idVoiture.set(idVoiture);
        }

        public int getDisponibilite() {
            return disponibilite.get();  // Get as an int
        }

        public void setDisponibilite(int disponibilite) {
            this.disponibilite.set(disponibilite);  // Set as an int
        }

        public String getMarqueVoiture() {
            return marqueVoiture.get();
        }

        public void setMarqueVoiture(String marqueVoiture) {
            this.marqueVoiture.set(marqueVoiture);
        }

        public String getModeleVoiture() {
            return modeleVoiture.get();
        }

        public void setModeleVoiture(String modeleVoiture) {
            this.modeleVoiture.set(modeleVoiture);
        }

        public String getTypeVoiture() {
            return typeVoiture.get();
        }

        public void setTypeVoiture(String typeVoiture) {
            this.typeVoiture.set(typeVoiture);
        }

        @Override
        public String toString() {
            return "Conducteur{" +
                    "id=" + id.get() +
                    ", idVoiture=" + idVoiture.get() +
                    ", disponibilite=" + disponibilite.get() +
                    ", marqueVoiture='" + marqueVoiture.get() + '\'' +
                    ", modeleVoiture='" + modeleVoiture.get() + '\'' +
                    ", typeVoiture='" + typeVoiture.get() + '\'' +
                    ", dateDepart=" + dateDepart +
                    ", dateArret=" + dateArret +
                    '}';
        }
    }





