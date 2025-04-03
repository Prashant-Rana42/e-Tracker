package billing;
//by Prashant Rana(2300680140090),prashantrana422@gmail.com
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Invoice {
    private SimpleIntegerProperty invoiceId;
    private SimpleIntegerProperty customerId;
    private SimpleStringProperty date;
    private SimpleStringProperty time;
    private SimpleDoubleProperty amount;
    private SimpleStringProperty paymentStatus;

    // Constructor with time parameter
    public Invoice(int invoiceId, int customerId, String time, String date, double amount, String paymentStatus) {
        this.invoiceId = new SimpleIntegerProperty(invoiceId);
        this.customerId = new SimpleIntegerProperty(customerId);
        this.time = new SimpleStringProperty(time);
        this.date = new SimpleStringProperty(date);
        this.amount = new SimpleDoubleProperty(amount);
        this.paymentStatus = new SimpleStringProperty(paymentStatus);
    }

    // Getters and setters
    public int getInvoiceId() {
        return invoiceId.get();
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId.set(invoiceId);
    }

    public SimpleIntegerProperty invoiceIdProperty() {
        return invoiceId;
    }

    public int getCustomerId() {
        return customerId.get();
    }

    public void setCustomerId(int customerId) {
        this.customerId.set(customerId);
    }

    public SimpleIntegerProperty customerIdProperty() {
        return customerId;
    }

    public String getDate() {
        return date.get();
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public SimpleStringProperty dateProperty() {
        return date;
    }

    public String getTime() {
        return time.get();
    }

    public void setTime(String time) {
        this.time.set(time);
    }

    public SimpleStringProperty timeProperty() {
        return time;
    }

    public double getAmount() {
        return amount.get();
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    public SimpleDoubleProperty amountProperty() {
        return amount;
    }

    public String getPaymentStatus() {
        return paymentStatus.get();
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus.set(paymentStatus);
    }

    public SimpleStringProperty paymentStatusProperty() {
        return paymentStatus;
    }
}
