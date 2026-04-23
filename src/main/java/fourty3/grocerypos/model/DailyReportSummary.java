package fourty3.grocerypos.model;

public class DailyReportSummary {

    private int totalOrders;
    private double totalRevenue;
    private double totalProfit;

    public DailyReportSummary() {
    }

    public DailyReportSummary(int totalOrders, double totalRevenue, double totalProfit) {
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.totalProfit = totalProfit;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(double totalProfit) {
        this.totalProfit = totalProfit;
    }
}