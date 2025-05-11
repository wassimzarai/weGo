package gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import java.net.URL;
import java.util.ResourceBundle;

public class MessagerieController implements Initializable {
    @FXML private Label chatTitleLabel;
    @FXML private Label unreadCountLabel;
    @FXML private VBox messagesContainer;
    @FXML private TextArea messageInput;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private WebView mapView;
    @FXML private Button refreshMapButton;

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chatTitleLabel.setText("Chat principal");
        loadMap();
    }

    private void loadMap() {
        try {
            URL mapUrl = getClass().getResource("/fxml/map.html");
            if (mapUrl != null) {
                mapView.getEngine().load(mapUrl.toExternalForm());
            } else {
                System.out.println("Fichier map.html introuvable !");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void sendMessage() {
        // Ajoute ici la logique d'envoi de message (exemple d'affichage local)
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            Label msgLabel = new Label(message);
            msgLabel.setStyle("-fx-background-color: #e1bee7; -fx-padding: 8 12; -fx-background-radius: 10; -fx-margin: 4;");
            messagesContainer.getChildren().add(msgLabel);
            messageInput.clear();
            scrollToBottom();
        }
    }

    private void scrollToBottom() {
        Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
    }

    @FXML
    public void sendQuickMessage(ActionEvent actionEvent) {
        Button clickedButton = (Button) actionEvent.getSource();
        String quickMessage = clickedButton.getText();
        if (!quickMessage.isEmpty()) {
            Label msgLabel = new Label(quickMessage);
            msgLabel.setStyle("-fx-background-color: #b2dfdb; -fx-padding: 8 12; -fx-background-radius: 10; -fx-margin: 4;");
            messagesContainer.getChildren().add(msgLabel);
            scrollToBottom();
        }
    }

    @FXML
    private void refreshMap() {
        loadMap();
    }
}
