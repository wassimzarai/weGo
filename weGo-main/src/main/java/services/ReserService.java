// src/main/java/services/ReserService.java
package services;

import Entites.Reservation;
import utils.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReserService {
    private final Connection connection;

    public ReserService() {
        // Utilise la méthode statique ou singleton de ta classe database
        this.connection = utils.database.connectDb();
        if (this.connection == null) {
            throw new IllegalStateException("Connexion JDBC non initialisée dans ReserService");
        }
    }

    public void ajouterReservation(Reservation reservation) {
        String sql = """
            INSERT INTO reservation
              (nom_passager, date_reservation, point_depart, point_arrivee, statut, type_trajet, commentaire)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, reservation.getNomPassager());
            stmt.setDate(2, Date.valueOf(reservation.getDateReservation()));
            stmt.setString(3, reservation.getPointDepart());
            stmt.setString(4, reservation.getPointArrivee());
            stmt.setString(5, "En attente");
            stmt.setString(6, reservation.getTypeTrajet());
            stmt.setString(7, reservation.getCommentaire());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        reservation.setId(keys.getInt(1));
                        System.out.println("Réservation ajoutée, ID = " + reservation.getId());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifierReservation(Reservation reservation) {
        String sql = """
            UPDATE reservation
               SET nom_passager=?, date_reservation=?, point_depart=?, point_arrivee=?,
                   statut=?, type_trajet=?, commentaire=?
             WHERE id=?
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reservation.getNomPassager());
            stmt.setDate(2, Date.valueOf(reservation.getDateReservation()));
            stmt.setString(3, reservation.getPointDepart());
            stmt.setString(4, reservation.getPointArrivee());
            stmt.setString(5, reservation.getStatut());
            stmt.setString(6, reservation.getTypeTrajet());
            stmt.setString(7, reservation.getCommentaire());
            stmt.setInt(8, reservation.getId());
            stmt.executeUpdate();
            System.out.println("Réservation modifiée !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimerReservation(int id) {
        String sql = "DELETE FROM reservation WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Réservation supprimée, ID = " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Reservation> recupererToutesReservations() {
        List<Reservation> liste = new ArrayList<>();
        String sql = "SELECT * FROM reservation";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(new Reservation(
                        rs.getInt("id"),
                        rs.getString("nom_passager"),
                        rs.getDate("date_reservation").toLocalDate(),
                        rs.getString("point_depart"),
                        rs.getString("point_arrivee"),
                        rs.getString("statut"),
                        rs.getString("type_trajet"),
                        rs.getString("commentaire")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    public Reservation recupererReservationParId(int id) {
        String sql = "SELECT * FROM reservation WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Reservation(
                            rs.getInt("id"),
                            rs.getString("nom_passager"),
                            rs.getDate("date_reservation").toLocalDate(),
                            rs.getString("point_depart"),
                            rs.getString("point_arrivee"),
                            rs.getString("statut"),
                            rs.getString("type_trajet"),
                            rs.getString("commentaire")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
