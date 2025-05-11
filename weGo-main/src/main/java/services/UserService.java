package services;

import Entites.utilisateur;
import Entites.UtilisateurRole;
import utils.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private Connection conn;

    public UserService() {
        conn = MyConnection.getInstance().getConnection();
    }

    public void ajouter(utilisateur user) {
        String query = "INSERT INTO utilisateur (username, email, password, image) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, user.getUsername());
            pst.setString(2, user.getEmail());
            pst.setString(3, user.getPassword());
            pst.setString(4, "default.jpg"); // Set a default image

            int affectedRows = pst.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            System.out.println("Utilisateur ajouté avec succès");
        } catch (SQLException ex) {
            System.out.println("Erreur lors de l'ajout de l'utilisateur: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public List<utilisateur> voir() {
        List<utilisateur> users = new ArrayList<>();
        String query = "SELECT * FROM utilisateur";
        
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            
            while (rs.next()) {
                utilisateur user = new utilisateur();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                users.add(user);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la récupération des utilisateurs: " + ex.getMessage());
            ex.printStackTrace();
        }
        return users;
    }

    public utilisateur getById(int id) {
        String query = "SELECT * FROM utilisateur WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    utilisateur user = new utilisateur();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setImage(rs.getString("image"));
                    
                    // Récupérer les champs de profil
                    try {
                        user.setNom(rs.getString("nom"));
                        user.setPrenom(rs.getString("prenom"));
                        user.setTelephone(rs.getString("telephone"));
                    } catch (SQLException ex) {
                        // Ignorer si les colonnes n'existent pas encore
                        System.out.println("Certains champs de profil ne sont pas disponibles: " + ex.getMessage());
                    }
                    
                    return user;
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la récupération de l'utilisateur: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    public void modifier(utilisateur user) {
        String query = "UPDATE utilisateur SET username = ?, email = ?, password = ?, role = ?, " +
                       "nom = ?, prenom = ?, telephone = ? WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, user.getUsername());
            pst.setString(2, user.getEmail());
            pst.setString(3, user.getPassword());
            pst.setString(4, user.getRole().toString());
            pst.setString(5, user.getNom());
            pst.setString(6, user.getPrenom());
            pst.setString(7, user.getTelephone());
            pst.setInt(8, user.getId());
            pst.executeUpdate();
            System.out.println("Utilisateur modifié avec succès!");
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public void supprimer(int id) {
        String query = "DELETE FROM utilisateur WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Utilisateur supprimé avec succès!");
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public utilisateur login(String email, String password) {
        String query = "SELECT * FROM utilisateur WHERE email = ? AND password = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, email);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                utilisateur user = new utilisateur();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(UtilisateurRole.valueOf(rs.getString("role").toUpperCase()));
                
                // Récupérer les champs de profil
                try {
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setTelephone(rs.getString("telephone"));
                } catch (SQLException ex) {
                    // Ignorer si les colonnes n'existent pas encore
                    System.out.println("Certains champs de profil ne sont pas disponibles: " + ex.getMessage());
                }
                
                return user;
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return null;
    }

    public List<utilisateur> getAdmins() {
        List<utilisateur> list = new ArrayList<>();
        String query = "SELECT * FROM utilisateur WHERE role = 'ADMIN'";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                utilisateur user = new utilisateur();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(UtilisateurRole.valueOf(rs.getString("role").toUpperCase()));
                list.add(user);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return list;
    }

    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return false;
    }

    public boolean usernameExists(String username) {
        String query = "SELECT COUNT(*) FROM utilisateur WHERE username = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return false;
    }
} 