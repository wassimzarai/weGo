package Controllers;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;

public class CarteTrajet {

    @FXML
    private WebView mapView;

    private static String mapUrl;

    public static void setMapUrl(String url) {
        mapUrl = url;
    }

    @FXML
    public void initialize() {
        if (mapUrl != null && !mapUrl.isEmpty()) {
            mapView.getEngine().load(mapUrl);
        }
    }
}
