package fourty3.grocerypos.controller;

import fourty3.grocerypos.app.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class MainLayoutController {

    @FXML
    private BorderPane rootPane;

    @FXML
    public void initialize() {
        showSalesView();
    }

    @FXML
    private void showSalesView() {
        rootPane.setCenter(SceneManager.loadView("/fourty3/grocerypos/fxml/SalesView.fxml"));
    }

    @FXML
    private void showProductView() {
        rootPane.setCenter(SceneManager.loadView("/fourty3/grocerypos/fxml/ProductView.fxml"));
    }

    @FXML
    private void showInventoryView() {
        rootPane.setCenter(SceneManager.loadView("/fourty3/grocerypos/fxml/InventoryView.fxml"));
    }

    @FXML
    private void showReportView() {
        rootPane.setCenter(SceneManager.loadView("/fourty3/grocerypos/fxml/ReportView.fxml"));
    }
}