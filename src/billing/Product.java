package billing;
//by Prashant Rana(2300680140090),prashantrana422@gmail.com
import javafx.beans.property.*;

public class Product {

    private final IntegerProperty productId;
    private final StringProperty productName;
    private final DoubleProperty price;    // For Add Product
    private final DoubleProperty gst;      // For Add Product
    private final IntegerProperty quantity; // For Stock Manager
    private final IntegerProperty minStock; // For Stock Manager

    // Constructor for Add Product (with price and gst)
    public Product(int productId, String productName, double price, double gst) {
        this.productId = new SimpleIntegerProperty(productId);
        this.productName = new SimpleStringProperty(productName);
        this.price = new SimpleDoubleProperty(price);
        this.gst = new SimpleDoubleProperty(gst);
        this.quantity = new SimpleIntegerProperty(0);   // Default to 0 for Add Product
        this.minStock = new SimpleIntegerProperty(0);   // Default to 0 for Add Product
    }

    // Constructor for Stock Manager (with quantity and minStock)
    public Product(int productId, String productName, int quantity, int minStock) {
        this.productId = new SimpleIntegerProperty(productId);
        this.productName = new SimpleStringProperty(productName);
        this.price = new SimpleDoubleProperty(0);  // Default to 0 for Stock Management
        this.gst = new SimpleDoubleProperty(0);    // Default to 0 for Stock Management
        this.quantity = new SimpleIntegerProperty(quantity);
        this.minStock = new SimpleIntegerProperty(minStock);
    }

    // Getters and Setters for all properties

    public int getProductId() {
        return productId.get();
    }

    public void setProductId(int productId) {
        this.productId.set(productId);
    }

    public IntegerProperty productIdProperty() {
        return productId;
    }

    public String getProductName() {
        return productName.get();
    }

    public void setProductName(String productName) {
        this.productName.set(productName);
    }

    public StringProperty productNameProperty() {
        return productName;
    }

    public double getPrice() {
        return price.get();
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public double getGst() {
        return gst.get();
    }

    public void setGst(double gst) {
        this.gst.set(gst);
    }

    public DoubleProperty gstProperty() {
        return gst;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public int getMinStock() {
        return minStock.get();
    }

    public void setMinStock(int minStock) {
        this.minStock.set(minStock);
    }

    public IntegerProperty minStockProperty() {
        return minStock;
    }
}
