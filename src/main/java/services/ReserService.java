package services;

import entities.Reservation;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReserService {

    private Connection connection;

    public ReserService() {
        connection = MyConnection.getConnection();
    }

    // ✅ Ajouter une nouvelle réservation (avec récupération d'ID généré)
    public void ajouterReservation(Reservation reservation) {
        String sql = "INSERT INTO reservation (nom_passager, date_reservation, point_depart, point_arrivee, statut, type_trajet, commentaire) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, reservation.getNomPassager());
            statement.setDate(2, Date.valueOf(reservation.getDateReservation()));
            statement.setString(3, reservation.getPointDepart());
            statement.setString(4, reservation.getPointArrivee());
            statement.setString(5, "En attente"); // statut par défaut
            statement.setString(6, reservation.getTypeTrajet());
            statement.setString(7, reservation.getCommentaire());

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        reservation.setId(generatedId);  // ⬅️ met à jour l'objet
                        System.out.println("Réservation ajoutée avec succès ! ID généré : " + generatedId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Modifier une réservation
    public void modifierReservation(Reservation reservation) {
        String sql = "UPDATE reservation SET nom_passager = ?, date_reservation = ?, point_depart = ?, point_arrivee = ?, statut = ?, type_trajet = ?, commentaire = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reservation.getNomPassager());
            statement.setDate(2, Date.valueOf(reservation.getDateReservation()));
            statement.setString(3, reservation.getPointDepart());
            statement.setString(4, reservation.getPointArrivee());
            statement.setString(5, reservation.getStatut());
            statement.setString(6, reservation.getTypeTrajet());
            statement.setString(7, reservation.getCommentaire());
            statement.setInt(8, reservation.getId());
            statement.executeUpdate();
            System.out.println("Réservation modifiée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Supprimer une réservation
    public void supprimerReservation(int id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
            System.out.println("Réservation supprimée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Récupérer toutes les réservations
    public List<Reservation> recupererToutesReservations() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservation";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Reservation reservation = new Reservation(
                        resultSet.getInt("id"),
                        resultSet.getString("nom_passager"),
                        resultSet.getDate("date_reservation").toLocalDate(),
                        resultSet.getString("point_depart"),
                        resultSet.getString("point_arrivee"),
                        resultSet.getString("statut"),
                        resultSet.getString("type_trajet"),
                        resultSet.getString("commentaire")
                );
                reservations.add(reservation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    // ✅ Récupérer une réservation par son ID
    public Reservation recupererReservationParId(int id) {
        String sql = "SELECT * FROM reservation WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new Reservation(
                    resultSet.getInt("id"),
                    resultSet.getString("nom_passager"),
                    resultSet.getDate("date_reservation").toLocalDate(),
                    resultSet.getString("point_depart"),
                    resultSet.getString("point_arrivee"),
                    resultSet.getString("statut"),
                    resultSet.getString("type_trajet"),
                    resultSet.getString("commentaire")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
