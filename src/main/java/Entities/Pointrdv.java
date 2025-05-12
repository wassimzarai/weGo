package Entities;

public class Pointrdv {
    private int id_rdv;
    private int id_trajet;
    private String adresse;

    public Pointrdv(int id_trajet, String adresse) {
        this.id_trajet = id_trajet;
        this.adresse = adresse;
    }

    public Pointrdv(String adresse) {
        this.adresse = adresse;
    }

    public Pointrdv(){}

    public int getId_rdv() {
        return id_rdv;
    }

    public void setId_rdv(int id_rdv) {
        this.id_rdv = id_rdv;
    }

    public int getId_trajet() {
        return id_trajet;
    }


    public void setId_trajet(int id_trajet) {
        this.id_trajet = id_trajet;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    @Override
    public String toString() {
        return "Pointrdv{" + "id_rdv=" + id_rdv + ", id_trajet=" + id_trajet + ", adresse='" + adresse + '\'' + '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return false;
    }

}
