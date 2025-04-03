package billing;
//by Prashant Rana(2300680140090),prashantrana422@gmail.com
import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class LoginController implements Initializable {

    @FXML
    private TextField username;
    
    
    @FXML
    private PasswordField passwordField; 
    @FXML
    private TextField passwordTextField; 

    @FXML
    private ImageView eyeopen;
    @FXML
    private ImageView eyeclosed;
    @FXML
    private Button loginButton;
     String localAppDataPath = System.getenv("LOCALAPPDATA");//gets the path to local folder of current user
    private final String CENTRAL_DB_PATH = localAppDataPath + "\\AppUser\\user.db";
    // Folder where individual user account DBs will be stored
    private final String ACCOUNTS_FOLDER = localAppDataPath+"\\AppUser\\accounts";
    //private static final String CENTRAL_DB_PATH = "C:\\Users\\user\\AppData\\Local\\AppUser\\user.db";
    
    @FXML
    private Label new_account;
    @FXML
    private Button exitbtn;
    @FXML
    private ImageView loginbtn;
    @FXML
    private Hyperlink facebooklink;
    @FXML
    private Hyperlink instalink;
    @FXML
    private Hyperlink linkedin;
    @FXML
    private Hyperlink gitlink;
    @FXML
    private AnchorPane loginpane;
    @FXML
    private Label resetpassword;
    
    @FXML
    private void handleExit() {
        // Exit the application
        System.exit(0);
    }
    
    @FXML
    private void OpenNewUserWindow() {
        try {
            // Close the current window
            Stage currentStage = (Stage) exitbtn.getScene().getWindow();
            currentStage.close();
            
            // Open the new window (New User window)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("NewUser.fxml"));
            Parent newRoot = loader.load();
            Scene newScene = new Scene(newRoot);
            Stage newStage = new Stage();
            newStage.setScene(newScene);
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void OpenPaswordResetWindow() {
        try {
            // Close the current window
            Stage currentStage = (Stage) exitbtn.getScene().getWindow();
            currentStage.close();
            
            // Open the new window (Password Reset window)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ResetPassword.fxml"));
            Parent newRoot = loader.load();
            Scene newScene = new Scene(newRoot);
            Stage newStage = new Stage();
            newStage.setScene(newScene);
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initially, show the masked password field and hide the plain text one.
        passwordTextField.setVisible(false);
        eyeopen.setVisible(false); // closed eye is visible when password is hidden
        
        // Bind both password controls so they always have the same text
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        
        // Toggle to show password
        eyeclosed.setOnMouseClicked(event -> {
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);
            eyeclosed.setVisible(false);
            eyeopen.setVisible(true);
        });
        
        // Toggle to hide password
        eyeopen.setOnMouseClicked(event -> {
            passwordField.setVisible(true);
            passwordTextField.setVisible(false);
            eyeopen.setVisible(false);
            eyeclosed.setVisible(true);
        });
        
        // Handle the login button click
        loginButton.setOnAction(event -> handleLogin());
        
        // Set up hyperlink actions
        facebooklink.setOnAction(e -> openURL("https://www.facebook.com/YourPage"));
        instalink.setOnAction(e -> openURL("https://www.instagram.com/YourProfile"));
        linkedin.setOnAction(e -> openURL("https://www.linkedin.com/in/YourProfile"));
        gitlink.setOnAction(e -> openURL("https://github.com/YourUsername"));
    }
    
    private void handleLogin() {
        // Get the username and password (depending on which field is visible)
        String user = username.getText().trim();
        String pass = passwordField.isVisible() ? passwordField.getText().trim() : passwordTextField.getText().trim();
        
        if(user.isEmpty() || pass.isEmpty()){
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter both username and password.");
            return;
        }
        
        // Validate login credentials against the central user database
        if (validateLogin(user, pass)) {
            System.out.println("Login successful!");
            // Set the current user so that other modules can use it.
            CurrentUser.username = user;
            
            // After a successful login, load the Billing module.
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("BillingDesign.fxml"));
                Parent billingRoot = loader.load();
                Scene billingScene = new Scene(billingRoot);
                billingScene.getStylesheets().add(getClass().getResource("table.css").toExternalForm());
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(billingScene);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid Credentials");
        }
    }
    
    private boolean validateLogin(String username, String password) {
        boolean valid = false;
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + CENTRAL_DB_PATH)) {
            String query = "SELECT * FROM users WHERE username=? AND password=?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                try (ResultSet rs = pstmt.executeQuery()) {
                    valid = rs.next();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return valid;
    }
    
    private void openURL(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open the URL: " + url);
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
