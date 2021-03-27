import java.io.IOException;

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
        client.openSession();
    }
}
