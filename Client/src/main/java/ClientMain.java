import java.io.IOException;
import java.net.ConnectException;

public class ClientMain {

    // run this method to start the client
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Please specify your nickname, and the server's IP and port.");
            return;
        }

        String userID = args[0];
        String serverIP = args[1];
        String serverPort = args[2];

        ClientCommunicator client = new ClientCommunicator(userID, "127.0.0.1", "59001", serverIP, serverPort);
        try {
            client.openSession();
        } catch (ConnectException e) {
            System.out.println("No server found with specified IP and port.");
            System.exit(0);
        }
    }
}
