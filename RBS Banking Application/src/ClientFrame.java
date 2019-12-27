import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDate;

public class ClientFrame{
    private startScreenPanel startPanel = new startScreenPanel(); // panel to hold start screen with login and register
    private registerPanel registerPanel = new registerPanel(); // panel for registering user
    private loginPanel loginPanel = new loginPanel(); // panel for logging in user
    private mainPanel mainPanel = new mainPanel(); // panel for making transactions
    private transactionPanel transactionPanel = new transactionPanel(); // panel for displaying private/public transaction data
    private accountPanel accountPanel = new accountPanel(); // panel that shows different actions a user can take
    private accountInfoPanel accountInfoPanel = new accountInfoPanel(); // panel where user can set profile picture and change password
    private Socket client; // socket for client
    private ObjectOutputStream output; // output stream
    private ObjectInputStream input; // input stream
    private String chatServer; // the IP address of the server
    private String message = ""; // variable to read the server's message into
    private String userLogin; // the current user's username
    private JTextArea transactionField = new JTextArea(); // the text area to display transaction information
    private String password; // the user's password
    private static JLabel userLabel = new JLabel(); // the label to show the user's username

    private static JFrame frame = new JFrame("RBS Banking"); // frame for displaying panels

    ClientFrame(String host) { // takes in IP address of server
        // sets up background colors for the panels
        startPanel.setBackground(new Color(255, 182, 193));
        registerPanel.setBackground(new Color(255, 182, 193));
        loginPanel.setBackground(new Color(255, 182, 193));
        mainPanel.setBackground(new Color(255, 182, 193));
        transactionPanel.setBackground(new Color(255, 182, 193));
        accountPanel.setBackground(new Color(255, 182, 193));
        accountInfoPanel.setBackground(new Color(255, 182, 193));
        chatServer = host; // sets IP address variable
        frame.add(startPanel); // adds start panel to frame

        // next bit used to send client terminate message when x button is clicked to exit client window
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent event){
                exitProcedure();
            }
        });

        // more GUI setup
        frame.setSize(500,300);
        frame.setVisible(true);
    }

    // actions to take when the window is exited
    private void exitProcedure(){
        frame.dispose();
        sendData("clientTerminate#" + userLogin); // send message to server
        System.exit(0);
    }

    // panel to hold start screen buttons and logo
    private class startScreenPanel extends JPanel {
        private ImageIcon Picture = new ImageIcon("ZelleMoMe/RBSBanking.png");
        Image scaled = Picture.getImage();
        Image newimage = scaled.getScaledInstance(220, 220,  java.awt.Image.SCALE_SMOOTH);

        startScreenPanel(){
            setLayout(new BorderLayout());
            ImageIcon picturebetter = new ImageIcon(newimage);
            JLabel showPic = new JLabel(picturebetter);
            add(showPic, BorderLayout.CENTER);
            JButton loginButton = new JButton("Login");

            loginButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    // switch to login panel
                    frame.remove(startPanel);
                    frame.add(loginPanel);
                    frame.setSize(250,150);
                }
            });
            add(loginButton, BorderLayout.EAST);

            JButton registerButton = new JButton("Register");
            registerButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    // switch to register panel
                    frame.remove(startPanel);
                    frame.add(registerPanel);
                    frame.setSize(275, 250);
                }
            });
            add(registerButton, BorderLayout.WEST);
            setVisible(true);
        }
    }

    // the panel for registering a user
    private class registerPanel extends JPanel{
        private JTextField usernameField = new JTextField(10);
        private JPasswordField passwordField = new JPasswordField(10);
        private JPasswordField retypepasswordField = new JPasswordField(10);

        registerPanel(){
            // setting up GUI for panel
            JLabel usernameLabel = new JLabel("Username ");
            usernameLabel.setLabelFor(usernameField);
            add(usernameLabel);
            add(usernameField);
            JLabel passwordLabel = new JLabel("Password ");
            passwordLabel.setLabelFor(passwordField);
            add(passwordLabel);
            add(passwordField);
            JLabel retypepasswordLabel = new JLabel("Retype Password");
            add(retypepasswordLabel);
            add(retypepasswordField);

            JButton backButton = new JButton("BACK");
            backButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    // switch back to start panel
                    frame.remove(registerPanel);
                    frame.add(startPanel);
                    frame.setSize(500, 300);
                }
            });
            JButton submitButton = new JButton("SUBMIT");
            submitButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    String password1 = new String(passwordField.getPassword());
                    String password2 = new String(retypepasswordField.getPassword());

                    // do some verification then send username and password to server
                    if (usernameField.getText().equals("")){
                        JOptionPane.showMessageDialog(registerPanel, "Invalid username", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else if(password1.equals("")){
                        JOptionPane.showMessageDialog(registerPanel, "Invalid password", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else if(!password1.equals(password2)){
                        JOptionPane.showMessageDialog(registerPanel, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        userLogin = usernameField.getText();
                        String output = "register#" + usernameField.getText() + "#" + password1;
                        userLabel.setText("Username: " + userLogin);
                        sendData(output);
                    }
                }
            });
            add(backButton);
            add(submitButton);
            setVisible(true);
        }
    }

    // panel for logging in a user
    private class loginPanel extends JPanel{
        private JTextField usernameField = new JTextField(10);
        private JPasswordField passwordField = new JPasswordField(10);

        loginPanel(){
            // setting up GUI
            usernameField.setEditable(true);
            passwordField.setEditable(true);
            JLabel usernameLabel = new JLabel("Username ");
            usernameLabel.setLabelFor(usernameField);
            add(usernameLabel);
            add(usernameField);
            JLabel passwordLabel = new JLabel("Password ");
            passwordLabel.setLabelFor(passwordField);
            add(passwordLabel);
            add(passwordField);

            JButton backButton = new JButton("BACK");
            backButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    // switch back to start panel
                    frame.remove(loginPanel);
                    frame.add(startPanel);
                    frame.setSize(500,300);
                }
            });
            add(backButton);

            JButton submitButton = new JButton("SUBMIT");
            submitButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    password = new String(passwordField.getPassword());

                    // do some verification
                    if (usernameField.getText().equals("") || password.equals("")){
                        JOptionPane.showMessageDialog(loginPanel, "Invalid username/password", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else { // send username and password to user for database verification
                        password = new String(passwordField.getPassword());
                        userLogin = usernameField.getText();
                        String output = "login#" + usernameField.getText() + "#" + password;
                        userLabel.setText("Username: " + userLogin); // sets the username label for the account info page
                        sendData(output);
                    }
                    usernameField.setText("");
                    passwordField.setText("");
                }
            });
            add(submitButton);
            setVisible(true);
        }
    }

    // panel for making transactions
    private class mainPanel extends JPanel{
        // GUI components
        String[] privacyLevels = new String[]{"Private", "Public"};
        private JComboBox dropDown = new JComboBox(privacyLevels);
        private JTextField labelField = new JTextField(10);
        private JTextField amountField = new JTextField(10);
        private JTextField memoField = new JTextField(20);

        mainPanel(){
            // setting up GUI
            JLabel toLabel = new JLabel("To ");
            toLabel.setLabelFor(labelField);
            add(toLabel);
            add(labelField);

            JLabel amountLabel = new JLabel("Amount ");
            amountLabel.setLabelFor(amountField);
            add(amountLabel);
            add(amountField);

            JLabel memoLabel = new JLabel("Memo ");
            memoLabel.setLabelFor(memoField);
            add(memoLabel);
            add(memoField);

            JLabel privacyLabel = new JLabel("Privacy ");
            privacyLabel.setLabelFor(dropDown);
            add(privacyLabel);
            add(dropDown);

            JButton submitButton = new JButton("SUBMIT");

            submitButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    // sends data to server for input into database then clears fields
                    String output = "transaction#" + userLogin + "#" + labelField.getText() + "#" + memoField.getText() + "#" + LocalDate.now() + "#" + amountField.getText() + "#" + dropDown.getItemAt(dropDown.getSelectedIndex());
                    sendData(output);
                    labelField.setText("");
                    amountField.setText("");
                    memoField.setText("");
                }
            });

            JButton backButton = new JButton("BACK");
            backButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    // switches back to account panel
                    frame.remove(mainPanel);
                    frame.add(accountPanel);
                    frame.setSize(200,200);
                }
            });

            add(backButton);
            add(submitButton);
            setVisible(true);
        }
    }

    // panel for showing private/public transaction histories
    private class transactionPanel extends JPanel{
        String[] privacy = new String[]{"Private","Public"};
        private JComboBox<String> dropDown = new JComboBox(privacy);

        transactionPanel(){
            setLayout(new BorderLayout());
            JButton backButton = new JButton("BACK");

            backButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    // goes back to account panel
                    frame.remove(transactionPanel);
                    frame.add(accountPanel);
                    frame.setSize(200,200);
                    dropDown.setSelectedIndex(0);
                }
            });

            dropDown.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // gets whatever list of transactions is needed based on the value of the dropdown
                    String sendText = "get" + dropDown.getSelectedItem() + "List#" + userLogin;
                    sendData(sendText);
                }
            });
            // more setting up GUI
            add(backButton, BorderLayout.SOUTH);
            add(dropDown, BorderLayout.EAST);
            JLabel title = new JLabel("TRANSACTION HISTORY");
            add(title, BorderLayout.NORTH);
            setVisible(true);
        }
    }

    // panel for holding buttons for the actions a user can take
    private class accountPanel extends JPanel{
        accountPanel(){
            JButton accountInfo = new JButton("Account Info");
            accountInfo.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    // switches to the account info panel
                    frame.remove(accountPanel);
                    frame.add(accountInfoPanel);
                    frame.setSize(250,300);
                }
            });
            add(accountInfo);

            JButton makePayment = new JButton("Make Payment");
            makePayment.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    // switches to the making a transaction panel
                    frame.remove(accountPanel);
                    frame.add(mainPanel);
                    frame.setSize(850,100);
                }
            });
            add(makePayment);

            JButton transactionHistoryButton = new JButton("Transaction History");
            transactionHistoryButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    // switches to the the transaction history panel where the default is private transactions
                    sendData("getPrivateList#" + userLogin); // tells server to get the private transactions
                    frame.remove(accountPanel);
                    frame.add(transactionPanel);
                    frame.setSize(800,300);
                }
            });
            add(transactionHistoryButton);
            setVisible(true);
        }
    }

    //the accountInfoPanel class allows you to create a profile picture for your time on the app, or modify your password inside of the SQL Database
    private class accountInfoPanel extends JPanel{
        private JPasswordField inputPassword = new JPasswordField(20); //inputPassword allows the user to input a new password
        private JPasswordField inputPasswordAgain = new JPasswordField(20); // inputPasswordAgain allows you to retype your password to verify it is the same
        private JLabel imageHolder = new JLabel(); // imageHolder holds your current profile picture
        String password1;
        String password2;

        //accountInfoPanel constructs the basic account info screen displayed on the GUI
        accountInfoPanel(){
            JButton back = new JButton("Back");
            back.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // switches back to account panel
                    frame.remove(accountInfoPanel);
                    frame.add(accountPanel);
                    frame.setSize(200, 200);
                }
            });

            //Source used (but modified) https://1bestcsharp.blogspot.com/2015/04/java-how-to-browse-image-file-and-And-Display-It-Using-JFileChooser-In-Java.html
            JButton uploadImage = new JButton("Update Profile Picture");
            uploadImage.addActionListener(new ActionListener() {
                //updates profile picture
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser file = new JFileChooser();
                    file.setCurrentDirectory(new File(System.getProperty("user.home")));
                    //filter the files
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("*.Images", "jpg","gif","png");
                    file.addChoosableFileFilter(filter);
                    int result = file.showSaveDialog(null);
                    //if the user click on save in Jfilechooser
                    if(result == JFileChooser.APPROVE_OPTION){
                        File selectedFile = file.getSelectedFile();
                        String imageLoc = selectedFile.getAbsolutePath();
                        ImageIcon MyImage = new ImageIcon(imageLoc);
                        Image img = MyImage.getImage();
                        Image newImg = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                        ImageIcon image = new ImageIcon(newImg);
                        imageHolder.setIcon((image));
                    }
                }
            });

            JButton changePassword = new JButton("Change Password");
            changePassword.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    password1 = new String(inputPassword.getPassword());
                    password2 = new String(inputPasswordAgain.getPassword());
                    if(!inputPassword.getPassword().equals("")) { // provides some verification
                        if (password1.equals(password2)) { // more verification
                            String sendText = "changePassword#" + userLogin + "#" + password1;
                            sendData(sendText); // sends the password to the server to be updates
                        } else {
                            JOptionPane.showMessageDialog(loginPanel, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    else{
                        JOptionPane.showMessageDialog(loginPanel, "Please input password", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    inputPassword.setText("");
                    inputPasswordAgain.setText("");
                }
            });

            // more set up of GUI
            add(imageHolder);
            add(uploadImage);
            add(userLabel);
            JLabel newPassword = new JLabel("New Password: ");
            add(newPassword);
            add(inputPassword);
            JLabel retypePassword = new JLabel("Retype Password: ");
            add(retypePassword);
            add(inputPasswordAgain);
            add(changePassword);
            add(back);
        }
    }

    // handles running the client
    public void runClient() {
        try // connect to server, get streams, process connection
        {
            connectToServer(); // create a Socket to make connection
            getStreams(); // get the input and output streams
            processConnection(); // process connection
        } // end try
        catch (EOFException eofException) {
            System.out.println("Client terminated connection");
        } // end catch
        catch (IOException ioException) {
            ioException.printStackTrace();
        } // end catch
        finally {
            closeConnection(); // close connection
        } // end finally
    } // end method runClient

    // connect to server
    private void connectToServer() throws IOException {
        // create Socket to make connection to server
        client = new Socket(InetAddress.getByName(chatServer), 12345);
    } // end method connectToServer

    // get streams to send and receive data
    private void getStreams() throws IOException {
        // set up output stream for objects
        output = new ObjectOutputStream(client.getOutputStream());
        output.flush(); // flush output buffer to send header information

        // set up input stream for objects
        input = new ObjectInputStream(client.getInputStream());
    } // end method getStreams

    // process connection with server
    private void processConnection() throws IOException {
        do // process messages sent from server
        {
            try // read message and display it
            {
                message = (String) input.readObject(); // read new message
                // when registration is successful
                if(message.equals("registerSuccess")){
                    // maninpulate transaction panel so it will show the display area when navigated to
                    transactionField.setEditable(false);
                    transactionPanel.add(new JScrollPane(transactionField), BorderLayout.CENTER);

                    // switch to account panel after registering
                    frame.remove(registerPanel);
                    frame.add(accountPanel);
                    frame.setSize(200,200);
                }
                // display error if registration fails
                else if(message.equals("registerFail")){
                    JOptionPane.showMessageDialog(registerPanel, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
                }
                // display error if login fails
                else if(message.equals("loginFail")){
                    JOptionPane.showMessageDialog(loginPanel, "Username/Password incorrect", "Error", JOptionPane.ERROR_MESSAGE);
                }
                // when login is successful
                else if(message.equals("loginSuccess")){
                    // maninpulate transaction panel so it will show the display area when navigated to
                    transactionField.setEditable(false);
                    transactionPanel.add(new JScrollPane(transactionField), BorderLayout.CENTER);

                    // switch to account panel after logging in
                    frame.remove(loginPanel);
                    frame.add(accountPanel);
                    frame.setSize(200,200);
                }
                // when transactions fail show an error (for instance when To: user doesn't exist)
                else if(message.equals("transactionFail")) {
                    JOptionPane.showMessageDialog(mainPanel, "Transaction unsuccessful", "Error", JOptionPane.ERROR_MESSAGE);
                }
                // when server send back the public transaction history (yesterday and today)
                else if(message.substring(0,10).equals("publicList")) {
                    transactionField.setText(message.substring(11));
                }
                // when server sends back the user's own history whether private or public (all time)
                else if(message.substring(0,11).equals("privateList")) {
                    transactionField.setText(message.substring(12));
                }
            } // end try
            catch (ClassNotFoundException classNotFoundException) {
                System.out.println("Error in process connection");
            } // end catch

        } while (!message.equals("serverTerminate"));
    } // end method processConnection

    // close streams and socket
    private void closeConnection() {
        try {
            output.close(); // close output stream
            input.close(); // close input stream
            client.close(); // close socket
        } // end try
        catch (IOException ioException) {
            ioException.printStackTrace();
        } // end catch
    } // end method closeConnection

    // send message to server
    private void sendData(String message) {
        try // send object to server
        {
            output.writeObject(message);
            output.flush(); // flush data to output
        } // end try
        catch (IOException ioException) {
            System.out.println("Error writing object.");
        } // end catch
    } // end method sendData
}
