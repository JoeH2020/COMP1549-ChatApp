import junit.framework.Assert;
import org.apache.maven.settings.Server;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.mockito.Mockito.*;

public class ServerInputHandlerFacadeTest {

    private static ServerInputHandlerFacade handler;

    private static ClientCommunicator communicator;

    @BeforeAll
    public static void beforeAll () {
        // mock the communicator
        communicator = Mockito.mock(ClientCommunicator.class);

        // instantiate a input handler with the mock communicator
        handler = new ServerInputHandlerFacade(communicator);
    }

    @BeforeEach
    public void beforeEach() {
        // reset the mock to avoid unexpected results
        reset(communicator);
    }

    @Test
    public void submitName() {
        // make a member
        Member paul = new Member("Paul", null, null);

        // set the communicator to return paul when asked about itself
        when(communicator.getSelf()).thenReturn(paul);

        // now make the handler handle a submit name request
        handler.handleInput("SUBMITNAME");

        // now let's verify that expected behaviour has been carried out
        verify(communicator).sendMessage("Paul");

    }

    @Test
    public void nameAccepted() {
        // send a name accepted message
        handler.handleInput("NAMEACCEPTED");

        // now let's verify that expected behaviour has been carried out
        verify(communicator).sendMessage("VIEWMEMBERS");
    }

    @Test
    public void message() {
        // we need to verify a System.out call, so let's set the default output stream to one we can read
        // first let's save the current system out so we can reset it at the end
        PrintStream originalStream = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        // send a message
        handler.handleInput("MESSAGE1500John:Hello There! :)");

        // make sure the correct message has been printed out
        String expectedOutput = "[15:00] John:Hello There! :)\n";
        Assert.assertEquals(expectedOutput, output.toString());

        // let's reset the System.out stream
        System.setOut(originalStream);
    }

    @Test
    public void setCoordinator() {
        // send a set coordinator message
        handler.handleInput("SETCOORDINATOR");

        // make sure the setCoordinator method has been called on the communicator
        verify(communicator, times(1)).setCoordinator();
    }

    @Test
    public void checkAlive() {
        // let's set up a fake self for the communicator
        Member paul = new Member("Paul", null, null);
        when(communicator.getSelf()).thenReturn(paul);

        // now send a checkalive message
        handler.handleInput("CHECKALIVE");

        // check for expected behaviour
        verify(communicator, times(1)).sendMessage("ALIVEPaul");

    }

    @Test
    public void alive() {
        // set up a mock coordinator
        ClientCoordinator coordinator = Mockito.mock(ClientCoordinator.class);
        when(communicator.getCoordinator()).thenReturn(coordinator);

        // send an alive message
        handler.handleInput("ALIVEJohn");

        // check for correct behaviour
        verify(coordinator, times(1)).confirmedAlive("John");
    }

    @Test
    public void join() {
        // send a join message
        handler.handleInput("JOINJohn");

        // make sure the correct behaviour has been executed
        Member john = new Member("John", null, null);
        verify(communicator, times(1)).addMember(john);
    }

    @Test
    public void timeout() {
        // send a time out message
        handler.handleInput("TIMEOUTJohn");

        // check correct behaviour
        Member john = new Member("John", null, null);
        verify(communicator, times(1)).removeMember(john);
    }

    @Test
    public void disconnected() {
        // send a disconnect message
        handler.handleInput("DISCONNECTEDJohn");

        // check correct behaviour
        Member john = new Member("John", null, null);
        verify(communicator, times(1)).removeMember(john);
    }

    @Test
    public void whisper() {
        // we need to verify a System.out call, so let's set the default output stream to one we can read
        // first let's save the current system out so we can reset it at the end
        PrintStream originalStream = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        // send a message
        handler.handleInput("WHISPER1500John:Hello There! :)");

        // make sure the correct message has been printed out
        String expectedOutput = "[15:00] John whispered: Hello There! :)\n";
        Assert.assertEquals(expectedOutput, output.toString());

        // let's reset the System.out stream
        System.setOut(originalStream);
    }

    @Test
    public void members() {
        // make a members input call
        handler.handleInput("MEMBERSJohn,Paul,Ringo,George");

        // check correct behaviour
        verify(communicator, times(1))
                .addMembers(new String[]{"John", "Paul", "Ringo", "George"});
    }

}
