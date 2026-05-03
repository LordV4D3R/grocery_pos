package fourty3.grocerypos.model;

public class DailyReportSummary {

    private int totalOrders;
    private double totalRevenue;
    private double totalCollectedAtSale;
    private double totalDebtCollected;
    private double totalActualCollected;
    private double totalOutstandingDebt;
    private double totalProfit;

    public DailyReportSummary() {
    }

    public DailyReportSummary(int totalOrders, double totalRevenue, double totalCollectedAtSale,
                              double totalDebtCollected, double totalActualCollected,
                              double totalOutstandingDebt, double totalProfit) {
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.totalCollectedAtSale = totalCollectedAtSale;
        this.totalDebtCollected = totalDebtCollected;
        this.totalActualCollected = totalActualCollected;
        this.totalOutstandingDebt = totalOutstandingDebt;
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

    public double getTotalCollectedAtSale() {
        return totalCollectedAtSale;
    }

    public void setTotalCollectedAtSale(double totalCollectedAtSale) {
        this.totalCollectedAtSale = totalCollectedAtSale;
    }

    public double getTotalDebtCollected() {
        return totalDebtCollected;
    }

    public void setTotalDebtCollected(double totalDebtCollected) {
        this.totalDebtCollected = totalDebtCollected;
    }

    public double getTotalActualCollected() {
        return totalActualCollected;
    }

    public void setTotalActualCollected(double totalActualCollected) {
        this.totalActualCollected = totalActualCollected;
    }

    public double getTotalOutstandingDebt() {
        return totalOutstandingDebt;
    }

    public void setTotalOutstandingDebt(double totalOutstandingDebt) {
        this.totalOutstandingDebt = totalOutstandingDebt;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(double totalProfit) {
        this.totalProfit = totalProfit;
    }
}