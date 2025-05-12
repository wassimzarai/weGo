package services;

import entities.Reclamation;
import entities.User;
import utils.MyConnection;
import utils.NLPAnalyzer;
import utils.NotificationService;
import utils.GeoLocationService;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService implements IService<Reclamation> {
    private Connection conn;

    public ReclamationService() {
        conn = MyConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Reclamation reclamation) {
        // Ensure statut is initialized
        if (reclamation.getStatut() == null || reclamation.getStatut().isEmpty()) {
            reclamation.setStatut("ouvert");
        }
        
        // Analyse NLP pour déterminer la gravité, la catégorie et la priorité
        NLPAnalyzer.analyseReclamation(reclamation);
        
        // Generate ticket ID if not already set
        if (reclamation.getTicketId() == null || reclamation.getTicketId().isEmpty()) {
            reclamation.setTicketId(reclamation.generateTicketId());
        }
        
        String query = "INSERT INTO reclamation (titre, description, date, statut, id_utilisateur, " +
                       "latitude, longitude, adresse, ticket_id, priorite, categorie, gravite) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            // Debug output
            System.out.println("Adding reclamation with statut: " + reclamation.getStatut());
            
            pst.setString(1, reclamation.getTitre());
            pst.setString(2, reclamation.getDescription());
            pst.setDate(3, Date.valueOf(reclamation.getDate()));
            pst.setString(4, reclamation.getStatut());
            pst.setInt(5, reclamation.getUser().getId());
            pst.setDouble(6, reclamation.getLatitude());
            pst.setDouble(7, reclamation.getLongitude());
            pst.setString(8, reclamation.getAdresse());
            pst.setString(9, reclamation.getTicketId());
            pst.setInt(10, reclamation.getPriorite());
            pst.setString(11, reclamation.getCategorie());
            pst.setString(12, reclamation.getGravite());

            int affectedRows = pst.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating reclamation failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reclamation.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating reclamation failed, no ID obtained.");
                }
            }
            
            // Envoyer une notification de création
            User completeUser = getUserById(reclamation.getUser().getId());
            if (completeUser != null) {
                NotificationService.envoyerNotifications(completeUser, reclamation, "creation");
            }
            
            System.out.println("Réclamation ajoutée avec succès");
        } catch (SQLException ex) {
            System.out.println("Erreur lors de l'ajout de la réclamation: " + ex.getMessage());
            ex.printStackTrace();
            
            // If the error is related to database structure, try to fix it
            if (ex.getMessage().contains("Column") || ex.getMessage().contains("column") || 
                ex.getMessage().contains("Dalumn") || ex.getMessage().contains("Unknown")) {
                System.out.println("Trying to fix database structure and retry...");
                try {
                    utils.DatabaseUpdater.updateReclamationTable();
                    // Try again after fixing structure
                    this.ajouter(reclamation);
                } catch (Exception e) {
                    System.err.println("Failed to fix database and retry: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void modifier(Reclamation reclamation) {
        try {
            // Récupérer l'ancien statut pour vérifier s'il a changé
            String oldStatus = getById(reclamation.getId()).getStatut();
            boolean statusChanged = !oldStatus.equals(reclamation.getStatut());
            
            String query = "UPDATE reclamation SET titre=?, description=?, statut=?, " +
                           "latitude=?, longitude=?, adresse=?, priorite=?, categorie=?, gravite=? " +
                           "WHERE id=?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, reclamation.getTitre());
                pst.setString(2, reclamation.getDescription());
                pst.setString(3, reclamation.getStatut());
                pst.setDouble(4, reclamation.getLatitude());
                pst.setDouble(5, reclamation.getLongitude());
                pst.setString(6, reclamation.getAdresse());
                pst.setInt(7, reclamation.getPriorite());
                pst.setString(8, reclamation.getCategorie());
                pst.setString(9, reclamation.getGravite());
                pst.setInt(10, reclamation.getId());

                pst.executeUpdate();
                
                // Envoyer des notifications en cas de changement de statut
                if (statusChanged) {
                    User completeUser = getUserById(reclamation.getUser().getId());
                    if (completeUser != null) {
                        String notificationType = "statut";
                        if (reclamation.getStatut().equals("resolu")) {
                            notificationType = "resolution";
                        }
                        NotificationService.envoyerNotifications(completeUser, reclamation, notificationType);
                    }
                }
                
                System.out.println("Réclamation modifiée avec succès");
            }
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la modification de la réclamation: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void supprimer(int id) {
        String query = "DELETE FROM reclamation WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Réclamation supprimée avec succès");
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la suppression de la réclamation: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public List<Reclamation> voir() {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT r.*, u.username FROM reclamation r JOIN utilisateur u ON r.id_utilisateur = u.id";
        
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Reclamation r = creerReclamationDepuisResultSet(rs);
                reclamations.add(r);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la récupération des réclamations: " + ex.getMessage());
            ex.printStackTrace();
        }
        return reclamations;
    }

    @Override
    public Reclamation getById(int id) {
        String query = "SELECT r.*, u.username FROM reclamation r JOIN utilisateur u ON r.id_utilisateur = u.id WHERE r.id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return creerReclamationDepuisResultSet(rs);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la récupération de la réclamation: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    public List<Reclamation> getByUser(int userId) {
        List<Reclamation> list = new ArrayList<>();
        String query = "SELECT r.*, u.username FROM reclamation r LEFT JOIN utilisateur u ON r.id_utilisateur = u.id WHERE r.id_utilisateur = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Reclamation reclamation = creerReclamationDepuisResultSet(rs);
                list.add(reclamation);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return list;
    }
    
    // Méthodes utilitaires
    
    private Reclamation creerReclamationDepuisResultSet(ResultSet rs) throws SQLException {
        Reclamation r = new Reclamation();
        r.setId(rs.getInt("id"));
        r.setTitre(rs.getString("titre"));
        r.setDescription(rs.getString("description"));
        r.setDate(rs.getDate("date").toLocalDate());
        
        // Handle status with a default value if null
        String statut = rs.getString("statut");
        if (statut == null) {
            statut = "ouvert";
        }
        r.setStatut(statut);
        
        // Créer un User basique avec id et username
        User user = new User();
        user.setId(rs.getInt("id_utilisateur"));
        user.setUsername(rs.getString("username"));
        r.setUser(user);
        
        // Récupérer les nouveaux champs s'ils existent
        try {
            // Set default values for new fields
            r.setLatitude(rs.getDouble("latitude"));
            r.setLongitude(rs.getDouble("longitude"));
            
            String adresse = rs.getString("adresse");
            r.setAdresse(adresse); // Can be null
            
            String ticketId = rs.getString("ticket_id");
            if (ticketId == null || ticketId.isEmpty()) {
                r.setTicketId(r.generateTicketId());
            } else {
                r.setTicketId(ticketId);
            }
            
            int priorite = rs.getInt("priorite");
            if (rs.wasNull()) {
                priorite = 3;
            }
            r.setPriorite(priorite);
            
            String categorie = rs.getString("categorie");
            r.setCategorie(categorie); // Can be null
            
            String gravite = rs.getString("gravite");
            r.setGravite(gravite); // Will handle null in setGravite method
            
        } catch (SQLException ex) {
            // Ignorer les erreurs si les colonnes n'existent pas encore
            System.out.println("Certaines colonnes ne sont pas disponibles: " + ex.getMessage());
        }
        
        return r;
    }
    
    /**
     * Met à jour les coordonnées GPS et l'adresse d'une réclamation
     * @param reclamation La réclamation à mettre à jour
     * @param adresse L'adresse à géocoder
     */
    public void mettreAJourGeolocalisation(Reclamation reclamation, String adresse) {
        double[] coordonnees = GeoLocationService.getCoordonnees(adresse);
        if (coordonnees != null) {
            reclamation.setLatitude(coordonnees[0]);
            reclamation.setLongitude(coordonnees[1]);
            reclamation.setAdresse(adresse);
            modifier(reclamation);
        }
    }
    
    /**
     * Récupère les réclamations autour d'un point géographique
     * @param latitude La latitude du point central
     * @param longitude La longitude du point central
     * @param rayonKm Le rayon de recherche en kilomètres
     * @return Liste des réclamations dans la zone spécifiée
     */
    public List<Reclamation> getReclamationsParZone(double latitude, double longitude, double rayonKm) {
        List<Reclamation> resultat = new ArrayList<>();
        List<Reclamation> toutesReclamations = voir();
        
        for (Reclamation r : toutesReclamations) {
            // Si la réclamation a des coordonnées
            if (r.getLatitude() != 0 && r.getLongitude() != 0) {
                double distance = GeoLocationService.calculerDistance(
                    latitude, longitude, r.getLatitude(), r.getLongitude());
                
                if (distance <= rayonKm) {
                    resultat.add(r);
                }
            }
        }
        
        return resultat;
    }
    
    /**
     * Récupère l'utilisateur complet à partir de son ID
     * @param userId ID de l'utilisateur
     * @return L'utilisateur complet ou null
     */
    private User getUserById(int userId) {
        String query = "SELECT * FROM utilisateur WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    
                    // Essayer de récupérer les champs supplémentaires s'ils existent
                    try {
                        user.setNom(rs.getString("nom"));
                        user.setPrenom(rs.getString("prenom"));
                        user.setTelephone(rs.getString("telephone"));
                        
                        // Afficher des informations de débogage pour comprendre les valeurs récupérées
                        System.out.println("Utilisateur récupéré: " + user.getUsername() + 
                                          ", Email: " + user.getEmail() + 
                                          ", Téléphone: " + user.getTelephone() +
                                          ", Nom: " + user.getNom() + 
                                          ", Prénom: " + user.getPrenom());
                    } catch (SQLException ex) {
                        // Si les colonnes n'existent pas, mettre à jour la structure de la table
                        System.out.println("Colonnes de profil manquantes: " + ex.getMessage());
                        utils.DatabaseUpdater.updateUserTable();
                        
                        // Réessayer après la mise à jour de la structure
                        return getUserById(userId);
                    }
                    
                    return user;
                }
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la récupération de l'utilisateur: " + ex.getMessage());
            
            // Vérifier si l'erreur est liée à la structure de la base de données
            if (ex.getMessage().contains("Column") || ex.getMessage().contains("column")) {
                System.out.println("Tentative de correction de la structure de la table utilisateur...");
                try {
                    utils.DatabaseUpdater.updateUserTable();
                    // Réessayer après la mise à jour
                    return getUserById(userId);
                } catch (Exception e) {
                    System.err.println("Impossible de corriger la structure et de réessayer: " + e.getMessage());
                }
            }
        }
        return null;
    }
    
    /**
     * Obtient le nombre de réclamations par statut
     * @param userId ID de l'utilisateur (0 pour tous les utilisateurs)
     * @return Map des statuts et leurs nombres
     */
    public int getReclamationsCountByStatus(int userId, String status) {
        String query = "SELECT COUNT(*) as count FROM reclamation WHERE statut = ?";
        if (userId > 0) {
            query += " AND id_utilisateur = ?";
        }
        
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, status);
            if (userId > 0) {
                pst.setInt(2, userId);
            }
            
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors du comptage des réclamations: " + ex.getMessage());
        }
        
        return 0;
    }
} 