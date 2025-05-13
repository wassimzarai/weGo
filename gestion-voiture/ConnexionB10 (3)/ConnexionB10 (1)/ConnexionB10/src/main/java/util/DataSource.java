package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {

    private static DataSource instance;
    private Connection cnx;

    // Database connection parameters
    private final String URL = "jdbc:mysql://localhost:3306/wego";  // Adjust this URL if needed
    private final String USER = "root";  // Update this if necessary
    private final String PASS = "";  // Update password if necessary

    private DataSource() {
        try {
            // Establish connection
            cnx = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("✅ Connection to MySQL established");
        } catch (SQLException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
            e.printStackTrace();  // Print stack trace for better debugging
        }
    }

    public static DataSource getInstance() {
        if (instance == null) {
            instance = new DataSource();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
