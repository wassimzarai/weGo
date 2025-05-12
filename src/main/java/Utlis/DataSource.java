package Utlis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private static DataSource instance;
    private Connection connection;

    private final String URL = "jdbc:mysql://localhost:3306/app-covoiturage?useSSL=false&serverTimezone=UTC";
    private final String USERNAME = "root";
    private final String PASSWORD = "";

    private DataSource() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexion établie à la base de données !");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion à la base de données", e);
        }
    }

    public static DataSource getInstance() {
        if (instance == null) {
            instance = new DataSource();
        } else {
            try {
                if (instance.getConnection().isClosed()) {
                    instance = new DataSource();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erreur lors de la vérification de la connexion", e);
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
