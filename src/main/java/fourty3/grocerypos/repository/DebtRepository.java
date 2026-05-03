package fourty3.grocerypos.repository;

import fourty3.grocerypos.model.DebtOrderRow;
import fourty3.grocerypos.model.DebtPayment;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DebtRepository {

    private static final double EPSILON = 0.0001;

    public List<DebtOrderRow> findOutstandingOrders() {
        String sql = """
                SELECT
                    o.id AS order_id,
                    o.created_at,
                    c.id AS customer_id,
                    c.customer_code,
                    c.name AS customer_name,
                    c.phone AS customer_phone,
                    o.total_amount,
                    COALESCE(o.paid_amount, 0) AS paid_amount,
                    o.total_amount - COALESCE(o.paid_amount, 0) AS remaining_amount,
                    COALESCE(o.payment_status, 'PAID') AS payment_status
                FROM sale_orders o
                JOIN customers c ON c.id = o.customer_id
                WHERE (o.total_amount - COALESCE(o.paid_amount, 0)) > 0
                ORDER BY datetime(o.created_at) DESC, o.id DESC
                """;

        List<DebtOrderRow> result = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                result.add(mapDebtOrderRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot load outstanding debt orders", e);
        }

        return result;
    }

    public List<DebtOrderRow> searchOutstandingOrders(String keyword) {
        String sql = """
                SELECT
                    o.id AS order_id,
                    o.created_at,
                    c.id AS customer_id,
                    c.customer_code,
                    c.name AS customer_name,
                    c.phone AS customer_phone,
                    o.total_amount,
                    COALESCE(o.paid_amount, 0) AS paid_amount,
                    o.total_amount - COALESCE(o.paid_amount, 0) AS remaining_amount,
                    COALESCE(o.payment_status, 'PAID') AS payment_status
                FROM sale_orders o
                JOIN customers c ON c.id = o.customer_id
                WHERE (o.total_amount - COALESCE(o.paid_amount, 0)) > 0
                  AND (
                      lower(c.name) LIKE ?
                      OR lower(c.phone) LIKE ?
                      OR lower(coalesce(c.customer_code, '')) LIKE ?
                      OR cast(o.id as text) LIKE ?
                  )
                ORDER BY datetime(o.created_at) DESC, o.id DESC
                """;

        List<DebtOrderRow> result = new ArrayList<>();
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        String likeValue = "%" + normalizedKeyword + "%";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, likeValue);
            statement.setString(2, likeValue);
            statement.setString(3, likeValue);
            statement.setString(4, likeValue);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapDebtOrderRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot search outstanding debt orders", e);
        }

        return result;
    }

    public List<DebtPayment> findAllPaymentHistory() {
        String sql = """
                SELECT
                    dp.id,
                    dp.sale_order_id,
                    c.customer_code,
                    c.name AS customer_name,
                    dp.amount_paid,
                    dp.paid_at,
                    dp.note
                FROM debt_payments dp
                JOIN customers c ON c.id = dp.customer_id
                ORDER BY datetime(dp.paid_at) DESC, dp.id DESC
                """;

        List<DebtPayment> result = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                result.add(mapDebtPayment(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot load debt payment history", e);
        }

        return result;
    }

    public List<DebtPayment> searchPaymentHistory(String keyword) {
        String sql = """
                SELECT
                    dp.id,
                    dp.sale_order_id,
                    c.customer_code,
                    c.name AS customer_name,
                    dp.amount_paid,
                    dp.paid_at,
                    dp.note
                FROM debt_payments dp
                JOIN customers c ON c.id = dp.customer_id
                WHERE lower(c.name) LIKE ?
                   OR lower(c.phone) LIKE ?
                   OR lower(coalesce(c.customer_code, '')) LIKE ?
                   OR cast(dp.sale_order_id as text) LIKE ?
                   OR lower(coalesce(dp.note, '')) LIKE ?
                ORDER BY datetime(dp.paid_at) DESC, dp.id DESC
                """;

        List<DebtPayment> result = new ArrayList<>();
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        String likeValue = "%" + normalizedKeyword + "%";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, likeValue);
            statement.setString(2, likeValue);
            statement.setString(3, likeValue);
            statement.setString(4, likeValue);
            statement.setString(5, likeValue);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapDebtPayment(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot search debt payment history", e);
        }

        return result;
    }

    public List<DebtPayment> findPaymentHistoryByOrderId(int orderId) {
        String sql = """
                SELECT
                    dp.id,
                    dp.sale_order_id,
                    c.customer_code,
                    c.name AS customer_name,
                    dp.amount_paid,
                    dp.paid_at,
                    dp.note
                FROM debt_payments dp
                JOIN customers c ON c.id = dp.customer_id
                WHERE dp.sale_order_id = ?
                ORDER BY datetime(dp.paid_at) DESC, dp.id DESC
                """;

        List<DebtPayment> result = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapDebtPayment(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot load debt payment history", e);
        }

        return result;
    }

    public void collectPayment(int saleOrderId, double amountPaid, String note) {
        String selectOrderSql = """
                SELECT id, customer_id, total_amount, COALESCE(paid_amount, 0) AS paid_amount
                FROM sale_orders
                WHERE id = ?
                LIMIT 1
                """;

        String insertPaymentSql = """
                INSERT INTO debt_payments(
                    sale_order_id,
                    customer_id,
                    amount_paid,
                    paid_at,
                    note
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        String updateOrderSql = """
                UPDATE sale_orders
                SET paid_amount = ?,
                    payment_status = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Integer customerId = null;
                double totalAmount = 0;
                double currentPaidAmount = 0;

                try (PreparedStatement statement = connection.prepareStatement(selectOrderSql)) {
                    statement.setInt(1, saleOrderId);

                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Không tìm thấy đơn hàng cần thu nợ.");
                        }

                        customerId = rs.getInt("customer_id");
                        if (rs.wasNull()) {
                            customerId = null;
                        }

                        totalAmount = rs.getDouble("total_amount");
                        currentPaidAmount = rs.getDouble("paid_amount");
                    }
                }

                if (customerId == null) {
                    throw new IllegalArgumentException("Đơn hàng này không gắn khách hàng.");
                }

                double remainingAmount = totalAmount - currentPaidAmount;

                if (amountPaid <= 0) {
                    throw new IllegalArgumentException("Số tiền thu phải lớn hơn 0.");
                }

                if (amountPaid - remainingAmount > EPSILON) {
                    throw new IllegalArgumentException("Số tiền thu vượt quá số tiền còn nợ.");
                }

                try (PreparedStatement statement = connection.prepareStatement(insertPaymentSql)) {
                    statement.setInt(1, saleOrderId);
                    statement.setInt(2, customerId);
                    statement.setDouble(3, amountPaid);
                    statement.setString(4, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    statement.setString(5, note);
                    statement.executeUpdate();
                }

                double newPaidAmount = currentPaidAmount + amountPaid;
                String newStatus = (totalAmount - newPaidAmount) <= EPSILON ? "PAID" : "PARTIAL";

                try (PreparedStatement statement = connection.prepareStatement(updateOrderSql)) {
                    statement.setDouble(1, newPaidAmount);
                    statement.setString(2, newStatus);
                    statement.setInt(3, saleOrderId);
                    statement.executeUpdate();
                }

                connection.commit();

            } catch (Exception e) {
                connection.rollback();

                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }

                throw new RuntimeException("Cannot collect debt payment", e);

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot collect debt payment", e);
        }
    }

    private DebtOrderRow mapDebtOrderRow(ResultSet rs) throws SQLException {
        return new DebtOrderRow(
                rs.getInt("order_id"),
                rs.getString("created_at"),
                rs.getInt("customer_id"),
                rs.getString("customer_code"),
                rs.getString("customer_name"),
                rs.getString("customer_phone"),
                rs.getDouble("total_amount"),
                rs.getDouble("paid_amount"),
                rs.getDouble("remaining_amount"),
                rs.getString("payment_status")
        );
    }

    private DebtPayment mapDebtPayment(ResultSet rs) throws SQLException {
        return new DebtPayment(
                rs.getInt("id"),
                rs.getInt("sale_order_id"),
                rs.getString("customer_code"),
                rs.getString("customer_name"),
                rs.getDouble("amount_paid"),
                rs.getString("paid_at"),
                rs.getString("note")
        );
    }
}