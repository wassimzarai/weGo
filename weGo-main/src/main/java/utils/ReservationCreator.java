package utils;

import Entites.Reservation;
import services.ReserService;

import java.time.LocalDate;

/**
 * Utilitaire pour créer des réservations d'exemple
 */
public class ReservationCreator {

    public static void main(String[] args) {
        createSampleReservations();
    }
    
    /**
     * Crée et ajoute des réservations d'exemple à la base de données
     */
    public static void createSampleReservations() {
        System.out.println("=== CREATION DE RESERVATIONS D'EXEMPLE ===");
        
        ReserService service = new ReserService();
        
        try {
            // Exemple 1
            Reservation reservation1 = new Reservation(
                "Jean Dupont",
                LocalDate.now().plusDays(5),
                "Paris",
                "Lyon",
                "Confirme",
                "Aller simple", 
                "RAS"
            );
            service.ajouterReservation(reservation1);
            System.out.println("SUCCES: Reservation 1 ajoutee avec ID: " + reservation1.getId());
            
            // Exemple 2
            Reservation reservation2 = new Reservation(
                "Marie Martin",
                LocalDate.now().plusDays(10),
                "Marseille",
                "Nice",
                "En attente",
                "Aller-retour", 
                "Besoin de 2 places"
            );
            service.ajouterReservation(reservation2);
            System.out.println("SUCCES: Reservation 2 ajoutee avec ID: " + reservation2.getId());
            
            // Exemple 3
            Reservation reservation3 = new Reservation(
                "Pierre Durand",
                LocalDate.now().plusDays(15),
                "Bordeaux",
                "Toulouse",
                "Confirme",
                "Aller simple", 
                "Bagage volumineux"
            );
            service.ajouterReservation(reservation3);
            System.out.println("SUCCES: Reservation 3 ajoutee avec ID: " + reservation3.getId());
            
            // Exemple 4
            Reservation reservation4 = new Reservation(
                "Sophie Leroy",
                LocalDate.now().plusDays(7),
                "Lille",
                "Paris",
                "En attente",
                "Aller simple", 
                "Arrivee tardive possible"
            );
            service.ajouterReservation(reservation4);
            System.out.println("SUCCES: Reservation 4 ajoutee avec ID: " + reservation4.getId());
            
            // Exemple 5
            Reservation reservation5 = new Reservation(
                "Thomas Bernard",
                LocalDate.now().plusDays(12),
                "Strasbourg",
                "Lyon",
                "Confirme",
                "Aller-retour", 
                "Voyage professionnel"
            );
            service.ajouterReservation(reservation5);
            System.out.println("SUCCES: Reservation 5 ajoutee avec ID: " + reservation5.getId());
            
            System.out.println("\nSUCCES: Toutes les reservations d'exemple ont ete ajoutees avec succes!");
            System.out.println("Vous pouvez maintenant creer des abonnements en les associant a ces reservations.");
            
        } catch (Exception e) {
            System.err.println("ECHEC: Erreur lors de la creation des reservations: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=========================================");
    }
} 