package fourty3.grocerypos.service;

import fourty3.grocerypos.model.CartRow;
import fourty3.grocerypos.repository.SaleOrderRepository;

import java.util.List;

public class SalesService {

    private final SaleOrderRepository saleOrderRepository = new SaleOrderRepository();

    public void checkout(List<CartRow> cartItems) {
        saleOrderRepository.checkout(cartItems);
    }

    public void checkout(List<CartRow> cartItems, Integer customerId, double paidAmount, String paymentStatus) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Chưa có sản phẩm trong đơn.");
        }

        if (paidAmount < 0) {
            throw new IllegalArgumentException("Số tiền khách trả không hợp lệ.");
        }

        saleOrderRepository.checkout(cartItems, customerId, paidAmount, paymentStatus);
    }
}