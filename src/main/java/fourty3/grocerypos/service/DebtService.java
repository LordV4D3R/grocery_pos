package fourty3.grocerypos.service;

import fourty3.grocerypos.model.DebtOrderRow;
import fourty3.grocerypos.model.DebtPayment;
import fourty3.grocerypos.repository.DebtRepository;

import java.util.List;

public class DebtService {

    private final DebtRepository debtRepository = new DebtRepository();

    public List<DebtOrderRow> getOutstandingOrders() {
        return debtRepository.findOutstandingOrders();
    }

    public List<DebtOrderRow> searchOutstandingOrders(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return debtRepository.findOutstandingOrders();
        }

        return debtRepository.searchOutstandingOrders(keyword);
    }

    public List<DebtPayment> getAllPaymentHistory() {
        return debtRepository.findAllPaymentHistory();
    }

    public List<DebtPayment> searchPaymentHistory(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return debtRepository.findAllPaymentHistory();
        }

        return debtRepository.searchPaymentHistory(keyword);
    }

    public List<DebtPayment> getPaymentHistoryByOrderId(int orderId) {
        return debtRepository.findPaymentHistoryByOrderId(orderId);
    }

    public void collectPayment(int saleOrderId, double amountPaid, String note) {
        String normalizedNote = note == null ? "" : note.trim();
        debtRepository.collectPayment(saleOrderId, amountPaid, normalizedNote);
    }
}