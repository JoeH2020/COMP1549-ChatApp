import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class ClientCoordinator extends Coordinator {

    private final String PING_MESSAGE = "CHECKALIVE";
    private final String PING_CONFIRM = "ALIVE";
    private final int TIMEOUT_SECONDS = 5;
    private final String TIMEOUT_MESSAGE = "TIMEOUT";
    private final int PING_INTERVAL = 10;
    private final String DISCONNECT_MESSAGE = "DISCONNECTED";

    private ArrayList<String> aliveIds = new ArrayList<>();
    private Member[] allMembers;

    public ClientCoordinator(ClientCommunicator communicator) {
        super(communicator);
        System.out.println("This client is now the coordinator.");
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(PING_INTERVAL * 1000);
            } catch (InterruptedException e) { }
            // reset the arrayList of people that have confirmed to be alive
            aliveIds.clear();
            allMembers = communicator.getMembers();
            communicator.sendMessage(PING_MESSAGE);
            try {
                Thread.sleep(TIMEOUT_SECONDS * 1000);
            } catch (InterruptedException e) {}
            for (Member m : allMembers) {
                if (!aliveIds.contains(m.getUID())) {
                    // if the member did not return a ping, they timed out
                    communicator.sendMessage("TIMEOUT" + m.getUID());
                }
            }
        }
    }


    public void sendDisconnect(String id) {
        communicator.sendMessage("DISCONNECTED" + id);
    }

    public void confirmedAlive(String id) {
        aliveIds.add(id);
    }

    protected PrintWriter getServerPrintWriter(String IP, String port) throws IOException {
        Socket socket = getSocket(IP, port);
        return new PrintWriter(socket.getOutputStream(), true);
    }


    // We need this method so we can mock the socket
    protected Socket getSocket(String IP, String port) throws IOException {
        return new Socket(IP, Integer.parseInt(port));
    }
}
