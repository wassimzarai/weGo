package Controllers;

import Entites.Trajet;
import Entites.StatutTrajet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import services.ServiceTrajet;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class AfficherTrajet {

    @FXML
    private TableColumn<Trajet, String> arriveeColumn;

    @FXML
    private TableColumn<Trajet, LocalDate> dateColumn;

    @FXML
    private TableColumn<Trajet, String> departColumn;

    @FXML
    private TableColumn<Trajet, LocalTime> heureColumn;

    @FXML
    private TableColumn<Trajet, Integer> idTrajetColumn;

    @FXML
    private TableColumn<Trajet, Integer> nbrplacescolumn;

    @FXML
    private TableColumn<Trajet, Integer> prixColumn;

    @FXML
    private TableColumn<Trajet, StatutTrajet> statutColumn;

    @FXML
    private TableView<Trajet> trajetTable;

    private ServiceTrajet sp = new ServiceTrajet();
    @FXML
    void initialize() throws SQLException {
        ArrayList<Trajet> list = sp.getList();
        trajetTable.getItems().addAll(list);
        ObservableList<Trajet> obe= FXCollections.observableArrayList(list);
        trajetTable.setItems(obe);
        idTrajetColumn.setCellValueFactory(new PropertyValueFactory<>("id_trajet"));
        departColumn.setCellValueFactory(new PropertyValueFactory<>("depart"));
        arriveeColumn.setCellValueFactory(new PropertyValueFactory<>("arrivee"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        heureColumn.setCellValueFactory(new PropertyValueFactory<>("heure"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix_place"));
        nbrplacescolumn.setCellValueFactory(new PropertyValueFactory<>("nb_place"));




    }





}
