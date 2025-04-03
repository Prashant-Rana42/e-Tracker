package billing;
// by Prashant Rana(2300680140090), prashantrana422@gmail.com
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    static String localAppDataPath = System.getenv("LOCALAPPDATA");//gets the path to local folder of current user
    // Central user DB path (for checking/adding user credentials)
    private final String CENTRAL_DB_PATH = localAppDataPath + "\\AppUser\\user.db";
    // Folder where individual user account DBs will be stored
    private final String ACCOUNTS_FOLDER = localAppDataPath+"\\AppUser\\accounts";
    public static Connection connect() {
        if (CurrentUser.username == null || CurrentUser.username.isEmpty()) {
            System.out.println("Error: No user is logged in.");
            return null;
        }
        // Construct the dynamic database path using the logged-in username
        String dbPath = "jdbc:sqlite:"+localAppDataPath+"\\AppUser\\accounts\\" + CurrentUser.username + ".db";
        
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbPath);
            System.out.println("Connected to database: " + CurrentUser.username + ".db");
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
        return connection;
    }
}
