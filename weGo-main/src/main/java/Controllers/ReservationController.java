package Controllers;

import services.AbonnementService;
import services.ReserService;
import services.TrajetService;
import services.PassagerService;
import Entites.Reservation;
import Entites.Passager;
import Entites.trajet1;
import Entites.Abonnement;
import models.ApiResponse;
import models.ReservationAutomatiqueRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Path("/reservations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReservationController {

    private ReserService reservationService;
    private AbonnementService abonnementService;
    private TrajetService trajetService;
    private PassagerService passagerService;
    
    // Vitesse moyenne en km/h pour l'estimation du temps d'arrivée
    private static final double VITESSE_MOYENNE = 30.0;
    
    // Rayon de la Terre en kilomètres
    private static final double RAYON_TERRE = 6371.0;
    
    public ReservationController() {
        this.reservationService = new ReserService();
        this.abonnementService = new AbonnementService();
        this.trajetService = new TrajetService();
        this.passagerService = new PassagerService();
    }
    
    /**
     * API pour créer automatiquement une réservation à partir d'un abonnement actif
     * @param request Les informations pour la réservation automatique
     * @return Une réponse API avec un message utilisateur
     */
    @POST
    @Path("/automatique")
    public Response creerReservationAutomatique(ReservationAutomatiqueRequest request) {
        try {
            // 1. Vérifier que le passager existe
            Passager passager = passagerService.recupererPassagerParId(request.getPassagerId());
            if (passager == null) {
                return Response.status(404)
                        .entity(ApiResponse.notFound("Impossible de créer la réservation : compte utilisateur introuvable."))
                        .build();
            }
            
            // 2. Vérifier si le passager a un abonnement actif
            List<Reservation> reservationsPassager = passager.getReservations();
            if (reservationsPassager.isEmpty()) {
                return Response.status(400)
                        .entity(ApiResponse.badRequest("Réservation impossible : vous n'avez aucune réservation associée à votre compte."))
                        .build();
            }
            
            boolean abonnementActifTrouve = false;
            Abonnement abonnementActif = null;
            
            for (Reservation reservation : reservationsPassager) {
                if (reservation.hasActiveAbonnement()) {
                    abonnementActifTrouve = true;
                    // Récupérer le premier abonnement actif trouvé
                    abonnementActif = reservation.getAbonnements().stream()
                            .filter(a -> "Actif".equals(a.getStatut()) && 
                                     (a.getDateFin() == null || !LocalDate.now().isAfter(a.getDateFin())))
                            .findFirst()
                            .orElse(null);
                    break;
                }
            }
            
            if (!abonnementActifTrouve || abonnementActif == null) {
                return Response.status(400)
                        .entity(ApiResponse.badRequest("Réservation impossible : votre abonnement est expiré. Veuillez le renouveler pour continuer."))
                        .build();
            }
            
            // 3. Déterminer l'itinéraire
            String villeDepart, villeArrivee;
            
            if (request.isUtiliserItineraireHabituel()) {
                // Utiliser l'itinéraire habituel du passager s'il existe
                String[] itineraireHabituel = passagerService.recupererItineraireHabituel(passager.getId());
                if (itineraireHabituel == null) {
                    return Response.status(400)
                            .entity(ApiResponse.badRequest("Réservation impossible : aucun itinéraire habituel trouvé. Veuillez spécifier l'itinéraire souhaité."))
                            .build();
                }
                villeDepart = itineraireHabituel[0];
                villeArrivee = itineraireHabituel[1];
            } else {
                // Utiliser les villes spécifiées dans la requête
                villeDepart = request.getVilleDepart();
                villeArrivee = request.getVilleArrivee();
                
                if (villeDepart == null || villeArrivee == null || villeDepart.isEmpty() || villeArrivee.isEmpty()) {
                    return Response.status(400)
                            .entity(ApiResponse.badRequest("Réservation impossible : veuillez spécifier les villes de départ et d'arrivée."))
                            .build();
                }
            }
            
            // 4. Rechercher le prochain trajet disponible
            int nombrePlaces = request.getNombrePlaces() > 0 ? request.getNombrePlaces() : 1;
            trajet1 trajetDisponible = trajetService.trouverProchainTrajetDisponible(villeDepart, villeArrivee, nombrePlaces);
            
            if (trajetDisponible == null) {
                return Response.status(404)
                        .entity(ApiResponse.notFound("Réservation impossible : aucun trajet disponible ne correspond à votre demande dans les prochains jours."))
                        .build();
            }
            
            // 5. Créer la réservation
            Reservation nouvelleReservation = new Reservation(
                    passager.getNomComplet(),
                    LocalDate.now(), // Date de réservation (aujourd'hui)
                    trajetDisponible.getPointDepart(),
                    trajetDisponible.getPointArrivee(),
                    "Confirmée", // Statut directement confirmé car abonnement actif
                    "Aller simple", // Type par défaut
                    "Réservation automatique via abonnement #" + abonnementActif.getIdAbonnement(),
                    trajetDisponible,
                    passager
            );
            
            // 6. Sauvegarder la réservation
            reservationService.ajouterReservation(nouvelleReservation);
            
            // 7. Formater l'heure pour le message utilisateur
            String heureFormatee = trajetDisponible.getHeureDepart().format(DateTimeFormatter.ofPattern("HH'h'mm"));
            
            // 8. Retourner une réponse avec message personnalisé
            return Response.status(201)
                    .entity(ApiResponse.success(
                            "Votre réservation pour le trajet de " + heureFormatee + " a été confirmée avec succès.",
                            nouvelleReservation))
                    .build();
            
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity(ApiResponse.serverError("Une erreur est survenue lors de la création de votre réservation. Veuillez réessayer plus tard."))
                    .build();
        }
    }
    
    /**
     * API pour estimer le temps d'arrivée au point de rendez-vous
     * Calcule le temps en minutes nécessaire pour atteindre le point de rendez-vous
     * à partir de la position courante de l'utilisateur
     * 
     * @param userLat Latitude de la position actuelle de l'utilisateur
     * @param userLng Longitude de la position actuelle de l'utilisateur
     * @param pickupLat Latitude du point de rendez-vous
     * @param pickupLng Longitude du point de rendez-vous
     * @return Une réponse API avec l'estimation du temps en minutes
     */
    @GET
    @Path("/eta")
    public Response estimerTempsArrivee(
            @QueryParam("userLat") double userLat,
            @QueryParam("userLng") double userLng,
            @QueryParam("pickupLat") double pickupLat,
            @QueryParam("pickupLng") double pickupLng) {
        try {
            // Validation des paramètres
            if (Double.isNaN(userLat) || Double.isNaN(userLng) || 
                Double.isNaN(pickupLat) || Double.isNaN(pickupLng)) {
                return Response.status(400)
                        .entity(ApiResponse.badRequest("Paramètres invalides. Veuillez fournir des coordonnées GPS valides."))
                        .build();
            }
            
            // 1. Calcul de la distance en utilisant la formule de Haversine
            double distance = calculerDistance(userLat, userLng, pickupLat, pickupLng);
            
            // 2. Calcul du temps estimé (en minutes) basé sur une vitesse moyenne
            int tempsEstimeMinutes = calculerTempsEstime(distance);
            
            // 3. Préparation de la réponse
            return Response.status(200)
                    .entity(ApiResponse.success(
                            "Estimation du temps d'arrivée: " + tempsEstimeMinutes + " minutes",
                            new EstimationTemps(distance, tempsEstimeMinutes)))
                    .build();
            
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity(ApiResponse.serverError("Une erreur est survenue lors du calcul du temps estimé."))
                    .build();
        }
    }
    
    /**
     * Calcule la distance entre deux points en utilisant la formule de Haversine
     * 
     * @param lat1 Latitude du premier point
     * @param lon1 Longitude du premier point
     * @param lat2 Latitude du deuxième point
     * @param lon2 Longitude du deuxième point
     * @return La distance en kilomètres
     */
    private double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        // Conversion des degrés en radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        
        // Différence des longitudes et latitudes
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;
        
        // Formule de Haversine
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // Distance en kilomètres
        return RAYON_TERRE * c;
    }
    
    /**
     * Calcule le temps estimé pour parcourir une distance donnée
     * 
     * @param distanceKm Distance en kilomètres
     * @return Temps estimé en minutes
     */
    private int calculerTempsEstime(double distanceKm) {
        // Convertir la vitesse (km/h) en km/min
        double vitesseKmMin = VITESSE_MOYENNE / 60;
        
        // Calculer le temps en minutes
        double tempsMinutes = distanceKm / vitesseKmMin;
        
        // Ajouter un petit buffer de sécurité (5 minutes) et arrondir
        return (int) Math.ceil(tempsMinutes + 5);
    }
    
    /**
     * Classe interne pour encapsuler les résultats de l'estimation du temps
     */
    private static class EstimationTemps {
        private double distanceKm;
        private int tempsMinutes;
        
        public EstimationTemps(double distanceKm, int tempsMinutes) {
            this.distanceKm = distanceKm;
            this.tempsMinutes = tempsMinutes;
        }
        
        public double getDistanceKm() {
            return distanceKm;
        }
        
        public int getTempsMinutes() {
            return tempsMinutes;
        }
    }
} 