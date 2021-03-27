import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.HashSet;

import static org.mockito.Mockito.*;

public class ClientInputHandlerFacadeTest {

    private static ClientInputHandlerFacade handler;

    private static ServerThread thread;

    private static ServerSingleton singleton;

    @BeforeAll
    public static void beforeAll() throws NoSuchFieldException, IllegalAccessException {
        // mock the thread
        thread = Mockito.mock(ServerThread.class);

        // we need to mock the singleton as well
        singleton = Mockito.mock(ServerSingleton.class);

        // now we need to make sure the actual singleton class returns this instance when asked
        // there is no way other than reflections to do this (without PowerMockito)
        Class singletonClass = ServerSingleton.class;
        Field instanceField = singletonClass.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, singleton);

        handler = new ClientInputHandlerFacade(thread);
    }

    @BeforeEach
    public void beforeEach() {
        // reset the mock
        reset(thread);

        // and the singleton mock too
        reset(ServerSingleton.getInstance());
    }

    @Test
    public void message() {
        // make the singleton return a set time when asked
        when(singleton.getTime()).thenReturn("1500");

        // call the message input
        handler.handleInput("MESSAGEPaul:Hey There!");

        // verify correct behaviour
        verify(singleton, times(1)).broadcast("MESSAGE1500Paul:Hey There!");
    }

    @Test
    public void viewMembers() {
        // make the singleton return a set of members
        Member paul = new Member("Paul", null, null);
        Member john = new Member("John", null, null);
        HashSet<Member> members = new HashSet<Member>();
        members.add(paul);
        members.add(john);
        when(singleton.getMembers()).thenReturn(members);

        // send a viewmembers message
        handler.handleInput("VIEWMEMBERS");

        // ensure correct behaviour
        verify(thread, times(1)).sendMessage("MEMBERSPaul,John");
    }

    @Test
    public void whisper() {
        // create a fake identity for thread
        Member paul = new Member("Paul", null, null);
        when(thread.getSelf()).thenReturn(paul);

        // send a whisper message
        handler.handleInput("WHISPERJohn:Hey there John! :)");

        // ensure correct behaviour
        verify(singleton, times(1))
                .whisper("John", "Hey there John! :)", "Paul");
    }

    @Test
    public void checkAlive() {
        // send checkalive message
        handler.handleInput("CHECKALIVE");

        // ensure broadcasting
        verify(singleton).broadcast("CHECKALIVE");
    }

    @Test
    public void alive() {
        // we'll need a coordinator for this one
        ServerCoordinator coordinator = Mockito.mock(ServerCoordinator.class);
        when(singleton.getServerCoordinator()).thenReturn(coordinator);
        when(singleton.getCoordinatorOut()).thenReturn(new PrintWriter(System.out));

        // also make the thread say they are the coordinator
        when(thread.isCoordinatorThread()).thenReturn(true);

        // send alive message
        handler.handleInput("ALIVEJohn");

        // ensure correct behaviour
        verify(singleton).getCoordinatorOut();
        verify(coordinator).positiveResponse();

    }

    @Test
    public void timeout() {
        // send timeout message
        handler.handleInput("TIMEOUTJohn");

        // create john
        Member john = new Member("John", null, null);

        //ensure correct behaviour
        verify(singleton).broadcast("TIMEOUTJohn");
        verify(singleton).removeMember(john);
    }
}
