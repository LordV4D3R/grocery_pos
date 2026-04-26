package fourty3.grocerypos.controller;

import fourty3.grocerypos.model.CartRow;
import fourty3.grocerypos.model.Customer;
import fourty3.grocerypos.model.Product;
import fourty3.grocerypos.service.CustomerService;
import fourty3.grocerypos.service.ProductService;
import fourty3.grocerypos.service.SalesService;
import fourty3.grocerypos.util.CurrencyUtils;
import fourty3.grocerypos.util.MoneyInputUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.converter.IntegerStringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class SalesController {

    private static final double EPSILON = 0.0001;

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

    @FXML
    private ComboBox<Customer> cbCustomer;

    @FXML
    private TextField txtPaidAmount;

    @FXML
    private Label lblRemainingAmount;

    @FXML
    private Label lblPaymentStatus;

    private final ProductService productService = new ProductService();
    private final CustomerService customerService = new CustomerService();
    private final SalesService salesService = new SalesService();

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<CartRow> cartItems = FXCollections.observableArrayList();
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupQuantityField();
        setupPaidAmountField();
        setupSearchRealtime();
        setupProductList();
        setupCustomerBox();
        setupCartTable();
        loadProducts();
        loadCustomers();
        clearSelectedProductInfo();
        updatePaymentSummary();
    }

    private void setupQuantityField() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d*") ? change : null;
        };

        txtQuantity.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), null, filter));
    }

    private void setupPaidAmountField() {
        MoneyInputUtils.installMoneyFormatter(txtPaidAmount);
        txtPaidAmount.textProperty().addListener((obs, oldValue, newValue) -> updatePaymentSummary());
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

    private void setupCustomerBox() {
        cbCustomer.setItems(customerList);
        cbCustomer.setButtonCell(createCustomerCell());
        cbCustomer.setCellFactory(listView -> createCustomerCell());
        cbCustomer.valueProperty().addListener((obs, oldValue, newValue) -> updatePaymentSummary());
    }

    private ListCell<Customer> createCustomerCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    String code = item.getCustomerCode() == null ? "" : item.getCustomerCode();
                    setText(code + " - " + item.getName() + " - " + item.getPhone());
                }
            }
        };
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

    private void loadCustomers() {
        customerList.setAll(
                customerService.getAllCustomers()
                        .stream()
                        .filter(Customer::isActive)
                        .toList()
        );
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
    private void handleClearCustomerSelection() {
        cbCustomer.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            showWarning("Chưa có sản phẩm trong đơn.");
            return;
        }

        try {
            double totalAmount = calculateCartTotal();
            double paidAmount = parsePaidAmount(totalAmount);

            validateCheckout(totalAmount, paidAmount);

            String paymentStatus = determinePaymentStatus(totalAmount, paidAmount);
            Customer selectedCustomer = cbCustomer.getValue();
            Integer customerId = selectedCustomer == null ? null : selectedCustomer.getId();
            String customerText = selectedCustomer == null ? "Khách lẻ" : selectedCustomer.getName();
            double remainingAmount = Math.max(0, totalAmount - paidAmount);

            salesService.checkout(new ArrayList<>(cartItems), customerId, paidAmount, paymentStatus);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thanh toán");
            alert.setHeaderText("Thanh toán thành công");
            alert.setContentText(
                    "Khách hàng: " + customerText
                            + "\nTổng tiền: " + CurrencyUtils.formatVnd(totalAmount)
                            + "\nĐã trả: " + CurrencyUtils.formatVnd(paidAmount)
                            + "\nCòn nợ: " + CurrencyUtils.formatVnd(remainingAmount)
                            + "\nTrạng thái: " + toPaymentStatusLabel(paymentStatus)
            );
            alert.showAndWait();

            Integer selectedProductId = null;
            Product selectedProduct = lvProducts.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                selectedProductId = selectedProduct.getId();
            }

            cartItems.clear();
            txtQuantity.clear();
            txtPaidAmount.clear();
            cbCustomer.getSelectionModel().clearSelection();
            updateTotalAmount();
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

    private void validateCheckout(double totalAmount, double paidAmount) {
        if (paidAmount < 0) {
            throw new IllegalArgumentException("Số tiền khách trả không hợp lệ.");
        }

        if (paidAmount > totalAmount + EPSILON) {
            throw new IllegalArgumentException("Số tiền khách trả không được lớn hơn tổng tiền.");
        }

        if (paidAmount + EPSILON < totalAmount && cbCustomer.getValue() == null) {
            throw new IllegalArgumentException("Đơn chưa thanh toán đủ phải chọn khách hàng.");
        }
    }

    private String determinePaymentStatus(double totalAmount, double paidAmount) {
        if (paidAmount <= EPSILON) {
            return "UNPAID";
        }

        if (paidAmount + EPSILON < totalAmount) {
            return "PARTIAL";
        }

        return "PAID";
    }

    private String toPaymentStatusLabel(String paymentStatus) {
        return switch (paymentStatus) {
            case "UNPAID" -> "Chưa thanh toán";
            case "PARTIAL" -> "Thanh toán một phần";
            default -> "Đã thanh toán đủ";
        };
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

    private double parsePaidAmount(double totalAmount) {
        String paidText = txtPaidAmount.getText();

        if (paidText == null || paidText.trim().isEmpty()) {
            return totalAmount;
        }

        return MoneyInputUtils.parseMoney(paidText, "Số tiền khách trả");
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

    private double calculateCartTotal() {
        double total = 0.0;

        for (CartRow item : cartItems) {
            total += item.getLineTotal();
        }

        return total;
    }

    private void updateTotalAmount() {
        double total = calculateCartTotal();
        lblTotalAmount.setText(CurrencyUtils.formatVnd(total));
        updatePaymentSummary();
    }

    private void updatePaymentSummary() {
        double totalAmount = calculateCartTotal();

        if (totalAmount <= EPSILON) {
            lblRemainingAmount.setText("0 VNĐ");
            lblPaymentStatus.setText("Chưa có đơn");
            return;
        }

        try {
            double paidAmount = parsePaidAmount(totalAmount);

            if (paidAmount > totalAmount + EPSILON) {
                lblRemainingAmount.setText("0 VNĐ");
                lblPaymentStatus.setText("Khách trả vượt tổng tiền");
                return;
            }

            double remainingAmount = Math.max(0, totalAmount - paidAmount);
            lblRemainingAmount.setText(CurrencyUtils.formatVnd(remainingAmount));

            if (remainingAmount <= EPSILON) {
                lblPaymentStatus.setText("Đã thanh toán đủ");
            } else if (paidAmount <= EPSILON) {
                lblPaymentStatus.setText(cbCustomer.getValue() == null
                        ? "Nợ toàn bộ (chưa chọn KH)"
                        : "Nợ toàn bộ");
            } else {
                lblPaymentStatus.setText(cbCustomer.getValue() == null
                        ? "Thanh toán một phần (chưa chọn KH)"
                        : "Thanh toán một phần");
            }

        } catch (Exception e) {
            lblRemainingAmount.setText("0 VNĐ");
            lblPaymentStatus.setText("Số tiền khách trả không hợp lệ");
        }
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