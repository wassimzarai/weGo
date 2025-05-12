package services;

import entities.Avis;
import entities.User;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AvisService {
    private final Connection connection;
    private final UserService userService;

    public AvisService() {
        connection = MyConnection.getInstance().getConnection();
        userService = new UserService();
    }

    public void ajouter(Avis avis) throws SQLException {
        String query = "INSERT INTO avis (commentaire, note, date, id_utilisateur) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, avis.getComment());
            ps.setString(2, avis.getRating());
            ps.setDate(3, avis.getDate());
            ps.setInt(4, avis.getUser().getId());
            
            int result = ps.executeUpdate();
            System.out.println("Rows affected: " + result);
            
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                avis.setId(generatedKeys.getInt(1));
                System.out.println("Generated ID: " + avis.getId());
            }
        } catch (SQLException e) {
            System.err.println("Error details: " + e.getMessage());
            throw e;
        }
    }

    public void modifier(Avis avis) throws SQLException {
        String query = "UPDATE avis SET note = ?, commentaire = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, avis.getRating());
            ps.setString(2, avis.getComment());
            ps.setInt(3, avis.getId());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM avis WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Avis> getAll() throws SQLException {
        List<Avis> avisList = new ArrayList<>();
        String query = "SELECT * FROM avis";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Avis avis = new Avis();
                avis.setId(rs.getInt("id"));
                avis.setRating(rs.getString("note"));
                avis.setComment(rs.getString("commentaire"));
                avis.setDate(rs.getDate("date"));
                
                User user = userService.getById(rs.getInt("id_utilisateur"));
                avis.setUser(user);
                
                avisList.add(avis);
            }
        }
        return avisList;
    }

    public Avis getById(int id) throws SQLException {
        String query = "SELECT * FROM avis WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Avis avis = new Avis();
                avis.setId(rs.getInt("id"));
                avis.setRating(rs.getString("note"));
                avis.setComment(rs.getString("commentaire"));
                avis.setDate(rs.getDate("date"));
                
                User user = userService.getById(rs.getInt("id_utilisateur"));
                avis.setUser(user);
                
                return avis;
            }
        }
        return null;
    }

    public List<Avis> getByUser(int userId) throws SQLException {
        List<Avis> userAvisList = new ArrayList<>();
        String query = "SELECT * FROM avis WHERE id_utilisateur = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Avis avis = new Avis();
                avis.setId(rs.getInt("id"));
                avis.setRating(rs.getString("note"));
                avis.setComment(rs.getString("commentaire"));
                avis.setDate(rs.getDate("date"));
                
                User user = userService.getById(userId);
                avis.setUser(user);
                
                userAvisList.add(avis);
            }
        }
        return userAvisList;
    }
} 