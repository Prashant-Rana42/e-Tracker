package billing;
// by Prashant Rana(2300680140090), prashantrana422@gmail.com
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class BillgenerationController {

    @FXML private TableView<Product> boughtitemlist;
    @FXML private TableColumn<Product, String> productnameB;
    @FXML private TableColumn<Product, Integer> productQuantityB;
    @FXML private TableColumn<Product, Double> totalPriceB;

    @FXML private TableView<Product> product_list;
    @FXML private TableColumn<Product, String> productnameA;
    @FXML private TableColumn<Product, Double> productPriceA;

    @FXML private Label totalamount_label;
    @FXML private TextField customer_name_txt;
    @FXML private TextField customer_phone_txt;
    @FXML private ToggleButton statustoggle_paid_unpaid;
    @FXML private TextArea recipt_textarea;

    private double totalAmount = 0.0; // subtotal (without GST)
    
    @FXML private AnchorPane billpane;
    @FXML private Button Printbtn;    // This is our save button
    @FXML private Button clear_btn;
    @FXML private Button printpdf;
    @FXML private TextField Searchfield;
    
    // Buttons for modifying items in bought item list
    @FXML private Button plusone;
    @FXML private Button minusone;
    @FXML private Button cutproduct;
    
    // Flag to track if current bought item list has been saved.
    private boolean dataSaved = false;

    public void initialize() {
        // Bind Product List TableView columns
        productnameA.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productPriceA.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        // Capture current date and time (for later use)
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        productnameB.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productQuantityB.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalPriceB.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            double total = product.getPrice() * product.getQuantity();
            return new SimpleDoubleProperty(total).asObject();
        });

        // Allow editing of quantity column and update totals accordingly
        boughtitemlist.setEditable(true);
        productQuantityB.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        productQuantityB.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            int newQuantity = event.getNewValue();
            int availableStock = getAvailableStock(product.getProductId());
            if(newQuantity > availableStock) {
                showAlert("Low Stock", "Cannot set quantity to " + newQuantity + ". Only " + availableStock + " items available in stock.");
                boughtitemlist.refresh();
            } else {
                product.setQuantity(newQuantity);
                boughtitemlist.refresh();
                updateTotalAmount();
            }
        });

        loadProductData();
        // --- Begin Search Functionality Addition ---
        ObservableList<Product> masterList = product_list.getItems();
        FilteredList<Product> filteredData = new FilteredList<>(masterList, p -> true);
        Searchfield.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(product -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return product.getProductName().toLowerCase().contains(lowerCaseFilter);
            });
        });
        SortedList<Product> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(product_list.comparatorProperty());
        product_list.setItems(sortedData);
        // --- End Search Functionality Addition ---

        setupToggleButton();
        product_list.setOnMouseClicked(this::handleProductClick);
        
        // Listen for changes in the bought item list to mark unsaved changes.
        boughtitemlist.getItems().addListener((ListChangeListener<Product>) change -> {
            dataSaved = false;
            if (!boughtitemlist.getItems().isEmpty()) {
                Printbtn.setDisable(false);
            }
        });
        
        // Register button event handlers for modifying the bought item list.
        plusone.setOnAction(this::handlePlusOne);
        minusone.setOnAction(this::handleMinusOne);
        cutproduct.setOnAction(this::handleCutProduct);
    }

    private void loadProductData() {
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Product")) {

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                double price = rs.getDouble("price");
                double gst = rs.getDouble("gst");

                product_list.getItems().add(new Product(productId, productName, price, gst));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading products: " + e.getMessage());
        }
    }

    private void setupToggleButton() {
        statustoggle_paid_unpaid.setText("Unpaid");
        statustoggle_paid_unpaid.setStyle("-fx-base: red;");
        statustoggle_paid_unpaid.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                statustoggle_paid_unpaid.setText("Paid");
                statustoggle_paid_unpaid.setStyle("-fx-base: green;");
            } else {
                statustoggle_paid_unpaid.setText("Unpaid");
                statustoggle_paid_unpaid.setStyle("-fx-base: red;");
            }
        });
    }

    private void handleProductClick(MouseEvent event) {
        if (event.getClickCount() == 1) {
            Product selectedProduct = product_list.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                addToBill(selectedProduct);
            }
        }
    }

    // Modified addToBill: set focus on the added or updated product.
    private void addToBill(Product selectedProduct) {
        int currentQuantity = 0;
        Product existing = null;
        for (Product product : boughtitemlist.getItems()) {
            if (product.getProductId() == selectedProduct.getProductId()) {
                currentQuantity = product.getQuantity();
                existing = product;
                break;
            }
        }
        int desiredQuantity = currentQuantity + 1;
        int availableStock = getAvailableStock(selectedProduct.getProductId());
        if (desiredQuantity > availableStock) {
            showAlert("Low Stock", "Cannot add more of " + selectedProduct.getProductName() + ". Only " + availableStock + " in stock.");
            return;
        }
        if (existing != null) {
            existing.setQuantity(desiredQuantity);
            boughtitemlist.refresh();
            updateTotalAmount();
            // Set focus to the updated product.
            boughtitemlist.getSelectionModel().select(existing);
            boughtitemlist.requestFocus();
        } else {
            Product newProduct = new Product(
                selectedProduct.getProductId(),
                selectedProduct.getProductName(),
                selectedProduct.getPrice(),
                selectedProduct.getGst()
            );
            newProduct.setQuantity(1);
            boughtitemlist.getItems().add(newProduct);
            updateTotalAmount();
            // Set focus to the newly added product.
            boughtitemlist.getSelectionModel().select(newProduct);
            boughtitemlist.requestFocus();
        }
    }
    
    @FXML
    private void clearButtonAction(ActionEvent event) {
        boughtitemlist.getItems().clear();
        recipt_textarea.clear();
        totalAmount = 0.0;
        totalamount_label.setText(String.format("%.2f", totalAmount));
    }

    // Recalculate totals (subtotal and GST)
    private void updateTotalAmount() {
        totalAmount = 0.0;
        double totalGST = 0.0;
        for (Product product : boughtitemlist.getItems()) {
            double productTotal = product.getPrice() * product.getQuantity();
            totalAmount += productTotal;
            totalGST += product.getPrice() * product.getGst() * product.getQuantity() / 100.0;
        }
        double grandTotal = totalAmount + totalGST;
        totalamount_label.setText(String.format("%.2f", grandTotal));
    }

    // Helper method to retrieve business details from the Business table.
    // Returns an array: [0]=name, [1]=address, [2]=contact, [3]=email
    private String[] getBusinessDetails() {
        String[] details = new String[4];
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT name, address, contact, email FROM Business LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                details[0] = rs.getString("name");
                details[1] = rs.getString("address");
                details[2] = rs.getString("contact");
                details[3] = rs.getString("email");
            } else {
                details[0] = "Default Shop";
                details[1] = "Default Address";
                details[2] = "0000000000";
                details[3] = "default@example.com";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            details[0] = "Default Shop";
            details[1] = "Default Address";
            details[2] = "0000000000";
            details[3] = "default@example.com";
        }
        return details;
    }

    // Helper method to fetch latest invoice and customer IDs using customer phone
    private int[] fetchLatestInvoiceIds(String customerPhone) {
        int[] ids = new int[2]; // ids[0] = invoiceId, ids[1] = customerId
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT i.invoice_id, c.customer_id " +
                 "FROM InvoiceDetails i " +
                 "INNER JOIN Customers c ON i.customer_id = c.customer_id " +
                 "WHERE c.phone = ? " +
                 "ORDER BY i.invoice_id DESC LIMIT 1"
             )) {
            ps.setString(1, customerPhone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ids[0] = rs.getInt("invoice_id");
                ids[1] = rs.getInt("customer_id");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error fetching invoice details: " + e.getMessage());
        }
        return ids;
    }

    @FXML
    private void PrintPDF(ActionEvent event) {
        String customerName = customer_name_txt.getText();
        String customerPhone = customer_phone_txt.getText();
        // Validate phone number length before proceeding.
        if (!isValidPhone(customerPhone)) {
            showAlert("Validation Error", "Phone number must be exactly 10 digits.");
            return;
        }
        String paymentStatus = statustoggle_paid_unpaid.isSelected() ? "Paid" : "Unpaid";
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        String date = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        // In this method, we pass 0 for IDs, so generateReceiptPDF will try to fetch them.
        generateReceiptPDF(customerName, customerPhone, date, time, paymentStatus, 0, 0);
    }

    @FXML
    private void Print(ActionEvent event) {
        // Check if data has already been saved.
        if (dataSaved) {
            showAlert("Info", "Data has already been saved. Make changes to enable saving again.");
            return;
        }
        
        if (boughtitemlist.getItems().isEmpty()) {
            showAlert("Error", "The bought item list is empty. Add items to generate a receipt.");
            return;
        }
        if (customer_name_txt.getText().isEmpty() || customer_phone_txt.getText().isEmpty()) {
            showAlert("Error", "Customer name or phone number is missing.");
            return;
        }
        String customerName = customer_name_txt.getText();
        String customerPhone = customer_phone_txt.getText();
        // Validate phone number length before proceeding.
        if (!isValidPhone(customerPhone)) {
            showAlert("Validation Error", "Phone number must be exactly 10 digits.");
            return;
        }
        String paymentStatus = statustoggle_paid_unpaid.isSelected() ? "Paid" : "Unpaid";
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        String date = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // Recalculate GST and grand total
        double totalGST = 0.0;
        for (Product product : boughtitemlist.getItems()) {
            totalGST += product.getPrice() * product.getGst() * product.getQuantity() / 100.0;
        }
        double grandTotal = totalAmount + totalGST;

        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement()) {

            // Check if customer exists
            String checkCustomerQuery = "SELECT customer_id FROM Customers WHERE phone = '" + customerPhone + "'";
            ResultSet rs = stmt.executeQuery(checkCustomerQuery);
            int customerId;
            if (rs.next()) {
                customerId = rs.getInt("customer_id");
            } else {
                String insertCustomerQuery = "INSERT INTO Customers (customer_name, phone, date_of_visit) " +
                        "VALUES ('" + customerName + "', '" + customerPhone + "', '" + date + "')";
                stmt.executeUpdate(insertCustomerQuery, Statement.RETURN_GENERATED_KEYS);
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    customerId = generatedKeys.getInt(1);
                } else {
                    showAlert("Error", "Failed to insert customer into database.");
                    return;
                }
            }

            // Insert invoice with date and time
            String insertInvoiceQuery = "INSERT INTO InvoiceDetails (customer_id, date, time, amount, payment_status) " +
                    "VALUES (" + customerId + ", '" + date + "', '" + time + "', " + grandTotal + ", '" + paymentStatus + "')";
            stmt.executeUpdate(insertInvoiceQuery, Statement.RETURN_GENERATED_KEYS);
            ResultSet invoiceKeys = stmt.getGeneratedKeys();
            int invoiceId;
            if (invoiceKeys.next()) {
                invoiceId = invoiceKeys.getInt(1);
            } else {
                showAlert("Error", "Failed to insert invoice into database.");
                return;
            }

            // Deduct stock for each item
            for (Product product : boughtitemlist.getItems()) {
                String updateStockQuery = "UPDATE Product SET quantity = quantity - " + product.getQuantity() +
                        " WHERE product_id = " + product.getProductId();
                stmt.executeUpdate(updateStockQuery);
            }

            // Get business details from database
            String[] business = getBusinessDetails();

            // Build receipt string using business details
            StringBuilder receipt = new StringBuilder();
            receipt.append(business[0]).append("\n");   // Shop name
            receipt.append(business[1]).append("\n");   // Address
            receipt.append(business[2]).append(", ").append(business[3]).append("\n");
            receipt.append("=================================\n");
            receipt.append("Invoice ID: ").append(invoiceId).append("\n");
            receipt.append("Customer ID: ").append(customerId).append("\n");
            receipt.append("Customer Name: ").append(customerName).append("\n");
            receipt.append("Phone: ").append(customerPhone).append("\n");
            receipt.append("Date: ").append(date).append("\n");
            receipt.append("Time: ").append(time).append("\n");
            receipt.append("Status: ").append(paymentStatus).append("\n");
            receipt.append("=================================\n");

            for (Product product : boughtitemlist.getItems()) {
                double productTotal = product.getPrice() * product.getQuantity();
                receipt.append(String.format("%-25s %-10d %.2f\n", product.getProductName(), product.getQuantity(), productTotal));
            }

            receipt.append("=================================\n");
            receipt.append("Total Amount: ").append(String.format("%.2f", grandTotal)).append("\n");
            receipt.append("Total GST: ").append(String.format("%.2f", totalGST)).append("\n");
            receipt.append("=================================\n");
            receipt.append("Thank You for your Purchase!\n");
            recipt_textarea.setText(receipt.toString());

            // Mark current data as saved and disable the save button.
            dataSaved = true;
            Printbtn.setDisable(true);

            // The generateReceiptPDF call is commented out here because we are treating Print() as a save-only method.
            // To print the PDF, use the PrintPDF button which will fetch the latest IDs.
            // generateReceiptPDF(customerName, customerPhone, date, time, paymentStatus, invoiceId, customerId);

        } catch (SQLException e) {
            showAlert("Database Error", "Error generating the bill: " + e.getMessage());
        }
    }

    private void generateReceiptPDF(String customerName, String customerPhone, String date, String time, String paymentStatus, int invoiceId, int customerId) {
        // If the IDs are 0, fetch the latest invoice and customer IDs from the database
        if (invoiceId == 0 || customerId == 0) {
            int[] ids = fetchLatestInvoiceIds(customerPhone);
            invoiceId = ids[0];
            customerId = ids[1];
        }

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        // Get business details
        String[] business = getBusinessDetails();

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.setLeading(18f);
            float margin = 40;
            float startY = page.getMediaBox().getHeight() - margin;
            contentStream.newLineAtOffset(margin, startY);

            // Header: use business details from database (or defaults)
            contentStream.showText(business[0]);  // Shop name
            contentStream.newLine();
            contentStream.showText(business[1]);  // Address
            contentStream.newLine();
            contentStream.showText(business[2] + ", " + business[3]);  // Contact and email
            contentStream.newLine();
            contentStream.showText("=================================");
            contentStream.newLine();

            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("Invoice ID: " + invoiceId);
            contentStream.newLine();
            contentStream.showText("Customer ID: " + customerId);
            contentStream.newLine();
            contentStream.showText("Customer Name: " + customerName);
            contentStream.newLine();
            contentStream.showText("Phone: " + customerPhone);
            contentStream.newLine();
            contentStream.showText("Date: " + date);
            contentStream.newLine();
            contentStream.showText("Time: " + time);
            contentStream.newLine();
            contentStream.showText("Status: " + paymentStatus);
            contentStream.newLine();
            contentStream.showText("=================================");
            contentStream.newLine();

            double totalGST = 0.0;
            for (Product product : boughtitemlist.getItems()) {
                double productTotal = product.getPrice() * product.getQuantity();
                totalGST += product.getPrice() * product.getGst() * product.getQuantity() / 100.0;
                String productLine = String.format("%-25s %-10d %.2f",
                        product.getProductName(), product.getQuantity(), productTotal);
                contentStream.showText(productLine);
                contentStream.newLine();
            }

            double grandTotal = totalAmount + totalGST;
            contentStream.showText("=================================");
            contentStream.newLine();
            contentStream.showText("Total Amount: " + String.format("%.2f", grandTotal));
            contentStream.newLine();
            contentStream.showText("Total GST: " + String.format("%.2f", totalGST));
            contentStream.newLine();
            contentStream.showText("=================================");
            contentStream.newLine();
            contentStream.showText("Thank You for your Purchase!");
            contentStream.endText();
        } catch (IOException e) {
            showAlert("Error", "Error generating the receipt PDF: " + e.getMessage());
            return;
        }

        File receiptFile = new File("receipt.pdf");
        try {
            document.save(receiptFile);
            java.awt.Desktop.getDesktop().open(receiptFile);
        } catch (IOException e) {
            showAlert("Error", "Error saving or opening the receipt PDF: " + e.getMessage());
        } finally {
            try {
                document.close();
            } catch (IOException e) {
                showAlert("Error", "Error closing PDF document: " + e.getMessage());
            }
        }
        clearButtonAction(new ActionEvent());
    }

    // Helper method to get available stock from the Product table using product_id
    private int getAvailableStock(int productId) {
        int stock = 0;
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT quantity, min_stock FROM Product WHERE product_id = " + productId)) {
            if (rs.next()) {
                stock = rs.getInt("quantity");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error fetching stock data for product ID " + productId + ": " + e.getMessage());
        }
        return stock;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Helper method to validate a phone number is exactly 10 digits.
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return phone.matches("^\\d{10}$"); // Ensures phone number is exactly 10 digits long
    }
    
    @FXML
    private void handlePlusOne(ActionEvent event) {
        Product selected = boughtitemlist.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a product from the list to increase its quantity.");
            return;
        }
        int currentQuantity = selected.getQuantity();
        int availableStock = getAvailableStock(selected.getProductId());
        if (currentQuantity + 1 > availableStock) {
            showAlert("Low Stock", "Cannot increase quantity. Only " + availableStock + " items available in stock.");
            return;
        }
        selected.setQuantity(currentQuantity + 1);
        boughtitemlist.refresh();
        updateTotalAmount();
        // Mark data as unsaved and enable the save button.
        dataSaved = false;
        Printbtn.setDisable(false);
    }
    
    // Handler for the minusone button
    @FXML
    private void handleMinusOne(ActionEvent event) {
        Product selected = boughtitemlist.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a product from the list to decrease its quantity.");
            return;
        }
        int currentQuantity = selected.getQuantity();
        if (currentQuantity <= 1) {
            // If quantity goes to zero or less, remove the product.
            boughtitemlist.getItems().remove(selected);
        } else {
            selected.setQuantity(currentQuantity - 1);
        }
        boughtitemlist.refresh();
        updateTotalAmount();
        // Mark data as unsaved and enable the save button.
        dataSaved = false;
        Printbtn.setDisable(false);
    }
    
    // Handler for the cutproduct button
    @FXML
    private void handleCutProduct(ActionEvent event) {
        Product selected = boughtitemlist.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a product from the list to remove it.");
            return;
        }
        boughtitemlist.getItems().remove(selected);
        boughtitemlist.refresh();
        updateTotalAmount();
        // Mark data as unsaved and enable the save button.
        dataSaved = false;
        Printbtn.setDisable(false);
    }
}
