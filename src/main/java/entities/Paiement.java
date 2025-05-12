package entities;

public class Paiement {
    private int id;
    private int reservationId;
    private double montant;
    private String datePaiement;
    private String methodePaiement;
    private String statut;
    private String email;  // Nouvel attribut pour l'email

    // Constructeur avec ID et email
    public Paiement(int id, int reservationId, double montant, String datePaiement, String methodePaiement, String statut, String email) {
        this.id = id;
        this.reservationId = reservationId;
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.methodePaiement = methodePaiement;
        this.statut = statut;
        this.email = email;
    }

    // Constructeur sans ID, avec email
    public Paiement(int reservationId, double montant, String datePaiement, String methodePaiement, String statut, String email) {
        this.reservationId = reservationId;
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.methodePaiement = methodePaiement;
        this.statut = statut;
        this.email = email;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public String getDatePaiement() { return datePaiement; }
    public void setDatePaiement(String datePaiement) { this.datePaiement = datePaiement; }

    public String getMethodePaiement() { return methodePaiement; }
    public void setMethodePaiement(String methodePaiement) { this.methodePaiement = methodePaiement; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getEmail() { return email; }  // Getter pour l'email
    public void setEmail(String email) { this.email = email; }  // Setter pour l'email

    @Override
    public String toString() {
        return "Paiement{" +
                "id=" + id +
                ", reservationId=" + reservationId +
                ", montant=" + montant +
                ", datePaiement='" + datePaiement + '\'' +
                ", methodePaiement='" + methodePaiement + '\'' +
                ", statut='" + statut + '\'' +
                ", email='" + email + '\'' +  // Ajout de l'email dans la toString
                '}';
    }
}
