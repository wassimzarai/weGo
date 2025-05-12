package entities;

import java.time.LocalDate;

public class Reponse {
    private int id;
    private String message;
    private LocalDate dateReponse;
    private Reclamation reclamation;
    private User admin;

    public Reponse() {
    }

    public Reponse(String message, LocalDate dateReponse, Reclamation reclamation, User admin) {
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

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
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