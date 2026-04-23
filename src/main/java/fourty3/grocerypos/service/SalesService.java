package fourty3.grocerypos.service;

import fourty3.grocerypos.model.CartRow;
import fourty3.grocerypos.repository.SaleOrderRepository;

import java.util.List;

public class SalesService {

    private final SaleOrderRepository saleOrderRepository = new SaleOrderRepository();

    public void checkout(List<CartRow> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Chưa có sản phẩm trong đơn.");
        }

        for (CartRow item : cartItems) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng sản phẩm không hợp lệ.");
            }
        }

        saleOrderRepository.checkout(cartItems);
    }
}
