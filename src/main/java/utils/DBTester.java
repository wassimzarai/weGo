package utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitaire pour tester la connexion à la base de données et la structure des tables
 */
public class DBTester {

    public static void main(String[] args) {
        testConnection();
        checkReservationTable();
    }
    
    /**
     * Teste la connexion à la base de données
     */
    public static void testConnection() {
        System.out.println("=== TEST DE CONNEXION A LA BASE DE DONNEES ===");
        Connection connection = MyConnection.getConnection();
        
        if (connection != null) {
            System.out.println("SUCCES: Connexion a la base de donnees reussie!");
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                System.out.println("Base de donnees: " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
                System.out.println("URL: " + metaData.getURL());
                System.out.println("Utilisateur: " + metaData.getUserName());
            } catch (SQLException e) {
                System.err.println("Erreur lors de la recuperation des metadonnees: " + e.getMessage());
            }
        } else {
            System.err.println("ECHEC: Echec de la connexion a la base de donnees!");
        }
        System.out.println("============================================");
    }
    
    /**
     * Vérifie si la table reservation existe et sa structure
     */
    public static void checkReservationTable() {
        System.out.println("\n=== VERIFICATION DE LA TABLE RESERVATION ===");
        Connection connection = MyConnection.getConnection();
        
        if (connection == null) {
            System.err.println("ECHEC: Echec de la connexion a la base de donnees!");
            return;
        }
        
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "reservation", null);
            
            if (tables.next()) {
                System.out.println("SUCCES: La table 'reservation' existe!");
                
                // Vérifier les colonnes
                List<String> columns = new ArrayList<>();
                ResultSet cols = metaData.getColumns(null, null, "reservation", null);
                System.out.println("\nStructure de la table 'reservation':");
                System.out.println("----------------------------------");
                while (cols.next()) {
                    String columnName = cols.getString("COLUMN_NAME");
                    String columnType = cols.getString("TYPE_NAME");
                    int columnSize = cols.getInt("COLUMN_SIZE");
                    String nullable = cols.getInt("NULLABLE") == 1 ? "NULL" : "NOT NULL";
                    
                    columns.add(columnName);
                    System.out.println(columnName + " - " + columnType + "(" + columnSize + ") " + nullable);
                }
                
                // Vérifier le nombre de lignes
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM reservation")) {
                    if (rs.next()) {
                        int count = rs.getInt("count");
                        System.out.println("\nNombre d'enregistrements: " + count);
                        
                        if (count == 0) {
                            System.out.println("ATTENTION: La table 'reservation' est vide! Vous devez ajouter des reservations.");
                            System.out.println("\nUtilisez l'interface de l'application pour ajouter des reservations, ou");
                            System.out.println("executez le script SQL suivant pour ajouter des reservations de test:");
                            System.out.println("--------------------------------------------------------------");
                            System.out.println("INSERT INTO reservation (nom_passager, date_reservation, point_depart, point_arrivee, statut, type_trajet, commentaire)");
                            System.out.println("VALUES ('Jean Dupont', '2023-05-15', 'Paris', 'Lyon', 'Confirme', 'Aller simple', 'RAS');");
                            System.out.println("");
                            System.out.println("INSERT INTO reservation (nom_passager, date_reservation, point_depart, point_arrivee, statut, type_trajet, commentaire)");
                            System.out.println("VALUES ('Marie Martin', '2023-05-20', 'Marseille', 'Nice', 'En attente', 'Aller-retour', 'Besoin de 2 places');");
                            System.out.println("");
                            System.out.println("INSERT INTO reservation (nom_passager, date_reservation, point_depart, point_arrivee, statut, type_trajet, commentaire)");
                            System.out.println("VALUES ('Pierre Durand', '2023-05-25', 'Bordeaux', 'Toulouse', 'Confirme', 'Aller simple', 'Bagage volumineux');");
                            System.out.println("--------------------------------------------------------------");
                        } else {
                            // Afficher les réservations existantes
                            try (ResultSet rsReservations = stmt.executeQuery("SELECT id, nom_passager, date_reservation, point_depart, point_arrivee, statut FROM reservation LIMIT 10")) {
                                System.out.println("\nReservations existantes (10 premieres):");
                                System.out.println("----------------------------------------");
                                while (rsReservations.next()) {
                                    System.out.printf("ID: %d | Passager: %s | Date: %s | Trajet: %s -> %s | Statut: %s%n",
                                        rsReservations.getInt("id"),
                                        rsReservations.getString("nom_passager"),
                                        rsReservations.getDate("date_reservation"),
                                        rsReservations.getString("point_depart"),
                                        rsReservations.getString("point_arrivee"),
                                        rsReservations.getString("statut")
                                    );
                                }
                            }
                        }
                    }
                }
            } else {
                System.err.println("ECHEC: La table 'reservation' n'existe pas!");
                System.out.println("\nVous devez creer la table reservation. Voici le script SQL:");
                System.out.println("--------------------------------------------------------------");
                System.out.println("CREATE TABLE IF NOT EXISTS reservation (");
                System.out.println("    id INT AUTO_INCREMENT PRIMARY KEY,");
                System.out.println("    nom_passager VARCHAR(100) NOT NULL,");
                System.out.println("    date_reservation DATE NOT NULL,");
                System.out.println("    point_depart VARCHAR(100) NOT NULL,");
                System.out.println("    point_arrivee VARCHAR(100) NOT NULL,");
                System.out.println("    statut VARCHAR(50) NOT NULL DEFAULT 'En attente',");
                System.out.println("    type_trajet VARCHAR(50) NOT NULL,");
                System.out.println("    commentaire TEXT");
                System.out.println(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
                System.out.println("--------------------------------------------------------------");
            }
        } catch (SQLException e) {
            System.err.println("ECHEC: Erreur lors de la verification de la table: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=============================================");
    }
} 