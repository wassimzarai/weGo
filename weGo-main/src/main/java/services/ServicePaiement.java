package services;

import utils.EmailSender;
import Entites.Paiement;
import utils.database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServicePaiement {
    private static final Logger LOGGER = Logger.getLogger(ServicePaiement.class.getName());

    private static final String INSERT_QUERY =
            "INSERT INTO paiement (reservationId, montant, datePaiement, methodePaiement, statutPaiement, Email) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE paiement SET reservationId=?, montant=?, datePaiement=?, methodePaiement=?, statutPaiement=?, email=? WHERE id=?";
    private static final String DELETE_QUERY =
            "DELETE FROM paiement WHERE id=?";
    private static final String SELECT_ALL_QUERY =
            "SELECT * FROM paiement";
    private static final String SELECT_BY_ID_QUERY =
            "SELECT * FROM paiement WHERE id=?";

    public boolean ajouterPaiement(Paiement p) {
        final String INSERT_QUERY = "INSERT INTO paiement (reservationId, montant, datePaiement, methodePaiement, statutPaiement, email) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = database.getInstance().connectDb();
             PreparedStatement pst = con.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {

            // Définir les paramètres (hors ID car auto-incrémenté)
            pst.setInt(1, p.getReservationId());     // reservationId
            pst.setDouble(2, p.getMontant());        // montant
            pst.setString(3, p.getDatePaiement());   // datePaiement
            pst.setString(4, p.getMethodePaiement());        // methodePaiement
            pst.setString(5, p.getStatut());         // statutPaiement
            pst.setString(6, p.getEmail());          // email

            int affectedRows = pst.executeUpdate();

            if (affectedRows == 0) {
                LOGGER.warning("Aucune ligne insérée dans la table paiement.");
                return false;
            }

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    p.setId(generatedKeys.getInt(1)); // Récupération de l'ID généré
                } else {
                    LOGGER.warning("Aucun ID généré retourné.");
                }
            }

            // Envoi d'un e-mail de confirmation (optionnel)
            envoyerEmailConfirmation(p.getEmail());

            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de l'ajout du paiement", e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur inattendue lors de l'ajout du paiement", e);
            return false;
        }
    }

    public boolean modifierPaiement(Paiement p) {
        final String UPDATE_QUERY =
                "UPDATE paiement " +
                        "SET reservationId=?, montant=?, datePaiement=?, methodePaiement=?, statutPaiement=?" +
                        "WHERE id=?";
        try (Connection con = database.getInstance().connectDb();
             PreparedStatement pst = con.prepareStatement(UPDATE_QUERY)) {

            // 1) paramètres 1 à 6 pour les colonnes à mettre à jour
            pst.setInt(1, p.getReservationId());
            pst.setDouble(2, p.getMontant());
            pst.setString(3, p.getDatePaiement());
            pst.setString(4, p.getMethodePaiement());
            pst.setString(5, p.getStatut());
            // 2) paramètre 7 pour la clause WHERE
            pst.setInt(6, p.getId());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la modification du paiement ID: " + p.getId(), e);
        }
        return false;
    }

    public boolean supprimerPaiement(int id) {
        try (Connection con = database.getInstance().connectDb();
             PreparedStatement pst = con.prepareStatement(DELETE_QUERY)) {

            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression du paiement ID: " + id, e);
        }
        return false;
    }

    public List<Paiement> getList() {
        List<Paiement> paiements = new ArrayList<>();
        try (Connection con = database.getInstance().connectDb();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SELECT_ALL_QUERY)) {

            while (rs.next()) {
                paiements.add(mapToPaiement(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de la liste des paiements", e);
        }
        return paiements;
    }

    public Paiement getById(int id) {
        try (Connection con = database.getInstance().connectDb();
             PreparedStatement pst = con.prepareStatement(SELECT_BY_ID_QUERY)) {

            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapToPaiement(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération du paiement ID: " + id, e);
        }
        return null;
    }

    private void setPaiementParameters(PreparedStatement pst, Paiement p) throws SQLException {
        pst.setInt(1, p.getReservationId());
        pst.setDouble(2, p.getMontant());
        pst.setString(3, p.getDatePaiement());
        pst.setString(4, p.getMethodePaiement());
        pst.setString(5, p.getStatut());
        pst.setString(6, p.getEmail()); // Ajout de l'email dans la requête
    }
    // Dans la classe ServicePaiement

    public Paiement rechercherPaiementParId(int id) {
        Paiement paiement = null;
        String sql =
                "SELECT id, reservationId, montant, datePaiement, methodePaiement, statutPaiement, email " +
                        "FROM paiement WHERE id = ?";

        try (Connection conn = database.getInstance().connectDb();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    paiement = new Paiement(
                            rs.getInt("id"),
                            rs.getInt("reservationId"),
                            rs.getDouble("montant"),
                            rs.getString("datePaiement"),
                            rs.getString("methodePaiement"),
                            rs.getString("statutPaiement"),
                            rs.getString("email")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paiement;
    }

    private void envoyerEmailConfirmation(String emailClient) {
        if (emailClient != null && !emailClient.isEmpty()) {
            String subject = "Confirmation de Paiement";
            String body = "Bonjour,\n\nNous vous confirmons que votre paiement a été effectué avec succès. Merci d'utiliser notre service !\n\nCordialement, L'équipe WeGo.";

            EmailSender.sendEmail(emailClient, subject, body);
        } else {
            LOGGER.log(Level.WARNING, "Email du client vide, impossible d'envoyer la confirmation.");
        }
    }

    private Paiement mapToPaiement(ResultSet rs) throws SQLException {
        return new Paiement(
                rs.getInt("id"),
                rs.getInt("reservationId"),
                rs.getDouble("montant"),
                rs.getString("datePaiement"),
                rs.getString("methodePaiement"),
                rs.getString("statutPaiement"),
                rs.getString("email") // Ajouter l'email lors du mappage
        );
    }
}
