package Entites;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un conducteur qui propose des trajets
 */
public class Conducteur {
    
    private int id;
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private double noteMoyenne;
    private int nombreTrajetEffectues;
    private String numeroPermis;
    private String vehicule;
    private String immatriculation;
    
    // Relations
    private List<Trajet> trajetsOfferts = new ArrayList<>();
    
    // Constructeur sans ID
    public Conducteur(String nom, String prenom, String telephone, String email, 
                       String numeroPermis, String vehicule, String immatriculation) {
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.email = email;
        this.numeroPermis = numeroPermis;
        this.vehicule = vehicule;
        this.immatriculation = immatriculation;
        this.noteMoyenne = 0.0;
        this.nombreTrajetEffectues = 0;
    }
    
    // Constructeur avec ID
    public Conducteur(int id, String nom, String prenom, String telephone, String email, 
                      double noteMoyenne, int nombreTrajetEffectues, String numeroPermis, 
                      String vehicule, String immatriculation) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.email = email;
        this.noteMoyenne = noteMoyenne;
        this.nombreTrajetEffectues = nombreTrajetEffectues;
        this.numeroPermis = numeroPermis;
        this.vehicule = vehicule;
        this.immatriculation = immatriculation;
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
    
    public double getNoteMoyenne() {
        return noteMoyenne;
    }
    
    public void setNoteMoyenne(double noteMoyenne) {
        this.noteMoyenne = noteMoyenne;
    }
    
    public int getNombreTrajetEffectues() {
        return nombreTrajetEffectues;
    }
    
    public void setNombreTrajetEffectues(int nombreTrajetEffectues) {
        this.nombreTrajetEffectues = nombreTrajetEffectues;
    }
    
    public String getNumeroPermis() {
        return numeroPermis;
    }
    
    public void setNumeroPermis(String numeroPermis) {
        this.numeroPermis = numeroPermis;
    }
    
    public String getVehicule() {
        return vehicule;
    }
    
    public void setVehicule(String vehicule) {
        this.vehicule = vehicule;
    }
    
    public String getImmatriculation() {
        return immatriculation;
    }
    
    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }
    
    // Méthodes pour gérer les trajets
    public List<Trajet> getTrajetsOfferts() {
        return trajetsOfferts;
    }
    
    public void setTrajetsOfferts(List<Trajet> trajetsOfferts) {
        this.trajetsOfferts = trajetsOfferts;
    }
    
    public void ajouterTrajet(Trajet trajet) {
        if (trajet != null) {
            this.trajetsOfferts.add(trajet);
            trajet.setConducteur(this);
        }
    }
    
    public void retirerTrajet(Trajet trajet) {
        if (trajet != null && this.trajetsOfferts.contains(trajet)) {
            this.trajetsOfferts.remove(trajet);
            if (trajet.getConducteur().equals(this)) {
                trajet.setConducteur(null);
            }
        }
    }
    
    // Méthode pour mettre à jour la note moyenne
    public void ajouterNote(double nouvelleNote) {
        // Calcul de la nouvelle moyenne
        double sommeNotes = this.noteMoyenne * this.nombreTrajetEffectues;
        sommeNotes += nouvelleNote;
        this.nombreTrajetEffectues++;
        this.noteMoyenne = sommeNotes / this.nombreTrajetEffectues;
    }
    
    // Méthode pour le nom complet
    public String getNomComplet() {
        return this.prenom + " " + this.nom;
    }
    
    @Override
    public String toString() {
        return "Conducteur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", noteMoyenne=" + noteMoyenne +
                ", nombreTrajetEffectues=" + nombreTrajetEffectues +
                ", vehicule='" + vehicule + '\'' +
                '}';
    }
} 