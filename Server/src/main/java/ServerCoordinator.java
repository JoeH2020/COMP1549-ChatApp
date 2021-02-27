import org.apache.maven.surefire.shade.org.apache.commons.lang3.ArrayUtils;

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

    // Getting the coordinator's ip
    String targetIP = communicator.getTargetIP();

    // Getting the coordinator's port
    String targetPort = communicator.getTargetPort();

    // Getting the coordinator's UID


    Member coordinator;
    Scanner in;
    PrintWriter out;
    ServerSocket listener;


    @Override
    public void run() {
        coordinator = new Member("3",targetIP,targetPort);
        try {
            listener = new ServerSocket(59001);
            Socket socket = getClientSocket(listener);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(PING_MESSAGE);
            Instant time1 = Instant.now();

            while (true) {
                if (in.hasNextLine() && in.nextLine().equals(PING_CONFIRM)) {
                    //Coordinator is still alive
                    System.out.println("Still alive.");
                    break;
                } else if (Duration.between(time1, Instant.now()).getSeconds() > TIMEOUT_SECONDS) {
                    //Coordinator is not responding so we need a new one
                    try {
                        socket.close();
                        //Printing old coordinator's info
                        System.out.println("Old coordinator is: " + coordinator.getUID()  + " (" + coordinator.getIP() + ":" + coordinator.getPort()+ ")" );
                        Member[] members = communicator.getMembers();

                        int i = 0;
                        boolean flag = false;
                        while (i < members.length) {
                            if (!members[i].getUID().equals(coordinator.getUID())) {
                                coordinator.setIP(members[i].getIP());
                                coordinator.setPort(members[i].getPort());
                                //Printing new coordinator's info
                                System.out.println("New coordinator is: " + coordinator.getUID()  + " (" + coordinator.getIP() + ":" + coordinator.getPort()+ ")" );
                                flag = true;
                                break;
                            }
                            else {
                                i++;
                            }}
                        //There are no users so no coordinator can be chosen.
                        if (!flag) {
                            System.out.println("Not enough users to pick another coordinator");
                        }
                        break;
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
    
    protected Socket getClientSocket(ServerSocket listener) {
        try {
            return listener.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}


