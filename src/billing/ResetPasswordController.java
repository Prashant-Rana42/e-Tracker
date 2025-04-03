package billing;
//by Prashant Rana(2300680140090),prashantrana422@gmail.com
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ResetPasswordController implements Initializable {

    @FXML
    private AnchorPane signuppane;
    @FXML
    private Button exitbt;
    @FXML
    private Button resetpsswordbtn;
    @FXML
    private ImageView loginbtn;
    @FXML
    private TextField username;
    @FXML
    private TextField newpassword;
    @FXML
    private TextField confirmpassword;
    @FXML
    private TextField securityQuestion;
    
    // Environment variable for local AppData
    String localAppDataPath = System.getenv("LOCALAPPDATA");
    // Central DB path (user credentials)
    private final String CENTRAL_DB_PATH = localAppDataPath + "\\AppUser\\user.db";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Any initialization if needed.
    }
    
    @FXML
    private void resetPassword(ActionEvent event) {
        // Retrieve input from text fields
        String user = username.getText().trim();
        String newPass = newpassword.getText().trim();
        String confirmPass = confirmpassword.getText().trim();
        String secAns = securityQuestion.getText().trim();
        
        // Validate input fields
        if(user.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty() || secAns.isEmpty()){
            showAlert("Error", "Please fill in all fields.", Alert.AlertType.ERROR);
            return;
        }
        
        if(!newPass.equals(confirmPass)){
            showAlert("Error", "Passwords do not match.", Alert.AlertType.ERROR);
            return;
        }
        
        // Connect to central user DB (user.db)
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + CENTRAL_DB_PATH)) {
            // Check if username and security answer match a record in the database
            String checkQuery = "SELECT * FROM users WHERE username = ? AND security = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
                pstmt.setString(1, user);
                pstmt.setString(2, secAns);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if(rs.next()){
                        // Username and security answer match: update password
                        String updateQuery = "UPDATE users SET password = ? WHERE username = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setString(1, newPass);
                            updateStmt.setString(2, user);
                            int rowsAffected = updateStmt.executeUpdate();
                            if(rowsAffected > 0){
                                showAlert("Success", "Password reset successfully.", Alert.AlertType.INFORMATION);
                            } else {
                                showAlert("Error", "Password reset failed.", Alert.AlertType.ERROR);
                            }
                        }
                    } else {
                        showAlert("Error", "Invalid username or security answer.", Alert.AlertType.ERROR);
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error resetting password: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void OpenLoginWindow() {
        try {
            // Close the current window
            Stage currentStage = (Stage) exitbt.getScene().getWindow();
            currentStage.close();

            // Open the login window
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("Login.fxml"));
            Parent newRoot = loader.load();
            Scene newScene = new Scene(newRoot);
            Stage newStage = new Stage();
            newStage.setScene(newScene);
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
