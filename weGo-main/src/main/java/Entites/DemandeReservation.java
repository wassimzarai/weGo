package Entites;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Représente une demande de réservation faite par un passager
 * Cette entité est utilisée pour le matching intelligent
 */
public class DemandeReservation {
    
    private int id;
    private String villeDepart;
    private String villeArrivee;
    private String pointDepart; // Point précis de départ souhaité
    private String pointArrivee; // Point précis d'arrivée souhaité
    private LocalDate date;
    private LocalTime heureSouhaitee;
    private int nombrePlaces;
    private double prixMaximum;
    private boolean accepteFumeur;
    private boolean accepteAnimaux;
    private boolean besoinPlaceGrossBagages;
    private String commentaires;
    private LocalDate dateCreation;
    private String statut; // En attente, Traitée, Annulée
    
    // Relations
    private Passager passager;
    private Entites.Reservation reservationAssociee; // La réservation créée à partir de cette demande
    
    // Constructeur sans ID
    public DemandeReservation(String villeDepart, String villeArrivee, String pointDepart,
                             String pointArrivee, LocalDate date, LocalTime heureSouhaitee,
                             int nombrePlaces, double prixMaximum, boolean accepteFumeur,
                             boolean accepteAnimaux, boolean besoinPlaceGrossBagages,
                             String commentaires, Passager passager) {
        this.villeDepart = villeDepart;
        this.villeArrivee = villeArrivee;
        this.pointDepart = pointDepart;
        this.pointArrivee = pointArrivee;
        this.date = date;
        this.heureSouhaitee = heureSouhaitee;
        this.nombrePlaces = nombrePlaces;
        this.prixMaximum = prixMaximum;
        this.accepteFumeur = accepteFumeur;
        this.accepteAnimaux = accepteAnimaux;
        this.besoinPlaceGrossBagages = besoinPlaceGrossBagages;
        this.commentaires = commentaires;
        this.dateCreation = LocalDate.now();
        this.statut = "En attente";
        this.passager = passager;
    }
    
    // Constructeur avec ID
    public DemandeReservation(int id, String villeDepart, String villeArrivee, String pointDepart,
                             String pointArrivee, LocalDate date, LocalTime heureSouhaitee,
                             int nombrePlaces, double prixMaximum, boolean accepteFumeur,
                             boolean accepteAnimaux, boolean besoinPlaceGrossBagages,
                             String commentaires, LocalDate dateCreation, String statut) {
        this.id = id;
        this.villeDepart = villeDepart;
        this.villeArrivee = villeArrivee;
        this.pointDepart = pointDepart;
        this.pointArrivee = pointArrivee;
        this.date = date;
        this.heureSouhaitee = heureSouhaitee;
        this.nombrePlaces = nombrePlaces;
        this.prixMaximum = prixMaximum;
        this.accepteFumeur = accepteFumeur;
        this.accepteAnimaux = accepteAnimaux;
        this.besoinPlaceGrossBagages = besoinPlaceGrossBagages;
        this.commentaires = commentaires;
        this.dateCreation = dateCreation;
        this.statut = statut;
    }
    
    // Constructeur vide pour l'instanciation par réflexion
    public DemandeReservation() {
        this.dateCreation = LocalDate.now();
        this.statut = "En attente";
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVilleDepart() {
        return villeDepart;
    }

    public void setVilleDepart(String villeDepart) {
        this.villeDepart = villeDepart;
    }

    public String getVilleArrivee() {
        return villeArrivee;
    }

    public void setVilleArrivee(String villeArrivee) {
        this.villeArrivee = villeArrivee;
    }

    public String getPointDepart() {
        return pointDepart;
    }

    public void setPointDepart(String pointDepart) {
        this.pointDepart = pointDepart;
    }

    public String getPointArrivee() {
        return pointArrivee;
    }

    public void setPointArrivee(String pointArrivee) {
        this.pointArrivee = pointArrivee;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getHeureSouhaitee() {
        return heureSouhaitee;
    }

    public void setHeureSouhaitee(LocalTime heureSouhaitee) {
        this.heureSouhaitee = heureSouhaitee;
    }

    public int getNombrePlaces() {
        return nombrePlaces;
    }

    public void setNombrePlaces(int nombrePlaces) {
        this.nombrePlaces = nombrePlaces;
    }

    public double getPrixMaximum() {
        return prixMaximum;
    }

    public void setPrixMaximum(double prixMaximum) {
        this.prixMaximum = prixMaximum;
    }

    public boolean isAccepteFumeur() {
        return accepteFumeur;
    }

    public void setAccepteFumeur(boolean accepteFumeur) {
        this.accepteFumeur = accepteFumeur;
    }

    public boolean isAccepteAnimaux() {
        return accepteAnimaux;
    }

    public void setAccepteAnimaux(boolean accepteAnimaux) {
        this.accepteAnimaux = accepteAnimaux;
    }

    public boolean isBesoinPlaceGrossBagages() {
        return besoinPlaceGrossBagages;
    }

    public void setBesoinPlaceGrossBagages(boolean besoinPlaceGrossBagages) {
        this.besoinPlaceGrossBagages = besoinPlaceGrossBagages;
    }

    public String getCommentaires() {
        return commentaires;
    }

    public void setCommentaires(String commentaires) {
        this.commentaires = commentaires;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Passager getPassager() {
        return passager;
    }

    public void setPassager(Passager passager) {
        this.passager = passager;
    }

    public Entites.Reservation getReservationAssociee() {
        return reservationAssociee;
    }


    
    /**
     * Vérifie si la demande est compatible avec un trajet donné
     * @param trajet Le trajet à vérifier
     * @return true si la demande est compatible avec le trajet
     */
    public boolean estCompatibleAvec(trajet1 trajet) {
        // Vérifier les critères de base
        if (!trajet1.getVilleDepart().equalsIgnoreCase(this.villeDepart) ||
            !trajet1.getVilleArrivee().equalsIgnoreCase(this.villeArrivee) ||
            !trajet.getDate().equals(this.date) ||
            trajet.getNombrePlacesRestantes() < this.nombrePlaces ||
            trajet1.getPrix() > this.prixMaximum) {
            return false;
        }
        
        // Vérifier les préférences
        if (this.besoinPlaceGrossBagages && !trajet1.isAutoriseGrossBagages()) {
            return false;
        }
        
        if (!this.accepteFumeur && trajet1.isAutoriseFumeur()) {
            return false;
        }
        
        if (!this.accepteAnimaux && trajet1.isAutoriseAnimaux()) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        return "DemandeReservation{" +
                "id=" + id +
                ", villeDepart='" + villeDepart + '\'' +
                ", villeArrivee='" + villeArrivee + '\'' +
                ", date=" + date +
                ", heureSouhaitee=" + heureSouhaitee +
                ", nombrePlaces=" + nombrePlaces +
                ", statut='" + statut + '\'' +
                ", passager=" + (passager != null ? passager.getNom() : "Non assigné") +
                '}';
    }

    public void setReservationAssociee(Reservation reservation) {
    }
}