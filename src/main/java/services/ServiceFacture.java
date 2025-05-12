package services;

import entities.Facture;
import entities.Paiement;
import Utlis.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceFacture {
    private static final Logger LOGGER = Logger.getLogger(ServiceFacture.class.getName());
    private final ServicePaiement servicePaiement = new ServicePaiement();

    private static final String INSERT_QUERY =
            "INSERT INTO facture (idFacture, reservationId, paiementId, montantTotal, montantPaye, dateEmission, description) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_QUERY =
            "UPDATE facture SET reservationId=?, paiementId=?, montantTotal=?, montantPaye=?, dateEmission=?, description=? WHERE idFacture=?";
    private static final String DELETE_QUERY =
            "DELETE FROM facture WHERE idFacture=?";
    private static final String SELECT_ALL_QUERY =
            "SELECT * FROM facture";
    private static final String SELECT_BY_ID_QUERY =
            "SELECT * FROM facture WHERE idFacture=?";

    public boolean ajouterFacture(Facture f) {
        try (Connection con = DataSource.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(INSERT_QUERY)) {  // Pas besoin de RETURN_GENERATED_KEYS puisque idFacture est manuel

            setFactureParameters(pst, f);
            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "Facture ajoutée avec succès : ID " + f.getIdFacture());
                return true;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout de la facture", e);
        }
        return false;
    }


    public boolean ajouterFacturesPourPaiement(List<Facture> factures) {
        boolean allSuccess = true;

        for (Facture f : factures) {
            boolean success = ajouterFacture(f);
            if (!success) {
                allSuccess = false;
                LOGGER.log(Level.WARNING, "Échec de l'ajout de la facture liée au paiement ID : " + f.getPaiementId());
            }
        }

        if (allSuccess) {
            LOGGER.log(Level.INFO, "Toutes les factures ont été ajoutées avec succès pour le paiement.");
        } else {
            LOGGER.log(Level.WARNING, "Certaines factures n'ont pas pu être ajoutées.");
        }

        return allSuccess;
    }

    public boolean updateFacture(Facture f) {
        try (Connection con = DataSource.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(UPDATE_QUERY)) {

            setFactureParameters(pst, f);
            pst.setInt(7, f.getIdFacture()); // L'ID va dans le WHERE

            int rowsUpdated = pst.executeUpdate();

            if (rowsUpdated > 0) {
                LOGGER.log(Level.INFO, "Facture mise à jour avec succès : ID " + f.getIdFacture());
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Aucune facture mise à jour (ID introuvable) : ID " + f.getIdFacture());
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la modification de la facture ID: " + f.getIdFacture(), e);
        }
        return false;
    }

    public boolean supprimerFacture(int idFacture) {
        try (Connection con = DataSource.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(DELETE_QUERY)) {

            pst.setInt(1, idFacture);
            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de la facture ID: " + idFacture, e);
        }
        return false;
    }

    public List<Facture> getList() {
        List<Facture> factures = new ArrayList<>();
        try (Connection con = DataSource.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SELECT_ALL_QUERY)) {

            while (rs.next()) {
                factures.add(mapToFacture(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de la liste des factures", e);
        }
        return factures;
    }

    public Facture getById(int idFacture) {
        try (Connection con = DataSource.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(SELECT_BY_ID_QUERY)) {

            pst.setInt(1, idFacture);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapToFacture(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de la facture ID: " + idFacture, e);
        }
        return null;
    }

    private void setFactureParameters(PreparedStatement pst, Facture f) throws SQLException {
        pst.setInt(1, f.getIdFacture());
        pst.setInt(2, f.getReservationId());
        pst.setInt(3, f.getPaiementId());
        pst.setDouble(4, f.getMontantTotal());
        pst.setDouble(5, f.getMontantPaye());
        pst.setString(6, f.getDateEmission());
        pst.setString(7, f.getDescription());
    }


    private Facture mapToFacture(ResultSet rs) throws SQLException {
        return new Facture(
                rs.getInt("idFacture"),
                rs.getInt("reservationId"),
                rs.getInt("paiementId"),
                rs.getDouble("montantTotal"),
                rs.getDouble("montantPaye"),
                rs.getString("dateEmission"),
                rs.getString("description")
        );
    }

    public boolean creerFactureDepuisPaiement(int paiementId) {
        try {
            Paiement p = servicePaiement.getById(paiementId);
            if (p != null && "PAYE".equalsIgnoreCase(p.getStatut())) {
                Facture f = new Facture(
                        p.getReservationId(),
                        p.getId(),
                        p.getMontant(),
                        p.getMontant(),
                        p.getDatePaiement(),
                        "Facture générée automatiquement"
                );
                return ajouterFacture(f);
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création de la facture", e);
            return false;
        }
    }
    public double calculerSoldeRestant(int idFacture) {
        Facture facture = getById(idFacture);
        if (facture != null) {
            return facture.getMontantTotal() - facture.getMontantPaye();
        } else {
            LOGGER.log(Level.WARNING, "Facture introuvable pour ID: " + idFacture);
            return -1; // -1 signifie facture non trouvée
        }
    }

}
