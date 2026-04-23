package fourty3.grocerypos.app;

import fourty3.grocerypos.repository.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseManager.initializeDatabase();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fourty3/grocerypos/fxml/MainLayout.fxml")
        );

        BorderPane root = loader.load();
        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(
                getClass().getResource("/fourty3/grocerypos/css/app.css").toExternalForm()
        );

        primaryStage.setTitle("Store POS");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);
        primaryStage.show();

        System.out.println("Database path: " + DatabaseManager.getDatabasePath());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
