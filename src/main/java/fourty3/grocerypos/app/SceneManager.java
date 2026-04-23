package fourty3.grocerypos.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;

public class SceneManager {

    private SceneManager() {
    }

    public static Node loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Cannot load FXML: " + fxmlPath, e);
        }
    }
}