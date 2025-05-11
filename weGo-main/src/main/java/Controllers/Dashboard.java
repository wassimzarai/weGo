package Controllers;
import Entites.user;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utils.database;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Dashboard implements Initializable {


    @FXML
    private AnchorPane edit_information;
    @FXML
    private Button editprofil_button;
    @FXML
    private AnchorPane details_page;
    @FXML
    private Label label_totale_utilisateur;
    @FXML
    private Button edit;
    @FXML
    private Button soulayma;
    @FXML
    private Button salma;
    @FXML
    private Button eya;




    @FXML
    private Button historique;

    @FXML
    private Button home;

    @FXML
    private AnchorPane leith;


    @FXML
    private Label userr;

    @FXML
    private TextField username;

    @FXML
    private Circle circle;

    @FXML
    private TextField email;

    @FXML
    private AnchorPane home_page;

    @FXML
    private ComboBox<?> gender;

    @FXML
    private TextField date_update;

    @FXML
    private TextField username_update;

    @FXML
    private TextField email_update;

    @FXML
    private TextField password_update;

    @FXML
    private TextField role_update;

    @FXML
    private AnchorPane totale_produits;

    @FXML
    private AnchorPane totale_stations;

    @FXML
    private AnchorPane totale_users;

    @FXML
    private AnchorPane totale_velos;
    @FXML
    private AnchorPane navbar;

    @FXML
    private Label gender_details;

    @FXML
    private Label email_details;


    @FXML
    private Label username_details;

    @FXML
    private Label role_details;

    @FXML
    private Label date_details;

    @FXML
    private Circle circle1;
    @FXML
    private ComboBox<String> roleComboBox;


    private Connection connect;
    private Statement statement;
    private PreparedStatement prepare;
    private ResultSet result;
    private Image image;


    public void account() {

        userr.setText(user.username);

    }

    public void exit() {

        System.exit(0);

    }


    public void navButton() {

        home.setOnMouseClicked((MouseEvent event) -> {

            home.setStyle("-fx-background-color:linear-gradient(to bottom right,"
                    + " rgba(121, 172, 255, 0.6), rgba(255, 106, 239, 0.6));"
                    + "-fx-border-color:linear-gradient(to bottom, #517ab5, #ae44a5);"
                    + "-fx-border-width:0px 0px 0px 5px");


        });


    }


    public void textfieldRecord() {

//        THIS IS ARE JUST LIKE THE TEXTFIELD ON THE LOGIN AND SIGN UP FORM!
        if (username.isFocused()) {

            username.setStyle("-fx-background-color:#fff; -fx-border-width:2px");
//            DEFAULT TEXTFIELD!
            email.setStyle("-fx-background-color:transparent; -fx-border-width:1px");
            gender.setStyle("-fx-background-color:transparent; -fx-border-width:1px");

        } else if (email.isFocused()) {

            username.setStyle("-fx-background-color:transparent; -fx-border-width:1px");
            email.setStyle("-fx-background-color:#fff; -fx-border-width:2px");
            gender.setStyle("-fx-background-color:transparent; -fx-border-width:1px");


        } else if (gender.isFocused()) {

            username.setStyle("-fx-background-color:transparent; -fx-border-width:1px");
            email.setStyle("-fx-background-color:transparent; -fx-border-width:1px");

            gender.setStyle("-fx-background-color:#fff; -fx-border-width:2px");

        }

    }

    public void insertImage() {

        FileChooser open = new FileChooser();

        open.setTitle("Open image file");

        Stage stage = (Stage) navbar.getScene().getWindow();

        File file = open.showOpenDialog(stage);


        if (file != null) {

            user.path = file.getAbsolutePath();

            System.out.println(file.getAbsolutePath());

            image = new Image(file.toURI().toString(), 85, 85, false, true);

            circle.setFill(new ImagePattern(image));
            circle1.setFill(new ImagePattern(image));

            changeProfile();

        }

    }

    public void changeProfile() {

        connect = database.connectDb();

        String uri = user.path;

        uri = uri.replace("\\", "\\\\");

        String sql = "UPDATE `account` SET `image` = '"
                + uri + "' WHERE username = '" + user.username + "'";

        try {

            statement = connect.createStatement();
            statement.executeUpdate(sql);

        } catch (Exception e) {
        }

    }

    public void displayImage() {

//        MAKE SURE THAT YOU DIDNT FORGET THE "file:"
        String uri = "file:" + user.path;

        image = new Image(uri, 150, 150, false, true);

        circle.setFill(new ImagePattern(image));
        circle1.setFill(new ImagePattern(image));


    }


    public void changePage() {

        if (home.isFocused()) {

            home_page.setVisible(true);
            details_page.setVisible(false);
            edit_information.setVisible(false);
            leith.setVisible(false);
        }
        if (edit.isFocused()) {
            home_page.setVisible(false);
            details_page.setVisible(true);
            edit_information.setVisible(false);
        }
        if (editprofil_button.isFocused()) {
            edit_information.setVisible(true);
            details_page.setVisible(false);
            home_page.setVisible(false);
        }


    }

    private double x = 0;
    private double y = 0;

    public void logoutAccount() {

        historique.getScene().getWindow().hide();

        try {
            //                NAME OF LOGIN FORM
            Parent root = FXMLLoader.load(getClass().getResource("hello-view.fxml"));

            Scene scene = new Scene(root);

            Stage stage = new Stage();

            root.setOnMousePressed((MouseEvent event) -> {

                x = event.getSceneX();
                y = event.getSceneY();


            });

            root.setOnMouseDragged((MouseEvent event) -> {

                stage.setX(event.getScreenX() - x);
                stage.setY(event.getScreenY() - y);

                stage.setOpacity(0.8);

            });

            root.setOnMouseReleased((MouseEvent event) -> {

                stage.setOpacity(1);

            });

            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
        }
    }

    private String genderData[] = {"Male", "Female", "Others"};

    public void comboBox() {

        List<String> list = new ArrayList<>();

        for (String data1 : genderData) {

            list.add(data1);

        }

        ObservableList dataList = FXCollections.observableArrayList(list);

        gender.setItems(dataList);

    }

    public void update() {
        connect = database.connectDb();

        String sql = "UPDATE utilisateur SET `username` = ?, `password` = ?, `email` = ?, `gender` = ?, `role` = ? WHERE `username` = ?";

        try {
            prepare = connect.prepareStatement(sql);

            prepare.setString(1, username_update.getText());
            prepare.setString(2, password_update.getText());
            prepare.setString(3, email_update.getText());
            prepare.setString(4, gender.getSelectionModel().getSelectedItem().toString());
            prepare.setString(5, role_update.getText());
            prepare.setString(6, username_update.getText()); // Utilisez le nom d'utilisateur de la page de connexion comme condition

            int rowsAffected = prepare.executeUpdate();

            if (rowsAffected > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Message de réussite");
                alert.setHeaderText(null);
                alert.setContentText("Mise à jour réussie !");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Message d'erreur");
                alert.setHeaderText(null);
                alert.setContentText("Échec de la mise à jour. Veuillez réessayer.");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Message d'erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur s'est produite lors de la mise à jour. Veuillez réessayer.");
            alert.showAndWait();
        }
    }


    public void afficherDetailsUtilisateur() {


        gender_details.setText(user.gender);
        email_details.setText(user.email);
        username_details.setText(user.username);
        role_details.setText(user.role);
        date_details.setText(user.date);


    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        account();
        displayImage();
        changePage();
        navButton();
        afficherDetailsUtilisateur();
        comboBox();


    }


    public void btnpaiement(javafx.event.ActionEvent actionEvent) {
        try {
            // Charger le nouveau FXML
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/MenuPrincipal.fxml"));

            // Récupérer la scène actuelle
            Scene scene = ((Node) actionEvent.getSource()).getScene();

            // Changer la racine de la scène
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            // Gérer l'erreur (afficher un message à l'utilisateur)
            System.err.println("Erreur lors du chargement de Menuprincipal.fxml");
        }
}

    public void btntarjet(ActionEvent actionEvent) {

        try {
            // Charger le nouveau FXML
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/principal.fxml"));

            // Récupérer la scène actuelle
            Scene scene = ((Node) actionEvent.getSource()).getScene();

            // Changer la racine de la scène
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            // Gérer l'erreur (afficher un message à l'utilisateur)
            System.err.println("Erreur lors du chargement de MenuPrincipal.fxml");
        }

    }

    public void btnreservation(ActionEvent actionEvent) {
        try {
            // Charger le nouveau FXML
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/ListeReservations.fxml"));

            // Récupérer la scène actuelle
            Scene scene = ((Node) actionEvent.getSource()).getScene();

            // Changer la racine de la scène
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            // Gérer l'erreur (afficher un message à l'utilisateur)
            System.err.println("Erreur lors du chargement de MenuPrincipal.fxml");
        }

    }

    public void btnvoit(ActionEvent actionEvent) {
        try {
            // Charger le nouveau FXML
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/fxmlfile.fxml"));

            // Récupérer la scène actuelle
            Scene scene = ((Node) actionEvent.getSource()).getScene();

            // Changer la racine de la scène
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            // Gérer l'erreur (afficher un message à l'utilisateur)
            System.err.println("Erreur lors du chargement de MenuPrincipal.fxml");
        }

    }

    @FXML
    public void btnAvisReclamation(ActionEvent event) {
        try {
            // Charger la page d'accueil qui contient les modules avis et réclamations
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/home_page.fxml"));
            Parent root = loader.load();
            
            // Obtenir la scène actuelle et la remplacer par home_page.fxml
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            
            System.out.println("Navigation vers la page Avis et Réclamations réussie!");
        } catch (IOException e) {
            e.printStackTrace();
            // Afficher une alerte en cas d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger la page Avis et Réclamations: " + e.getMessage());
            alert.showAndWait();
        }
    }

    }
