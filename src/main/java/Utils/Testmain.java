package Utils;

import Entities.Pointrdv;
import Entities.Trajet;
import Entities.StatutTrajet;
import service.ServiceTrajet;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public class Testmain {
    public static void main(String[] args) {
        ServiceTrajet st=new ServiceTrajet();
        StatutTrajet statutTrajet = StatutTrajet.TERMINE; // PAS new, c'est une valeur de l'énum !

//        Trajet t = new Trajet(
//
//                "Tunis",
//                "Djerba",
//                LocalDate.of(2025, 5, 23),      // LocalDate correctement créé
//                LocalTime.of(9, 30),            // LocalTime correctement créé
//                statutTrajet,
//                20
//        );
////        try {
////            st.getList();
////            System.out.println(st.getList());
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
        Trajet t1=new Trajet("Tunis",
                "sfax",
                LocalDate.of(2027, 5, 23),      // LocalDate correctement créé
                LocalTime.of(9, 30),            // LocalTime correctement créé
                statutTrajet,
                20);
//
//        t1.setId_trajet(13);
//        try {
//            st.delete(t1);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//




        Pointrdv p = new Pointrdv("Monoprix,riadhandalous");
        Trajet t2=new Trajet("Tunis",
                "sfax",
                LocalDate.of(2027, 5, 23),      // LocalDate correctement créé
                LocalTime.of(9, 30),            // LocalTime correctement créé
                statutTrajet,
                20,4,p);



//        try {
//            st.ajouter(t2);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
       t2.setId_trajet(40);
       t2.setPrix_place(10);
       p.setAdresse("mohammed5,univcentral");
      try {
          st.update(t2);
     } catch (SQLException e) {}
//        try {
//            st.delete(t2);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }


        try {
            System.out.println(st.recupererTousLesTrajets());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
