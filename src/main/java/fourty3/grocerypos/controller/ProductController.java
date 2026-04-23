package fourty3.grocerypos.controller;

import fourty3.grocerypos.model.Product;
import fourty3.grocerypos.service.ProductService;
import fourty3.grocerypos.util.CurrencyUtils;
import fourty3.grocerypos.util.MoneyInputUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ProductController {

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtSellingPrice;

    @FXML
    private TextField txtImportPrice;

    @FXML
    private TextField txtStockQuantity;

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
    private TableColumn<Product, Double> colSellingPrice;

    @FXML
    private TableColumn<Product, Double> colImportPrice;

    @FXML
    private TableColumn<Product, Integer> colStockQuantity;

    @FXML
    private TableColumn<Product, String> colStatus;

    private final ProductService productService = new ProductService();
    private final ObservableList<Product> productItems = FXCollections.observableArrayList();
    private Product selectedProduct;

    @FXML
    public void initialize() {
        setupMoneyFields();
        setupTable();
        setupSelection();
        setupSearchRealtime();
        refreshTable();
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

        colName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        colSellingPrice.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getSellingPrice()));

        colImportPrice.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getImportPrice()));

        colStockQuantity.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getStockQuantity()));

        colStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActive() ? "Đang bán" : "Ngừng bán"));

        colSellingPrice.setCellFactory(CurrencyUtils.tableCellFactory());
        colImportPrice.setCellFactory(CurrencyUtils.tableCellFactory());

        productTable.setItems(productItems);
    }

    private void setupSelection() {
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedProduct = newValue;

            if (newValue != null) {
                fillFormFromProduct(newValue);
            } else {
                clearInputFields();
            }
        });
    }

    private void setupSearchRealtime() {
        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> handleSearch());
    }

    private void refreshTable() {
        productItems.setAll(productService.getAllProducts());
        productTable.refresh();
    }

    @FXML
    private void handleAddProduct() {
        try {
            Product product = readProductFromForm();
            productService.addProduct(product);
            clearForm();
            refreshTable();
            showInfo("Thêm sản phẩm thành công.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleUpdateProduct() {
        if (selectedProduct == null) {
            showError("Vui lòng chọn sản phẩm cần sửa.");
            return;
        }

        try {
            Product product = readProductFromForm();
            product.setId(selectedProduct.getId());

            productService.updateProduct(product);
            clearForm();
            refreshTable();
            showInfo("Cập nhật sản phẩm thành công.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleDeleteProduct() {
        if (selectedProduct == null) {
            showError("Vui lòng chọn sản phẩm cần xoá.");
            return;
        }

        if (!selectedProduct.isActive()) {
            showError("Sản phẩm này đã ở trạng thái ngừng bán.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xoá");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xoá / ngừng bán sản phẩm này không?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                boolean deleted = productService.deleteProduct(selectedProduct.getId());

                clearForm();
                refreshTable();

                if (deleted) {
                    showInfo("Xoá sản phẩm thành công.");
                } else {
                    showInfo("Sản phẩm đã phát sinh dữ liệu nên được chuyển sang trạng thái Ngừng bán.");
                }

            } catch (Exception e) {
                showError(e.getMessage());
            }
        }
    }

    @FXML
    private void handleReactivateProduct() {
        if (selectedProduct == null) {
            showError("Vui lòng chọn sản phẩm cần mở bán lại.");
            return;
        }

        if (selectedProduct.isActive()) {
            showError("Sản phẩm này đang ở trạng thái Đang bán.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận mở bán lại");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn mở bán lại sản phẩm này không?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                productService.reactivateProduct(selectedProduct.getId());
                clearForm();
                refreshTable();
                showInfo("Mở bán lại sản phẩm thành công.");
            } catch (Exception e) {
                showError(e.getMessage());
            }
        }
    }

    @FXML
    private void handleSearch() {
        try {
            productItems.setAll(productService.searchProducts(txtSearch.getText()));
            productTable.refresh();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        clearForm();
        txtSearch.clear();
        refreshTable();
    }

    private Product readProductFromForm() {
        String name = txtName.getText() == null ? "" : txtName.getText().trim();

        double sellingPrice = MoneyInputUtils.parseMoney(txtSellingPrice.getText(), "Giá bán");
        double importPrice = MoneyInputUtils.parseMoney(txtImportPrice.getText(), "Giá nhập");

        int stockQuantity;
        try {
            stockQuantity = Integer.parseInt(txtStockQuantity.getText().trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Số lượng tồn không hợp lệ.");
        }

        return new Product(name, sellingPrice, importPrice, stockQuantity);
    }

    private void fillFormFromProduct(Product product) {
        txtName.setText(product.getName());
        MoneyInputUtils.setMoneyText(txtSellingPrice, product.getSellingPrice());
        MoneyInputUtils.setMoneyText(txtImportPrice, product.getImportPrice());
        txtStockQuantity.setText(String.valueOf(product.getStockQuantity()));
    }

    private void clearInputFields() {
        txtName.clear();
        txtSellingPrice.clear();
        txtImportPrice.clear();
        txtStockQuantity.clear();
    }

    private void clearForm() {
        clearInputFields();
        productTable.getSelectionModel().clearSelection();
        selectedProduct = null;
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupMoneyFields() {
        MoneyInputUtils.installMoneyFormatter(txtSellingPrice);
        MoneyInputUtils.installMoneyFormatter(txtImportPrice);
    }
}