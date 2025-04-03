package billing;
//by Prashant Rana(2300680140090),prashantrana422@gmail.com
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class NewUserController implements Initializable {

    @FXML
    private AnchorPane signuppane;
    @FXML
    private Button exitbtn;
    @FXML
    private Button signupbtn;
    @FXML
    private ImageView loginbtn;
    @FXML
    private TextField newusername;
    @FXML
    private TextField newpassword;
    @FXML
    private TextField confirmpassword;
    @FXML
    private TextField securityQuestion;
    
    String localAppDataPath = System.getenv("LOCALAPPDATA");//gets the path to local folder of current user
    private final String CENTRAL_DB_PATH = localAppDataPath + "\\AppUser\\user.db";
    // Folder where individual user account DBs will be stored
    private final String ACCOUNTS_FOLDER = localAppDataPath+"\\AppUser\\accounts";

    @FXML
    private void handleSignup() {
        // Validate that all fields are filled
        String username = newusername.getText().trim();
        String password = newpassword.getText().trim();
        String confirmPassword = confirmpassword.getText().trim();
        String securityAns = securityQuestion.getText().trim();
        
        if(username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || securityAns.isEmpty()){
            showAlert(Alert.AlertType.ERROR, "Signup Error", "Please fill all the fields.");
            return;
        }
        
        // Check that password and confirm password match
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", "Passwords do not match.");
            return;
        }
        
        // First, ensure the folder for central DB exists
        File centralFolder = new File(localAppDataPath+"\\AppUser");
        if (!centralFolder.exists()) {
            centralFolder.mkdirs();
        }
        
        // Insert the new user into the central user DB (user.db)
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + CENTRAL_DB_PATH)) {
            // Create table if it doesn't exist
            String createCentralTable = "CREATE TABLE IF NOT EXISTS users (" +
                                        "username TEXT PRIMARY KEY, " +
                                        "password TEXT NOT NULL, " +
                                        "security TEXT NOT NULL)";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createCentralTable);
            }
            
            // Check if username already exists
            String checkQuery = "SELECT username FROM users WHERE username=?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
                pstmt.setString(1, username);
                if (pstmt.executeQuery().next()) {
                    showAlert(Alert.AlertType.ERROR, "Signup Error", "User already exists.");
                    return;
                }
            }
            
            // Insert new user
            String insertUser = "INSERT INTO users (username, password, security) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertUser)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, securityAns);
                pstmt.executeUpdate();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Signup Error", "An error occurred while saving user data.");
            return;
        }
        
        // Now create the user-specific account database
        try {
            // Ensure the accounts folder exists
            File accountsFolder = new File(ACCOUNTS_FOLDER);
            if (!accountsFolder.exists()) {
                accountsFolder.mkdirs();
            }
            
            // Database file for the new user
            String userDbPath = ACCOUNTS_FOLDER + File.separator + username + ".db";
            File userDbFile = new File(userDbPath);
            
            // Create the database if it doesn't exist
            if (!userDbFile.exists()) {
                try (Connection userConn = DriverManager.getConnection("jdbc:sqlite:" + userDbPath);
                     Statement stmt = userConn.createStatement()) {
                    
                    // Create the Customers table
                    String createCustomers = "CREATE TABLE IF NOT EXISTS Customers (" +
                                             "customer_id INTEGER PRIMARY KEY, " +
                                             "customer_name TEXT, " +
                                             "phone TEXT, " +
                                             "date_of_visit TEXT)";
                    stmt.execute(createCustomers);
                    
                    // Create the Invoice_Details table
                    String createInvoiceDetails = "CREATE TABLE IF NOT EXISTS InvoiceDetails (" +
                                                  "invoice_id INTEGER PRIMARY KEY, " +
                                                  "customer_id INTEGER, " +
                                                  "date TEXT, " +
                                                  "time TEXT, " +
                                                  "amount REAL, " +
                                                  "payment_status TEXT)";
                    stmt.execute(createInvoiceDetails);
                    
                    // Create the Product table
                    String createProduct = "CREATE TABLE IF NOT EXISTS Product (" +
                                           "product_id INTEGER PRIMARY KEY, " +
                                           "product_name TEXT, " +
                                           "price REAL, " +
                                           "gst REAL, " +
                                           "quantity INTEGER, " +
                                           "min_stock INTEGER)";
                    stmt.execute(createProduct);
                    
                    // Create the Stock table
                    String createBusiness = "CREATE TABLE IF NOT EXISTS Business (" +
                                           "name TEXT, " +
                                           "address TEXT, " +
                                           "contact TEXT, " +        
                                           "email TEXT)";
                    stmt.execute(createBusiness);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while creating the account database.");
            return;
        }
        
        showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully!");
        // Optionally, open the login window now.
        OpenLoginWindow();
    }
    
    @FXML
    private void OpenLoginWindow() {
        try {
            // Close the current window
            Stage currentStage = (Stage) exitbtn.getScene().getWindow();
            currentStage.close();
            
            // Open the login window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent newRoot = loader.load();
            Scene newScene = new Scene(newRoot);
            Stage newStage = new Stage();
            newStage.setScene(newScene);
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO: Any initialization if needed.
    }
}
