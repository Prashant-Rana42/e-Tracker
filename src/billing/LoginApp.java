package billing;
// by Prashant Rana(2300680140090), prashantrana422@gmail.com

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import javafx.scene.Parent;

public class LoginApp extends Application {

    // Get the LOCALAPPDATA path
    String localAppDataPath = System.getenv("LOCALAPPDATA");
    // Define the main AppUser folder path
    private final String APP_USER_FOLDER = localAppDataPath + "\\AppUser";
    // Path for the central database file
    private final String CENTRAL_DB_PATH = APP_USER_FOLDER + "\\user.db";
    // Folder for individual user account databases
    private final String ACCOUNTS_FOLDER = APP_USER_FOLDER + "\\accounts";

    @Override
    public void start(Stage primaryStage) {
        // Set up the folder and database if not already created
        setupDatabase();

        try {
            // Load the login UI (assumed to be defined in Login.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("table.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle("e-Tracker");
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupDatabase() {
        try {
            // Create the main AppUser folder if it doesn't exist
            File appUserFolder = new File(APP_USER_FOLDER);
            if (!appUserFolder.exists()) {
                appUserFolder.mkdirs();
                System.out.println("Created folder: " + APP_USER_FOLDER);
            }
            
            // Create the accounts folder if it doesn't exist
            File accountsFolder = new File(ACCOUNTS_FOLDER);
            if (!accountsFolder.exists()) {
                accountsFolder.mkdirs();
                System.out.println("Created accounts folder: " + ACCOUNTS_FOLDER);
            }

            // Create the central database file if it does not exist
            File dbFile = new File(CENTRAL_DB_PATH);
            if (!dbFile.exists()) {
                Connection conn = DriverManager.getConnection("jdbc:sqlite:" + CENTRAL_DB_PATH);
                Statement stmt = conn.createStatement();
                // Create a table to store users (if not exists)
                String createTable = "CREATE TABLE IF NOT EXISTS users (" +
                                     "username TEXT PRIMARY KEY, " +
                                     "password TEXT NOT NULL, " +
                                     "security TEXT NOT NULL)";
                stmt.execute(createTable);
                // Insert a default user into the central database
                String insertUser = "INSERT INTO users (username, password, security) VALUES ('Paras', '12345', 'Pizza')";
                stmt.execute(insertUser);
                stmt.close();
                conn.close();
                System.out.println("Database created and default user inserted.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Main method as the application entry point
    public static void main(String[] args) {
        launch(args);
    }
}
