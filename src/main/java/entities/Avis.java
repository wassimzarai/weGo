package entities;

import javafx.beans.property.*;

public class Avis {
    private final IntegerProperty id;
    private final StringProperty rating;
    private final StringProperty comment;
    private final ObjectProperty<User> user;
    private final ObjectProperty<java.sql.Date> date;

    public Avis() {
        this.id = new SimpleIntegerProperty(this, "id");
        this.rating = new SimpleStringProperty(this, "rating");
        this.comment = new SimpleStringProperty(this, "comment");
        this.user = new SimpleObjectProperty<>(this, "user");
        this.date = new SimpleObjectProperty<>(this, "date");
    }

    public Avis(String rating, String comment, User user) {
        this();
        this.rating.set(rating);
        this.comment.set(comment);
        this.user.set(user);
        this.date.set(new java.sql.Date(System.currentTimeMillis()));
    }

    // ID
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // Rating
    public String getRating() {
        return rating.get();
    }

    public void setRating(String rating) {
        this.rating.set(rating);
    }

    public StringProperty ratingProperty() {
        return rating;
    }

    // Comment
    public String getComment() {
        return comment.get();
    }

    public void setComment(String comment) {
        this.comment.set(comment);
    }

    public StringProperty commentProperty() {
        return comment;
    }

    // User
    public User getUser() {
        return user.get();
    }

    public void setUser(User user) {
        this.user.set(user);
    }

    public ObjectProperty<User> userProperty() {
        return user;
    }

    // Date
    public java.sql.Date getDate() {
        return date.get();
    }

    public void setDate(java.sql.Date date) {
        this.date.set(date);
    }

    public ObjectProperty<java.sql.Date> dateProperty() {
        return date;
    }

    @Override
    public String toString() {
        return String.format("Avis{id=%d, rating='%s', comment='%s', user=%s, date=%s}",
                getId(), getRating(), getComment(), getUser(), getDate());
    }
} 