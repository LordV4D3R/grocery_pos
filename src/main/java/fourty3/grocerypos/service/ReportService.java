package fourty3.grocerypos.service;

import fourty3.grocerypos.model.DailyReportSummary;
import fourty3.grocerypos.model.TopSellingProductReport;
import fourty3.grocerypos.repository.ReportRepository;

import java.time.LocalDate;
import java.util.List;

public class ReportService {

    private final ReportRepository reportRepository = new ReportRepository();

    public DailyReportSummary getDailySummary(LocalDate reportDate) {
        validateReportRange(reportDate, reportDate);
        return reportRepository.getDailySummary(reportDate);
    }

    public DailyReportSummary getSummaryByDateRange(LocalDate fromDate, LocalDate toDate) {
        validateReportRange(fromDate, toDate);
        return reportRepository.getSummaryByDateRange(fromDate, toDate);
    }

    public List<TopSellingProductReport> getTopSellingProducts(LocalDate reportDate) {
        validateReportRange(reportDate, reportDate);
        return reportRepository.getTopSellingProducts(reportDate);
    }

    public List<TopSellingProductReport> getTopSellingProductsByDateRange(LocalDate fromDate, LocalDate toDate) {
        validateReportRange(fromDate, toDate);
        return reportRepository.getTopSellingProductsByDateRange(fromDate, toDate);
    }

    private void validateReportRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null) {
            throw new IllegalArgumentException("Vui lòng chọn từ ngày.");
        }

        if (toDate == null) {
            throw new IllegalArgumentException("Vui lòng chọn đến ngày.");
        }

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Từ ngày không được lớn hơn đến ngày.");
        }
    }
}