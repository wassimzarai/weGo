package Service;

import Entities.Voiture;
import Entities.TypeVoiture;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoitureService {

    private final Connection cnx;

    public VoitureService() {
        cnx = DataSource.getInstance().getCnx();
    }

    /* -------- CREATE -------- */
    public void insert(Voiture v) throws SQLException {
        String sql = "INSERT INTO voiture(marque, modele, idType, matricule) VALUES (?,?,?,?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, v.getMarque());
            pst.setString(2, v.getModele());
            pst.setInt(3, v.getType().ordinal());  // Save the ordinal value of TypeVoiture
            pst.setString(4, v.getMatricule());  // Save matricule
            pst.executeUpdate();
        }
    }

    /* -------- READ -------- */
    public List<Voiture> getAll() throws SQLException {
        List<Voiture> list = new ArrayList<>();
        String sql = """
                     SELECT v.id, v.marque, v.modele, v.idType, t.libelle, v.matricule
                     FROM   voiture v
                     JOIN   type_voiture t ON v.idType = t.id
                     """;
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Voiture(
                        rs.getInt("id"),
                        rs.getString("marque"),
                        rs.getString("modele"),
                        TypeVoiture.fromInt(rs.getInt("idType")), // Convert from int to TypeVoiture
                        rs.getString("matricule")  // Get matricule
                ));
            }
        }
        return list;
    }

    public Voiture getById(int id) throws SQLException {
        String sql = """
                     SELECT v.id, v.marque, v.modele, v.idType, t.libelle, v.matricule
                     FROM   voiture v
                     JOIN   type_voiture t ON v.idType = t.id
                     WHERE  v.id = ? 
                     """;
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Voiture(
                            id,
                            rs.getString("marque"),
                            rs.getString("modele"),
                            TypeVoiture.fromInt(rs.getInt("idType")), // Convert from int to TypeVoiture
                            rs.getString("matricule")  // Get matricule
                    );
                }
            }
        }
        return null;
    }

    /* -------- UPDATE -------- */
    public boolean update(Voiture v) throws SQLException {
        String sql = "UPDATE voiture SET marque=?, modele=?, idType=?, matricule=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, v.getMarque());
            pst.setString(2, v.getModele());
            pst.setInt(3, v.getType().ordinal()); // Update using the ordinal value of TypeVoiture
            pst.setString(4, v.getMatricule());  // Update matricule
            pst.setInt(5, v.getId());
            return pst.executeUpdate() > 0;
        }
    }

    /* -------- DELETE -------- */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM voiture WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        }
    }

    /* -------- Unique Brand Count -------- */
    // Method to count the number of unique brands
    public int getUniqueBrandCount() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT marque) AS brand_count FROM voiture";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("brand_count");
            }
        }
        return 0; // Default value if no data found
    }

    /* -------- Brand Distribution -------- */
    // Method to count the distribution (percentage) of each brand
    public Map<String, Double> getBrandDistribution() throws SQLException {
        Map<String, Double> brandDistribution = new HashMap<>();

        // First, get the total number of voitures
        String countSql = "SELECT COUNT(*) AS total FROM voiture";
        int totalVoitures = 0;
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(countSql)) {
            if (rs.next()) {
                totalVoitures = rs.getInt("total");
            }
        }

        if (totalVoitures == 0) {
            return brandDistribution; // No voitures, no distribution
        }

        // Get the number of voitures for each brand
        String sql = "SELECT marque, COUNT(*) AS count FROM voiture GROUP BY marque";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String marque = rs.getString("marque");
                int brandCount = rs.getInt("count");

                // Calculate the percentage distribution for this brand
                double percentage = ((double) brandCount / totalVoitures) * 100;
                brandDistribution.put(marque, percentage);
            }
        }
        return brandDistribution;
    }

    public Voiture getByMarqueAndModele(String marque, String modele) throws SQLException {
        String sql = "SELECT v.id, v.marque, v.modele, v.idType, v.matricule FROM voiture v WHERE v.marque = ? AND v.modele = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, marque);
            pst.setString(2, modele);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Voiture(
                            rs.getInt("id"),
                            rs.getString("marque"),
                            rs.getString("modele"),
                            TypeVoiture.fromInt(rs.getInt("idType")),
                            rs.getString("matricule")  // Get matricule
                    );
                }
            }
        }
        return null;
    }
}
