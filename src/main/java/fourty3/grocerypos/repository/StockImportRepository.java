package fourty3.grocerypos.repository;

import fourty3.grocerypos.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StockImportRepository {

    public void importStock(Product product, int importQuantity, double newImportPrice) {
        String updateProductSql = """
                UPDATE products
                SET stock_quantity = stock_quantity + ?,
                    import_price = ?
                WHERE id = ?
                """;

        String insertStockImportSql = """
                INSERT INTO stock_imports(
                    product_id,
                    product_name,
                    quantity,
                    import_price,
                    imported_at
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement updateStatement = connection.prepareStatement(updateProductSql)) {
                    updateStatement.setInt(1, importQuantity);
                    updateStatement.setDouble(2, newImportPrice);
                    updateStatement.setInt(3, product.getId());
                    updateStatement.executeUpdate();
                }

                try (PreparedStatement insertStatement = connection.prepareStatement(insertStockImportSql)) {
                    insertStatement.setInt(1, product.getId());
                    insertStatement.setString(2, product.getName());
                    insertStatement.setInt(3, importQuantity);
                    insertStatement.setDouble(4, newImportPrice);
                    insertStatement.setString(5, LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    insertStatement.executeUpdate();
                }

                connection.commit();

            } catch (Exception e) {
                connection.rollback();

                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }

                throw new RuntimeException("Cannot import stock", e);

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot import stock", e);
        }
    }
}
