package Entites;

import java.time.LocalDate;

public class Reponse {
    private int id;
    private String message;
    private LocalDate dateReponse;
    private Reclamation reclamation;
    private utilisateur admin;

    public Reponse() {
    }

    public Reponse(String message, LocalDate dateReponse, Reclamation reclamation, utilisateur admin) {
        this.message = message;
        this.dateReponse = dateReponse;
        this.reclamation = reclamation;
        this.admin = admin;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDate getDateReponse() {
        return dateReponse;
    }

    public void setDateReponse(LocalDate dateReponse) {
        this.dateReponse = dateReponse;
    }

    public Reclamation getReclamation() {
        return reclamation;
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
    }

    public utilisateur getAdmin() {
        return admin;
    }

    public void setAdmin(utilisateur admin) {
        this.admin = admin;
    }

    @Override
    public String toString() {
        return "Reponse{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", dateReponse=" + dateReponse +
                ", admin=" + (admin != null ? admin.getUsername() : "null") +
                '}';
    }
} 