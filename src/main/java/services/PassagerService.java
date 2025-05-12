package services;

import entities.Passager;
import entities.DemandeReservation;
import entities.Reservation;
import utils.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PassagerService {

    private Connection connection;

    public PassagerService() {
        connection = MyConnection.getConnection();
    }

    // Ajouter un nouveau passager
    public void ajouterPassager(Passager passager) {
        String sql = "INSERT INTO passager (nom, prenom, telephone, email, date_inscription, note_moyenne, nombre_trajets_effectues) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, passager.getNom());
            statement.setString(2, passager.getPrenom());
            statement.setString(3, passager.getTelephone());
            statement.setString(4, passager.getEmail());
            statement.setDate(5, Date.valueOf(passager.getDateInscription()));
            statement.setDouble(6, passager.getNoteMoyenne());
            statement.setInt(7, passager.getNombreTrajetsEffectues());

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        passager.setId(generatedId);
                        System.out.println("Passager ajouté avec succès ! ID généré : " + generatedId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupérer tous les passagers
    public List<Passager> recupererTousPassagers() {
        List<Passager> passagers = new ArrayList<>();
        String sql = "SELECT * FROM passager";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Passager passager = extrairePassagerDuResultSet(resultSet);
                passagers.add(passager);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return passagers;
    }

    // Récupérer un passager par son ID
    public Passager recupererPassagerParId(int id) {
        String sql = "SELECT * FROM passager WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extrairePassagerDuResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Récupérer un passager par son email
    public Passager recupererPassagerParEmail(String email) {
        String sql = "SELECT * FROM passager WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extrairePassagerDuResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Récupérer l'itinéraire habituel d'un passager (le plus fréquent)
    public String[] recupererItineraireHabituel(int passagerId) {
        String sql = "SELECT point_depart, point_arrivee, COUNT(*) as count " +
                     "FROM reservation " +
                     "JOIN passager_reservation ON reservation.id = passager_reservation.reservation_id " +
                     "WHERE passager_reservation.passager_id = ? " +
                     "GROUP BY point_depart, point_arrivee " +
                     "ORDER BY count DESC " +
                     "LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, passagerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new String[] {
                        resultSet.getString("point_depart"),
                        resultSet.getString("point_arrivee")
                    };
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Méthode utilitaire pour extraire un passager d'un ResultSet
    private Passager extrairePassagerDuResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String nom = resultSet.getString("nom");
        String prenom = resultSet.getString("prenom");
        String telephone = resultSet.getString("telephone");
        String email = resultSet.getString("email");
        LocalDate dateInscription = resultSet.getDate("date_inscription").toLocalDate();
        double noteMoyenne = resultSet.getDouble("note_moyenne");
        int nombreTrajetsEffectues = resultSet.getInt("nombre_trajets_effectues");

        return new Passager(
                id, nom, prenom, telephone, email,
                dateInscription, noteMoyenne, nombreTrajetsEffectues
        );
    }
} 