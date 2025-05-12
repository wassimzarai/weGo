package services;

import entities.Trajet;
import entities.Conducteur;
import entities.Passager;
import entities.DemandeReservation;
import utils.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrajetService {

    private Connection connection;
    private ConducteurService conducteurService;

    public TrajetService() {
        connection = MyConnection.getConnection();
        conducteurService = new ConducteurService();
    }

    // Ajouter un nouveau trajet
    public void ajouterTrajet(Trajet trajet) {
        String sql = "INSERT INTO trajet (ville_depart, ville_arrivee, point_depart, point_arrivee, date, heure_depart, " +
                     "places_disponibles, places_reservees, prix, autorise_fumeur, autorise_animaux, autorise_gros_bagages, " +
                     "commentaires, conducteur_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, trajet.getVilleDepart());
            statement.setString(2, trajet.getVilleArrivee());
            statement.setString(3, trajet.getPointDepart());
            statement.setString(4, trajet.getPointArrivee());
            statement.setDate(5, Date.valueOf(trajet.getDate()));
            statement.setTime(6, Time.valueOf(trajet.getHeureDepart()));
            statement.setInt(7, trajet.getPlacesDisponibles());
            statement.setInt(8, trajet.getPlacesReservees());
            statement.setDouble(9, trajet.getPrix());
            statement.setBoolean(10, trajet.isAutoriseFumeur());
            statement.setBoolean(11, trajet.isAutoriseAnimaux());
            statement.setBoolean(12, trajet.isAutoriseGrossBagages());
            statement.setString(13, trajet.getCommentaires());
            
            if (trajet.getConducteur() != null) {
                statement.setInt(14, trajet.getConducteur().getId());
            } else {
                statement.setNull(14, Types.INTEGER);
            }

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        trajet.setId(generatedId);
                        System.out.println("Trajet ajouté avec succès ! ID généré : " + generatedId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupérer tous les trajets
    public List<Trajet> recupererTousTrajets() {
        List<Trajet> trajets = new ArrayList<>();
        String sql = "SELECT * FROM trajet";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Trajet trajet = extraireTrajetDuResultSet(resultSet);
                trajets.add(trajet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trajets;
    }
    
    // Récupérer un trajet par ID
    public Trajet recupererTrajetParId(int id) {
        String sql = "SELECT * FROM trajet WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extraireTrajetDuResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Récupérer les trajets disponibles par ville de départ et d'arrivée
    public List<Trajet> recupererTrajetsDisponibles(String villeDepart, String villeArrivee, LocalDate date) {
        List<Trajet> trajets = new ArrayList<>();
        String sql = "SELECT * FROM trajet WHERE ville_depart = ? AND ville_arrivee = ? AND date = ? AND places_disponibles > places_reservees";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, villeDepart);
            statement.setString(2, villeArrivee);
            statement.setDate(3, Date.valueOf(date));
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Trajet trajet = extraireTrajetDuResultSet(resultSet);
                    trajets.add(trajet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trajets;
    }
    
    // Trouver des trajets compatibles avec une demande de réservation
    public List<Trajet> trouverTrajetsCompatibles(DemandeReservation demande) {
        // D'abord, récupérer tous les trajets disponibles pour les villes et la date demandées
        List<Trajet> trajetsDisponibles = recupererTrajetsDisponibles(
                demande.getVilleDepart(), 
                demande.getVilleArrivee(), 
                demande.getDate());
        
        // Ensuite, filtrer selon les critères plus spécifiques
        return trajetsDisponibles.stream()
                .filter(demande::estCompatibleAvec)
                .collect(Collectors.toList());
    }
    
    // Trouver le meilleur trajet compatible pour une demande (matching intelligent)
    public Trajet trouverMeilleurTrajet(DemandeReservation demande) {
        List<Trajet> trajetsCompatibles = trouverTrajetsCompatibles(demande);
        
        if (trajetsCompatibles.isEmpty()) {
            return null;
        }
        
        // Algorithme de scoring pour déterminer le meilleur trajet
        return trajetsCompatibles.stream()
                .sorted((t1, t2) -> {
                    // Priorité 1: Note du conducteur (plus élevée est meilleure)
                    if (t1.getConducteur() != null && t2.getConducteur() != null) {
                        double noteT1 = t1.getConducteur().getNoteMoyenne();
                        double noteT2 = t2.getConducteur().getNoteMoyenne();
                        if (Math.abs(noteT1 - noteT2) > 0.5) { // Différence significative
                            return Double.compare(noteT2, noteT1); // Ordre décroissant
                        }
                    }
                    
                    // Priorité 2: Proximité de l'heure souhaitée
                    if (demande.getHeureSouhaitee() != null) {
                        long diffT1 = Math.abs(t1.getHeureDepart().toSecondOfDay() - demande.getHeureSouhaitee().toSecondOfDay());
                        long diffT2 = Math.abs(t2.getHeureDepart().toSecondOfDay() - demande.getHeureSouhaitee().toSecondOfDay());
                        if (Math.abs(diffT1 - diffT2) > 900) { // Différence de plus de 15 min
                            return Long.compare(diffT1, diffT2);
                        }
                    }
                    
                    // Priorité 3: Nombre de places disponibles (préférer ceux avec juste ce qu'il faut)
                    int placesRequises = demande.getNombrePlaces();
                    int diffPlacesT1 = t1.getNombrePlacesRestantes() - placesRequises;
                    int diffPlacesT2 = t2.getNombrePlacesRestantes() - placesRequises;
                    if (diffPlacesT1 != diffPlacesT2) {
                        return Integer.compare(diffPlacesT1, diffPlacesT2); // Optimiser l'allocation des places
                    }
                    
                    // Priorité 4: Prix (moins cher est mieux)
                    return Double.compare(t1.getPrix(), t2.getPrix());
                })
                .findFirst()
                .orElse(null);
    }
    
    // Chercher le prochain trajet disponible pour un itinéraire habituel (utilisé pour réservation automatique)
    public Trajet trouverProchainTrajetDisponible(String villeDepart, String villeArrivee, int nombrePlaces) {
        LocalDate aujourdhui = LocalDate.now();
        
        // Rechercher des trajets jusqu'à 7 jours dans le futur
        for (int i = 0; i < 7; i++) {
            LocalDate dateRecherche = aujourdhui.plusDays(i);
            List<Trajet> trajetsDisponibles = recupererTrajetsDisponibles(villeDepart, villeArrivee, dateRecherche);
            
            // Filtrer ceux qui ont assez de places
            List<Trajet> trajetsAvecAssezDePlaces = trajetsDisponibles.stream()
                    .filter(t -> t.getNombrePlacesRestantes() >= nombrePlaces)
                    .sorted((t1, t2) -> t1.getHeureDepart().compareTo(t2.getHeureDepart()))
                    .collect(Collectors.toList());
            
            if (!trajetsAvecAssezDePlaces.isEmpty()) {
                return trajetsAvecAssezDePlaces.get(0); // Retourner le premier disponible
            }
        }
        
        return null; // Aucun trajet disponible trouvé
    }
    
    // Méthode utilitaire pour extraire un trajet d'un ResultSet
    private Trajet extraireTrajetDuResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String villeDepart = resultSet.getString("ville_depart");
        String villeArrivee = resultSet.getString("ville_arrivee");
        String pointDepart = resultSet.getString("point_depart");
        String pointArrivee = resultSet.getString("point_arrivee");
        LocalDate date = resultSet.getDate("date").toLocalDate();
        LocalTime heureDepart = resultSet.getTime("heure_depart").toLocalTime();
        int placesDisponibles = resultSet.getInt("places_disponibles");
        int placesReservees = resultSet.getInt("places_reservees");
        double prix = resultSet.getDouble("prix");
        boolean autoriseFumeur = resultSet.getBoolean("autorise_fumeur");
        boolean autoriseAnimaux = resultSet.getBoolean("autorise_animaux");
        boolean autoriseGrossBagages = resultSet.getBoolean("autorise_gros_bagages");
        String commentaires = resultSet.getString("commentaires");
        
        Trajet trajet = new Trajet(
                id, villeDepart, villeArrivee, pointDepart, pointArrivee,
                date, heureDepart, placesDisponibles, placesReservees,
                prix, autoriseFumeur, autoriseAnimaux, autoriseGrossBagages,
                commentaires
        );
        
        // Récupérer le conducteur
        int conducteurId = resultSet.getInt("conducteur_id");
        if (!resultSet.wasNull()) {
            Conducteur conducteur = conducteurService.recupererConducteurParId(conducteurId);
            if (conducteur != null) {
                trajet.setConducteur(conducteur);
            }
        }
        
        return trajet;
    }
} 