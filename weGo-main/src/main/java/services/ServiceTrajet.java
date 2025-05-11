package services;
import Entites.Trajet;
import utils.database;
import Entites.StatutTrajet;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Entites.Voiture;
import Entites.Pointrdv;


public class ServiceTrajet {
    public Connection connectDb;
    private Statement ste;
    public ServiceTrajet() {
        connectDb = database.getInstance().connectDb();
        try {
            ste = connectDb.createStatement(); // ici l'initialisation
        } catch (SQLException e) {
            System.out.println("Erreur lors de la création du Statement : " + e.getMessage());
        }
    }

    //    public boolean ajouter(Personne p) throws SQLException {
//        String req = "INSERT INTO personne (nom, prenom, age) VALUES ('" +
//                p.getNom() + "', '" + p.getPrenom() + "', " + p.getAge() + ")";
//        int res = ste.executeUpdate(req);
//        return res > 0;
//    }
    public boolean ajouter(Trajet t) throws SQLException {
//        String req = "INSERT INTO `trajets` ( depart, arrivee, date, heure, prix_place,  nb_place) VALUES ( ?, ?, ?, ?, ?, ?)";
//
//        PreparedStatement ps = con2.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
//
//
//        ps.setString(1, t.getDepart());
//        ps.setString(2, t.getArrivee());
//        ps.setDate(3, java.sql.Date.valueOf(t.getDate()));
//        ps.setTime(4, java.sql.Time.valueOf(t.getHeure()));
//        ps.setInt(5,t.getPrix_place());
//
//        ps.setInt(6,t.getNb_place());
//        Pointrdv p=new Pointrdv();
////
//        String req2= "INSERT INTO `point_rdv` (id_trajet, adresse) VALUES (?, ?)";
//        PreparedStatement ps2 = con2.prepareStatement(req2);
//        ps2.setInt(1,p.getId_trajet());
//        ps2.setString(2,p.getAdresse());
//
//
//
//
//
//
//        int res = ps.executeUpdate();
//
//        if (res > 0) {
//            // Récupérer l'ID généré automatiquement
//            ResultSet rs = ps.getGeneratedKeys();
//            if (rs.next()) {
//                int id = rs.getInt(1); // Premier champ retourné = ID
//                t.setId_trajet(id); // On le met dans l'objet Personne
//                System.out.println("trajet insérée avec ID = " + id);
//            }
//            return true;
//        } else {
//            return false;
//        }
//
        String req = "INSERT INTO `trajets` (depart, arrivee, date, heure, prix_place, nb_place) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connectDb.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, t.getDepart());
        ps.setString(2, t.getArrivee());
        ps.setDate(3, java.sql.Date.valueOf(t.getDate()));
        ps.setTime(4, java.sql.Time.valueOf(t.getHeure()));
        ps.setInt(5, t.getPrix_place());
        ps.setInt(6, t.getNb_place());

        int res = ps.executeUpdate();

        if (res > 0) {
            // 2. Récupérer l’ID du trajet inséré
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1); // ID généré
                t.setId_trajet(id);    // On le stocke dans l'objet trajet

                System.out.println("Trajet inséré avec ID = " + id);

                // 3. Récupérer le point de RDV depuis l'objet trajet
                Pointrdv p = t.getPointrdv();

                // Vérifie que le point de RDV n'est pas vide
                if (p != null && p.getAdresse() != null && !p.getAdresse().isEmpty()) {
                    // 4. Insérer dans la table point_rdv
                    String req2 = "INSERT INTO `point_rdv` (id_trajet, adresse) VALUES (?, ?)";
                    PreparedStatement ps2 = connectDb.prepareStatement(req2);
                    ps2.setInt(1, id); // clé étrangère vers le trajet
                    ps2.setString(2, p.getAdresse());

                    ps2.executeUpdate();
                    System.out.println("Point de rendez-vous ajouté.");
                } else {
                    System.out.println("Aucun point de RDV à insérer.");
                }

                return true;
            }
        }

        return false; // insertion échouée


    }


    //    public boolean update(Personne p) throws SQLException {
//        String req = "UPDATE personne SET nom='" + p.getNom() +
//                "', prenom='" + p.getPrenom() +
//                "', age=" + p.getAge() +
//                " WHERE id=" + p.getId();
//        int res = ste.executeUpdate(req);
//        return res > 0;
//    }
    public boolean update(Trajet t) throws SQLException {
//        String req = "UPDATE `trajets` SET depart = ?, arrivee = ?, date = ?, heure = ?, prix_place = ?, nb_place = ? WHERE `trajets`.`id_trajet` = ? ";
//
//
//
//        PreparedStatement ps = con2.prepareStatement(req);
//
//        ps.setString(1, t.getDepart());
//        ps.setString(2, t.getArrivee());
//        ps.setDate(3, java.sql.Date.valueOf(t.getDate()));
//        ps.setTime(4, java.sql.Time.valueOf(t.getHeure()));
//        ps.setInt(5,t.getPrix_place());
//        ps.setInt(6,t.getNb_place());
//        ps.setInt(7,t.getId_trajet());
//
//
//
//        int res = ps.executeUpdate();
//
//        if (res > 0) {
//            System.out.println("Update réussi pour id = " + t.getId_trajet());
//            return true;
//        } else {
//            System.out.println("Aucun enregistrement modifié. ID = " + t.getId_trajet());
//            return false;
//        }
        String req = "UPDATE `trajets` SET depart = ?, arrivee = ?, date = ?, heure = ?, prix_place = ?, nb_place = ? WHERE id_trajet = ?";
        PreparedStatement ps = connectDb.prepareStatement(req);

        ps.setString(1, t.getDepart());
        ps.setString(2, t.getArrivee());
        ps.setDate(3, java.sql.Date.valueOf(t.getDate()));
        ps.setTime(4, java.sql.Time.valueOf(t.getHeure()));
        ps.setInt(5, t.getPrix_place());
        ps.setInt(6, t.getNb_place());
        ps.setInt(7, t.getId_trajet());

        int res = ps.executeUpdate();

        // 2. Si la mise à jour du trajet a réussi
        if (res > 0) {
            System.out.println("Update réussi pour trajet ID = " + t.getId_trajet());

            // 3. Mettre à jour le point de RDV
            Pointrdv p = t.getPointrdv();
            if (p != null && p.getAdresse() != null) {
                String req2 = "UPDATE point_rdv SET adresse = ? WHERE id_trajet = ?";
                PreparedStatement ps2 = connectDb.prepareStatement(req2);
                ps2.setString(1, p.getAdresse());
                ps2.setInt(2, t.getId_trajet());

                int res2 = ps2.executeUpdate();
                if (res2 > 0) {
                    System.out.println("Point de RDV mis à jour pour trajet ID = " + t.getId_trajet());
                } else {
                    System.out.println("Aucun point de RDV mis à jour.");
                }
            }

            return true;
        } else {
            System.out.println("Aucun trajet modifié. ID = " + t.getId_trajet());
            return false;
        }
    }


    public boolean delete(Trajet t) throws SQLException {
        String req = "DELETE FROM trajets WHERE ID_trajet = ?";
        PreparedStatement ps = connectDb.prepareStatement(req);
        ps.setInt(1, t.getId_trajet());

        int res = ps.executeUpdate();

        if (res > 0) {
            System.out.println("Suppression réussie pour ID = " + t.getId_trajet());
            return true;
        } else {
            System.out.println("Aucune personne trouvée avec ID = " + t.getId_trajet());
            return false;
        }
    }

    //    public Trajet getById(int id) throws SQLException {
//        // Utilisation de PreparedStatement pour éviter les injections SQL
//        String req = "SELECT * FROM Trajet WHERE ID_trajet = ?";
//
//        // Création d'un PreparedStatement
//        PreparedStatement pstmt = ste.getConnection().prepareStatement(req);
//        pstmt.setInt(1, id); // Remplace le "?" par l'ID de la personne
//
//        ResultSet rs = pstmt.executeQuery();
//
//        if (rs.next()) {
//            return new Trajet(
//
//                    rs.getString("depart"),
//                    rs.getString("arrivee"),
//                    rs.getDate("date").toLocalDate(),   // conversion SQL Date -> LocalDate
//                    rs.getTime("heure").toLocalTime(),   // conversion SQL Time -> LocalTime
//                    StatutTrajet.valueOf(rs.getString("statut")),  // conversion String -> Enum
//                    rs.getInt("prix_place"),rs.getInt("nb_place")
//
//            );
//        }
//        return null;
//    }
    public List<Trajet> getByArrivee(String arrivee) throws SQLException {
        List<Trajet> trajets = new ArrayList<>();

        String req = "SELECT t.*, v.id, v.marque, v.modele, v.matricule, p.adresse FROM trajets t JOIN voiture v ON t.id = v.id LEFT JOIN point_rdv p ON t.id_trajet = p.id_trajet WHERE t.arrivee LIKE ?";

        PreparedStatement pstmt = ste.getConnection().prepareStatement(req);
        pstmt.setString(1, "%" + arrivee + "%");

        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {

            Voiture voiture = new Voiture(
                    rs.getInt("id"),
                    rs.getString("marque"),
                    rs.getString("modele"),
                    rs.getString("matricule")
            );

            String adresse = rs.getString("adresse");
            Pointrdv p = (adresse != null) ? new Pointrdv(rs.getInt("id_trajet"), adresse) : null;

            trajets.add(new Trajet(
                    rs.getInt("id_trajet"),
                    rs.getString("depart"),
                    rs.getString("arrivee"),
                    rs.getDate("date").toLocalDate(),
                    rs.getTime("heure").toLocalTime(),
                    StatutTrajet.valueOf(rs.getString("statut")),
                    rs.getInt("prix_place"),
                    rs.getInt("nb_place"),
                    voiture,
                    p
            ));
        }

        return trajets;
    }



    public ArrayList<Trajet> getList() throws SQLException {
        ArrayList<Trajet> list = new ArrayList<>();
        String req = "SELECT t.*, v.id, v.marque, v.modele, v.matricule FROM trajets JOIN voiture v ON t.id = v.id ";
        ResultSet rs = ste.executeQuery(req);

        while (rs.next()) {

            Voiture voiture = new Voiture(
                    rs.getInt("id"),
                    rs.getString("marque"),
                    rs.getString("modele"),
                    rs.getString("matricule")
            );


            list.add(new Trajet( rs.getInt("id_trajet"),
                    rs.getString("depart"),
                    rs.getString("arrivee"),
                    rs.getDate("date").toLocalDate(),   // conversion SQL Date -> LocalDate
                    rs.getTime("heure").toLocalTime(),   // conversion SQL Time -> LocalTime
                    StatutTrajet.valueOf(rs.getString("statut")),  // conversion String -> Enum
                    rs.getInt("prix_place"),rs.getInt("nb_place"),voiture


            ));
        }

        return list;
    }




    public ArrayList<Trajet> getList1() throws SQLException {
        ArrayList<Trajet> list = new ArrayList<>();
        String req = "SELECT * FROM trajets";
        ResultSet rs = ste.executeQuery(req);

        while (rs.next()) {
            Voiture voiture = new Voiture(); // on crée un objet Voiture
            voiture.setId(rs.getInt("id")); // on récupère l'id_voiture

            Trajet trajet = new Trajet(
                    rs.getInt("id_trajet"),
                    rs.getString("depart"),
                    rs.getString("arrivee"),
                    rs.getDate("date").toLocalDate(),
                    rs.getTime("heure").toLocalTime(),
                    StatutTrajet.valueOf(rs.getString("statut")),
                    rs.getInt("prix_place"),
                    rs.getInt("nb_place"),
                    voiture // on injecte l’objet voiture avec juste l'id
            );

            list.add(trajet);
        }

        return list;
    }

    public Map<String, Integer> getNombreTrajetsParArrivee() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT arrivee, COUNT(*) AS nombre FROM trajets GROUP BY arrivee";


        ResultSet rs = ste.executeQuery(sql);

        while (rs.next()) {
            String ville = rs.getString("arrivee");
            int count = rs.getInt("nombre");
            stats.put(ville, count);
        }
        return stats;
    }
    public Pointrdv getByIdTrajet(int idTrajet) throws SQLException {
        String req = "SELECT * FROM point_rdv WHERE id_trajet = ?";
        PreparedStatement ps = connectDb.prepareStatement(req);
        ps.setInt(1, idTrajet);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Pointrdv p = new Pointrdv();
            p.setId_trajet(rs.getInt("id_trajet"));
            p.setAdresse(rs.getString("adresse"));
            return p;
        }
        return null;
    }
    public ArrayList<Trajet> recupererTousLesTrajets() throws SQLException {
        ArrayList<Trajet> list = new ArrayList<>();
        String req = "SELECT * FROM trajets";
        ResultSet rs = ste.executeQuery(req);

        while (rs.next()) {
            Voiture voiture = new Voiture();
            voiture.setId(rs.getInt("id"));

            Trajet trajet = new Trajet(
                    rs.getInt("id_trajet"),
                    rs.getString("depart"),
                    rs.getString("arrivee"),
                    rs.getDate("date").toLocalDate(),
                    rs.getTime("heure").toLocalTime(),
                    StatutTrajet.valueOf(rs.getString("statut")),
                    rs.getInt("prix_place"),
                    rs.getInt("nb_place"),
                    voiture
            );

            list.add(trajet);
        }

        return list;
    }









}