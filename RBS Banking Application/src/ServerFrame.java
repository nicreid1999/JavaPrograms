import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerFrame{
    private startPanel startPanel = new startPanel(); // panel to hold start menu
    private ConnectedUserPanel connectedUserPanel; // panel that shows currently connected users
    private TransactionsPanel transactionsPanel; // panel that shows the public transactions
    private ExecutorService executor; // used to execute threads
    private ServerSocket server; // socket for the server
    private ArrayList<MultiThreadWorker> connections = new ArrayList<>(); // holds the different threads
    private int counter = 0; // used to access the array above
    private Connection database; // the database
    private ArrayList<String> connectedUsers = new ArrayList<>(); // list of usernames of people connected
    private JTextArea displayArea = new JTextArea(); // display area used in connected users and transaction history panels
    private static JFrame frame = new JFrame("RBS Banking Server"); // title for the server frame

    ServerFrame() throws SQLException {
        executor = Executors.newCachedThreadPool(); // sets up thread executor

        // sets up the database connection
        database = DriverManager.getConnection(
                "jdbc:mysql://s-l112.engr.uiowa.edu:3306/engr_class005", "engr_class005", "engr_class005-xyz");

        // sets up the start GUI and the frame that is used to hold everything server GUI related
        startPanel.setBackground(new Color(255,182,193));
        frame.add(startPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(200, 200);
        frame.setVisible(true);
    }

    // method used to set up the server socket and the threads
    public void runServer() {
        try {
            // sets up server on port 12345
            server = new ServerSocket(12345, 100);

            while (true) {
                try {
                    //execute a thread if there is a connection
                    connections.add(new MultiThreadWorker());
                    connections.get(counter).waitForConnection();
                    executor.execute(connections.get(counter));
                } catch (EOFException e) {
                    displayMessage("\nServer terminated connection.");
                } finally {
                    counter++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // used to put a message into the display area text area
    private void displayMessage(String messageToDisplay) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        displayArea.setText(messageToDisplay);
                    }
                }
        );
    }

    // runnable class to make our application multithreaded
    private class MultiThreadWorker implements Runnable {
        private ObjectOutputStream output; // output stream
        private ObjectInputStream input; // input stream
        private Socket connection; // connection socket

        @Override
        public void run() {
            try {
                try {
                    getStreams(); // get input and output streams
                    processConnection(); // run the process connection method to handle messages from client
                } catch (EOFException eofException) {
                    System.out.println("Error in run.");
                } finally {
                    closeConnection();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        // gets the input and output streams
        private void getStreams() throws IOException {
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
        }

        // waits for a connection then accepts if there is one on the connection socket
        private void waitForConnection() throws IOException {
            connection = server.accept();
        }

        // method that handles all the logic for messages from the client
        private void processConnection() {
            StringBuilder message = new StringBuilder(); // message that will get sent back to the client
            String in = "Valid operation not found.";
            do {
                try {
                    message.delete(0, message.length()); // clear it out each time
                    in = (String) input.readObject(); // read in client's message from input stream
                    String[] input = in.split("#"); // split the message into its pieces
                    if (!input[0].equals("clientTerminate")) { // only check for other messages if the client hasn't terminated
                        // gets the private transaction history
                        if (input[0].equals("getPrivateList")) {
                            message.append(getPrivateHist(input));
                        }
                        // gets the public transaction history
                        else if (input[0].equals("getPublicList")) {
                            message.append("publicList#");
                            message.append(getPublicHist());
                        }
                        // calls function to log user in
                        else if (input[0].equals("login")) {
                            message.append(userLogin(input));
                        }
                        // calls function to register user
                        else if (input[0].equals("register")) {
                            message.append(registerUser(input));
                        }
                        // calls method to handle transactions
                        else if (input[0].equals("transaction")) {
                            message.append(transaction(input));
                        }
                        // calls method to change the user's password
                        else if (input[0].equals("changePassword")) {
                            message.append(changePassword(input));
                        }
                    } else {
                        //send terminate messages and take username off connected users list
                        message.append("serverTerminate");
                        in = "clientTerminate";
                        connectedUsers.remove(input[1]);
                    }
                } catch(IOException | ClassNotFoundException | SQLException e){
                    e.printStackTrace();
                }
                sendData(message.toString()); // send the message back to the client
            }
            while (!in.equals("clientTerminate")) ;
        }

        //checks if a username currently exists in the system
        private boolean checkUsername(String username) throws SQLException {
            PreparedStatement ps;
            ResultSet rs;
            boolean checkUser = false;
            String query = "SELECT Username FROM Users WHERE Username = ?";
            ps = database.prepareStatement(query);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (!rs.next()) {
                checkUser = true;
            }
            return checkUser;
        }

        // gets the private history
        private String getPrivateHist(String[] input) {
            StringBuilder message = new StringBuilder();
            PreparedStatement ps;
            ResultSet rs;

            String query = "SELECT * FROM Transactions WHERE Sender=? OR Receiver=?";

            try {
                message.append("privateList#");
                ps = database.prepareStatement(query);
                ps.setString(1, input[1]);
                ps.setString(2, input[1]);
                rs = ps.executeQuery();

                // formats the records to how we wanted them to look then append them to the message
                String disText = "";
                while (rs.next()) {
                    disText = rs.getObject(1).toString() + " paid " +
                            rs.getObject(2).toString() + " $" +
                            rs.getObject(5).toString() + ": " +
                            rs.getObject(3).toString() + "\n";
                    message.append(disText);
                }
            }catch (SQLException e) {
                e.printStackTrace();
            }
            return message.toString();
        }

        // performs the actions for logging in a user
        private String userLogin(String[] input) {
            StringBuilder message = new StringBuilder();
            PreparedStatement ps;
            ResultSet rs;

            String query = "SELECT * FROM Users WHERE Username = ? AND Password = ?";

            try {
                // queries the database to see if the username and password are in the database
                ps = database.prepareStatement(query);
                ps.setString(1, input[1]);
                ps.setString(2, cryptWithMD5(input[2]));
                rs = ps.executeQuery();
                if (rs.next()) { // successful login
                    message.append("loginSuccess");
                    connectedUsers.add(input[1]); // add user to list of connected users
                }
                else { // login failure
                    message.append("loginFail");
                }
            } catch (SQLException ex) {
                System.out.println("Error with SQL");
            }
            return message.toString();
        }

        // registers a user with the database
        private String registerUser(String[] input) {
            StringBuilder message = new StringBuilder();
            try {
                if (!checkUsername(input[1])) { // first checks to make sure user doesn't already exist
                    message.append("registerFail");
                } else {
                    // inserts user's username and encrypted password into the database
                    PreparedStatement ps;
                    String query = "INSERT INTO Users(Username, Password) VALUES (?, ?)";
                    try {
                        ps = database.prepareStatement(query);
                        ps.setString(1, input[1]);
                        ps.setString(2, cryptWithMD5(input[2]));
                        ps.executeUpdate();
                        message.append("registerSuccess");
                        connectedUsers.add(input[1]); // adds user to list of connected users
                    } catch (SQLException ex) {
                        System.out.println("Error registering user.");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Error checking user.");
            }
            return message.toString();
        }

        // updates transaction table in database with new transaction
        private String transaction(String[] input) throws SQLException {
            StringBuilder message = new StringBuilder();
            if (!checkUsername(input[1])) {
                if (!checkUsername(input[2])) { // checks that the user transaction is sent to is an existing user
                    // inserts transaction into the database
                    PreparedStatement ps;
                    String query = "INSERT INTO Transactions(Sender, Receiver, Memo, Date, Amount, Private) VALUES (?,?,?,?,?,?)";

                    try {
                        ps = database.prepareStatement(query);
                        ps.setString(1, input[1]);
                        ps.setString(2, input[2]);
                        ps.setString(3, input[3]);
                        ps.setString(4, input[4]);
                        ps.setString(5, input[5]);
                        ps.setString(6, input[6]);
                        ps.executeUpdate();
                    } catch (SQLException ex) {
                        System.out.println("Error writing transaction.");
                    }
                    String sendText = input[1] + " paid " + input[2] + " $" + input[5] + ": " + input[3] + "\n";
                    message.append("transactionSuccess#").append(sendText);
                } else { // user receiving payment does not exist
                    message.append("transactionFail");
                }
            }
            else {
                message.append("transactionFail");
            }
            return message.toString();
        }

        // changes the user's password in the database
        private String changePassword(String[] input) {
            StringBuilder message = new StringBuilder();
            PreparedStatement ps;
            String query;

            //first delete the old username and password row
            query = "DELETE FROM Users WHERE Username = ?";
            try {
                ps = database.prepareStatement(query);
                ps.setString(1, input[1]);
                ps.executeUpdate();
            } catch (SQLException ex) {
                System.out.println("Error deleting row.");
            }

            //insert new encrypted password with username back into user database table
            query = "INSERT INTO Users(Username, Password) VALUES (?, ?)";
            try {
                ps = database.prepareStatement(query);
                ps.setString(1, input[1]);
                ps.setString(2, cryptWithMD5(input[2]));
                ps.executeUpdate();
            } catch (SQLException ex) {
                System.out.println("Error changing password in database.");
            }
            message.append("changePasswordSuccess");
            return message.toString();
        }

        // handles closing the connection
        private void closeConnection () {
            try {
                output.close();
                input.close();
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // sends the data to the output stream so the client will be able to read the message on their side
        private void sendData (String message){
            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException ioException) {
                displayMessage("\nError writing object");
            }
        }
    }

    // panel to hold the main buttons for the server GUI
    private class startPanel extends JPanel {
        startPanel() {
            setLayout(new FlowLayout());

            JButton connectedUsers = new JButton("Connected Users");
            connectedUsers.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.remove(startPanel); // remove start panel
                    connectedUserPanel = new ConnectedUserPanel();
                    connectedUserPanel.setBackground(new Color(255,182,193));
                    connectedUserPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);
                    displayMessage(getConnected()); // update display area with connected users information
                    frame.add(connectedUserPanel); // add connected users panel
                    frame.setSize(400, 300);
                    frame.revalidate();
                }
            });
            add(connectedUsers);

            JButton tHistory = new JButton("Public Transactions");
            tHistory.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.remove(startPanel); // remove start panel from frame
                    transactionsPanel = new TransactionsPanel();
                    transactionsPanel.setBackground(new Color(255,182,193));
                    transactionsPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);
                    displayMessage(getPublicHist()); // updates display area with public transaction history
                    frame.add(transactionsPanel); // add transaction panel to frame GUI
                    frame.setSize(400,300);
                    frame.revalidate();
                }
            });
            add(tHistory);
        }
    }

    // panel to hold connected users GUI
    private class ConnectedUserPanel extends JPanel {
        ConnectedUserPanel() {
            setLayout(new BorderLayout());
            JButton backButton = new JButton("BACK");
            backButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.remove(connectedUserPanel); // remove connected users panel
                    frame.add(startPanel); // add the start panel again
                    frame.setSize(200, 200);
                }
            });
            add(backButton, BorderLayout.SOUTH);
            JLabel title = new JLabel("Currently Connected Users");
            add(title, BorderLayout.NORTH);

            setVisible(true);
        }
    }

    // panel to hold public transactions GUI
    private class TransactionsPanel extends JPanel {
        TransactionsPanel() {
            setLayout(new BorderLayout());
            JButton backButton = new JButton("BACK");
            backButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.remove(transactionsPanel); // remove transaction panel
                    frame.add(startPanel); // add start panel back again
                    frame.setSize(200, 200);
                }
            });
            add(backButton, BorderLayout.SOUTH);
            JLabel title = new JLabel("Public Transactions");
            add(title, BorderLayout.NORTH);

            setVisible(true);
        }
    }

    // gets the public transaction history from the last day in the database
    private String getPublicHist() {
        StringBuilder message = new StringBuilder();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = LocalDate.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth()-1);

        PreparedStatement ps;
        ResultSet rs;

        String query = "SELECT * FROM Transactions WHERE Private='Public' AND Date=? OR Date=?"; // gets transactions from yesterday and today

        try {
            ps = database.prepareStatement(query);
            ps.setString(1, yesterday.toString());
            ps.setString(2, today.toString());
            rs = ps.executeQuery();

            String disText;
            // formats them to be displayed
            while (rs.next()) {
                if (rs.getObject(6).toString().equals("Public")) {
                    disText = rs.getObject(1).toString() + " paid " +
                            rs.getObject(2).toString() + ": " +
                            rs.getObject(3).toString() + "\n";
                    message.append(disText);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return message.toString();
    }

    // gets the users that are currently connected to the user
    private String getConnected() {
        StringBuilder message = new StringBuilder();
        for (String user: connectedUsers)
            message.append(user).append("\n");
        return message.toString();
    }

    //Encryption Function: https://stackoverflow.com/questions/10696432/encryption-of-password-in-java-or-mysql
    //encrypts all passwords inputted into the database as to not allow sql injection
    private static String cryptWithMD5(String pass){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] passBytes = pass.getBytes();
            md.reset();
            byte[] digested = md.digest(passBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digested) {
                sb.append(Integer.toHexString(0xff & b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}

