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
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class AddproductController implements Initializable {

    @FXML
    private AnchorPane addproductpane;
    @FXML
    private TextField productIDField;
    @FXML
    private TextField productNameField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField gstField;
    @FXML
    private Button addProductButton;
    @FXML
    private Button updateProductButton;
    @FXML
    private Button deleteProductButton;
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, Integer> productIDColumn;
    @FXML
    private TableColumn<Product, String> productNameColumn;
    @FXML
    private TableColumn<Product, Double> priceColumn;
    @FXML
    private TableColumn<Product, Double> gstColumn;
    
    // Search components
    @FXML
    private TextField Searchfield;
    @FXML
    private ChoiceBox<String> searchchoice; // Changed to ChoiceBox<String>

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    // Wrap the productList with a FilteredList for real-time filtering.
    private FilteredList<Product> filteredProductList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set up TableView columns
        productIDColumn.setCellValueFactory(cellData -> cellData.getValue().productIdProperty().asObject());
        productNameColumn.setCellValueFactory(cellData -> cellData.getValue().productNameProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        gstColumn.setCellValueFactory(cellData -> cellData.getValue().gstProperty().asObject());

        // Load data into TableView
        loadProductData();

        // Initialize the search criteria for the ChoiceBox
        searchchoice.setItems(FXCollections.observableArrayList("ID", "Name", "Price", "GST"));
        searchchoice.setValue("Name"); // Default selection

        // Wrap productList in a FilteredList and set it to the table
        filteredProductList = new FilteredList<>(productList, p -> true);
        productTable.setItems(filteredProductList);

        // Add listener for real-time filtering on search text field
        Searchfield.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProductList();
        });
        // Listener for when the search criteria changes
        searchchoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filterProductList();
        });

        // Add listener for TableView row selection
        productTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                productIDField.setText(String.valueOf(newValue.getProductId()));
                productNameField.setText(newValue.getProductName());
                priceField.setText(String.valueOf(newValue.getPrice()));
                gstField.setText(String.valueOf(newValue.getGst()));
            }
        });

        // Button actions
        addProductButton.setOnAction(event -> addProduct());
        updateProductButton.setOnAction(event -> updateProduct());
        deleteProductButton.setOnAction(event -> deleteProduct());
    }

    // Filter method for products based on search field and criteria
    private void filterProductList() {
        String filterText = Searchfield.getText().toLowerCase().trim();
        String selectedCriteria = searchchoice.getValue();

        filteredProductList.setPredicate(product -> {
            // Show all products if search field is empty
            if (filterText.isEmpty()) {
                return true;
            }
            switch (selectedCriteria) {
                case "ID":
                    return String.valueOf(product.getProductId()).toLowerCase().contains(filterText);
                case "Name":
                    return product.getProductName().toLowerCase().contains(filterText);
                case "Price":
                    return String.valueOf(product.getPrice()).toLowerCase().contains(filterText);
                case "GST":
                    return String.valueOf(product.getGst()).toLowerCase().contains(filterText);
                default:
                    return true;
            }
        });
    }

    private void loadProductData() {
        String query = "SELECT * FROM Product";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            // Clear the list before loading new data
            productList.clear();

            // Iterate through result set and add products to the observable list
            while (resultSet.next()) {
                Product product = new Product(
                        resultSet.getInt("product_id"),
                        resultSet.getString("product_name"),
                        resultSet.getDouble("price"),
                        resultSet.getDouble("gst")
                );
                productList.add(product);
            }
            // TableView is automatically updated via filteredProductList.

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load product data.");
        }
    }

    private void addProduct() {
        String productID = productIDField.getText().trim();
        String productName = productNameField.getText().trim();
        String price = priceField.getText().trim();
        String gst = gstField.getText().trim();

        if (productID.isEmpty() || productName.isEmpty() || price.isEmpty() || gst.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill all fields.");
            return;
        }

        String query = "INSERT INTO Product (product_id, product_name, price, gst) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, Integer.parseInt(productID));
            preparedStatement.setString(2, productName);
            preparedStatement.setDouble(3, Double.parseDouble(price));
            preparedStatement.setDouble(4, Double.parseDouble(gst));

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully.");
                loadProductData();  // Reload data in table
                clearFields();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add product(Product ID already exists).");
        }
    }

    private void updateProduct() {
        String productID = productIDField.getText().trim();
        String productName = productNameField.getText().trim();
        String price = priceField.getText().trim();
        String gst = gstField.getText().trim();

        if (productID.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Product ID is required to update a product.");
            return;
        }

        String query = "UPDATE Product SET product_name = ?, price = ?, gst = ? WHERE product_id = ?";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, productName);
            preparedStatement.setDouble(2, Double.parseDouble(price));
            preparedStatement.setDouble(3, Double.parseDouble(gst));
            preparedStatement.setInt(4, Integer.parseInt(productID));

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully.");
                loadProductData();  // Reload data in table
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No product found with the given ID.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update product.");
        }
    }

    private void deleteProduct() {
        String productID = productIDField.getText().trim();

        if (productID.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Product ID is required to delete a product.");
            return;
        }

        String query = "DELETE FROM Product WHERE product_id = ?";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, Integer.parseInt(productID));
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully.");
                loadProductData();  // Reload data in table
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No product found with the given ID.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete product.");
        }
    }

    private void clearFields() {
        productIDField.clear();
        productNameField.clear();
        priceField.clear();
        gstField.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
