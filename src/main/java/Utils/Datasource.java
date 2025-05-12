package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Datasource {

    private Connection con;
    private final String url = "jdbc:mysql://localhost:3306/esprit-b10";
    private String user = "root";
    private String password = "";

    private static Datasource data;

    private Datasource() {
        try {
            con = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion établie");
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
    }

    public Connection getCon() {
        return con;
    }

    public static Datasource getInstance() {
        if (data == null) {
            data = new Datasource();
        }
        return data;
    }
}

