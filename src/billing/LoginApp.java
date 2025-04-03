package billing;
//by Prashant Rana(2300680140090),prashantrana422@gmail.com
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

    private static final String FOLDER_PATH = "C:\\Users\\user\\AppData\\Local\\AppUser";
    private static final String DB_PATH = FOLDER_PATH + File.separator + "user.db";

    @Override
    public void start(Stage primaryStage) {
        // Set up the folder and database if not already created
        setupDatabase();

        try {
             //Load the login UI (assumed to be defined in LoginDesign.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Scene scene = new Scene(loader.load());
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
            // Create folder if it doesn't exist
            File folder = new File(FOLDER_PATH);
            if (!folder.exists()) {
                folder.mkdirs();
                System.out.println("Created folder: " + FOLDER_PATH);
            }

            // Create the database file if it does not exist
            File dbFile = new File(DB_PATH);
            if (!dbFile.exists()) {
                Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
                Statement stmt = conn.createStatement();
                // Create a table to store users (if not exists)
                String createTable = "CREATE TABLE IF NOT EXISTS users (" +
                                     "username TEXT PRIMARY KEY, " +
                                     "password TEXT NOT NULL, "+
                                     "security TEXT NOT NULL)";
                stmt.execute(createTable);
                // Insert default user: Paras / 12345
                String insertUser = "INSERT INTO users (username, password,security) VALUES ('Paras', '12345', 'Pizza')";
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
