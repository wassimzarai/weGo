package entities;

import javafx.beans.property.*;

public class User {
    private IntegerProperty id;
    private StringProperty username;
    private StringProperty email;
    private StringProperty password;
    private StringProperty role;
    private StringProperty image;
    private String gender;
    private String date;
    
    // Nouveaux champs pour le profil
    private StringProperty nom;
    private StringProperty prenom;
    private StringProperty telephone;

    public User() {
        this.id = new SimpleIntegerProperty();
        this.username = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
        this.password = new SimpleStringProperty();
        this.role = new SimpleStringProperty();
        this.image = new SimpleStringProperty("default.jpg");
        this.nom = new SimpleStringProperty();
        this.prenom = new SimpleStringProperty();
        this.telephone = new SimpleStringProperty();
    }

    public User(String username, String password, String email, String image, String role, String gender, String date) {
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.email = new SimpleStringProperty(email);
        this.image = new SimpleStringProperty(image);
        this.role = new SimpleStringProperty(role);
        this.gender = gender;
        this.date = date;
        this.nom = new SimpleStringProperty();
        this.prenom = new SimpleStringProperty();
        this.telephone = new SimpleStringProperty();
    }
    
    public User(String username, String password, String email, String image, String role, 
                String gender, String date, String nom, String prenom, String telephone) {
        this(username, password, email, image, role, gender, date);
        this.nom.set(nom);
        this.prenom.set(prenom);
        this.telephone.set(telephone);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getPassword() {
        return password.get();
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public String getRole() {
        return role.get();
    }

    public StringProperty roleProperty() {
        return role;
    }

    public void setRole(String role) {
        this.role.set(role);
    }

    public String getImage() {
        return image.get();
    }

    public StringProperty imageProperty() {
        return image;
    }

    public void setImage(String image) {
        this.image.set(image);
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    
    public String getNom() {
        return nom.get();
    }

    public StringProperty nomProperty() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom.set(nom);
    }

    public String getPrenom() {
        return prenom.get();
    }

    public StringProperty prenomProperty() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom.set(prenom);
    }

    public String getTelephone() {
        return telephone.get();
    }

    public StringProperty telephoneProperty() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone.set(telephone);
    }

    @Override
    public String toString() {
        return username.get();
    }
} 