import java.sql.SQLException;

// run this class to start up a server
public class ServerDriver {
    public static void main(String[] args) throws SQLException {
        ServerFrame application = new ServerFrame(); // intializes server class
        application.runServer(); // runs the server
    }
}