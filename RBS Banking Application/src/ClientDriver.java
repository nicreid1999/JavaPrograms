// run this class to start up a client
public class ClientDriver {
    public static void main(String[] args) {
        ClientFrame frame = new ClientFrame("127.0.0.1"); // creates a client that will connect to the localhost IP address
        frame.runClient(); // runs the client
    }
}
