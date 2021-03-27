import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class UserInputHandlerFacadeTest {
    private static UserInputHandlerFacade handler;

    private static ClientCommunicator communicator;

    @BeforeAll
    public static void beforeAll () {
        // mock the communicator
        communicator = Mockito.mock(ClientCommunicator.class);

        // instantiate a input handler with the mock communicator
        handler = new UserInputHandlerFacade(communicator);
    }

    @BeforeEach
    public void beforeEach() {
        // reset the mock to avoid unexpected results
        reset(communicator);
    }

    @Test
    public void viewMembers() {
        // send a view member input
        handler.handleInput("/VIEWMEMBERS");

        // check correct behaviour
        verify(communicator, times(1)).sendMessage("VIEWMEMBERS");
    }

    @Test
    public void whisper() {
        // send whisper input
        handler.handleInput("/WHISPER John Hey there John! :)");

        // check correct behaviour
        verify(communicator, times(1)).sendMessage("WHISPERJohn:Hey there John! :)");
    }

    @Test
    public void timeout() {
        // send time out input
        handler.handleInput("/TIMEOUT");

        // check correct behaviour
        verify(communicator, times(1)).timeOut();
    }

    @Test
    public void message() {
        // check that all other inputs are sent as message

        // make the communicator return someone when the getSelf method is called
        Member paul = new Member("Paul", null, null);
        when(communicator.getSelf()).thenReturn(paul);

        handler.handleInput("Anyone there???");

        // check correct behaviour
        verify(communicator, times(1)).sendMessage("MESSAGEPaul:Anyone there???");
    }
}
