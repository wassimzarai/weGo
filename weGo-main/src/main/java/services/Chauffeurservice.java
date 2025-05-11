package services;
import Entites.Chauffeur;
import utils.database;
import java.time.LocalDate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Chauffeurservice {


        private final Connection cnx;

        public Chauffeurservice() {
            this.cnx = database.getInstance().connectDb();
        }

        // INSERT Method
        public void insert(Chauffeur C) throws SQLException {
            String sql = "INSERT INTO conducteur(id_voiture, disponibilite, date_depart, date_arret) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pst = cnx.prepareStatement(sql)) {
                pst.setInt(1, C.getIdVoiture());  // Set ID Voiture
                pst.setInt(2, C.getDisponibilite());  // Use setInt for IntegerProperty (disponibilite is now IntegerProperty)
                pst.setObject(3, C.getDateDepart());  // Use setObject for LocalDate (dateDepart)
                pst.setObject(4, C.getDateArret());   // Use setObject for LocalDate (dateArret)
                pst.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error while inserting conducteur: " + e.getMessage());
                throw e; // Re-throw exception
            }
        }

        // Fetch All Conducteurs
        public List<Chauffeur> getAll() throws SQLException {
            List<Chauffeur> liste = new ArrayList<>();
            String sql = "SELECT id, id_voiture, disponibilite, date_depart, date_arret FROM conducteur";

            try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Chauffeur cond = new Chauffeur();
                    cond.setId(rs.getInt("id"));
                    cond.setIdVoiture(rs.getInt("id_voiture"));
                    cond.setDisponibilite(rs.getInt("disponibilite"));  // Corrected to use getInt for IntegerProperty
                    cond.setDateDepart(rs.getObject("date_depart", LocalDate.class));
                    cond.setDateArret(rs.getObject("date_arret", LocalDate.class));
                    liste.add(cond);
                }
            } catch (SQLException e) {
                System.err.println("Error while fetching conducteurs: " + e.getMessage());
                throw e; // Re-throw exception
            }
            return liste;
        }

        // Fetch all distinct ID Voitures
        public List<Integer> getAllIdVoitures() throws SQLException {
            List<Integer> idVoitures = new ArrayList<>();
            String sql = "SELECT DISTINCT id FROM voiture";

            try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    idVoitures.add(rs.getInt("id"));
                }
            } catch (SQLException e) {
                System.err.println("Error while fetching ID Voitures: " + e.getMessage());
                throw e; // Re-throw exception
            }
            return idVoitures;
        }

        // UPDATE Method
        public boolean update(Chauffeur conducteur) throws SQLException {
            String sql = "UPDATE conducteur SET id_voiture=?, disponibilite=?, date_depart=?, date_arret=? WHERE id=?";
            try (PreparedStatement pst = cnx.prepareStatement(sql)) {
                pst.setInt(1, conducteur.getIdVoiture());
                pst.setInt(2, conducteur.getDisponibilite());  // Use setInt for IntegerProperty (disponibilite is now IntegerProperty)
                pst.setObject(3, conducteur.getDateDepart());  // Store LocalDate
                pst.setObject(4, conducteur.getDateArret());   // Store LocalDate
                pst.setInt(5, conducteur.getId());
                return pst.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Error while updating conducteur: " + e.getMessage());
                throw e; // Re-throw exception
            }
        }

        // DELETE Method
        public boolean delete(int id) throws SQLException {
            String sql = "DELETE FROM conducteur WHERE id=?";
            try (PreparedStatement pst = cnx.prepareStatement(sql)) {
                pst.setInt(1, id);
                return pst.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Error while deleting conducteur: " + e.getMessage());
                throw e; // Re-throw exception
            }
        }

        // ADD CONDUCTEUR (Delegates to the insert method)
        public void addConducteur(Chauffeur conducteur) throws SQLException {
            insert(conducteur);  // Calling the insert method
        }
    }


