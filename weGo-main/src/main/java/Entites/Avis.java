package Entites;

import java.sql.Date;

public class Avis {
    private int id;
    private String comment;
    private String rating;
    private Date date;
    private utilisateur user;

    public Avis() {
        // Initialiser les champs avec des valeurs par défaut
        this.comment = "";
        this.rating = "0";
        
        // Initialiser la date avec la date actuelle
        java.util.Date utilDate = new java.util.Date();
        this.date = new java.sql.Date(utilDate.getTime());
    }

    public Avis(int id, String comment, String rating, Date date, utilisateur user) {
        this.id = id;
        this.comment = comment != null ? comment : "";
        this.rating = rating != null ? rating : "0";
        this.date = date != null ? date : new java.sql.Date(System.currentTimeMillis());
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment != null ? comment : "";
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating != null ? rating : "0";
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date != null ? date : new java.sql.Date(System.currentTimeMillis());
    }

    public utilisateur getUser() {
        return user;
    }

    public void setUser(utilisateur user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Avis{" +
                "id=" + id +
                ", commentaire='" + comment + '\'' +
                ", note='" + rating + '\'' +
                ", date=" + date +
                ", utilisateur=" + (user != null ? user.getUsername() : "null") +
                '}';
    }
} 