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
    public void cyclicalCheckAliveTest() throws InterruptedException {
        // ping interval is 10 seconds, let's check that the sendMessage is called every 10 seconds
        // with the message CHECKALIVE

        // first run the coordinator
        clientCoordinator.start();

        // then wait the 10 seconds
        Thread.sleep(10 * 1000);

        // now let's check
        Mockito.verify(clientCommunicator, Mockito.times(1)).sendMessage("CHECKALIVE");

        // let's wait another 10 seconds and check again
        Thread.sleep(10 * 1000);

        // this time it should have been called twice (previous time and now)
        Mockito.verify(clientCommunicator, Mockito.times(1)).sendMessage("CHECKALIVE");
    }

    @Test
    public void everythingFine() throws InterruptedException {
        // let's test what happens if two members are alive and well

        // first let's make a fake list with two members
        Member m1 = new Member("Steve", "128.123.123.123", "56001");
        Member m2 = new Member("Paul", "123.13.123.123", "56001");
        Member[] members = {m1, m2};

        // now let's tell clientCommunicator to return this list when requested
        when(clientCommunicator.getMembers()).thenReturn(members);

        // now run the coordinator
        clientCoordinator.start();

        // let's wait 11 seconds, then tell the coordinator both members are alive
        Thread.sleep(11 * 1000);
        clientCoordinator.confirmedAlive(m1.getUID());
        clientCoordinator.confirmedAlive(m2.getUID());

        // now let's make sure that the clientCommunicator.sendMessage method
        // was never called to communicate a timeout
        Mockito.verify(clientCommunicator, Mockito.times(0)).sendMessage("TIMEOUT" + m1.getUID());
        Mockito.verify(clientCommunicator, Mockito.times(0)).sendMessage("TIMEOUT" + m2.getUID());
    }

    @Test
    public void oneMemberHasTimedOut() throws InterruptedException {
        // let's test what happens if one of the members times out

        // first let's make a fake list with two members
        Member m1 = new Member("Steve", "128.123.123.123", "56001");
        Member m2 = new Member("Paul", "123.13.123.123", "56001");
        Member[] members = {m1, m2};

        // now let's tell clientCommunicator to return this list when requested
        when(clientCommunicator.getMembers()).thenReturn(members);

        // now run the coordinator
        clientCoordinator.start();

        // let's wait 11 seconds, then tell the coordinator only m1 is alive
        // then wait 5 more seconds to make sure m2 times out
        Thread.sleep(11 * 1000);
        clientCoordinator.confirmedAlive(m1.getUID());
        Thread.sleep(5 * 1000);

        // now let's make sure that the clientCommunicator.sendMessage method
        // was called once and only once for m2
        Mockito.verify(clientCommunicator, Mockito.times(0)).sendMessage("TIMEOUT" + m1.getUID());
        Mockito.verify(clientCommunicator, Mockito.times(1)).sendMessage("TIMEOUT" + m2.getUID());
    }

    @Test
    public void bothMembersHaveTimedOut() throws InterruptedException {
        // let's test what happens if both members time out

        // first let's make a fake list with two members
        Member m1 = new Member("Steve", "128.123.123.123", "56001");
        Member m2 = new Member("Paul", "123.13.123.123", "56001");
        Member[] members = {m1, m2};

        // now let's tell clientCommunicator to return this list when requested
        when(clientCommunicator.getMembers()).thenReturn(members);

        // now run the coordinator
        clientCoordinator.start();

        // let's wait 16 seconds, enough time for the first request to go out and for both members to time out
        Thread.sleep(16 * 1000);

        // now let's make sure that the clientCommunicator.sendMessage method
        // was called once and only once for each of the two members
        Mockito.verify(clientCommunicator, Mockito.times(1)).sendMessage("TIMEOUT" + m1.getUID());
        Mockito.verify(clientCommunicator, Mockito.times(1)).sendMessage("TIMEOUT" + m2.getUID());

    }

}
