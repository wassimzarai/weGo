package Entites;

import Entites.Trajet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class trajet1 extends Trajet {

    public static boolean GetConducteur() {
        return false;
    }

    public static void SetConducteur(Object o) {
    }

    public void setConducteur(Object o) {
    }



   
  

        private int id;
        private static String villeDepart;
        private static String villeArrivee;
        private String pointDepart; // Adresse ou lieu précis de départ
        private String pointArrivee; // Adresse ou lieu précis d'arrivée
        private LocalDate date;
        private LocalTime heureDepart;
        private int placesDisponibles;
        private int placesReservees;
        private static double prix;
        private static boolean autoriseFumeur;
        private static boolean autoriseAnimaux;
        private static boolean autoriseGrossBagages;
        private String commentaires;

        // Relations
        private Conducteur conducteur;
        private List<Entites.Reservation> reservations = new ArrayList<>();

        // Constructeur sans ID
        public trajet1(String villeDepart, String villeArrivee, String pointDepart, String pointArrivee,
                      LocalDate date, LocalTime heureDepart, int placesDisponibles, double prix,
                      boolean autoriseFumeur, boolean autoriseAnimaux, boolean autoriseGrossBagages,
                      String commentaires, Conducteur conducteur) {
            this.villeDepart = villeDepart;
            this.villeArrivee = villeArrivee;
            this.pointDepart = pointDepart;
            this.pointArrivee = pointArrivee;
            this.date = date;
            this.heureDepart = heureDepart;
            this.placesDisponibles = placesDisponibles;
            this.placesReservees = 0; // Initialement aucune place réservée
            this.prix = prix;
            this.autoriseFumeur = autoriseFumeur;
            this.autoriseAnimaux = autoriseAnimaux;
            this.autoriseGrossBagages = autoriseGrossBagages;
            this.commentaires = commentaires;
            this.conducteur = conducteur;

            // Établir la relation bidirectionnelle avec le conducteur
            if (conducteur != null) {
                conducteur.ajouterTrajet(this);
            }
        }

        // Constructeur avec ID
        public trajet1(int id, String villeDepart, String villeArrivee, String pointDepart, String pointArrivee,
                      LocalDate date, LocalTime heureDepart, int placesDisponibles, int placesReservees,
                      double prix, boolean autoriseFumeur, boolean autoriseAnimaux, boolean autoriseGrossBagages,
                      String commentaires) {
            this.id = id;
            this.villeDepart = villeDepart;
            this.villeArrivee = villeArrivee;
            this.pointDepart = pointDepart;
            this.pointArrivee = pointArrivee;
            this.date = date;
            this.heureDepart = heureDepart;
            this.placesDisponibles = placesDisponibles;
            this.placesReservees = placesReservees;
            this.prix = prix;
            this.autoriseFumeur = autoriseFumeur;
            this.autoriseAnimaux = autoriseAnimaux;
            this.autoriseGrossBagages = autoriseGrossBagages;
            this.commentaires = commentaires;
        }

        // Getters et Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public static String getVilleDepart() {
            return villeDepart;
        }

        public void setVilleDepart(String villeDepart) {
            this.villeDepart = villeDepart;
        }

        public static String getVilleArrivee() {
            return villeArrivee;
        }

        public void setVilleArrivee(String villeArrivee) {
            this.villeArrivee = villeArrivee;
        }

        public String getPointDepart() {
            return pointDepart;
        }

        public void setPointDepart(String pointDepart) {
            this.pointDepart = pointDepart;
        }

        public String getPointArrivee() {
            return pointArrivee;
        }

        public void setPointArrivee(String pointArrivee) {
            this.pointArrivee = pointArrivee;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public LocalTime getHeureDepart() {
            return heureDepart;
        }

        public void setHeureDepart(LocalTime heureDepart) {
            this.heureDepart = heureDepart;
        }

        public int getPlacesDisponibles() {
            return placesDisponibles;
        }

        public void setPlacesDisponibles(int placesDisponibles) {
            this.placesDisponibles = placesDisponibles;
        }

        public int getPlacesReservees() {
            return placesReservees;
        }

        public void setPlacesReservees(int placesReservees) {
            this.placesReservees = placesReservees;
        }

        public static double getPrix() {
            return prix;
        }

        public void setPrix(double prix) {
            this.prix = prix;
        }

        public static boolean isAutoriseFumeur() {
            return autoriseFumeur;
        }

        public void setAutoriseFumeur(boolean autoriseFumeur) {
            this.autoriseFumeur = autoriseFumeur;
        }

        public static boolean isAutoriseAnimaux() {
            return autoriseAnimaux;
        }

        public void setAutoriseAnimaux(boolean autoriseAnimaux) {
            this.autoriseAnimaux = autoriseAnimaux;
        }

        public static boolean isAutoriseGrossBagages() {
            return autoriseGrossBagages;
        }

        public void setAutoriseGrossBagages(boolean autoriseGrossBagages) {
            this.autoriseGrossBagages = autoriseGrossBagages;
        }

        public String getCommentaires() {
            return commentaires;
        }

        public void setCommentaires(String commentaires) {
            this.commentaires = commentaires;
        }

        public Conducteur getConducteur() {
            return conducteur;
        }

        public void setConducteur(Conducteur conducteur) {
            this.conducteur = conducteur;
        }

        public List<Entites.Reservation> getReservations() {
            return reservations;
        }

        public void setReservations(List<Entites.Reservation> reservations) {
            this.reservations = reservations;
        }

        // Méthodes de gestion des réservations
        public void ajouterReservation(Entites.Reservation reservation) {
            if (reservation != null && this.placesReservees < this.placesDisponibles) {
                this.reservations.add(reservation);
                this.placesReservees++;
                // Lier le trajet à la réservation (non implémenté encore dans Reservation)
                // reservation.setTrajet(this);
            }
        }

        public void retirerReservation(Entites.Reservation reservation) {
            if (reservation != null && this.reservations.contains(reservation)) {
                this.reservations.remove(reservation);
                this.placesReservees = Math.max(0, this.placesReservees - 1);
                // Délier le trajet de la réservation (non implémenté encore dans Reservation)
                // if (reservation.getTrajet() == this) {
                //     reservation.setTrajet(null);
                // }
            }
        }

        // Méthode pour vérifier si des places sont encore disponibles
        public boolean placesEncoreDisponibles() {
            return this.placesReservees < this.placesDisponibles;
        }

        // Méthode pour obtenir le nombre de places restantes
        public int getNombrePlacesRestantes() {
            return this.placesDisponibles - this.placesReservees;
        }

        @Override
        public String toString() {
            return "Trajet{" +
                    "id=" + id +
                    ", villeDepart='" + villeDepart + '\'' +
                    ", villeArrivee='" + villeArrivee + '\'' +
                    ", date=" + date +
                    ", heureDepart=" + heureDepart +
                    ", places disponibles=" + (placesDisponibles - placesReservees) + "/" + placesDisponibles +
                    ", prix=" + prix +
                    ", conducteur=" + (conducteur != null ? conducteur.getNomComplet() : "Non assigné") +
                    '}';
        }



    }


