package fourty3.grocerypos.repository;

import fourty3.grocerypos.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    public List<Product> findAll() {
        String sql = """
                SELECT id, product_code, name, selling_price, import_price, stock_quantity, is_active
                FROM products
                ORDER BY is_active DESC, lower(name) ASC, id ASC
                """;

        List<Product> products = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                products.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot load products", e);
        }

        return products;
    }

    public List<Product> searchByName(String keyword) {
        String sql = """
                SELECT id, product_code, name, selling_price, import_price, stock_quantity, is_active
                FROM products
                WHERE lower(name) LIKE ?
                ORDER BY is_active DESC, lower(name) ASC, id ASC
                """;

        List<Product> products = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + keyword.toLowerCase().trim() + "%");

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot search products", e);
        }

        return products;
    }

    public void insert(Product product) {
        String insertSql = """
            INSERT INTO products(name, selling_price, import_price, stock_quantity)
            VALUES (?, ?, ?, ?)
            """;

        String updateCodeSql = """
            UPDATE products
            SET product_code = ?
            WHERE id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int generatedId;

                try (PreparedStatement statement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, product.getName());
                    statement.setDouble(2, product.getSellingPrice());
                    statement.setDouble(3, product.getImportPrice());
                    statement.setInt(4, product.getStockQuantity());

                    statement.executeUpdate();

                    try (ResultSet rs = statement.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("Cannot get generated product id.");
                        }
                        generatedId = rs.getInt(1);
                    }
                }

                String productCode = generateProductCode(generatedId);

                try (PreparedStatement statement = connection.prepareStatement(updateCodeSql)) {
                    statement.setString(1, productCode);
                    statement.setInt(2, generatedId);
                    statement.executeUpdate();
                }

                connection.commit();

                product.setId(generatedId);
                product.setProductCode(productCode);
                product.setActive(true);

            } catch (Exception e) {
                connection.rollback();

                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }

                throw new RuntimeException("Cannot insert product", e);

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot insert product", e);
        }
    }

    public void update(Product product) {
        String sql = """
                UPDATE products
                SET name = ?, selling_price = ?, import_price = ?, stock_quantity = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, product.getName());
            statement.setDouble(2, product.getSellingPrice());
            statement.setDouble(3, product.getImportPrice());
            statement.setInt(4, product.getStockQuantity());
            statement.setInt(5, product.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Cannot update product", e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Cannot delete product", e);
        }
    }

    public boolean existsByName(String name) {
        String sql = """
                SELECT 1
                FROM products
                WHERE lower(trim(name)) = lower(trim(?))
                LIMIT 1
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot check duplicate product name", e);
        }
    }

    public boolean existsByNameExcludingId(String name, int excludedId) {
        String sql = """
                SELECT 1
                FROM products
                WHERE lower(trim(name)) = lower(trim(?))
                  AND id <> ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setInt(2, excludedId);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot check duplicate product name", e);
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("product_code"),
                rs.getString("name"),
                rs.getDouble("selling_price"),
                rs.getDouble("import_price"),
                rs.getInt("stock_quantity"),
                rs.getInt("is_active") == 1
        );
    }

    public List<Product> findAllActive() {
        String sql = """
                SELECT id, product_code, name, selling_price, import_price, stock_quantity, is_active
                FROM products
                WHERE is_active = 1
                ORDER BY lower(name) ASC, id ASC
                """;

        List<Product> products = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                products.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot load active products", e);
        }

        return products;
    }

    public List<Product> searchActiveByName(String keyword) {
        String sql = """
                SELECT id, product_code, name, selling_price, import_price, stock_quantity, is_active
                FROM products
                WHERE is_active = 1
                  AND lower(name) LIKE ?
                ORDER BY lower(name) ASC, id ASC
                """;

        List<Product> products = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + keyword.toLowerCase().trim() + "%");

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot search active products", e);
        }

        return products;
    }

    public boolean hasUsageHistory(int productId) {
        String saleOrderItemSql = """
                SELECT 1
                FROM sale_order_items
                WHERE product_id = ?
                LIMIT 1
                """;

        String stockImportSql = """
                SELECT 1
                FROM stock_imports
                WHERE product_id = ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseManager.getConnection()) {

            try (PreparedStatement statement = connection.prepareStatement(saleOrderItemSql)) {
                statement.setInt(1, productId);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(stockImportSql)) {
                statement.setInt(1, productId);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }

            return false;

        } catch (SQLException e) {
            throw new RuntimeException("Cannot check product usage history", e);
        }
    }

    public void deactivateById(int id) {
        String sql = """
            UPDATE products
            SET is_active = 0
            WHERE id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Cannot deactivate product", e);
        }
    }

    public void reactivateById(int id) {
        String sql = """
            UPDATE products
            SET is_active = 1
            WHERE id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Cannot reactivate product", e);
        }
    }
    private String generateProductCode(int id) {
        return String.format("SP%03d", id);
    }
}
