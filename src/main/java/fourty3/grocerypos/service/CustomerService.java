package fourty3.grocerypos.service;

import fourty3.grocerypos.model.Customer;
import fourty3.grocerypos.repository.CustomerRepository;

import java.util.List;

public class CustomerService {

    private final CustomerRepository customerRepository = new CustomerRepository();

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<Customer> searchCustomers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return customerRepository.findAll();
        }

        return customerRepository.search(keyword);
    }

    public Customer getCustomerById(int id) {
        return customerRepository.findById(id);
    }

    public void addCustomer(Customer customer) {
        validateCustomer(customer);

        if (customerRepository.existsByPhone(customer.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại khách hàng đã tồn tại.");
        }

        customerRepository.insert(customer);
    }

    public void updateCustomer(Customer customer) {
        if (customer.getId() == null) {
            throw new IllegalArgumentException("Customer ID is required for update.");
        }

        validateCustomer(customer);

        if (customerRepository.existsByPhoneExcludingId(customer.getPhone(), customer.getId())) {
            throw new IllegalArgumentException("Số điện thoại khách hàng đã tồn tại.");
        }

        customerRepository.update(customer);
    }

    public void deactivateCustomer(int id) {
        customerRepository.deactivateById(id);
    }

    public void reactivateCustomer(int id) {
        customerRepository.reactivateById(id);
    }

    private void validateCustomer(Customer customer) {
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khách hàng không được để trống.");
        }

        if (customer.getPhone() == null || customer.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        }

        customer.setName(customer.getName().trim());
        customer.setPhone(normalizePhone(customer.getPhone()));

        if (!customer.getPhone().matches("\\d{8,15}")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ.");
        }

        if (customer.getAddress() != null) {
            customer.setAddress(customer.getAddress().trim());
        }

        if (customer.getNote() != null) {
            customer.setNote(customer.getNote().trim());
        }
    }

    private String normalizePhone(String phone) {
        return phone.trim()
                .replace(" ", "")
                .replace(".", "")
                .replace("-", "");
    }
}