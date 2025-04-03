package billing;
//by Prashant Rana(2300680140090),prashantrana422@gmail.com
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class SettingsController implements Initializable {

    @FXML
    private AnchorPane SettingPane;
    @FXML
    private TextField shopname;      // Business name
    @FXML
    private TextField Address;       // Business address
    @FXML
    private TextField Phone_no;      // Business phone number (contact)
    @FXML
    private TextField gmail;         // Business email
    @FXML
    private Button savebtn;
    @FXML
    private Button switchaccount;
    @FXML
    private Button changePassword;

    // Obtain a connection to the user's account database using DBUtil.
    // (This assumes DBUtil.getUserConnection() returns a connection to the appropriate DB.)
    private Connection conn;
    @FXML
    private Hyperlink facebooklink;
    @FXML
    private Hyperlink instalink;
    @FXML
    private Hyperlink linkedin;
    @FXML
    private Hyperlink gitlink;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = new DBUtil().getUserConnection();
        
        // Load existing business settings if any
        loadBusinessDetails();
        
        // Set actions for buttons
        savebtn.setOnAction(e -> saveBusinessDetails());
        switchaccount.setOnAction(e -> switchAccount());
        changePassword.setOnAction(e -> changePassword());
        
        facebooklink.setOnAction(e -> openURL("https://www.facebook.com/profile.php?id=100039767195383"));
        instalink.setOnAction(e -> openURL("https://www.instagram.com/prashant_.rana472?igsh=YzIjYTk10Dg3Zg=="));
        linkedin.setOnAction(e -> openURL("https://www.linkedin.com/in/prashant-rana-29526a254"));
        gitlink.setOnAction(e -> openURL("https://github.com/Prashant-Rana42"));
    }
    
    private void loadBusinessDetails() {
        String query = "SELECT * FROM Business LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                shopname.setText(rs.getString("name"));
                Address.setText(rs.getString("address"));
                Phone_no.setText(rs.getString("contact"));
                gmail.setText(rs.getString("email"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load business details: " + ex.getMessage());
        }
    }
    
    private void saveBusinessDetails() {
        String name = shopname.getText().trim();
        String address = Address.getText().trim();
        String contact = Phone_no.getText().trim();
        String email = gmail.getText().trim();
        
        if (name.isEmpty() || address.isEmpty() || contact.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill in all fields.");
            return;
        }
        
        try {
            // Check if a business record exists
            String checkQuery = "SELECT COUNT(*) FROM Business";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                 ResultSet rs = checkStmt.executeQuery()) {
                int count = rs.getInt(1);
                if (count > 0) {
                    // Update the existing record
                    String updateQuery = "UPDATE Business SET name = ?, address = ?, contact = ?, email = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, name);
                        updateStmt.setString(2, address);
                        updateStmt.setString(3, contact);
                        updateStmt.setString(4, email);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Insert a new record
                    String insertQuery = "INSERT INTO Business (name, address, contact, email) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, name);
                        insertStmt.setString(2, address);
                        insertStmt.setString(3, contact);
                        insertStmt.setString(4, email);
                        insertStmt.executeUpdate();
                    }
                }
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", "Business details saved successfully.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save business details: " + ex.getMessage());
        }
    }
    
    private void openURL(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open the URL: " + url);
        }
    }
    
    private void switchAccount() {
        try {
            // Close current settings window
            Stage currentStage = (Stage) switchaccount.getScene().getWindow();
            currentStage.close();
            
            // Open the login window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to switch account: " + ex.getMessage());
        }
    }
    
    private void changePassword() {
        try {
            // Close current settings window
            Stage currentStage = (Stage) changePassword.getScene().getWindow();
            //currentStage.close();
            
            // Open the Reset Password window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ResetPassword.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Reset Password");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Reset Password window: " + ex.getMessage());
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
