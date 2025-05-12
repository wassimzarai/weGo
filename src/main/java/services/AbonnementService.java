package services;

import entities.Abonnement;
import entities.Reservation;
import utils.MyConnection;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AbonnementService {

    private Connection connection;
    private ReserService reservationService;

    public AbonnementService() {
        connection = MyConnection.getConnection();
        reservationService = new ReserService();
    }

    // Ajouter un nouvel abonnement (avec récupération d'ID généré)
    public void ajouterAbonnement(Abonnement abonnement) {
        String sql = "INSERT INTO abonnement (date_debut, date_fin, reservation_id, montant, type_abonnement, statut, remarques) VALUES (?, ?, ?, ?, ?, ?, ?)";
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
                        System.out.println("Abonnement ajouté avec succès ! ID généré : " + generatedId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Modifier un abonnement
    public void modifierAbonnement(Abonnement abonnement) {
        String sql = "UPDATE abonnement SET date_debut = ?, date_fin = ?, reservation_id = ?, montant = ?, type_abonnement = ?, statut = ?, remarques = ? WHERE id_abonnement = ?";
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
            System.out.println("Abonnement modifié avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Supprimer un abonnement
    public void supprimerAbonnement(int idAbonnement) {
        String sql = "DELETE FROM abonnement WHERE id_abonnement = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idAbonnement);
            statement.executeUpdate();
            System.out.println("Abonnement supprimé avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupérer tous les abonnements
    public List<Abonnement> recupererTousAbonnements() {
        List<Abonnement> abonnements = new ArrayList<>();
        String sql = "SELECT * FROM abonnement";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Abonnement abonnement = extraireAbonnementDuResultSet(resultSet);
                abonnements.add(abonnement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return abonnements;
    }

    // Récupérer un abonnement par son ID
    public Abonnement recupererAbonnementParId(int idAbonnement) {
        String sql = "SELECT * FROM abonnement WHERE id_abonnement = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idAbonnement);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extraireAbonnementDuResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Récupérer les abonnements par ID de réservation
    public List<Abonnement> recupererAbonnementsParReservation(int reservationId) {
        List<Abonnement> abonnements = new ArrayList<>();
        String sql = "SELECT * FROM abonnement WHERE reservation_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reservationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Abonnement abonnement = extraireAbonnementDuResultSet(resultSet);
                    abonnements.add(abonnement);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return abonnements;
    }

    // Récupérer les abonnements actifs
    public List<Abonnement> recupererAbonnementsActifs() {
        List<Abonnement> abonnements = new ArrayList<>();
        String sql = "SELECT * FROM abonnement WHERE statut = 'Actif' AND (date_fin IS NULL OR date_fin >= CURRENT_DATE)";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Abonnement abonnement = extraireAbonnementDuResultSet(resultSet);
                abonnements.add(abonnement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return abonnements;
    }

    // Méthode utilitaire pour extraire un abonnement d'un ResultSet
    private Abonnement extraireAbonnementDuResultSet(ResultSet resultSet) throws SQLException {
        int idAbonnement = resultSet.getInt("id_abonnement");
        LocalDate dateDebut = resultSet.getDate("date_debut").toLocalDate();
        
        Date dateFin = resultSet.getDate("date_fin");
        LocalDate dateFinLocalDate = (dateFin != null) ? dateFin.toLocalDate() : null;
        
        int reservationId = resultSet.getInt("reservation_id");
        BigDecimal montant = resultSet.getBigDecimal("montant");
        String typeAbonnement = resultSet.getString("type_abonnement");
        String statut = resultSet.getString("statut");
        String remarques = resultSet.getString("remarques");

        Abonnement abonnement = new Abonnement(
                idAbonnement, dateDebut, dateFinLocalDate, reservationId,
                montant, typeAbonnement, statut, remarques
        );
        
        // Récupérer la réservation associée
        Reservation reservation = reservationService.recupererReservationParId(reservationId);
        if (reservation != null) {
            abonnement.setReservation(reservation);
        }
        
        return abonnement;
    }

    public void ajouter(Abonnement abonnement) {
    }
} 