package Entites;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import Entites.StatutTrajet;

public class Trajet {
    private int id_trajet;
    private String depart;
    private String arrivee;
    private LocalDate date;
    private LocalTime heure;
    private StatutTrajet statut;
    private int prix_place;
    private int nb_place;
    private Voiture voiture;
    private Pointrdv pointrdv;

    public Trajet(int id_trajet, String depart, String arrivee, LocalDate date, LocalTime heure, StatutTrajet statut, int prix_place, int nb_place) {
        this.id_trajet = id_trajet;
        this.depart = depart;
        this.arrivee = arrivee;
        this.date = date;
        this.heure = heure;
        this.statut = statut;
        this.prix_place = prix_place;
        this.nb_place = nb_place;
    }

    public Trajet(String depart, String arrivee, LocalDate date, LocalTime heure, StatutTrajet statut, int prix_place) {
        this.depart = depart;
        this.arrivee = arrivee;
        this.date = date;
        this.heure = heure;
        this.statut = statut;
        this.prix_place = prix_place;
    }

    public Trajet(String depart, String arrivee, LocalDate date, LocalTime heure, int prix_place, int nb_place) {
        this.depart = depart;
        this.arrivee = arrivee;
        this.date = date;
        this.heure = heure;
        this.prix_place = prix_place;
        this.nb_place = nb_place;

    }

    public Trajet(String depart, String arrivee, LocalDate date, LocalTime heure, StatutTrajet statut, int prix_place, int nb_place) {
        this.depart = depart;
        this.arrivee = arrivee;
        this.date = date;
        this.heure = heure;
        this.statut = statut;
        this.prix_place = prix_place;
        this.nb_place = nb_place;
    }

    public Trajet() {
    }

    public Trajet(String depart, String arrivee, LocalDate date, LocalTime heure, StatutTrajet statut, int prix_place, int nb_place, Voiture voiture) {
        this.depart = depart;
        this.arrivee = arrivee;
        this.date = date;
        this.heure = heure;
        this.statut = statut;
        this.prix_place = prix_place;
        this.nb_place = nb_place;
        this.voiture = voiture;
    }

    public Trajet(int id_trajet, String depart, String arrivee, LocalDate date, LocalTime heure, StatutTrajet statut, int prix_place, int nb_place, Voiture voiture) {
        this.id_trajet = id_trajet;
        this.depart = depart;
        this.arrivee = arrivee;
        this.date = date;
        this.heure = heure;
        this.statut = statut;
        this.prix_place = prix_place;
        this.nb_place = nb_place;
        this.voiture = voiture;
    }

    public Trajet(String depart, String arrivee, LocalDate date, LocalTime heure, StatutTrajet statut, int prix_place, int nb_place, Pointrdv pointrdv) {

        this.depart = depart;
        this.arrivee = arrivee;
        this.date = date;
        this.heure = heure;
        this.statut = statut;
        this.prix_place = prix_place;
        this.nb_place = nb_place;
        this.pointrdv = pointrdv;
    }

    public Trajet(String depart, String arrivee, LocalDate date, LocalTime heure, int prix_place, int nb_place, Pointrdv pointrdv) {

        this.depart = depart;
        this.arrivee = arrivee;
        this.date = date;
        this.heure = heure;

        this.prix_place = prix_place;
        this.nb_place = nb_place;
        this.pointrdv = pointrdv;
    }

    public Trajet(int id_trajet, String depart, String arrivee, LocalDate date, LocalTime heure, StatutTrajet statut, int prix_place, int nb_place, Voiture voiture, Pointrdv pointrdv) {
        this.id_trajet = id_trajet;
        this.depart = depart;
        this.arrivee = arrivee;
        this.date = date;
        this.heure = heure;
        this.statut = statut;
        this.prix_place = prix_place;
        this.nb_place = nb_place;
        this.voiture = voiture;
        this.pointrdv = pointrdv;
    }

    public int getId_trajet() {
        return id_trajet;
    }

    public void setId_trajet(int id_trajet) {
        this.id_trajet = id_trajet;
    }

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }

    public String getArrivee() {
        return arrivee;
    }

    public void setArrivee(String arrivee) {
        this.arrivee = arrivee;
    }

    public LocalDate getDate() {
        return date;

    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getHeure() {
        return heure;
    }

    public void setHeure(LocalTime heure) {
        this.heure = heure;
    }

    public StatutTrajet getStatut() {
        return statut;
    }

    public void setStatut(StatutTrajet statut) {
        this.statut = statut;
    }

    public int getPrix_place() {
        return prix_place;
    }

    public void setPrix_place(int prix_place) {
        this.prix_place = prix_place;
    }

    public int getNb_place() {
        return nb_place;
    }

    public void setNb_place(int nb_place) {
        this.nb_place = nb_place;
    }

    public Voiture getVoiture() {
        return voiture;
    }

    public Pointrdv getPointrdv() {
        return pointrdv;
    }

    public void setPointrdv(Pointrdv pointrdv) {
        this.pointrdv = pointrdv;
    }

    public void setVoiture(Voiture voiture) {
        this.voiture = voiture;
    }

    @Override
    public String toString() {
        return "Trajet{" + "id_trajet=" + id_trajet + ", depart=" + depart + ", arrivee=" + arrivee + ", date=" + date + ", heure=" + heure + ", statut=" + statut + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Trajet trajet = (Trajet) o;
        return id_trajet == trajet.id_trajet && Objects.equals(depart, trajet.depart) && Objects.equals(arrivee, trajet.arrivee) && Objects.equals(date, trajet.date) && Objects.equals(heure, trajet.heure) && statut == trajet.statut;
    }

    public Object getConducteur() {
        return null;
    }

    public void setConducteur(Object o) {
    }
}



