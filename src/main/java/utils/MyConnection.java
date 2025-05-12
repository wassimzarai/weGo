package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

//Singleton Design Pattern
public class MyConnection {

    private static final Logger LOGGER = Logger.getLogger(MyConnection.class.getName());
    private final String URL = "jdbc:mysql://localhost:3306/wego";
    private final String USER = "root";
    private final String PASS = "";
    private Connection connection;
    private static MyConnection instance;

    private MyConnection(){
        try {
            connection = DriverManager.getConnection(URL, USER, PASS);
            LOGGER.info("Database connection established successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to database: " + e.getMessage(), e);
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    public static MyConnection getInstance(){
        if(instance == null)
            instance = new MyConnection();
        return instance;
    }

    public Connection getConnection() {
        try {
            // Check if connection is valid, reconnect if needed
            if (connection == null || connection.isClosed()) {
                LOGGER.warning("Connection is null or closed, attempting to reconnect");
                instance = new MyConnection();
                return instance.connection;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking connection status: " + e.getMessage(), e);
        }
        return connection;
    }
}
