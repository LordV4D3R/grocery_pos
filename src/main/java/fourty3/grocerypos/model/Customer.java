package fourty3.grocerypos.model;

public class Customer {

    private Integer id;
    private String customerCode;
    private String name;
    private String phone;
    private String address;
    private String note;
    private boolean active;

    public Customer() {
    }

    public Customer(Integer id, String customerCode, String name, String phone, String address, String note, boolean active) {
        this.id = id;
        this.customerCode = customerCode;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.note = note;
        this.active = active;
    }

    public Customer(String name, String phone, String address, String note) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.note = note;
        this.active = true;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}