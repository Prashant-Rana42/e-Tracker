package billing;
// by Prashant Rana(2300680140090), prashantrana422@gmail.com
import javafx.beans.property.SimpleDoubleProperty;
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
    @FXML private Button Printbtn;
    @FXML private Button clear_btn;
    @FXML private Button printpdf;
    @FXML private TextField Searchfield;

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
        // Create a FilteredList that wraps the current items in product_list
        ObservableList<Product> masterList = product_list.getItems();
        FilteredList<Product> filteredData = new FilteredList<>(masterList, p -> true);

        // Add listener to the Searchfield to filter products by name in real time
        Searchfield.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(product -> {
                // If search field is empty, display all products.
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                // Compare product name with search input (case-insensitive)
                String lowerCaseFilter = newValue.toLowerCase();
                return product.getProductName().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // Wrap the FilteredList in a SortedList to preserve table sorting
        SortedList<Product> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(product_list.comparatorProperty());

        // Set the sorted (and filtered) data as items in the product_list TableView
        product_list.setItems(sortedData);
        // --- End Search Functionality Addition ---

        setupToggleButton();
        product_list.setOnMouseClicked(this::handleProductClick);
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

    private void addToBill(Product selectedProduct) {
        int currentQuantity = 0;
        for (Product product : boughtitemlist.getItems()) {
            if (product.getProductId() == selectedProduct.getProductId()) {
                currentQuantity = product.getQuantity();
                break;
            }
        }
        int desiredQuantity = currentQuantity + 1;
        int availableStock = getAvailableStock(selectedProduct.getProductId());
        if (desiredQuantity > availableStock) {
            showAlert("Low Stock", "Cannot add more of " + selectedProduct.getProductName() + ". Only " + availableStock + " in stock.");
            return;
        }
        for (Product product : boughtitemlist.getItems()) {
            if (product.getProductId() == selectedProduct.getProductId()) {
                product.setQuantity(desiredQuantity);
                boughtitemlist.refresh();
                updateTotalAmount();
                return;
            }
        }
        Product newProduct = new Product(
            selectedProduct.getProductId(),
            selectedProduct.getProductName(),
            selectedProduct.getPrice(),
            selectedProduct.getGst()
        );
        newProduct.setQuantity(1);
        boughtitemlist.getItems().add(newProduct);
        updateTotalAmount();
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
        // In this method, we only generate the PDF without invoice/customer IDs
        // You might call the full Print() method instead for complete functionality
        // For demonstration, we assume invoice/customer IDs are not available here
        generateReceiptPDF(customerName, customerPhone, date, time, paymentStatus, 0, 0);
    }

    @FXML
    private void Print(ActionEvent event) {
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
            showAlert("Validation Error", "Phone number must be exactly 10 digits starting from 6-9");
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

            // Generate PDF with all details including invoice and customer IDs
            //generateReceiptPDF(customerName, customerPhone, date, time, paymentStatus, invoiceId, customerId);

        } catch (SQLException e) {
            showAlert("Database Error", "Error generating the bill: " + e.getMessage());
        }
    }

    private void generateReceiptPDF(String customerName, String customerPhone, String date, String time, String paymentStatus, int invoiceId, int customerId) {
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
        return phone.matches("^[6-9]\\d{9}$"); // Ensures it starts with 6-9 (common for India) and is 10 digits long
    }
}
