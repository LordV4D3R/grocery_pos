package fourty3.grocerypos.controller;

import fourty3.grocerypos.model.Product;
import fourty3.grocerypos.service.InventoryService;
import fourty3.grocerypos.service.ProductService;
import fourty3.grocerypos.util.CurrencyUtils;
import fourty3.grocerypos.util.MoneyInputUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.converter.IntegerStringConverter;

import java.util.List;
import java.util.function.UnaryOperator;

public class InventoryController {

    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, Integer> colRowNumber;

    @FXML
    private TableColumn<Product, String> colProductCode;

    @FXML
    private TableColumn<Product, String> colName;

    @FXML
    private TableColumn<Product, Double> colImportPrice;

    @FXML
    private TableColumn<Product, Integer> colStockQuantity;

    @FXML
    private Label lblSelectedProduct;

    @FXML
    private Label lblCurrentStock;

    @FXML
    private Label lblCurrentImportPrice;

    @FXML
    private TextField txtImportQuantity;

    @FXML
    private TextField txtNewImportPrice;

    private final ProductService productService = new ProductService();
    private final InventoryService inventoryService = new InventoryService();
    private final ObservableList<Product> productItems = FXCollections.observableArrayList();

    private Product selectedProduct;

    @FXML
    public void initialize() {
        setupNumberFields();
        setupTable();
        setupSelection();
        loadProducts();
        clearSelectedInfo();
    }

    private void setupNumberFields() {
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d*") ? change : null;
        };

        txtImportQuantity.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), null, integerFilter));
        MoneyInputUtils.installMoneyFormatter(txtNewImportPrice);
    }

    private void setupTable() {
        colRowNumber.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(productTable.getItems().indexOf(cellData.getValue()) + 1));

        colProductCode.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getProductCode() == null
                                ? ""
                                : cellData.getValue().getProductCode()
                ));

        colName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        colImportPrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getImportPrice()));
        colStockQuantity.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStockQuantity()));

        colImportPrice.setCellFactory(CurrencyUtils.tableCellFactory());

        productTable.setItems(productItems);
    }

    private void setupSelection() {
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedProduct = newValue;

            if (newValue != null) {
                lblSelectedProduct.setText(newValue.getName());
                lblCurrentStock.setText(String.valueOf(newValue.getStockQuantity()));
                lblCurrentImportPrice.setText(formatCurrency(newValue.getImportPrice()));
                MoneyInputUtils.setMoneyText(txtNewImportPrice, newValue.getImportPrice());
            } else {
                clearSelectedInfo();
            }
        });
    }

    private void loadProducts() {
        productItems.setAll(productService.getAllProducts());
    }

    @FXML
    private void handleSearch() {
        try {
            List<Product> products = productService.searchProducts(txtSearch.getText());
            productItems.setAll(products);
        } catch (Exception e) {
            showWarning(e.getMessage());
        }
    }

    @FXML
    private void handleImportStock() {
        try {
            if (selectedProduct == null) {
                showWarning("Vui lòng chọn sản phẩm.");
                return;
            }

            int selectedProductId = selectedProduct.getId();
            int importQuantity = parseImportQuantity(txtImportQuantity.getText());
            double newImportPrice = parseImportPrice(txtNewImportPrice.getText());

            inventoryService.importStock(selectedProduct, importQuantity, newImportPrice);

            showInfo("Nhập hàng thành công.");

            txtImportQuantity.clear();
            txtSearch.clear();
            loadProducts();

            Product refreshedProduct = findProductById(selectedProductId);
            if (refreshedProduct != null) {
                productTable.getSelectionModel().select(refreshedProduct);
                productTable.scrollTo(refreshedProduct);
            } else {
                productTable.getSelectionModel().clearSelection();
                selectedProduct = null;
                clearSelectedInfo();
            }

        } catch (IllegalArgumentException e) {
            showWarning(e.getMessage());
        } catch (Exception e) {
            showWarning("Có lỗi xảy ra khi nhập hàng.");
        }
    }

    @FXML
    private void handleRefresh() {
        txtSearch.clear();
        txtImportQuantity.clear();
        txtNewImportPrice.clear();
        productTable.getSelectionModel().clearSelection();
        selectedProduct = null;
        clearSelectedInfo();
        loadProducts();
    }

    private Product findProductById(Integer id) {
        for (Product product : productItems) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        return null;
    }

    private int parseImportQuantity(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập số lượng nhập.");
        }

        try {
            int quantity = Integer.parseInt(text.trim());
            if (quantity <= 0) {
                throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0.");
            }
            return quantity;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số lượng nhập không hợp lệ.");
        }
    }

    private double parseImportPrice(String text) {
        double value = MoneyInputUtils.parseMoney(text, "Giá nhập");

        if (value < 0) {
            throw new IllegalArgumentException("Giá nhập không hợp lệ.");
        }

        return value;
    }

    private void clearSelectedInfo() {
        lblSelectedProduct.setText("Chưa chọn sản phẩm");
        lblCurrentStock.setText("0");
        lblCurrentImportPrice.setText("0 VNĐ");
        txtNewImportPrice.clear();
    }

    private String formatCurrency(double amount) {
        return CurrencyUtils.formatVnd(amount);
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}