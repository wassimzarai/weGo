package main ;

import Entites.Paiement;
import services.ServicePaiement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestPaiement {
    private static final Logger LOGGER = Logger.getLogger(TestPaiement.class.getName());

    public static void main(String[] args) {
        ServicePaiement service = new ServicePaiement();

        // 1. Test d'ajout
        testerAjoutPaiement(service);

        // 2. Test de liste
        testerListePaiements(service);

        // 3. Tests supplémentaires (optionnels)
        // testerRechercheParId(service);
        // testerSuppression(service);
        Paiement p=new Paiement(12,70.0,"2025-04-29 10:30:00","espece","a payer","soulaymaboukhadra@gmail.com");
        service.ajouterPaiement(p);

    }

    private static void testerAjoutPaiement(ServicePaiement service) {
        LOGGER.info("=== TEST AJOUT PAIEMENT ===");

        // ID auto-généré sera affecté après l'ajout dans la base de données
        Paiement nouveauPaiement = new Paiement(
                0, // ID auto-généré
                 // ID Réservation
                150.00, // Montant (double)
                "2025-04-29 10:30:00", // Date (String)
                "Carte Bancaire", // Méthode
                "Réussi", // Statut
                "client@example.com" // Email du client
        );

        try {
            boolean resultat = service.ajouterPaiement(nouveauPaiement);
            if (resultat) {
                LOGGER.info("SUCCÈS: Paiement ajouté avec ID: " + nouveauPaiement.getId());
            } else {
                LOGGER.warning("ÉCHEC: L'ajout du paiement a échoué");
            }
        } catch (Exception e) {  // Capture d'exception plus générique si nécessaire
            LOGGER.log(Level.SEVERE, "ERREUR: Échec de l'ajout du paiement", e);
        }
    }

    private static void testerListePaiements(ServicePaiement service) {
        LOGGER.info("\n=== TEST LISTE PAIEMENTS ===");

        try {
            List<Paiement> paiements = service.getList();
            if (paiements.isEmpty()) {
                LOGGER.info("Aucun paiement trouvé dans la base de données");
            } else {
                LOGGER.info("Liste des paiements (" + paiements.size() + " trouvés):");
                paiements.forEach(p -> LOGGER.info(formatPaiement(p)));
            }
        } catch (Exception e) {  // Capture d'exception pour cette méthode
            LOGGER.log(Level.SEVERE, "ERREUR: Échec de la récupération des paiements", e);
        }
    }

    // Méthodes utilitaires
    private static String formatPaiement(Paiement p) {
        return String.format(
                "ID: %d | Réservation: %d | Montant: %.2f | Date: %s | Méthode: %s | Statut: %s | Email: %s",
                p.getId(),
                p.getReservationId(),
                p.getMontant(),
                p.getDatePaiement(),
                p.getMethodePaiement(),
                p.getStatut(),
                p.getEmail()  // Affichage de l'email du client
        );
    }

    /* Méthodes optionnelles pour des tests plus complets
    private static void testerRechercheParId(ServicePaiement service) {
        // Implémentation similaire...
    }

    private static void testerSuppression(ServicePaiement service) {
        // Implémentation similaire...
    }
    */


}
