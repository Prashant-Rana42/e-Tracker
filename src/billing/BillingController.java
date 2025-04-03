package billing;
//by Prashant Rana(2300680140090),prashantrana422@gmail.com
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class BillingController implements Initializable {

    @FXML
    private Label dateLabel;  
    @FXML
    private Pane dashpane;    
    @FXML
    private Label dash;      
    @FXML
    private Label newproduct; 
    @FXML
    private Label stock; 
    @FXML
    private Label customerbtn; 
    @FXML
    private Label Invoicebtn; 
    @FXML
    private Label billbtn; 
    @FXML
    private Pane Dashboard;
    @FXML
    private AnchorPane anchor; // Main home panel
    @FXML
    private Label UserLabel;
    @FXML
    private Label settings;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Platform.runLater(() -> {
            Stage stage = (Stage) anchor.getScene().getWindow();
            stage.centerOnScreen();
        });
        UserLabel.setText(CurrentUser.username);
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateLabel.setText(today.format(formatter));  
        setupSidebarActions();
        loadDashboardPane();
        //applyFadeInEffect(); // Apply fade-in to the main home panel after loading
    }

    private void applyFadeInEffect() {
        FadeTransition fade = new FadeTransition(Duration.seconds(0.5), anchor);
        fade.setFromValue(0); // Start fully transparent
        fade.setToValue(1);   // End fully visible
        fade.play();
    }

    private void setupSidebarActions() {
        newproduct.setOnMouseClicked(event -> loadAddProductPane());
        stock.setOnMouseClicked(event -> loadStockPane());
        customerbtn.setOnMouseClicked(event -> loadCustomerPane());
        Invoicebtn.setOnMouseClicked(event -> loadInvoicePane());
        billbtn.setOnMouseClicked(event -> loadBillPane());
        dash.setOnMouseClicked(event -> loadDashboardPane());
        settings.setOnMouseClicked(event -> loadSettingsPane());
    }

    private void loadAddProductPane() {
        try {
            dashpane.getChildren().clear(); // Clear existing children
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Addproduct.fxml"));
            Pane addProductPane = loader.load();
            dashpane.getChildren().setAll(addProductPane); // Add the new pane
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    
    private void loadBillPane() {
        try {
            dashpane.getChildren().clear(); 
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Billgeneration.fxml"));
            Pane addBillPane = loader.load();
            dashpane.getChildren().setAll(addBillPane); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadSettingsPane() {
        try {
            dashpane.getChildren().clear(); // Clear existing children
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Settings.fxml"));
            Pane addProductPane = loader.load();
            dashpane.getChildren().setAll(addProductPane); // Add the new pane
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadDashboardPane() {
        try {
            dashpane.getChildren().clear(); 
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
            Pane dashboardPane = loader.load();
            dashpane.getChildren().setAll(dashboardPane); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }   
}
