package billing;
//by Prashant Rana(2300680140090),prashantrana422@gmail.com
import static billing.BillgenerationController.isValidPhone;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

public class CustomerController implements Initializable {

    @FXML
    private AnchorPane CustomerPane;
    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<Customer, Integer> customer_id_column;
    @FXML
    private TableColumn<Customer, String> customer_name_column;
    @FXML
    private TableColumn<Customer, String> phone_column;
    @FXML
    private TableColumn<Customer, String> date_column;
    @FXML
    private TextField customerNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private DatePicker datepicker;
    @FXML
    private Button updateRecordButton;
    @FXML
    private Button deleteRecordButton;

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    // Wrap your list in a FilteredList for filtering purposes.
    private FilteredList<Customer> filteredCustomerList;

    private Connection conn;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    @FXML
    private TextField Searchfield;
    @FXML
    private ChoiceBox<String> searchchoice; // Define as ChoiceBox<String>

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = DatabaseConnection.connect();

        // Initialize table columns
        customer_id_column.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customer_name_column.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        phone_column.setCellValueFactory(new PropertyValueFactory<>("phone"));
        date_column.setCellValueFactory(new PropertyValueFactory<>("dateOfVisit"));

        // Initialize search criteria for ChoiceBox (can also be done in SceneBuilder)
        searchchoice.setItems(FXCollections.observableArrayList("ID", "Name", "Date", "Phone"));
        searchchoice.setValue("Name"); // Set a default choice

        // Load data and set date format for DatePicker
        loadCustomerData();
        configureDatePicker();

        // Wrap customerList with FilteredList and set it to the table
        filteredCustomerList = new FilteredList<>(customerList, p -> true);
        customerTable.setItems(filteredCustomerList);

        // Add listener for the search text field (real-time filtering)
        Searchfield.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCustomerList();
        });
        
        // Also update filtering when the search criteria changes
        searchchoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filterCustomerList();
        });

        // Handle table row selection
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });

        // Button actions
        updateRecordButton.setOnAction(e -> updateCustomerRecord());
        deleteRecordButton.setOnAction(e -> deleteCustomerRecord());
    }

    // Method to filter the customer list based on search text and chosen criteria
    private void filterCustomerList() {
        String filterText = Searchfield.getText().toLowerCase().trim();
        String selectedCriteria = searchchoice.getValue();
        
        filteredCustomerList.setPredicate(customer -> {
            // If filter text is empty, display all customers.
            if (filterText.isEmpty()) {
                return true;
            }
            
            switch(selectedCriteria) {
                case "ID":
                    return String.valueOf(customer.getCustomerId()).contains(filterText);
                case "Name":
                    return customer.getCustomerName().toLowerCase().contains(filterText);
                case "Date":
                    return customer.getDateOfVisit().toLowerCase().contains(filterText);
                case "Phone":
                    return customer.getPhone().toLowerCase().contains(filterText);
                default:
                    return false;
            }
        });
    }

    private void configureDatePicker() {
        datepicker.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? DATE_FORMATTER.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, DATE_FORMATTER) : null;
            }
        });
    }

    private void loadCustomerData() {
        customerList.clear();
        String query = "SELECT * FROM customers";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                customerList.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("phone"),
                        rs.getString("date_of_visit")
                ));
            }
            // No need to reset table items as filteredCustomerList wraps customerList.

        } catch (SQLException ex) {
            showAlert("Error", "Failed to load customer data.", Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
    }

    private void populateForm(Customer customer) {
        customerNameField.setText(customer.getCustomerName());
        phoneField.setText(customer.getPhone());

        try {
            datepicker.setValue(LocalDate.parse(customer.getDateOfVisit(), DATE_FORMATTER));
        } catch (Exception e) {
            datepicker.setValue(null);
            showAlert("Error", "Invalid date format for customer record.", Alert.AlertType.ERROR);
        }
    }

    private void updateCustomerRecord() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            showAlert("Warning", "Please select a customer to update.", Alert.AlertType.WARNING);
            return;
        }

        String newName = customerNameField.getText().trim();
        String newPhone = phoneField.getText().trim();
        LocalDate newDate = datepicker.getValue();
        if (!isValidPhone(newPhone)) {
            showAlert("Validation Error", "Phone number must be exactly 10 digits starting from 6-9", Alert.AlertType.WARNING);
            return;
        }
        if (newName.isEmpty() || newPhone.isEmpty() || newDate == null) {
            showAlert("Warning", "All fields must be filled out.", Alert.AlertType.WARNING);
            return;
        }

        String query = "UPDATE customers SET customer_name = ?, phone = ?, date_of_visit = ? WHERE customer_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newName);
            stmt.setString(2, newPhone);
            stmt.setString(3, newDate.format(DATE_FORMATTER));
            stmt.setInt(4, selectedCustomer.getCustomerId());
            stmt.executeUpdate();

            showAlert("Success", "Customer record updated successfully!", Alert.AlertType.INFORMATION);
            loadCustomerData();

        } catch (SQLException ex) {
            showAlert("Error", "Failed to update customer record.", Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
    }

    private void deleteCustomerRecord() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            showAlert("Warning", "Please select a customer to delete.", Alert.AlertType.WARNING);
            return;
        }

        String query = "DELETE FROM customers WHERE customer_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, selectedCustomer.getCustomerId());
            stmt.executeUpdate();

            showAlert("Success", "Customer record deleted successfully!", Alert.AlertType.INFORMATION);
            loadCustomerData();

        } catch (SQLException ex) {
            showAlert("Error", "Failed to delete customer record.", Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
    }
    
    public boolean loadCustomerDetails(int customerId) {
        String query = "SELECT * FROM customers WHERE customer_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                populateForm(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("phone"),
                        rs.getString("date_of_visit")
                ));
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            showAlert("Error", "Failed to load customer details: " + ex.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return phone.matches("^[6-9]\\d{9}$"); // Ensures it starts with 6-9 and is 10 digits long
    }
}
