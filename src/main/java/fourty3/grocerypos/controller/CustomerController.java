package fourty3.grocerypos.controller;

import fourty3.grocerypos.model.Customer;
import fourty3.grocerypos.service.CustomerService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CustomerController {

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtPhone;

    @FXML
    private TextField txtAddress;

    @FXML
    private TextArea txtNote;

    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<Customer> customerTable;

    @FXML
    private TableColumn<Customer, Integer> colRowNumber;

    @FXML
    private TableColumn<Customer, String> colCustomerCode;

    @FXML
    private TableColumn<Customer, String> colName;

    @FXML
    private TableColumn<Customer, String> colPhone;

    @FXML
    private TableColumn<Customer, String> colAddress;

    @FXML
    private TableColumn<Customer, String> colStatus;

    private final CustomerService customerService = new CustomerService();
    private final ObservableList<Customer> customerItems = FXCollections.observableArrayList();
    private Customer selectedCustomer;

    @FXML
    public void initialize() {
        setupTable();
        setupSelection();
        setupSearchRealtime();
        refreshTable();
    }

    private void setupTable() {
        colRowNumber.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(customerTable.getItems().indexOf(cellData.getValue()) + 1));

        colCustomerCode.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getCustomerCode() == null
                                ? ""
                                : cellData.getValue().getCustomerCode()
                ));

        colName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        colPhone.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPhone()));

        colAddress.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getAddress() == null
                                ? ""
                                : cellData.getValue().getAddress()
                ));

        colStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActive() ? "Đang hoạt động" : "Ngừng hoạt động"));

        customerTable.setItems(customerItems);
    }

    private void setupSelection() {
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedCustomer = newValue;

            if (newValue != null) {
                fillFormFromCustomer(newValue);
            } else {
                clearInputFields();
            }
        });
    }

    private void setupSearchRealtime() {
        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> handleSearch());
    }

    private void refreshTable() {
        customerItems.setAll(customerService.getAllCustomers());
        customerTable.refresh();
    }

    @FXML
    private void handleAddCustomer() {
        try {
            Customer customer = readCustomerFromForm();
            customerService.addCustomer(customer);
            clearForm();
            refreshTable();
            showInfo("Thêm khách hàng thành công.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleUpdateCustomer() {
        if (selectedCustomer == null) {
            showError("Vui lòng chọn khách hàng cần sửa.");
            return;
        }

        try {
            Customer customer = readCustomerFromForm();
            customer.setId(selectedCustomer.getId());

            customerService.updateCustomer(customer);
            clearForm();
            refreshTable();
            showInfo("Cập nhật khách hàng thành công.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleDeactivateCustomer() {
        if (selectedCustomer == null) {
            showError("Vui lòng chọn khách hàng cần ngừng hoạt động.");
            return;
        }

        if (!selectedCustomer.isActive()) {
            showError("Khách hàng này đã ở trạng thái ngừng hoạt động.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn ngừng hoạt động khách hàng này không?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                customerService.deactivateCustomer(selectedCustomer.getId());
                clearForm();
                refreshTable();
                showInfo("Đã chuyển khách hàng sang trạng thái ngừng hoạt động.");
            } catch (Exception e) {
                showError(e.getMessage());
            }
        }
    }

    @FXML
    private void handleReactivateCustomer() {
        if (selectedCustomer == null) {
            showError("Vui lòng chọn khách hàng cần mở lại.");
            return;
        }

        if (selectedCustomer.isActive()) {
            showError("Khách hàng này đang ở trạng thái hoạt động.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn mở lại khách hàng này không?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                customerService.reactivateCustomer(selectedCustomer.getId());
                clearForm();
                refreshTable();
                showInfo("Mở lại khách hàng thành công.");
            } catch (Exception e) {
                showError(e.getMessage());
            }
        }
    }

    @FXML
    private void handleSearch() {
        try {
            customerItems.setAll(customerService.searchCustomers(txtSearch.getText()));
            customerTable.refresh();
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

    private Customer readCustomerFromForm() {
        String name = txtName.getText() == null ? "" : txtName.getText().trim();
        String phone = txtPhone.getText() == null ? "" : txtPhone.getText().trim();
        String address = txtAddress.getText() == null ? "" : txtAddress.getText().trim();
        String note = txtNote.getText() == null ? "" : txtNote.getText().trim();

        return new Customer(name, phone, address, note);
    }

    private void fillFormFromCustomer(Customer customer) {
        txtName.setText(customer.getName());
        txtPhone.setText(customer.getPhone());
        txtAddress.setText(customer.getAddress() == null ? "" : customer.getAddress());
        txtNote.setText(customer.getNote() == null ? "" : customer.getNote());
    }

    private void clearInputFields() {
        txtName.clear();
        txtPhone.clear();
        txtAddress.clear();
        txtNote.clear();
    }

    private void clearForm() {
        clearInputFields();
        customerTable.getSelectionModel().clearSelection();
        selectedCustomer = null;
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
}