package fourty3.grocerypos.model;

import javafx.beans.property.*;

public class CartRow {

    private final IntegerProperty productId = new SimpleIntegerProperty();
    private final StringProperty productName = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final DoubleProperty unitPrice = new SimpleDoubleProperty();
    private final DoubleProperty importPrice = new SimpleDoubleProperty();
    private final DoubleProperty lineTotal = new SimpleDoubleProperty();

    public CartRow(int productId, String productName, int quantity, double unitPrice, double importPrice) {
        this.productId.set(productId);
        this.productName.set(productName);
        this.quantity.set(quantity);
        this.unitPrice.set(unitPrice);
        this.importPrice.set(importPrice);
        recalculateLineTotal();
    }

    public int getProductId() {
        return productId.get();
    }

    public IntegerProperty productIdProperty() {
        return productId;
    }

    public String getProductName() {
        return productName.get();
    }

    public StringProperty productNameProperty() {
        return productName;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
        recalculateLineTotal();
    }

    public double getUnitPrice() {
        return unitPrice.get();
    }

    public DoubleProperty unitPriceProperty() {
        return unitPrice;
    }

    public double getImportPrice() {
        return importPrice.get();
    }

    public DoubleProperty importPriceProperty() {
        return importPrice;
    }

    public double getLineTotal() {
        return lineTotal.get();
    }

    public DoubleProperty lineTotalProperty() {
        return lineTotal;
    }

    private void recalculateLineTotal() {
        lineTotal.set(getQuantity() * getUnitPrice());
    }
}