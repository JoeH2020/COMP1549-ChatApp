import org.apache.maven.settings.Server;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;

public class ServerCoordinatorTest {

    @Mock
    static ServerCommunicator serverCommunicator;

    @Mock
    static ServerSocket serverSocket;
    static Socket socket;

    static ServerCoordinator serverCoordinator;

    static final String PING_CONFIRM = "ALIVE";
    static final String PING_MESSAGE = "CHECKALIVE";
    static final String TIMEOUT_MESSAGE = "TIMEOUT";

    static Member[] members;

    static ByteArrayOutputStream serverOutputStream = new ByteArrayOutputStream();

    @BeforeAll
    public static void beforeAll() throws IOException {

        serverCommunicator = Mockito.mock(ServerCommunicator.class);

        //Creating an array with two 'fake' members.
        members = new Member[3];
        members[0] = new Member("3", "192.168.0.3", "4300");
        members[1] = new Member("2", "192.168.0.2", "4300");
        members[2] = new Member("1", "192.168.0.1", "4300");

        //Creating mock methods to return correct values which in this case
        // are the coordinator's ip and port.
        when(serverCommunicator.getTargetIP()).thenReturn("192.168.0.3");
        when(serverCommunicator.getTargetPort()).thenReturn("4300");
        when(serverCommunicator.getMembers()).thenReturn(members);

        ServerSocket serverSocket = Mockito.mock(ServerSocket.class);
        Socket socket = Mockito.mock(Socket.class);

        serverCoordinator = new ServerCoordinator(serverCommunicator) {
            @Override
            protected Socket getClientSocket(ServerSocket listener) {return socket;}

            };
        when(socket.getOutputStream()).thenReturn(serverOutputStream);

    }



    @BeforeEach
    public void beforeEach() {
        // empty the serverOutputStream
        serverOutputStream.reset();
    }

    @Test
    public void coordinatorTimedOut() {
        socket = serverCoordinator.getClientSocket(serverSocket);
        ByteArrayInputStream coordinatorInputStream = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream coordinatorOutputStream = new ByteArrayOutputStream();

        try {
            when(socket.getInputStream()).thenReturn(coordinatorInputStream);
            when(socket.getOutputStream()).thenReturn(coordinatorOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        serverCoordinator.run();
    }

    @Test
    public void coordinatorStillAlive() {
        socket = serverCoordinator.getClientSocket(serverSocket);
        ByteArrayInputStream coordinatorInputStream = new ByteArrayInputStream((PING_CONFIRM).getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream coordinatorOutputStream = new ByteArrayOutputStream();

        try {
            when(socket.getInputStream()).thenReturn(coordinatorInputStream);
            when(socket.getOutputStream()).thenReturn(coordinatorOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        serverCoordinator.run();
    }
}
