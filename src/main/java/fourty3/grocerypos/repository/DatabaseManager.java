package fourty3.grocerypos.repository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_FOLDER = "data";
    private static final String DB_FILE = "storepos.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FOLDER + "/" + DB_FILE;

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        ensureDatabaseFolderExists();

        Connection connection = DriverManager.getConnection(JDBC_URL);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        return connection;
    }

    public static void initializeDatabase() {
        ensureDatabaseFolderExists();

        String createProductsTable = """
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    product_code TEXT UNIQUE,
                    name TEXT NOT NULL,
                    selling_price REAL NOT NULL,
                    import_price REAL NOT NULL,
                    stock_quantity INTEGER NOT NULL,
                    is_active INTEGER NOT NULL DEFAULT 1
                );
                """;

        String createSaleOrdersTable = """
                CREATE TABLE IF NOT EXISTS sale_orders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    created_at TEXT NOT NULL,
                    customer_id INTEGER,
                    total_amount REAL NOT NULL,
                    initial_paid_amount REAL,
                    paid_amount REAL,
                    payment_status TEXT
                );
                """;

        String createSaleOrderItemsTable = """
                CREATE TABLE IF NOT EXISTS sale_order_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_id INTEGER NOT NULL,
                    product_id INTEGER NOT NULL,
                    product_name TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    unit_price REAL NOT NULL,
                    import_price REAL NOT NULL,
                    line_total REAL NOT NULL,
                    FOREIGN KEY (order_id) REFERENCES sale_orders(id)
                );
                """;

        String createStockImportsTable = """
                CREATE TABLE IF NOT EXISTS stock_imports (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    product_id INTEGER NOT NULL,
                    product_name TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    import_price REAL NOT NULL,
                    imported_at TEXT NOT NULL,
                    FOREIGN KEY (product_id) REFERENCES products(id)
                );
                """;

        String createCustomersTable = """
                CREATE TABLE IF NOT EXISTS customers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_code TEXT UNIQUE,
                    name TEXT NOT NULL,
                    phone TEXT NOT NULL,
                    address TEXT,
                    note TEXT,
                    is_active INTEGER NOT NULL DEFAULT 1
                );
                """;

        String createDebtPaymentsTable = """
                CREATE TABLE IF NOT EXISTS debt_payments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_order_id INTEGER NOT NULL,
                    customer_id INTEGER NOT NULL,
                    amount_paid REAL NOT NULL,
                    paid_at TEXT NOT NULL,
                    note TEXT,
                    FOREIGN KEY (sale_order_id) REFERENCES sale_orders(id),
                    FOREIGN KEY (customer_id) REFERENCES customers(id)
                );
                """;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(createProductsTable);
            statement.execute(createSaleOrdersTable);
            statement.execute(createSaleOrderItemsTable);
            statement.execute(createStockImportsTable);
            statement.execute(createCustomersTable);
            statement.execute(createDebtPaymentsTable);

            ensureProductActiveColumnExists(connection);
            ensureCustomerCodeColumnExists(connection);
            ensureCustomerActiveColumnExists(connection);
            ensureCustomerCodeUniqueIndex(connection);
            ensureProductCodeColumnExists(connection);
            backfillProductCodes(connection);
            ensureProductCodeUniqueIndex(connection);

            ensureSaleOrderCustomerIdColumnExists(connection);
            ensureSaleOrderPaidAmountColumnExists(connection);
            backfillSaleOrderPaidAmount(connection);
            ensureSaleOrderInitialPaidAmountColumnExists(connection);
            backfillSaleOrderInitialPaidAmount(connection);
            ensureSaleOrderPaymentStatusColumnExists(connection);
            backfillSaleOrderPaymentStatus(connection);

        } catch (SQLException e) {
            throw new RuntimeException("Cannot initialize database", e);
        }
    }

    private static void ensureDatabaseFolderExists() {
        Path folderPath = Paths.get(DB_FOLDER);

        try {
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot create database folder: " + DB_FOLDER, e);
        }
    }

    public static String getDatabasePath() {
        return Paths.get(DB_FOLDER, DB_FILE).toAbsolutePath().toString();
    }

    private static void ensureProductActiveColumnExists(Connection connection) {
        String checkSql = "PRAGMA table_info(products)";
        boolean hasIsActiveColumn = false;

        try (Statement statement = connection.createStatement();
             var rs = statement.executeQuery(checkSql)) {

            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("is_active".equalsIgnoreCase(columnName)) {
                    hasIsActiveColumn = true;
                    break;
                }
            }

            if (!hasIsActiveColumn) {
                statement.execute("ALTER TABLE products ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot migrate products.is_active column", e);
        }
    }

    private static void ensureProductCodeColumnExists(Connection connection) {
        String checkSql = "PRAGMA table_info(products)";
        boolean hasProductCodeColumn = false;

        try (Statement statement = connection.createStatement();
             var rs = statement.executeQuery(checkSql)) {

            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("product_code".equalsIgnoreCase(columnName)) {
                    hasProductCodeColumn = true;
                    break;
                }
            }

            if (!hasProductCodeColumn) {
                statement.execute("ALTER TABLE products ADD COLUMN product_code TEXT");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot migrate products.product_code column", e);
        }
    }

    private static void backfillProductCodes(Connection connection) {
        String sql = """
                UPDATE products
                SET product_code = 'SP' || printf('%03d', id)
                WHERE product_code IS NULL OR trim(product_code) = ''
                """;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot backfill products.product_code", e);
        }
    }

    private static void ensureProductCodeUniqueIndex(Connection connection) {
        String sql = """
                CREATE UNIQUE INDEX IF NOT EXISTS idx_products_product_code
                ON products(product_code)
                """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot create unique index for products.product_code", e);
        }
    }

    private static void ensureCustomerCodeColumnExists(Connection connection) {
        String checkSql = "PRAGMA table_info(customers)";
        boolean hasCustomerCodeColumn = false;

        try (Statement statement = connection.createStatement();
             var rs = statement.executeQuery(checkSql)) {

            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("customer_code".equalsIgnoreCase(columnName)) {
                    hasCustomerCodeColumn = true;
                    break;
                }
            }

            if (!hasCustomerCodeColumn) {
                statement.execute("ALTER TABLE customers ADD COLUMN customer_code TEXT");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot migrate customers.customer_code column", e);
        }
    }

    private static void ensureCustomerActiveColumnExists(Connection connection) {
        String checkSql = "PRAGMA table_info(customers)";
        boolean hasIsActiveColumn = false;

        try (Statement statement = connection.createStatement();
             var rs = statement.executeQuery(checkSql)) {

            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("is_active".equalsIgnoreCase(columnName)) {
                    hasIsActiveColumn = true;
                    break;
                }
            }

            if (!hasIsActiveColumn) {
                statement.execute("ALTER TABLE customers ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot migrate customers.is_active column", e);
        }
    }

    private static void ensureCustomerCodeUniqueIndex(Connection connection) {
        String sql = """
                CREATE UNIQUE INDEX IF NOT EXISTS idx_customers_customer_code
                ON customers(customer_code)
                """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot create unique index for customers.customer_code", e);
        }
    }

    private static void ensureSaleOrderCustomerIdColumnExists(Connection connection) {
        String checkSql = "PRAGMA table_info(sale_orders)";
        boolean hasCustomerIdColumn = false;

        try (Statement statement = connection.createStatement();
             var rs = statement.executeQuery(checkSql)) {

            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("customer_id".equalsIgnoreCase(columnName)) {
                    hasCustomerIdColumn = true;
                    break;
                }
            }

            if (!hasCustomerIdColumn) {
                statement.execute("ALTER TABLE sale_orders ADD COLUMN customer_id INTEGER");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot migrate sale_orders.customer_id column", e);
        }
    }

    private static void ensureSaleOrderPaidAmountColumnExists(Connection connection) {
        String checkSql = "PRAGMA table_info(sale_orders)";
        boolean hasPaidAmountColumn = false;

        try (Statement statement = connection.createStatement();
             var rs = statement.executeQuery(checkSql)) {

            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("paid_amount".equalsIgnoreCase(columnName)) {
                    hasPaidAmountColumn = true;
                    break;
                }
            }

            if (!hasPaidAmountColumn) {
                statement.execute("ALTER TABLE sale_orders ADD COLUMN paid_amount REAL");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot migrate sale_orders.paid_amount column", e);
        }
    }

    private static void backfillSaleOrderPaidAmount(Connection connection) {
        String sql = """
                UPDATE sale_orders
                SET paid_amount = total_amount
                WHERE paid_amount IS NULL
                """;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot backfill sale_orders.paid_amount", e);
        }
    }

    private static void ensureSaleOrderPaymentStatusColumnExists(Connection connection) {
        String checkSql = "PRAGMA table_info(sale_orders)";
        boolean hasPaymentStatusColumn = false;

        try (Statement statement = connection.createStatement();
             var rs = statement.executeQuery(checkSql)) {

            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("payment_status".equalsIgnoreCase(columnName)) {
                    hasPaymentStatusColumn = true;
                    break;
                }
            }

            if (!hasPaymentStatusColumn) {
                statement.execute("ALTER TABLE sale_orders ADD COLUMN payment_status TEXT");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot migrate sale_orders.payment_status column", e);
        }
    }

    private static void backfillSaleOrderPaymentStatus(Connection connection) {
        String sql = """
                UPDATE sale_orders
                SET payment_status = 'PAID'
                WHERE payment_status IS NULL OR trim(payment_status) = ''
                """;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot backfill sale_orders.payment_status", e);
        }
    }

    private static void ensureSaleOrderInitialPaidAmountColumnExists(Connection connection) {
        String checkSql = "PRAGMA table_info(sale_orders)";
        boolean hasInitialPaidAmountColumn = false;

        try (Statement statement = connection.createStatement();
             var rs = statement.executeQuery(checkSql)) {

            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("initial_paid_amount".equalsIgnoreCase(columnName)) {
                    hasInitialPaidAmountColumn = true;
                    break;
                }
            }

            if (!hasInitialPaidAmountColumn) {
                statement.execute("ALTER TABLE sale_orders ADD COLUMN initial_paid_amount REAL");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot migrate sale_orders.initial_paid_amount column", e);
        }
    }

    private static void backfillSaleOrderInitialPaidAmount(Connection connection) {
        String sql = """
                UPDATE sale_orders
                SET initial_paid_amount = COALESCE(paid_amount, total_amount)
                WHERE initial_paid_amount IS NULL
                """;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot backfill sale_orders.initial_paid_amount", e);
        }
    }

}