package entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un passager qui peut faire des demandes de réservation
 */
public class Passager {
    
    private int id;
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private LocalDate dateInscription;
    private double noteMoyenne;
    private int nombreTrajetsEffectues;
    
    // Relations
    private List<DemandeReservation> demandesReservation = new ArrayList<>();
    private List<Reservation> reservations = new ArrayList<>();
    
    // Constructeur sans ID
    public Passager(String nom, String prenom, String telephone, String email) {
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.email = email;
        this.dateInscription = LocalDate.now();
        this.noteMoyenne = 0.0;
        this.nombreTrajetsEffectues = 0;
    }
    
    // Constructeur avec ID
    public Passager(int id, String nom, String prenom, String telephone, String email, 
                   LocalDate dateInscription, double noteMoyenne, int nombreTrajetsEffectues) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.email = email;
        this.dateInscription = dateInscription;
        this.noteMoyenne = noteMoyenne;
        this.nombreTrajetsEffectues = nombreTrajetsEffectues;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }

    public double getNoteMoyenne() {
        return noteMoyenne;
    }

    public void setNoteMoyenne(double noteMoyenne) {
        this.noteMoyenne = noteMoyenne;
    }

    public int getNombreTrajetsEffectues() {
        return nombreTrajetsEffectues;
    }

    public void setNombreTrajetsEffectues(int nombreTrajetsEffectues) {
        this.nombreTrajetsEffectues = nombreTrajetsEffectues;
    }
    
    // Méthodes pour gérer les demandes de réservation
    public List<DemandeReservation> getDemandesReservation() {
        return demandesReservation;
    }
    
    public void setDemandesReservation(List<DemandeReservation> demandesReservation) {
        this.demandesReservation = demandesReservation;
    }
    
    public void ajouterDemandeReservation(DemandeReservation demande) {
        if (demande != null) {
            this.demandesReservation.add(demande);
            demande.setPassager(this);
        }
    }
    
    // Méthodes pour gérer les réservations
    public List<Reservation> getReservations() {
        return reservations;
    }
    
    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
    
    public void ajouterReservation(Reservation reservation) {
        if (reservation != null) {
            this.reservations.add(reservation);
            // association implicite avec le passager via le nomPassager dans Reservation
        }
    }
    
    // Méthode pour mettre à jour la note moyenne
    public void ajouterNote(double nouvelleNote) {
        // Calcul de la nouvelle moyenne
        double sommeNotes = this.noteMoyenne * this.nombreTrajetsEffectues;
        sommeNotes += nouvelleNote;
        this.nombreTrajetsEffectues++;
        this.noteMoyenne = sommeNotes / this.nombreTrajetsEffectues;
    }
    
    // Méthode pour le nom complet
    public String getNomComplet() {
        return this.prenom + " " + this.nom;
    }
    
    @Override
    public String toString() {
        return "Passager{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", nombreTrajetsEffectues=" + nombreTrajetsEffectues +
                ", noteMoyenne=" + noteMoyenne +
                '}';
    }
} 