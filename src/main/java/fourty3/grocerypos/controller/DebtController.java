package fourty3.grocerypos.controller;

import fourty3.grocerypos.model.DebtOrderRow;
import fourty3.grocerypos.model.DebtPayment;
import fourty3.grocerypos.service.DebtService;
import fourty3.grocerypos.util.CurrencyUtils;
import fourty3.grocerypos.util.MoneyInputUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class DebtController {

    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<DebtOrderRow> debtTable;

    @FXML
    private TableColumn<DebtOrderRow, Integer> colRowNumber;

    @FXML
    private TableColumn<DebtOrderRow, Integer> colOrderId;

    @FXML
    private TableColumn<DebtOrderRow, String> colCustomerCode;

    @FXML
    private TableColumn<DebtOrderRow, String> colCustomerName;

    @FXML
    private TableColumn<DebtOrderRow, String> colCreatedAt;

    @FXML
    private TableColumn<DebtOrderRow, Double> colTotalAmount;

    @FXML
    private TableColumn<DebtOrderRow, Double> colPaidAmount;

    @FXML
    private TableColumn<DebtOrderRow, Double> colRemainingAmount;

    @FXML
    private TableColumn<DebtOrderRow, String> colPaymentStatus;

    @FXML
    private Label lblSelectedOrder;

    @FXML
    private Label lblSelectedCustomer;

    @FXML
    private Label lblSelectedRemainingAmount;

    @FXML
    private TextField txtCollectAmount;

    @FXML
    private TextArea txtNote;

    @FXML
    private TableView<DebtPayment> historyTable;

    @FXML
    private TableColumn<DebtPayment, String> colHistoryPaidAt;

    @FXML
    private TableColumn<DebtPayment, Double> colHistoryAmount;

    @FXML
    private TableColumn<DebtPayment, String> colHistoryNote;

    private final DebtService debtService = new DebtService();
    private final ObservableList<DebtOrderRow> debtItems = FXCollections.observableArrayList();
    private final ObservableList<DebtPayment> historyItems = FXCollections.observableArrayList();

    private DebtOrderRow selectedOrder;

    @FXML
    public void initialize() {
        setupDebtTable();
        setupHistoryTable();
        setupSelection();
        setupSearchRealtime();
        setupMoneyField();
        refreshData();
        clearSelectedInfo();
    }

    private void setupDebtTable() {
        colRowNumber.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(debtTable.getItems().indexOf(cellData.getValue()) + 1));

        colOrderId.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getOrderId()));

        colCustomerCode.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCustomerCode()));

        colCustomerName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCustomerName()));

        colCreatedAt.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreatedAt()));

        colTotalAmount.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));

        colPaidAmount.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getPaidAmount()));

        colRemainingAmount.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getRemainingAmount()));

        colPaymentStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(toPaymentStatusLabel(cellData.getValue().getPaymentStatus())));

        colTotalAmount.setCellFactory(CurrencyUtils.tableCellFactory());
        colPaidAmount.setCellFactory(CurrencyUtils.tableCellFactory());
        colRemainingAmount.setCellFactory(CurrencyUtils.tableCellFactory());

        debtTable.setItems(debtItems);
    }

    private void setupHistoryTable() {
        colHistoryPaidAt.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPaidAt()));

        colHistoryAmount.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getAmountPaid()));

        colHistoryNote.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNote() == null ? "" : cellData.getValue().getNote()));

        colHistoryAmount.setCellFactory(CurrencyUtils.tableCellFactory());

        historyTable.setItems(historyItems);
    }

    private void setupSelection() {
        debtTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedOrder = newValue;

            if (newValue != null) {
                fillSelectedInfo(newValue);
                loadPaymentHistory(newValue.getOrderId());
                MoneyInputUtils.setMoneyText(txtCollectAmount, newValue.getRemainingAmount());
            } else {
                clearSelectedInfo();
            }
        });
    }

    private void setupSearchRealtime() {
        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> handleSearch());
    }

    private void setupMoneyField() {
        MoneyInputUtils.installMoneyFormatter(txtCollectAmount);
    }

    @FXML
    private void handleSearch() {
        try {
            debtItems.setAll(debtService.searchOutstandingOrders(txtSearch.getText()));
            debtTable.refresh();
        } catch (Exception e) {
            showWarning(e.getMessage());
        }
    }

    @FXML
    private void handleCollectPayment() {
        if (selectedOrder == null) {
            showWarning("Vui lòng chọn đơn cần thu nợ.");
            return;
        }

        try {
            double amount = MoneyInputUtils.parseMoney(txtCollectAmount.getText(), "Số tiền thu");
            debtService.collectPayment(selectedOrder.getOrderId(), amount, txtNote.getText());

            showInfo("Thu nợ thành công.");

            Integer selectedOrderId = selectedOrder.getOrderId();

            txtCollectAmount.clear();
            txtNote.clear();
            refreshData();

            DebtOrderRow refreshedOrder = findDebtOrderById(selectedOrderId);
            if (refreshedOrder != null) {
                debtTable.getSelectionModel().select(refreshedOrder);
                debtTable.scrollTo(refreshedOrder);
            } else {
                debtTable.getSelectionModel().clearSelection();
                clearSelectedInfo();
            }

        } catch (Exception e) {
            showWarning(e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        txtSearch.clear();
        txtCollectAmount.clear();
        txtNote.clear();
        debtTable.getSelectionModel().clearSelection();
        refreshData();
        clearSelectedInfo();
    }

    private void refreshData() {
        debtItems.setAll(debtService.getOutstandingOrders());
        debtTable.refresh();
    }

    private void loadPaymentHistory(int orderId) {
        historyItems.setAll(debtService.getPaymentHistoryByOrderId(orderId));
    }

    private DebtOrderRow findDebtOrderById(Integer orderId) {
        for (DebtOrderRow item : debtItems) {
            if (item.getOrderId().equals(orderId)) {
                return item;
            }
        }
        return null;
    }

    private void fillSelectedInfo(DebtOrderRow order) {
        lblSelectedOrder.setText("Đơn #" + order.getOrderId());
        lblSelectedCustomer.setText(order.getCustomerCode() + " - " + order.getCustomerName());
        lblSelectedRemainingAmount.setText(CurrencyUtils.formatVnd(order.getRemainingAmount()));
    }

    private void clearSelectedInfo() {
        lblSelectedOrder.setText("Chưa chọn đơn");
        lblSelectedCustomer.setText("Chưa chọn khách");
        lblSelectedRemainingAmount.setText("0 VNĐ");
        historyItems.clear();
    }

    private String toPaymentStatusLabel(String paymentStatus) {
        return switch (paymentStatus) {
            case "UNPAID" -> "Chưa thanh toán";
            case "PARTIAL" -> "Thanh toán một phần";
            default -> "Đã thanh toán đủ";
        };
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