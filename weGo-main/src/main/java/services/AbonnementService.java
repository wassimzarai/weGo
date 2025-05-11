package services;

import Entites.Abonnement;
import Entites.Reservation;
import utils.database;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AbonnementService {

    private final Connection connection;
    private final ReserService reservationService;

    public AbonnementService() {
        // On récupère la connexion via la méthode statique de database
        this.connection = database.connectDb();
        if (this.connection == null) {
            throw new IllegalStateException("Impossible d'établir la connexion JDBC pour AbonnementService");
        }
        this.reservationService = new ReserService();
    }

    public void ajouterAbonnement(Abonnement abonnement) {
        String sql = "INSERT INTO abonnement "
                + "(date_debut, date_fin, reservation_id, montant, type_abonnement, statut, remarques) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setDate(1, Date.valueOf(abonnement.getDateDebut()));
            if (abonnement.getDateFin() != null) {
                statement.setDate(2, Date.valueOf(abonnement.getDateFin()));
            } else {
                statement.setNull(2, Types.DATE);
            }
            statement.setInt(3, abonnement.getReservationId());
            statement.setBigDecimal(4, abonnement.getMontant());
            statement.setString(5, abonnement.getTypeAbonnement());
            statement.setString(6, abonnement.getStatut());
            statement.setString(7, abonnement.getRemarques());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        abonnement.setIdAbonnement(generatedId);
                        System.out.println("Abonnement ajouté, ID = " + generatedId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifierAbonnement(Abonnement abonnement) {
        String sql = "UPDATE abonnement SET "
                + "date_debut=?, date_fin=?, reservation_id=?, montant=?, "
                + "type_abonnement=?, statut=?, remarques=? "
                + "WHERE id_abonnement=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(abonnement.getDateDebut()));
            if (abonnement.getDateFin() != null) {
                statement.setDate(2, Date.valueOf(abonnement.getDateFin()));
            } else {
                statement.setNull(2, Types.DATE);
            }
            statement.setInt(3, abonnement.getReservationId());
            statement.setBigDecimal(4, abonnement.getMontant());
            statement.setString(5, abonnement.getTypeAbonnement());
            statement.setString(6, abonnement.getStatut());
            statement.setString(7, abonnement.getRemarques());
            statement.setInt(8, abonnement.getIdAbonnement());

            statement.executeUpdate();
            System.out.println("Abonnement modifié, ID = " + abonnement.getIdAbonnement());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimerAbonnement(int idAbonnement) {
        String sql = "DELETE FROM abonnement WHERE id_abonnement=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idAbonnement);
            statement.executeUpdate();
            System.out.println("Abonnement supprimé, ID = " + idAbonnement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Abonnement> recupererTousAbonnements() {
        List<Abonnement> abonnements = new ArrayList<>();
        String sql = "SELECT * FROM abonnement";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                abonnements.add(extraireAbonnementDuResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return abonnements;
    }

    public Abonnement recupererAbonnementParId(int idAbonnement) {
        String sql = "SELECT * FROM abonnement WHERE id_abonnement=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idAbonnement);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return extraireAbonnementDuResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Abonnement> recupererAbonnementsParReservation(int reservationId) {
        List<Abonnement> abonnements = new ArrayList<>();
        String sql = "SELECT * FROM abonnement WHERE reservation_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reservationId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    abonnements.add(extraireAbonnementDuResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return abonnements;
    }

    public List<Abonnement> recupererAbonnementsActifs() {
        List<Abonnement> abonnements = new ArrayList<>();
        String sql = "SELECT * FROM abonnement "
                + "WHERE statut='Actif' AND (date_fin IS NULL OR date_fin>=CURRENT_DATE)";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                abonnements.add(extraireAbonnementDuResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return abonnements;
    }

    private Abonnement extraireAbonnementDuResultSet(ResultSet rs) throws SQLException {
        int idAb = rs.getInt("id_abonnement");
        LocalDate dDeb = rs.getDate("date_debut").toLocalDate();
        Date df = rs.getDate("date_fin");
        LocalDate dFin = (df != null) ? df.toLocalDate() : null;
        int resId = rs.getInt("reservation_id");
        BigDecimal montant = rs.getBigDecimal("montant");
        String type = rs.getString("type_abonnement");
        String statut = rs.getString("statut");
        String rem = rs.getString("remarques");

        Abonnement a = new Abonnement(idAb, dDeb, dFin, resId, montant, type, statut, rem);
        // récupère et associe la réservation
        Reservation res = reservationService.recupererReservationParId(resId);
        if (res != null) {
            a.setReservation(res);
        }
        return a;
    }

    // Méthode inutilisée, tu peux la supprimer ou implémenter
    public void ajouter(Abonnement abonnement) { }
}
