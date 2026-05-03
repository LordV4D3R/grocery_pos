package fourty3.grocerypos.repository;

import fourty3.grocerypos.model.DailyReportSummary;
import fourty3.grocerypos.model.TopSellingProductReport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportRepository {

    public DailyReportSummary getDailySummary(LocalDate reportDate) {
        return getSummaryByDateRange(reportDate, reportDate);
    }

    public DailyReportSummary getSummaryByDateRange(LocalDate fromDate, LocalDate toDate) {
        String saleSummarySql = """
                SELECT
                    COUNT(*) AS total_orders,
                    COALESCE(SUM(total_amount), 0) AS total_revenue,
                    COALESCE(SUM(COALESCE(initial_paid_amount, COALESCE(paid_amount, total_amount))), 0) AS total_collected_at_sale
                FROM sale_orders
                WHERE date(created_at) BETWEEN ? AND ?
                """;

        String profitSql = """
                SELECT
                    COALESCE(SUM((i.unit_price - i.import_price) * i.quantity), 0) AS total_profit
                FROM sale_order_items i
                JOIN sale_orders o ON o.id = i.order_id
                WHERE date(o.created_at) BETWEEN ? AND ?
                """;

        String debtCollectedSql = """
                SELECT
                    COALESCE(SUM(amount_paid), 0) AS total_debt_collected
                FROM debt_payments
                WHERE date(paid_at) BETWEEN ? AND ?
                """;

        String outstandingDebtSql = """
                WITH debt_state AS (
                    SELECT
                        o.id,
                        o.total_amount,
                        COALESCE(o.initial_paid_amount, COALESCE(o.paid_amount, 0)) AS initial_paid_amount,
                        COALESCE((
                            SELECT SUM(dp.amount_paid)
                            FROM debt_payments dp
                            WHERE dp.sale_order_id = o.id
                              AND date(dp.paid_at) <= ?
                        ), 0) AS collected_later
                    FROM sale_orders o
                    WHERE date(o.created_at) <= ?
                )
                SELECT
                    COALESCE(SUM(
                        CASE
                            WHEN total_amount - initial_paid_amount - collected_later > 0
                            THEN total_amount - initial_paid_amount - collected_later
                            ELSE 0
                        END
                    ), 0) AS total_outstanding_debt
                FROM debt_state
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            int totalOrders = 0;
            double totalRevenue = 0;
            double totalCollectedAtSale = 0;
            double totalProfit = 0;
            double totalDebtCollected = 0;
            double totalOutstandingDebt = 0;

            try (PreparedStatement statement = connection.prepareStatement(saleSummarySql)) {
                statement.setString(1, fromDate.toString());
                statement.setString(2, toDate.toString());

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        totalOrders = rs.getInt("total_orders");
                        totalRevenue = rs.getDouble("total_revenue");
                        totalCollectedAtSale = rs.getDouble("total_collected_at_sale");
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(profitSql)) {
                statement.setString(1, fromDate.toString());
                statement.setString(2, toDate.toString());

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        totalProfit = rs.getDouble("total_profit");
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(debtCollectedSql)) {
                statement.setString(1, fromDate.toString());
                statement.setString(2, toDate.toString());

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        totalDebtCollected = rs.getDouble("total_debt_collected");
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(outstandingDebtSql)) {
                statement.setString(1, toDate.toString());
                statement.setString(2, toDate.toString());

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        totalOutstandingDebt = rs.getDouble("total_outstanding_debt");
                    }
                }
            }

            double totalActualCollected = totalCollectedAtSale + totalDebtCollected;

            return new DailyReportSummary(
                    totalOrders,
                    totalRevenue,
                    totalCollectedAtSale,
                    totalDebtCollected,
                    totalActualCollected,
                    totalOutstandingDebt,
                    totalProfit
            );

        } catch (SQLException e) {
            throw new RuntimeException("Cannot load report summary", e);
        }
    }

    public List<TopSellingProductReport> getTopSellingProducts(LocalDate reportDate) {
        return getTopSellingProductsByDateRange(reportDate, reportDate);
    }

    public List<TopSellingProductReport> getTopSellingProductsByDateRange(LocalDate fromDate, LocalDate toDate) {
        String sql = """
                SELECT
                    i.product_id,
                    i.product_name,
                    SUM(i.quantity) AS total_quantity,
                    SUM(i.line_total) AS total_revenue
                FROM sale_order_items i
                JOIN sale_orders o ON o.id = i.order_id
                WHERE date(o.created_at) BETWEEN ? AND ?
                GROUP BY i.product_id, i.product_name
                ORDER BY total_quantity DESC, total_revenue DESC, i.product_name ASC
                LIMIT 10
                """;

        List<TopSellingProductReport> result = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, fromDate.toString());
            statement.setString(2, toDate.toString());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(new TopSellingProductReport(
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("total_quantity"),
                            rs.getDouble("total_revenue")
                    ));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot load top selling products", e);
        }

        return result;
    }
}