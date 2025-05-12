package entities;

public class Facture {
    private int idFacture;      // Identifiant unique de la facture
    private int reservationId; // Clé étrangère vers la table Réservation
    private int paiementId;     // Clé étrangère vers la table Paiement
    private double montantTotal;
    private double montantPaye;
    private String dateEmission;
    private String description;
    public Facture() {} // Obligatoire pour JPA
    // Constructeur avec ID (pour les modifications)
    public Facture(int idFacture, int reservationId, int paiementId, double montantTotal,
                   double montantPaye, String dateEmission, String description) {
        this.idFacture = idFacture;
        this.reservationId = reservationId;
        this.paiementId = paiementId;
        this.montantTotal = montantTotal;
        this.montantPaye = montantPaye;
        this.dateEmission = dateEmission;
        this.description = description;
    }

    // Constructeur sans ID (pour les insertions)
    public Facture(int reservationId, int paiementId, double montantTotal,
                   double montantPaye, String dateEmission, String description) {
        this.reservationId = reservationId;
        this.paiementId = paiementId;
        this.montantTotal = montantTotal;
        this.montantPaye = montantPaye;
        this.dateEmission = dateEmission;
        this.description = description;
    }

    // Getters et Setters
    public int getIdFacture() { return idFacture; }
    public void setIdFacture(int idFacture) { this.idFacture = idFacture; }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public int getPaiementId() { return paiementId; }
    public void setPaiementId(int paiementId) { this.paiementId = paiementId; }

    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }

    public double getMontantPaye() { return montantPaye; }
    public void setMontantPaye(double montantPaye) { this.montantPaye = montantPaye; }

    public String getDateEmission() { return dateEmission; }
    public void setDateEmission(String dateEmission) { this.dateEmission = dateEmission; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Facture{" +
                "idFacture=" + idFacture +
                ", reservationId=" + reservationId +
                ", paiementId=" + paiementId +
                ", montantTotal=" + montantTotal +
                ", montantPaye=" + montantPaye +
                ", dateEmission='" + dateEmission + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}