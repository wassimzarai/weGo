package services;

import Utlis.EmailSender;
import entities.Paiement;
import Utlis.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServicePaiement {
    private static final Logger LOGGER = Logger.getLogger(ServicePaiement.class.getName());

    private static final String INSERT_QUERY =
            "INSERT INTO paiement (reservationId, montant, datePaiement, methodePaiement, statutPaiement, email) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE paiement SET reservationId=?, montant=?, datePaiement=?, methodePaiement=?, statutPaiement=?, email=? WHERE id=?";
    private static final String DELETE_QUERY =
            "DELETE FROM paiement WHERE id=?";
    private static final String SELECT_ALL_QUERY =
            "SELECT * FROM paiement";
    private static final String SELECT_BY_ID_QUERY =
            "SELECT * FROM paiement WHERE id=?";

    public boolean ajouterPaiement(Paiement p) {
        try (Connection con = DataSource.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {

            setPaiementParameters(pst, p);

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        p.setId(generatedKeys.getInt(1));
                    }
                }
                // Envoyer l'email de confirmation après l'ajout du paiement
                envoyerEmailConfirmation(p.getEmail());
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout du paiement", e);
        }
        return false;
    }

    public boolean modifierPaiement(Paiement p) {
        try (Connection con = DataSource.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(UPDATE_QUERY)) {

            setPaiementParameters(pst, p);
            pst.setInt(7, p.getId());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la modification du paiement ID: " + p.getId(), e);
        }
        return false;
    }

    public boolean supprimerPaiement(int id) {
        try (Connection con = DataSource.getInstance().getConnection();
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
        try (Connection con = DataSource.getInstance().getConnection();
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
        try (Connection con = DataSource.getInstance().getConnection();
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
        String sql = "SELECT * FROM paiement WHERE id = ?";

        try (Connection conn =DataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                paiement = new Paiement(
                        rs.getInt("id"),
                        rs.getInt("reservation_id"),
                        rs.getDouble("montant"),
                        rs.getString("date_paiement"),
                        rs.getString("methode_paiement"),
                        rs.getString("statut"),
                        rs.getString("email")  // Assure-toi que ta table paiement a bien une colonne 'email'
                );
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
