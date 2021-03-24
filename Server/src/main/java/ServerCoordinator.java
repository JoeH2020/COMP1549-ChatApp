import org.apache.maven.settings.Server;
import org.apache.maven.surefire.shade.org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

public class ServerCoordinator extends Coordinator {
    public ServerCoordinator(ServerThread communicator) {
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

    boolean isOnline = false;
    boolean isConnected = false;

    private ServerSingleton serverSingleton = ServerSingleton.getInstance();

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
            System.out.println("Checking on coordinator...");
            isOnline = false;
            communicator.sendMessage("CHECKALIVE");
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
            }
            if (!isOnline) {
                System.out.println("Coordinator has timed out");

                // tell everyone the coordinator has timed out
                serverSingleton.broadcast("TIMEOUT" + communicator.getSelf().getUID());

                // select a new coordinator
                serverSingleton.selectNewCoordinator();
            }

        }
    }

    public void setThread(ServerThread c) {
        this.communicator = c;
    }

    public void positiveResponse() {
        System.out.println("Positive response from coordinator");
        isOnline = true;
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


