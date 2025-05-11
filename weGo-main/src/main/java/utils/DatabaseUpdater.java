package utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class to update the database schema
 */
public class DatabaseUpdater {
    
    /**
     * Updates the reclamation table with new intelligent features columns
     * @return true if successful
     */
    public static boolean updateReclamationTable() {
        Connection conn = null;
        Statement stmt = null;
        boolean success = false;
        
        try {
            conn = MyConnection.getInstance().getConnection();
            
            // First, verify if there are any database structure issues
            if (isTableCorrupted(conn, "reclamation")) {
                // If so, recreate the table with proper structure
                fixTableStructure(conn, "reclamation");
                return true;
            }
            
            // Check if columns already exist
            List<String> existingColumns = getTableColumns(conn, "reclamation");
            List<String> requiredColumns = Arrays.asList(
                "latitude", "longitude", "adresse", "ticket_id", 
                "priorite", "categorie", "gravite"
            );
            
            List<String> columnsToAdd = new ArrayList<>();
            for (String col : requiredColumns) {
                if (!existingColumns.contains(col.toLowerCase())) {
                    columnsToAdd.add(col);
                }
            }
            
            if (columnsToAdd.isEmpty()) {
                System.out.println("All required columns already exist in reclamation table");
                return true;
            }
            
            // Add missing columns
            stmt = conn.createStatement();
            StringBuilder alterSql = new StringBuilder("ALTER TABLE reclamation ");
            
            for (int i = 0; i < columnsToAdd.size(); i++) {
                String col = columnsToAdd.get(i);
                if (i > 0) {
                    alterSql.append(", ");
                }
                
                switch (col) {
                    case "latitude":
                    case "longitude":
                        alterSql.append("ADD COLUMN ").append(col).append(" DOUBLE DEFAULT 0");
                        break;
                    case "adresse":
                        alterSql.append("ADD COLUMN ").append(col).append(" VARCHAR(255)");
                        break;
                    case "ticket_id":
                        alterSql.append("ADD COLUMN ").append(col).append(" VARCHAR(20)");
                        break;
                    case "priorite":
                        alterSql.append("ADD COLUMN ").append(col).append(" INT DEFAULT 3");
                        break;
                    case "categorie":
                        alterSql.append("ADD COLUMN ").append(col).append(" VARCHAR(50)");
                        break;
                    case "gravite":
                        alterSql.append("ADD COLUMN ").append(col).append(" VARCHAR(20)");
                        break;
                }
            }
            
            if (columnsToAdd.size() > 0) {
                System.out.println("Executing SQL: " + alterSql.toString());
                stmt.executeUpdate(alterSql.toString());
                System.out.println("Database schema updated successfully!");
                
                // Update existing records with default values
                updateExistingRecords(conn, columnsToAdd);
            }
            
            success = true;
        } catch (Exception e) {
            System.err.println("Error updating database schema: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return success;
    }
    
    /**
     * Checks if the table structure is corrupted or has issues
     */
    private static boolean isTableCorrupted(Connection conn, String tableName) {
        try {
            // Try to perform a simple query to check if the table is usable
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
            rs.next();
            rs.close();
            stmt.close();
            return false; // Table is fine
        } catch (Exception e) {
            System.err.println("Table " + tableName + " appears to be corrupted: " + e.getMessage());
            return true; // Table has issues
        }
    }
    
    /**
     * Recreates the reclamation table with the correct structure
     */
    private static void fixTableStructure(Connection conn, String tableName) {
        try {
            Statement stmt = conn.createStatement();
            
            // Vérifier si la table existe
            ResultSet tables = conn.getMetaData().getTables(null, null, tableName, null);
            boolean tableExists = tables.next();
            tables.close();
            
            if (tableExists) {
                // Avant toute modification, vérifier les clés primaires existantes
                boolean multiplePrimaryKeys = false;
                List<String> primaryKeyColumns = new ArrayList<>();
                
                try {
                    // Obtenir les informations sur les clés primaires
                    ResultSet primaryKeys = conn.getMetaData().getPrimaryKeys(null, null, tableName);
                    while (primaryKeys.next()) {
                        String columnName = primaryKeys.getString("COLUMN_NAME");
                        primaryKeyColumns.add(columnName);
                    }
                    primaryKeys.close();
                    
                    if (primaryKeyColumns.size() > 1) {
                        multiplePrimaryKeys = true;
                        System.out.println("Multiple primary keys detected: " + String.join(", ", primaryKeyColumns));
                    }
                } catch (Exception e) {
                    System.err.println("Error checking primary keys: " + e.getMessage());
                }
                
                // Si plusieurs clés primaires sont détectées, supprimer les contraintes
                if (multiplePrimaryKeys) {
                    System.out.println("Removing multiple primary key constraints...");
                    try {
                        // Désactiver les contraintes
                        stmt.executeUpdate("SET FOREIGN_KEY_CHECKS=0");
                        
                        // Récupérer le nom de la contrainte de clé primaire
                        ResultSet constraints = stmt.executeQuery(
                            "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '" + tableName + "' " +
                            "AND CONSTRAINT_TYPE = 'PRIMARY KEY'"
                        );
                        
                        if (constraints.next()) {
                            String constraintName = constraints.getString("CONSTRAINT_NAME");
                            System.out.println("Dropping primary key constraint: " + constraintName);
                            stmt.executeUpdate("ALTER TABLE " + tableName + " DROP PRIMARY KEY");
                        }
                        constraints.close();
                        
                        // Réactiver les contraintes
                        stmt.executeUpdate("SET FOREIGN_KEY_CHECKS=1");
                    } catch (Exception e) {
                        System.err.println("Error removing primary keys: " + e.getMessage());
                        // Si on ne peut pas supprimer les clés, on va recréer la table
                    }
                }
                
                // Vérifier si la colonne id existe et a AUTO_INCREMENT
                boolean needToFixId = false;
                boolean hasIdColumn = false;
                
                ResultSet columns = stmt.executeQuery("DESCRIBE " + tableName);
                while (columns.next()) {
                    if (columns.getString("Field").equals("id")) {
                        hasIdColumn = true;
                        String extra = columns.getString("Extra");
                        String key = columns.getString("Key");
                        if (extra == null || !extra.contains("auto_increment") || !key.equals("PRI")) {
                            needToFixId = true;
                        }
                        break;
                    }
                }
                columns.close();
                
                if (hasIdColumn && needToFixId) {
                    // Si la colonne id existe mais n'est pas AUTO_INCREMENT ou pas PRIMARY KEY, on la modifie
                    System.out.println("Fixing id column to use AUTO_INCREMENT...");
                    try {
                        // D'abord essayer de supprimer la contrainte PRIMARY KEY si elle existe
                        try {
                            stmt.executeUpdate("ALTER TABLE " + tableName + " DROP PRIMARY KEY");
                        } catch (Exception e) {
                            // Ignorer si la contrainte n'existe pas
                        }
                        
                        // Ensuite ajouter la nouvelle contrainte PRIMARY KEY avec AUTO_INCREMENT
                        stmt.executeUpdate("ALTER TABLE " + tableName + " MODIFY id INT AUTO_INCREMENT PRIMARY KEY");
                        System.out.println("Column id fixed successfully!");
                        return;
                    } catch (Exception e) {
                        System.err.println("Error fixing id column: " + e.getMessage());
                        // Continuer avec la reconstruction complète
                    }
                } else if (!hasIdColumn) {
                    // Si la colonne id n'existe pas, on essaie de l'ajouter
                    System.out.println("Adding id column with AUTO_INCREMENT...");
                    try {
                        stmt.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN id INT AUTO_INCREMENT PRIMARY KEY FIRST");
                        System.out.println("Column id added successfully!");
                        return;
                    } catch (Exception e) {
                        System.err.println("Error adding id column: " + e.getMessage());
                        // Continuer avec la reconstruction complète
                    }
                } else {
                    // La colonne id existe et est correctement configurée
                    System.out.println("id column is correctly configured");
                    return;
                }
            }
            
            // Si on arrive ici, on doit reconstruire complètement la table
            // Backup existing data if possible
            System.out.println("Backing up existing reclamation data...");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS reclamation_backup AS SELECT * FROM reclamation");
            
            // Disable foreign key checks
            System.out.println("Disabling foreign key checks...");
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS=0");
            
            try {
                // Drop the corrupted table
                System.out.println("Dropping the corrupted table...");
                stmt.executeUpdate("DROP TABLE IF EXISTS reclamation");
                
                // Create the table with the correct structure
                System.out.println("Creating reclamation table with correct structure...");
                String createTableSQL = "CREATE TABLE reclamation (" +
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
                stmt.executeUpdate(createTableSQL);
                
                // Try to restore data from backup if possible
                try {
                    System.out.println("Restoring data from backup...");
                    
                    // Vérifier la structure de la table de sauvegarde pour déterminer les colonnes à copier
                    List<String> backupColumns = getTableColumns(conn, "reclamation_backup");
                    List<String> baseColumns = Arrays.asList("titre", "description", "date", "statut", "id_utilisateur");
                    
                    StringBuilder columnsToRestore = new StringBuilder();
                    for (String col : baseColumns) {
                        if (backupColumns.contains(col.toLowerCase())) {
                            if (columnsToRestore.length() > 0) {
                                columnsToRestore.append(", ");
                            }
                            columnsToRestore.append(col);
                        }
                    }
                    
                    if (columnsToRestore.length() > 0) {
                        String insertSQL = "INSERT INTO reclamation (" + columnsToRestore.toString() + ") " +
                                           "SELECT " + columnsToRestore.toString() + " FROM reclamation_backup";
                        System.out.println("Executing restore SQL: " + insertSQL);
                        stmt.executeUpdate(insertSQL);
                    System.out.println("Data restored successfully!");
                    } else {
                        System.out.println("No matching columns found for restoration");
                    }
                    
                } catch (Exception e) {
                    System.err.println("Could not restore data: " + e.getMessage());
                }
            } finally {
                // Re-enable foreign key checks
                System.out.println("Re-enabling foreign key checks...");
                stmt.executeUpdate("SET FOREIGN_KEY_CHECKS=1");
            }
            
            stmt.close();
            System.out.println("Table structure has been fixed!");
        } catch (Exception e) {
            System.err.println("Error fixing table structure: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Updates existing records with default values for new columns
     */
    private static void updateExistingRecords(Connection conn, List<String> newColumns) {
        try {
            Statement stmt = conn.createStatement();
            
            // Prepare update SQL for each new column
            if (newColumns.contains("ticket_id")) {
                stmt.executeUpdate("UPDATE reclamation SET ticket_id = CONCAT(YEAR(date), '-', LPAD(MONTH(date), 2, '0'), '-', LEFT(UUID(), 4)) WHERE ticket_id IS NULL");
                System.out.println("Updated ticket_id for existing records");
            }
            
            if (newColumns.contains("priorite")) {
                stmt.executeUpdate("UPDATE reclamation SET priorite = 3 WHERE priorite IS NULL");
                System.out.println("Updated priorite for existing records");
            }
            
            if (newColumns.contains("categorie")) {
                stmt.executeUpdate("UPDATE reclamation SET categorie = 'autre' WHERE categorie IS NULL");
                System.out.println("Updated categorie for existing records");
            }
            
            if (newColumns.contains("gravite")) {
                stmt.executeUpdate("UPDATE reclamation SET gravite = 'moyenne' WHERE gravite IS NULL");
                System.out.println("Updated gravite for existing records");
            }
            
            stmt.close();
            System.out.println("Existing records updated with default values");
        } catch (Exception e) {
            System.err.println("Error updating existing records: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gets a list of column names for a table
     */
    private static List<String> getTableColumns(Connection conn, String tableName) {
        List<String> columns = new ArrayList<>();
        
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, tableName, null);
            
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
            
            rs.close();
        } catch (Exception e) {
            System.err.println("Error getting table columns: " + e.getMessage());
        }
        
        return columns;
    }
    
    /**
     * Updates the user table with profile columns
     * @return true if successful
     */
    public static boolean updateUserTable() {
        Connection conn = null;
        Statement stmt = null;
        boolean success = false;
        
        try {
            conn = MyConnection.getInstance().getConnection();
            
            // Check if columns already exist
            List<String> existingColumns = getTableColumns(conn, "utilisateur");
            List<String> requiredColumns = Arrays.asList(
                "nom", "prenom", "telephone"
            );
            
            List<String> columnsToAdd = new ArrayList<>();
            for (String col : requiredColumns) {
                if (!existingColumns.contains(col.toLowerCase())) {
                    columnsToAdd.add(col);
                }
            }
            
            if (columnsToAdd.isEmpty()) {
                System.out.println("All required profile columns already exist in utilisateur table");
                return true;
            }
            
            // Add missing columns
            stmt = conn.createStatement();
            StringBuilder alterSql = new StringBuilder("ALTER TABLE utilisateur ");
            
            for (int i = 0; i < columnsToAdd.size(); i++) {
                String col = columnsToAdd.get(i);
                if (i > 0) {
                    alterSql.append(", ");
                }
                
                // All profile fields are VARCHAR
                alterSql.append("ADD COLUMN ").append(col).append(" VARCHAR(100)");
            }
            
            if (columnsToAdd.size() > 0) {
                System.out.println("Executing SQL: " + alterSql.toString());
                stmt.executeUpdate(alterSql.toString());
                System.out.println("User table schema updated successfully!");
            }
            
            success = true;
        } catch (Exception e) {
            System.err.println("Error updating user table schema: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return success;
    }
} 