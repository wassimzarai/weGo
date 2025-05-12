package services;

import entities.Conducteur;
import entities.Trajet;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConducteurService {

    private Connection connection;

    public ConducteurService() {
        connection = MyConnection.getConnection();
    }

    // Ajouter un nouveau conducteur
    public void ajouterConducteur(Conducteur conducteur) {
        String sql = "INSERT INTO conducteur (nom, prenom, telephone, email, note_moyenne, nombre_trajet_effectues, " +
                     "numero_permis, vehicule, immatriculation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, conducteur.getNom());
            statement.setString(2, conducteur.getPrenom());
            statement.setString(3, conducteur.getTelephone());
            statement.setString(4, conducteur.getEmail());
            statement.setDouble(5, conducteur.getNoteMoyenne());
            statement.setInt(6, conducteur.getNombreTrajetEffectues());
            statement.setString(7, conducteur.getNumeroPermis());
            statement.setString(8, conducteur.getVehicule());
            statement.setString(9, conducteur.getImmatriculation());

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        conducteur.setId(generatedId);
                        System.out.println("Conducteur ajouté avec succès ! ID généré : " + generatedId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupérer tous les conducteurs
    public List<Conducteur> recupererTousConducteurs() {
        List<Conducteur> conducteurs = new ArrayList<>();
        String sql = "SELECT * FROM conducteur";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Conducteur conducteur = extraireConducteurDuResultSet(resultSet);
                conducteurs.add(conducteur);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conducteurs;
    }

    // Récupérer un conducteur par son ID
    public Conducteur recupererConducteurParId(int id) {
        String sql = "SELECT * FROM conducteur WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extraireConducteurDuResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Méthode utilitaire pour extraire un conducteur d'un ResultSet
    private Conducteur extraireConducteurDuResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String nom = resultSet.getString("nom");
        String prenom = resultSet.getString("prenom");
        String telephone = resultSet.getString("telephone");
        String email = resultSet.getString("email");
        double noteMoyenne = resultSet.getDouble("note_moyenne");
        int nombreTrajetEffectues = resultSet.getInt("nombre_trajet_effectues");
        String numeroPermis = resultSet.getString("numero_permis");
        String vehicule = resultSet.getString("vehicule");
        String immatriculation = resultSet.getString("immatriculation");

        return new Conducteur(
                id, nom, prenom, telephone, email,
                noteMoyenne, nombreTrajetEffectues, numeroPermis,
                vehicule, immatriculation
        );
    }
} 