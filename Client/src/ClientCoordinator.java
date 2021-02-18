import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

public class ClientCoordinator extends Coordinator {

    private final String PING_MESSAGE = "CHECKALIVE";
    private final String PING_CONFIRM = "ALIVE";
    private final int TIMEOUT_SECONDS = 15;
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
            try (Socket socket = new Socket(m.getIP(), Integer.parseInt(m.getPort()));) {
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
                    }
                }


            } catch (IOException e) {
                // this member has timed out
                e.printStackTrace();
            }
        }

        // check if they're alive
    }

    private PrintWriter getServerPrintWriter(String IP, String port) throws IOException {
        Socket socket = new Socket(IP, Integer.parseInt(port));
        return new PrintWriter(socket.getOutputStream(), true);
    }
}
