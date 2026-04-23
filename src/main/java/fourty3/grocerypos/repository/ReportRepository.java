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
        String sql = """
                SELECT
                    COUNT(DISTINCT o.id) AS total_orders,
                    COALESCE(SUM(i.line_total), 0) AS total_revenue,
                    COALESCE(SUM((i.unit_price - i.import_price) * i.quantity), 0) AS total_profit
                FROM sale_orders o
                LEFT JOIN sale_order_items i ON o.id = i.order_id
                WHERE date(o.created_at) BETWEEN ? AND ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, fromDate.toString());
            statement.setString(2, toDate.toString());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new DailyReportSummary(
                            rs.getInt("total_orders"),
                            rs.getDouble("total_revenue"),
                            rs.getDouble("total_profit")
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot load report summary", e);
        }

        return new DailyReportSummary(0, 0, 0);
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