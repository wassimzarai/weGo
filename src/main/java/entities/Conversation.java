package entities;

import java.time.LocalDateTime;
import java.util.List;

public class Conversation {
    private int id;
    private int tripId;
    private int passengerId;
    private int driverId;
    private LocalDateTime createdAt;
    private List<Message> messages;
    private String passengerName;
    private String driverName;
    private int unreadCount;

    public Conversation() {
        this.createdAt = LocalDateTime.now();
        this.unreadCount = 0;
    }

    public Conversation(int tripId, int passengerId, int driverId, String passengerName, String driverName) {
        this();
        this.tripId = tripId;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.passengerName = passengerName;
        this.driverName = driverName;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void incrementUnreadCount() {
        this.unreadCount++;
    }

    public void resetUnreadCount() {
        this.unreadCount = 0;
    }
} 