package fourty3.grocerypos.controller;

import fourty3.grocerypos.model.CartRow;
import fourty3.grocerypos.model.Product;
import fourty3.grocerypos.service.ProductService;
import fourty3.grocerypos.service.SalesService;
import fourty3.grocerypos.util.CurrencyUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.converter.IntegerStringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class SalesController {

    @FXML
    private TextField txtSearch;

    @FXML
    private ListView<Product> lvProducts;

    @FXML
    private Label lblSelectedProduct;

    @FXML
    private Label lblSelectedPrice;

    @FXML
    private Label lblSelectedStock;

    @FXML
    private TextField txtQuantity;

    @FXML
    private TableView<CartRow> cartTable;

    @FXML
    private TableColumn<CartRow, String> colProductName;

    @FXML
    private TableColumn<CartRow, Integer> colQuantity;

    @FXML
    private TableColumn<CartRow, Double> colUnitPrice;

    @FXML
    private TableColumn<CartRow, Double> colLineTotal;

    @FXML
    private Label lblTotalAmount;

    private final ProductService productService = new ProductService();
    private final SalesService salesService = new SalesService();

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<CartRow> cartItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupQuantityField();
        setupSearchRealtime();
        setupProductList();
        setupCartTable();
        loadProducts();
        clearSelectedProductInfo();
    }

    private void setupQuantityField() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d*") ? change : null;
        };

        txtQuantity.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), null, filter));
    }

    private void setupSearchRealtime() {
        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> handleSearch());
    }

    private void setupProductList() {
        lvProducts.setItems(productList);

        lvProducts.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName()
                            + " | Giá: " + CurrencyUtils.formatVnd(item.getSellingPrice())
                            + " | Tồn: " + item.getStockQuantity());
                }
            }
        });

        lvProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                lblSelectedProduct.setText(newValue.getName());
                lblSelectedPrice.setText(CurrencyUtils.formatVnd(newValue.getSellingPrice()));
                lblSelectedStock.setText(String.valueOf(newValue.getStockQuantity()));
                txtQuantity.requestFocus();
                txtQuantity.selectAll();
            } else {
                clearSelectedProductInfo();
            }
        });
    }

    private void setupCartTable() {
        colProductName.setCellValueFactory(cellData -> cellData.getValue().productNameProperty());
        colQuantity.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        colUnitPrice.setCellValueFactory(cellData -> cellData.getValue().unitPriceProperty().asObject());
        colLineTotal.setCellValueFactory(cellData -> cellData.getValue().lineTotalProperty().asObject());

        colUnitPrice.setCellFactory(CurrencyUtils.tableCellFactory());
        colLineTotal.setCellFactory(CurrencyUtils.tableCellFactory());

        cartTable.setItems(cartItems);
    }

    private void loadProducts() {
        productList.setAll(productService.getActiveProducts());
        lvProducts.setItems(productList);
    }

    @FXML
    private void handleSearch() {
        try {
            List<Product> products = productService.searchActiveProducts(txtSearch.getText());
            productList.setAll(products);
        } catch (Exception e) {
            showWarning(e.getMessage());
        }
    }

    @FXML
    private void handleAddToCart() {
        try {
            Product selectedProduct = lvProducts.getSelectionModel().getSelectedItem();
            if (selectedProduct == null) {
                showWarning("Vui lòng chọn sản phẩm.");
                return;
            }

            int quantity = parseQuantity(txtQuantity.getText());
            int quantityAlreadyInCart = getQuantityAlreadyInCart(selectedProduct.getId());
            int newTotalQuantity = quantityAlreadyInCart + quantity;

            if (newTotalQuantity > selectedProduct.getStockQuantity()) {
                showWarning("Số lượng vượt quá tồn kho hiện tại.");
                return;
            }

            CartRow existingRow = findCartRowByProductId(selectedProduct.getId());

            if (existingRow != null) {
                existingRow.setQuantity(existingRow.getQuantity() + quantity);
                cartTable.refresh();
            } else {
                cartItems.add(new CartRow(
                        selectedProduct.getId(),
                        selectedProduct.getName(),
                        quantity,
                        selectedProduct.getSellingPrice(),
                        selectedProduct.getImportPrice()
                ));
            }

            updateTotalAmount();
            txtQuantity.clear();
            txtQuantity.requestFocus();

        } catch (IllegalArgumentException e) {
            showWarning(e.getMessage());
        } catch (Exception e) {
            showWarning("Có lỗi xảy ra khi thêm sản phẩm vào đơn.");
        }
    }

    @FXML
    private void handleRemoveSelectedItem() {
        CartRow selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showWarning("Vui lòng chọn dòng cần xoá.");
            return;
        }

        cartItems.remove(selectedItem);
        updateTotalAmount();
    }

    @FXML
    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            showWarning("Chưa có sản phẩm trong đơn.");
            return;
        }

        try {
            String totalText = lblTotalAmount.getText();

            salesService.checkout(new ArrayList<>(cartItems));

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thanh toán");
            alert.setHeaderText("Thanh toán thành công");
            alert.setContentText("Tổng tiền: " + totalText);
            alert.showAndWait();

            Integer selectedProductId = null;
            Product selectedProduct = lvProducts.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                selectedProductId = selectedProduct.getId();
            }

            cartItems.clear();
            updateTotalAmount();
            txtQuantity.clear();
            loadProducts();

            if (selectedProductId != null) {
                reselectProduct(selectedProductId);
            } else {
                lvProducts.getSelectionModel().clearSelection();
                clearSelectedProductInfo();
            }

            txtQuantity.requestFocus();

        } catch (Exception e) {
            showWarning(e.getMessage());
        }
    }

    private void reselectProduct(Integer productId) {
        for (Product product : productList) {
            if (product.getId().equals(productId)) {
                lvProducts.getSelectionModel().select(product);
                lvProducts.scrollTo(product);
                return;
            }
        }

        lvProducts.getSelectionModel().clearSelection();
        clearSelectedProductInfo();
    }

    private int parseQuantity(String quantityText) {
        if (quantityText == null || quantityText.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập số lượng.");
        }

        try {
            int quantity = Integer.parseInt(quantityText.trim());

            if (quantity <= 0) {
                throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
            }

            return quantity;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số lượng không hợp lệ.");
        }
    }

    private int getQuantityAlreadyInCart(Integer productId) {
        for (CartRow item : cartItems) {
            if (item.getProductId() == productId) {
                return item.getQuantity();
            }
        }
        return 0;
    }

    private CartRow findCartRowByProductId(Integer productId) {
        for (CartRow item : cartItems) {
            if (item.getProductId() == productId) {
                return item;
            }
        }
        return null;
    }

    private void updateTotalAmount() {
        double total = 0.0;

        for (CartRow item : cartItems) {
            total += item.getLineTotal();
        }

        lblTotalAmount.setText(CurrencyUtils.formatVnd(total));
    }

    private void clearSelectedProductInfo() {
        lblSelectedProduct.setText("Chưa chọn sản phẩm");
        lblSelectedPrice.setText("0 VNĐ");
        lblSelectedStock.setText("0");
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}