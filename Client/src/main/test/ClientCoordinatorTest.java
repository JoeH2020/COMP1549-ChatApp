import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class ClientCoordinatorTest {

    /*
    Please refer to the membersSendOtherMessagesBeforeConfirming method for some comments on how all the tests
    roughly work.
     */

    @Mock
    static ClientCommunicator clientCommunicator;

    @Mock
    static Socket socket;

    static ClientCoordinator clientCoordinator;

    static final String PING_CONFIRM = "ALIVE";
    static final String PING_MESSAGE = "CHECKALIVE";
    static final String TIMEOUT_MESSAGE = "TIMEOUT";

    static Member[] members;

    static ByteArrayOutputStream serverOutputStream = new ByteArrayOutputStream();

    @BeforeAll
    public static void beforeAll() {
        // set up the clientCommunicator mock
        clientCommunicator = Mockito.mock(ClientCommunicator.class);

        // first set up an array of fake members
        members = new Member[2];

        members[0] = new Member("1", "192.168.0.1", "4300");
        members[1] = new Member("2", "192.168.0.2", "4300");

        // now configure the communicator methods
        when(clientCommunicator.getMembers()).thenReturn(members);

        when(clientCommunicator.getTargetIP()).thenReturn("192.168.0.255");
        when(clientCommunicator.getTargetPort()).thenReturn("4300");

        // Override the ClientCoordinator getSocket method with one that returns the mock socket
        socket = Mockito.mock(Socket.class);

        // the socket mock will be configured in each test case
        clientCoordinator = new ClientCoordinator(clientCommunicator) {
            @Override
            protected Socket getSocket(String IP, String port) {
                return socket;
            }

            // overriding this method allows us to read what the ClientCoordinator sends to the server
            @Override
            protected PrintWriter getServerPrintWriter(String IP, String port) {
                return new PrintWriter(serverOutputStream, true);
            }
        };
    }

    @BeforeEach
    public void beforeEach() {
        // empty the serverOutputStream
        serverOutputStream.reset();
    }

    @Test
    public void membersSendOtherMessagesBeforeConfirming() {
        // these Input/Output streams allow us to write/read what the ClientCoordinator read/writes from the socket
        ByteArrayInputStream member1InputStream = new ByteArrayInputStream(
                ("SOME RANDOM STUFF\n" + PING_CONFIRM).getBytes());
        ByteArrayInputStream member2InputStream = new ByteArrayInputStream(
                ("OTHER RANDOM STUFF\n" + PING_CONFIRM).getBytes());
        ByteArrayOutputStream member1OutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream member2OutputStream = new ByteArrayOutputStream();

        // here the mock sockets is configured to return the above streams, whereas the normal socket would return
        // the proper streams
        try {
            when(socket.getInputStream()).thenReturn(member1InputStream, member2InputStream);
            when(socket.getOutputStream()).thenReturn(member1OutputStream, member2OutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        clientCoordinator.run();

        // there should be no timeout messages sent to the server
        assertEquals("", serverOutputStream.toString());
    }

    @Test
    public void everythingFine() {
        ByteArrayInputStream member1InputStream = new ByteArrayInputStream(PING_CONFIRM.getBytes());
        ByteArrayInputStream member2InputStream = new ByteArrayInputStream(PING_CONFIRM.getBytes());
        ByteArrayOutputStream member1OutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream member2OutputStream = new ByteArrayOutputStream();

        try {
            when(socket.getInputStream()).thenReturn(member1InputStream, member2InputStream);
            when(socket.getOutputStream()).thenReturn(member1OutputStream, member2OutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        clientCoordinator.run();

        assertEquals("", serverOutputStream.toString());
    }

    @Test
    public void oneMemberHasTimedOut() {
        ByteArrayInputStream member1InputStream = new ByteArrayInputStream(new byte[0]);
        ByteArrayInputStream member2InputStream = new ByteArrayInputStream(PING_CONFIRM.getBytes());
        ByteArrayOutputStream member1OutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream member2OutputStream = new ByteArrayOutputStream();

        try {
            when(socket.getInputStream()).thenReturn(member1InputStream, member2InputStream);
            when(socket.getOutputStream()).thenReturn(member1OutputStream, member2OutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        clientCoordinator.run();

        String serverOutput = serverOutputStream.toString();

        assertEquals(TIMEOUT_MESSAGE + members[0].getUID(), serverOutput.strip());
    }

    @Test
    public void bothMembersHaveTimedOut() {
        ByteArrayInputStream member1InputStream = new ByteArrayInputStream(new byte[0]);
        ByteArrayInputStream member2InputStream = new ByteArrayInputStream("Interrupted messa".getBytes());
        ByteArrayOutputStream member1OutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream member2OutputStream = new ByteArrayOutputStream();

        try {
            when(socket.getInputStream()).thenReturn(member1InputStream, member2InputStream);
            when(socket.getOutputStream()).thenReturn(member1OutputStream, member2OutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        clientCoordinator.run();

        String serverOutput = serverOutputStream.toString();

        assertEquals(TIMEOUT_MESSAGE + members[0].getUID() + "\n" + TIMEOUT_MESSAGE + members[1].getUID(),
                replaceNewLinesWithUnixNewlines(serverOutput.strip()));
    }

    private String replaceNewLinesWithUnixNewlines(String in) {
        in.replaceAll("\r\n", "\n");
        return in;
    }

}
