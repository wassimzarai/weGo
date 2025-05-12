package entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Reservation {

    private int id;
    private String nomPassager;
    private LocalDate dateReservation;
    private String pointDepart;
    private String pointArrivee;
    private String statut;
    private String typeTrajet;
    private String commentaire;
    
    // Relations
    private List<Abonnement> abonnements = new ArrayList<>();
    private Trajet trajet; // Le trajet associé à cette réservation
    private Passager passager; // Le passager qui a fait cette réservation
    private DemandeReservation demandeSource; // La demande qui a généré cette réservation

    // Constructeur sans ID (utilisé lors de l'ajout)
    public Reservation(String nomPassager, LocalDate dateReservation, String pointDepart, String pointArrivee, String statut, String typeTrajet, String commentaire) {
        this.nomPassager = nomPassager;
        this.dateReservation = dateReservation;
        this.pointDepart = pointDepart;
        this.pointArrivee = pointArrivee;
        this.statut = statut;
        this.typeTrajet = typeTrajet;
        this.commentaire = commentaire;
    }
    
    // Constructeur complet pour la création intelligente
    public Reservation(String nomPassager, LocalDate dateReservation, String pointDepart, String pointArrivee, 
                      String statut, String typeTrajet, String commentaire, Trajet trajet, Passager passager) {
        this.nomPassager = nomPassager;
        this.dateReservation = dateReservation;
        this.pointDepart = pointDepart;
        this.pointArrivee = pointArrivee;
        this.statut = statut;
        this.typeTrajet = typeTrajet;
        this.commentaire = commentaire;
        this.trajet = trajet;
        this.passager = passager;
        
        // Établir les relations bidirectionnelles
        if (trajet != null) {
            trajet.ajouterReservation(this);
        }
        if (passager != null) {
            passager.ajouterReservation(this);
        }
    }

    // Constructeur avec ID (utilisé après la récupération de la réservation)
    public Reservation(int id, String nomPassager, LocalDate dateReservation, String pointDepart, String pointArrivee, String statut, String typeTrajet, String commentaire) {
        this.id = id;
        this.nomPassager = nomPassager;
        this.dateReservation = dateReservation;
        this.pointDepart = pointDepart;
        this.pointArrivee = pointArrivee;
        this.statut = statut;
        this.typeTrajet = typeTrajet;
        this.commentaire = commentaire;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomPassager() {
        return nomPassager;
    }

    public void setNomPassager(String nomPassager) {
        this.nomPassager = nomPassager;
    }

    public LocalDate getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDate dateReservation) {
        this.dateReservation = dateReservation;
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

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getTypeTrajet() {
        return typeTrajet;
    }

    public void setTypeTrajet(String typeTrajet) {
        this.typeTrajet = typeTrajet;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
    
    // Méthodes pour gérer les abonnements
    public List<Abonnement> getAbonnements() {
        return abonnements;
    }
    
    public void setAbonnements(List<Abonnement> abonnements) {
        this.abonnements = abonnements;
    }
    
    /**
     * Ajoute un abonnement à cette réservation et établit la relation bidirectionnelle
     * @param abonnement L'abonnement à ajouter
     */
    public void addAbonnement(Abonnement abonnement) {
        if (abonnement != null) {
            this.abonnements.add(abonnement);
            abonnement.setReservation(this);
        }
    }
    
    /**
     * Supprime un abonnement de cette réservation
     * @param abonnement L'abonnement à supprimer
     */
    public void removeAbonnement(Abonnement abonnement) {
        if (abonnement != null && this.abonnements.contains(abonnement)) {
            this.abonnements.remove(abonnement);
            if (abonnement.getReservation() == this) {
                abonnement.setReservation(null);
            }
        }
    }
    
    /**
     * Vérifie si cette réservation a un abonnement actif
     * @return true si un abonnement actif existe, false sinon
     */
    public boolean hasActiveAbonnement() {
        LocalDate today = LocalDate.now();
        return abonnements.stream()
            .anyMatch(a -> "Actif".equals(a.getStatut()) && 
                     (a.getDateFin() == null || !today.isAfter(a.getDateFin())));
    }
    
    // Getters et setters pour les nouvelles relations
    public Trajet getTrajet() {
        return trajet;
    }

    public void setTrajet(Trajet trajet) {
        // Supprimer la réservation de l'ancien trajet si elle existe
        if (this.trajet != null && this.trajet != trajet) {
            this.trajet.retirerReservation(this);
        }
        
        this.trajet = trajet;
        
        // Ajouter la réservation au nouveau trajet
        if (trajet != null) {
            trajet.ajouterReservation(this);
        }
    }

    public Passager getPassager() {
        return passager;
    }

    public void setPassager(Passager passager) {
        this.passager = passager;
        
        // Mettre à jour le nom du passager pour la cohérence
        if (passager != null) {
            this.nomPassager = passager.getNomComplet();
        }
    }

    public DemandeReservation getDemandeSource() {
        return demandeSource;
    }

    public void setDemandeSource(DemandeReservation demandeSource) {
        this.demandeSource = demandeSource;
        
        // Établir la relation bidirectionnelle
        if (demandeSource != null) {
            demandeSource.setReservationAssociee(this);
        }
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", nomPassager='" + nomPassager + '\'' +
                ", dateReservation=" + dateReservation +
                ", pointDepart='" + pointDepart + '\'' +
                ", pointArrivee='" + pointArrivee + '\'' +
                ", statut='" + statut + '\'' +
                ", typeTrajet='" + typeTrajet + '\'' +
                ", nombreAbonnements=" + (abonnements != null ? abonnements.size() : 0) +
                (trajet != null ? ", trajet ID=" + trajet.getId() : "") +
                '}';
    }

    // Méthodes alias pour compatibilité
    public String getNom() {
        return nomPassager;
    }

    public String getDepart() {
        return pointDepart;
    }

    public String getArrivee() {
        return pointArrivee;
    }

    public String getType() {
        return typeTrajet;
    }

    public LocalDate getDate() {
        return dateReservation;
    }
    
    // Setters alias pour compatibilité
    public void setNom(String nom) {
        this.nomPassager = nom;
    }
    
    public void setDepart(String depart) {
        this.pointDepart = depart;
    }
    
    public void setArrivee(String arrivee) {
        this.pointArrivee = arrivee;
    }
    
    public void setType(String type) {
        this.typeTrajet = type;
    }
    
    public void setDate(LocalDate date) {
        this.dateReservation = date;
    }
}
