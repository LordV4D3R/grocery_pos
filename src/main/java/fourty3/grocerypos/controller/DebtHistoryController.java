package fourty3.grocerypos.controller;

import fourty3.grocerypos.model.DebtPayment;
import fourty3.grocerypos.service.DebtService;
import fourty3.grocerypos.util.CurrencyUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class DebtHistoryController {

    @FXML
    private TextField txtSearch;

    @FXML
    private Label lblTotalCollected;

    @FXML
    private TableView<DebtPayment> historyTable;

    @FXML
    private TableColumn<DebtPayment, Integer> colRowNumber;

    @FXML
    private TableColumn<DebtPayment, String> colPaidAt;

    @FXML
    private TableColumn<DebtPayment, Integer> colOrderId;

    @FXML
    private TableColumn<DebtPayment, String> colCustomerCode;

    @FXML
    private TableColumn<DebtPayment, String> colCustomerName;

    @FXML
    private TableColumn<DebtPayment, Double> colAmountPaid;

    @FXML
    private TableColumn<DebtPayment, String> colNote;

    private final DebtService debtService = new DebtService();
    private final ObservableList<DebtPayment> historyItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupSearchRealtime();
        refreshData();
    }

    private void setupTable() {
        colRowNumber.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(historyTable.getItems().indexOf(cellData.getValue()) + 1));

        colPaidAt.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPaidAt()));

        colOrderId.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getSaleOrderId()));

        colCustomerCode.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCustomerCode() == null ? "" : cellData.getValue().getCustomerCode()));

        colCustomerName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCustomerName() == null ? "" : cellData.getValue().getCustomerName()));

        colAmountPaid.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getAmountPaid()));

        colNote.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNote() == null ? "" : cellData.getValue().getNote()));

        colAmountPaid.setCellFactory(CurrencyUtils.tableCellFactory());

        historyTable.setItems(historyItems);
    }

    private void setupSearchRealtime() {
        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> handleSearch());
    }

    @FXML
    private void handleSearch() {
        historyItems.setAll(debtService.searchPaymentHistory(txtSearch.getText()));
        historyTable.refresh();
        updateTotalCollected();
    }

    @FXML
    private void handleRefresh() {
        txtSearch.clear();
        refreshData();
    }

    private void refreshData() {
        historyItems.setAll(debtService.getAllPaymentHistory());
        historyTable.refresh();
        updateTotalCollected();
    }

    private void updateTotalCollected() {
        double total = 0.0;

        for (DebtPayment item : historyItems) {
            total += item.getAmountPaid();
        }

        lblTotalCollected.setText(CurrencyUtils.formatVnd(total));
    }
}