package services;

import entities.Reponse;
import entities.Reclamation;
import entities.User;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReponseService implements IService<Reponse> {
    private Connection conn;

    public ReponseService() {
        conn = MyConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Reponse reponse) {
        String query = "INSERT INTO reponse (message, date_reponse, id_reclamation, id_admin) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, reponse.getMessage());
            pst.setDate(2, Date.valueOf(reponse.getDateReponse()));
            pst.setInt(3, reponse.getReclamation().getId());
            pst.setInt(4, reponse.getAdmin().getId());
            pst.executeUpdate();
            System.out.println("Réponse ajoutée avec succès!");
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public void modifier(Reponse reponse) {
        String query = "UPDATE reponse SET message = ?, date_reponse = ?, id_reclamation = ?, id_admin = ? WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, reponse.getMessage());
            pst.setDate(2, Date.valueOf(reponse.getDateReponse()));
            pst.setInt(3, reponse.getReclamation().getId());
            pst.setInt(4, reponse.getAdmin().getId());
            pst.setInt(5, reponse.getId());
            pst.executeUpdate();
            System.out.println("Réponse modifiée avec succès!");
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String query = "DELETE FROM reponse WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Réponse supprimée avec succès!");
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public List<Reponse> voir() {
        List<Reponse> list = new ArrayList<>();
        String query = "SELECT r.*, u.username, rec.titre FROM reponse r " +
                      "JOIN utilisateur u ON r.id_admin = u.id " +
                      "JOIN reclamation rec ON r.id_reclamation = rec.id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Reponse reponse = new Reponse();
                reponse.setId(rs.getInt("id"));
                reponse.setMessage(rs.getString("message"));
                reponse.setDateReponse(rs.getDate("date_reponse").toLocalDate());
                
                User admin = new User();
                admin.setId(rs.getInt("id_admin"));
                admin.setUsername(rs.getString("username"));
                reponse.setAdmin(admin);
                
                Reclamation reclamation = new Reclamation();
                reclamation.setId(rs.getInt("id_reclamation"));
                reclamation.setTitre(rs.getString("titre"));
                reponse.setReclamation(reclamation);
                
                list.add(reponse);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return list;
    }

    @Override
    public Reponse getById(int id) {
        String query = "SELECT r.*, u.username, rec.titre FROM reponse r " +
                      "JOIN utilisateur u ON r.id_admin = u.id " +
                      "JOIN reclamation rec ON r.id_reclamation = rec.id " +
                      "WHERE r.id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Reponse reponse = new Reponse();
                reponse.setId(rs.getInt("id"));
                reponse.setMessage(rs.getString("message"));
                reponse.setDateReponse(rs.getDate("date_reponse").toLocalDate());
                
                User admin = new User();
                admin.setId(rs.getInt("id_admin"));
                admin.setUsername(rs.getString("username"));
                reponse.setAdmin(admin);
                
                Reclamation reclamation = new Reclamation();
                reclamation.setId(rs.getInt("id_reclamation"));
                reclamation.setTitre(rs.getString("titre"));
                reponse.setReclamation(reclamation);
                
                return reponse;
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return null;
    }

    public List<Reponse> getByReclamation(int reclamationId) {
        List<Reponse> list = new ArrayList<>();
        String query = "SELECT r.*, u.username, rec.titre FROM reponse r " +
                      "JOIN utilisateur u ON r.id_admin = u.id " +
                      "JOIN reclamation rec ON r.id_reclamation = rec.id " +
                      "WHERE r.id_reclamation = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, reclamationId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Reponse reponse = new Reponse();
                reponse.setId(rs.getInt("id"));
                reponse.setMessage(rs.getString("message"));
                reponse.setDateReponse(rs.getDate("date_reponse").toLocalDate());
                
                User admin = new User();
                admin.setId(rs.getInt("id_admin"));
                admin.setUsername(rs.getString("username"));
                reponse.setAdmin(admin);
                
                Reclamation reclamation = new Reclamation();
                reclamation.setId(rs.getInt("id_reclamation"));
                reclamation.setTitre(rs.getString("titre"));
                reponse.setReclamation(reclamation);
                
                list.add(reponse);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return list;
    }
} 