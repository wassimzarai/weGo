package main;
import services.*;
import entities.*;
import utils.MyConnection;
import java.time.LocalDate;
import java.util.List;

public class TestJDBC {
    public static void main(String[] args) {
        // Initialiser la connexion à la base de données
        MyConnection.getConnection();

        ReserService reservationService = new ReserService();

        // Ajouter une nouvelle réservation
        Reservation reservation1 = new Reservation(
                "Salma",
                LocalDate.of(2025, 5, 5),
                "Tunis",
                "Sousse",
                "En attente", // statut par défaut
                "Train",
                "Commentaire de réservation"
        );
        reservationService.ajouterReservation(reservation1);

        // Ajouter une autre réservation
        Reservation reservation2 = new Reservation(
                "Ahmed",
                LocalDate.of(2025, 5, 6),
                "Tunis",
                "Monastir",
                "En attente", // statut par défaut
                "Avion",
                "Réservation urgente"
        );
        reservationService.ajouterReservation(reservation2);

        // Modifier une réservation (par exemple, réserver pour Ahmed)
        reservation2.setStatut("Confirmée");
        reservationService.modifierReservation(reservation2);

        // Récupérer toutes les réservations
        List<Reservation> allReservations = reservationService.recupererToutesReservations();
        System.out.println("\nListe de toutes les réservations:");
        for (Reservation reservation : allReservations) {
            System.out.println("ID: " + reservation.getId() + 
                    ", Nom Passager: " + reservation.getNomPassager() +
                    ", Statut: " + reservation.getStatut() + 
                    ", Date Réservation: " + reservation.getDateReservation() +
                    ", Point de départ: " + reservation.getPointDepart() + 
                    ", Point d'arrivée: " + reservation.getPointArrivee() +
                    ", Type de trajet: " + reservation.getTypeTrajet() + 
                    ", Commentaire: " + reservation.getCommentaire());
        }

        // Supprimer une réservation (par exemple, la réservation d'Ahmed)
        reservationService.supprimerReservation(reservation2.getId());

        // Vérifier la suppression en récupérant toutes les réservations à nouveau
        List<Reservation> updatedReservations = reservationService.recupererToutesReservations();
        System.out.println("\nListe des réservations après suppression de celle d'Ahmed:");
        for (Reservation reservation : updatedReservations) {
            System.out.println("ID: " + reservation.getId() + 
                    ", Nom Passager: " + reservation.getNomPassager() +
                    ", Statut: " + reservation.getStatut() + 
                    ", Date Réservation: " + reservation.getDateReservation() +
                    ", Point de départ: " + reservation.getPointDepart() + 
                    ", Point d'arrivée: " + reservation.getPointArrivee() +
                    ", Type de trajet: " + reservation.getTypeTrajet() + 
                    ", Commentaire: " + reservation.getCommentaire());
        }
    }
}

