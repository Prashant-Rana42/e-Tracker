package billing;
//by Prashant Rana(2300680140090),prashantrana422@gmail.com
public class Customer {
    private int customerId;
    private String customerName;
    private String phone;
    private String dateOfVisit;

    // Constructor
    public Customer(int customerId, String customerName, String phone, String dateOfVisit) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.dateOfVisit = dateOfVisit;
    }

    // Getters and Setters
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDateOfVisit() {
        return dateOfVisit;
    }

    public void setDateOfVisit(String dateOfVisit) {
        this.dateOfVisit = dateOfVisit;
    }
}
