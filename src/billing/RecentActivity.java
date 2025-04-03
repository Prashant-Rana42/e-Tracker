package billing;
//by Prashant Rana(2300680140090),prashantrana422@gmail.com
public class RecentActivity {
    private int activityId;
    private int invoiceId;
    private String customerName;
    private String createdAt; // Change the meaning of this to store time instead of date
    private double amount;
    private String paymentStatus;

    public RecentActivity(int activityId, int invoiceId, String customerName, String time, double amount, String paymentStatus) {
        this.activityId = activityId;
        this.invoiceId = invoiceId;
        this.customerName = customerName;
        this.createdAt = time; // Now this holds the time instead of the date
        this.amount = amount;
        this.paymentStatus = paymentStatus;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getTime() { // Renamed method from getCreatedAt() to getTime()
        return createdAt;
    }

    public void setTime(String time) { // Renamed method from setCreatedAt() to setTime()
        this.createdAt = time;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
