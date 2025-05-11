package Controllers;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import utils.database;
import javafx.fxml.FXMLLoader;
import Entites.UtilisateurRole;
import Entites.user;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import Entites.utilisateur;

public class Backoffice implements Initializable {
    @FXML
    public Button export_excel;
    @FXML
    private Circle circle_back;
    @FXML
    private Label hello;
    @FXML
    private Button home_back;
    @FXML
    private Button edit_back;
    @FXML
    private AnchorPane admin_page;

    @FXML
    private ImageView imageview_back;

    @FXML
    private AnchorPane backoffice1;

    @FXML
    private Button clear_back;

    @FXML
    private Button afficher;

    @FXML
    private DatePicker date_back;

    @FXML
    private TableColumn<utilisateur, String> date_col;

    @FXML
    private Button delete_back;

    @FXML
    private TextField email_back;

    @FXML
    private TableColumn<utilisateur, String> email_col;

    @FXML
    private ComboBox<String> gender_back;

    @FXML
    private TableColumn<utilisateur, String> gender_col;

    @FXML
    private TextField id_back;

    @FXML
    private TableColumn<utilisateur, Integer> id_col;

    @FXML
    private Button image_back;

    @FXML
    private TableColumn<utilisateur, String> image_col;

    @FXML
    private Button insert_back;

    @FXML
    private TextField password_back;

    @FXML
    private TableColumn<utilisateur, String> password_col;

    @FXML
    private ComboBox<String> role_back;

    @FXML
    private TableColumn<utilisateur, String> role_col;

    @FXML
    private TableView<utilisateur> table_view;

    @FXML
    private Button update_back;

    @FXML
    private TextField username_back;

    @FXML
    private TableColumn<utilisateur, String> username_col;

    @FXML
    private Label user_count_label;

    @FXML
    private Button generate_excel;
    @FXML
    private Button solyama1;

    @FXML
    private Button voitrue1;

    @FXML
    private Button reservation;
    @FXML
    private Button TRAJET1;


    private Connection connect;
    private Statement statement;
    private PreparedStatement prepare;
    private ResultSet result;
    private Image image;

    private String genderData[] = {"Male", "Female", "Others"};

    public void comboBox() {

        List<String> list = new ArrayList<>();

        for (String data1 : genderData) {

            list.add(data1);

        }

        ObservableList dataList = FXCollections.observableArrayList(list);

        gender_back.setItems(dataList);

    }


    private String role[] = {"admin", "client"};

    public void comboBox1() {

        List<String> list = new ArrayList<>();

        for (String data1 : role) {

            list.add(data1);

        }

        ObservableList dataList = FXCollections.observableArrayList(list);

        role_back.setItems(dataList);

    }

    public void insert() {
        connect = database.connectDb();
        String sql = "INSERT INTO utilisateur (username, password, email, image, id, gender, date, role) VALUES (?,?,?,?,?,?,?,?)";

        try {
            if (id_back.getText().isEmpty() || username_back.getText().isEmpty()
                    || password_back.getText().isEmpty()
                    || email_back.getText().isEmpty()
                    || gender_back.getSelectionModel().isEmpty()
                    || role_back.getSelectionModel().isEmpty()
                    || date_back.getValue() == null) {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Message d'erreur");
                alert.setHeaderText(null);
                alert.setContentText("Veuillez remplir tous les champs !");
                alert.showAndWait();
                return;
            }


            prepare = connect.prepareStatement(sql);
            prepare.setString(1, username_back.getText());
            prepare.setString(2, password_back.getText());
            prepare.setString(3, email_back.getText());
            prepare.setNull(4, java.sql.Types.VARCHAR); // image
            prepare.setString(5, id_back.getText());
            prepare.setString(6, gender_back.getSelectionModel().getSelectedItem());
            prepare.setString(7, date_back.getValue().toString());
            prepare.setString(8, role_back.getSelectionModel().getSelectedItem());

            int rowsAffected = prepare.executeUpdate();

            if (rowsAffected > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Message de succès");
                alert.setHeaderText(null);
                alert.setContentText("Enregistrement ajouté avec succès !");
                alert.showAndWait();

                showData();
                clear();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Message d'avertissement");
                alert.setHeaderText(null);
                alert.setContentText("Aucun enregistrement n'a été ajouté. Veuillez vérifier vos données.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Message d'erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur s'est produite lors de l'ajout de l'enregistrement : " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void clear() {

        id_back.setText("");
        username_back.setText("");
        password_back.setText("");
        email_back.setText("");

        gender_back.getSelectionModel().clearSelection();
        role_back.getSelectionModel().clearSelection();
        imageview_back.setImage(null);
        date_back.setValue(null);

    }

    public void selectutilisateur() {
        try {
            int num = table_view.getSelectionModel().getSelectedIndex();
            if (num < 0) {
                return;
            }

            utilisateur utilisateur1 = table_view.getSelectionModel().getSelectedItem();
            if (utilisateur1 == null) {
                return;
            }

            id_back.setText(String.valueOf(utilisateur1.getId()));
            username_back.setText(utilisateur1.getUsername());
            password_back.setText(utilisateur1.getPassword());
            email_back.setText(utilisateur1.getEmail());
            gender_back.getSelectionModel().clearSelection();
            role_back.getSelectionModel().clearSelection();

            if (utilisateur1.getImage() != null && !utilisateur1.getImage().isEmpty()) {
                try {
                    image = new Image("file:" + utilisateur1.getImage(), 110, 110, false, true);
                    imageview_back.setImage(image);
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement de l'image : " + e.getMessage());
                    imageview_back.setImage(null);
                }
            } else {
                imageview_back.setImage(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Message d'erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur s'est produite lors de la sélection de l'enregistrement : " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void exit() {

        System.exit(0);

    }

    public void update() {
        connect = database.connectDb();
        String sql = "UPDATE utilisateur SET username = ?, password = ?, email = ?, image = ?, gender = ?, date = ?, role = ? WHERE id = ?";

        try {
            if (id_back.getText().isEmpty() || username_back.getText().isEmpty()
                    || email_back.getText().isEmpty()
                    || password_back.getText().isEmpty()
                    || gender_back.getSelectionModel().isEmpty()
                    || role_back.getSelectionModel().isEmpty()
                    || date_back.getValue() == null) {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Please fill in all required fields!");
                alert.showAndWait();
                return;
            }

            prepare = connect.prepareStatement(sql);
            prepare.setString(1, username_back.getText());
            prepare.setString(2, password_back.getText());
            prepare.setString(3, email_back.getText());
            prepare.setNull(4, java.sql.Types.VARCHAR); // image
            prepare.setString(5, (String) gender_back.getSelectionModel().getSelectedItem());
            prepare.setString(6, date_back.getValue().toString());
            prepare.setString(7, (String) role_back.getSelectionModel().getSelectedItem());
            prepare.setString(8, id_back.getText());

            int rowsAffected = prepare.executeUpdate();

            if (rowsAffected > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success Message");
                alert.setHeaderText(null);
                alert.setContentText("Successfully Updated!");
                alert.showAndWait();

                showData();
                clear();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning Message");
                alert.setHeaderText(null);
                alert.setContentText("No records were updated. Please check the ID.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while updating the record: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void delete() {
        connect = database.connectDb();
        String sql = "DELETE FROM utilisateur WHERE id = ?";

        try {
            if (id_back.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Please enter the ID of the record to delete");
                alert.showAndWait();
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Message");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete this record?");

            Optional<ButtonType> option = alert.showAndWait();
            if (option.get() != ButtonType.OK) {
                return;
            }

            prepare = connect.prepareStatement(sql);
            prepare.setString(1, id_back.getText());

            int rowsAffected = prepare.executeUpdate();

            if (rowsAffected > 0) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success Message");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Record successfully deleted!");
                successAlert.showAndWait();

                showData();
                clear();
            } else {
                Alert warningAlert = new Alert(Alert.AlertType.WARNING);
                warningAlert.setTitle("Warning Message");
                warningAlert.setHeaderText(null);
                warningAlert.setContentText("No records were deleted. Please check the ID.");
                warningAlert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while deleting the record: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public ObservableList<utilisateur> listutilisateur() {
        ObservableList<utilisateur> utilisateurList = FXCollections.observableArrayList();
        connect = database.connectDb();
        String sql = "SELECT * FROM utilisateur";

        try {
            statement = connect.createStatement();
            result = statement.executeQuery(sql);
            utilisateur utilisateur1;

            while (result.next()) {
                // Convert the role string to enum
                String roleString = result.getString("role");
                UtilisateurRole role = null;
                if (roleString != null) {
                    try {
                        role = UtilisateurRole.valueOf(roleString.toUpperCase()); // assuming DB values are in lower/mixed case
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid role found in DB: " + roleString);
                        continue; // skip this record or handle it as needed
                    }
                }

                utilisateur1 = new utilisateur(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("email"),
                        result.getString("password"),
                        role,
                        result.getString("gender"),
                        result.getString("image"),
                        result.getString("date")
                );

                utilisateurList.add(utilisateur1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return utilisateurList;
    }


    //    INSERT IMAGE


    public void showData() {

        ObservableList<utilisateur> show = listutilisateur();

        username_col.setCellValueFactory(new PropertyValueFactory<>("username"));
        password_col.setCellValueFactory(new PropertyValueFactory<>("password"));
        password_col.setCellFactory(getPasswordTableCellFactory());
        email_col.setCellValueFactory(new PropertyValueFactory<>("email"));
        image_col.setCellValueFactory(new PropertyValueFactory<>("image"));
        id_col.setCellValueFactory(new PropertyValueFactory<>("id"));
        gender_col.setCellValueFactory(new PropertyValueFactory<>("gender"));
        date_col.setCellValueFactory(new PropertyValueFactory<>("date"));

        table_view.setItems(show);

    }

    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> getPasswordTableCellFactory() {
        return column -> {
            TableCell<S, String> cell = new TableCell<S, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                    } else {
                        // Affichez une valeur masquée (par exemple, des astérisques) au lieu du mot de passe
                        StringBuilder maskedPassword = new StringBuilder();
                        for (int i = 0; i < item.length(); i++) {
                            maskedPassword.append("*");
                        }
                        setText(maskedPassword.toString());
                    }
                }
            };
            return cell;
        };
    }

    public void afficherStatistiques() {
        try {
            // Charger le fichier FXML de la vue "statics.fxml"
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/statics.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène et y ajouter la vue chargée
            Scene scene = new Scene(root);

            // Accéder à la scène actuelle à partir du bouton (ou d'un autre nœud)
            Stage stage = (Stage) afficher.getScene().getWindow();

            // Définir la nouvelle scène sur la fenêtre principale
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void changePage() {

        if (home_back.isFocused()) {

            backoffice1.setVisible(true);
            admin_page.setVisible(false);

        }
        if (edit_back.isFocused()) {
            backoffice1.setVisible(false);
            admin_page.setVisible(true);

        }
    }

    public void account() {

        hello.setText(user.username);

    }


    public void displayImage() {
        // Use the provided image path directly
        String uri = "file:/C:/Users/wassi/Pictures/487673136_3987121521573655_8977661362410615366_n (1).jpg";
        image = new Image(uri, 150, 150, false, true);
        circle_back.setFill(new ImagePattern(image));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        showData();
        comboBox();
        comboBox1();
        account();
        displayImage();
    }

    @FXML
    public void generateExcel() {
        Workbook workbook = null;
        FileOutputStream fileOut = null;
        try {
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Users Data");

            // Créer un style pour les en-têtes
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Créer l'en-tête
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Username", "Password", "Email", "Gender", "Date"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Récupérer les données de la base de données
            String sql = "SELECT * FROM utilisateur";
            connect = database.connectDb();
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            int rowNum = 1;
            while (result.next()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(result.getInt("id"));
                row.createCell(1).setCellValue(result.getString("username"));
                String password = result.getString("password");
                String maskedPassword = "*".repeat(password != null ? password.length() : 0);
                row.createCell(2).setCellValue(maskedPassword);
                row.createCell(3).setCellValue(result.getString("email"));
                row.createCell(4).setCellValue(result.getString("gender"));
                row.createCell(5).setCellValue(result.getString("date"));
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Excel File");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );
            java.io.File file = fileChooser.showSaveDialog(generate_excel.getScene().getWindow());

            if (file != null) {
                fileOut = new FileOutputStream(file);
                workbook.write(fileOut);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Excel file has been generated successfully!");
                alert.showAndWait();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error generating Excel file: " + e.getMessage());
            alert.showAndWait();
        } finally {
            try {
                if (fileOut != null) {
                    fileOut.close();
                }
                if (workbook != null) {
                    workbook.close();
                }
                if (connect != null) {
                    connect.close();
                }
            } catch (IOException | java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void btnpaiement1(ActionEvent actionEvent) {
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
            System.err.println("Erreur lors du chargement de MenuPrincipal.fxml");
        }
    }

    public void btnvoiture1(ActionEvent actionEvent) {
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

    public void btnReservation(ActionEvent actionEvent) {
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


    public void btnTrajet1(ActionEvent actionEvent) {
        try {
            // Charger la nouvelle scène
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/trajet.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Gérer l'exception
        }
    }
    
    @FXML
    public void btnAvisReclamation(ActionEvent actionEvent) {
        try {
            // Charger la page d'accueil avec le bon chemin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/home_page.fxml"));
            Parent root = loader.load();
            
            // Obtenir la scène actuelle et changer à la nouvelle scène
            Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            
            // Afficher un message de succès de navigation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Navigation réussie");
            alert.setHeaderText(null);
            alert.setContentText("Navigation vers la page d'accueil réussie!");
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            // Afficher une alerte en cas d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger la page d'accueil. Erreur: " + e.getMessage());
            alert.showAndWait();
        }
    }
}

