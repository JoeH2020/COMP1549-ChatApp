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

public class ServerCommunicatorTest {

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
        members[0] = new Member("John", "192.168.0.3", "4300");
        members[1] = new Member("Mike", "192.168.0.2", "4300");
        members[2] = new Member("Peter", "192.168.0.1", "4300");

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
    public void joiningRoom() {
        //Test joining the chat room

    }

    @Test
    public void leavingRoom() {
        //Test when a person wants to leave the chat room

    }

    @Test
    public void messageSent() {
        //Test that a person can actually send a message

    }
}
