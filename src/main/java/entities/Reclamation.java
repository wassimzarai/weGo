package entities;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Reclamation {
    private int id;
    private String titre;
    private String description;
    private LocalDate date;
    private User user;
    private String statut;
    private List<Reponse> reponses;
    
    // Nouveaux champs pour la géolocalisation
    private double latitude;
    private double longitude;
    private String adresse;
    
    // Ticket système
    private String ticketId;
    private int priorite; // 1-5, 5 étant le plus urgent
    private String categorie;
    
    // Champs pour l'analyse IA
    private String gravite; // "basse", "moyenne", "élevée", "critique"
    private boolean notificationsEnvoyees;

    public Reclamation() {
        this.reponses = new ArrayList<>();
        this.statut = "ouvert"; // Nouveau statut initial
        this.ticketId = generateTicketId();
        this.priorite = 3; // Priorité moyenne par défaut
        this.notificationsEnvoyees = false;
    }

    public Reclamation(String titre, String description, LocalDate date, User user) {
        this();
        this.titre = titre;
        this.description = description;
        this.date = date;
        this.user = user;
    }
    
    public Reclamation(String titre, String description, LocalDate date, User user, 
                      double latitude, double longitude, String adresse) {
        this(titre, description, date, user);
        this.latitude = latitude;
        this.longitude = longitude;
        this.adresse = adresse;
    }

    public String generateTicketId() {
        // Format: année-mois-4 premiers caractères UUID
        String uuid = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return String.format("%d-%02d-%s", 
                            LocalDate.now().getYear(), 
                            LocalDate.now().getMonthValue(),
                            uuid);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        if (statut.equals("ouvert") || statut.equals("en cours") || statut.equals("resolu") || statut.equals("rejete")) {
            this.statut = statut;
        } else {
            throw new IllegalArgumentException("Statut must be 'ouvert', 'en cours', 'resolu', or 'rejete'");
        }
    }

    public List<Reponse> getReponses() {
        return reponses;
    }

    public void setReponses(List<Reponse> reponses) {
        this.reponses = reponses;
    }
    
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public int getPriorite() {
        return priorite;
    }

    public void setPriorite(int priorite) {
        if (priorite >= 1 && priorite <= 5) {
            this.priorite = priorite;
        } else {
            throw new IllegalArgumentException("Priorité must be between 1 and 5");
        }
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getGravite() {
        return gravite;
    }

    public void setGravite(String gravite) {
        if (gravite == null) {
            // Set a default value if null is provided
            this.gravite = "moyenne";
        } else if (gravite.equals("basse") || gravite.equals("moyenne") || 
            gravite.equals("élevée") || gravite.equals("critique")) {
            this.gravite = gravite;
        } else {
            throw new IllegalArgumentException("Gravité must be 'basse', 'moyenne', 'élevée', or 'critique'");
        }
    }

    public boolean isNotificationsEnvoyees() {
        return notificationsEnvoyees;
    }

    public void setNotificationsEnvoyees(boolean notificationsEnvoyees) {
        this.notificationsEnvoyees = notificationsEnvoyees;
    }

    @Override
    public String toString() {
        return "Reclamation{" +
                "id=" + id +
                ", ticketId='" + ticketId + '\'' +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", statut='" + statut + '\'' +
                ", priorite=" + priorite +
                ", categorie='" + categorie + '\'' +
                ", gravite='" + gravite + '\'' +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", coordonnées=[" + latitude + ", " + longitude + "]" +
                '}';
    }
}