package services;

import entities.DemandeReservation;
import entities.Passager;
import utils.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DemandeReservationService {

    private Connection connection;
    private PassagerService passagerService;

    public DemandeReservationService() {
        connection = MyConnection.getConnection();
        passagerService = new PassagerService();
    }

    // Ajouter une nouvelle demande de réservation
    public void ajouterDemandeReservation(DemandeReservation demande) {
        String sql = "INSERT INTO demande_reservation (ville_depart, ville_arrivee, point_depart, point_arrivee, " +
                     "date, heure_souhaitee, nombre_places, prix_maximum, accepte_fumeur, accepte_animaux, " +
                     "besoin_place_gros_bagages, commentaires, date_creation, statut, passager_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, demande.getVilleDepart());
            statement.setString(2, demande.getVilleArrivee());
            statement.setString(3, demande.getPointDepart());
            statement.setString(4, demande.getPointArrivee());
            statement.setDate(5, Date.valueOf(demande.getDate()));
            
            if (demande.getHeureSouhaitee() != null) {
                statement.setTime(6, Time.valueOf(demande.getHeureSouhaitee()));
            } else {
                statement.setNull(6, Types.TIME);
            }
            
            statement.setInt(7, demande.getNombrePlaces());
            statement.setDouble(8, demande.getPrixMaximum());
            statement.setBoolean(9, demande.isAccepteFumeur());
            statement.setBoolean(10, demande.isAccepteAnimaux());
            statement.setBoolean(11, demande.isBesoinPlaceGrossBagages());
            statement.setString(12, demande.getCommentaires());
            statement.setDate(13, Date.valueOf(demande.getDateCreation()));
            statement.setString(14, demande.getStatut());
            
            if (demande.getPassager() != null) {
                statement.setInt(15, demande.getPassager().getId());
            } else {
                statement.setNull(15, Types.INTEGER);
            }

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        demande.setId(generatedId);
                        System.out.println("Demande de réservation ajoutée avec succès ! ID généré : " + generatedId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupérer toutes les demandes de réservation
    public List<DemandeReservation> recupererToutesDemandes() {
        List<DemandeReservation> demandes = new ArrayList<>();
        String sql = "SELECT * FROM demande_reservation";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                DemandeReservation demande = extraireDemandeDuResultSet(resultSet);
                demandes.add(demande);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return demandes;
    }

    // Récupérer une demande par son ID
    public DemandeReservation recupererDemandeParId(int id) {
        String sql = "SELECT * FROM demande_reservation WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extraireDemandeDuResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Récupérer les demandes en attente pour un passager
    public List<DemandeReservation> recupererDemandesEnAttente(int passagerId) {
        List<DemandeReservation> demandes = new ArrayList<>();
        String sql = "SELECT * FROM demande_reservation WHERE passager_id = ? AND statut = 'En attente'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, passagerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    DemandeReservation demande = extraireDemandeDuResultSet(resultSet);
                    demandes.add(demande);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return demandes;
    }
    
    // Mettre à jour le statut d'une demande
    public void mettreAJourStatut(int demandeId, String nouveauStatut) {
        String sql = "UPDATE demande_reservation SET statut = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nouveauStatut);
            statement.setInt(2, demandeId);
            statement.executeUpdate();
            System.out.println("Statut de la demande mis à jour avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthode utilitaire pour extraire une demande d'un ResultSet
    private DemandeReservation extraireDemandeDuResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String villeDepart = resultSet.getString("ville_depart");
        String villeArrivee = resultSet.getString("ville_arrivee");
        String pointDepart = resultSet.getString("point_depart");
        String pointArrivee = resultSet.getString("point_arrivee");
        LocalDate date = resultSet.getDate("date").toLocalDate();
        
        Time heureSouhaiteeTime = resultSet.getTime("heure_souhaitee");
        LocalTime heureSouhaitee = (heureSouhaiteeTime != null) ? heureSouhaiteeTime.toLocalTime() : null;
        
        int nombrePlaces = resultSet.getInt("nombre_places");
        double prixMaximum = resultSet.getDouble("prix_maximum");
        boolean accepteFumeur = resultSet.getBoolean("accepte_fumeur");
        boolean accepteAnimaux = resultSet.getBoolean("accepte_animaux");
        boolean besoinPlaceGrossBagages = resultSet.getBoolean("besoin_place_gros_bagages");
        String commentaires = resultSet.getString("commentaires");
        LocalDate dateCreation = resultSet.getDate("date_creation").toLocalDate();
        String statut = resultSet.getString("statut");
        
        DemandeReservation demande = new DemandeReservation(
                id, villeDepart, villeArrivee, pointDepart, pointArrivee,
                date, heureSouhaitee, nombrePlaces, prixMaximum,
                accepteFumeur, accepteAnimaux, besoinPlaceGrossBagages,
                commentaires, dateCreation, statut
        );
        
        // Récupérer le passager associé
        int passagerId = resultSet.getInt("passager_id");
        if (!resultSet.wasNull()) {
            Passager passager = passagerService.recupererPassagerParId(passagerId);
            if (passager != null) {
                demande.setPassager(passager);
            }
        }
        
        return demande;
    }
} 