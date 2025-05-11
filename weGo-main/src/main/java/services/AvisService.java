package services;

import Entites.Avis;
import Entites.utilisateur;
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
        if (avis.getUser() == null) {
            utilisateur defaultUser = new utilisateur();
            defaultUser.setId(1);
            avis.setUser(defaultUser);
            System.out.println("Utilisation d'un utilisateur par défaut (ID=1) pour l'avis");
        }
        
        ensureAvisTableExists();
        
        String query = "INSERT INTO avis (commentaire, note, date, id_utilisateur) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, avis.getComment() != null ? avis.getComment() : "");
            ps.setString(2, avis.getRating() != null ? avis.getRating() : "0");
            
            if (avis.getDate() == null) {
                java.util.Date utilDate = new java.util.Date();
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                avis.setDate(sqlDate);
            }
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
            
            if (e.getMessage().contains("Field 'id' doesn't have a default value") || 
                e.getMessage().contains("doesn't exist") || 
                e.getMessage().contains("no such table") ||
                e.getMessage().contains("Multiple primary key")) {
                
                System.out.println("Problème détecté avec la table avis. Tentative de recréation...");
                createAvisTable();
                
                try {
                    PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, avis.getComment() != null ? avis.getComment() : "");
                    ps.setString(2, avis.getRating() != null ? avis.getRating() : "0");
                    ps.setDate(3, avis.getDate());
                    ps.setInt(4, avis.getUser().getId());
                    
                    ps.executeUpdate();
                    
                    ResultSet generatedKeys = ps.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        avis.setId(generatedKeys.getInt(1));
                    }
                    
                    ps.close();
                    System.out.println("Avis ajouté après correction de la structure de la table");
                } catch (SQLException ex) {
                    System.err.println("Échec de la seconde tentative: " + ex.getMessage());
                    throw ex;
                }
            } else {
                throw e;
            }
        }
    }

    private void ensureAvisTableExists() {
        try {
            Statement stmt = connection.createStatement();
            try {
                ResultSet rs = stmt.executeQuery("SELECT 1 FROM avis LIMIT 1");
                rs.close();
            } catch (SQLException e) {
                if (e.getMessage().contains("doesn't exist") || e.getMessage().contains("no such table")) {
                    createAvisTable();
                }
            } finally {
                stmt.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de la table avis: " + e.getMessage());
        }
    }

    private void createAvisTable() {
        try {
            Statement stmt = connection.createStatement();
            
            try {
                stmt.executeUpdate("SET FOREIGN_KEY_CHECKS=0");
                
                stmt.executeUpdate("DROP TABLE IF EXISTS avis");
                
                stmt.executeUpdate("SET FOREIGN_KEY_CHECKS=1");
                
                System.out.println("Table avis supprimée pour recréation");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la suppression de la table avis: " + e.getMessage());
            }
            
            String createTableSQL = 
                "CREATE TABLE avis (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "commentaire TEXT, " +
                "note VARCHAR(10) NOT NULL, " +
                "date DATE NOT NULL, " +
                "id_utilisateur INT NOT NULL" +
                ")";
            stmt.executeUpdate(createTableSQL);
            System.out.println("Table avis créée avec succès.");
            
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la table avis: " + e.getMessage());
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

                utilisateur user = userService.getById(rs.getInt("id_utilisateur"));
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

                utilisateur user = userService.getById(rs.getInt("id_utilisateur"));
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

                utilisateur user = userService.getById(userId);
                avis.setUser(user);
                
                userAvisList.add(avis);
            }
        }
        return userAvisList;
    }
} 