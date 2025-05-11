package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class database {

    private static database instance;
    public Connection connectDb;

    private database() {
        // Constructeur privé pour singleton
    }

    public static database getInstance() {
        if (instance == null) {
            instance = new database();
        } else {
            try {
                if (instance.connectDb().isClosed()) {
                    instance = new database();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erreur lors de la vérification de la connexion", e);
            }
        }
        return instance;
    }

    public static Connection connectDb() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/lastbd", "root", "");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Connection getConnection() {
        return connectDb;
    }
}
