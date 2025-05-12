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
                    "gravite VARCHAR(20) DEFAULT 'moyenne', " +
                    "FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id)" +
                    ")";
                stmt.executeUpdate(createTableSQL);
                
                // Try to restore data from backup if possible
                try {
                    System.out.println("Restoring data from backup...");
                    stmt.executeUpdate("INSERT INTO reclamation (id, titre, description, date, statut, id_utilisateur) " +
                                       "SELECT id, titre, description, date, statut, id_utilisateur FROM reclamation_backup");
                    System.out.println("Data restored successfully!");
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