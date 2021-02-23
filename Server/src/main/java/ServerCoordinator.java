import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

public class ServerCoordinator extends Coordinator {
    public ServerCoordinator(ICommunicator communicator) {
        super(communicator);
    }

    private final String PING_MESSAGE = "CHECKALIVE";
    private final String PING_CONFIRM = "ALIVE";
    private final int TIMEOUT_SECONDS = 15;

    // Getting the coordinator's ip();
    String targetIP = communicator.getTargetIP();
    // Getting the coordinator's port()
    String targetPort = communicator.getTargetPort();

    Member coordinator;
    Scanner in ;
    PrintWriter out;
    ServerSocket listener;


    @Override
    public void run() {
        coordinator.setIP(targetIP);
        coordinator.setPort(targetPort);
        try {
            listener = new ServerSocket(59001);
            Socket socket = listener.accept();
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(PING_MESSAGE);
            Instant time1 = Instant.now();

            while (true) {
                if (in.hasNextLine() && in.nextLine().equals(PING_CONFIRM)) {
                    //Coordinator is still alive
                    break;
                } else if (Duration.between(time1, Instant.now()).getSeconds() > TIMEOUT_SECONDS) {
                    //Coordinator is not responding so we need a new one
                    try {
                        socket.close();
                        Member[] members = communicator.getMembers();
                        coordinator.setIP(members[0].getIP());
                        coordinator.setPort(members[0].getPort());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (!(listener.isClosed())) {
                try {
                    listener.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
