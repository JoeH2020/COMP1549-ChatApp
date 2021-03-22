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

    @Mock
    static ClientCommunicator clientCommunicator;

    static final String PING_MESSAGE = "CHECKALIVE";
    static final String TIMEOUT_MESSAGE = "TIMEOUT";

    @BeforeEach
    public void beforeEach() {
        // reset the mock clientCommunicator
        clientCommunicator = Mockito.mock(ClientCommunicator.class);
    }

    @Test
    public void cyclicalCheckAliveTest() throws InterruptedException {
        // ping interval is 10 seconds, let's check that the sendMessage is called every 10 seconds
        // with the message CHECKALIVE

        // set up the clientCommunicator to return an empty array
        when(clientCommunicator.getMembers()).thenReturn(new Member[0]);

        // run the coordinator
        ClientCoordinator clientCoordinator = new ClientCoordinator(clientCommunicator);
        clientCoordinator.start();

        // then wait the 11 seconds
        Thread.sleep(11 * 1000);

        // now let's check
        Mockito.verify(clientCommunicator, Mockito.times(1)).sendMessage(PING_MESSAGE);

        // let's wait another 10 seconds and check again
        Thread.sleep(10 * 1000);

        // check again
        Mockito.verify(clientCommunicator, Mockito.times(1)).sendMessage(PING_MESSAGE);
    }

    @Test
    public void everythingFine() throws InterruptedException {
        // let's test what happens if two members are alive and well

        // first let's make a fake list with two members
        Member m1 = new Member("Steve", "128.123.123.123", "56001");
        Member m2 = new Member("Paul", "123.13.123.123", "56001");
        Member[] members = {m1, m2};

        // now let's tell clientCommunicator to return this list when requested
        when(clientCommunicator.getMembers()).thenReturn(members, new Member[0]);

        // now run the coordinator
        ClientCoordinator clientCoordinator = new ClientCoordinator(clientCommunicator);
        clientCoordinator.start();

        // let's wait 11 seconds, then tell the coordinator both members are alive
        Thread.sleep(11 * 1000);
        clientCoordinator.confirmedAlive(m1.getUID());
        clientCoordinator.confirmedAlive(m2.getUID());

        // now let's make sure that the clientCommunicator.sendMessage method
        // was never called to communicate a timeout
        Mockito.verify(clientCommunicator, Mockito.times(0))
                .sendMessage(TIMEOUT_MESSAGE + m1.getUID());
        Mockito.verify(clientCommunicator, Mockito.times(0))
                .sendMessage(TIMEOUT_MESSAGE + m2.getUID());
    }

    @Test
    public void oneMemberHasTimedOut() throws InterruptedException {
        // let's test what happens if one of the members times out

        // first let's make a fake list with two members
        Member m1 = new Member("Steve", "128.123.123.123", "56001");
        Member m2 = new Member("Paul", "123.13.123.123", "56001");
        Member[] members = {m1, m2};

        // now let's tell clientCommunicator to return this list when requested
        when(clientCommunicator.getMembers()).thenReturn(members, new Member[0]);

        // now run the coordinator
        ClientCoordinator clientCoordinator = new ClientCoordinator(clientCommunicator);
        clientCoordinator.start();

        // let's wait 11 seconds, then tell the coordinator only m1 is alive
        // then wait 5 more seconds to make sure m2 times out
        Thread.sleep(11 * 1000);
        clientCoordinator.confirmedAlive(m1.getUID());
        Thread.sleep(5 * 1000);

        // now let's make sure that the clientCommunicator.sendMessage method
        // was called once and only once for m2
        Mockito.verify(clientCommunicator, Mockito.times(0))
                .sendMessage(TIMEOUT_MESSAGE + m1.getUID());
        Mockito.verify(clientCommunicator, Mockito.times(1))
                .sendMessage(TIMEOUT_MESSAGE + m2.getUID());
    }

    @Test
    public void bothMembersHaveTimedOut() throws InterruptedException {
        // let's test what happens if both members time out

        // first let's make a fake list with two members
        Member m1 = new Member("Steve", "128.123.123.123", "56001");
        Member m2 = new Member("Paul", "123.13.123.123", "56001");
        Member[] members = {m1, m2};

        // now let's tell clientCommunicator to return this list when requested
        when(clientCommunicator.getMembers()).thenReturn(members, new Member[0]);

        // now run the coordinator
        ClientCoordinator clientCoordinator = new ClientCoordinator(clientCommunicator);
        clientCoordinator.start();

        // let's wait 16 seconds, enough time for the first request to go out and for both members to time out
        Thread.sleep(16 * 1000);

        // now let's make sure that the clientCommunicator.sendMessage method
        // was called once and only once for each of the two members
        Mockito.verify(clientCommunicator, Mockito.times(1))
                .sendMessage(TIMEOUT_MESSAGE + m1.getUID());
        Mockito.verify(clientCommunicator, Mockito.times(1))
                .sendMessage(TIMEOUT_MESSAGE + m2.getUID());
    }

}
