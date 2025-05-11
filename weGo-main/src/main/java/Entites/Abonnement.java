package Entites;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Abonnement {

    private int idAbonnement;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int reservationId;
    private BigDecimal montant;
    private String typeAbonnement;
    private String statut;
    private String remarques;
    
    // Relation avec Reservation (non persistée mais utilisée en mémoire)
    private Entites.Reservation reservation;

    // Constructeur sans ID (utilisé lors de l'ajout)
    public Abonnement(LocalDate dateDebut, LocalDate dateFin, int reservationId, 
                      BigDecimal montant, String typeAbonnement, String statut, String remarques) {
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.reservationId = reservationId;
        this.montant = montant;
        this.typeAbonnement = typeAbonnement;
        this.statut = statut;
        this.remarques = remarques;
    }

    // Constructeur avec ID (utilisé après la récupération de l'abonnement)
    public Abonnement(int idAbonnement, LocalDate dateDebut, LocalDate dateFin, int reservationId, 
                      BigDecimal montant, String typeAbonnement, String statut, String remarques) {
        this.idAbonnement = idAbonnement;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.reservationId = reservationId;
        this.montant = montant;
        this.typeAbonnement = typeAbonnement;
        this.statut = statut;
        this.remarques = remarques;
    }

    public Abonnement() {

    }

    // Getters et Setters
    public int getIdAbonnement() {
        return idAbonnement;
    }

    public void setIdAbonnement(int idAbonnement) {
        this.idAbonnement = idAbonnement;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getTypeAbonnement() {
        return typeAbonnement;
    }

    public void setTypeAbonnement(String typeAbonnement) {
        this.typeAbonnement = typeAbonnement;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getRemarques() {
        return remarques;
    }

    public void setRemarques(String remarques) {
        this.remarques = remarques;
    }
    
    public Entites.Reservation getReservation() {
        return reservation;
    }
    
    public void setReservation(Entites.Reservation reservation) {
        this.reservation = reservation;
        if (reservation != null) {
            this.reservationId = reservation.getId();
        }
    }

    @Override
    public String toString() {
        return "Abonnement{" +
                "idAbonnement=" + idAbonnement +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", reservationId=" + reservationId +
                ", montant=" + montant +
                ", typeAbonnement='" + typeAbonnement + '\'' +
                ", statut='" + statut + '\'' +
                ", remarques='" + remarques + '\'' +
                '}';
    }

    public void setType(String type) {
    }


} 