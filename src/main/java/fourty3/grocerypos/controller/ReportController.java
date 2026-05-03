package fourty3.grocerypos.controller;

import fourty3.grocerypos.model.DailyReportSummary;
import fourty3.grocerypos.model.TopSellingProductReport;
import fourty3.grocerypos.service.ReportService;
import fourty3.grocerypos.util.CurrencyUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.util.List;

public class ReportController {

    @FXML
    private DatePicker dpFromDate;

    @FXML
    private DatePicker dpToDate;

    @FXML
    private Label lblTotalOrders;

    @FXML
    private Label lblTotalRevenue;

    @FXML
    private Label lblCollectedAtSale;

    @FXML
    private Label lblDebtCollected;

    @FXML
    private Label lblActualCollected;

    @FXML
    private Label lblOutstandingDebt;

    @FXML
    private Label lblTotalProfit;

    @FXML
    private TableView<TopSellingProductReport> reportTable;

    @FXML
    private TableColumn<TopSellingProductReport, Integer> colRank;

    @FXML
    private TableColumn<TopSellingProductReport, String> colProductName;

    @FXML
    private TableColumn<TopSellingProductReport, Integer> colTotalQuantity;

    @FXML
    private TableColumn<TopSellingProductReport, Double> colTotalRevenue;

    private final ReportService reportService = new ReportService();
    private final ObservableList<TopSellingProductReport> reportItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();

        LocalDate today = LocalDate.now();
        dpFromDate.setValue(today);
        dpToDate.setValue(today);

        loadReport();
    }

    private void setupTable() {
        colTotalRevenue.setCellFactory(CurrencyUtils.tableCellFactory());

        colRank.setCellValueFactory(cellData -> {
            int index = reportTable.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleObjectProperty<>(index);
        });

        colProductName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProductName()));

        colTotalQuantity.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getTotalQuantity()));

        colTotalRevenue.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getTotalRevenue()));

        reportTable.setItems(reportItems);
    }

    @FXML
    private void handleLoadReport() {
        try {
            loadReport();
        } catch (Exception e) {
            showWarning(e.getMessage());
        }
    }

    private void loadReport() {
        LocalDate fromDate = dpFromDate.getValue();
        LocalDate toDate = dpToDate.getValue();

        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("Vui lòng chọn đầy đủ từ ngày và đến ngày.");
        }

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Từ ngày không được lớn hơn đến ngày.");
        }

        DailyReportSummary summary = reportService.getSummaryByDateRange(fromDate, toDate);
        List<TopSellingProductReport> topProducts =
                reportService.getTopSellingProductsByDateRange(fromDate, toDate);

        lblTotalOrders.setText(String.valueOf(summary.getTotalOrders()));
        lblTotalRevenue.setText(formatCurrency(summary.getTotalRevenue()));
        lblCollectedAtSale.setText(formatCurrency(summary.getTotalCollectedAtSale()));
        lblDebtCollected.setText(formatCurrency(summary.getTotalDebtCollected()));
        lblActualCollected.setText(formatCurrency(summary.getTotalActualCollected()));
        lblOutstandingDebt.setText(formatCurrency(summary.getTotalOutstandingDebt()));
        lblTotalProfit.setText(formatCurrency(summary.getTotalProfit()));

        reportItems.setAll(topProducts);
        reportTable.refresh();
    }

    private String formatCurrency(double amount) {
        return CurrencyUtils.formatVnd(amount);
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}