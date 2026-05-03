package fourty3.grocerypos.model;

public class DebtPayment {

    private Integer id;
    private Integer saleOrderId;
    private String customerCode;
    private String customerName;
    private double amountPaid;
    private String paidAt;
    private String note;

    public DebtPayment() {
    }

    public DebtPayment(Integer id, Integer saleOrderId, String customerCode, String customerName,
                       double amountPaid, String paidAt, String note) {
        this.id = id;
        this.saleOrderId = saleOrderId;
        this.customerCode = customerCode;
        this.customerName = customerName;
        this.amountPaid = amountPaid;
        this.paidAt = paidAt;
        this.note = note;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSaleOrderId() {
        return saleOrderId;
    }

    public void setSaleOrderId(Integer saleOrderId) {
        this.saleOrderId = saleOrderId;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(String paidAt) {
        this.paidAt = paidAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}