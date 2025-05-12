package utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Classe utilitaire pour tester la connexion à la base de données et vérifier la structure
 */
public class DatabaseTest {

    public static void main(String[] args) {
        testConnection();
    }
    
    public static void testConnection() {
        System.out.println("=== TEST DE CONNEXION À LA BASE DE DONNÉES ===");
        Connection connection = MyConnection.getConnection();
        
        if (connection != null) {
            System.out.println("✅ Connexion à la base de données réussie!");
            
            try {
                // Obtenir les métadonnées de la base de données
                DatabaseMetaData metaData = connection.getMetaData();
                System.out.println("Serveur de base de données: " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
                System.out.println("Pilote JDBC: " + metaData.getDriverName() + " " + metaData.getDriverVersion());
                
                // Vérifier les tables existantes
                System.out.println("\n=== TABLES DANS LA BASE DE DONNÉES ===");
                ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
                boolean hasTables = false;
                
                while (tables.next()) {
                    hasTables = true;
                    String tableName = tables.getString("TABLE_NAME");
                    System.out.println("- Table: " + tableName);
                    
                    // Pour chaque table, lister les colonnes
                    System.out.println("  Colonnes:");
                    ResultSet columns = metaData.getColumns(null, null, tableName, "%");
                    while (columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME");
                        String columnType = columns.getString("TYPE_NAME");
                        String columnSize = columns.getString("COLUMN_SIZE");
                        String nullable = columns.getInt("NULLABLE") == 1 ? "NULL" : "NOT NULL";
                        System.out.println("    - " + columnName + " : " + columnType + 
                                "(" + columnSize + ") " + nullable);
                    }
                    System.out.println();
                }
                
                if (!hasTables) {
                    System.out.println("❌ Aucune table trouvée dans la base de données!");
                    System.out.println("Assurez-vous d'exécuter le script SQL pour créer les tables nécessaires.");
                }
                
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de l'accès aux métadonnées: " + e.getMessage());
                e.printStackTrace();
            }
            
        } else {
            System.err.println("❌ Échec de la connexion à la base de données!");
            System.err.println("Vérifiez les paramètres de connexion et assurez-vous que le serveur MySQL est en cours d'exécution.");
        }
        
        System.out.println("\n=== CONSEILS DE DÉPANNAGE ===");
        System.out.println("1. Vérifiez que le serveur MySQL est en cours d'exécution sur localhost:3306");
        System.out.println("2. Assurez-vous que la base de données 'lastbd' existe");
        System.out.println("3. Vérifiez que l'utilisateur 'root' a accès sans mot de passe");
        System.out.println("4. Exécutez le script SQL 'database_setup.sql' pour créer les tables nécessaires");
        
        // Fermer la connexion
        MyConnection.closeConnection();
    }
} 