package models;

/**
 * Classe qui représente une requête pour une réservation automatique
 */
public class ReservationAutomatiqueRequest {
    
    private int passagerId;
    private String villeDepart;
    private String villeArrivee;
    private int nombrePlaces;
    private boolean utiliserItineraireHabituel;
    
    // Constructeur par défaut requis pour le parsing JSON
    public ReservationAutomatiqueRequest() {
    }
    
    // Constructeur complet
    public ReservationAutomatiqueRequest(int passagerId, String villeDepart, String villeArrivee, 
                                        int nombrePlaces, boolean utiliserItineraireHabituel) {
        this.passagerId = passagerId;
        this.villeDepart = villeDepart;
        this.villeArrivee = villeArrivee;
        this.nombrePlaces = nombrePlaces;
        this.utiliserItineraireHabituel = utiliserItineraireHabituel;
    }
    
    // Getters et setters
    public int getPassagerId() {
        return passagerId;
    }
    
    public void setPassagerId(int passagerId) {
        this.passagerId = passagerId;
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
    
    public int getNombrePlaces() {
        return nombrePlaces;
    }
    
    public void setNombrePlaces(int nombrePlaces) {
        this.nombrePlaces = nombrePlaces;
    }
    
    public boolean isUtiliserItineraireHabituel() {
        return utiliserItineraireHabituel;
    }
    
    public void setUtiliserItineraireHabituel(boolean utiliserItineraireHabituel) {
        this.utiliserItineraireHabituel = utiliserItineraireHabituel;
    }
} 