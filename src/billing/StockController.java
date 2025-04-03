package billing;
//by Prashant Rana(2300680140090),prashantrana422@gmail.com
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

public class StockController implements Initializable {

    @FXML
    private AnchorPane StockPane;
    @FXML
    private TableView<Product> stock_list_table;
    @FXML
    private TableColumn<Product, Integer> product_id_column;
    @FXML
    private TableColumn<Product, String> product_name_column;
    @FXML
    private TableColumn<Product, Integer> quantity_column;
    @FXML
    private TableColumn<Product, Integer> min_stock_column;
    @FXML
    private TextField product_id_txt;
    @FXML
    private TextField product_name_txt;
    @FXML
    private TextField quantity_txt;
    @FXML
    private TextField min_stock_txt;
    @FXML
    private Button add_stock_btn;
    @FXML
    private Button remove_stock_btn;
    @FXML
    private Button delete_product_btn;
    
    // Search components
    @FXML
    private TextField Searchfield;
    @FXML
    private ChoiceBox<String> searchchoice; // Changed to ChoiceBox<String>

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    // Wrap productList in a FilteredList for dynamic filtering.
    private FilteredList<Product> filteredProductList;
    
    private Connection conn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize database connection
        conn = DatabaseConnection.connect();

        // Set up TableView columns
        product_id_column.setCellValueFactory(new PropertyValueFactory<>("productId"));
        product_name_column.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantity_column.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        min_stock_column.setCellValueFactory(new PropertyValueFactory<>("minStock"));

        // Load data into TableView
        loadStockData();

        // Initialize search criteria for ChoiceBox
        searchchoice.setItems(FXCollections.observableArrayList("ID", "Name", "Quantity", "Min"));
        searchchoice.setValue("Name"); // Default selection

        // Wrap productList in a FilteredList and set it to the table
        filteredProductList = new FilteredList<>(productList, p -> true);
        stock_list_table.setItems(filteredProductList);

        // Add listener for real-time filtering on search text field
        Searchfield.textProperty().addListener((observable, oldValue, newValue) -> {
            filterStockList();
        });
        // Listener for when the search criteria changes
        searchchoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filterStockList();
        });

        // Highlight rows where stock is below the minimum
        stock_list_table.setRowFactory(tv -> new TableRow<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.getQuantity() < item.getMinStock()) {
                    setStyle("-fx-background-color: rgba(255, 0, 0, 0.5);");
                } else {
                    setStyle("");
                }
            }
        });

        // Add listener for TableView row selection to populate text fields
        stock_list_table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                product_id_txt.setText(String.valueOf(newValue.getProductId()));
                product_name_txt.setText(newValue.getProductName());
                quantity_txt.setText(String.valueOf(newValue.getQuantity()));
                min_stock_txt.setText(String.valueOf(newValue.getMinStock()));
            }
        });

        // Set button actions
        add_stock_btn.setOnAction(e -> addStock());
        remove_stock_btn.setOnAction(e -> removeStock());
        delete_product_btn.setOnAction(e -> deleteProduct());
    }
    
    // Filter method for stock based on search field and criteria
    private void filterStockList() {
        String filterText = Searchfield.getText().toLowerCase().trim();
        String selectedCriteria = searchchoice.getValue();

        filteredProductList.setPredicate(product -> {
            if (filterText.isEmpty()) {
                return true;
            }
            switch (selectedCriteria) {
                case "ID":
                    return String.valueOf(product.getProductId()).toLowerCase().contains(filterText);
                case "Name":
                    return product.getProductName().toLowerCase().contains(filterText);
                case "Quantity":
                    return String.valueOf(product.getQuantity()).toLowerCase().contains(filterText);
                case "Min":
                    return String.valueOf(product.getMinStock()).toLowerCase().contains(filterText);
                default:
                    return true;
            }
        });
    }
    
    private void loadStockData() {
        productList.clear();
        String stockQuery = "SELECT * FROM Product";
        try (PreparedStatement stmt = conn.prepareStatement(stockQuery);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                productList.add(new Product(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getInt("min_stock")
                ));
            }
            // TableView updates automatically via filteredProductList.
        } catch (SQLException ex) {
            showAlert("Error", "Failed to load stock data: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void addStock() {
        try {
            int productId = Integer.parseInt(product_id_txt.getText());
            String productName = product_name_txt.getText();
            int quantity = Integer.parseInt(quantity_txt.getText());
            int minStock = Integer.parseInt(min_stock_txt.getText());

            String query = "SELECT * FROM Product WHERE product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, productId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int currentQuantity = rs.getInt("quantity");
                    String updateQuery = "UPDATE Product SET quantity = ?, min_stock = ? WHERE product_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, currentQuantity + quantity);
                        updateStmt.setInt(2, minStock);
                        updateStmt.setInt(3, productId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    String insertQuery = "INSERT INTO Product(product_id, product_name, quantity, min_stock) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, productId);
                        insertStmt.setString(2, productName);
                        insertStmt.setInt(3, quantity);
                        insertStmt.setInt(4, minStock);
                        insertStmt.executeUpdate();
                    }
                }
            }
            showAlert("Success", "Stock added successfully!", Alert.AlertType.INFORMATION);
            loadStockData();
        } catch (Exception ex) {
            showAlert("Error", "Failed to add stock: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void removeStock() {
        try {
            int productId = Integer.parseInt(product_id_txt.getText());
            int quantity = Integer.parseInt(quantity_txt.getText());

            String query = "SELECT * FROM Product WHERE product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, productId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int currentQuantity = rs.getInt("quantity");
                    if (currentQuantity >= quantity) {
                        int newQuantity = currentQuantity - quantity;
                        String updateQuery = "UPDATE Product SET quantity = ? WHERE product_id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setInt(1, newQuantity);
                            updateStmt.setInt(2, productId);
                            updateStmt.executeUpdate();
                        }
                        showAlert("Success", "Stock removed successfully!", Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Error", "Not enough stock to remove!", Alert.AlertType.ERROR);
                    }
                } else {
                    showAlert("Error", "Product not found!", Alert.AlertType.ERROR);
                }
            }
            loadStockData();
        } catch (Exception ex) {
            showAlert("Error", "Failed to remove stock: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteProduct() {
        try {
            int productId = Integer.parseInt(product_id_txt.getText());
            String query = "DELETE FROM Product WHERE product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, productId);
                stmt.executeUpdate();
            }
            showAlert("Success", "Product deleted successfully!", Alert.AlertType.INFORMATION);
            loadStockData();
        } catch (Exception ex) {
            showAlert("Error", "Failed to delete product: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
