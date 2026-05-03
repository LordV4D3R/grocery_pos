package fourty3.grocerypos.repository;

import fourty3.grocerypos.model.CartRow;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SaleOrderRepository {

    public void checkout(List<CartRow> cartItems) {
        double totalAmount = calculateTotalAmount(cartItems);
        checkout(cartItems, null, totalAmount, "PAID");
    }

    public void checkout(List<CartRow> cartItems, Integer customerId, double paidAmount, String paymentStatus) {
        String insertOrderSql = """
                INSERT INTO sale_orders(
                    created_at,
                    customer_id,
                    total_amount,
                    initial_paid_amount,
                    paid_amount,
                    payment_status
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        String insertOrderItemSql = """
                INSERT INTO sale_order_items(
                    order_id,
                    product_id,
                    product_name,
                    quantity,
                    unit_price,
                    import_price,
                    line_total
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        String updateStockSql = """
                UPDATE products
                SET stock_quantity = stock_quantity - ?
                WHERE id = ? AND stock_quantity >= ?
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int orderId = insertOrder(connection, insertOrderSql, cartItems, customerId, paidAmount, paymentStatus);

                for (CartRow item : cartItems) {
                    decreaseStock(connection, updateStockSql, item);
                    insertOrderItem(connection, insertOrderItemSql, orderId, item);
                }

                connection.commit();

            } catch (Exception e) {
                connection.rollback();

                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }

                throw new RuntimeException("Cannot checkout order", e);

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot checkout order", e);
        }
    }

    private int insertOrder(Connection connection,
                            String sql,
                            List<CartRow> cartItems,
                            Integer customerId,
                            double paidAmount,
                            String paymentStatus) throws SQLException {

        double totalAmount = calculateTotalAmount(cartItems);
        String createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, createdAt);

            if (customerId == null) {
                statement.setNull(2, Types.INTEGER);
            } else {
                statement.setInt(2, customerId);
            }

            statement.setDouble(3, totalAmount);
            statement.setDouble(4, paidAmount);
            statement.setDouble(5, paidAmount);
            statement.setString(6, paymentStatus);
            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Cannot create sale order.");
    }

    private void decreaseStock(Connection connection, String sql, CartRow item) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, item.getQuantity());
            statement.setInt(2, item.getProductId());
            statement.setInt(3, item.getQuantity());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new IllegalArgumentException("Sản phẩm \"" + item.getProductName() + "\" không đủ tồn kho.");
            }
        }
    }

    private void insertOrderItem(Connection connection, String sql, int orderId, CartRow item) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, item.getProductId());
            statement.setString(3, item.getProductName());
            statement.setInt(4, item.getQuantity());
            statement.setDouble(5, item.getUnitPrice());
            statement.setDouble(6, item.getImportPrice());
            statement.setDouble(7, item.getLineTotal());

            statement.executeUpdate();
        }
    }

    private double calculateTotalAmount(List<CartRow> cartItems) {
        double total = 0.0;

        for (CartRow item : cartItems) {
            total += item.getLineTotal();
        }

        return total;
    }
}