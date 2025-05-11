package utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDB {
    
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://localhost:3306/lastbd";
        String user = "root";
        String password = "";
        
        try {
            // Charger le pilote MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Pilote MySQL charge avec succes!");
            
            // Etablir la connexion
            Connection connection = DriverManager.getConnection(jdbcUrl, user, password);
            System.out.println("Connexion a la base de donnees reussie!");
            
            // Afficher des informations sur la base de données
            DatabaseMetaData metaData = connection.getMetaData();
            System.out.println("Base de donnees: " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
            System.out.println("URL: " + metaData.getURL());
            System.out.println("Utilisateur: " + metaData.getUserName());
            
            // Fermer la connexion
            connection.close();
            System.out.println("Connexion fermee.");
            
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur de chargement du pilote MySQL: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion a la base de donnees: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 