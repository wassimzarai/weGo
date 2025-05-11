package utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Classe utilitaire pour initialiser les bases de données au démarrage de l'application
 */
public class DatabaseInitializer {
    
    /**
     * Initialise toutes les tables nécessaires pour l'application
     */
    public static void initializeDatabase() {
        try {
            Connection conn = MyConnection.getInstance().getConnection();
            System.out.println("Initialisation des tables de la base de données...");
            
            // Initialiser la table des utilisateurs
            createUserTable(conn);
            
            // Initialiser la table des réclamations
            createReclamationTable(conn);
            
            // Initialiser la table des avis
            createAvisTable(conn);
            
            System.out.println("Initialisation de la base de données terminée avec succès");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'initialisation de la base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Crée la table utilisateur si elle n'existe pas
     */
    private static void createUserTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "utilisateur")) {
            System.out.println("Création de la table utilisateur...");
            Statement stmt = conn.createStatement();
            String sql = "CREATE TABLE utilisateur (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "username VARCHAR(50) NOT NULL, " +
                         "password VARCHAR(100) NOT NULL, " +
                         "email VARCHAR(100) NOT NULL, " +
                         "role VARCHAR(20) NOT NULL, " +
                         "nom VARCHAR(50), " +
                         "prenom VARCHAR(50), " +
                         "telephone VARCHAR(20)" +
                         ")";
            stmt.executeUpdate(sql);
            
            // Insérer un utilisateur par défaut
            sql = "INSERT INTO utilisateur (username, password, email, role, nom, prenom) " +
                  "VALUES ('admin', 'admin123', 'admin@wego.com', 'ADMIN', 'Admin', 'User')";
            stmt.executeUpdate(sql);
            
            sql = "INSERT INTO utilisateur (username, password, email, role, nom, prenom) " +
                  "VALUES ('client', 'client123', 'client@wego.com', 'CLIENT', 'Client', 'User')";
            stmt.executeUpdate(sql);
            
            System.out.println("Table utilisateur créée avec utilisateurs par défaut");
            stmt.close();
        }
    }
    
    /**
     * Crée la table reclamation si elle n'existe pas
     */
    private static void createReclamationTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "reclamation")) {
            System.out.println("Création de la table reclamation...");
            Statement stmt = conn.createStatement();
            String sql = "CREATE TABLE reclamation (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "titre VARCHAR(100) NOT NULL, " +
                         "description TEXT, " +
                         "date DATE NOT NULL, " +
                         "statut VARCHAR(20) DEFAULT 'ouvert', " +
                         "id_utilisateur INT NOT NULL, " +
                         "latitude DOUBLE DEFAULT 0, " +
                         "longitude DOUBLE DEFAULT 0, " +
                         "adresse VARCHAR(255), " +
                         "ticket_id VARCHAR(20), " +
                         "priorite INT DEFAULT 3, " +
                         "categorie VARCHAR(50), " +
                         "gravite VARCHAR(20) DEFAULT 'moyenne'" +
                         ")";
            stmt.executeUpdate(sql);
            System.out.println("Table reclamation créée");
            stmt.close();
        }
    }
    
    /**
     * Crée la table avis si elle n'existe pas
     */
    private static void createAvisTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "avis")) {
            System.out.println("Création de la table avis...");
            Statement stmt = conn.createStatement();
            String sql = "CREATE TABLE avis (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "commentaire TEXT, " +
                         "note VARCHAR(10) NOT NULL, " +
                         "date DATE NOT NULL, " +
                         "id_utilisateur INT NOT NULL" +
                         ")";
            stmt.executeUpdate(sql);
            System.out.println("Table avis créée");
            stmt.close();
        }
    }
    
    /**
     * Vérifie si une table existe dans la base de données
     */
    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet rs = meta.getTables(null, null, tableName, new String[] {"TABLE"});
        boolean exists = rs.next();
        rs.close();
        return exists;
    }
} 