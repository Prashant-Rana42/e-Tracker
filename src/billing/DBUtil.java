package billing;
//by Prashant Rana(2300680140090),prashantrana422@gmail.com
import java.sql.Connection;
import java.sql.DriverManager;

public class DBUtil {
    /**
     * Returns a connection to the currently logged-in user's database.
     */
    String localAppDataPath = System.getenv("LOCALAPPDATA");//gets the path to local folder of current user
    // Central user DB path (for checking/adding user credentials)
    private final String CENTRAL_DB_PATH = localAppDataPath + "\\AppUser\\user.db";
    // Folder where individual user account DBs will be stored
    private final String ACCOUNTS_FOLDER = localAppDataPath+"\\AppUser\\accounts";
    public  Connection getUserConnection() {
        try {
            
            String user = CurrentUser.username;
            // Construct the path to the user's database file.
            String userDbPath = localAppDataPath+"\\AppUser\\accounts\\" + user + ".db";
            return DriverManager.getConnection("jdbc:sqlite:" + userDbPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * You can also call this method manually if you need to connect to a specific user's DB.
     */
    public Connection getUserConnection(String username) {
        try {
            String userDbPath = localAppDataPath+"\\AppUser\\accounts\\" + username + ".db";
            return DriverManager.getConnection("jdbc:sqlite:" + userDbPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
