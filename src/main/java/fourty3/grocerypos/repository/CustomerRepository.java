package fourty3.grocerypos.repository;

import fourty3.grocerypos.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {

    public List<Customer> findAll() {
        String sql = """
                SELECT id, customer_code, name, phone, address, note, is_active
                FROM customers
                ORDER BY is_active DESC, lower(name) ASC, id ASC
                """;

        List<Customer> customers = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                customers.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot load customers", e);
        }

        return customers;
    }

    public List<Customer> search(String keyword) {
        String sql = """
                SELECT id, customer_code, name, phone, address, note, is_active
                FROM customers
                WHERE lower(name) LIKE ?
                   OR lower(phone) LIKE ?
                   OR lower(coalesce(customer_code, '')) LIKE ?
                ORDER BY is_active DESC, lower(name) ASC, id ASC
                """;

        List<Customer> customers = new ArrayList<>();
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        String likeValue = "%" + normalizedKeyword + "%";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, likeValue);
            statement.setString(2, likeValue);
            statement.setString(3, likeValue);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot search customers", e);
        }

        return customers;
    }

    public Customer findById(int id) {
        String sql = """
                SELECT id, customer_code, name, phone, address, note, is_active
                FROM customers
                WHERE id = ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot find customer by id", e);
        }

        return null;
    }

    public void insert(Customer customer) {
        String insertSql = """
                INSERT INTO customers(name, phone, address, note)
                VALUES (?, ?, ?, ?)
                """;

        String updateCodeSql = """
                UPDATE customers
                SET customer_code = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int generatedId;

                try (PreparedStatement statement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, customer.getName());
                    statement.setString(2, customer.getPhone());
                    statement.setString(3, customer.getAddress());
                    statement.setString(4, customer.getNote());
                    statement.executeUpdate();

                    try (ResultSet rs = statement.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("Cannot get generated customer id.");
                        }
                        generatedId = rs.getInt(1);
                    }
                }

                String customerCode = generateCustomerCode(generatedId);

                try (PreparedStatement statement = connection.prepareStatement(updateCodeSql)) {
                    statement.setString(1, customerCode);
                    statement.setInt(2, generatedId);
                    statement.executeUpdate();
                }

                connection.commit();

                customer.setId(generatedId);
                customer.setCustomerCode(customerCode);
                customer.setActive(true);

            } catch (Exception e) {
                connection.rollback();

                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }

                throw new RuntimeException("Cannot insert customer", e);

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot insert customer", e);
        }
    }

    public void update(Customer customer) {
        String sql = """
                UPDATE customers
                SET name = ?, phone = ?, address = ?, note = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getName());
            statement.setString(2, customer.getPhone());
            statement.setString(3, customer.getAddress());
            statement.setString(4, customer.getNote());
            statement.setInt(5, customer.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Cannot update customer", e);
        }
    }

    public boolean existsByPhone(String phone) {
        String sql = """
                SELECT 1
                FROM customers
                WHERE lower(trim(phone)) = lower(trim(?))
                LIMIT 1
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, phone);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot check duplicate customer phone", e);
        }
    }

    public boolean existsByPhoneExcludingId(String phone, int excludedId) {
        String sql = """
                SELECT 1
                FROM customers
                WHERE lower(trim(phone)) = lower(trim(?))
                  AND id <> ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, phone);
            statement.setInt(2, excludedId);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot check duplicate customer phone", e);
        }
    }

    public void deactivateById(int id) {
        String sql = """
                UPDATE customers
                SET is_active = 0
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Cannot deactivate customer", e);
        }
    }

    public void reactivateById(int id) {
        String sql = """
                UPDATE customers
                SET is_active = 1
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Cannot reactivate customer", e);
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("id"),
                rs.getString("customer_code"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("note"),
                rs.getInt("is_active") == 1
        );
    }

    private String generateCustomerCode(int id) {
        return String.format("KH%03d", id);
    }
}