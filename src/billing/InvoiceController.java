package billing;
//by Prashant Rana(2300680140090),prashantrana422@gmail.com
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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class InvoiceController implements Initializable {

    @FXML
    private AnchorPane InvoicePane;
    @FXML
    private TableView<Invoice> invoicetable;
    @FXML
    private TableColumn<Invoice, Integer> invoiceID;
    @FXML
    private TableColumn<Invoice, String> time;
    @FXML
    private TableColumn<Invoice, String> date;
    @FXML
    private TableColumn<Invoice, Double> amount;
    @FXML
    private TableColumn<Invoice, String> status;
    @FXML
    private DatePicker date_picker;
    @FXML
    private TextField amount_txt;
    @FXML
    private ToggleButton statustogglebtn;
    @FXML
    private Button updatebtn;
    @FXML
    private Button deletebtn;
    @FXML
    private Button customerdetails;
    @FXML
    private TextField customername_txt;

    // Search components
    @FXML
    private TextField Searchfield;
    @FXML
    private ChoiceBox<String> searchchoice; // Change to ChoiceBox<String>

    private Connection conn;
    private ObservableList<Invoice> invoiceList = FXCollections.observableArrayList();
    // Wrap invoiceList with a FilteredList for search filtering.
    private FilteredList<Invoice> filteredInvoiceList;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = DatabaseConnection.connect();

        // Set up TableView columns using Invoice properties
        invoiceID.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        time.setCellValueFactory(new PropertyValueFactory<>("time"));
        date.setCellValueFactory(new PropertyValueFactory<>("date"));
        amount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        status.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        loadInvoiceData();

        // Initialize ChoiceBox search criteria (can also be set in SceneBuilder)
        searchchoice.setItems(FXCollections.observableArrayList("ID", "Time", "Date", "Amount", "Status"));
        searchchoice.setValue("Status"); // Default selection

        // Wrap the invoiceList in a FilteredList for dynamic filtering
        filteredInvoiceList = new FilteredList<>(invoiceList, p -> true);
        invoicetable.setItems(filteredInvoiceList);

        // Add listeners for real-time filtering:
        // When the search field text changes
        Searchfield.textProperty().addListener((observable, oldValue, newValue) -> {
            filterInvoiceList();
        });
        // When the choice box selection changes
        searchchoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filterInvoiceList();
        });

        invoicetable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                populateFields(newValue);
            }
        });

        setupToggleButton();
        updatebtn.setOnAction(e -> updateInvoice());
        deletebtn.setOnAction(e -> deleteInvoice());
        customerdetails.setOnAction(e -> viewCustomerDetails());

        invoicetable.setRowFactory(tv -> new TableRow<Invoice>() {
            @Override
            protected void updateItem(Invoice item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if ("Unpaid".equalsIgnoreCase(item.getPaymentStatus())) {
                    setStyle("-fx-background-color: #FFB3B3;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    // Method to update the filteredInvoiceList predicate based on search field and criteria
    private void filterInvoiceList() {
        String filterText = Searchfield.getText().toLowerCase().trim();
        String selectedCriteria = searchchoice.getValue();

        filteredInvoiceList.setPredicate(invoice -> {
            // If no search text, display all invoices
            if (filterText.isEmpty()) {
                return true;
            }

            switch (selectedCriteria) {
                case "ID":
                    return String.valueOf(invoice.getInvoiceId()).toLowerCase().contains(filterText);
                case "Time":
                    return invoice.getTime().toLowerCase().contains(filterText);
                case "Date":
                    return invoice.getDate().toLowerCase().contains(filterText);
                case "Amount":
                    return String.valueOf(invoice.getAmount()).toLowerCase().contains(filterText);
                case "Status":
                    return invoice.getPaymentStatus().toLowerCase().contains(filterText);
                default:
                    return true;
            }
        });
    }

    private void loadInvoiceData() {
        invoiceList.clear();
        // Modified query: retrieve invoice_id, customer_id, date, time, amount, and payment_status
        String query = "SELECT invoice_id, customer_id, date, time, amount, payment_status FROM InvoiceDetails";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                invoiceList.add(new Invoice(
                    rs.getInt("invoice_id"),
                    rs.getInt("customer_id"),
                    rs.getString("time"),
                    rs.getString("date"),
                    rs.getDouble("amount"),
                    rs.getString("payment_status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // No need to set items here as filteredInvoiceList is already linked.
    }

    private void populateFields(Invoice selectedInvoice) {
        try {
            customername_txt.setText(String.valueOf(selectedInvoice.getCustomerId()));
            date_picker.setValue(LocalDate.parse(selectedInvoice.getDate(), DATE_FORMATTER));
            amount_txt.setText(String.valueOf(selectedInvoice.getAmount()));
            if ("Paid".equalsIgnoreCase(selectedInvoice.getPaymentStatus())) {
                statustogglebtn.setSelected(true);
                statustogglebtn.setText("Paid");
                statustogglebtn.setStyle("-fx-base: green;");
            } else {
                statustogglebtn.setSelected(false);
                statustogglebtn.setText("Unpaid");
                statustogglebtn.setStyle("-fx-base: red;");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupToggleButton() {
        statustogglebtn.setText("Unpaid");
        statustogglebtn.setStyle("-fx-base: red;");
        statustogglebtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                statustogglebtn.setText("Paid");
                statustogglebtn.setStyle("-fx-base: green;");
            } else {
                statustogglebtn.setText("Unpaid");
                statustogglebtn.setStyle("-fx-base: red;");
            }
        });
    }

    private void viewCustomerDetails() {
        Invoice selectedInvoice = invoicetable.getSelectionModel().getSelectedItem();
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.ERROR, "No Invoice Selected", "Please select an invoice first.");
            return;
        }
        int customerId = selectedInvoice.getCustomerId(); // Assumes Invoice has a getCustomerId() method
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer.fxml"));
            Parent root = loader.load();
            CustomerController customerController = loader.getController();
            if (!customerController.loadCustomerDetails(customerId)) {
                showAlert(Alert.AlertType.ERROR, "Customer Not Found", "No customer details found for the selected invoice.");
                return;
            }
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Customer Details");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Customer Details module.");
        }
    }

    private void updateInvoice() {
        if (invoicetable.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.ERROR, "No Invoice Selected", "Please select an invoice to update.");
            return;
        }
        Invoice selectedInvoice = invoicetable.getSelectionModel().getSelectedItem();
        // Since there's no editable time field, we keep the original time.
        String query = "UPDATE InvoiceDetails SET time = ?, date = ?, amount = ?, payment_status = ? WHERE invoice_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, selectedInvoice.getTime());
            ps.setString(2, date_picker.getValue().format(DATE_FORMATTER));
            ps.setDouble(3, Double.parseDouble(amount_txt.getText()));
            ps.setString(4, statustogglebtn.isSelected() ? "Paid" : "Unpaid");
            ps.setInt(5, selectedInvoice.getInvoiceId());
            ps.executeUpdate();
            loadInvoiceData();
            showAlert(Alert.AlertType.INFORMATION, "Invoice Updated", "Invoice details updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update invoice.");
        }
    }

    private void deleteInvoice() {
        if (invoicetable.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.ERROR, "No Invoice Selected", "Please select an invoice to delete.");
            return;
        }
        Invoice selectedInvoice = invoicetable.getSelectionModel().getSelectedItem();
        String query = "DELETE FROM InvoiceDetails WHERE invoice_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, selectedInvoice.getInvoiceId());
            ps.executeUpdate();
            loadInvoiceData();
            showAlert(Alert.AlertType.INFORMATION, "Invoice Deleted", "Invoice deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete invoice.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
