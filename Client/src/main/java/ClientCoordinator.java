import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

public class ClientCoordinator extends Coordinator {

    private final String PING_MESSAGE = "CHECKALIVE";
    private final String PING_CONFIRM = "ALIVE";
    private final int TIMEOUT_SECONDS = 5;
    private final String TIMEOUT_MESSAGE = "TIMEOUT";

    public ClientCoordinator(ICommunicator communicator) {
        super(communicator);
    }

    @Override
    public void run() {
        // get all the members from the Communicator
        Member[] members = communicator.getMembers();

        // connect to all members
        for (Member m: members) {
            // open the relevant socket
            try (Socket socket = getSocket(m.getIP(), m.getPort())) {
                // obtain the relevant IO classes
                Scanner in = new Scanner(socket.getInputStream());
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // send out the message
                out.println(PING_MESSAGE);

                // record the current time so we know when to time out
                Instant start = Instant.now();

                // try to get an answer for as long as possible
                while(true) {
                    if(in.hasNextLine() && in.nextLine().equals(PING_CONFIRM)) {
                        // everything is fine
                        break;
                    } else if (Duration.between(start, Instant.now()).getSeconds() > TIMEOUT_SECONDS) {
                        // it's been more than the timeout allows, time to tell the server this client is dead
                        PrintWriter serverPrintWriter = getServerPrintWriter(communicator.getTargetIP(),
                                communicator.getTargetPort());

                        // send a timeout message to the server with the user's id
                        serverPrintWriter.println(TIMEOUT_MESSAGE + m.getUID());
                        break;
                    }
                }


            } catch (IOException e) {
                // this member has timed out
                e.printStackTrace();
            }
        }
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
