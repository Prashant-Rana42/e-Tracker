package billing;
//by Prashant Rana(2300680140090),prashantrana422@gmail.com
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private Pane dashpane;
    @FXML
    private Label dash_invoice;
    @FXML
    private Label dash_customer;
    @FXML
    private Label dash_product;
    
    @FXML
    private TableView<RecentActivity> recent_table;
    @FXML
    private TableColumn<RecentActivity, Integer> invoice_id;
    @FXML
    private TableColumn<RecentActivity, String> time;
    @FXML
    private TableColumn<RecentActivity, String> customer_name;
    @FXML
    private TableColumn<RecentActivity, Double> amount;
    @FXML
    private TableColumn<RecentActivity, String> payment_status;
    
    private Connection conn;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("Current user: " + CurrentUser.username);
        setupDashboardActions();
        conn = DatabaseConnection.connect();
        invoice_id.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        time.setText("Time"); 
        time.setCellValueFactory(new PropertyValueFactory<>("time"));
        customer_name.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        amount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        payment_status.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        LocalDate today = LocalDate.now();
        String formattedDate = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        loadDashboardData(formattedDate);
    }
    
    /**
     * Sets up the click actions for the dashboard side labels.
     */
    private void setupDashboardActions() {
//        dash_product.setOnMouseClicked(event -> loadStockPane());
//        dash_customer.setOnMouseClicked(event -> loadCustomerPane());
//        dash_invoice.setOnMouseClicked(event -> loadInvoicePane());
    }
    
    private void loadStockPane() {
        try {
            dashpane.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Stock.fxml"));
            Pane addStockPane = loader.load();
            dashpane.getChildren().setAll(addStockPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadCustomerPane() {
        try {
            dashpane.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer.fxml"));
            Pane addCustomerPane = loader.load();
            dashpane.getChildren().setAll(addCustomerPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadInvoicePane() {
        try {
            dashpane.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Invoice.fxml"));
            Pane addInvoicePane = loader.load();
            dashpane.getChildren().setAll(addInvoicePane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadDashboardData(String dateStr) {
        if (conn == null) {
    showAlert("Database Error", "No database connection. Ensure the user is logged in.");
    return;
}
        double totalInvoice = 0.0;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT SUM(amount) AS totalInvoice FROM InvoiceDetails WHERE date = '" + dateStr + "'")) {
            if (rs.next()) {
                totalInvoice = rs.getDouble("totalInvoice");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error fetching invoice data: " + e.getMessage());
        }
        dash_invoice.setText(String.format("%.2f", totalInvoice));
        
        int totalCustomers = 0;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) AS totalCustomers FROM Customers WHERE date_of_visit = '" + dateStr + "'")) {
            if (rs.next()) {
                totalCustomers = rs.getInt("totalCustomers");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error fetching customer data: " + e.getMessage());
        }
        dash_customer.setText(String.valueOf(totalCustomers));
        
        int productsBelowMin = 0;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) AS belowMinCount FROM Product WHERE quantity < min_stock")) {
            if (rs.next()) {
                productsBelowMin = rs.getInt("belowMinCount");
            }
        } catch (SQLException e) {
            productsBelowMin = 0;
        }
        dash_product.setText(String.valueOf(productsBelowMin));
        
        // Load Recent Invoice Activities directly from InvoiceDetails (joined with Customers for customer name)
        ObservableList<RecentActivity> activityList = FXCollections.observableArrayList();
        // Adjust the query to join with Customers to get customer_name
        String query = "SELECT i.invoice_id, i.time, c.customer_name, i.amount, i.payment_status " +
                       "FROM InvoiceDetails i " +
                       "JOIN Customers c ON i.customer_id = c.customer_id " +
                       "WHERE i.date = '" + dateStr + "' " +
                       "ORDER BY i.invoice_id DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int invId = rs.getInt("invoice_id");
                String invTime = rs.getString("time"); 
                String custName = rs.getString("customer_name");
                double amt = rs.getDouble("amount");
                String payStatus = rs.getString("payment_status");
                // Reuse the RecentActivity model by setting both activityId and invoiceId to invId
                RecentActivity activity = new RecentActivity(invId, invId, custName, invTime, amt, payStatus);
                activityList.add(activity);
            } 
        } catch (SQLException e) {
            showAlert("Database Error", "Error fetching recent activities: " + e.getMessage());
        }
        recent_table.setItems(activityList);
    }
 
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
