package fourty3.grocerypos.model;

public class Product {
    private Integer id;
    private String productCode;
    private String name;
    private double sellingPrice;
    private double importPrice;
    private int stockQuantity;
    private boolean active;

    public Product() {
    }

    public Product(Integer id, String name, double sellingPrice, double importPrice, int stockQuantity, boolean active) {
        this(id, null, name, sellingPrice, importPrice, stockQuantity, active);
    }

    public Product(Integer id, String productCode, String name, double sellingPrice, double importPrice, int stockQuantity, boolean active) {
        this.id = id;
        this.productCode = productCode;
        this.name = name;
        this.sellingPrice = sellingPrice;
        this.importPrice = importPrice;
        this.stockQuantity = stockQuantity;
        this.active = active;
    }

    public Product(String name, double sellingPrice, double importPrice, int stockQuantity) {
        this.name = name;
        this.sellingPrice = sellingPrice;
        this.importPrice = importPrice;
        this.stockQuantity = stockQuantity;
        this.active = true;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public double getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(double importPrice) {
        this.importPrice = importPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}